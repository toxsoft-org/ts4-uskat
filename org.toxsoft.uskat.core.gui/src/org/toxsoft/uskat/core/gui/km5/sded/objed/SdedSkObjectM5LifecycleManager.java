package org.toxsoft.uskat.core.gui.km5.sded.objed;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * LM class for this model.
 * <p>
 *
 * @author dima
 */
class SdedSkObjectM5LifecycleManager
    extends M5LifecycleManager<ISkObject, ISkConnection>
    implements ISkConnected {

  private SdedSkObjectMpc mpc;

  public void setMpc( SdedSkObjectMpc aMpc ) {
    mpc = aMpc;
  }

  public SdedSkObjectM5LifecycleManager( IM5Model<ISkObject> aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
  }

  private IDtoObject makeDtoObject( IM5Bunch<ISkObject> aValues ) {

    String classId = aValues.getAsAv( ISkHardConstants.AID_CLASS_ID ).asString();
    String id = aValues.getAsAv( ISkHardConstants.AID_STRID ).asString();
    Skid skid = new Skid( classId, id );
    DtoObject dtoObject = DtoObject.createDtoObject( skid, coreApi() );
    dtoObject.attrs().setValue( ISkHardConstants.AID_NAME, aValues.getAsAv( ISkHardConstants.AID_NAME ) );
    dtoObject.attrs().setValue( ISkHardConstants.AID_DESCRIPTION, aValues.getAsAv( ISkHardConstants.AID_DESCRIPTION ) );
    return dtoObject;

  }

  // ------------------------------------------------------------------------------------
  // implementation
  //
  // @Override
  // protected ValidationResult doBeforeCreate( IM5Bunch<ISkObject> aValues ) {
  // IDtoObject dtoObject = makeDtoObject( aValues );
  // return skObjServ().svs().validator().canCreateObject( dtoObject );
  // }

  /**
   * Subclass may perform additional tuning of the values for new entity creation.
   * <p>
   * In base class does nothing, there is no need to call superclass method when overriding.
   *
   * @param aValues {@link IM5BunchEdit} - new editable bunch with default field values
   */
  @Override
  protected void doSetupNewItemValues( IM5BunchEdit<ISkObject> aValues ) {
    // id класса берем из дерева в панели
    String classId = mpc.getSelClass().id();
    aValues.set( ISkHardConstants.AID_CLASS_ID, avStr( classId ) );
  }

  @Override
  protected ISkObject doCreate( IM5Bunch<ISkObject> aValues ) {
    IDtoObject dtoObject = makeDtoObject( aValues );
    return skObjServ().defineObject( dtoObject );
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<ISkObject> aValues ) {
    IDtoObject dtoObject = makeDtoObject( aValues );
    return skObjServ().svs().validator().canEditObject( dtoObject, aValues.originalEntity() );
  }

  @Override
  protected ISkObject doEdit( IM5Bunch<ISkObject> aValues ) {
    IDtoObject dtoObject = makeDtoObject( aValues );
    return skObjServ().defineObject( dtoObject );
  }

  @Override
  protected ValidationResult doBeforeRemove( ISkObject aEntity ) {
    return skObjServ().svs().validator().canRemoveObject( aEntity.skid() );
  }

  @Override
  protected void doRemove( ISkObject aEntity ) {
    skObjServ().removeObject( aEntity.skid() );
  }

  @Override
  protected IList<ISkObject> doListEntities() {
    return skObjServ().listObjs( IGwHardConstants.GW_ROOT_CLASS_ID, true );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return master();
  }

}
