package org.toxsoft.uskat.core.api.ugwis.kinds;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.ugwis.kinds.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
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
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * UGWI kind: Sk-object attribute value.
 * <p>
 * Format is 3-branches {@link IdChain}: "classId/objStrid/attrId".
 *
 * @author hazard157
 */
public class UgwiRegistratorSkAttr
    extends AbstractUgwiKindRegistrator<IAtomicValue> {

  /**
   * The index of the class ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_CLASS_ID = 0;

  /**
   * The index of the object STRID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_OBJ_STRID = 1;

  /**
   * The index of the attribute ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_ATTR_ID = 2;

  /**
   * {@link IUgwiKind} implementation.
   *
   * @author hazard157
   */
  public static class Kind
      extends AbstractUgwiKind<IAtomicValue> {

    Kind( AbstractUgwiKindRegistrator<IAtomicValue> aRegistrator, ISkCoreApi aCoreApi ) {
      super( aRegistrator, aCoreApi );
    }

    @Override
    protected IAtomicValue doFindContent( Ugwi aUgwi ) {
      IdChain chain = IdChain.of( aUgwi.essence() );
      Skid skid = new Skid( chain.branches().get( IDX_CLASS_ID ), chain.branches().get( IDX_OBJ_STRID ) );
      ISkObject sko = coreApi().objService().find( skid );
      if( sko != null ) {
        return sko.attrs().getValue( chain.branches().get( IDX_ATTR_ID ), IAtomicValue.NULL );
      }
      return IAtomicValue.NULL;
    }

  }

  /**
   * The UGWI kind ID.
   */
  public static final String KIND_ID = SK_ID + ".attr"; //$NON-NLS-1$

  /**
   * The registrator instance.
   */
  public static final UgwiRegistratorSkAttr REGISTRATOR = new UgwiRegistratorSkAttr();

  /**
   * Constructor.
   */
  private UgwiRegistratorSkAttr() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_ATTR, //
        TSID_DESCRIPTION, STR_UK_ATTR_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKindRegistrator
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    if( IdChain.isValidCanonicalString( aEssence ) ) {
      IdChain chain = IdChain.of( aEssence );
      if( chain != IdChain.NULL ) {
        if( chain.branches().size() == 3 ) {
          return ValidationResult.SUCCESS;
        }
      }
    }
    return makeGeneralInvalidEssenceVr( aEssence );
  }

  @Override
  protected AbstractUgwiKind<?> doCreateUgwiKind( ISkCoreApi aSkConn ) {
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
    TsValidationFailedRtException.checkError( REGISTRATOR.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.branches().get( IDX_CLASS_ID );
  }

  /**
   * Extracts object STRID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return String - the object STRID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static String getObjStrid( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( REGISTRATOR.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.branches().get( IDX_OBJ_STRID );
  }

  /**
   * Extracts attribute ID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return String - the attribute ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static String getAttrId( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( REGISTRATOR.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.branches().get( IDX_ATTR_ID );
  }

  /**
   * Creates the UGWI of this kind.
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
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of this kind.
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
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

}
