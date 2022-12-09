package org.toxsoft.uskat.ggprefs.lib.impl;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemAlreadyExistsRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;

/**
 * Служебный класс обеспечения привязки опции к сущностям зеленого мира.
 *
 * @author goga
 */
class OptionBindings {

  private final ISkCoreApi coreApi;

  private final ISkSysdescr cim;

  /**
   * Описания опций раздела, привязанные к своим гвидам (в строковом виде)
   */
  private IStringMapEdit<IStridablesList<IDataDef>> sectOptionsDefs = new StringMap<>();

  public OptionBindings( ISkCoreApi aCoreApi ) {
    coreApi = aCoreApi;
    cim = coreApi.sysdescr();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  public void bindOptions( Gwid aGwid, IStridablesList<IDataDef> aOpDefs ) {
    TsNullArgumentRtException.checkNulls( aGwid, aOpDefs );
    coreApi.sysdescr().getClassInfo( aGwid.classId() );

    // проверка наличия объекта - отключена 2021.12.23
    // if( !aGwid.isAbstract() ) {
    // coreApi.objService().get( aGwid.skid() );
    // }

    // строковое представление гвида
    String gwidStr = aGwid.asString();

    // TsItemAlreadyExistsRtException.checkTrue( sectOptionsDefs.hasKey( gwidStr ),
    // "Options Defs have already bound whith gwid %s", gwidStr ); //$NON-NLS-1$

    // Проверка на наличие данных опций в списках зарегистрированных
    boolean alreadyExist = false;
    StringBuilder alreadyExistOptDefIds = new StringBuilder();
    for( String existedGwidStr : sectOptionsDefs.keys() ) {
      IStridablesList<IDataDef> opDefs = sectOptionsDefs.getByKey( existedGwidStr );
      for( IDataDef aOpDef : aOpDefs ) {
        boolean opDefAlreadyExist = opDefs.hasKey( aOpDef.id() );
        if( opDefAlreadyExist ) {
          alreadyExistOptDefIds.append( aOpDef.id() );
          alreadyExistOptDefIds.append( ", " ); //$NON-NLS-1$
        }

        alreadyExist = alreadyExist || opDefAlreadyExist;
      }
    }

    TsItemAlreadyExistsRtException.checkTrue( alreadyExist, "Option Defs: %s - have already bound whith gwid %s", //$NON-NLS-1$
        alreadyExistOptDefIds.toString(), gwidStr );

    // для данного гвида просто добавить определения опций
    if( !sectOptionsDefs.hasKey( gwidStr ) ) {
      sectOptionsDefs.put( gwidStr, aOpDefs );
    }
    else {
      IStridablesListEdit<IDataDef> newList = new StridablesList<>( sectOptionsDefs.getByKey( gwidStr ) );
      newList.addAll( aOpDefs );
      sectOptionsDefs.put( gwidStr, newList );
    }

  }

  public IStridablesList<IDataDef> listOptionDefs( Skid aObjSkid ) {
    // сначала проверяем наличие опций для объекта
    Gwid objGwid = Gwid.createObj( aObjSkid );

    if( sectOptionsDefs.hasKey( objGwid.asString() ) ) {
      return sectOptionsDefs.getByKey( objGwid.asString() );
    }

    // если для объекта опции не найдены - искать для класса и в супер-классах
    IStridablesListEdit<IDataDef> result = new StridablesList<>();

    // перебираем все классы, для которых связаны опции - ищем сам класс сущности или его супер-классы
    for( String gwidStr : sectOptionsDefs.keys() ) {
      Gwid gwid = Gwid.of( gwidStr );
      if( gwid.isAbstract() ) {
        if( cim.hierarchy().isAssignableFrom( gwid.classId(), aObjSkid.classId() ) ) {
          IStridablesList<IDataDef> gwidOpts = sectOptionsDefs.getByKey( gwidStr );
          result.addAll( gwidOpts );
        }
      }
    }

    return result;
  }

}
