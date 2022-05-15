package org.toxsoft.uskat.legacy.plugins.impl;

import static org.toxsoft.uskat.legacy.plugins.impl.ISkResources.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListBasicEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsRuntimeException;
import org.toxsoft.uskat.legacy.plugins.IChangedPluginsInfo;
import org.toxsoft.uskat.legacy.plugins.IPluginInfo;

/**
 * Реализация интерфейса {@link IChangedPluginsInfo}.
 * <p>
 * Результат проверки изменений в перечне JAR-файлов подключаемых модулей в путях поиска.<br>
 * Содержит информацию о новых и удаленных модулях, а также об изменениях версии в существующих модулях.
 * <p>
 * В классе реализован механизм накопления изменений. Механизм реализован через т.н. таблицу переходов. В заголовках
 * строк таблицы состояния предыдущее состояние плагина (до последнего сканирования), в заголовках столбцов - текущее
 * состояние плагина. <br>
 * Состояния могут быть следующими: <br>
 * - плагин добавлен (ДОБ)<br>
 * - плагин изменен (ИЗМ)<br>
 * - плагин удален (УДЛ)<br>
 * Соотвественно таблица переходов выглядит следующим образом: <br>
 *
 * <pre>
 * БЫЛО \ СТАЛО | ДОБ | ИЗМ | УДЛ |
 *        ДОБ   |  X  |  1  |  2  |
 *        ИЗМ   |  X  |  3  |  4  |
 *        УДЛ   |  5  |  X  |  X  |
 * </pre>
 *
 * Где: <br>
 * X - невозможный переход. Например, плагин не может быть добавлен, после того как он уже был добавлен<br>
 * 1 - обновить описание плагина в списке добавленных<br>
 * 2 - удалить описание плагина из списка добавленных, в список удаленных не вносить<br>
 * 3 - обновить описание плагина (удалить старое и добавить новое описание) в списке измененных<br>
 * 4 - удалить описание плагина из списка измененных, добавить в список удаленных<br>
 * 5 - удалить описание плагина из списка удаленных<br>
 *
 * @author Дима
 */
