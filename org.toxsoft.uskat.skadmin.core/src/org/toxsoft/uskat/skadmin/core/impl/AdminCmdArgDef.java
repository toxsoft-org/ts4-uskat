package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils;
import org.toxsoft.uskat.skadmin.core.IAdminCmdArgDef;

/**
 * Реализация аргумента по умолчанию
 *
 * @author mvk
 */
public class AdminCmdArgDef
    implements IAdminCmdArgDef {

  private final String     id;
  private final String     description;
  private final String     name;
  private final String     alias;
  private final IPlexyType type;

  /**
   * Конструктор аргумента без алиаса и не являющимся массивом с указанием только {@link IDataType}
   *
   * @param aId String - иденитфикатор аргумента
   * @param aType {@link IDataType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdArgDef( String aId, IDataType aType, String aDescription ) {
    this( aId, EMPTY_STRING, EMPTY_STRING, PlexyValueUtils.ptSingleValue( aType ), aDescription );
  }

  /**
   * Конструктор
   *
   * @param aId String - иденитфикатор аргумента
   * @param aType {@link IPlexyType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdArgDef( String aId, IPlexyType aType, String aDescription ) {
    this( aId, EMPTY_STRING, EMPTY_STRING, aType, aDescription );
  }

  /**
   * Конструктор аргумента без алиаса и не являющимся массивом с указанием только {@link IDataType}
   *
   * @param aId String - иденитфикатор аргумента
   * @param aAlias String - алиас аргумента или пустая строка, если нет алиаса
   * @param aType {@link IDataType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdArgDef( String aId, String aAlias, IDataType aType, String aDescription ) {
    this( aId, aAlias, EMPTY_STRING, PlexyValueUtils.ptSingleValue( aType ), aDescription );
  }

  /**
   * Конструктор
   *
   * @param aId String - иденитфикатор аргумента
   * @param aAlias String - алиас аргумента или пустая строка, если нет алиаса
   * @param aType {@link IPlexyType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdArgDef( String aId, String aAlias, IPlexyType aType, String aDescription ) {
    this( aId, aAlias, EMPTY_STRING, aType, aDescription );
  }

  /**
   * Конструктор аргумента без алиаса и не являющимся массивом с указанием только {@link IDataType}
   *
   * @param aId String - иденитфикатор аргумента
   * @param aAlias String - алиас аргумента или пустая строка, если нет алиаса
   * @param aName String - краткое удобочитаемое название команды
   * @param aType {@link IDataType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdArgDef( String aId, String aAlias, String aName, IDataType aType, String aDescription ) {
    this( aId, aAlias, aName, PlexyValueUtils.ptSingleValue( aType ), aDescription );
  }

  /**
   * Конструктор
   *
   * @param aId String - иденитфикатор аргумента
   * @param aAlias String - алиас аргумента или пустая строка, если нет алиаса
   * @param aName String - краткое удобочитаемое название команды
   * @param aType {@link IPlexyType} - тип значения аргумента
   * @param aDescription String - описание аргумента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdArgDef( String aId, String aAlias, String aName, IPlexyType aType, String aDescription ) {
    TsNullArgumentRtException.checkNulls( aId, aDescription, aName, aAlias, aType );
    id = aId;
    description = aDescription;
    name = aName;
    alias = aAlias;
    type = aType;
  }

  // ------------------------------------------------------------------------------------
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

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса INameable
  //
  @Override
  public String nmName() {
    return name;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmdArgDef
  //
  @Override
  public String alias() {
    return alias;
  }

  @Override
  public IPlexyType type() {
    return type;
  }
}
