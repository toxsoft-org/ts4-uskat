package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.impl.dto.IDtoHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoLinkInfo} implementation.
 * <p>
 * As {@link StridableParameterized} extension this class stores all fields in {@link #params()}:
 * <ul>
 * <li>{@link #rightClassIds()} - as option {@link IDtoHardConstants#OPDEF_RIGHT_CLASS_IDS};</li>
 * <li>{@link #linkConstraint()} - as option {@link IDtoHardConstants#OPDEF_LINK_CONSTRAINT}.</li>
 * </ul>
 *
 * @author hazard157
 */
public final class DtoLinkInfo
    extends DtoAbstractClassPropInfoBase
    implements IDtoLinkInfo {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoLinkInfo> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDtoLinkInfo.class, null ) {

        @Override
        protected IDtoLinkInfo doCreate( String aId, IOptionSet aParams ) {
          return new DtoLinkInfo( aId, aParams );
        }
      };

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - parameters values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  private DtoLinkInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aRightClassIds {@link IStringList} - right class IDs
   * @param aConstraint {@link CollConstraint} - constraints on linked objects
   * @param aParams {@link IOptionSet} - parameters values
   * @return {@link DtoLinkInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any identifier is not an IDpath
   */
  public static DtoLinkInfo create1( String aId, IStringList aRightClassIds, CollConstraint aConstraint,
      IOptionSet aParams ) {
    DtoLinkInfo ainf = new DtoLinkInfo( aId, aParams );
    ainf.setProps( aRightClassIds, aConstraint );
    return ainf;
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aRightClassIds {@link IStringList} - right class IDs
   * @param aConstraint {@link CollConstraint} - constraints on linked objects
   * @param aIdsAndValues Object[] - parameters values
   * @return {@link DtoLinkInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any identifier is not an IDpath
   */
  public static DtoLinkInfo create2( String aId, IStringList aRightClassIds, CollConstraint aConstraint,
      Object... aIdsAndValues ) {
    DtoLinkInfo ainf = new DtoLinkInfo( aId, OptionSetUtils.createOpSet( aIdsAndValues ) );
    ainf.setProps( aRightClassIds, aConstraint );
    return ainf;
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public ESkClassPropKind kind() {
    return ESkClassPropKind.LINK;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public DtoLinkInfo makeCopy() {
    return new DtoLinkInfo( id(), params() );
  }

  // ------------------------------------------------------------------------------------
  // IDtoLinkInfo
  //

  @Override
  public IStringList rightClassIds() {
    return OPDEF_RIGHT_CLASS_IDS.getValue( params() ).asValobj();
  }

  @Override
  public CollConstraint linkConstraint() {
    return OPDEF_LINK_CONSTRAINT.getValue( params() ).asValobj();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets link propertoes.
   *
   * @param aRightClassIds {@link IStringList} - right class IDs
   * @param aConstraint {@link CollConstraint} - constraints on linked objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any class ID is not an IDpath
   */
  public void setProps( IStringList aRightClassIds, CollConstraint aConstraint ) {
    TsNullArgumentRtException.checkNulls( aRightClassIds, aConstraint );
    for( String s : aRightClassIds ) {
      StridUtils.checkValidIdPath( s );
    }
    OPDEF_RIGHT_CLASS_IDS.setValue( params(), avValobj( aRightClassIds ) );
    OPDEF_LINK_CONSTRAINT.setValue( params(), avValobj( aConstraint ) );
  }

}
