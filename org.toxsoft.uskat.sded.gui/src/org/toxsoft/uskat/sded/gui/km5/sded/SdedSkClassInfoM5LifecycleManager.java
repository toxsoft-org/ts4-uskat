package org.toxsoft.uskat.sded.gui.km5.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.sded.gui.km5.IKM5SdedConstants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * LM class for this model.
 * <p>
 * Allows only enumeration of classes, no editing is allowed.
 *
 * @author hazard157
 */
class SdedSkClassInfoM5LifecycleManager
    extends M5LifecycleManager<ISkClassInfo, ISkConnection>
    implements ISkConnected {

  public SdedSkClassInfoM5LifecycleManager( IM5Model<ISkClassInfo> aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

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

    // TODO SdedSkClassInfoM5LifecycleManager.makeDtoClassInfo()

    return cinf;
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<ISkClassInfo> aValues ) {
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return skSysdescr().svs().validator().canCreateClass( dtoClassInfo );
  }

  @Override
  protected ISkClassInfo doCreate( IM5Bunch<ISkClassInfo> aValues ) {
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return skSysdescr().defineClass( dtoClassInfo );
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<ISkClassInfo> aValues ) {
    IDtoClassInfo dtoClassInfo = makeDtoClassInfo( aValues );
    return skSysdescr().svs().validator().canEditClass( dtoClassInfo, aValues.originalEntity() );
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
