package org.toxsoft.uskat.s5.server.backend.supports.lobs;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.lobs.IS5LobsInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.lobs.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.lobs.IS5SQLConstants.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.persistence.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

import ru.uskat.legacy.IdPair;

/**
 * Реализация {@link IS5BackendLobsSingleton}.
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
public class S5BackendLobsSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendLobsSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_LOBS_ID = "S5BackendLobsSingleton"; //$NON-NLS-1$

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
  private final S5InterceptorSupport<IS5LobsInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Конструктор.
   */
  public S5BackendLobsSingleton() {
    super( BACKEND_LOBS_ID, STR_D_BACKEND_LOBS );
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
  // IS5BackendLobsSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IdPair> listLobIds() {
    // Результат
    IListEdit<IdPair> retValue = new ElemLinkedList<>();

    // Пред-интерсепция
    callBeforeListLobIds( interceptors, retValue );

    // Текст SQL-запроса
    String sql = format( QFRMT_GET_SYS_IDS );
    // Запрос
    TypedQuery<String> query = entityManager.createQuery( sql, String.class );
    List<String> entities = query.getResultList();
    for( String id : entities ) {
      retValue.add( new IdPair( id ) );
    }

    // Пост-интерсепция
    callAfterListLobIds( interceptors, retValue );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public boolean hasLob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    return listLobIds().hasElem( aId );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public String readClob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );

    // Пред-интерсепция
    String retValue = callBeforeReadClob( interceptors, aId );

    if( retValue == null ) {
      S5LobEntity lob = findLob( aId );
      if( lob != null ) {
        try {
          retValue = new String( lob.getBlob(), CHARSET_NAME );
        }
        catch( UnsupportedEncodingException e ) {
          throw new TsInternalErrorRtException( e );
        }
      }
    }

    // Пост-интерсепция
    retValue = callAfterReadClob( interceptors, aId, retValue );

    // Проверка выполнения контракта метода
    if( retValue == null ) {
      // lob не найден
      throw new TsIllegalArgumentRtException( MSG_ERR_LOB_NOT_FOUND, aId.pairId() );
    }
    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public boolean writeClob( IdPair aId, String aValue ) {
    TsNullArgumentRtException.checkNulls( aId, aValue );

    // Пред-интерсепция
    callBeforeWriteClobInterceptors( interceptors, aId, aValue );

    boolean retValue = false;
    try {
      S5LobEntity lob = new S5LobEntity( aId.pairId(), aValue.getBytes( CHARSET_NAME ) );
      retValue = writeLob( aId, lob );
    }
    catch( UnsupportedEncodingException e ) {
      throw new TsInternalErrorRtException( e );
    }

    // Пост-интерсепция
    callAfterWriteClobInterceptors( interceptors, aId, aValue );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public boolean copyClob( IdPair aSourceId, IdPair aDestId ) {
    TsNullArgumentRtException.checkNulls( aSourceId, aDestId );
    // Значение исходного lob
    String clob = readClob( aSourceId );
    // Запись в приемный lob
    return writeClob( aDestId, clob );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public boolean removeClob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );

    // Пред-интерсепция
    callBeforeRemoveClobInterceptors( interceptors, aId );

    // Текст SQL-запроса
    String sql = format( QFRMT_DELETE_LOB, aId.pairId() );
    // Удаление узла
    Query query = entityManager.createQuery( sql );
    // Признак удаления lob
    boolean retValue = (query.executeUpdate() > 0);

    // Пост-интерсепция
    callAfterRemoveClobInterceptors( interceptors, aId );

    return retValue;
  }

  @Override
  public void addLobsInterceptor( IS5LobsInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeLobsInterceptor( IS5LobsInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Читает значение lob-данного из системы
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @return {@link S5LobEntity} lob-значение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private S5LobEntity findLob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_LOB, aId.pairId() );
    // Запрос
    TypedQuery<S5LobEntity> query = entityManager.createQuery( sql, S5LobEntity.class );
    List<S5LobEntity> entities = query.getResultList();
    if( entities.size() == 0 ) {
      return null;
    }
    // Отсоединяем узел от JPA
    S5LobEntity lob = entities.get( 0 );
    entityManager.detach( lob );
    return lob;
  }

  /**
   * Сохраняет значение lob-данного в системе
   * <p>
   * Если в системе уже существует указанное данное, то его значение обновляется.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aValue {@link S5LobEntity} lob-значение
   * @return boolean <b>true</b> новое значение;<b>false</b> значение было обновлено.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean writeLob( IdPair aId, S5LobEntity aValue ) {
    TsNullArgumentRtException.checkNulls( aId, aValue );
    boolean hasLob = hasLob( aId );
    entityManager.merge( aValue );
    return !hasLob;
  }
}
