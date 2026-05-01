package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * LM for {@link Sded2SkClassInfoM5Model}.
 * <p>
 * Allows to enumerate classes, create and class without properties such as attributes, links, etc.
 *
 * @author hazard157
 */
class SkClassM5LifecycleManager
    extends M5LifecycleManager<ISkClassInfo, ISkConnection>
    implements ISkConnected {

  /**
   * Maps {@link ESkClassPropKind} to the field ID in M5-model {@link Sded2SkClassInfoM5Model}.
   */
  private static final IMap<ESkClassPropKind, String> propFieldIds;

  static {
    IMapEdit<ESkClassPropKind, String> mm = new ElemMap<>();
    mm.put( ESkClassPropKind.ATTR, FID_SELF_ATTR_INFOS );
    mm.put( ESkClassPropKind.CLOB, FID_SELF_CLOB_INFOS );
    mm.put( ESkClassPropKind.CMD, FID_SELF_CMD_INFOS );
    mm.put( ESkClassPropKind.EVENT, FID_SELF_EVENT_INFOS );
    mm.put( ESkClassPropKind.LINK, FID_SELF_LINK_INFOS );
    mm.put( ESkClassPropKind.RIVET, FID_SELF_RIVET_INFOS );
    mm.put( ESkClassPropKind.RTDATA, FID_SELF_RTDATA_INFOS );
    TsInternalErrorRtException.checkFalse( mm.size() == ESkClassPropKind.asList().size() );
    propFieldIds = mm;
  }

  public SkClassM5LifecycleManager( IM5Model<ISkClassInfo> aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static <T extends IDtoClassPropInfoBase> ValidationResult validatePropsList( IList<T> aProps,
      ESkClassPropKind aKind ) {

    // TODO check list does not contains duplicated items

    return ValidationResult.SUCCESS;
  }

  private static ValidationResult validateDtoClassInfo( IM5Bunch<ISkClassInfo> aValues ) {
    ValidationResult vr = ValidationResult.SUCCESS;
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      IList<? extends IDtoClassPropInfoBase> llProps = aValues.getAs( propFieldIds.getByKey( k ), IList.class );
      vr = ValidationResult.firstNonOk( vr, validatePropsList( llProps, k ) );
      if( vr.isError() ) {
        return vr;
      }
    }
    return vr;
  }

  private static IDtoClassInfo makeDtoClassInfo( IM5Bunch<ISkClassInfo> aValues ) {
    String id = aValues.getAsAv( FID_CLASS_ID ).asString();
    String parentId = aValues.getAs( FID_PARENT_ID, String.class );
    IOptionSetEdit params = new OptionSet();
    if( aValues.originalEntity() != null ) {
      params.setAll( aValues.originalEntity().params() );
    }
    params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
    params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
    DtoClassInfo cinf = new DtoClassInfo( id, parentId, params );
    cinf.attrInfos().setAll( aValues.getAs( FID_SELF_ATTR_INFOS, IList.class ) );
    cinf.linkInfos().setAll( aValues.getAs( FID_SELF_LINK_INFOS, IList.class ) );
    cinf.rivetInfos().setAll( aValues.getAs( FID_SELF_RIVET_INFOS, IList.class ) );
    cinf.rtdataInfos().setAll( aValues.getAs( FID_SELF_RTDATA_INFOS, IList.class ) );
    cinf.cmdInfos().setAll( aValues.getAs( FID_SELF_CMD_INFOS, IList.class ) );
    cinf.eventInfos().setAll( aValues.getAs( FID_SELF_EVENT_INFOS, IList.class ) );
    cinf.clobInfos().setAll( aValues.getAs( FID_SELF_CLOB_INFOS, IList.class ) );
    return cinf;
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<ISkClassInfo> aValues ) {
    ValidationResult vr = validateDtoClassInfo( aValues );
    if( vr.isError() ) {
      return vr;
    }
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return ValidationResult.firstNonOk( vr, skSysdescr().svs().validator().canCreateClass( dtoClassInfo ) );
  }

  @Override
  protected ISkClassInfo doCreate( IM5Bunch<ISkClassInfo> aValues ) {
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return skSysdescr().defineClass( dtoClassInfo );
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<ISkClassInfo> aValues ) {
    ValidationResult vr = validateDtoClassInfo( aValues );
    if( vr.isError() ) {
      return vr;
    }
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return ValidationResult.firstNonOk( vr,
        skSysdescr().svs().validator().canEditClass( dtoClassInfo, aValues.originalEntity() ) );
  }

  @Override
  protected ISkClassInfo doEdit( IM5Bunch<ISkClassInfo> aValues ) {
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return skSysdescr().defineClass( dtoClassInfo );
  }

  @Override
  protected ValidationResult doBeforeRemove( ISkClassInfo aEntity ) {
    return skSysdescr().svs().validator().canRemoveClass( aEntity.id() );
  }

  @Override
  protected void doRemove( ISkClassInfo aEntity ) {
    skSysdescr().removeClass( aEntity.id() );
  }

  @Override
  protected IList<ISkClassInfo> doListEntities() {
    return master().coreApi().sysdescr().listClasses();
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return master();
  }

}
