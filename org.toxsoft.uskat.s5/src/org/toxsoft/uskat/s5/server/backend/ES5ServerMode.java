package org.toxsoft.uskat.s5.server.backend;

import static org.toxsoft.uskat.s5.server.backend.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Режимы работы сервера
 *
 * @author mvk
 */
public enum ES5ServerMode
    implements IStridable {

  /**
   * Сервер находится в состоянии запуска.
   * <p>
   * Это первое состояние (то есть, начало жизненного цикла сервера). Сервер находится в этом состоянии некоторое время
   * (задается конфигурацией), после этого переключается в другой режим в зависимости от текущей загруженности.
   */
  STARTING( "Starting", STR_D_STARTING, STR_N_STARTING ), //$NON-NLS-1$

  /**
   * Сервер находится в состоянии работы.
   * <p>
   * В рабочем режиме сервер полностью функционален.
   */
  WORKING( "Working", STR_D_WORKING, STR_N_WORKING ), //$NON-NLS-1$

  /**
   * Сервер находится в режиме усиления (форсажа).
   * <p>
   * В режиме усиления сервер откладывает операции которые могут быть выполнены позже, при переходе в режим
   * {@link #WORKING} или {@link #SHUTDOWNING}.
   */
  BOOSTED( "Boost", STR_D_BOOSTED, STR_N_BOOSTED ), //$NON-NLS-1$

  /**
   * Сервер находится в режиме перегрузки (форсажа).
   * <p>
   * В режиме перегрузки сервер откладывает операции которые могут быть выполнены позже, при переходе в режим
   * {@link #WORKING} или {@link #SHUTDOWNING}.
   * <p>
   * Операции которые не могут быть перенесены отменяются. Формируются отказы выполнять запросы клиентов частично или
   * полностью. Возможна потеря данных.
   */
  OVERLOADED( "Overloaded", STR_D_OVERLOADED, STR_N_OVERLOADED ), //$NON-NLS-1$

  /**
   * Сервер находится в состоянии завершения работы.
   */
  SHUTDOWNING( "Shutdowning", STR_D_SHUTDOWNING, STR_N_SHUTDOWNING ), //$NON-NLS-1$

  /**
   * Сервер завершил свою работу.
   * <p>
   * Это терминальное состояние (то есть, жизненный цикл сервера завершен).
   */
  OFF( "Shutdown", STR_D_SHUTDOWN, STR_N_SHUTDOWN ); //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<ES5ServerMode> KEEPER = new StridableEnumKeeper<>( ES5ServerMode.class );

  private static IStridablesList<ES5ServerMode> list = null;

  private final String id;
  private final String nmName;
  private final String description;

  /**
   * Constructor.
   *
   * @param aId String - identifier (IDpath)
   * @param aName - short name
   * @param aDescription String - description
   */
  ES5ServerMode( String aId, String aName, String aDescription ) {
    id = aId;
    nmName = aName;
    description = aDescription;
  }

  // --------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return nmName;
  }

  @Override
  public String description() {
    return description;
  }

  // ----------------------------------------------------------------------------------
  // Additional API
  //

  /**
   * Returns all constants as list.
   *
   * @return {@link IStridablesList}&lt;{@link ES5ServerMode}&gt; - list of all constants
   */
  public static IStridablesList<ES5ServerMode> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  // ----------------------------------------------------------------------------------
  // Find and get
  //

  /**
   * Finds the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ES5ServerMode} - found constant or <code>null</code> there is no constant with specified identifier
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ES5ServerMode findById( String aId ) {
    return asList().findByKey( aId );
  }

  /**
   * Returns the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ES5ServerMode} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no constant with specified identifier
   */
  public static ES5ServerMode getById( String aId ) {
    return asList().getByKey( aId );
  }

}
