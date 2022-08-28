package org.toxsoft.uskat.base.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Base class for subject area Sk-classes modelling.
 * <p>
 * This is M5-model of classes claimed by {@link ISkSysdescr}. For other service classes use {@link KM5ModelBasic}
 * instead.
 * <p>
 * This model creates M5-model with all M5-modellable properties (attributes, rivets, CLOBs and links) defined. Default
 * lifycle manager and GUI panels are also provided.
 * <p>
 * This class may be used to dierctly create models or to be subclassed.
 *
 * @see ISkSysdescr#determineClassClaimingServiceId(String)
 * @see KM5ModelGeneric
 * @author hazard157
 * @param <T> - modelled entity type
 */
public class KM5ModelGeneric<T extends ISkObject>
    extends KM5ModelBasic<T> {

  /**
   * Constructor.
   *
   * @param aClassId String - ID of Sk-class to be modelled
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such class in {@link ISkSysdescr}
   */
  public KM5ModelGeneric( String aClassId, ISkConnection aConn ) {
    this( getClassInfo( aClassId, aConn ), aConn );
  }

  /**
   * Constructor.
   *
   * @param aClassInfo {@link ISkClassInfo} - description of the Sk-class to be modelled
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  public KM5ModelGeneric( ISkClassInfo aClassInfo, ISkConnection aConn ) {
    super( TsNullArgumentRtException.checkNull( aClassInfo.id() ), (Class)ISkObject.class, aConn );
    setNameAndDescription( aClassInfo.nmName(), aClassInfo.description() );
    IStridablesListEdit<IM5FieldDef<? extends ISkObject, ?>> fdefs = new StridablesList<>();
    fdefs.addAll( SKID, CLASS_ID, STRID, NAME, DESCRIPTION );
    // attributes
    for( IDtoAttrInfo ainf : aClassInfo.attrs().list() ) {
      if( !fdefs.hasKey( ainf.id() ) && !ISkHardConstants.isSkSysAttr( ainf ) ) {
        fdefs.add( new KM5AttributeFieldDef<>( ainf ) );
      }
    }
    // FIXME rivets
    // FIXME clobs
    // links
    for( IDtoLinkInfo linf : aClassInfo.links().list() ) {
      IM5FieldDef<? extends ISkObject, ?> fd;
      if( linf.linkConstraint().maxCount() == 1 ) {
        fd = new KM5SingleLinkFieldDef( linf );
      }
      else {
        fd = new KM5MultiLinkFieldDef( linf );
      }
      fdefs.add( fd );
    }
    addFieldDefs( (IStridablesList)fdefs );
    // set deafule GUI panels creator
    setPanelCreator( new KM5GenericPanelCreator<T>() );
  }

  @Override
  protected IM5LifecycleManager<T> doCreateDefaultLifecycleManager() {
    ISkConnection conn = tsContext().get( ISkConnection.class );
    return new KM5LifecycleManagerGeneric<>( this, conn );
  }

  @Override
  protected IM5LifecycleManager<T> doCreateLifecycleManager( Object aMaster ) {
    return new KM5LifecycleManagerGeneric<>( this, aMaster );
  }

  private static ISkClassInfo getClassInfo( String aClassId, ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNulls( aClassId, aConnection );
    ISkSysdescr cim = aConnection.coreApi().sysdescr();
    return cim.getClassInfo( aClassId );
  }

}
