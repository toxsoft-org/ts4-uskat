package org.toxsoft.uskat.ggprefs.lib.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.ggprefs.lib.impl.IServiceInternalConstants.*;
import static org.toxsoft.uskat.ggprefs.lib.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemAlreadyExistsRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.core.impl.dto.DtoClassInfo;
import org.toxsoft.uskat.core.impl.dto.DtoObject;
import org.toxsoft.uskat.ggprefs.lib.*;

/**
 * Реализация {@link ISkGuiGwPrefsService}.
 *
 * @author goga
 */
public class SkGuiGwPrefsService
    extends AbstractSkService
    implements ISkGuiGwPrefsService {

  /**
   * Creator singleton.
   */
  public static final ISkServiceCreator<SkGuiGwPrefsService> CREATOR = SkGuiGwPrefsService::new;

  private final IMapEdit<Skid, GuiGwPrefsSection> loadedSectionsMap = new ElemMap<>();

  // final SectionEventer eventer;

  /**
   * The constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - core API for service developers
   */
  public SkGuiGwPrefsService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );

    // eventer = new SectionEventer( this, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // Implementation
  //

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    ISkSysdescr cim = coreApi().sysdescr();
    DtoClassInfo sectionClassInfo = new DtoClassInfo( CLSID_SECTION, GW_ROOT_CLASS_ID, IOptionSet.NULL );
    sectionClassInfo.attrInfos().add( AINF_SECTTION_DEF_PARAMS );
    cim.defineClass( sectionClassInfo );
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkGuiGwPrefsService
  //

  @Override
  public IStridablesList<IDpuGuiGwPrefsSectionDef> listSections() {
    ISkObjectService os = coreApi().objService();
    IStridablesListEdit<IDpuGuiGwPrefsSectionDef> ll = new StridablesList<>();
    for( ISkObject o : os.listObjs( CLSID_SECTION, true ) ) {
      IOptionSet defParams = o.attrs().getValobj( AINF_SECTTION_DEF_PARAMS.id() );
      IDpuGuiGwPrefsSectionDef def = new DpuGuiGwPrefsSectionDef( o.id(), o.nmName(), o.classId(), defParams );
      ll.add( def );
    }
    return ll;
  }

  @Override
  public IGuiGwPrefsSection defineSection( IDpuGuiGwPrefsSectionDef aSectionDef ) {
    TsNullArgumentRtException.checkNull( aSectionDef );
    ISkObjectService os = coreApi().objService();
    Skid sectionSkid = new Skid( CLSID_SECTION, aSectionDef.id() );
    ISkObject oldSectObj = os.find( sectionSkid );
    if( oldSectObj != null ) {
      throw new TsItemAlreadyExistsRtException( FMT_ERR_DUP_SECTION_ID, aSectionDef.id() );
    }
    DtoObject dpuObj = new DtoObject( sectionSkid, IOptionSet.NULL, IStringMap.EMPTY );
    dpuObj.attrs().setStr( TSID_NAME, aSectionDef.nmName() );
    dpuObj.attrs().setStr( TSID_DESCRIPTION, aSectionDef.description() );
    dpuObj.attrs().setValobj( AINF_SECTTION_DEF_PARAMS.id(), aSectionDef.params() );
    ISkObject sectSkObj = os.defineObject( dpuObj );

    GuiGwPrefsSection sectObj = new GuiGwPrefsSection( coreApi(), sectSkObj );

    // добавим в кеш созданный раздел
    loadedSectionsMap.put( sectionSkid, sectObj );

    // fire event
    // ECrudOperation op = ECrudOperation.ADDED;
    // eventer.fireObjectEvent( sectionSkid );
    return sectObj;

    // TODO Auto-generated method stub
    // TODO реализовать SkGuiGwPrefsService.defineSection()
    // throw new TsUnderDevelopmentRtException( "SkGuiGwPrefsService.defineSection()" );
  }

  @Override
  public IGuiGwPrefsSection getSection( String aSectionId ) {
    Skid sectSkid = new Skid( CLSID_SECTION, aSectionId );
    if( loadedSectionsMap.hasKey( sectSkid ) ) {
      return loadedSectionsMap.getByKey( sectSkid );
    }
    ISkObjectService os = coreApi().objService();
    ISkObject sectSkObj = os.get( sectSkid );

    GuiGwPrefsSection sectObj = new GuiGwPrefsSection( coreApi(), sectSkObj );

    // добавим в кеш созданный раздел
    loadedSectionsMap.put( sectSkid, sectObj );

    return sectObj;

    // TODO Auto-generated method stub
    // TODO реализовать SkGuiGwPrefsService.getSection()
    // throw new TsUnderDevelopmentRtException( "SkGuiGwPrefsService.getSection()" );
  }

  @Override
  public void removeSection( String aSectionId ) {
    Skid sectSkid = new Skid( CLSID_SECTION, aSectionId );
    // очистим кеш и открытй (если есть) раздел
    GuiGwPrefsSection ggpSection = loadedSectionsMap.removeByKey( sectSkid );
    ISkObjectService os = coreApi().objService();
    if( ggpSection == null ) {
      ISkObject sectSkObj = os.find( sectSkid );
      if( sectSkObj == null ) {
        return;
      }

      ggpSection = new GuiGwPrefsSection( coreApi(), sectSkObj );
    }

    ggpSection.close();

    // TODO удалить объект раздела

    // pauseExternalValidation();
    try {

      os.removeObject( sectSkid );

    }
    finally {
      // resumeExternalValidation();
    }
    // fire event
    // eventer.fireRefbookChanged( ECrudOperation.REMOVED, aRefbookId );
  }

  // private void ensureSectionClass() {
  // ISkClassInfoManager cim = coreApi().sysdescr().classInfoManager();
  // ISkObjectService os = coreApi().objService();
  // cim.defineClass( CLSINF_SECTION );
  // }

}
