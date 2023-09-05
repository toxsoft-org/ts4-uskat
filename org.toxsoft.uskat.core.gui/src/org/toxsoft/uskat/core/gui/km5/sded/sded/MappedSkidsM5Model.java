package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-model of the {@link ISkidList}.
 *
 * @author dima
 */
public class MappedSkidsM5Model
    extends KM5ConnectedModelBase<IMappedSkids> {

  /**
   * ID of model.
   */
  public static String M5MODEL_ID = "sded.mid.MappedSkids"; //$NON-NLS-1$

  /**
   * ID of field right Skids of link.
   */
  public String FID_RIGHT_SKIDS = "rightSkids"; //$NON-NLS-1$

  /**
   * link id
   */
  public static final String FID_LINK_ID = "linkId"; //$NON-NLS-1$

  /**
   * Attribute {@link IMappedSkids#map() } id of link
   */
  public M5AttributeFieldDef<IMappedSkids> LINK_ID = new M5AttributeFieldDef<>( FID_LINK_ID, EAtomicType.STRING, //
      TSID_NAME, STR_N_MAPPED_SKIDS_LINK_ID, //
      TSID_DESCRIPTION, STR_N_MAPPED_SKIDS_LINK_ID, //
      OPID_EDITOR_FACTORY_NAME, ValedAvStringText.FACTORY_NAME ) {

    @Override
    protected void doInit() {
      setFlags( M5FF_COLUMN | M5FF_READ_ONLY );
    }

    protected IAtomicValue doGetFieldValue( IMappedSkids aEntity ) {
      String linkId = aEntity.map().keys().first();
      return avStr( linkId );
    }

  };

  /**
   * Attribute {@link IMappedSkids#rightSkids() } Green world ID
   */
  public M5AttributeFieldDef<IMappedSkids> RIGHT_SKIDS = new M5AttributeFieldDef<>( FID_RIGHT_SKIDS, VALOBJ, //
      TSID_NAME, STR_N_MAPPED_SKIDS_RIGHT_SKIDS, //
      TSID_DESCRIPTION, STR_D_MAPPED_SKIDS_RIGHT_SKIDS, //
      TSID_KEEPER_ID, SkidListKeeper.KEEPER_ID/*
                                               * , // OPID_EDITOR_FACTORY_NAME, ValedAvValobjSkidListEditor.FACTORY_NAME
                                               */
  ) {

    @Override
    protected void doInit() {
      setFlags( M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( IMappedSkids aEntity ) {
      String linkId = aEntity.map().keys().first();
      return AvUtils.avValobj( aEntity.map().getByKey( linkId ) );
    }

  };

  /**
   * LM for this model.
   *
   * @author dima
   */
  class LifecycleManager
      extends M5LifecycleManager<IMappedSkids, ISkConnection> {

    public LifecycleManager( IM5Model<IMappedSkids> aModel, ISkConnection aMaster ) {
      super( aModel, false, true, false, false, aMaster );
    }

    private IMappedSkids makeMappedSkids( IM5Bunch<IMappedSkids> aValues ) {
      String linkId = aValues.originalEntity().map().keys().first();
      IList<Skid> rightSkids = aValues.getAsAv( FID_RIGHT_SKIDS ).asValobj();

      MappedSkids retVal = new MappedSkids();
      retVal.ensureSkidList( linkId, rightSkids );
      return retVal;
    }

    @Override
    protected IMappedSkids doCreate( IM5Bunch<IMappedSkids> aValues ) {
      return makeMappedSkids( aValues );
    }

    @Override
    protected IMappedSkids doEdit( IM5Bunch<IMappedSkids> aValues ) {
      IMappedSkids retVal = makeMappedSkids( aValues );
      // master().coreApi().linkService().setLink( retVal );
      return retVal;
    }

    @Override
    protected void doRemove( IMappedSkids aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MappedSkidsM5Model( ISkConnection aConn ) {
    super( M5MODEL_ID, IMappedSkids.class, aConn );
    setNameAndDescription( LINK.nmName(), LINK.description() );
    addFieldDefs( LINK_ID, RIGHT_SKIDS );
  }

  @Override
  protected IM5LifecycleManager<IMappedSkids> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this, skConn() );
  }

  @Override
  protected IM5LifecycleManager<IMappedSkids> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
