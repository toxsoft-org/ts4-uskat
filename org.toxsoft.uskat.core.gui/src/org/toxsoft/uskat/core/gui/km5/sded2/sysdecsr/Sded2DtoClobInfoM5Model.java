package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoClobInfo}.
 *
 * @author hazard157
 */
public class Sded2DtoClobInfoM5Model
    extends Sded2DtoPropInfoM5ModelBase<IDtoClobInfo> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".DtoClobInfo"; //$NON-NLS-1$

  /**
   * LM for this model.
   *
   * @author hazard157
   */
  class LifecycleManager
      extends LmBase {

    public LifecycleManager( IM5Model<IDtoClobInfo> aModel ) {
      super( aModel );
    }

    private IDtoClobInfo makeClobInfo( IM5Bunch<IDtoClobInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      DtoClobInfo inf = DtoClobInfo.create1( id, params );
      return inf;
    }

    @Override
    protected IDtoClobInfo doCreate( IM5Bunch<IDtoClobInfo> aValues ) {
      return makeClobInfo( aValues );
    }

    @Override
    protected IDtoClobInfo doEdit( IM5Bunch<IDtoClobInfo> aValues ) {
      return makeClobInfo( aValues );
    }

    @Override
    protected void doRemove( IDtoClobInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public Sded2DtoClobInfoM5Model( ISkConnection aConn ) {
    super( MODEL_ID, IDtoClobInfo.class, ESkClassPropKind.CLOB, aConn );
    setNameAndDescription( CLOB.nmName(), CLOB.description() );
    // no additional fields
  }

  @Override
  protected IM5LifecycleManager<IDtoClobInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoClobInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
