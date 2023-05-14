package org.toxsoft.uskat.backend.s5.gui.m5.hostlist;

import static org.toxsoft.uskat.backend.s5.gui.m5.ISkBackendS5M5Constants.*;
import static org.toxsoft.uskat.backend.s5.gui.m5.hostlist.ISkResources.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.s5.common.*;

/**
 * M5-model of {@link S5HostListM5Model}.
 *
 * @author hazard157
 */
public class S5HostListM5Model
    extends M5Model<S5HostList> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = S5GUI_M5_ID + ".S5HostList"; //$NON-NLS-1$

  /**
   * ID of the field {@link #HOSTS}.
   */
  public static final String FID_HOSTS = "hosts"; //$NON-NLS-1$

  /**
   * Field {@link S5HostList} itself as {@link IList}&lt;{@link S5Host}&gt;.
   */
  public static final IM5MultiModownFieldDef<S5HostList, S5Host> HOSTS =
      new M5MultiModownFieldDef<>( FID_HOSTS, S5HostM5Model.MODEL_ID ) {

        protected IList<S5Host> doGetFieldValue( S5HostList aEntity ) {
          return aEntity;
        }

      };

  /**
   * Constructor.
   */
  public S5HostListM5Model() {
    super( MODEL_ID, S5HostList.class );
    setNameAndDescription( STR_N_S5HOSTLIST_ADDRESS, STR_D_S5HOSTLIST_ADDRESS );
    addFieldDefs( HOSTS );
  }

  @Override
  protected IM5LifecycleManager<S5HostList> doCreateDefaultLifecycleManager() {
    return new S5HostListM5LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<S5HostList> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
