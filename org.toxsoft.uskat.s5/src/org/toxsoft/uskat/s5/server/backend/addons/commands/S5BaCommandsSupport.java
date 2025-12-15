package org.toxsoft.uskat.s5.server.backend.addons.commands;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.s5.server.backend.addons.commands.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.io.*;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.s5.legacy.*;
import org.toxsoft.uskat.s5.server.backend.supports.commands.impl.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Вспомогательный класс обработки комманд {@link IDtoCommand}.
 *
 * @author mvk
 */
public final class S5BaCommandsSupport
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Максимальное количество команд выполняемых в один момент времени, после которого из списка {@link #executingCmds}
   * будут удалены самые старые (с выдачей предупреждения в журнал).
   */
  private static final int EXEC_COMMAND_MAX = 64;

  /**
   * Список идентификаторов команд исполняемых исполнителем.
   * <p>
   * Список возможных {@link Gwid} идентифификаторов приведен в комментарии к методу
   * {@link ISkCommandService#registerExecutor(ISkCommandExecutor, IGwidList)}</code>.
   */
  private final GwidList gwids = new GwidList();

  /**
   * Список идентификаторов {@link IDtoCommand#instanceId()} команд ожидающих выполнения
   */
  private final IStringListEdit executingCmds = new StringArrayList( false );

  /**
   * Блокировка доступа к данным класса. (lazy)
   */
  private volatile transient S5Lockable lock;

  /**
   * Конструктор.
   */
  public S5BaCommandsSupport() {
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает список всех {@link Gwid}-идентификаторов команд которые выполняет исполнитель
   *
   * @return {@link IGwidList} список идентификаторов команд
   */
  public IGwidList getHandledCommandGwids() {
    lockRead( lock() );
    try {
      return new GwidList( gwids );
    }
    finally {
      unlockRead( lock() );
    }
  }

  /**
   * Добавляет идентификатор команды в список команд ожидающих выполнения.
   * <p>
   * Если команда уже добавлена, то ничего не делает
   * <p>
   * Если при добавлении команды будет обнаружено, что очередь команд переполнена, то самая старая команда будет удалена
   * из очереди ожидания.
   *
   * @param aCmdInstanceId String идентификатор команды {@link IDtoCommand#instanceId()}
   * @return {@link ValidationResult} результат добавления команды в очередь
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public ValidationResult addExecutingCmd( String aCmdInstanceId ) {
    TsNullArgumentRtException.checkNull( aCmdInstanceId );
    lockWrite( lock() );
    try {
      executingCmds.add( aCmdInstanceId );
      if( executingCmds.size() > EXEC_COMMAND_MAX ) {
        // Превышение максимального количества одновременно выполняемых команд
        String remoteCmdId = executingCmds.removeByIndex( executingCmds.size() - 1 );
        return ValidationResult.warn( ERR_EXEC_CMD_MAX, Integer.valueOf( EXEC_COMMAND_MAX ), remoteCmdId );
      }
      return ValidationResult.SUCCESS;
    }
    finally {
      unlockWrite( lock() );
    }
  }

  /**
   * Обрабатывает текущие состояния команд, обновляя список команд ожидающих выполнения.
   * <p>
   * Если состояние какой-либо команды определено как "команда завершена", то команда будет удалена из списка команд
   * ожидающих выполнения.
   *
   * @param aStates {@link ITimedList}&lt; {@link DtoCommandStateChangeInfo}&gt; список состояний для фильтрации
   * @return S5CommandStateChangeInfoList список состояний команд целевого fronedn.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5CommandStateChangeInfoList updateExecutingCmds( ITimedList<DtoCommandStateChangeInfo> aStates ) {
    TsNullArgumentRtException.checkNull( aStates );
    S5CommandStateChangeInfoList retValue = new S5CommandStateChangeInfoList();
    lockWrite( lock() );
    try {
      for( DtoCommandStateChangeInfo state : aStates ) {
        String cmdId = state.instanceId();
        if( !executingCmds.hasElem( cmdId ) ) {
          // Завершение команды не ожидается целевым frontend
          continue;
        }
        if( state.state().state().isComplete() ) {
          // Команда завершила свое выполнение
          executingCmds.remove( cmdId );
        }
        retValue.add( state );
      }
      return retValue;
    }
    finally {
      unlockWrite( lock() );
    }
  }

  /**
   * Указывет бекенду, какие команды готов выполнять этот фронтенд.
   * <p>
   * Аргумент заменяет существующий до этого список в бекенде. Пустой список означает, что фронтенд отказываеся от
   * выполнения каких либо команд.
   * <p>
   * Изменения списка не влияет на процесс исполнения текущих выполяемых команд.
   * <p>
   * Описание содержимого и использования списка-аргумента приведено в комментарии к методу
   * <code>ISkCommandService.registerExecutor()</code>.
   *
   * @param aNeededGwids {@link IGwidList} - список GWID-ов интересующих событий
   * @return {@link ValidationResult} результат регистрации исполнителя
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public IVrList setHandledCommandGwids( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    VrList retValue = new VrList();
    lockWrite( lock() );
    try {
      gwids.clear();
      for( Gwid gwid : aNeededGwids ) {
        if( gwids.hasElem( gwid ) ) {
          continue;
        }
        switch( gwid.kind() ) {
          case GW_CLASS:
          case GW_CMD:
            gwids.add( gwid );
            // Регистрация исполнителя команды идентификатором GWID
            retValue.add( info( MSG_REGISTER_CMD_GWID, gwid ) );
            continue;
          case GW_ATTR:
          case GW_CMD_ARG:
          case GW_EVENT:
          case GW_EVENT_PARAM:
          case GW_LINK:
          case GW_RTDATA:
          case GW_CLOB:
          case GW_RIVET:
            // Попытка регистрации исполнителя на команды с игнорируемый типом идентификатора GWID
            retValue.add( warn( ERR_IGNORE_GWID_BY_KIND, gwid ) );
            continue;
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
      return retValue;
    }
    finally {
      unlockWrite( lock() );
    }
  }

  /**
   * Возвращает признак того, что представленная команда может быть исполнена исполнителем
   *
   * @param aHierarchy {@link ISkClassHierarchyExplorer} поставщик информации об иерархии классов
   * @param aCommand {@link IDtoCommand} проверяемая команда {@link IDtoCommand#cmdGwid()}
   * @return boolean <b>true</b> команда исполняется; <b>false</b> команда не исполняется.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public boolean acceptable( ISkClassHierarchyExplorer aHierarchy, IDtoCommand aCommand ) {
    TsNullArgumentRtException.checkNulls( aHierarchy, aCommand );
    // Блокирование доступа на чтение карты обработчиков
    lockRead( lock() );
    try {
      // Формирование карты событий
      for( Gwid gwid : gwids ) {
        if( SkGwidUtils.acceptableCmd( aHierarchy, gwid, aCommand.cmdGwid() ) ) {
          return true;
        }
      }
      return false;
    }
    finally {
      // Разблокирование доступа на чтение карты обработчиков
      unlockRead( lock() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает блокировку класса
   *
   * @return {@link S5Lockable} блокировка класса
   */
  private synchronized S5Lockable lock() {
    if( lock == null ) {
      lock = new S5Lockable();
    }
    return lock;
  }
}
