package org.toxsoft.uskat.legacy.plexy;

import static org.toxsoft.uskat.legacy.plexy.ISkResources.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Виды типов значений {@link IPlexyValue}.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
public enum EPlexyKind
    implements IStridable {

  /**
   * Единичное атомарное значение {@link IPlexyValue#singleValue()}.
   */
  SINGLE_VALUE( "SingleValue", STR_D_PLXK_SINGLE_VALUE, STR_D_PLXK_SINGLE_VALUE, true, false ),

  /**
   * Список атомарных значений {@link IPlexyValue#valueList()}.
   */
  VALUE_LIST( "ValueList", STR_D_PLXK_VALUE_LIST, STR_D_PLXK_VALUE_LIST, true, false ),

  /**
   * Единичная ссылка на Java-объект {@link IPlexyValue#singleRef()}.
   */
  SINGLE_REF( "SingleRef", STR_D_PLXK_SINGLE_REF, STR_D_PLXK_SINGLE_REF, false, true ),

  /**
   * Список ссылкок на однотипные Java-объектs {@link IPlexyValue#refList()}.
   */
  REF_LIST( "RefList", STR_D_PLXK_REF_LIST, STR_D_PLXK_REF_LIST, false, true ),

  /**
   * Набор именнованных атомарных значений.
   */
  OPSET( "OptionSet", STR_D_PLXK_OPSET, STR_D_PLXK_OPSET, false, false )

  ;

  private final String  id;
  private final String  name;
  private final String  description;
  private final boolean isAtomic;
  private final boolean isReference;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aName String - отображаемое имя константы
   * @param aDescr String - отображаемое описание константы
   * @param aIsAtomic boolean - признак содержания в плекси-значений атоманоных значений
   * @param aIsReference boolean - признак содержания в плекси-значений ссылок на Java-объекты
   */
  EPlexyKind( String aId, String aName, String aDescr, boolean aIsAtomic, boolean aIsReference ) {
    id = aId;
    name = aName;
    description = aDescr;
    isAtomic = aIsAtomic;
    isReference = aIsReference;
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return name;
  }

  @Override
  public String description() {
    return description;
  }

  // ------------------------------------------------------------------------------------
  // Дополнительное API
  //

  /**
   * Определяет, содержит ли плекси-значение ссылки на Java-объекты.
   * <p>
   * В плекси-типе метод {@link IPlexyType#refClass()} имеет смысл только для тех {@link EPlexyKind}, у котороых данный
   * метод возвращает <code>true</code>.
   * <p>
   * Равнозначно проверке на то, что это одна из констант {@link EPlexyKind#SINGLE_REF} или {@link EPlexyKind#REF_LIST}.
   *
   * @return boolean - признак содержания в плекси-значений ссылок на Java-объекты
   */
  public boolean isReference() {
    return isReference;
  }

  /**
   * Определяет, содержит ли плекси-значение атомарные значения.
   * <p>
   * В плекси-типе метод {@link IPlexyType#dataType()} имеет смысл только для тех {@link EPlexyKind}, у котороых данный
   * метод возвращает <code>true</code>.
   * <p>
   * Равнозначно проверке на то, что это одна из констант {@link EPlexyKind#SINGLE_VALUE} или
   * {@link EPlexyKind#VALUE_LIST}.
   *
   * @return boolean - признак содержания в плекси-значений атоманоных значений
   */
  public boolean isAtomic() {
    return isAtomic;
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

  // ----------------------------------------------------------------------------------
  // Методы поиска
  //

  /**
   * Возвращает константу по идентификатору или null.
   *
   * @param aId String - идентификатор искомой константы
   * @return EAcvType - найденная константа, или null если нет константы с таимк идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EPlexyKind findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EPlexyKind item : values() ) {
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
   * @return EAcvType - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EPlexyKind findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return EAcvType - найденная константа, или null если нет константы с таким идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EPlexyKind findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( EPlexyKind item : values() ) {
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
   * @return EAcvType - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EPlexyKind findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

}
