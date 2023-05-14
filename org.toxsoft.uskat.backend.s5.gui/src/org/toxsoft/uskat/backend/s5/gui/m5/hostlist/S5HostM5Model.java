package org.toxsoft.uskat.backend.s5.gui.m5.hostlist;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.s5.gui.m5.hostlist.ISkResources.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.uskat.s5.common.*;

/**
 * M5-model of {@link S5Host}.
 *
 * @author hazard157
 */
public class S5HostM5Model
    extends M5Model<S5Host> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = M5_ID + ".S5Host"; //$NON-NLS-1$

  /**
   * ID of the field {@link #ADDRESS}.
   */
  public static final String FID_ADDRESS = "address"; //$NON-NLS-1$

  /**
   * ID of the field {@link #PORT}.
   */
  public static final String FID_PORT = "port"; //$NON-NLS-1$

  /**
   * Field {@link S5Host#address()}.
   */
  public static final IM5AttributeFieldDef<S5Host> ADDRESS = new M5AttributeFieldDef<>( FID_ADDRESS, STRING, //
      TSID_NAME, STR_N_S5HOST_ADDRESS, //
      TSID_DESCRIPTION, STR_D_S5HOST_ADDRESS, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY, //
      M5_OPDEF_FLAGS, avInt( M5FF_COLUMN ) //
  ) {

    @Override
    protected IAtomicValue doGetFieldValue( S5Host aEntity ) {
      return avStr( aEntity.address() );
    }

  };

  /**
   * Field {@link S5Host#port()}.
   */
  public static final IM5AttributeFieldDef<S5Host> PORT = new M5AttributeFieldDef<>( FID_PORT, INTEGER, //
      TSID_NAME, STR_N_S5HOST_PORT, //
      TSID_DESCRIPTION, STR_D_S5HOST_PORT, //
      TSID_DEFAULT_VALUE, avInt( 8080 ), //
      M5_OPDEF_FLAGS, avInt( M5FF_COLUMN ) //
  ) {

    @Override
    protected IAtomicValue doGetFieldValue( S5Host aEntity ) {
      return avInt( aEntity.port() );
    }

  };

  /**
   * Constructor.
   */
  public S5HostM5Model() {
    super( MODEL_ID, S5Host.class );
    setNameAndDescription( STR_N_M5M_S5HOST, STR_D_M5M_S5HOST );
    addFieldDefs( ADDRESS, PORT );
  }

  @Override
  protected IM5LifecycleManager<S5Host> doCreateLifecycleManager( Object aMaster ) {
    return new S5HostM5LifecycleManager( this, S5HostList.class.cast( aMaster ) );
  }

}
