package org.toxsoft.uskat.core.gui.km5;

import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.km5.ISkKm5SharedResources.*;

import java.util.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Lifecycle manager implementation base for subject area Sk-classes modeling.
 * <p>
 * This manager assumes that M5-model has the same ID as Sk-class ID {@link ISkClassInfo#id()}.
 * <p>
 * Вообще говоря, логика проверки возможности создания/редактирования и удаления объектов должна находится в службе
 * {@link ISkObjectService}, а не в этом классе.
 *
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class KM5LifecycleManagerGeneric<T extends ISkObject>
    extends KM5LifecycleManagerBasic<T> {

  /**
   * Constructor.
   * <p>
   * For {@link ISkSysdescr} claimed Sk-classes CRUD operations are always enabled.
   *
   * @param aModel {@link IM5Model}&lt;T&gt; - the model
   * @param aMaster {@link ISkConnection} - master object, may be <code>null</code>
   */
  public KM5LifecycleManagerGeneric( IM5Model<T> aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private DtoFullObject makeDtoFullObject( IM5Bunch<T> aValues ) {
    // создаем структуру объекта
    String classId = aValues.getAsAv( AID_CLASS_ID ).asString();
    String strid = aValues.getAsAv( AID_STRID ).asString();
    Skid skid = new Skid( classId, strid );
    DtoFullObject objDto = DtoFullObject.createDtoFullObject( skid, coreApi() );
    // заносим туда значения атрибутов
    ISkClassInfo cinf = skSysdescr().getClassInfo( classId );
    for( IDtoAttrInfo ainf : cinf.attrs().list() ) {
      if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
        IAtomicValue av = aValues.getAsAv( ainf.id() );
        objDto.attrs().setValue( ainf.id(), av );
      }
    }
    // TODO rivets?
    // TODO links?
    // TODO CLOBs?
    return objDto;
  }

  @SuppressWarnings( "unchecked" )
  private ISkObject internalDefineObject( IM5Bunch<T> aValues ) {
    DtoFullObject objDto = makeDtoFullObject( aValues );
    // создаем объект
    ISkObject sko = skObjServ().defineObject( objDto );
    // TODO rivets? CLOBs?
    // задаем связи
    ISkClassInfo cinf = skSysdescr().getClassInfo( objDto.classId() );
    for( IDtoLinkInfo linf : cinf.links().list() ) {
      ISkidList rightSkids = ISkidList.EMPTY;
      if( linf.linkConstraint().maxCount() == 1 ) {
        ISkObject robj = aValues.getAs( linf.id(), ISkObject.class );
        if( robj != null ) {
          rightSkids = new SkidList( robj.skid() );
        }
      }
      else {
        IList<ISkObject> robjs = (IList<ISkObject>)aValues.get( linf.id() );
        rightSkids = SkHelperUtils.objsToSkids( robjs );
      }
      skLinkServ().defineLink( sko.skid(), linf.id(), null, rightSkids );
    }
    return sko;
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<T> aValues ) {
    // проверим, что есть такой класс
    String classId = aValues.getAsAv( AID_CLASS_ID ).asString();
    ISkClassInfo cinf = skSysdescr().findClassInfo( classId );
    if( cinf == null ) {
      return ValidationResult.error( FMT_ERR_NO_CLASS_ID, classId );
    }
    // если задан strid, то проверим что такого объекта еще нет
    String strid = aValues.getAsAv( AID_STRID ).asString();
    if( !StridUtils.isValidIdPath( strid ) ) {
      return ValidationResult.error( FMT_ERR_INV_OBJ_STRID, strid );
    }
    Skid skid = new Skid( classId, strid );
    if( skObjServ().find( skid ) != null ) {
      return ValidationResult.error( FMT_ERR_DUP_CLASS_STRID, classId, strid );
    }
    // проверить значения атрибутов
    IDtoObject dpu = makeDtoFullObject( aValues );
    ValidationResult vr = skObjServ().svs().validator().canCreateObject( dpu );
    // TODO проверить значения связей
    return vr;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected T doCreate( IM5Bunch<T> aValues ) {
    return (T)internalDefineObject( aValues );
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<T> aValues ) {
    // нельзя менять класс
    String oldClassId = aValues.originalEntity().classId();
    String classId = aValues.getAsAv( AID_CLASS_ID ).asString();
    if( !Objects.equals( oldClassId, classId ) ) {
      return ValidationResult.error( FMT_ERR_CANT_EDIT_CLASS_ID, aValues.originalEntity().skid() );
    }
    // нельзя менять strid
    String oldStrid = aValues.originalEntity().strid();
    String strid = aValues.getAsAv( AID_STRID ).asString();
    if( !Objects.equals( oldStrid, strid ) ) {
      return ValidationResult.error( FMT_ERR_CANT_EDIT_STRID, aValues.originalEntity().skid() );
    }
    // проверить значения атрибутов
    IDtoObject dpu = makeDtoFullObject( aValues );
    ValidationResult vr = skObjServ().svs().validator().canEditObject( dpu, aValues.originalEntity() );
    // TODO проверить значения связей
    return vr;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected T doEdit( IM5Bunch<T> aValues ) {
    return (T)internalDefineObject( aValues );
  }

  @Override
  protected ValidationResult doBeforeRemove( T aEntity ) {
    if( skObjServ().find( aEntity.skid() ) == null ) {
      return ValidationResult.error( FMT_ERR_NO_OBJ_SKID, aEntity.skid() );
    }
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doRemove( T aEntity ) {
    skObjServ().removeObject( aEntity.skid() );
  }

}
