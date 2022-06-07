package org.toxsoft.uskat.skadmin.cli;

import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.impl.AdminCmdLibraryUtils.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.IAdminCmdArgDef;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContext;

/**
 * Вспомогательные методы консоли
 *
 * @author mvk
 */
public class AdminConsoleUtils {

  /**
   * Возвращает строковую константу цвета для вывода имени plexy-типа
   *
   * @param aType {@link IPlexyType} тип
   * @return String строковая константа цвета
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String getTypeNameColor( IPlexyType aType ) {
    TsNullArgumentRtException.checkNull( aType );
    // Вывод на консоль
    switch( aType.kind() ) {
      case SINGLE_VALUE:
        return COLOR_SINGLE_VALUE;
      case VALUE_LIST:
        return COLOR_VALUE_LIST;
      case OPSET:
        return COLOR_VALUE_LIST;
      case SINGLE_REF:
      case REF_LIST:
        return COLOR_SINGLE_REF;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  /**
   * Возвращает имя plexy-типа для вывода на экран
   *
   * @param aType {@link IPlexyType} тип
   * @return String строковое представление типа
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String getTypeName( IPlexyType aType ) {
    TsNullArgumentRtException.checkNull( aType );
    switch( aType.kind() ) {
      case SINGLE_VALUE:
        return aType.dataType().atomicType().id();
      case VALUE_LIST:
        return "List " + aType.dataType().atomicType().id(); //$NON-NLS-1$
      case OPSET:
        return "OptionSet"; //$NON-NLS-1$
      case SINGLE_REF:
        return aType.refClass().getSimpleName();
      case REF_LIST:
        return "List " + aType.refClass().getSimpleName(); //$NON-NLS-1$
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  /**
   * Дополняет карту значений аргументов недостающими значениями из текущего контекста
   *
   * @param aArgDefs {@link IList}&lt;{@link IAdminCmdArgDef}&gt; список аргументов команды
   * @param aArgValues {@link IStringMapEdit}&lt;{@link IPlexyValue}&gt; список значений команды
   * @param aContextInputs {@link IStringList} список имен параметров контекста определяемых для выполнения команды
   * @param aContext {@link IAdminCmdContext} текущий контекст команд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество фактических значений контекста превышает допустимое в команде
   */
  public static void appendContextValues( IList<IAdminCmdArgDef> aArgDefs, IStringMapEdit<IPlexyValue> aArgValues,
      IStringList aContextInputs, IAdminCmdContext aContext ) {
    TsNullArgumentRtException.checkNulls( aArgDefs, aArgValues, aContextInputs, aContext );
    // Список аргументов определнных как единичные объектные ссылки или их списки
    IList<IAdminCmdArgDef> contextArgDefs = getArgDefs( aArgDefs, true );
    if( aContextInputs.size() == 0 ) {
      // Аргументы объектных ссылок не указаны
      return;
    }
    // Индекс текущего имени фактического параметра контекста
    int inputIndex = 0;
    // Количество созданных параметров
    int createCount = 0;
    for( IAdminCmdArgDef argDef : contextArgDefs ) {
      if( createCount > contextArgDefs.size() ) {
        // Количество введенных параметров больше чем требуется для выполнения команды
        Integer createCountI = Integer.valueOf( createCount );
        Integer allCountI = Integer.valueOf( contextArgDefs.size() );
        throw new TsIllegalArgumentRtException( MSG_ERR_CONTEXT_OVER_INPUT, createCountI, allCountI );
      }
      if( inputIndex == aContextInputs.size() ) {
        // Недостаточное количество введенных параметров для выполнения команды
        break;
      }
      String contextInput = aContextInputs.get( inputIndex );
      EPlexyKind kind = argDef.type().kind();
      if( kind == EPlexyKind.SINGLE_REF ) {
        // Одиночное значение
        IPlexyValue value = aContext.paramValue( contextInput );
        aArgValues.put( argDef.id(), value );
        createCount++;
        inputIndex++;
        continue;
      }
      if( kind != EPlexyKind.REF_LIST ) {
        // Значения обрабатываемые методом могут быть только EPlexyKind.SINGLE_REF и EPlexyKind.REF_LIST
        throw new TsInternalErrorRtException();
      }
      Class<?> refClass = argDef.type().refClass();
      IListEdit<Object> refList = new ElemLinkedList<>();
      for( ; inputIndex < aContextInputs.size(); inputIndex++ ) {
        String paramName = aContextInputs.get( inputIndex );
        IPlexyValue inputValue = aContext.paramValue( paramName );
        if( !inputValue.type().kind().isReference() || !refClass.isAssignableFrom( inputValue.type().refClass() ) ) {
          // Тип значения не соответствует типу значений списка аргумента
          inputIndex--;
          break;
        }
        // Добавляем в результат одиночное значение или список
        refList
            .add( inputValue.type().kind() == EPlexyKind.SINGLE_REF ? inputValue.singleRef() : inputValue.refList() );
      }
      IPlexyValue value = pvRefList( argDef.type(), refList );
      aArgValues.put( argDef.id(), value );
      createCount++;
    }
  }

}
