package org.toxsoft.uskat.core.api.sysdescr;

import static org.toxsoft.uskat.core.api.sysdescr.ISkResources.*;

import org.toxsoft.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.tslib.bricks.keeper.std.StridableEnumKeeper;
import org.toxsoft.tslib.bricks.strid.IStridable;
import org.toxsoft.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.tslib.gw.IGwHardConstants;
import org.toxsoft.tslib.gw.gwid.EGwidKind;
import org.toxsoft.tslib.gw.gwid.Gwid;
import org.toxsoft.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.tslib.utils.errors.TsNullArgumentRtException;

/**
 * The enumeration of XXX.
 *
 * @author goga
 */
public enum ESkClassPropKind
    implements IStridable {

  /**
   * Attribute.
   */
  ATTR( IGwHardConstants.GW_KEYWORD_ATTR, STR_N_ATTR, STR_D_ATTR, EGwidKind.GW_ATTR ),

  /**
   * RT-data.
   */
  RTDATA( IGwHardConstants.GW_KEYWORD_RTDATA, STR_N_RTDATA, STR_D_RTDATA, EGwidKind.GW_RTDATA ),

  /**
   * Rivet - Склёпка.
   */
  RIVET( IGwHardConstants.GW_KEYWORD_RIVET, STR_N_RIVET, STR_D_RIVET, EGwidKind.GW_RIVET ),

  /**
   * Link.
   */
  LINK( IGwHardConstants.GW_KEYWORD_LINK, STR_N_LINK, STR_D_LINK, EGwidKind.GW_LINK ),

  /**
   * Command.
   */
  CMD( IGwHardConstants.GW_KEYWORD_CMD, STR_N_CMD, STR_D_CMD, EGwidKind.GW_CMD ),

  /**
   * Event.
   */
  EVENT( IGwHardConstants.GW_KEYWORD_EVENT, STR_N_EVENT, STR_D_EVENT, EGwidKind.GW_EVENT ),

  /**
   * CLOB.
   */
  CLOB( IGwHardConstants.GW_KEYWORD_CLOB, STR_N_CLOB, STR_D_CLOB, EGwidKind.GW_CLOB ),

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
