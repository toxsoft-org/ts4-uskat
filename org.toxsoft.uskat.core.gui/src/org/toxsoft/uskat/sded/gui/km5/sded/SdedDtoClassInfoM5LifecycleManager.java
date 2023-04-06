package org.toxsoft.uskat.sded.gui.km5.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.sded.gui.km5.IKM5SdedConstants.*;
import static org.toxsoft.uskat.sded.gui.km5.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * LM class for this model.
 * <p>
 * Allows only enumeration of classes, no editing is allowed.
 *
 * @author hazard157
 */
class SdedDtoClassInfoM5LifecycleManager
    extends M5LifecycleManager<IDtoClassInfo, ISkConnection> {

  public SdedDtoClassInfoM5LifecycleManager( SdedDtoClassInfoM5Model aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
  }

  @Override
  public SdedDtoClassInfoM5Model model() {
    return (SdedDtoClassInfoM5Model)super.model();
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IDtoClassInfo makeDtoClassInfo( IM5Bunch<IDtoClassInfo> aValues ) {
    String id = aValues.getAsAv( FID_CLASS_ID ).asString();
    String parentId = aValues.getAs( FID_PARENT_ID, String.class );
    IOptionSetEdit params = new OptionSet();
    if( aValues.originalEntity() != null ) {
      params.setAll( aValues.originalEntity().params() );
    }
    params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
    params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
    DtoClassInfo cinf;
    if( id.equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
      cinf = new DtoClassInfo( params );
    }
    else {
      cinf = new DtoClassInfo( id, parentId, params );
    }
    cinf.attrInfos().setAll( model().SELF_ATTR_INFOS.getFieldValue( aValues ) );
    cinf.rtdataInfos().setAll( model().SELF_RTDATA_INFOS.getFieldValue( aValues ) );
    cinf.cmdInfos().setAll( model().SELF_CMD_INFOS.getFieldValue( aValues ) );
    cinf.eventInfos().setAll( model().SELF_EVENT_INFOS.getFieldValue( aValues ) );
    cinf.linkInfos().setAll( model().SELF_LINK_INFOS.getFieldValue( aValues ) );
    cinf.rivetInfos().setAll( model().SELF_RIVET_INFOS.getFieldValue( aValues ) );

    // TODO SdedSkClassInfoM5LifecycleManager.makeDtoClassInfo()

    return cinf;
  }

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<IDtoClassInfo> aValues ) {
    String id = aValues.getAsAv( FID_CLASS_ID ).asString();
    if( !StridUtils.isValidIdPath( id ) ) {
      return ValidationResult.error( FMT_ERR_ID_NOT_IDPATH, id );
    }
    String parentId = aValues.getAs( FID_PARENT_ID, String.class );
    if( !StridUtils.isValidIdPath( parentId ) ) {
      // FIXME ???
      return ValidationResult.error( FMT_ERR_ID_NOT_IDPATH, parentId );
    }
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return master().coreApi().sysdescr().svs().validator().canCreateClass( dtoClassInfo );
  }

  @Override
  protected IDtoClassInfo doCreate( IM5Bunch<IDtoClassInfo> aValues ) {
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    ISkClassInfo cinf = master().coreApi().sysdescr().defineClass( dtoClassInfo );
    return DtoClassInfo.createFromSk( cinf, true );
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<IDtoClassInfo> aValues ) {
    String id = aValues.getAsAv( FID_CLASS_ID ).asString();
    if( !StridUtils.isValidIdPath( id ) ) {
      return ValidationResult.error( FMT_ERR_ID_NOT_IDPATH, id );
    }
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    ISkClassInfo cinf = master().coreApi().sysdescr().getClassInfo( aValues.originalEntity().id() );
    return master().coreApi().sysdescr().svs().validator().canEditClass( dtoClassInfo, cinf );
  }

  @Override
  protected IDtoClassInfo doEdit( IM5Bunch<IDtoClassInfo> aValues ) {
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    ISkClassInfo cinf = master().coreApi().sysdescr().defineClass( dtoClassInfo );
    return DtoClassInfo.createFromSk( cinf, true );
  }

  @Override
  protected ValidationResult doBeforeRemove( IDtoClassInfo aEntity ) {
    return master().coreApi().sysdescr().svs().validator().canRemoveClass( aEntity.id() );
  }

  @Override
  protected void doRemove( IDtoClassInfo aEntity ) {
    master().coreApi().sysdescr().removeClass( aEntity.id() );
  }

  @Override
  protected IList<IDtoClassInfo> doListEntities() {
    IList<ISkClassInfo> skClassesList = master().coreApi().sysdescr().listClasses();
    IListEdit<IDtoClassInfo> dtoClassesList = new ElemArrayList<>( skClassesList.size() );
    for( ISkClassInfo skInf : skClassesList ) {
      IDtoClassInfo dtoInf = DtoClassInfo.createFromSk( skInf, true );
      dtoClassesList.add( dtoInf );
    }
    return dtoClassesList;
  }

} // class LifecycleManager
