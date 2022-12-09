package org.toxsoft.uskat.s5.server.backend.supports.clobs;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.clobs.IS5ClobsInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.clobs.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.clobs.IS5SQLConstants.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.IBaClobsMessages;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Реализация {@link IS5BackendClobsSingleton}.
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
public class S5BackendClobsSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendClobsSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_CLOBS_ID = "S5BackendClobsSingleton"; //$NON-NLS-1$

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
   * Поддержка интерсепторов операций проводимых над объектами
   */
  private final S5InterceptorSupport<IS5ClobsInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Конструктор.
   */
  public S5BackendClobsSingleton() {
    super( BACKEND_CLOBS_ID, STR_D_BACKEND_LOBS );
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
  // IS5BackendClobsSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public String readClob( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );

    // Пред-интерсепция
    String retValue = callBeforeReadClob( interceptors, aGwid );

    if( retValue == null ) {
      S5ClobEntity clob = findClob( aGwid );
      if( clob != null ) {
        try {
          retValue = new String( clob.getBlob(), CHARSET_NAME );
        }
        catch( UnsupportedEncodingException e ) {
          throw new TsInternalErrorRtException( e );
        }
      }
    }

    // Пост-интерсепция
    retValue = callAfterReadClob( interceptors, aGwid, retValue );

    // Проверка выполнения контракта метода
    // 2022-12-08 mvk по контракту если clob нет, то возвращается null
    // if( retValue == null ) {
    // // lob не найден
    // throw new TsIllegalArgumentRtException( MSG_ERR_LOB_NOT_FOUND, aGwid );
    // }
    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public boolean writeClob( Gwid aGwid, String aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );

    // Пред-интерсепция
    callBeforeWriteClobInterceptors( interceptors, aGwid, aValue );

    boolean retValue = false;
    try {
      S5ClobEntity clob = new S5ClobEntity( aGwid.asString(), aValue.getBytes( CHARSET_NAME ) );
      retValue = writeClob( aGwid, clob );
    }
    catch( UnsupportedEncodingException e ) {
      throw new TsInternalErrorRtException( e );
    }

    // Пост-интерсепция
    callAfterWriteClobInterceptors( interceptors, aGwid, aValue );

    // Оповещение об изменении данного
    fireWhenClobChanged( backend().attachedFrontends(), aGwid );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public boolean copyClob( Gwid aSourceGwid, Gwid aDestGwid ) {
    TsNullArgumentRtException.checkNulls( aSourceGwid, aDestGwid );
    // Значение исходного lob
    String clob = readClob( aSourceGwid );
    // Запись в приемный lob
    return writeClob( aDestGwid, clob );
  }

  @Override
  public void addLobsInterceptor( IS5ClobsInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeLobsInterceptor( IS5ClobsInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Читает значение конкретного clob-данного из системы
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @return {@link S5ClobEntity} lob-значение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private S5ClobEntity findClob( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_LOB, aGwid.asString() );
    // Запрос
    TypedQuery<S5ClobEntity> query = entityManager.createQuery( sql, S5ClobEntity.class );
    List<S5ClobEntity> entities = query.getResultList();
    if( entities.size() == 0 ) {
      return null;
    }
    // Отсоединяем узел от JPA
    S5ClobEntity lob = entities.get( 0 );
    entityManager.detach( lob );
    return lob;
  }

  /**
   * Сохраняет значение lob-данного в системе
   * <p>
   * Если в системе уже существует указанное данное, то его значение обновляется.
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aValue {@link S5ClobEntity} lob-значение
   * @return boolean <b>true</b> новое значение;<b>false</b> значение было обновлено.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean writeClob( Gwid aGwid, S5ClobEntity aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );
    boolean hasLob = hasClob( aGwid );
    entityManager.merge( aValue );
    return !hasLob;
  }

  private IGwidList listLobIds() {
    // Результат
    GwidList retValue = new GwidList();
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_SYS_IDS );
    // Запрос
    TypedQuery<String> query = entityManager.createQuery( sql, String.class );
    List<String> entities = query.getResultList();
    for( String id : entities ) {
      retValue.add( Gwid.of( id ) );
    }

    return retValue;
  }

  private boolean hasClob( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return listLobIds().hasElem( aGwid );
  }

  /**
   * Формирование события: произошло изменение clob
   *
   * @param aFrontends {@link IS5FrontendRear} список фронтендов подключенных к бекенду
   * @param aClobGwid {@link Gwid} идентификатор clob.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void fireWhenClobChanged( IList<IS5FrontendRear> aFrontends, Gwid aClobGwid ) {
    TsNullArgumentRtException.checkNulls( aFrontends, aClobGwid );
    GtMessage message = IBaClobsMessages.makeMessage( aClobGwid );
    for( IS5FrontendRear frontend : aFrontends ) {
      frontend.onBackendMessage( message );
    }
  }
}
