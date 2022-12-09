package org.toxsoft.uskat.ggprefs.lib.impl;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassHierarchyExplorer;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.ggprefs.lib.*;

/**
 * Реализация евентера {@link IGuiGwPrefsSectionEventer}.
 *
 * @author goga
 */
class SectionEventer
    implements IGuiGwPrefsSectionEventer {

  private final IMapEdit<IGuiGwPrefsObjectListener, GwidList> objListeners = new ElemMap<>();

  private final IMapEdit<IGuiGwPrefsOptionListener, IMapEdit<Gwid, IStringList>> optListeners = new ElemMap<>();

  private final IGuiGwPrefsSection source;
  private final ISkCoreApi         coreApi;
  private final ISkSysdescr        cim;

  private final IMapEdit<Skid, IStringListEdit> changedOpsMap = new ElemMap<>();

  private boolean paused = false;

  public SectionEventer( IGuiGwPrefsSection aSource, ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNulls( aSource, aCoreApi );
    source = aSource;
    coreApi = aCoreApi;
    cim = coreApi.sysdescr();
  }

  // ------------------------------------------------------------------------------------
  // Implemetation
  //

  private void reallyFireOptionEvent( Skid aObjSkid, IStringList aOpIds ) {
    if( aOpIds.isEmpty() ) {
      return;
    }
    for( IGuiGwPrefsOptionListener l : optListeners.keys() ) {
      IMap<Gwid, IStringList> gl = optListeners.getByKey( l );
      IGwidList keysList = new GwidList( gl.keys() );
      if( isSkidCoveredByGwidList( keysList, aObjSkid ) ) {
        IGwidList coverList = findGwidsCoverSkid( keysList, aObjSkid );
        for( String optId : aOpIds ) {
          for( Gwid coverGwid : coverList ) {
            if( gl.getByKey( coverGwid ).hasElem( optId ) ) {
              try {
                l.onGuiGwPrefsBundleChanged( source, aObjSkid, optId );
                break;
              }
              catch( Exception ex ) {
                LoggerUtils.errorLogger().error( ex );
              }
            }
          }
        }
      }
    }
  }

  private void reallyFireObjectEvent( Skid aObjSkid ) {
    for( IGuiGwPrefsObjectListener l : objListeners.keys() ) {
      IGwidList gl = objListeners.getByKey( l );
      if( isSkidCoveredByGwidList( gl, aObjSkid ) ) {
        try {
          l.onGuiGwPrefsBundleChanged( source, aObjSkid );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }
  }

  private boolean isSkidCoveredByGwidList( IGwidList aList, Skid aSkid ) {
    for( Gwid g : aList ) {
      if( g.isAbstract() ) {
        ISkClassHierarchyExplorer h = cim.hierarchy();
        if( h.isSuperclassOf( g.classId(), aSkid.classId() ) ) {
          return true;
        }
      }
      else {
        if( g.skid().equals( aSkid ) ) {
          return true;
        }
      }
    }
    return false;
  }

  private IGwidList findGwidsCoverSkid( IGwidList aList, Skid aSkid ) {
    GwidList result = new GwidList();
    for( Gwid g : aList ) {
      if( g.isAbstract() ) {
        ISkClassHierarchyExplorer h = cim.hierarchy();
        if( h.isSuperclassOf( g.classId(), aSkid.classId() ) ) {
          result.add( g );
        }
      }
      else {
        if( g.skid().equals( aSkid ) ) {
          result.add( g );
        }
      }
    }
    return result;
  }

  /**
   * Нормализует список {@link Gwid}-ов при добалении элемента.
   * <p>
   * Список <code>aList</code> содержит абстрактные и конкретные {@link Gwid}-ы вида {@link EGwidKind#GW_CLASS}.
   * Добавляемый GWID должен быть таким же.
   * <p>
   * Список является нормализованным, когда для любого SKID ему в списке соответствует не более одного GWID. Нужно для
   * того, чтобы для при изменений настроек люого объекта генерировать не более одного вызова объектоного слушателя.
   *
   * @param aList {@link GwidList} - нормализуемый список
   * @param aGwid {@link Gwid} - добавляемый GWID
   */
  private void normalizeGwidList( GwidList aList, Gwid aGwid ) {
    IListEdit<Gwid> toRemove = new ElemArrayList<>();
    for( Gwid g : aList ) {
      if( isGE( aGwid, g ) ) {
        toRemove.add( g );
      }
    }
    for( Gwid g : toRemove ) {
      aList.remove( g );
    }
    aList.add( aGwid );
  }

  private void normalizeGwidKeyList( IMapEdit<Gwid, IStringList> aGwidKeyMap, Gwid aGwid ) {
    IListEdit<Gwid> toRemove = new ElemArrayList<>();
    for( Gwid g : aGwidKeyMap.keys() ) {
      if( isGE( aGwid, g ) ) {
        toRemove.add( g );
      }
    }
    IStringListEdit newOptionIds = new StringArrayList();
    for( Gwid g : toRemove ) {
      newOptionIds.addAll( aGwidKeyMap.removeByKey( g ) );
    }
    aGwidKeyMap.put( aGwid, newOptionIds );
  }

  /**
   * Проверят, что новsq GWID является более "общей", чем старый.
   *
   * @param aNewGwid {@link Gwid} - новый GWID
   * @param aOldGwid {@link Gwid} - старый GWID
   * @return boolean - новая пара обеспечивает вызов слушателя как и старая, или в больших случаях
   */
  private boolean isGE( Gwid aNewGwid, Gwid aOldGwid ) {
    // класс нового объекта должен быть родителем или самим проверяемим классом
    ISkClassHierarchyExplorer h = cim.hierarchy();
    if( !h.isAssignableFrom( aNewGwid.classId(), aOldGwid.classId() ) ) {
      return false;
    }
    // если новый Gwid абстрактный, то достаточно - новый более общий Gwid чем старый
    if( aNewGwid.isAbstract() ) {
      return true;
    }
    // если новый конкретный, а старый абстрактный Gwid, то false
    if( aOldGwid.isAbstract() ) {
      return false;
    }
    // когда оба Gwid конкретный, то должны совпадать объекты
    return aNewGwid.skid().equals( aOldGwid.skid() );
  }

  // ------------------------------------------------------------------------------------
  // ITsPausabeEventsProducer
  //

  @Override
  public void pauseFiring() {
    paused = true;
  }

  @Override
  public void resumeFiring( boolean aFireDelayed ) {
    if( paused ) {
      paused = false;
      if( aFireDelayed && isPendingEvents() ) {
        for( Skid skid : changedOpsMap.keys() ) {
          reallyFireOptionEvent( skid, changedOpsMap.getByKey( skid ) );
          reallyFireObjectEvent( skid );
        }
      }
      resetPendingEvents();
    }
  }

  @Override
  public boolean isFiringPaused() {
    return paused;
  }

  @Override
  public boolean isPendingEvents() {
    return !changedOpsMap.isEmpty();
  }

  @Override
  public void resetPendingEvents() {
    changedOpsMap.clear();
  }

  // ------------------------------------------------------------------------------------
  // IGuiGwPrefsSectionEventer
  //

  @Override
  public void addObjectListener( Gwid aGwid, IGuiGwPrefsObjectListener aListener ) {
    TsNullArgumentRtException.checkNulls( aGwid, aListener );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_CLASS );
    GwidList gl = objListeners.findByKey( aListener );
    if( gl != null ) {
      normalizeGwidList( gl, aGwid );
    }
    else {
      objListeners.put( aListener, new GwidList( aGwid ) );
    }
  }

  @Override
  public void addOptionListener( Gwid aGwid, IStringList aOptionIds, IGuiGwPrefsOptionListener aListener ) {
    TsNullArgumentRtException.checkNulls( aGwid, aOptionIds, aListener );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_CLASS );
    IMapEdit<Gwid, IStringList> gl = optListeners.findByKey( aListener );

    if( gl != null ) {
      normalizeGwidKeyList( gl, aGwid );
      IStringListEdit newOptIds = new StringArrayList( gl.getByKey( aGwid ) );
      newOptIds.addAll( aOptionIds );
      gl.put( aGwid, newOptIds );
    }
    else {
      IMapEdit<Gwid, IStringList> newGl = new ElemMap<>();
      newGl.put( aGwid, aOptionIds );
      optListeners.put( aListener, newGl );
    }
  }

  @Override
  public void removeObjectListener( IGuiGwPrefsObjectListener aListener ) {
    objListeners.removeByKey( aListener );
  }

  @Override
  public void removeOptionListener( IGuiGwPrefsOptionListener aListener ) {
    optListeners.removeByKey( aListener );
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  void fireOptionEvent( Skid aObjSkid, IStringList aOpIds ) {
    if( !paused ) {
      reallyFireOptionEvent( aObjSkid, aOpIds );
      return;
    }
    else
      if( changedOpsMap.hasKey( aObjSkid ) ) {
        changedOpsMap.getByKey( aObjSkid ).addAll( aOpIds );
      }
      else {
        changedOpsMap.put( aObjSkid, new StringArrayList( aOpIds ) );
      }
  }

  void fireObjectEvent( Skid aObjSkid ) {
    if( !paused ) {
      reallyFireObjectEvent( aObjSkid );
      return;
    }
    else
      if( !changedOpsMap.hasKey( aObjSkid ) ) {
        changedOpsMap.put( aObjSkid, new StringArrayList() );
      }
  }

  void clearListeners() {
    objListeners.clear();
  }

}
