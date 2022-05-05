package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link ISkSysdescr} implementation.
 *
 * @author hazard157
 */
class SkSysdescr
    extends AbstractSkCoreService
    implements ISkSysdescr {

  class ValidationSupport
      extends AbstractTsValidationSupport<ISkSysdescrValidator>
      implements ISkSysdescrValidator {

    @Override
    public ISkSysdescrValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canCreateClass( IDtoClassInfo aClassInfo ) {
      TsNullArgumentRtException.checkNull( aClassInfo );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkSysdescrValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateClass( aClassInfo ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canEditClass( IDtoClassInfo aClassInfo, ISkClassInfo aOldInfo ) {
      TsNullArgumentRtException.checkNull( aClassInfo );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkSysdescrValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canEditClass( aClassInfo, aOldInfo ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveClass( String aClassId ) {
      TsNullArgumentRtException.checkNull( aClassId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkSysdescrValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveClass( aClassId ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

  }

  final ValidationSupport                validationSupport    = new ValidationSupport();
  final IStridablesListEdit<SkClassInfo> internalClassesCache = new StridablesList<>();

  /**
   * Marks {@link #internalClassesCache} as invalid.
   * <p>
   * When flag is set to <code>true</code> call to {@link #listClasses()} will cause classes list to be reload from
   * backend and initalized.
   */
  private boolean cacheIsInvalid = true;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkSysdescr( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void internalRefreshCacheIfNeeded() {
    if( cacheIsInvalid ) {
      // подготовим список классов, которм заменим существующий classesList
      IStridablesList<IDtoClassInfo> cinfsList = ba().baClasses().readClassInfos();
      cinfsList = coreApi().l10n().l10nClassInfos( cinfsList );
      IStridablesList<SkClassInfo> ll = loadClassInfos( cinfsList );
      // заменим имеющейся список классов только что загруженным
      internalClassesCache.setAll( ll );
      cacheIsInvalid = false;
    }
  }

  /**
   * Initializes list of {@link ISkClassInfo} from the list of {@link IDtoClassInfo}.
   * <p>
   * Returned list is ordered: any class is <b>after</b> it's parent class in the list, that is index of the subclass in
   * list is always greater than index of the superclass. Resulting list contains at leas one item - the root class as
   * the first element (at the index 0).
   *
   * @param aDtoClassInfos {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - class DTOs
   * @return {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - list of created class infoes
   */
  public static IStridablesList<SkClassInfo> loadClassInfos( IStridablesList<IDtoClassInfo> aDtoClassInfos ) {
    // all classes except root one
    IStridablesListEdit<IDtoClassInfo> dtoList = new StridablesList<>( aDtoClassInfos );
    dtoList.removeById( IGwHardConstants.GW_ROOT_CLASS_ID );
    //
    IStridablesListEdit<SkClassInfo> ll = new StridablesList<>();
    ll.add( SkClassInfo.createRootClassInfo() ); // initially thers is only root class in the ll list
    // recursively add direct childs of classes already in the ll list
    int addCount = 0;
    do {
      addCount = 0;
      // remove from source already added classes
      for( String alreadyAddedId : ll.keys() ) {
        dtoList.removeByKey( alreadyAddedId );
      }
      // add direct childs of the added classes
      for( IDtoClassInfo dpu : dtoList ) {
        if( ll.keys().hasElem( dpu.parentId() ) ) {
          SkClassInfo skinf = fromDto( dpu, ll.getByKey( dpu.parentId() ) );
          ll.add( skinf );
          ++addCount;
        }
      }
    } while( addCount > 0 ); // while nothing was added
    return ll;
  }

  private static SkClassInfo fromDto( IDtoClassInfo aClassDto, ISkClassInfo aSuperClass ) {
    if( aSuperClass == null ) {
      TsInternalErrorRtException.checkFalse( aClassDto.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) );
    }
    SkClassInfo cinf = new SkClassInfo( aClassDto.id(), aSuperClass, aClassDto.params() );
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      cinf.props( k ).papiSetSelf( aClassDto.propInfos( k ) );
    }
    return cinf;
  }

  private void internalClearHierarchyCache() {
    for( SkClassInfo cinf : internalClassesCache ) {
      cinf.papiClearHierarchyCache();
    }
  }

  private void internalWriteSkClassToBackend( ISkClassInfo aSkClass ) {
    IDtoClassInfo dtoClass = DtoClassInfo.createFromSk( aSkClass, true );
    ba().baClasses().writeClassInfos( IStringList.EMPTY, new StridablesList<>( dtoClass ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // TODO SkSysdescr.doInit()
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkSysdescr
  //

  @Override
  public ISkClassInfo findClassInfo( String aClassId ) {
    return internalClassesCache.findByKey( aClassId );
  }

  @Override
  public ISkClassInfo getClassInfo( String aClassId ) {
    return internalClassesCache.getByKey( aClassId );
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesList<ISkClassInfo> listClasses() {
    coreApi().papiCheckIsOpen();
    internalRefreshCacheIfNeeded();
    return (IStridablesList)internalClassesCache;
  }

  @Override
  public ISkClassInfo defineClass( IDtoClassInfo aDtoClassInfo ) {
    TsNullArgumentRtException.checkNull( aDtoClassInfo );
    coreApi().papiCheckIsOpen();
    // validate creation
    ISkClassInfo cInfo = listClasses().findByKey( aDtoClassInfo.id() );
    SkCoreEvent event;
    if( cInfo != null ) {
      TsValidationFailedRtException.checkError( validationSupport.canEditClass( aDtoClassInfo, cInfo ) );
      event = new SkCoreEvent( ECrudOp.EDIT, Gwid.createClass( aDtoClassInfo.id() ) );
    }
    else {
      TsValidationFailedRtException.checkError( validationSupport.canCreateClass( aDtoClassInfo ) );
      event = new SkCoreEvent( ECrudOp.CREATE, Gwid.createClass( aDtoClassInfo.id() ) );
    }
    // create class, put to the cache, write to backend and update other classes in cache
    SkClassInfo cinf = fromDto( aDtoClassInfo, listClasses().getByKey( aDtoClassInfo.parentId() ) );
    internalClassesCache.put( cinf );
    internalClearHierarchyCache();
    internalWriteSkClassToBackend( cinf );
    coreApi().fireCoreEvent( event );
    return cinf;
  }

  @Override
  public void removeClass( String aClassId ) {
    // TODO реализовать SkSysdescr.removeClass()
    throw new TsUnderDevelopmentRtException( "SkSysdescr.removeClass()" );
  }

  @Override
  public ITsValidationSupport<ISkSysdescrValidator> svs() {
    // TODO Auto-generated method stub
    return null;
  }

}
