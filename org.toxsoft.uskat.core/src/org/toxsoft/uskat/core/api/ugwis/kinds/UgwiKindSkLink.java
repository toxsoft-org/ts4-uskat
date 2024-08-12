package org.toxsoft.uskat.core.api.ugwis.kinds;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
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
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * UGWI kind: Sk-object link.
 * <p>
 * Format is 3-branches {@link IdChain}: "classId/objStrid/linkId".
 *
 * @author hazard157, vs
 */
public class UgwiKindSkLink
    extends AbstractUgwiKind<ISkidList> {

  /**
   * The index of the class ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_CLASS_ID = 0;

  /**
   * The index of the object STRID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_OBJ_STRID = 1;

  /**
   * The index of the link ID in the {@link IdChain} made from {@link Ugwi#essence()}.
   */
  public static final int IDX_LINK_ID = 2;

  /**
   * Number of branches in {@link IdChain}.
   */
  private static final int NUM_BRANCHES = 3;

  private static final IDataType DT_SKID_LIST = DataType.create( VALOBJ, //
      TSID_KEEPER_ID, SkidListKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( ISkidList.EMPTY ) //
  );

  /**
   * {@link ISkUgwiKind} implementation.
   *
   * @author hazard157
   */
  public static class Kind
      extends AbstractSkUgwiKind<ISkidList> {

    Kind( AbstractUgwiKind<ISkidList> aRegistrator, ISkCoreApi aCoreApi ) {
      super( aRegistrator, aCoreApi );
    }

    @Override
    public ISkidList doFindContent( Ugwi aUgwi ) {
      Skid skid = getSkid( aUgwi );
      String linkId = getLinkId( aUgwi );
      ISkLinkService ls = coreApi().linkService();
      IStringMap<IDtoLinkFwd> linksMap = ls.getAllLinksFwd( skid );
      if( linksMap.hasKey( linkId ) ) {
        return linksMap.getByKey( linkId ).rightSkids();
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
      ISkidList skids = doFindContent( aUgwi );
      if( skids != null ) {
        return AvUtils.avValobj( skids );
      }
      return IAtomicValue.NULL;
    }

    @Override
    protected IDataType doGetAtomicValueDataType( Ugwi aUgwi ) {
      return DT_SKID_LIST;
    }

  }

  /**
   * The UGWI kind ID.
   */
  public static final String KIND_ID = SK_ID + ".sk.link"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKindSkLink INSTANCE = new UgwiKindSkLink();

  /**
   * Constructor.
   */
  private UgwiKindSkLink() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_LINK, //
        TSID_DESCRIPTION, STR_UK_LINK_D //
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
   * Extracts object STRID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return String - the object STRID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static String getObjStrid( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.get( IDX_OBJ_STRID );
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
    TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return new Skid( chain.get( IDX_CLASS_ID ), chain.get( IDX_OBJ_STRID ) );
  }

  /**
   * Extracts link ID from the UGWI of this kind.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return String - the attribute ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException invalid UGWI for this kind
   */
  public static String getLinkId( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
    IdChain chain = IdChain.of( aUgwi.essence() );
    return chain.get( IDX_LINK_ID );
  }

  /**
   * Creates the UGWI of this kind.
   *
   * @param aClassId String - class ID
   * @param aObjStrid String - object STRID
   * @param aLinkId String - link ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( String aClassId, String aObjStrid, String aLinkId ) {
    StridUtils.checkValidIdPath( aClassId );
    StridUtils.checkValidIdPath( aObjStrid );
    StridUtils.checkValidIdPath( aLinkId );
    IdChain chain = new IdChain( aClassId, aObjStrid, aLinkId );
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

  /**
   * Creates the UGWI of this kind.
   *
   * @param aObjSkid {@link Skid} - SKID of the object
   * @param aLinkId String - link ID
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( Skid aObjSkid, String aLinkId ) {
    TsNullArgumentRtException.checkNull( aObjSkid );
    TsIllegalArgumentRtException.checkTrue( aObjSkid == Skid.NONE );
    StridUtils.checkValidIdPath( aLinkId );
    IdChain chain = new IdChain( aObjSkid.classId(), aObjSkid.strid(), aLinkId );
    return Ugwi.of( KIND_ID, chain.canonicalString() );
  }

  /**
   * Возвращает признак существования объекта и связи, на которые указывает {@link Ugwi}.<br>
   *
   * @param aUgwi {@link Ugwi} - ИД сущности
   * @param aCoreApi {@link ISkCoreApi} - API сервера
   * @return <b>true</b> - сущность есть<br>
   *         <b>false</b> - сущность отсутствует
   */
  public static boolean isEntityExists( Ugwi aUgwi, ISkCoreApi aCoreApi ) {
    if( INSTANCE.validateUgwi( aUgwi ) != ValidationResult.SUCCESS ) {
      return false;
    }
    TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( KIND_ID ) );
    ISkObject skObj = aCoreApi.objService().find( getSkid( aUgwi ) );
    if( skObj != null ) {
      return skObj.classInfo().links().list().hasKey( getLinkId( aUgwi ) );
    }
    return false;
  }

}
