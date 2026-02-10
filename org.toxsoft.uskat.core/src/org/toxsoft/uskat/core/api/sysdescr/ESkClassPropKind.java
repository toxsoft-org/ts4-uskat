package org.toxsoft.uskat.core.api.sysdescr;

import static org.toxsoft.uskat.core.api.sysdescr.ISkResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The kind of the class property.
 *
 * @author hazard157
 */
public enum ESkClassPropKind
    implements IStridable {

  /**
   * Attribute.
   */
  ATTR( IGwHardConstants.GW_KEYWORD_ATTR, STR_CPK_ATTR, STR_CPK_ATTR_PLURAL, STR_CPK_ATTR_D, EGwidKind.GW_ATTR ),

  /**
   * RT-data.
   */
  RTDATA( IGwHardConstants.GW_KEYWORD_RTDATA, STR_CPK_RTDATA, STR_CPK_RTDATA_PLURAL, STR_CPK_RTDATA_D,
      EGwidKind.GW_RTDATA ),

  /**
   * Rivet.
   */
  RIVET( IGwHardConstants.GW_KEYWORD_RIVET, STR_CPK_RIVET, STR_CPK_RIVET_PLURAL, STR_CPK_RIVET_D, EGwidKind.GW_RIVET ),

  /**
   * Link.
   */
  LINK( IGwHardConstants.GW_KEYWORD_LINK, STR_CPK_LINK, STR_CPK_LINK_PLURAL, STR_CPK_LINK_D, EGwidKind.GW_LINK ),

  /**
   * Command.
   */
  CMD( IGwHardConstants.GW_KEYWORD_CMD, STR_CPK_CMD, STR_CPK_CMD_PLURAL, STR_CPK_CMD_D, EGwidKind.GW_CMD ),

  /**
   * Event.
   */
  EVENT( IGwHardConstants.GW_KEYWORD_EVENT, STR_CPK_EVENT, STR_CPK_EVENT_PLURAL, STR_CPK_EVENT_D, EGwidKind.GW_EVENT ),

  /**
   * CLOB.
   */
  CLOB( IGwHardConstants.GW_KEYWORD_CLOB, STR_CPK_CLOB, STR_CPK_CLOB_PLURAL, STR_CPK_CLOB_D, EGwidKind.GW_CLOB ),

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
  private final String    namePlural;
  private final String    description;
  private final EGwidKind gwidKind;

  ESkClassPropKind( String aId, String aName, String aNamePlural, String aDescription, EGwidKind aGwidKind ) {
    id = aId;
    name = aName;
    namePlural = aNamePlural;
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
   * Returns the plural name of the property kind.
   *
   * @return String - plural name
   */
  public String pluralName() {
    return namePlural;
  }

  /**
   * Return the corresponding {@link Gwid} kind.
   *
   * @return {@link EGwidKind} - the GWID kind
   */
  public EGwidKind gwidKind() {
    return gwidKind;
  }

  /**
   * Creates the abstract GWID of the class property.
   *
   * @param aClassId String - the class ID
   * @param aPropId String - the property ID
   * @return {@link Gwid} - created GWID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public Gwid createAbstractGwid( String aClassId, String aPropId ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aPropId );
    return Gwid.create( aClassId, null, id, aPropId, null, null );
  }

  /**
   * Creates the concrete GWID of the class property.
   *
   * @param aSkid {@link Skid} - the object SKID
   * @param aPropId String - the property ID
   * @return {@link Gwid} - created GWID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public Gwid createConcreteGwid( Skid aSkid, String aPropId ) {
    TsNullArgumentRtException.checkNull( aSkid );
    StridUtils.checkValidIdPath( aPropId );
    return Gwid.create( aSkid.classId(), aSkid.strid(), id, aPropId, null, null );
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
