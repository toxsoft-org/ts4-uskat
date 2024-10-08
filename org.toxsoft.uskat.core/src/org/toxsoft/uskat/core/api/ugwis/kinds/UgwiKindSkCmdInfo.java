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
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * UGWI kind: Sk-object rt-data info.
 * <p>
 * Format is 2-branches {@link IdChain}: "classId/cmdId".
 *
 * @author vs
 */
public class UgwiKindSkCmdInfo
    extends AbstractUgwiKind<IDtoCmdInfo> {

  /**
   * The index of the class ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_CLASS_ID = 0;

  /**
   * The index of the command ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_CMD_ID = 1;

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
      extends AbstractSkUgwiKind<IDtoCmdInfo> {

    Kind( AbstractUgwiKind<IDtoCmdInfo> aRegistrator, ISkCoreApi aCoreApi ) {
      super( aRegistrator, aCoreApi );
    }

    @Override
    public IDtoCmdInfo doFindContent( Ugwi aUgwi ) {
      ISkClassInfo clsInfo = coreApi().sysdescr().findClassInfo( getClassId( aUgwi ) );
      if( clsInfo != null ) {
        return clsInfo.cmds().list().getByKey( getCmdId( aUgwi ) );
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
      IDtoCmdInfo cmdInfo = doFindContent( aUgwi );
      if( cmdInfo != null ) {
        return AvUtils.avValobj( cmdInfo );
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
  public static final String KIND_ID = SK_ID + ".sk.cmd.info"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKindSkCmdInfo INSTANCE = new UgwiKindSkCmdInfo();

  /**
   * Constructor.
   */
  private UgwiKindSkCmdInfo() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_CMD, //
        TSID_DESCRIPTION, STR_UK_CMD_D //
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
   * Extracts rt-data ID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return String - the command ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static String getCmdId( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.get( IDX_CMD_ID );
  }

  /**
   * Creates the UGWI of this kind.
   *
   * @param aClassId String - class ID
   * @param aCmdId String - command ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId, String aCmdId ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aCmdId );
    IdChain chain = new IdChain( aClassId, aCmdId );
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of this kind.
   *
   * @param aObjSkid {@link Skid} - SKID of the object
   * @param aCmdId String - rt-data ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( Skid aObjSkid, String aCmdId ) {
    TsNullArgumentRtException.checkNull( aObjSkid );
    TsIllegalArgumentRtException.checkTrue( aObjSkid == Skid.NONE );
    StridUtils.checkValidIdPath( aCmdId );
    IdChain chain = new IdChain( aObjSkid.classId(), aObjSkid.strid(), aCmdId );
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

}
