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
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * UGWI kind: Sk-object rivet link info.
 * <p>
 * Format is 2-branches {@link IdChain}: "classId/rivetId".
 * <p>
 * {@link ISkObject#rivets()} is used to retrieve Sk-object rivet link.
 *
 * @author vs
 */
public class UgwiKindSkRivetInfo
    extends AbstractUgwiKind<IDtoRivetInfo> {

  /**
   * The index of the class ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_CLASS_ID = 0;

  /**
   * The index of the link ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_RIVET_ID = 1;

  /**
   * Number of branches in {@link IdChain}.
   */
  private static final int NUM_BRANCHES = 2;

  /**
   * {@link ISkUgwiKind} implementation.
   *
   * @author vs
   */
  public static class Kind
      extends AbstractSkUgwiKind<IDtoRivetInfo> {

    Kind( AbstractUgwiKind<IDtoRivetInfo> aRegistrator, ISkCoreApi aCoreApi ) {
      super( aRegistrator, aCoreApi );
    }

    @Override
    public IDtoRivetInfo doFindContent( Ugwi aUgwi ) {
      ISkClassInfo clsInfo = coreApi().sysdescr().findClassInfo( getClassId( aUgwi ) );
      if( clsInfo != null ) {
        return clsInfo.rivets().list().findByKey( getRivetId( aUgwi ) );
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
      IDtoRivetInfo rivetInfo = doFindContent( aUgwi );
      if( rivetInfo != null ) {
        return AvUtils.avValobj( rivetInfo );
      }
      return IAtomicValue.NULL;
    }

    @Override
    protected IDataType doGetAtomicValueDataType( Ugwi aUgwi ) {
      return null;
    }

  }

  /**
   * The UGWI kind ID.
   */
  public static final String KIND_ID = SK_ID + ".sk.rivet.info"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKindSkRivetInfo INSTANCE = new UgwiKindSkRivetInfo();

  /**
   * Constructor.
   */
  private UgwiKindSkRivetInfo() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_RIVET, //
        TSID_DESCRIPTION, STR_UK_RIVET_D //
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

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Extracts class ID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return String - the class ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static String getClassId( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.get( IDX_CLASS_ID );
  }

  /**
   * Extracts rivet ID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return String - the rivet ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static String getRivetId( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.get( IDX_RIVET_ID );
  }

  /**
   * Creates the UGWI of UgwiKindSkRivetInfo kind.
   *
   * @param aClassId String - class ID
   * @param aRivetId String - rivet ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId, String aRivetId ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aRivetId );
    IdChain chain = new IdChain( aClassId, aRivetId );
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of UgwiKindSkRivet kind.
   *
   * @param aClassId String - class ID
   * @param aObjStrid String - object STRID
   * @param aRivetId String - rivet ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId, String aObjStrid, String aRivetId ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aObjStrid );
    StridUtils.checkValidIdPath( aRivetId );
    IdChain chain = new IdChain( aClassId, aObjStrid, aRivetId );
    return Ugwi.of( UgwiKindSkRivet.KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of UgwiKindSkRivet kind.
   *
   * @param aObjSkid {@link Skid} - SKID of the object
   * @param aRivetId String - rivet ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( Skid aObjSkid, String aRivetId ) {
    TsNullArgumentRtException.checkNull( aObjSkid );
    TsIllegalArgumentRtException.checkTrue( aObjSkid == Skid.NONE );
    StridUtils.checkValidIdPath( aRivetId );
    IdChain chain = new IdChain( aObjSkid.classId(), aObjSkid.strid(), aRivetId );
    return Ugwi.of( UgwiKindSkRivet.KIND_ID, chain.canonicalString() );
  }

}
