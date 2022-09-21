package org.toxsoft.uskat.base.gui.km5.sgw;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.base.gui.km5.sgw.ISgwM5Constants.*;
import static org.toxsoft.uskat.base.gui.km5.sgw.ISkResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link ISkClassInfo}.
 *
 * @author hazard157
 */
public class SgwSkClassInfoM5Model
    extends KM5ConnectedModelBase<ISkClassInfo> {

  /**
   * Attribute {@link ISkClassInfo#id()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> CLASS_ID = new M5AttributeFieldDef<>( FID_CLASS_ID, DDEF_IDPATH ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_CLASS_ID, STR_D_CLASS_ID );
      setFlags( M5FF_READ_ONLY | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
      return avStr( aEntity.id() );
    }

  };

  /**
   * Attribute {@link ISkClassInfo#parentId()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> PARENT_ID = new M5AttributeFieldDef<>( FID_PARENT_ID, DDEF_STRING ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PARENT_ID, STR_D_PARENT_ID );
      setFlags( M5FF_READ_ONLY | M5FF_DETAIL );
    }

    protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
      return avStr( aEntity.parentId() );
    }

  };

  /**
   * Attribute {@link ISkClassInfo#nmName()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_CLASS_NAME, STR_D_CLASS_NAME );
      setFlags( M5FF_READ_ONLY | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
      return avStr( aEntity.nmName() );
    }

  };

  /**
   * Attribute {@link ISkClassInfo#description()}.
   */
  public final IM5AttributeFieldDef<ISkClassInfo> DESCRIPTION =
      new M5AttributeFieldDef<>( FID_DESCRIPTION, DDEF_DESCRIPTION ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_CLASS_DESCRIPTION, STR_D_CLASS_DESCRIPTION );
          setFlags( M5FF_READ_ONLY | M5FF_COLUMN );
        }

        protected IAtomicValue doGetFieldValue( ISkClassInfo aEntity ) {
          return avStr( aEntity.description() );
        }

      };

  /**
   * LM class for this model.
   * <p>
   * Allows only enumeration of classes, no editing is allowed.
   *
   * @author hazard157
   */
  static class LifecycleManager
      extends M5LifecycleManager<ISkClassInfo, ISkConnection> {

    public LifecycleManager( IM5Model<ISkClassInfo> aModel, ISkConnection aMaster ) {
      super( aModel, false, false, false, true, aMaster );
      TsNullArgumentRtException.checkNull( aMaster );
    }

    @Override
    protected IList<ISkClassInfo> doListEntities() {
      return master().coreApi().sysdescr().listClasses();
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwSkClassInfoM5Model( ISkConnection aConn ) {
    super( MID_SGW_CLASS_INFO, ISkClassInfo.class, aConn );
    addFieldDefs( CLASS_ID, NAME, PARENT_ID, DESCRIPTION );
    // HERE add more fields
  }

  // ------------------------------------------------------------------------------------
  // M5Model
  //

  @Override
  protected IM5LifecycleManager<ISkClassInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this, skConn() );
  }

  @Override
  protected IM5LifecycleManager<ISkClassInfo> doCreateLifecycleManager( Object aMaster ) {
    return new LifecycleManager( this, ISkConnection.class.cast( aMaster ) );
  }

}
