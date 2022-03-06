package org.toxsoft.uskat.s5.server.statistics;

import static org.toxsoft.uskat.s5.server.statistics.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Интервал формирования статистических данных.
 * <p>
 * Этот класс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный идентификатор интервала(ИД-имя);</li>
 * <li><b>nmName</b>() - краткое название интервала;</li>
 * <li><b>description</b>() - удобочитаемое описание интервала.</li>
 * </ul>
 * <p>
 * TODO: класс экспериментальный, посмотрим что получится
 *
 * @author mvk
 */
public enum EStatisticInterval
    implements IS5StatisticInterval {

  /**
   * За весь период работы.
   */
  ALL( "all", E_SI_D_ALL, E_SI_N_ALL, -1 ), //$NON-NLS-1$

  /**
   * За последнюю секунду работы.
   */
  SECOND( "sec", E_SI_D_SECOND, E_SI_N_SECOND, 1000 ), //$NON-NLS-1$

  /**
   * За последнюю минуту работы.
   */
  MINUTE( "min", E_SI_D_MINUTE, E_SI_N_MINUTE, 60 * 1000 ), //$NON-NLS-1$

  /**
   * За последний час работы.
   */
  HOUR( "hour", E_SI_D_HOUR, E_SI_N_HOUR, 60 * 60 * 1000 ), //$NON-NLS-1$

  /**
   * За последние сутки работы
   */
  DAY( "day", E_SI_D_DAY, E_SI_N_HOUR, 24 * 60 * 60 * 1000 ), //$NON-NLS-1$

  /**
   * За последние сутки работы
   */
  MONTH( "month", E_SI_D_MONTH, E_SI_N_MONTH, 30 * 24 * 60 * 60 * 1000 ), //$NON-NLS-1$

  /**
   * За последний год работы
   */
  YEAR( "year", E_SI_D_YEAR, E_SI_N_YEAR, 365 * 24 * 60 * 60 * 1000 ); //$NON-NLS-1$

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "EStatisticInterval"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<IS5StatisticInterval> KEEPER =
      new AbstractEntityKeeper<>( IS5StatisticInterval.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IS5StatisticInterval aEntity ) {
          aSw.writeQuotedString( aEntity.id() );
          aSw.writeSeparatorChar();
        }

        @Override
        protected IS5StatisticInterval doRead( IStrioReader aSr ) {
          EStatisticInterval retValue = EStatisticInterval.findById( aSr.readQuotedString() );
          aSr.ensureSeparatorChar();
          return retValue;
        }
      };

  private static IStridablesListEdit<EStatisticInterval> list = null;
  private final String                                   id;
  private final String                                   description;
  private final String                                   nmName;
  private final int                                      milli;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое удобовчитаемое название
   * @param aMilli int - время интервала в мсек. <= 0 - за весь период работы
   */
  EStatisticInterval( String aId, String aDescr, String aName, int aMilli ) {
    id = aId;
    description = aDescr;
    nmName = aName;
    milli = aMilli;
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
  // IS5StatisticInterval
  //
  @Override
  public int milli() {
    return milli;
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
   * @return {@link IStridablesList}&lt; {@link EStatisticInterval} &gt; - список всех констант
   */
  public static IStridablesList<EStatisticInterval> asStridablesList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IList}&lt; {@link EStatisticInterval} &gt; - список всех констант
   */
  public static IList<EStatisticInterval> asList() {
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
   * @return {@link EStatisticInterval} - найденная константа, или null если нет константы с таким идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EStatisticInterval findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EStatisticInterval item : values() ) {
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
   * @return {@link EStatisticInterval} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EStatisticInterval findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return {@link EStatisticInterval} - найденная константа, или null если нет константы с таким описанием
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EStatisticInterval findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( EStatisticInterval item : values() ) {
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
   * @return {@link EStatisticInterval} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EStatisticInterval findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

  /**
   * Возвращает константу по имени или null.
   *
   * @param aName String - имя искомой константы
   * @return {@link EStatisticInterval} - найденная константа, или null если нет константы с таким именем
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EStatisticInterval findByNameOrNull( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EStatisticInterval item : values() ) {
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
   * @return {@link EStatisticInterval} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким именем
   */
  public static EStatisticInterval findByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
  }

}
