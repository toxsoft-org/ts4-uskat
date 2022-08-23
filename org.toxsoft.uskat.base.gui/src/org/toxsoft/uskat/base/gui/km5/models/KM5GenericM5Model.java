package org.toxsoft.uskat.base.gui.km5.models;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Модель по умолчанию для S5-сущностей.
 * <p>
 * Это полноценная модель, которая автоматически создается средствами поддержки {@link KM5Support} для тех классов
 * системного описания, которые не были смоделированы тематическими модулями {@link KM5AbstractContributor}. Модель
 * полноценно работает с объектами, которые создаются и управляются службой {@link ISkObjectService}.
 *
 * @author goga
 * @param <T> - конкретный класс моделируемой Sk-сущности
 */
public class KM5GenericM5Model<T extends ISkObject>
    extends KM5BasicModel<T> {

  /**
   * Конструктор.
   *
   * @param aClassId String - идентификатор моделируемого класса
   * @param aConn {@link ISkConnection} - соединение с сервером, используемое моделью
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого класса
   */
  public KM5GenericM5Model( String aClassId, ISkConnection aConn ) {
    this( getClassInfo( aClassId, aConn ), aConn );
  }

  /**
   * Конструктор.
   *
   * @param aClassInfo {@link ISkClassInfo} - описание моделируемого класса
   * @param aConn {@link ISkConnection} - соединение с сервером, используемое моделью
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  public KM5GenericM5Model( ISkClassInfo aClassInfo, ISkConnection aConn ) {
    super( TsNullArgumentRtException.checkNull( aClassInfo.id() ), (Class)ISkObject.class, aConn );
    setNameAndDescription( aClassInfo.nmName(), aClassInfo.description() );
    IStridablesListEdit<IM5FieldDef<? extends ISkObject, ?>> fdefs = new StridablesList<>();
    fdefs.addAll( SKID, CLASS_ID, STRID, NAME, DESCRIPTION );
    // добавим атрибуты
    for( IDtoAttrInfo ainf : aClassInfo.attrs().list() ) {
      if( !fdefs.hasKey( ainf.id() ) && !ISkHardConstants.isSkSysAttr( ainf ) ) {
        fdefs.add( new KM5AttributeFieldDef<>( ainf ) );
      }
    }
    // добавим связи
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
    // задаем генератор панелей
    setPanelCreator( new KM5GenericPanelCreator<T>() );
  }

  @Override
  protected IM5LifecycleManager<T> doCreateDefaultLifecycleManager() {
    ISkConnection conn = tsContext().get( ISkConnection.class );
    return new KM5GenericLifecycleManager<>( this, conn );
  }

  @Override
  protected IM5LifecycleManager<T> doCreateLifecycleManager( Object aMaster ) {
    return new KM5GenericLifecycleManager<>( this, aMaster );
  }

  private static ISkClassInfo getClassInfo( String aClassId, ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNulls( aClassId, aConnection );
    ISkSysdescr cim = aConnection.coreApi().sysdescr();
    return cim.getClassInfo( aClassId );
  }

}
