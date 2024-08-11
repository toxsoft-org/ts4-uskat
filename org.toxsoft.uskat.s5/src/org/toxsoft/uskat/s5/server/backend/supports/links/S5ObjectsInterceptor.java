package org.toxsoft.uskat.s5.server.backend.supports.links;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;

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
  public IDtoObject beforeFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  @Override
  public IDtoObject afterFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  @Override
  public void beforeReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // Количество удалямых связей
    int removedLinkCount = 0;
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      removedLinkCount += aRemovedObjs.getByKey( classInfo ).size() * classInfo.links().list().size();
    }
    if( removedLinkCount > 0 ) {
      // Список удаляемых ПРЯМЫХ связей удаляемых объектов.
      IListEdit<IDtoLinkFwd> removedFwdLinks = new ElemArrayList<>( removedLinkCount );
      for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
        for( IDtoObject obj : aRemovedObjs.getByKey( classInfo ) ) {
          for( IDtoLinkInfo link : classInfo.links().list() ) {
            String linkId = link.id();
            String linkClassId = classInfo.links().findSuperDeclarer( linkId ).id();
            Gwid linkGwid = Gwid.createLink( linkClassId, linkId );
            removedFwdLinks.add( new DtoLinkFwd( linkGwid, obj.skid(), ISkidList.EMPTY ) );
          }
        }
      }
      // Запись в базу данных. false: запретить перехват
      linksBackend.writeLinksFwd( removedFwdLinks, false );
    }
  }

  @Override
  public void afterWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
