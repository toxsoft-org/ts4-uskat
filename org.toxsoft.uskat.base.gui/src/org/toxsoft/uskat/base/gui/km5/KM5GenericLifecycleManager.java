package org.toxsoft.uskat.base.gui.km5;

import static org.toxsoft.uskat.base.gui.km5.ISkResources.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import java.util.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Менеджер ЖЦ объектов, которые управляются службой {@link ISkObjectService}.
 * <p>
 * Менеджер исходит из того, что идентификатор модели совпадат с идентификатором класса {@link ISkClassInfo#id()}.
 * <p>
 * Вообще говоря, логика проверки возможности создания/редактирования и удаления объектов должна находится в службе
 * {@link ISkObjectService}, а не в этом классе.
 *
 * @author goga
 * @param <T> - конкретный класс моделируемой S5-сущности
 * @param <M> - класс мастер-объекта
 */
public class KM5GenericLifecycleManager<T extends ISkObject, M>
    extends KM5BasicLifecycleManager<T, M> {

  /**
   * Конструктор.
   *
   * @param aModel {@link IM5Model} - модель
   * @param aMaster &ltM&gt; - мастер-объект
   * @throws TsNullArgumentRtException aModel = null
   */
  public KM5GenericLifecycleManager( IM5Model<T> aModel, M aMaster ) {
    super( aModel, true, true, true, true, aMaster );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

  private DtoFullObject makeDtoObject( IM5Bunch<T> aValues ) {
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
    DtoObject objDto = makeDtoObject( aValues );
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
        rightSkids = SkUtils.objsToSkids( robjs );
      }
      skLinkServ().defineLink( sko.skid(), linf.id(), null, rightSkids );
    }
    return sko;
  }

  // ------------------------------------------------------------------------------------
  // Реализация M5LifecycleManager
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
    IDtoObject dpu = makeDtoObject( aValues );
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
    IDtoObject dpu = makeDtoObject( aValues );
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

  @Override
  protected IList<T> doListEntities() {
    return skObjServ().listObjs( model().id(), true );
  }

}
