package org.toxsoft.uskat.core.api.ugwis.kinds;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.ugwis.kinds.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * UGWI kind: Sk-object attribute info.
 * <p>
 * Format is 2-branches {@link IdChain}: "classId/attrId".
 *
 * @author vs
 */
public class UgwiKindSkAttrInfo
    extends AbstractUgwiKind<IDtoAttrInfo> {

  /**
   * The index of the class ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_CLASS_ID = 0;

  /**
   * The index of the attribute ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_ATTR_ID = 1;

  /**
   * Number of branches in {@link IdChain}.
   */
  private static final int NUM_BRANCHES = 2;

  /**
   * {@link ISkUgwiKind} implementation.
   *
   * @author hazard157
   */
  public static class Kind
      extends AbstractSkUgwiKind<IDtoAttrInfo> {

    Kind( AbstractUgwiKind<IDtoAttrInfo> aStaticKind, ISkCoreApi aCoreApi ) {
      super( aStaticKind, aCoreApi );
    }

    @Override
    public IDtoAttrInfo doFindContent( Ugwi aUgwi ) {
      Gwid gwid = ugwiKind().getGwid( aUgwi );
      ISkClassInfo clsInfo = coreApi().sysdescr().findClassInfo( gwid.classId() );
      if( clsInfo != null ) {
        return clsInfo.attrs().list().getByKey( gwid.propId() );
      }
      return null;
    }

    @Override
    protected boolean doCanRegister( SkCoreServUgwis aUgwiService ) {
      return true;
    }

    @Override
    protected boolean doIsNaturalAtomicValue( Ugwi aUgwi ) {
      return false;
    }

    @Override
    protected IAtomicValue doFindAtomicValue( Ugwi aUgwi ) {
      IDtoAttrInfo attrInfo = doFindContent( aUgwi );
      if( attrInfo != null ) {
        return AvUtils.avValobj( attrInfo );
      }
      return IAtomicValue.NULL;
    }

    @Override
    protected IDataType doGetAtomicValueDataType( Ugwi aUgwi ) {
      Gwid gwid = ugwiKind().getGwid( aUgwi );
      ISkClassInfo cinf = coreApi().sysdescr().findClassInfo( gwid.classId() );
      if( cinf != null ) {
        IDtoAttrInfo ainf = cinf.attrs().list().findByKey( gwid.propId() );
        if( ainf != null ) {
          return ainf.dataType();
        }
      }
      return null;
    }

  }

  /**
   * The UGWI kind ID.
   */
  public static final String KIND_ID = SK_ID + ".sk.attr.info"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKindSkAttrInfo INSTANCE = new UgwiKindSkAttrInfo();

  /**
   * Constructor.
   */
  private UgwiKindSkAttrInfo() {
    super( KIND_ID, true, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_ATTR, //
        TSID_DESCRIPTION, STR_UK_ATTR_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    if( IdChain.isValidCanonicalString( aEssence ) ) {
      IdChain chain = IdChain.of( aEssence );
      if( chain != IdChain.NULL ) {
        if( chain.branches().size() == NUM_BRANCHES ) {
          return ValidationResult.SUCCESS;
        }
      }
    }
    return makeGeneralInvalidEssenceVr( aEssence );
  }

  @Override
  protected AbstractSkUgwiKind<?> doCreateUgwiKind( ISkCoreApi aSkConn ) {
    return new Kind( this, aSkConn );
  }

  @Override
  protected Gwid doGetGwid( Ugwi aUgwi ) {
    IdChain chain = IdChain.of( aUgwi.essence() );
    return Gwid.createAttr( chain.get( IDX_CLASS_ID ), chain.get( IDX_ATTR_ID ) );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  // /**
  // * Extracts class ID from the UGWI of this kind.
  // *
  // * @param aUgwi {@link Ugwi} - the UGWI
  // * @return String - the class ID
  // * @throws TsNullArgumentRtException any argument = <code>null</code>
  // * @throws TsValidationFailedRtException invalid UGWI for this kind
  // */
  // public static String getClassId( Ugwi aUgwi ) {
  // TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
  // IdChain chain = IdChain.of( aUgwi.essence() );
  // return chain.get( IDX_CLASS_ID );
  // }
  //
  // /**
  // * Extracts attribute ID from the UGWI of this kind.
  // *
  // * @param aUgwi {@link Ugwi} - the UGWI
  // * @return String - the attribute ID
  // * @throws TsNullArgumentRtException any argument = <code>null</code>
  // * @throws TsValidationFailedRtException invalid UGWI for this kind
  // */
  // public static String getAttrId( Ugwi aUgwi ) {
  // TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
  // IdChain chain = IdChain.of( aUgwi.essence() );
  // return chain.get( IDX_ATTR_ID );
  // }

  /**
   * Creates the UGWI of UgwiKindSkAttr kind.
   *
   * @param aClassId String - class ID
   * @param aAttrId String - attribute ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId, String aAttrId ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aAttrId );
    IdChain chain = new IdChain( aClassId, aAttrId );
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of UgwiKindSkAttr kind.
   *
   * @param aClassId String - class ID
   * @param aObjStrid String - object STRID
   * @param aAttrId String - attribute ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId, String aObjStrid, String aAttrId ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aObjStrid );
    StridUtils.checkValidIdPath( aAttrId );
    IdChain chain = new IdChain( aClassId, aObjStrid, aAttrId );
    return Ugwi.of( UgwiKindSkAttr.KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of UgwiKindSkAttr kind.
   *
   * @param aObjSkid {@link Skid} - SKID of the object
   * @param aAttrId String - attribute ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( Skid aObjSkid, String aAttrId ) {
    TsNullArgumentRtException.checkNull( aObjSkid );
    TsIllegalArgumentRtException.checkTrue( aObjSkid == Skid.NONE );
    StridUtils.checkValidIdPath( aAttrId );
    IdChain chain = new IdChain( aObjSkid.classId(), aObjSkid.strid(), aAttrId );
    return Ugwi.of( UgwiKindSkAttr.KIND_ID, chain.canonicalString() );
  }

}
