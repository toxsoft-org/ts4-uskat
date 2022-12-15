package org.toxsoft.uskat.ggprefs.lib.impl;

import static org.toxsoft.uskat.ggprefs.lib.impl.IServiceInternalConstants.*;
import static org.toxsoft.uskat.ggprefs.lib.impl.ISkResources.*;

import java.util.Objects;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.devapi.gwiddb.ISkGwidDbSection;
import org.toxsoft.uskat.core.devapi.gwiddb.ISkGwidDbService;
import org.toxsoft.uskat.core.impl.SkCoreApi;
import org.toxsoft.uskat.ggprefs.lib.*;

/**
 * Внутрипакетная реализация {@link IGuiGwPrefsSection}.
 *
 * @author goga
 */
class GuiGwPrefsSection
    implements IGuiGwPrefsSection, ICloseable {

  private final SkCoreApi      coreApi;
  private final ISkObject      sectionObject;
  private final IOptionSet     sectionParams;
  private final OptionBindings bindingsHolder;
  private final SectionEventer eventer;

  public GuiGwPrefsSection( SkCoreApi aCoreApi, ISkObject aSectObj ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aSectObj );
    coreApi = aCoreApi;
    sectionObject = aSectObj;
    sectionParams = aSectObj.attrs().getValobj( AINF_SECTTION_DEF_PARAMS.id() );
    bindingsHolder = new OptionBindings( aCoreApi );
    eventer = new SectionEventer( this, coreApi );
  }

  // ------------------------------------------------------------------------------------
  // Implementation
  //

  private IdChain sectionId() {
    return new IdChain( ISkGuiGwPrefsService.SERVICE_ID, id() );
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    // удалить слушателей
    eventer.clearListeners();

    // удалить хранящиеся значения ????

  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return sectionObject.id();
  }

  @Override
  public String nmName() {
    return sectionObject.nmName();
  }

  @Override
  public String description() {
    return sectionObject.description();
  }

  @Override
  public IOptionSet params() {
    return sectionParams;
  }

  // ------------------------------------------------------------------------------------
  // IGuiGwPrefsSection
  //

  @Override
  public void bindOptions( Gwid aGwid, IStridablesList<IDataDef> aOpDefs ) {
    bindingsHolder.bindOptions( aGwid, aOpDefs );
  }

  @Override
  public IStridablesList<IDataDef> listOptionDefs( Skid aObjSkid ) {
    return bindingsHolder.listOptionDefs( aObjSkid );
  }

  @Override
  public IOptionSet getOptions( Skid aObjSkid ) {
    // считаем опции (если есть) из хранилища
    IOptionSet clobOps = IOptionSet.NULL;
    ISkGwidDbService dbService = coreApi.gwidDbService();
    ISkGwidDbSection dbSection = dbService.defineSection( sectionId() );
    Gwid gwid = Gwid.createObj( aObjSkid );
    if( dbSection.hasClob( gwid ) ) {
      String clobString = dbSection.readClob( gwid );
      clobOps = OptionSetKeeper.KEEPER.str2ent( clobString );
    }
    // формируем набор так, чтобы в нем были только известные опции
    IOptionSetEdit ops = new OptionSet();
    IStridablesList<IDataDef> defs = bindingsHolder.listOptionDefs( aObjSkid );
    for( IDataDef d : defs ) {
      IAtomicValue avDest = d.defaultValue();
      if( clobOps.hasValue( d ) ) {
        IAtomicValue av = clobOps.getValue( d );
        // не заносим несовместимые типы, чтобы отбросить старое значение (после измненения типа в описании опции)
        if( AvTypeCastRtException.canAssign( d.atomicType(), av.atomicType() ) ) {
          avDest = av;
        }
      }
      ops.setValue( d, avDest );
    }
    return ops;
  }

  @Override
  public void setOptions( Skid aObjSkid, IOptionSet aOps ) {
    TsNullArgumentRtException.checkNulls( aObjSkid, aOps );
    // проверка существования объекта - отключено 2021.12.23
    // ISkObjectService os = coreApi.objService();
    // if( os.find( aObjSkid ) == null ) {
    // throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_OBJ, aObjSkid.toString() );
    // }

    // если нет описанных опции, нечего делать
    IStridablesList<IDataDef> opDefs = bindingsHolder.listOptionDefs( aObjSkid );
    if( opDefs.isEmpty() ) {
      return;
    }
    // запомним текущие значения
    IOptionSet oldOps = getOptions( aObjSkid );
    IStringListEdit changedOpIds = new StringArrayList();
    // подготовим полный набор значении к записи: только описанные опции, с проверкой типов
    IOptionSetEdit newOps = new OptionSet();
    for( IDataDef d : opDefs ) {
      IAtomicValue av = aOps.getValue( d.id(), d.defaultValue() );
      AvTypeCastRtException.checkCanAssign( d.atomicType(), av.atomicType(), FMT_ERR_INV_OP_TYPE, av.atomicType().id(),
          d.atomicType().id() );
      IAtomicValue oldVal = oldOps.findValue( d.id() );
      if( !Objects.equals( av, oldVal ) ) {
        changedOpIds.add( d.id() );
      }
      newOps.setValue( d, av );
    }

    ISkGwidDbService dbService = coreApi.gwidDbService();
    ISkGwidDbSection dbSection = dbService.defineSection( sectionId() );

    String clobStr = OptionSetKeeper.KEEPER.ent2str( newOps );
    dbSection.writeClob( Gwid.createObj( aObjSkid ), clobStr );

    // генерируем нужные сообщения
    if( !changedOpIds.isEmpty() ) {
      eventer.fireOptionEvent( aObjSkid, changedOpIds );
      eventer.fireObjectEvent( aObjSkid );
    }
  }

  @Override
  public IGuiGwPrefsSectionEventer eventer() {
    return eventer;
  }

}
