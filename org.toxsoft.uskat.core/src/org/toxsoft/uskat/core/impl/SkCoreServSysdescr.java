package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.backend.api.IBaClassesMessages.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.diff.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
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
public class SkCoreServSysdescr
    extends AbstractSkCoreService
    implements ISkSysdescr {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServSysdescr::new;

  /**
   * {@link ISkSysdescr#hierarchy()} implementation.
   *
   * @author hazard157
   */
  class HierarchyExplorer
      implements ISkClassHierarchyExplorer {

    @Override
    public boolean isSuperclassOf( String aClassId, String aSubclassId ) {
      checkThread();
      TsNullArgumentRtException.checkNulls( aClassId, aSubclassId );
      ISkClassInfo cinf = findClassInfo( aClassId );
      if( cinf != null ) {
        return cinf.isSuperclassOf( aSubclassId );
      }
      return false;
    }

    @Override
    public boolean isAssignableFrom( String aClassId, String aSubclassId ) {
      checkThread();
      TsNullArgumentRtException.checkNulls( aClassId, aSubclassId );
      ISkClassInfo cinf = findClassInfo( aClassId );
      if( cinf != null ) {
        return cinf.isAssignableFrom( aSubclassId );
      }
      return false;
    }

    @Override
    public boolean isSubclassOf( String aClassId, String aSuperclassId ) {
      checkThread();
      TsNullArgumentRtException.checkNulls( aClassId, aSuperclassId );
      ISkClassInfo cinf = findClassInfo( aClassId );
      if( cinf != null ) {
        return cinf.isSubclassOf( aSuperclassId );
      }
      return false;
    }

    @Override
    public boolean isAssignableTo( String aClassId, String aSuperclassId ) {
      checkThread();
      TsNullArgumentRtException.checkNulls( aClassId, aSuperclassId );
      ISkClassInfo cinf = findClassInfo( aClassId );
      if( cinf != null ) {
        return cinf.isAssignableTo( aSuperclassId );
      }
      return false;
    }

    @Override
    public boolean isOfClass( String aClassId, IStringList aClassIdsList ) {
      checkThread();
      TsNullArgumentRtException.checkNulls( aClassId, aClassIdsList );
      ISkClassInfo cinf = findClassInfo( aClassId );
      if( cinf != null ) {
        return cinf.isOfClass( aClassIdsList );
      }
      return false;
    }

    @Override
    public String findCommonRootClassId( IStringList aClassIds ) {
      checkThread();
      TsNullArgumentRtException.checkNull( aClassIds );
      // search for first existing class ID
      ISkClassInfo h = null;
      for( String cid : aClassIds ) {
        h = findClassInfo( cid );
        if( h != null ) {
          break;
        }
      }
      if( h == null ) { // no exsiting class ID in argument
        return IGwHardConstants.GW_ROOT_CLASS_ID;
      }
      // iterate over superclasses of found class and determine which is common root
      IStringList ancestorIdLists = h.listSuperclasses( true ).ids();
      // from found class up to (but not incluring) root class
      for( int i = ancestorIdLists.size() - 1; i >= 1; i-- ) {
        String ansId = ancestorIdLists.get( i );
        if( isCommonSuperclass( ansId, aClassIds ) ) {
          return ansId;
        }
      }
      return IGwHardConstants.GW_ROOT_CLASS_ID;
    }

    private boolean isCommonSuperclass( String aAncestorId, IStringList aClassIds ) {
      for( String cid : aClassIds ) {
        if( listClasses().hasKey( aAncestorId ) ) {
          ISkClassInfo h = getClassInfo( cid );
          if( !h.listSuperclasses( true ).ids().hasElem( aAncestorId ) ) {
            return false;
          }
        }
      }
      return true;
    }

  }

  /**
   * {@link ISkSysdescr#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkSysdescrListener> {

    private boolean isPending = false;

    @Override
    protected void doClearPendingEvents() {
      isPending = false;
    }

    @Override
    protected void doFirePendingEvents() {
      isPending = false;
      fireClassInfosChanged( ECrudOp.LIST, null );
    }

    @Override
    protected boolean doIsPendingEvents() {
      return isPending;
    }

    void fireClassInfosChanged( ECrudOp aOp, String aClassId ) {
      if( isFiringPaused() ) {
        isPending = true;
        return;
      }
      for( ISkSysdescrListener l : listeners() ) {
        try {
          l.onClassInfosChanged( coreApi(), aOp, aClassId );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

  }

  /**
   * {@link ISkSysdescr#svs()} implementation.
   *
   * @author hazard157
   */
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

  /**
   * Builtin classes editing validator is always on.
   */
  private final ISkSysdescrValidator builtinValidator = new ISkSysdescrValidator() {

    private ValidationResult checkInfo( IDtoClassInfo aDtoClassInfo ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      // warn if class ID is IStridable.NONE_ID
      String id = aDtoClassInfo.id();
      if( id.equals( IStridable.NONE_ID ) ) {
        vr = ValidationResult.warn( FMT_WARN_UNWANTED_CLASS_ID, id );
      }
      // warn if class name is not set or set to default string
      String name = aDtoClassInfo.params().getStr( TSID_NAME, TsLibUtils.EMPTY_STRING );
      if( name.isEmpty() || name.equals( DEFAULT_NAME ) ) {
        vr = ValidationResult.firstNonOk( vr, ValidationResult.warn( FMT_WARN_EMPTY_CLASS_NAME, aDtoClassInfo.id() ) );
      }
      return vr;
    }

    private ValidationResult checkUniquePropIds( IDtoClassInfo aClassInfo, ISkClassInfo aParent ) {
      // collect parent prop IDs
      IStringListEdit propIds = new StringArrayList();
      for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
        ISkClassProps<?> p = aParent.props( k );
        propIds.addAll( p.list().ids() );
      }
      // error if diff kind of props has the same ID
      for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
        for( IDtoClassPropInfoBase p : aClassInfo.propInfos( k ) ) {
          if( propIds.hasElem( p.id() ) ) {
            return ValidationResult.error( FMT_ERR_CLASS_HAS_PROP_ID, p.id() );
          }
          propIds.add( p.id() );
        }
      }
      return ValidationResult.SUCCESS;
    }

    private ValidationResult checkDupProp( ESkClassPropKind aKind, IDtoClassInfo aClassInfo, ISkClassInfo aParent ) {
      IStridablesList<?> newProps = aClassInfo.propInfos( aKind );
      ISkClassProps<?> parentProps = aParent.props( aKind );
      for( IStridable p : newProps ) {
        ISkClassInfo superDeclarer = parentProps.findSuperDeclarer( p.id() );
        if( superDeclarer != null ) {
          return ValidationResult.error( FMT_ERR_DUP_PROP_IN_SUPER, aKind.nmName(), p.id(), superDeclarer.id() );
        }
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canCreateClass( IDtoClassInfo aClassInfo ) {
      // check if class ID already exists
      if( listClasses().hasKey( aClassInfo.id() ) ) {
        return ValidationResult.error( FMT_ERR_CLASS_ALREADY_EXISTS, aClassInfo.id() );
      }
      // check that parent class exists
      ISkClassInfo parentInfo = listClasses().findByKey( aClassInfo.parentId() );
      if( parentInfo == null ) {
        return ValidationResult.error( FMT_ERR_NO_PARENT_CLASS, aClassInfo.parentId() );
      }
      // check that any property ID is not declared in the ancestor hierarchy
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
        vr = ValidationResult.firstNonOk( vr, checkDupProp( k, aClassInfo, parentInfo ) );
        if( vr.isError() ) {
          break;
        }
      }
      // check no duplicate prop IDs at all
      vr = ValidationResult.firstNonOk( vr, checkUniquePropIds( aClassInfo, parentInfo ) );
      if( vr.isError() ) {
        return vr;
      }
      return ValidationResult.firstNonOk( vr, checkInfo( aClassInfo ) );
    }

    @Override
    public ValidationResult canEditClass( IDtoClassInfo aClassInfo, ISkClassInfo aOldInfo ) {
      // can't edit root class
      if( aOldInfo.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
        return ValidationResult.error( MSG_ERR_CANT_CHANGE_ROOT_CLASS );
      }
      // can't change class ID
      if( !aClassInfo.id().equals( aOldInfo.id() ) ) {
        return ValidationResult.error( FMT_ERR_CANT_CHANGE_CLASS_ID, aOldInfo.id() );
      }
      // can't change parent class
      if( !aClassInfo.parentId().equals( aOldInfo.parentId() ) ) {
        return ValidationResult.error( FMT_ERR_CANT_CHANGE_PARENT, aOldInfo.id() );
      }
      // check properties: changed props must be compatible with old, new - must not duplicate in hierarchy
      for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
        IStridablesList<IDtoClassPropInfoBase> oldProps = aOldInfo.props( k ).list();
        IStridablesList<IDtoClassPropInfoBase> newProps = aClassInfo.propInfos( k );
        IMap<EDiffNature, IStridablesListEdit<IDtoClassPropInfoBase>> diff =
            DiffUtils.compareStridablesLists( newProps, oldProps );
        // check new properties
        for( IDtoClassPropInfoBase p : diff.getByKey( EDiffNature.LEFT ) ) {
          // check new properties are not declared in ancestors (super classes)
          ISkClassInfo superDeclarer = aOldInfo.props( k ).findSuperDeclarer( p.id() );
          if( superDeclarer != null && !superDeclarer.id().equals( aClassInfo.id() ) ) {
            return ValidationResult.error( FMT_ERR_DUP_PROP_IN_SUPER, k.nmName(), p.id(), superDeclarer.id() );
          }
          // check new properties are not declared in descendants (sub classes)
          IStridablesList<ISkClassInfo> subDeclarers = aOldInfo.props( k ).findSubDeclarers( p.id() );
          if( !subDeclarers.isEmpty() ) {
            String subClassIds = String.join( ", ", subDeclarers.ids() ); //$NON-NLS-1$
            return ValidationResult.error( FMT_ERR_DUP_PROP_IN_SUB, k.nmName(), p.id(), subClassIds );
          }
        }
        // FIXME check changed properties
        // FIXME GOGA 2023-02-10 error!
        // ValidationResult vr = ValidationResult.SUCCESS;
        // for( IDtoClassPropInfoBase p : diff.getByKey( EDiffNature.DIFF ) ) {
        // // check that changed property is compatible with existing one so that exsiting values in DB will remain
        // }
        // // check no duplicate prop IDs at all
        // vr = ValidationResult.firstNonOk( vr, checkUniquePropIds( aClassInfo, aOldInfo.parent() ) );
        // if( vr.isError() ) {
        // return vr;
        // }
      }
      return checkInfo( aClassInfo );
    }

    @Override
    public ValidationResult canRemoveClass( String aClassId ) {
      if( aClassId.equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
        return ValidationResult.error( MSG_ERR_CANT_REMOVE_ROOT_CLASS );
      }
      ISkClassInfo cinf = listClasses().findByKey( aClassId );
      if( cinf == null ) {
        return ValidationResult.error( FMT_ERR_CANT_REMOVE_ABSENT_CLASS, aClassId );
      }
      if( !cinf.listSubclasses( true, false ).isEmpty() ) {
        return ValidationResult.error( FMT_ERR_CANT_REMOVE_CHILDED_CLASS, aClassId );
      }
      return ValidationResult.SUCCESS;
    }

  };

  final Eventer                           eventer           = new Eventer();
  final ISkClassHierarchyExplorer         hierarchyExplorer = new HierarchyExplorer();
  final ValidationSupport                 validationSupport = new ValidationSupport();
  final IStridablesListEdit<ISkClassInfo> cachedClassesList = new StridablesList<>();

  /**
   * Marks {@link #cachedClassesList} as invalid.
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
  SkCoreServSysdescr( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void internalRefreshCacheIfNeeded() {
    if( cacheIsInvalid ) {
      // load DTOs from backend
      IStridablesList<IDtoClassInfo> loadedDtos = new StridablesList<>( ba().baClasses().readClassInfos() );
      // validate DTOs
      ValResList vrl = new ValResList();
      IStridablesList<IDtoClassInfo> validDtos = SkCoreUtils.makeHierarchyTreeOfClassDtos( loadedDtos, vrl );
      for( ValidationResult vr : vrl.results() ) {
        vr.logTo( logger() );
      }
      // localize DTOs
      IStridablesList<IDtoClassInfo> localizedDtos = coreApi().l10n().l10nClassInfos( validDtos );
      // make SkClassInfos
      IStridablesList<ISkClassInfo> skClasses = SkCoreUtils.makeHierarchyTreeOfSkClasses( localizedDtos );
      cachedClassesList.setAll( skClasses );
      cacheIsInvalid = false;
    }
  }

  IStridablesList<IDtoClassInfo> loadValidListOfClassDtos() {
    // read from backend
    IStridablesListEdit<IDtoClassInfo> dtoList = new StridablesList<>( ba().baClasses().readClassInfos() );
    // make list without orphans
    IStridablesListEdit<IDtoClassInfo> llResult = new StridablesList<>();
    IDtoClassInfo rootClassDto = SkCoreUtils.createRootClassDto();
    llResult.add( rootClassDto ); // add root class in the llResult
    dtoList.removeByKey( rootClassDto.id() );
    // now we have only root class, let us move all descendant tree from dtoList to llResult
    int addCount = 0;
    do {
      addCount = 0;
      // add direct childs of the added classes
      for( IDtoClassInfo dto : dtoList ) {
        if( llResult.keys().hasElem( dto.parentId() ) ) {
          dtoList.removeByKey( dto.id() ); // remove from source even it will not be added to llResult
          ++addCount;
          // check that DTO to be added is valid
          if( isValidToUseClass( rootClassDto, llResult ) ) {
            llResult.add( dto );
          }
        }
      }
    } while( addCount > 0 ); // while nothing was added
    // warn if there were orphan classes in the loaded list
    for( String orphanId : dtoList.keys() ) {
      logger().warning( FMT_WARN_ORPHAN_CLASS, orphanId );
    }
    return llResult;
  }

  /**
   * Checks if class to be used does not violates Sysdescr integrity.
   * <p>
   * This method guards against corrupted data read from the backend.
   * <p>
   * In any case when method returns <code>false</code> it also logs appropriate error message.
   *
   * @param aClassDto {@link IDtoClassInfo} - info about class to be used in Sysdescr
   * @param aAncestors {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - list with all ancestors
   * @return boolean - <code>true</code> class is vali9d, <code>false</code> - class must be ignored
   */
  private boolean isValidToUseClass( IDtoClassInfo aClassDto, IStridablesList<IDtoClassInfo> aAncestors ) {
    // check that properties in the class to use does not duplicates ancestors properties
    IDtoClassInfo parent = aAncestors.findByKey( aClassDto.parentId() );
    while( parent != null ) {
      for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
        IStridablesList<?> thisProps = aClassDto.propInfos( k );
        IStridablesList<?> parentProps = parent.propInfos( k );
        for( String propId : thisProps.ids() ) {
          if( parentProps.ids().hasElem( propId ) ) {
            logger().error( FMT_ERR_INV_CLASS_LOAD_IGNORED, aClassDto.id(), k.id(), propId, parent.id() );
            return false;
          }
        }
      }
      parent = aAncestors.findByKey( parent.parentId() );
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    internalRefreshCacheIfNeeded();
  }

  @Override
  protected void doClose() {
    eventer.clearListenersList();
    eventer.resetPendingEvents();
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    return switch( aMessage.messageId() ) {
      case MSGID_SYSDESCR_CHANGE -> {
        ECrudOp op = extractCrudOp( aMessage );
        String classId = extractClassId( aMessage );
        eventer.fireClassInfosChanged( op, classId );
        yield true;
      }
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // ISkSysdescr
  //

  @Override
  public ISkClassInfo findClassInfo( String aClassId ) {
    return listClasses().findByKey( aClassId );
  }

  @Override
  public ISkClassInfo getClassInfo( String aClassId ) {
    return listClasses().getByKey( aClassId );
  }

  @Override
  public IStridablesList<ISkClassInfo> listClasses() {
    checkThread();
    coreApi().papiCheckIsOpen();
    internalRefreshCacheIfNeeded();
    return cachedClassesList;
  }

  @Override
  public ISkClassInfo defineClass( IDtoClassInfo aDtoClassInfo ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aDtoClassInfo );
    coreApi().papiCheckIsOpen();
    // validate operation
    ISkClassInfo cInfo = listClasses().findByKey( aDtoClassInfo.id() );
    // validate the editing of existing class
    if( cInfo != null ) {
      // do not edit existing class if is the same
      IDtoClassInfo existngClassDto = DtoClassInfo.createFromSk( cInfo, true );
      if( existngClassDto.equals( aDtoClassInfo ) ) {
        return cInfo;
      }
      TsValidationFailedRtException.checkError( validationSupport.canEditClass( aDtoClassInfo, cInfo ) );
    }
    // validate the creation of the new class
    else {
      TsValidationFailedRtException.checkError( validationSupport.canCreateClass( aDtoClassInfo ) );
    }
    // write to backend
    cacheIsInvalid = true;
    ba().baClasses().writeClassInfos( IStringList.EMPTY, new StridablesList<>( aDtoClassInfo ) );
    ISkClassInfo result = listClasses().getByKey( aDtoClassInfo.id() );
    return result;
  }

  @Override
  public void removeClass( String aClassId ) {
    checkThread();
    coreApi().papiCheckIsOpen();
    TsValidationFailedRtException.checkError( validationSupport.canRemoveClass( aClassId ) );
    // remove from backend
    cacheIsInvalid = true;
    ba().baClasses().writeClassInfos( new SingleStringList( aClassId ), IStridablesList.EMPTY );
  }

  @Override
  public ISkClassHierarchyExplorer hierarchy() {
    return hierarchyExplorer;
  }

  @Override
  public String determineClassClaimingServiceId( String aClassId ) {
    checkThread();
    return coreApi().determineClassClaimingServiceId( aClassId );
  }

  @Override
  public ITsValidationSupport<ISkSysdescrValidator> svs() {
    return validationSupport;
  }

  @Override
  public ITsEventer<ISkSysdescrListener> eventer() {
    return eventer;
  }

}
