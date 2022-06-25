package org.toxsoft.uskat.s5.server.transactions;

import static org.toxsoft.uskat.s5.server.transactions.IS5Resources.*;

import java.util.List;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimedListEdit;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceWriteStat;

/**
 * Известные, общедоступные ресурсы транзакции.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public enum ES5TransactionResources
    implements IStridable {

  /**
   * Список объектов удаляемого класса
   * <p>
   * Тип: {@link IList}&lt;{@link IDtoObject}&gt; список объектов
   */
  TX_REMOVED_CLASS_OBJS( "removedClassObjs", E_TR_D_REMOVED_CLASS_OBJS, E_TR_N_REMOVED_CLASS_OBJS, IList.class ),

  /**
   * Список объектов изменяющихся классов
   * <p>
   * Тип: {@link IList}&lt;{@link IDtoObject}&gt; список объектов
   */
  TX_UPDATED_CLASS_OBJS( "updatedClassObjs", E_TR_D_UPDATED_CLASS_OBJS, E_TR_N_UPDATED_CLASS_OBJS, IList.class ),

  /**
   * Идентификатор ресурса транзакции: список объектов у которых изменяется тип атрибутов
   * <p>
   * Тип: {@link IList}&lt;{@link IDtoObject}&gt; список объектов
   */
  TX_UPDATED_OBJS_BY_ATTR_TYPE( "updatedObjsByAttrType", E_TR_D_UPDATED_OBJS_BY_ATTR_TYPE,
      E_TR_N_UPDATED_OBJS_BY_ATTR_TYPE, IList.class ),

  /**
   * Идентификатор ресурса транзакции: список объектов у которых изменяется тип текущих данных
   * <p>
   * Тип: {@link IList}&lt;{@link IDtoObject}&gt; список объектов
   */
  TX_UPDATED_OBJS_BY_CURRDATA_TYPE( "updatedObjsByCurrdataType", E_TR_D_UPDATED_OBJS_BY_CURRDATA_TYPE,
      E_TR_N_UPDATED_OBJS_BY_CURRDATA_TYPE, IList.class ),

  /**
   * Идентификатор ресурса транзакции: список описаний атрибутов добавляемых в класс
   * <p>
   * Тип: {@link IStridablesList}&lt;{@link IDtoAttrInfo}&gt; список описаний атрибутов
   */
  TX_ADDED_ATTRS( "addedClassAttrs", E_TR_D_ADDED_ATTRS, E_TR_N_ADDED_ATTRS, IStridablesList.class ),

  /**
   * Идентификатор ресурса транзакции: список описаний атрибутов удаляемых из класса
   * <p>
   * Тип: {@link IStridablesList}&lt;{@link IDtoAttrInfo}&gt; список описаний атрибутов
   */
  TX_REMOVED_ATTRS( "removedClassAttrs", E_TR_D_REMOVED_ATTRS, E_TR_N_REMOVED_ATTRS, IStridablesList.class ),

  /**
   * Идентификатор ресурса транзакции: список описаний текущих данных добавляемых в класс
   * <p>
   * Тип: {@link IStridablesList}&lt;{@link IDtoRtdataInfo}&gt; список описаний данных
   */
  TX_ADDED_CURRDATA( "addedClassCurrdata", E_TR_D_ADDED_CURRDATA, E_TR_N_ADDED_CURRDATA, IStridablesList.class ),

  /**
   * Идентификатор ресурса транзакции: список описаний текущих данных удаляемых из класса
   * <p>
   * Тип: {@link IStridablesList}&lt;{@link IDtoRtdataInfo}&gt; список описаний данных
   */
  TX_REMOVED_CURRDATA( "removedClassCurrdata", E_TR_D_REMOVED_CURRDATA, E_TR_N_REMOVED_CURRDATA,
      IStridablesList.class ),

  /**
   * Идентификатор ресурса транзакции: список объектов меняющих реализацию хранения
   * <p>
   * Тип: {@link IList}&lt;{@link IDtoObject}&gt; список объектов
   */
  TX_UPDATED_OBJS_BY_CHANGE_IMPL( "updatedObjsByChangeImpl", E_TR_D_UPDATED_OBJS_BY_CHANGE_IMPL,
      E_TR_N_UPDATED_OBJS_BY_CHANGE_IMPL, IList.class ),

  /**
   * Идентификатор ресурса транзакции: список ПРЯМЫХ связей объектов меняющих реализацию хранения
   * <p>
   * Тип: {@link List}&lt;{@link IDtoLinkFwd}&gt; список связей
   */
  TX_UPDATED_FWD_LINKS_BY_CHANGE_IMPL( "updatedFwdLinksByChangeImpl", E_TR_D_UPDATED_FWD_LINKS_BY_CHANGE_IMPL,
      E_TR_N_UPDATED_FWD_LINKS_BY_CHANGE_IMPL, List.class ),

  /**
   * Идентификатор ресурса транзакции: список ОБРАТНЫХ связей объектов меняющих реализацию хранения
   * <p>
   * Тип: {@link List}&lt;{@link IDtoLinkFwd}&gt; список связей
   */
  TX_UPDATED_REV_LINKS_BY_CHANGE_IMPL( "updatedRevLinksByChangeImpl", E_TR_D_UPDATED_REV_LINKS_BY_CHANGE_IMPL,
      E_TR_N_UPDATED_REV_LINKS_BY_CHANGE_IMPL, List.class ),

  /**
   * Идентификатор ресурса транзакции: список идентификаторов данных блокирующих в текущей транзакции запись
   * последовательности их значений ({@link IS5Sequence})
   * <p>
   * Тип: {@link IListEdit}&lt;{@link Gwid}&gt; редактируемый (потокобезопасный) список идентификаторов данных
   */
  TX_SEQUENCE_LOCKED_GWIDS( "sequenceLockedGwids", E_TR_D_SEQUENCE_LOCKED_GWIDS, E_TR_N_SEQUENCE_LOCKED_GWIDS,
      IListEdit.class ),

  /**
   * Идентификатор ресурса транзакции: статистика записи последовательности значений ({@link IS5Sequence}) в текущей
   * транзакции
   * <p>
   * Тип: {@link S5SequenceWriteStat} статистика записи значений последовательности
   */
  TX_SEQUENCE_WRITE_STAT( "sequenceWriteStat", E_TR_D_SEQUENCE_WRITE_STAT, E_TR_N_SEQUENCE_WRITE_STAT,
      IListEdit.class ),

  /**
   * Идентификатор ресурса транзакции: карта событий сформированных в транзакции
   * <p>
   * Тип: {@link IMapEdit}&lt;{@link IS5FrontendRear},{@link ITimedListEdit}&lt;{@link SkEvent}&gt; карта событий
   * сформированных в транзакции <br>
   * Ключ: frontend сформировавший события;<br>
   * Значение: список сформированных событий
   */
  TX_FIRED_EVENTS( "firedEvents", E_TR_D_FIRED_EVENTS, E_TR_N_FIRED_EVENTS, IMapEdit.class ),

  /**
   * Идентификатор ресурса транзакции: предыдущая конфигурация поддержки расширения бекенда сохраненная в транзакции
   * <p>
   * Тип: {@link IOptionSet} предыдущая конфигурация синглетона поддержки расширения бекенда
   */
  TX_SAVE_SUPPORT_PREV_CONFIG( "saveSupportPrevConfig", E_TR_D_SAVE_SUPPORT_PREV_CONFIG,
      E_TR_N_SAVE_SUPPORT_PREV_CONFIG, IOptionSet.class ),

  /**
   * Идентификатор ресурса транзакции: новая конфигурация поддержки расширения бекенда сохраненная в транзакции
   * <p>
   * Тип: {@link IOptionSet} новая конфигурация синглетона поддержки расширения бекенда
   */
  TX_SAVE_SUPPORT_NEW_CONFIG( "saveSupportNewConfig", E_TR_D_SAVE_SUPPORT_NEW_CONFIG, E_TR_N_SAVE_SUPPORT_NEW_CONFIG,
      IOptionSet.class ),

  ;

  private static IStridablesListEdit<ES5TransactionResources> list = null;
  private final String                                        id;
  private final String                                        description;
  private final String                                        nmName;
  private final Class<?>                                      type;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое удобовчитаемое название
   * @param aType тип ресурса
   */
  ES5TransactionResources( String aId, String aDescr, String aName, Class<?> aType ) {
    id = aId;
    description = aDescr;
    nmName = aName;
    type = aType;
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public String nmName() {
    return nmName;
  }

  // ------------------------------------------------------------------------------------
  // Дополнительное API
  //

  /**
   * Возвращает тип ресурса
   *
   * @return Class - тип ресурса
   */
  public Class<?> type() {
    return type;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методв класса Object
  //

  @Override
  public String toString() {
    return nmName;
  }

  // ------------------------------------------------------------------------------------
  // API класса
  //

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IStridablesList}&lt; {@link ES5TransactionResources} &gt; - список всех констант
   */
  public static IStridablesList<ES5TransactionResources> asStridablesList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IList}&lt; {@link ES5TransactionResources} &gt; - список всех констант
   */
  public static IList<ES5TransactionResources> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list.values();
  }

  // ----------------------------------------------------------------------------------
  // Методы проверки
  //

  /**
   * Определяет, существует ли константа перечисления с заданным идентификатором.
   *
   * @param aId String - идентификатор искомой константы
   * @return boolean - признак существования константы <br>
   *         <b>true</b> - константа с заданным идентификатором существует;<br>
   *         <b>false</b> - неет константы с таким идентификатором.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemById( String aId ) {
    return findByIdOrNull( aId ) != null;
  }

  /**
   * Определяет, существует ли константа перечисления с заданным описанием.
   *
   * @param aDescription String - описание искомой константы
   * @return boolean - признак существования константы <br>
   *         <b>true</b> - константа с заданным описанием существует;<br>
   *         <b>false</b> - неет константы с таким описанием.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemByDescription( String aDescription ) {
    return findByDescriptionOrNull( aDescription ) != null;
  }

  /**
   * Определяет, существует ли константа перечисления с заданным именем.
   *
   * @param aName String - имя (название) искомой константы
   * @return boolean - признак существования константы <br>
   *         <b>true</b> - константа с заданным именем существует;<br>
   *         <b>false</b> - неет константы с таким именем.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemByName( String aName ) {
    return findByNameOrNull( aName ) != null;
  }

  // ----------------------------------------------------------------------------------
  // Методы поиска
  //
  /**
   * Возвращает константу по идентификатору или null.
   *
   * @param aId String - идентификатор искомой константы
   * @return ES5TransactionResources - найденная константа, или null если нет константы с таимк идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ES5TransactionResources findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( ES5TransactionResources item : values() ) {
      if( item.id.equals( aId ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает константу по идентификатору или выбрасывает исключение.
   *
   * @param aId String - идентификатор искомой константы
   * @return ES5TransactionResources - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static ES5TransactionResources findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return ES5TransactionResources - найденная константа, или null если нет константы с таким описанием
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ES5TransactionResources findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( ES5TransactionResources item : values() ) {
      if( item.description.equals( aDescription ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает константу по описанию или выбрасывает исключение.
   *
   * @param aDescription String - описание искомой константы
   * @return ES5TransactionResources - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static ES5TransactionResources findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

  /**
   * Возвращает константу по имени или null.
   *
   * @param aName String - имя искомой константы
   * @return ES5TransactionResources - найденная константа, или null если нет константы с таким именем
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ES5TransactionResources findByNameOrNull( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( ES5TransactionResources item : values() ) {
      if( item.nmName.equals( aName ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает константу по имени или выбрасывает исключение.
   *
   * @param aName String - имя искомой константы
   * @return ES5TransactionResources - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким именем
   */
  public static ES5TransactionResources findByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
  }
}
