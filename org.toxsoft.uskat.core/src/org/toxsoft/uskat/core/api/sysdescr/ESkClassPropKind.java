package org.toxsoft.uskat.core.api.sysdescr;

import static org.toxsoft.uskat.core.api.sysdescr.ISkResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The enumeration of XXX.
 *
 * @author hazard157
 */
public enum ESkClassPropKind
    implements IStridable {

  /**
   * Attribute.
   */
  ATTR( IGwHardConstants.GW_KEYWORD_ATTR, STR_N_CPK_ATTR, STR_D_CPK_ATTR, EGwidKind.GW_ATTR ),

  /**
   * RT-data.
   */
  RTDATA( IGwHardConstants.GW_KEYWORD_RTDATA, STR_N_CPK_RTDATA, STR_D_CPK_RTDATA, EGwidKind.GW_RTDATA ),

  /**
   * Rivet - Склёпка.
   */
  RIVET( IGwHardConstants.GW_KEYWORD_RIVET, STR_N_CPK_RIVET, STR_D_CPK_RIVET, EGwidKind.GW_RIVET ),

  /**
   * Link.
   */
  LINK( IGwHardConstants.GW_KEYWORD_LINK, STR_N_CPK_LINK, STR_D_CPK_LINK, EGwidKind.GW_LINK ),

  /**
   * Command.
   */
  CMD( IGwHardConstants.GW_KEYWORD_CMD, STR_N_CPK_CMD, STR_D_CPK_CMD, EGwidKind.GW_CMD ),

  /**
   * Event.
   */
  EVENT( IGwHardConstants.GW_KEYWORD_EVENT, STR_N_CPK_EVENT, STR_D_CPK_EVENT, EGwidKind.GW_EVENT ),

  /**
   * CLOB.
   */
  CLOB( IGwHardConstants.GW_KEYWORD_CLOB, STR_N_CPK_CLOB, STR_D_CPK_CLOB, EGwidKind.GW_CLOB ),

  ;

  /**
   * The keeper ID.
   */
  public static final String KEEPER_ID = "ESkClassPropKind"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<ESkClassPropKind> KEEPER = new StridableEnumKeeper<>( ESkClassPropKind.class );

  private static IStridablesListEdit<ESkClassPropKind> list = null;

  private final String    id;
  private final String    name;
  private final String    description;
  private final EGwidKind gwidKind;

  ESkClassPropKind( String aId, String aName, String aDescription, EGwidKind aGwidKind ) {
    id = aId;
    name = aName;
    description = aDescription;
    gwidKind = aGwidKind;
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
    return name;
  }

  @Override
  public String description() {
    return description;
  }

  // ----------------------------------------------------------------------------------
  // API
  //

  /**
   * Return the corresponding {@link Gwid} kind.
   *
   * @return {@link EGwidKind} - the GWID kind
   */
  public EGwidKind gwidKind() {
    return gwidKind;
  }

  /**
   * Returns all constants in single list.
   *
   * @return {@link IStridablesList}&lt; {@link ESkClassPropKind} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<ESkClassPropKind> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link ESkClassPropKind} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static ESkClassPropKind getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link ESkClassPropKind} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ESkClassPropKind findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( ESkClassPropKind item : values() ) {
      if( item.name.equals( aName ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns the constant by the name.
   *
   * @param aName String - the name
   * @return {@link ESkClassPropKind} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static ESkClassPropKind getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}
