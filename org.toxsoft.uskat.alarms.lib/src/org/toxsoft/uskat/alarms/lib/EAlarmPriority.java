package org.toxsoft.uskat.alarms.lib;

import static org.toxsoft.uskat.alarms.lib.ISkResources.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Важность (приоритет) тревог.
 * <p>
 * TODO описать использование приоритетов, в т.ч. понятия уровней и подуровней приоритетов
 * <p>
 * Внимание: константы объявляются в порядку увеличения приоритета (важности).
 *
 * @author goga
 */
@SuppressWarnings( { "nls", "javadoc" } )
public enum EAlarmPriority
    implements IStridable {

  INFO( "Info", 127, STR_D_AP_INFO, STR_N_AP_INFO ),

  LOW( "Low", 256 + 127, STR_D_AP_LOW, STR_N_AP_LOW ),

  NORMAL( "Normal", 2 * 256 + 127, STR_D_AP_NORMAL, STR_N_AP_NORMAL ),

  HIGH( "High", 3 * 256 + 127, STR_D_AP_HIGH, STR_N_AP_HIGH ),

  CRITICAL( "Critical", 4 * 256 + 127, STR_D_AP_CRITICAL, STR_N_AP_CRITICAL )

  ;

  private final String id;
  private final int    sublevelBase;
  private final String description;
  private final String name;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aSublevelBase int - базовое числовое значение приоритета
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое название константы
   */
  EAlarmPriority( String aId, int aSublevelBase, String aDescr, String aName ) {
    id = aId;
    sublevelBase = aSublevelBase;
    description = aDescr;
    name = aName;
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
    return name;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Возвращает базовое числовое значение приоритета.
   * <p>
   * Числовое значение уточнения важности конкретной тревоги {@link ISkAlarm#sublevel()} (в пределах -127...+128) можно
   * сложить с базовым значением {@link EAlarmPriority#sublevelBase()} чтобы получить числовое значение приоритета.
   * Числовое значение приоритета принимает значения от 0 (малозначительное инфомационное сообщение) до 1280 (самая
   * критическая тревога).
   *
   * @return int - базовое числовое значение приоритета
   */
  public int sublevelBase() {
    return sublevelBase;
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
   * @return EAlarmPriority - найденная константа, или null если нет константы с таимк идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EAlarmPriority findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EAlarmPriority item : values() ) {
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
   * @return EAlarmPriority - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EAlarmPriority findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return EAlarmPriority - найденная константа, или null если нет константы с таким описанием
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EAlarmPriority findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( EAlarmPriority item : values() ) {
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
   * @return EAlarmPriority - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EAlarmPriority findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

  /**
   * Возвращает константу по имени или null.
   *
   * @param aName String - имя искомой константы
   * @return EAlarmPriority - найденная константа, или null если нет константы с таким именем
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EAlarmPriority findByNameOrNull( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EAlarmPriority item : values() ) {
      if( item.name.equals( aName ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает константу по имени или выбрасывает исключение.
   *
   * @param aName String - имя искомой константы
   * @return EAlarmPriority - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким именем
   */
  public static EAlarmPriority findByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
  }

}
