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
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * UGWI kind: Sk-object ID ({@link Skid}).
 * <p>
 * Format: Skid canonical string.
 *
 * @author vs
 */
public class UgwiKindSkSkid
    extends AbstractUgwiKind<Skid> {

  private static final IDataType DT_SKID = DataType.create( VALOBJ, //
      TSID_NAME, STR_PROP_SKID, //
      TSID_DESCRIPTION, STR_PROP_SKID_D, //
      TSID_KEEPER_ID, Skid.KEEPER_ID, //
      // OPDEF_EDITOR_FACTORY_NAME, ValedAvValobjSkidEditor.FACTORY_NAME, //
      TSID_DEFAULT_VALUE, avValobj( Skid.CANONICAL_STRING_NONE ) //
  );

  /**
   * {@link ISkUgwiKind} implementation.
   *
   * @author hazard157
   */
  public static class Kind
      extends AbstractSkUgwiKind<Skid> {

    Kind( AbstractUgwiKind<Skid> aStaticKind, ISkCoreApi aCoreApi ) {
      super( aStaticKind, aCoreApi );
    }

    @Override
    public Skid doFindContent( Ugwi aUgwi ) {
      Gwid gwid = ugwiKind().getGwid( aUgwi );
      ISkObject sko = coreApi().objService().find( gwid.skid() );
      if( sko != null ) {
        return gwid.skid();
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
      Skid skid = doFindContent( aUgwi );
      if( skid != null ) {
        return AvUtils.avValobj( skid );
      }
      return IAtomicValue.NULL;
    }

    @Override
    protected IDataType doGetAtomicValueDataType( Ugwi aUgwi ) {
      return DT_SKID;
    }

  }

  /**
   * The UGWI kind ID.
   */
  public static final String KIND_ID = SK_ID + ".sk.skid"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKindSkSkid INSTANCE = new UgwiKindSkSkid();

  /**
   * Constructor.
   */
  private UgwiKindSkSkid() {
    super( KIND_ID, true, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_SKID, //
        TSID_DESCRIPTION, STR_UK_SKID_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    if( Skid.isValidCanonicalString( aEssence ) ) {
      return ValidationResult.SUCCESS;
    }
    return makeGeneralInvalidEssenceVr( aEssence );
  }

  @Override
  protected AbstractSkUgwiKind<?> doCreateUgwiKind( ISkCoreApi aSkConn ) {
    return new Kind( this, aSkConn );
  }

  @Override
  protected Gwid doGetGwid( Ugwi aUgwi ) {
    return Gwid.createObj( Skid.of( aUgwi.essence() ) );
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
  // return getSkid( aUgwi ).classId();
  // }
  //
  // /**
  // * Extracts object STRID from the UGWI of this kind.
  // *
  // * @param aUgwi {@link Ugwi} - the UGWI
  // * @return String - the object STRID
  // * @throws TsNullArgumentRtException any argument = <code>null</code>
  // * @throws TsValidationFailedRtException invalid UGWI for this kind
  // */
  // public static String getObjStrid( Ugwi aUgwi ) {
  // return getSkid( aUgwi ).strid();
  // }
  //
  // /**
  // * Extracts object SKID from the UGWI of this kind.
  // *
  // * @param aUgwi {@link Ugwi} - the UGWI
  // * @return {@link Skid} - the object SKID
  // * @throws TsNullArgumentRtException any argument = <code>null</code>
  // * @throws TsValidationFailedRtException invalid UGWI for this kind
  // */
  // public static Skid getSkid( Ugwi aUgwi ) {
  // TsValidationFailedRtException.checkError( INSTANCE.validateUgwi( aUgwi ) );
  // return Skid.of( aUgwi.essence() );
  // }

  /**
   * Creates the UGWI of this kind.
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
    Skid skid = new Skid( aClassId, aObjStrid );
    return Ugwi.of( KIND_ID, skid.canonicalString() );
  }

  /**
   * Creates the UGWI of this kind.
   *
   * @param aObjSkid {@link Skid} - SKID of the object
   * @return {@link Ugwi} - created UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static Ugwi makeUgwi( Skid aObjSkid ) {
    TsNullArgumentRtException.checkNull( aObjSkid );
    TsIllegalArgumentRtException.checkTrue( aObjSkid == Skid.NONE );
    return Ugwi.of( KIND_ID, aObjSkid.canonicalString() );
  }

  // /**
  // * Возвращает признак существования объекта, на который указывает {@link Ugwi}.<br>
  // *
  // * @param aUgwi {@link Ugwi} - ИД сущности
  // * @param aCoreApi {@link ISkCoreApi} - API сервера
  // * @return <b>true</b> - сущность есть<br>
  // * <b>false</b> - сущность отсутствует
  // */
  // public static boolean isEntityExists( Ugwi aUgwi, ISkCoreApi aCoreApi ) {
  // if( INSTANCE.validateUgwi( aUgwi ) != ValidationResult.SUCCESS ) {
  // return false;
  // }
  // TsIllegalArgumentRtException.checkFalse( aUgwi.kindId().equals( KIND_ID ) );
  // return aCoreApi.objService().find( getSkid( aUgwi ) ) != null;
  // }

}
