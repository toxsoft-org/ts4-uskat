package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Point of entry to the USkat and some methods used by CoreAPI implementation also useful for users.
 *
 * @author hazard157
 */
public class SkCoreUtils {

  /**
   * Initializes static stuff, must be called once before any USkat usage.
   */
  public static void initialize() {
    TsValobjUtils.registerKeeper( SkEvent.KEEPER_ID, SkEvent.KEEPER );
    TsValobjUtils.registerKeeper( EQueryState.KEEPER_ID, EQueryState.KEEPER );
    TsValobjUtils.registerKeeper( SkCommandState.KEEPER_ID, SkCommandState.KEEPER );
  }

  /**
   * Creates the instance of the single threaded {@link ISkConnection}.
   *
   * @return {@link ISkConnection} - instance of the connection in {@link ESkConnState#CLOSED CLOSED} state
   */
  public static ISkConnection createConnection() {
    return new SkConnection();
  }

  // ------------------------------------------------------------------------------------
  // Classes & hierarchy
  //

  /**
   * Creates root class (with ID {@link IGwHardConstants#GW_ROOT_CLASS_ID}) description.
   * <p>
   * This is the main and only way to create correct root class description.
   *
   * @return {@link DtoClassInfo} - root class
   */
  public static DtoClassInfo createRootClassDto() {
    DtoClassInfo dpuRoot = new DtoClassInfo( OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE //
    ) );
    // root class name
    dpuRoot.params().setStr( TSID_NAME, STR_N_ROOT_CLASS );
    dpuRoot.params().setStr( TSID_DESCRIPTION, STR_D_ROOT_CLASS );
    // --- creating attributes
    // AID_SKID
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_SKID, DataType.create( VALOBJ, //
        TSID_NAME, STR_N_ATTR_SKID, //
        TSID_DESCRIPTION, STR_D_ATTR_SKID, //
        TSID_KEEPER_ID, Skid.KEEPER_ID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avValobj( Skid.NONE ) //
    ), OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SYS_ATTR, AV_TRUE //
    ) ) );
    // AID_CLASS_ID
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_CLASS_ID, DataType.create( STRING, //
        TSID_NAME, STR_N_ATTR_CLASS_ID, //
        TSID_DESCRIPTION, STR_D_ATTR_CLASS_ID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avStr( Skid.NONE.classId() ) //
    ), OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SYS_ATTR, AV_TRUE //
    ) ) );
    // AID_STRID
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_STRID, DataType.create( STRING, //
        TSID_NAME, STR_N_ATTR_STRID, //
        TSID_DESCRIPTION, STR_D_ATTR_STRID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avStr( Skid.NONE.strid() ) //
    ), OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SYS_ATTR, AV_TRUE //
    ) ) );
    // AID_NAME
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_NAME, DDEF_NAME, IOptionSet.NULL ) );
    // AID_DESCRIPTION
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_DESCRIPTION, DDEF_DESCRIPTION, IOptionSet.NULL ) );
    return dpuRoot;
  }

  /**
   * Checks if class to be used does not violates Sysdescr integrity.
   * <p>
   * Check is preformed if <code>aClassDto</code> duplicates any property from any superclass.
   *
   * @param aClassDto {@link IDtoClassInfo} - info about class to be used in Sysdescr
   * @param aAncestors {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - list wilh all ansestors
   * @return ValidationResult - check result
   */
  private static ValidationResult validateToUseClass( IDtoClassInfo aClassDto,
      IStridablesList<IDtoClassInfo> aAncestors ) {
    // check that properties in the class to use does not duplicates ancestors properties
    IDtoClassInfo parent = aAncestors.findByKey( aClassDto.parentId() );
    while( parent != null ) {
      for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
        IStridablesList<?> thisProps = aClassDto.propInfos( k );
        IStridablesList<?> parentProps = parent.propInfos( k );
        for( String propId : thisProps.ids() ) {
          if( parentProps.ids().hasElem( propId ) ) {
            return ValidationResult.error( FMT_ERR_INV_CLASS_LOAD_IGNORED, aClassDto.id(), k.id(), propId,
                parent.id() );
          }
        }
      }
      parent = aAncestors.findByKey( parent.parentId() );
    }
    return ValidationResult.SUCCESS;
  }

  /**
   * Extracts valid class hierarchy tree from the argument list.
   * <p>
   * <b>Hierarchy tree</b> means that resulting list contains the root class and it's subclasses tree. Root class DTO
   * will be created by {@link #createRootClassDto()}.
   * <p>
   * <b>Valid</b> means that class properties are valid, no duplicates exits in hierarchy tree.
   * <p>
   * Invalid (with duplicated properties) and orphan (outside of hierarchy tree) classes are ignored but meassages
   * logged to <code>aLog</code>.
   *
   * @param aClassDtos {@link IStridablesListEdit}&lt;{@link IDtoClassInfo}&gt; - source list
   * @param aLog {@link ValResList} - log to collect warning and error messages or <code>null</code>
   * @return {@link IStridablesListEdit}&lt;{@link IDtoClassInfo}&gt; - valid hierarchy tree classes list
   */
  public static IStridablesList<IDtoClassInfo> makeHierarchyTreeOfClassDtos(
      IStridablesListEdit<IDtoClassInfo> aClassDtos, ValResList aLog ) {
    TsNullArgumentRtException.checkNull( aClassDtos );
    // make list without orphans
    IStridablesListEdit<IDtoClassInfo> llResult = new StridablesList<>();
    IDtoClassInfo rootClassDto = SkCoreUtils.createRootClassDto();
    llResult.add( rootClassDto ); // add root class in the llResult
    aClassDtos.removeByKey( rootClassDto.id() );
    // now we have only root class, let us move all descendant tree from dtoList to llResult
    int addCount = 0;
    do {
      addCount = 0;
      // add direct childs of the added classes
      for( IDtoClassInfo dto : aClassDtos ) {
        if( llResult.keys().hasElem( dto.parentId() ) ) {
          aClassDtos.removeByKey( dto.id() ); // remove from source even it will not be added to llResult
          ++addCount;
          // check that DTO to be added is valid
          ValidationResult vr = validateToUseClass( rootClassDto, llResult );
          if( !vr.isError() ) {
            llResult.add( dto );
          }
          else {
            if( aLog != null ) {
              aLog.add( vr );
            }
          }
        }
      }
    } while( addCount > 0 ); // while nothing was added
    // warn if there were orphan classes in the loaded list
    if( aLog != null ) {
      for( String orphanId : aClassDtos.keys() ) {
        aLog.add( ValidationResult.warn( FMT_WARN_ORPHAN_CLASS, orphanId ) );
      }
    }
    return llResult;
  }

  /**
   * Builds class infos hierarchy tree from supplyed list of class DTOs.
   * <p>
   * Method implementation uses {@link #makeHierarchyTreeOfClassDtos(IStridablesListEdit, ValResList)} to prepare valid
   * list of DTOs. Note thst some classes from source list may be silently ommited from resulting list.
   *
   * @param aClassDtos {@link IStridablesListEdit}&lt;{@link IDtoClassInfo}&gt; - source list
   * @return {@link IStridablesListEdit}&lt;{@link ISkClassInfo}&gt; - valid hierarchy tree classes list
   */
  public static IStridablesList<ISkClassInfo> makeHierarchyTreeOfClassInfos(
      IStridablesListEdit<IDtoClassInfo> aClassDtos ) {
    IStridablesList<IDtoClassInfo> classDtos = makeHierarchyTreeOfClassDtos( aClassDtos, null );
    IStridablesListEdit<ISkClassInfo> ll = new StridablesList<>();
    // make list of SkClassInfo tree starting from the root class
    for( IDtoClassInfo cdto : classDtos ) {
      SkClassInfo cinf = new SkClassInfo( cdto, ll.findByKey( cdto.parentId() ), classDtos );
      ll.add( cinf );
    }
    // finish SkCLassInfo instance initialiation
    for( ISkClassInfo cinf : ll ) {
      ((SkClassInfo)cinf).papiInitClassHierarchy( ll );
    }
    return ll;
  }

  /**
   * Prohibit inheritance.
   */
  private SkCoreUtils() {
    // nop
  }
}
