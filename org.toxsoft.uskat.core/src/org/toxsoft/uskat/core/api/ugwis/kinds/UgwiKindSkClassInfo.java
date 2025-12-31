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
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * UGWI kind: Sk-class info.
 * <p>
 * Format is 1-branch {@link IdChain}: "classId".
 *
 * @author vs
 */
public class UgwiKindSkClassInfo
    extends AbstractUgwiKind<ISkClassInfo> {

  /**
   * The index of the class ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_CLASS_ID = 0;

  /**
   * Number of branches in {@link IdChain}.
   */
  private static final int NUM_BRANCHES = 1;

  /**
   * {@link ISkUgwiKind} implementation.
   *
   * @author vs
   */
  public static class Kind
      extends AbstractSkUgwiKind<ISkClassInfo> {

    Kind( AbstractUgwiKind<ISkClassInfo> aStaticKind, ISkCoreApi aCoreApi ) {
      super( aStaticKind, aCoreApi );
    }

    @Override
    public ISkClassInfo doFindContent( Ugwi aUgwi ) {
      Gwid gwid = ugwiKind().getGwid( aUgwi );
      return coreApi().sysdescr().findClassInfo( gwid.classId() );
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
      ISkClassInfo classInfo = doFindContent( aUgwi );
      if( classInfo != null ) {
        return AvUtils.avValobj( classInfo );
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
  public static final String KIND_ID = SK_ID + ".sk.class.info"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKindSkClassInfo INSTANCE = new UgwiKindSkClassInfo();

  /**
   * Constructor.
   */
  private UgwiKindSkClassInfo() {
    super( KIND_ID, true, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_CLASS_INFO, //
        TSID_DESCRIPTION, STR_UK_CLASS_INFO_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    // if( IdChain.isValidCanonicalString( aEssence ) ) {
    IdChain chain = IdChain.of( aEssence );
    if( chain != IdChain.NULL ) {
      if( chain.branches().size() == NUM_BRANCHES ) {
        return ValidationResult.SUCCESS;
      }
    }
    // }
    return makeGeneralInvalidEssenceVr( aEssence );
  }

  @Override
  protected AbstractSkUgwiKind<?> doCreateUgwiKind( ISkCoreApi aSkConn ) {
    return new Kind( this, aSkConn );
  }

  @Override
  protected Gwid doGetGwid( Ugwi aUgwi ) {
    IdChain chain = IdChain.of( aUgwi.essence() );
    return Gwid.createClass( chain.get( IDX_CLASS_ID ) );
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

  /**
   * Creates the UGWI of this kind.
   *
   * @param aClassId String - class ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId ) {
    StridUtils.checkValidIdPath( aClassId );
    IdChain chain = new IdChain( aClassId );
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of UgwiKindSkSkid kind.
   *
   * @param aClassId String - class ID
   * @param aObjStrid String - object STRID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId, String aObjStrid ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aObjStrid );
    IdChain chain = new IdChain( aClassId, aObjStrid );
    return Ugwi.of( UgwiKindSkSkid.KIND_ID, chain.canonicalString() );
  }

}
