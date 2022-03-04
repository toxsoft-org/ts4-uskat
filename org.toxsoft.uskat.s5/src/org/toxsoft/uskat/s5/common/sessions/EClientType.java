package org.toxsoft.uskat.s5.common.sessions;

import static org.toxsoft.uskat.s5.common.sessions.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Типы клиентов.
 * <p>
 * Этот класс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный идентификатор типа клиента(ИД-имя);</li>
 * <li><b>nmName</b>() - краткое название типа клиента;</li>
 * <li><b>description</b>() - удобочитаемое описание типа клиента.</li>
 * </ul>
 * <p>
 * TODO: класс экспериментальный, посмотрим что получится
 *
 * @author mvk
 */
public enum EClientType
    implements IStridable {

  /**
   * Неизвестный тип клиента.
   */
  UNKNOWN( "unknown", E_CT_D_UNKNOWN, E_CT_N_UNKNOWN ), //$NON-NLS-1$

  /**
   * Клиент верхнего уровня: Клиент S5RCP.
   */
  S5_RCP( "s5rcp", E_CT_D_RCP, E_CT_N_RCP ), //$NON-NLS-1$

  /**
   * Клиент верхнего уровня: WEB-клиент.<br>
   */
  S5_RAP( "s5rap", E_CT_D_RAP, E_CT_N_RAP ), //$NON-NLS-1$

  /**
   * Клиент нижнего уровня: Шкаф автоматизации
   */
  S5_BOX( "s5box", E_CT_D_BOX, E_CT_N_BOX ), //$NON-NLS-1$

  /**
   * Клиент middle-уровня: S5-сервер
   */
  S5_SERVER( "s5server", E_CT_D_S5SERVER, E_CT_N_S5SERVER ), //$NON-NLS-1$

  /**
   * Клиент администрирования S5-сервера: s5admin
   */
  S5_ADMIN( "s5admin", E_CT_D_S5ADMIN, E_CT_N_S5ADMIN ); //$NON-NLS-1$

  private static IStridablesListEdit<EClientType> list = null;
  private final String                            id;
  private final String                            description;
  private final String                            nmName;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое удобовчитаемое название
   */
  EClientType( String aId, String aDescr, String aName ) {
    id = aId;
    description = aDescr;
    nmName = aName;
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса INameable
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
   * @return {@link IStridablesList}&lt; {@link EClientType} &gt; - список всех констант
   */
  public static IStridablesList<EClientType> asStridablesList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IList}&lt; {@link EClientType} &gt; - список всех констант
   */
  public static IList<EClientType> asList() {
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
   * @return {@link EClientType} - найденная константа, или null если нет константы с таким идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EClientType findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EClientType item : values() ) {
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
   * @return {@link EClientType} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EClientType findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return {@link EClientType} - найденная константа, или null если нет константы с таким описанием
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EClientType findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( EClientType item : values() ) {
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
   * @return {@link EClientType} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EClientType findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

  /**
   * Возвращает константу по имени или null.
   *
   * @param aName String - имя искомой константы
   * @return {@link EClientType} - найденная константа, или null если нет константы с таким именем
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EClientType findByNameOrNull( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EClientType item : values() ) {
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
   * @return {@link EClientType} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким именем
   */
  public static EClientType findByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
  }

}
