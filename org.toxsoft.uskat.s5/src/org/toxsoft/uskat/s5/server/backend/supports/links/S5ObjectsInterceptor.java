package org.toxsoft.uskat.s5.server.backend.supports.links;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5ObjectsInterceptor;

import ru.uskat.common.dpu.IDpuLinkFwd;
import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.common.dpu.impl.DpuLinkFwd;
import ru.uskat.core.api.sysdescr.ISkClassInfo;
import ru.uskat.core.api.sysdescr.ISkLinkInfo;

/**
 * Интерсептор операций над объектами используемый {@link S5BackendLinksSingleton}
 * <p>
 * Решаемые задачи:
 * <ul>
 * <li>Отслеживание удаления объектов. Удаление их связей на другие объекты;</li>
 * </ul>
 *
 * @author mvk
 */
class S5ObjectsInterceptor
    implements IS5ObjectsInterceptor {

  private final IS5BackendLinksSingleton linksBackend;

  /**
   * Конструктор
   *
   * @param aLinksBackend {@link IS5BackendLinksSingleton} backend управления связями между объектами системы
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5ObjectsInterceptor( IS5BackendLinksSingleton aLinksBackend ) {
    linksBackend = TsNullArgumentRtException.checkNull( aLinksBackend );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5ObjectsInterceptor
  //
  @Override
  public IDpuObject beforeFindObject( Skid aSkid, IDpuObject aObj ) {
    return aObj;
  }

  @Override
  public IDpuObject afterFindObject( Skid aSkid, IDpuObject aObj ) {
    return aObj;
  }

  @Override
  public void beforeReadObjects( IStringList aClassIds, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjects( IStringList aClassIds, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeWriteObjects( IMap<ISkClassInfo, IList<IDpuObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDpuObject>> aCreatedObjs ) {
    // Количество удалямых связей
    int removedLinkCount = 0;
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      removedLinkCount += aRemovedObjs.getByKey( classInfo ).size() * classInfo.linkInfos().size();
    }
    if( removedLinkCount > 0 ) {
      // Список удаляемых ПРЯМЫХ связей удаляемых объектов.
      IListEdit<IDpuLinkFwd> removedFwdLinks = new ElemArrayList<>( removedLinkCount );
      for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
        for( IDpuObject obj : aRemovedObjs.getByKey( classInfo ) ) {
          for( ISkLinkInfo link : classInfo.linkInfos() ) {
            removedFwdLinks.add( new DpuLinkFwd( classInfo.id(), link.id(), obj.skid(), ISkidList.EMPTY ) );
          }
        }
      }
      // Запись в базу данных. false: запретить перехват
      linksBackend.writeLinks( removedFwdLinks, false );
    }
  }

  @Override
  public void afterWriteObjects( IMap<ISkClassInfo, IList<IDpuObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDpuObject>> aCreatedObjs ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
