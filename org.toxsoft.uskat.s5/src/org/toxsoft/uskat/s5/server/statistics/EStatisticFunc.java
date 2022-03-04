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
 * Возможные статистические функции на интервале (шаге)
 *
 * @author mvk
 */
public enum EStatisticFunc
    implements IStridable {

  /**
   * Первое значение.
   */
  FIRST( "first", E_RF_D_FIRST, E_RF_N_FIRST ), //$NON-NLS-1$

  /**
   * Последнее значение.
   */
  LAST( "last", E_RF_D_LAST, E_RF_N_LAST ), //$NON-NLS-1$

  /**
   * Миниммум.
   */
  MIN( "min", E_RF_D_MIN, E_RF_N_MIN ), //$NON-NLS-1$

  /**
   * Максимум.
   */
  MAX( "max", E_RF_D_MAX, E_RF_N_MAX ), //$NON-NLS-1$

  /**
   * Среднее.
   */
  AVERAGE( "average", E_RF_D_AVERAGE, E_RF_N_AVERAGE ), //$NON-NLS-1$

  /**
   * Сумма.
   */
  SUMMA( "summa", E_RF_D_SUMMA, E_RF_N_SUMMA ), //$NON-NLS-1$

  /**
   * Счетчик (количество значений).
   */
  COUNT( "count", E_RF_D_COUNT, E_RF_N_COUNT ); //$NON-NLS-1$

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "EStatisticFunc"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<EStatisticFunc> KEEPER =
      new AbstractEntityKeeper<>( EStatisticFunc.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, EStatisticFunc aEntity ) {
          aSw.writeQuotedString( aEntity.id );
          aSw.writeSeparatorChar();
        }

        @Override
        protected EStatisticFunc doRead( IStrioReader aSr ) {
          EStatisticFunc retValue = EStatisticFunc.findById( aSr.readQuotedString() );
          aSr.ensureSeparatorChar();
          return retValue;
        }
      };

  private static IStridablesListEdit<EStatisticFunc> list = null;
  private final String                               id;
  private final String                               description;
  private final String                               nmName;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое удобовчитаемое название
   */
  EStatisticFunc( String aId, String aDescr, String aName ) {
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
   * @return {@link IStridablesList}&lt; {@link EStatisticFunc} &gt; - список всех констант
   */
  public static IStridablesList<EStatisticFunc> asStridablesList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IList}&lt; {@link EStatisticFunc} &gt; - список всех констант
   */
  public static IList<EStatisticFunc> asList() {
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
   * @return {@link EStatisticFunc} - найденная константа, или null если нет константы с таимк идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EStatisticFunc findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EStatisticFunc item : values() ) {
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
   * @return {@link EStatisticFunc} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EStatisticFunc findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return {@link EStatisticFunc} - найденная константа, или null если нет константы с таким описанием
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EStatisticFunc findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( EStatisticFunc item : values() ) {
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
   * @return {@link EStatisticFunc} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EStatisticFunc findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

  /**
   * Возвращает константу по имени или null.
   *
   * @param aName String - имя искомой константы
   * @return {@link EStatisticFunc} - найденная константа, или null если нет константы с таким именем
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EStatisticFunc findByNameOrNull( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EStatisticFunc item : values() ) {
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
   * @return {@link EStatisticFunc} - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким именем
   */
  public static EStatisticFunc findByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
  }

}
