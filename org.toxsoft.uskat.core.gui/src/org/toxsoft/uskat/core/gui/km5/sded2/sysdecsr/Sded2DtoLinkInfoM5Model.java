package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.misc.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.sgw.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoLinkInfo}.
 *
 * @author hazard157
 */
public class Sded2DtoLinkInfoM5Model
    extends Sded2DtoPropInfoM5ModelBase<IDtoLinkInfo> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".DtoLinkInfo"; //$NON-NLS-1$

  /**
   * ID of field right class ids of link.
   */
  public static final String FID_RIGHT_CLASS_IDS = "rightClassIds"; //$NON-NLS-1$

  /**
   * ID of field link сonstraints.
   */
  public static final String FID_LINK_CONSTRAINT = "linkConstraint"; //$NON-NLS-1$

  /**
   * MultiLookup field {@link IDtoLinkInfo#rightClassIds()}.
   */
  public final IM5MultiLookupKeyFieldDef<IDtoLinkInfo, ISkClassInfo> CLASS_IDS = new M5MultiLookupKeyFieldDef<>(
      FID_RIGHT_CLASS_IDS, ISgwM5Constants.MID_SGW_CLASS_INFO, ISgwM5Constants.FID_CLASS_ID, ISkClassInfo.class ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_LINK_CLASS_IDS, STR_LINK_CLASS_IDS_D );
      setDefaultValue( IList.EMPTY );
    }

    protected IList<ISkClassInfo> doGetFieldValue( IDtoLinkInfo aEntity ) {
      IStringList classes = aEntity.rightClassIds();
      IListEdit<ISkClassInfo> retVal = new ElemArrayList<>();
      for( String classId : classes ) {
        retVal.add( skConn().coreApi().sysdescr().findClassInfo( classId ) );
      }
      return retVal;
    }

  };

  /**
   * Modown field {@link IDtoLinkInfo#linkConstraint()}.
   */
  public final IM5SingleModownFieldDef<IDtoLinkInfo, CollConstraint> LINK_CONSTRAINT =
      new M5SingleModownFieldDef<>( FID_LINK_CONSTRAINT, CollConstraintM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_LINK_CONSTRAINTS, STR_LINK_CONSTRAINTS_D );
          setDefaultValue( CollConstraint.NONE );
        }

        protected CollConstraint doGetFieldValue( IDtoLinkInfo aEntity ) {
          return aEntity.linkConstraint();
        }

      };

  /**
   * LM for this model.
   *
   * @author hazard157
   */
  class LifecycleManager
      extends LmBase {

    public LifecycleManager( IM5Model<IDtoLinkInfo> aModel ) {
      super( aModel );
    }

    // ------------------------------------------------------------------------------------
    // implementation
    //

    private IDtoLinkInfo makeLinkInfo( IM5Bunch<IDtoLinkInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      IList<ISkClassInfo> classInfoes = CLASS_IDS.getFieldValue( aValues );
      IStringListEdit classIds = new StringArrayList();
      for( ISkClassInfo ci : classInfoes ) {
        classIds.add( ci.id() );
      }
      CollConstraint cc = LINK_CONSTRAINT.getFieldValue( aValues );
      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      DtoLinkInfo inf = DtoLinkInfo.create1( id, classIds, cc, params );
      return inf;
    }

    // ------------------------------------------------------------------------------------
    // LmBase
    //

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<IDtoLinkInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeCreate( aValues );
    }

    @Override
    protected IDtoLinkInfo doCreate( IM5Bunch<IDtoLinkInfo> aValues ) {
      return makeLinkInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeEdit( IM5Bunch<IDtoLinkInfo> aValues ) {
      // no additional checks are needed
      return super.doBeforeEdit( aValues );
    }

    @Override
    protected IDtoLinkInfo doEdit( IM5Bunch<IDtoLinkInfo> aValues ) {
      return makeLinkInfo( aValues );
    }

    @Override
    protected ValidationResult doBeforeRemove( IDtoLinkInfo aEntity ) {
      // no additional checks are needed
      return super.doBeforeRemove( aEntity );
    }

    @Override
    protected void doRemove( IDtoLinkInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public Sded2DtoLinkInfoM5Model( ISkConnection aConn ) {
    super( MODEL_ID, IDtoLinkInfo.class, ESkClassPropKind.LINK, aConn );
    setNameAndDescription( LINK.nmName(), LINK.description() );
    addFieldDefs( LINK_CONSTRAINT, CLASS_IDS );
  }

  @Override
  protected IM5LifecycleManager<IDtoLinkInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoLinkInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