class ChangedPluginsInfo
    implements IChangedPluginsInfo {

  /**
   * Состояние изменений плагина - "Добавлен", используется в классе ChangedModulesInfo, методы: <br>
   * - addAddedPlugin(); <br>
   * - getOldStateDescription() <br>
   */
  private static final String PLUGIN_CHANGES_STATE_ADDED = "Added"; //$NON-NLS-1$

  /**
   * Состояние изменений плагина - "Удален", используется в классе ChangedModulesInfo, методы: <br>
   * - addRemovedPlugin(); <br>
   * - getOldStateDescription() <br>
   */
  private static final String PLUGIN_CHANGES_STATE_REMOVED = "Removed"; //$NON-NLS-1$

  /**
   * Состояние изменений плагина - "Изменен", используется в классе ChangedModulesInfo, методы: <br>
   * - addChangedPlugin(); <br>
   * - getOldStateDescription() <br>
   */
  private static final String PLUGIN_CHANGES_STATE_CHANGED = "Changed"; //$NON-NLS-1$

  /**
   * Состояние изменений плагина - "Неизвестно", используется в классе ChangedModulesInfo, методы: <br>
   * - getOldStateDescription() <br>
   */
  private static final String PLUGIN_CHANGES_STATE_UNKNOWN = "Unknown"; //$NON-NLS-1$

  /**
   * Знак обозначения преехода из одного состояния в таблице переходов в другое, используется в классе
   * ChangedModulesInfo, методы: <br>
   * - addAddedPlugin(); <br>
   * - addRemovedPlugin(); <br>
   * - addChangedPlugin(); <br>
   */
  private static final String STATE_TABLE_GOTO_SIGN = " -> "; //$NON-NLS-1$

  /**
   * Неизменяемая реализация интерфейса {@link IChangedPluginsInfo}.IChangedPluginInfo.
   */
  static class ChangedPluginInfo
      implements IChangedPluginInfo {

    private final IPluginInfo pluginInfo;
    private final TsVersion   oldVersion;

    @Override
    public IPluginInfo pluginInfo() {
      return pluginInfo;
    }

    @Override
    public TsVersion oldVersion() {
      return oldVersion;
    }

    /**
     * Создает описание изменений версии плагина
     *
     * @param aPluginInfo новое описание
     * @param aOldVersion старая версия (до изменений)
     */
    public ChangedPluginInfo( IPluginInfo aPluginInfo, TsVersion aOldVersion ) {
      super();
      this.pluginInfo = aPluginInfo;
      this.oldVersion = aOldVersion;
    }

    // GOGA
    // @Override
    // @SuppressWarnings("nls")
    // public String toString() {
    // return "Old version: " + oldVersion.verMajor() + "." + oldVersion.verMinor()
    // + "\nNew info: " + pluginInfo.toString();
    // }
  }

  /**
   * Список добавленных плагинов
   */
  private IListBasicEdit<IPluginInfo> addedPluginList = new ElemArrayList<>();

  /**
   * Список удаленных плагинов
   */
  private IListBasicEdit<IPluginInfo> removedPluginList = new ElemArrayList<>();

  /**
   * Список удаленных плагинов
   */
  private IListBasicEdit<IChangedPluginInfo> changedPluginList = new ElemArrayList<>();

  /**
   * Пустой конструктор.
   */
  ChangedPluginsInfo() {
    // ничего не делает
  }

  /**
   * Добавить описание добавленного плагина системы.
   *
   * @param aPluginInfo объект типа IPluginInfo
   */
  void addAddedPlugin( IPluginInfo aPluginInfo ) {
    // Оцениваем историю изменнеий описания
    if( !hasHistoryChanges( aPluginInfo ) ) {
      // У него нет истории изменений
      addedPluginList.add( aPluginInfo );
    }
    else {
      if( wasRemoved( aPluginInfo ) ) {
        // по таблице переходов
        action5( aPluginInfo );
      }
      else {
        String currState = PLUGIN_CHANGES_STATE_ADDED;
        String oldState = getOldStateDescription( aPluginInfo );
        throw new TsIllegalStateRtException(
            MSG_ERR_STATE_TABLE_UNSOLVED + ' ' + oldState + STATE_TABLE_GOTO_SIGN + currState );
      }
    }
  }

  /**
   * Возвращает строковое описание прежнего состояния изменений плагина
   *
   * @param aPluginInfo - описание иссчледуемого плагина
   * @return String - одна из конатснт PLUGIN_CHANGES_STATE_XXX
   */
  private String getOldStateDescription( IPluginInfo aPluginInfo ) {
    String retVal = PLUGIN_CHANGES_STATE_UNKNOWN;
    if( wasAdded( aPluginInfo ) ) {
      retVal = PLUGIN_CHANGES_STATE_ADDED;
    }
    else
      if( wasChanged( aPluginInfo ) ) {
        retVal = PLUGIN_CHANGES_STATE_CHANGED;
      }
      else
        if( wasRemoved( aPluginInfo ) ) {
          retVal = PLUGIN_CHANGES_STATE_REMOVED;
        }
    return retVal;
  }

  /**
   * @param aPluginInfo IPluginInfo - описание плагина
   * @return true, если данное описание имеет историю изменений
   */
  private boolean hasHistoryChanges( IPluginInfo aPluginInfo ) {
    return wasAdded( aPluginInfo ) || wasRemoved( aPluginInfo ) || wasChanged( aPluginInfo );
  }

  /**
   * Добавить описание удаленного плагина системы.
   *
   * @param aPluginInfo объект типа IPluginInfo
   */
  public void addRemovedPlugin( IPluginInfo aPluginInfo ) {
    if( !hasHistoryChanges( aPluginInfo ) ) {
      removedPluginList.add( aPluginInfo );
    }
    else {
      // Выясняем прежнее состяние описания
      if( wasAdded( aPluginInfo ) ) {
        action2( aPluginInfo );
      }
      else
        if( wasChanged( aPluginInfo ) ) {
          action4( aPluginInfo );
        }
        else {
          String currState = PLUGIN_CHANGES_STATE_REMOVED;
          String oldState = getOldStateDescription( aPluginInfo );
          throw new TsIllegalStateRtException(
              MSG_ERR_STATE_TABLE_UNSOLVED + ' ' + oldState + STATE_TABLE_GOTO_SIGN + currState );
        }
    }
  }

  /**
   * Сравнивает два описания плагинов на эквивалентность.
   *
   * @param aPluginInfo1 объект типа IPluginInfo
   * @param aPluginInfo2 объект типа IPluginInfo
   * @return true, если два описания эквивалентны
   */
  private static boolean isEqualPlugins( IPluginInfo aPluginInfo1, IPluginInfo aPluginInfo2 ) {
    if( !isSamePlugins( aPluginInfo1, aPluginInfo2 ) ) {
      return false;
    }
    if( (aPluginInfo1.pluginVersion().verMajor() != aPluginInfo2.pluginVersion().verMajor())
        || aPluginInfo1.pluginVersion().verMinor() != aPluginInfo2.pluginVersion().verMinor() ) {
      return false;
    }
    return true;
  }

  /**
   * Сравнивает описания плагинов НЕ проверяя номера версий
   *
   * @param aPluginInfo1 объект типа IPluginInfo
   * @param aPluginInfo2 объект типа IPluginInfo
   * @return true, если два описания одинаковые
   */
  private static boolean isSamePlugins( IPluginInfo aPluginInfo1, IPluginInfo aPluginInfo2 ) {
    if( aPluginInfo1.pluginClassName().compareTo( aPluginInfo2.pluginClassName() ) != 0 ) {
      return false;
    }
    if( aPluginInfo1.pluginId().compareTo( aPluginInfo2.pluginId() ) != 0 ) {
      return false;
    }
    if( aPluginInfo1.pluginJarFileName().compareTo( aPluginInfo2.pluginJarFileName() ) != 0 ) {
      return false;
    }
    if( aPluginInfo1.pluginType().compareTo( aPluginInfo2.pluginType() ) != 0 ) {
      return false;
    }
    return true;
  }

  /**
   * Добавить описание измененного плагина системы.
   *
   * @param aChangedPluginInfo объект типа IChangedPluginInfo
   * @throws TsRuntimeException внутренняя ошибка ???
   */
  public void addChangedPlugin( IChangedPluginInfo aChangedPluginInfo )
      throws TsRuntimeException {
    // Оцениваем историю изменений описания
    if( !hasHistoreChanges( aChangedPluginInfo ) ) {
      // У него нет истории изменений
      changedPluginList.add( aChangedPluginInfo );
    }
    else {
      if( wasAdded( aChangedPluginInfo ) ) {
        // по таблице переходов
        action1( aChangedPluginInfo );
      }
      else
        if( wasChanged( aChangedPluginInfo ) ) {
          action3( aChangedPluginInfo );
        }
        else {
          String currState = PLUGIN_CHANGES_STATE_CHANGED;
          String oldState = getOldStateDescription( getPreviousInfo( aChangedPluginInfo ) );
          throw new TsIllegalStateRtException(
              MSG_ERR_STATE_TABLE_UNSOLVED + ' ' + oldState + STATE_TABLE_GOTO_SIGN + currState );
        }
    }
  }

  /**
   * Выясняет наличие данного описания в списках прежних изменений
   *
   * @param aChangedPluginInfo - описание изменившегося плагина
   * @return <b>true</b> - данное описание уже "засветилось" ранее в изменениях;<br>
   *         <b>false</b> - этот плагин не менялся.
   */
  private boolean hasHistoreChanges( IChangedPluginInfo aChangedPluginInfo ) {
    IPluginInfo oldPluginInfo = getPreviousInfo( aChangedPluginInfo );
    return wasAdded( oldPluginInfo ) || wasRemoved( oldPluginInfo ) || wasChanged( oldPluginInfo );
  }

  /**
   * _______________________________________________________________<br>
   * Вспомогательные методы для реализации алгоритма таблицы переходов<br>
   * _______________________________________________________________<br>
   */
  /**
   * @param aChangedPluginInfo объект типа IChangedPluginInfo
   * @return true, если прежнее состояние описания плагина было "ДОБАВЛЕН"
   */
  private boolean wasAdded( IChangedPluginInfo aChangedPluginInfo ) {
    // Восстанавливаем старое описание
    IPluginInfo oldPluginInfo = getPreviousInfo( aChangedPluginInfo );
    for( IPluginInfo addedPluginInfo : addedPluginList ) {
      if( isEqualPlugins( oldPluginInfo, addedPluginInfo ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param aPluginInfo объект типа IPluginInfo
   * @return true, если прежнее состояние описания плагина было "ДОБАВЛЕН"
   */
  private boolean wasAdded( IPluginInfo aPluginInfo ) {
    for( IPluginInfo addedPluginInfo : addedPluginList ) {
      if( isEqualPlugins( aPluginInfo, addedPluginInfo ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param aChangedPluginInfo объект типа IChangedPluginInfo
   * @return true, если прежнее состояние описания плагина было "ИЗМЕНЕН"
   */
  private boolean wasChanged( IChangedPluginInfo aChangedPluginInfo ) {
    // Восстанавливаем старое описание
    IPluginInfo oldPluginInfo = getPreviousInfo( aChangedPluginInfo );
    for( IChangedPluginInfo changedPluginInfo : changedPluginList ) {
      if( isEqualPlugins( oldPluginInfo, changedPluginInfo.pluginInfo() ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param aPluginInfo объект типа IPluginInfo
   * @return true, если прежнее состояние описания плагина было "ИЗМЕНЕН"
   */
  private boolean wasChanged( IPluginInfo aPluginInfo ) {
    for( IChangedPluginInfo changedPluginInfo : changedPluginList ) {
      if( isEqualPlugins( aPluginInfo, changedPluginInfo.pluginInfo() ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param aPluginInfo объект типа IPluginInfo
   * @return true, если прежнее состояние описания плагина было "УДАЛЕН"
   */
  private boolean wasRemoved( IPluginInfo aPluginInfo ) {
    for( IPluginInfo removedPluginInfo : removedPluginList ) {
      if( isEqualPlugins( aPluginInfo, removedPluginInfo ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Обработка перехода "Добавлен" -> "Изменен" (см. таблицу переходов). <br>
   * 1 - обновить описание плагина в списке добавленных<br>
   *
   * @param aChangedPluginInfo IChangedPluginInfo
   */
  private void action1( IChangedPluginInfo aChangedPluginInfo ) {
    IPluginInfo oldPluginInfo = getPreviousInfo( aChangedPluginInfo );
    for( IPluginInfo currPluginInfo : addedPluginList ) {
      if( isEqualPlugins( oldPluginInfo, currPluginInfo ) ) {
        addedPluginList.remove( currPluginInfo );
        addedPluginList.add( aChangedPluginInfo.pluginInfo() );
        break;
      }
    }
  }

  /**
   * Обработка перехода "Добавлен" -> "Удален" (см. таблицу переходов). <br>
   * 2 - удалить описание плагина из списка добавленных, в список удаленных не вносить<br>
   *
   * @param aPluginInfo IPluginInfo
   */
  private void action2( IPluginInfo aPluginInfo ) {
    for( IPluginInfo currPluginInfo : addedPluginList ) {
      if( isEqualPlugins( aPluginInfo, currPluginInfo ) ) {
        addedPluginList.remove( currPluginInfo );
      }
    }
  }

  /**
   * Обработка перехода "Изменен" -> "Изменен" (см. таблицу переходов). <br>
   * 3 - обновить описание плагина (удалить старое и добавить новое описание) в списке измененных<br>
   *
   * @param aChangedPluginInfo IChangedPluginInfo
   */
  private void action3( IChangedPluginInfo aChangedPluginInfo ) {
    IPluginInfo oldPluginInfo = getPreviousInfo( aChangedPluginInfo );
    for( IChangedPluginInfo changedPluginInfo : changedPluginList ) {
      if( isEqualPlugins( oldPluginInfo, changedPluginInfo.pluginInfo() ) ) {
        changedPluginList.remove( changedPluginInfo );
        changedPluginList.add( aChangedPluginInfo );
      }
    }
  }

  /**
   * Обработка перехода "Изменен" -> "Удален" (см. таблицу переходов). <br>
   * 4 - удалить описание плагина из списка измененных, добавить в список удаленных<br>
   *
   * @param aPluginInfo IChangedPluginInfo
   */
  private void action4( IPluginInfo aPluginInfo ) {
    for( IChangedPluginInfo changedPluginInfo : changedPluginList ) {
      if( isEqualPlugins( aPluginInfo, changedPluginInfo.pluginInfo() ) ) {
        changedPluginList.remove( changedPluginInfo );
        removedPluginList.add( aPluginInfo );
      }
    }
  }

  /**
   * Обработка перехода "Удален" -> "Добавлен" (см. таблицу переходов). <br>
   * 5 - удалить описание плагина из списка удаленных<br>
   *
   * @param aPluginInfo IPluginInfo
   */
  private void action5( IPluginInfo aPluginInfo ) {
    for( IPluginInfo removedPluginInfo : removedPluginList ) {
      if( isEqualPlugins( aPluginInfo, removedPluginInfo ) ) {
        removedPluginList.remove( removedPluginInfo );
      }
    }
  }

  /**
   * Возвращает описание аналогичное состоянию до измения по информации из IChangedPluginInfo
   *
   * @param aChangedPluginInfo описание изменения
   * @return объект типа DefaultTsPluginInfo по состоянию до изменения описанного в aChangedPluginInfo
   */
  private static IPluginInfo getPreviousInfo( IChangedPluginInfo aChangedPluginInfo ) {
    PluginInfo prevPluginInfo =
        new PluginInfo( (PluginInfo)aChangedPluginInfo.pluginInfo(), aChangedPluginInfo.oldVersion() );
    return prevPluginInfo;
  }

  /**
   * Создает описание изменений версии плагина
   *
   * @param aPluginInfo новое описание
   * @param aOldVersion старая версия (до изменений)
   * @return объект типа IChangedPluginInfo
   */
  public IChangedPluginInfo createChangedPluginInfo( IPluginInfo aPluginInfo, TsVersion aOldVersion ) {
    return new ChangedPluginInfo( aPluginInfo, aOldVersion );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IChangedPluginsInfo
  //

  @Override
  public boolean isChanges() {
    return (addedPluginList.size() > 0 || removedPluginList.size() > 0 || changedPluginList.size() > 0);
  }

  @Override
  public IList<IPluginInfo> listAddedPlugins() {
    return addedPluginList;
  }

  @Override
  public IList<IPluginInfo> listRemovedPlugins() {
    return removedPluginList;
  }

  @Override
  public IList<IChangedPluginInfo> listChangedPlugins() {
    return changedPluginList;
  }
}
