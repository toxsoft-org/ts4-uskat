package org.toxsoft.uskat.s5.client.remote.connection;

import static org.toxsoft.uskat.s5.client.remote.connection.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Типы состояния связи с s5-сервером.
 * <p>
 * Этот класс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный идентификатор типа состояния(ИД-имя);</li>
 * <li><b>nmName</b>() - краткое название типа состояния;</li>
 * <li><b>description</b>() - удобочитаемое описание типа состояния.</li>
 * </ul>
 *
 * @author mvk
 */
public enum EConnectionState
    implements IStridable {

  /**
   * Связь с сервером не установлена.
   */
  DISCONNECTED( "disconnected", D_DISCONNECTED, N_DISCONNECTED ), //$NON-NLS-1$

  /**
   * Связь вообще-то установлена, но в данный момент ее нет, будет автоматически восттановлена при первой же возможности
   */
  INACTIVE( "inactive", D_INACTIVE, N_INACTIVE ), //$NON-NLS-1$

  /**
   * Связь установлена и имеется на данный момент.
   */
  CONNECTED( "connected", D_CONNECTED, N_CONNECTED ); //$NON-NLS-1$

  private static IStridablesListEdit<EConnectionState> list = null;
  private final String                                 id;
  private final String                                 description;
  private final String                                 nmName;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое удобовчитаемое название
   */
  EConnectionState( String aId, String aDescr, String aName ) {
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
   * @return {@link IStridablesList}&lt; {@link EConnectionState} &gt; - список всех констант
   */
  public static IStridablesList<EConnectionState> asStridablesList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IList}&lt; {@link EConnectionState} &gt; - список всех констант
   */
  public static IList<EConnectionState> asList() {
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
   * @return {@link EConnectionState} - найденная константа, или null если нет константы с таким идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EConnectionState findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EConnectionState item : values() ) {
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
   * @return {@link EConnectionState} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EConnectionState findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return {@link EConnectionState} - найденная константа, или null если нет константы с таким описанием
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EConnectionState findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( EConnectionState item : values() ) {
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
   * @return {@link EConnectionState} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EConnectionState findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

  /**
   * Возвращает константу по имени или null.
   *
   * @param aName String - имя искомой константы
   * @return {@link EConnectionState} - найденная константа, или null если нет константы с таким именем
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EConnectionState findByNameOrNull( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EConnectionState item : values() ) {
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
   * @return {@link EConnectionState} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким именем
   */
  public static EConnectionState findByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
  }

}
