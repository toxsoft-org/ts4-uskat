package org.toxsoft.uskat.users.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Lifecylce manager for {@link SkRoleM5Model}.
 *
 * @author dima
 */
public class SkRoleM5LifecycleManager
    extends KM5LifecycleManagerBasic<ISkRole, ISkConnection> {

  // FIXME create SkRoleMpc with filering out hidden roles

  /**
   * Constructor.
   *
   * @param aModel {@link IM5Model}&lt;T&gt; - the model
   * @param aMaster &lt;M&gt; - master object, may be <code>null</code>
   * @throws TsNullArgumentRtException model is <code>null</code>
   */
  public SkRoleM5LifecycleManager( IM5Model<ISkRole> aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IDtoObject makeRoleDto( IM5Bunch<ISkRole> aValues ) {
    // Создаем IDpuObject и инициализируем его значениями из пучка
    String id = aValues.getAsAv( AID_STRID ).asString();
    Skid skid = new Skid( ISkRole.CLASS_ID, id );
    DtoObject dtoRole = DtoObject.createDtoObject( skid, coreApi() );
    dtoRole.attrs().setValue( ATRID_ROLE_IS_ENABLED, aValues.getAsAv( ATRID_ROLE_IS_ENABLED ) );
    dtoRole.attrs().setValue( ATRID_ROLE_IS_HIDDEN, aValues.getAsAv( ATRID_ROLE_IS_HIDDEN ) );
    dtoRole.attrs().setValue( FID_NAME, aValues.getAsAv( FID_NAME ) );
    dtoRole.attrs().setValue( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ) );
    return dtoRole;
  }

  private ISkUserService userService() {
    return master().coreApi().userService();
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<ISkRole> aValues ) {
    IDtoObject dtoRole = makeRoleDto( aValues );
    return userService().svs().validator().canCreateRole( dtoRole );
  }

  @Override
  protected ISkRole doCreate( IM5Bunch<ISkRole> aValues ) {
    IDtoObject dtoRole = makeRoleDto( aValues );
    return userService().defineRole( dtoRole );
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<ISkRole> aValues ) {
    IDtoObject dtoRole = makeRoleDto( aValues );
    return userService().svs().validator().canEditRole( dtoRole, aValues.originalEntity() );
  }

  @Override
  protected ISkRole doEdit( IM5Bunch<ISkRole> aValues ) {
    IDtoObject dtoRole = makeRoleDto( aValues );
    return userService().defineRole( dtoRole );
  }

  @Override
  protected ValidationResult doBeforeRemove( ISkRole aEntity ) {
    return userService().svs().validator().canRemoveRole( aEntity.id() );
  }

  @Override
  protected void doRemove( ISkRole aEntity ) {
    userService().removeRole( aEntity.id() );
  }

  @Override
  protected IList<ISkRole> doListEntities() {
    return userService().listRoles();
  }

}
