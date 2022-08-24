package org.toxsoft.uskat.users.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.users.gui.km5.ISkResources.*;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of {@link ISkRole}.
 *
 * @author hazard157
 */
class SkRoleM5Model
    extends KM5ModelBasic<ISkRole> {

  /**
   * Attribute {@link ISkRole#id()}
   */
  public final M5AttributeFieldDef<ISkRole> ID = new M5AttributeFieldDef<>( FID_ID, EAtomicType.STRING ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_FDEF_ID, STR_D_FDEF_ID );
      setDefaultValue( AvUtils.avStr( IStridable.NONE_ID ) );
      setFlags( M5FF_INVARIANT );
    }

    @Override
    protected IAtomicValue doGetFieldValue( ISkRole aEntity ) {
      return AvUtils.avStr( aEntity.id() );
    }

  };

  /**
   * Attribute {@link ISkRole#isEnabled()}
   */
  public final M5AttributeFieldDef<ISkRole> ACTIVE =
      new M5AttributeFieldDef<>( ISkUserServiceHardConstants.ATRID_ROLE_IS_ENABLED, EAtomicType.BOOLEAN ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_FDEF_ACTIVE, STR_D_FDEF_ACTIVE );
          setFlags( M5FF_COLUMN );
          setDefaultValue( AvUtils.avBool( true ) );
        }

        @Override
        protected IAtomicValue doGetFieldValue( ISkRole aEntity ) {
          return AvUtils.avBool( aEntity.isEnabled() );
        }

      };

  /**
   * Attribute {@link ISkRole#isHidden()}
   */
  public final M5AttributeFieldDef<ISkRole> HIDDEN =
      new M5AttributeFieldDef<>( ISkUserServiceHardConstants.ATRID_ROLE_IS_HIDDEN, EAtomicType.BOOLEAN ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_FDEF_HIDDEN, STR_D_FDEF_HIDDEN );
          setFlags( M5FF_COLUMN );
          setDefaultValue( AvUtils.avBool( false ) );
        }

        @Override
        protected IAtomicValue doGetFieldValue( ISkRole aEntity ) {
          return AvUtils.avBool( aEntity.isHidden() );
        }

      };

  public SkRoleM5Model( ISkConnection aConn ) {
    super( ISkRole.CLASS_ID, ISkRole.class, aConn );
    setNameAndDescription( STR_N_ROLE, STR_D_ROLE );
    // fields
    NAME.setNameAndDescription( STR_N_FDEF_NAME, STR_D_FDEF_NAME );
    DESCRIPTION.setNameAndDescription( STR_N_FDEF_DESCR, STR_D_FDEF_DESCR );
    addFieldDefs( ID, NAME, ACTIVE, HIDDEN, DESCRIPTION );
  }

}
