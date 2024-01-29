package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.ITsHardConstants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.gui.km5.sded.sded.editors.*;

/**
 * Модель объектов типа {@link LinkIdSkidList}.
 *
 * @author dima
 */
public class LinkIdSkidListM5Model
    extends M5Model<LinkIdSkidList> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = TS_ID + ".LinkIdSkidList"; //$NON-NLS-1$

  /**
   * ID of the field {@link #SKID_LIST}.
   */
  public static final String FID_SKID_LIST = "SkidList"; //$NON-NLS-1$

  /**
   * Attribute {@link LinkIdSkidList#linkId()}
   */
  public final M5AttributeFieldDef<LinkIdSkidList> LINK_ID = new M5AttributeFieldDef<>( FID_ID, DDEF_STRING ) {

    @Override
    protected void doInit() {
      setNameAndDescription( "linkId", "link ID" );
      setDefaultValue( DEFAULT_ID_AV );
      setFlags( M5FF_COLUMN );
    }

    @Override
    protected IAtomicValue doGetFieldValue( LinkIdSkidList aEntity ) {
      return avStr( aEntity.linkId() );
    }

  };

  /**
   * Attribute {@link LinkIdSkidList#skidList()}
   */
  public final M5AttributeFieldDef<LinkIdSkidList> SKID_LIST = new M5AttributeFieldDef<>( FID_SKID_LIST, VALOBJ, //
      TSID_NAME, "SkidList", //
      TSID_DESCRIPTION, "SkidList", //
      TSID_KEEPER_ID, SkidListKeeper.KEEPER_ID, //
      OPID_EDITOR_FACTORY_NAME, ValedAvValobjSkidListEditor.FACTORY_NAME //
  ) {

    protected void doInit() {
      setFlags( M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( LinkIdSkidList aEntity ) {
      return AvUtils.avValobj( aEntity.skidList() );
    }

  };

  class LifecycleManager
      extends M5LifecycleManager<LinkIdSkidList, Object> {

    public LifecycleManager( IM5Model<LinkIdSkidList> aModel ) {
      super( aModel, true, true, true, false, null );
    }

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<LinkIdSkidList> aValues ) {
      String id = LINK_ID.getFieldValue( aValues ).asString();
      return StridUtils.validateIdPath( id );
    }

    @Override
    protected LinkIdSkidList doCreate( IM5Bunch<LinkIdSkidList> aValues ) {
      String linkIid = LINK_ID.getFieldValue( aValues ).asString();
      IList<Skid> rightSkids = aValues.getAsAv( FID_SKID_LIST ).asValobj();
      SkidList skidList = new SkidList( rightSkids );

      return new LinkIdSkidList( linkIid, skidList );
    }

    @Override
    protected ValidationResult doBeforeEdit( IM5Bunch<LinkIdSkidList> aValues ) {
      String id = LINK_ID.getFieldValue( aValues ).asString();
      return StridUtils.validateIdPath( id );
    }

    @Override
    protected LinkIdSkidList doEdit( IM5Bunch<LinkIdSkidList> aValues ) {
      return doCreate( aValues );
    }

    @Override
    protected void doRemove( LinkIdSkidList aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   */
  public LinkIdSkidListM5Model() {
    super( MODEL_ID, LinkIdSkidList.class );
    setNameAndDescription( "LinkIdSkidList", "LinkIdSkidList" );
    addFieldDefs( LINK_ID, SKID_LIST );
  }

  @Override
  protected IM5LifecycleManager<LinkIdSkidList> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<LinkIdSkidList> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
