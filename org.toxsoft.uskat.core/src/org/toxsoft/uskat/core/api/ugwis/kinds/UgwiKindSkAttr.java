package org.toxsoft.uskat.core.api.ugwis.kinds;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.ugwis.kinds.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
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
import org.toxsoft.uskat.core.api.ugwis.helpers.*;

/**
 * UGWI kind: Sk-object attribute value.
 * <p>
 * Format is 3-branches {@link IdChain}: "classId/objStrid/attrId".
 * <p>
 * {@link ISkObject#attrs()} is used to retrieve Sk-object attribute value.
 *
 * @author hazard157
 */
public class UgwiKindSkAttr
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
   * Number of branches in {@link IdChain}.
   */
  private static final int NUM_BRANCHES = 3;

  /**
   * {@link IUgwiKind} implementation.
   *
   * @author hazard157
   */
  public static class Kind
      extends AbstractUgwiKind<IAtomicValue> {

    Kind( AbstractUgwiKindRegistrator<IAtomicValue> aRegistrator, ISkCoreApi aCoreApi ) {
      super( aRegistrator, aCoreApi );
      // create and register IUgwiAvHelper instance
      AbstractUgwiAvHelperWithAv<IAtomicValue, Kind> avHelper = new AbstractUgwiAvHelperWithAv<>( this ) {

        @Override
        protected IAtomicValue doFindAtomicValue( Ugwi aUgwi ) {
          return doFindContent( aUgwi );
        }

        @Override
        protected IDataType doGetValueType( Ugwi aUgwi ) {
          String classId = getClassId( aUgwi );
          ISkClassInfo cinf = coreApi().sysdescr().findClassInfo( classId );
          if( cinf != null ) {
            String attrId = getAttrId( aUgwi );
            IDtoAttrInfo ainf = cinf.attrs().list().findByKey( attrId );
            if( ainf != null ) {
              return ainf.dataType();
            }
          }
          return null;
        }
      };
      registerHelper( IUgwiAvHelper.class, avHelper );
    }

    @Override
    public IAtomicValue doFindContent( Ugwi aUgwi ) {
      Skid skid = getSkid( aUgwi );
      ISkObject sko = coreApi().objService().find( skid );
      if( sko != null ) {
        return sko.attrs().getValue( getAttrId( aUgwi ), IAtomicValue.NULL );
      }
      return IAtomicValue.NULL;
    }

  }

  /**
   * The UGWI kind ID.
   */
  public static final String KIND_ID = SK_ID + ".sk.attr"; //$NON-NLS-1$

  /**
   * The registrator instance.
   */
  public static final UgwiKindSkAttr REGISTRATOR = new UgwiKindSkAttr();

  /**
   * Constructor.
   */
  private UgwiKindSkAttr() {
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
        if( chain.branches().size() == NUM_BRANCHES ) {
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
   * Extracts object SKID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Skid} - the object SKID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static Skid getSkid( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( REGISTRATOR.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return new Skid( chain.branches().get( IDX_CLASS_ID ), chain.branches().get( IDX_OBJ_STRID ) );
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
