package org.toxsoft.uskat.s5.server.backend.supports.gwiddb;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.gwiddb.IS5GwidDbSQLConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.gwiddb.IS5Resources.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.BaMsgGwidDbChanged;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Реализация {@link IS5BackendGwidDbSingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LINKS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendGwidDbSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendGwidDbSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_GWIDDB_ID = "S5BackendGwidDbSingleton"; //$NON-NLS-1$

  /**
   * Имя кодировки текста в значениях clob
   */
  private static final String CHARSET_NAME = "UTF-8"; //$NON-NLS-1$

  /**
   * Менеджер постоянства
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Конструктор.
   */
  public S5BackendGwidDbSingleton() {
    super( BACKEND_GWIDDB_ID, STR_D_BACKEND_GWIDDB );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // nop
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendGwidDbSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IdChain> listSectionIds() {
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_SECTIONS );
    Query query = entityManager.createNativeQuery( sql );
    List<String> sectionIds = query.getResultList();
    IListEdit<IdChain> retValue = new ElemArrayList<>( sectionIds.size() );
    for( String sectionId : sectionIds ) {
      retValue.add( IdChain.KEEPER.str2ent( sectionId ) );
    }
    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<Gwid> listKeys( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_KEYS, IdChain.KEEPER.ent2str( aSectionId ) );
    // Запрос
    TypedQuery<S5GwidDbEntity> query = entityManager.createQuery( sql, S5GwidDbEntity.class );
    List<S5GwidDbEntity> entities = query.getResultList();
    GwidList retValue = new GwidList();
    for( S5GwidDbEntity entiry : entities ) {
      retValue.add( entiry.id().gwid() );
    }
    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public String readValue( IdChain aSectionId, Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey );
    S5GwidDbEntity entity = entityManager.find( S5GwidDbEntity.class, new S5GwidDbID( aSectionId, aKey ) );
    try {
      return (entity != null ? new String( entity.getBlob(), CHARSET_NAME ) : null);
    }
    catch( UnsupportedEncodingException e ) {
      logger().error( e );
      throw new TsInternalErrorRtException( e );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeValue( IdChain aSectionId, Gwid aKey, String aValue ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey, aValue );
    try {
      S5GwidDbID id = new S5GwidDbID( aSectionId, aKey );
      S5GwidDbEntity oldEntity = entityManager.find( S5GwidDbEntity.class, id );
      S5GwidDbEntity newEntity = new S5GwidDbEntity( id, aValue.getBytes( CHARSET_NAME ) );
      entityManager.merge( newEntity );
      entityManager.flush();
      // Передача сообщения
      fireChangedEvent( backend().attachedFrontends(), aSectionId, oldEntity == null ? ECrudOp.CREATE : ECrudOp.EDIT,
          aKey );
    }
    catch( UnsupportedEncodingException e ) {
      logger().error( e );
      throw new TsInternalErrorRtException( e );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void removeValue( IdChain aSectionId, Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aKey );
    S5GwidDbEntity entity = entityManager.find( S5GwidDbEntity.class, new S5GwidDbID( aSectionId, aKey ) );
    if( entity != null ) {
      entityManager.remove( entity );
      entityManager.flush();
      // Передача сообщения
      fireChangedEvent( backend().attachedFrontends(), aSectionId, ECrudOp.REMOVE, aKey );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void removeSection( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    IList<Gwid> keys = listKeys( aSectionId );
    if( keys.size() == 0 ) {
      return;
    }
    for( Gwid key : keys ) {
      S5GwidDbEntity entity = entityManager.find( S5GwidDbEntity.class, new S5GwidDbID( aSectionId, key ) );
      if( entity != null ) {
        entityManager.remove( entity );
      }
    }
    entityManager.flush();
    // Передача сообщения
    fireChangedEvent( backend().attachedFrontends(), aSectionId, ECrudOp.REMOVE,
        keys.size() == 1 ? keys.first() : null );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

  /**
   * Формирование события: произошло изменение clob
   *
   * @param aFrontends {@link IS5FrontendRear} список фронтендов подключенных к бекенду
   * @param aSectionId {@link IdChain} идентификатор секции
   * @param aOp {@link ECrudOp} операция изменения
   * @param aKey {@link Gwid} ключ значения.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void fireChangedEvent( IList<IS5FrontendRear> aFrontends, IdChain aSectionId, ECrudOp aOp,
      Gwid aKey ) {
    TsNullArgumentRtException.checkNulls( aFrontends, aKey );
    GtMessage message = BaMsgGwidDbChanged.BUILDER.makeMessage( aSectionId, aOp, aKey );
    for( IS5FrontendRear frontend : aFrontends ) {
      frontend.onBackendMessage( message );
    }
  }
}
