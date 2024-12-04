package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.av.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.gui.km5.sded.sded.editors.*;

/**
 * M5-model of the {@link IMappedSkids}.
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
      TSID_NAME, STR_MAPPED_SKIDS_LINK_ID, //
      TSID_DESCRIPTION, STR_MAPPED_SKIDS_LINK_ID, //
      IValedControlConstants.OPID_EDITOR_FACTORY_NAME, ValedAvStringText.FACTORY_NAME ) {

    @Override
    protected void doInit() {
      setFlags( M5FF_COLUMN | M5FF_READ_ONLY );
    }

    protected IAtomicValue doGetFieldValue( IMappedSkids aEntity ) {
      String linkId = aEntity.map().keys().first();
      return AvUtils.avStr( linkId );
    }

  };
  /**
   * Attribute {@link IVtGraphParam#setPoints() } description of parameter
   */
  // public M5MultiModownFieldDef<IMappedSkids, String> LINK_ID =
  // new M5MultiModownFieldDef<>( FID_LINK_ID, StringM5Model.MODEL_ID ) //
  //
  // {
  //
  // @Override
  // protected void doInit() {
  // setNameAndDescription( STR_N_MAPPED_SKIDS_LINK_ID, STR_N_MAPPED_SKIDS_LINK_ID );
  // setFlags( M5FF_COLUMN );
  // // // панель высотой в 4 строки
  // // params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 4 ); //
  // // // строка поиска не нужна
  // // params().setBool( IMultiPaneComponentConstants.OPDEF_IS_FILTER_PANE, false );
  // // // прячем заголовок таблицы
  // // params().setBool( TsTreeViewer.OPDEF_IS_HEADER_SHOWN, false );
  // // прячем тулбар
  // // params().setBool( IMultiPaneComponentConstants.OPDEF_IS_TOOLBAR, false );
  // }
  //
  // protected IStringList doGetFieldValue( IMappedSkids aEntity ) {
  // return aEntity.map().keys();
  // }
  //
  // };

  /**
   * Attribute {@link IMappedSkids#rightSkids() } Green world ID
   */
  // public M5AttributeFieldDef<IMappedSkids> RIGHT_SKIDS = new M5AttributeFieldDef<>( FID_RIGHT_SKIDS, VALOBJ, //
  // TSID_NAME, STR_N_MAPPED_SKIDS_RIGHT_SKIDS, //
  // TSID_DESCRIPTION, STR_D_MAPPED_SKIDS_RIGHT_SKIDS, //
  // TSID_KEEPER_ID, SkidListKeeper.KEEPER_ID //
  // /*
  // * FIXME , // OPID_EDITOR_FACTORY_NAME, ValedAvValobjSkidListEditor.FACTORY_NAME
  // */
  // ) {
  //
  // @Override
  // protected void doInit() {
  // setFlags( M5FF_COLUMN );
  // }
  //
  // protected IAtomicValue doGetFieldValue( IMappedSkids aEntity ) {
  // String linkId = aEntity.map().keys().first();
  // return AvUtils.avValobj( aEntity.map().getByKey( linkId ) );
  // }
  //
  // };
  /**
   * Attribute {@link IMappedSkids#map() } Green world ID
   */
  public M5AttributeFieldDef<IMappedSkids> RIGHT_SKIDS = new M5AttributeFieldDef<>( FID_RIGHT_SKIDS, EAtomicType.VALOBJ, //
      TSID_NAME, STR_MAPPED_SKIDS_RIGHT_SKIDS, //
      TSID_DESCRIPTION, STR_MAPPED_SKIDS_RIGHT_SKIDS_D, //
      TSID_KEEPER_ID, SkidListKeeper.KEEPER_ID, //
      IValedControlConstants.OPID_EDITOR_FACTORY_NAME, ValedAvValobjSkidListEditor.FACTORY_NAME //
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
   * Поле классов связи.
   */

  // public final M5MultiLookupFieldDef<IMappedSkids, Skid> RIGHT_SKIDS =
  // new M5MultiLookupKeyFieldDef<>( FID_RIGHT_SKIDS, IGwM5Constants.MID_SKID, FID_LINK_ID, String.class ) {
  //
  // @Override
  // protected void doInit() {
  // setNameAndDescription( STR_N_MAPPED_SKIDS_RIGHT_SKIDS, STR_D_MAPPED_SKIDS_RIGHT_SKIDS );
  // setFlags( M5FF_COLUMN );
  // }
  //
  // @Override
  // protected IList<Skid> doGetFieldValue( IMappedSkids aEntity ) {
  // return aEntity.map().getByKey( keyFieldId() );
  // }
  //
  // @Override
  // protected String doGetFieldValueName( IMappedSkids aEntity ) {
  // IList<Skid> skidList = doGetFieldValue( aEntity );
  //
  // StringBuilder sb = new StringBuilder();
  //
  // String add = TsLibUtils.EMPTY_STRING;
  // for( Skid skid : skidList ) {
  // sb.append( add );
  // sb.append( skid.toString() );
  // add = ", "; //$NON-NLS-1$
  // }
  //
  // return sb.toString();
  // }
  //
  // };

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
