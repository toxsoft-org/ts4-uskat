package org.toxsoft.uskat.s5.server.transactions;

import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.transactions.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.annotation.*;
import javax.ejb.*;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.transaction.TransactionSynchronizationRegistry;

import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.info.ITransactionInfo;
import org.toxsoft.uskat.s5.common.info.ITransactionsInfos;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Начало выполнения кода сервера S5 - первый стартующий синглтон.
 * <p>
 * Мониторниг и управление транзакциями сервера s5
 * <p>
 * Внимание: реализация построена отдельно от {@link S5SingletonBase} так последний использует функции
 * {@link IS5TransactionManagerSingleton} при загрузке/выгрузе синглетонов
 * <p>
 * Источники:
 * <p>
 * http://docs.oracle.com/cd/E26576_01/doc.312/e24930/transactions-service.htm
 * <p>
 * http://docs.oracle.com/javaee/5/api/javax/transactions/TransactionSynchronizationRegistry.html
 *
 * @author mvk
 */
@Startup
@Singleton
@DependsOn( { //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@ExcludeDefaultInterceptors
@Lock( LockType.READ )
public class S5TransactionManager
    extends Stridable
    implements IS5TransactionManagerSingleton, IS5TransactionDetectorSingleton, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String TRANSACTION_MANAGER_ID = "S5TransactionManager"; //$NON-NLS-1$

  /**
   * Размер истории завершенных транзакций
   */
  private static final int COMMITED_HISTORY_SIZE = 100;

  /**
   * Размер истории отмененных транзакций
   */
  private static final int ROLLBACKED_HISTORY_SIZE = 100;

  /**
   * Размер списка самых длительных транзакций
   */
  private static final int LONG_HISTORY_SIZE = 10;

  @Resource
  private SessionContext sessionContext;

  /**
   * Слушатели ВСЕХ транзакций
   */
  private final IListEdit<IS5TransactionListener> listeners = new ElemArrayList<>( false );

  /**
   * Текущие активные транзакции
   */
  private final IStringMapEdit<S5Transaction> transactions = new StringMap<>();

  /**
   * Список последних завершенных транзакций
   */
  private final IListEdit<ITransactionInfo> commited   = new ElemLinkedList<>();
  /**
   * Список последних отменных транзакций
   */
  private final IListEdit<ITransactionInfo> rollbacked = new ElemLinkedList<>();
  /**
   * Список длительных транзакций
   */
  private final IListEdit<ITransactionInfo> longTimes  = new ElemLinkedList<>();

  /**
   * Количество успешно завершенных транзакций
   */
  private long commitCount;

  /**
   * Количество откатов транзакций
   */
  private long rollbackCount;

  /**
   * Реестр транзакций
   */
  @Resource
  private TransactionSynchronizationRegistry registry;

  /**
   * Блокировка доступа
   */
  private final S5Lockable lock = new S5Lockable();

  /**
   * Журнал
   */
  private final ILogger logger = getLogger( getClass() );

  /**
   * Пустой конструктор.
   */
  public S5TransactionManager() {
    super( TRANSACTION_MANAGER_ID, STR_D_TRANSACTION_MANAGER, TsLibUtils.EMPTY_STRING );
    logger.info( MSG_CREATE_SINGLETON );
  }

  // ------------------------------------------------------------------------------------
  // Определение жизненного цикла
  //
  /**
   * Загрузка синглетона в контейнер. Метод должен осуществлять всю иницилизацию, необходимую синглтону
   */
  @PostConstruct
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  private void load() {
    logger.info( MSG_INIT_SINGLETON );
  }

  /**
   * Выгрузка синглетона из контейнера. Метод должен завершать работу синглтона
   */
  @PreDestroy
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  private void unload() {
    logger.info( MSG_CLOSE_SINGLETON );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5TransactionSingleton
  //
  @Override
  public int openCount() {
    lockRead( lock );
    try {
      return transactions.size();
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  public ITransactionsInfos getInfos() {
    lockRead( lock );
    try {
      IListEdit<ITransactionInfo> openInfoes = new ElemArrayList<>( transactions.size() );
      IList<S5Transaction> transactionList = transactions.values();
      for( int index = 0, n = transactionList.size(); index < n; index++ ) {
        openInfoes.add( new S5TransactionInfo( transactionList.get( index ) ) );
      }
      return new S5TransactionInfos( commitCount, rollbackCount, openInfoes, commited, rollbacked, longTimes );
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  public S5Transaction findTransaction() {
    lockRead( lock );
    try {
      Object transactionKey = registry.getTransactionKey();
      if( transactionKey == null ) {
        // Транзакция не открыта
        return null;
      }
      S5Transaction transaction = transactions.findByKey( transactionKey.toString() );
      return transaction;
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  public S5Transaction getTransaction() {
    S5Transaction retValue = findTransaction();
    if( retValue == null ) {
      // Транзакция не открыта
      throw new TsIllegalArgumentRtException( MSG_ERR_NO_TRANSACTION );
    }
    return retValue;
  }

  @Override
  public void checkCommitReady() {
    S5Transaction transaction = null;
    lockRead( lock );
    try {
      transaction = getTransaction();
    }
    finally {
      unlockRead( lock );
    }
    checkCommitResources( transaction, logger );
  }

  @Override
  public void addTransactionListener( IS5TransactionListener aListener ) {
    TsNullArgumentRtException.checkNull( aListener );
    lockWrite( lock );
    try {
      listeners.add( aListener );
      // Добавление слушателя во все открытые транзакции
      for( S5Transaction tx : transactions.values() ) {
        tx.addListener( aListener );
      }
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public void removeTransactionListener( IS5TransactionListener aListener ) {
    lockWrite( lock );
    try {
      listeners.remove( aListener );
      // Удаления слушателя из всех открытых транзакций
      for( S5Transaction tx : transactions.values() ) {
        tx.removeListener( aListener );
      }
    }
    finally {
      unlockWrite( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5TransactionDetectorSingleton
  //
  @Override
  public void onCallBusinessMethod( Object aOwner, Method aMethod, Object[] aParams ) {
    TsNullArgumentRtException.checkNulls( aOwner, aMethod, aParams );
    S5Transaction transaction = null;
    Object transactionKey = registry.getTransactionKey();
    if( transactionKey == null ) {
      // Транзакция не открыта
      return;
    }
    String key = transactionKey.toString();
    ETransactionStatus status = getTransactionStatus();
    lockWrite( lock );
    try {
      transaction = transactions.findByKey( key );
      if( status == ETransactionStatus.ACTIVE && transaction == null ) {
        // Пояснение к транзакции
        String descr = TsLibUtils.EMPTY_STRING;
        if( commitCount + rollbackCount <= 0 ) {
          // Первая транзакция. Вероятно, что это инициализация
          descr = MSG_FIRST_TRANSACTION;
        }
        String caller = sessionContext.getCallerPrincipal().getName();
        transaction = new S5Transaction( this, caller, transactionKey, aOwner, aMethod, aParams, descr );
        transactions.put( key, transaction );
        // Регистрируемся на получений извещения об изменениях
        registry.registerInterposedSynchronization( transaction );
        // Регистрация слушателей менеджера
        for( IS5TransactionListener listener : listeners ) {
          transaction.addListener( listener );
        }
        // Журнал: регистрация транзакции
        logger.debug( MSG_TX_REGISTER, transaction );
        return;
      }
    }
    finally {
      unlockWrite( lock );
    }
    if( transaction != null ) {
      // Журнал: продолжение работы в транзакции или изменение ее статуса
      logger.debug( transaction.getStatus() == status ? MSG_TX_CONTINUE : MSG_TX_CHANGED, transaction );
    }
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Обработка события: изменение статуса транзакции "перед завершением"
   *
   * @param aTransaction {@link S5Transaction} транзакция
   * @throws TsNullArgumentRtException аргумент = null
   */
  void beforeCompletion( S5Transaction aTransaction ) {
    TsNullArgumentRtException.checkNull( aTransaction );
    lockWrite( lock, ACCESS_TIMEOUT_DEFAULT );
    try {
      // TODO: почему то в данном месте статус транзакции ACTIVE
      // ETransactionStatus status = ETransactionStatus.values()[registry.getTransactionStatus()];
      ETransactionStatus status = ETransactionStatus.PREPARED;
      aTransaction.setStatus( status );
      logger.debug( aTransaction.toString() );
    }
    finally {
      unlockWrite( lock );
    }
    // Журнал
    logger.debug( MSG_TX_CHANGED, aTransaction );
    // Оповещение слушателей
    fireTransactionEvent( aTransaction, logger );
  }

  /**
   * Обработка события: изменение статуса транзакции "после завершения"
   *
   * @param aTransaction {@link S5Transaction} транзакция
   * @param aStatus int статус транзакции
   * @throws TsNullArgumentRtException аргумент = null
   */
  void afterCompletion( S5Transaction aTransaction, int aStatus ) {
    TsNullArgumentRtException.checkNull( aTransaction );
    Object transactionKey = aTransaction.getKey();
    String key = transactionKey.toString();
    lockWrite( lock, ACCESS_TIMEOUT_DEFAULT );
    try {
      ETransactionStatus status = ETransactionStatus.values()[aStatus];
      aTransaction.setStatus( status );
      transactions.removeByKey( key );
      // Обработка счетчиков
      if( status == ETransactionStatus.COMMITED ) {
        // Транзакция успешно завершена
        commited.add( new S5TransactionInfo( aTransaction ) );
        if( commited.size() > COMMITED_HISTORY_SIZE ) {
          commited.removeByIndex( 0 );
        }
        commitCount++;
      }
      if( status == ETransactionStatus.ROLLEDBACK ) {
        // Откат транзакции
        rollbacked.add( new S5TransactionInfo( aTransaction ) );
        if( rollbacked.size() > ROLLBACKED_HISTORY_SIZE ) {
          rollbacked.removeByIndex( 0 );
        }
        rollbackCount++;
      }
      // Попытка добавить транзакцию в список долго выполняемых
      tryAddToLongTime( aTransaction );
      logger.debug( aTransaction.toString() );
    }
    finally {
      unlockWrite( lock );
    }
    // Журнал
    logger.debug( MSG_TX_CHANGED, aTransaction );
    // Оповещение слушателей
    fireTransactionEvent( aTransaction, logger );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает статус текущей транзакции
   *
   * @return {@link ETransactionStatus} - статус текущей транзакции
   */
  private ETransactionStatus getTransactionStatus() {
    return ETransactionStatus.values()[registry.getTransactionStatus()];
  }

  /**
   * Осуществляет попытку добавить завершившуюся транзакцию в список долго выполняемых транзакций
   *
   * @param aTransaction {@link S5Transaction} транзакция
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException транзакция не завершена
   */
  private void tryAddToLongTime( S5Transaction aTransaction ) {
    TsNullArgumentRtException.checkNull( aTransaction );
    TsIllegalArgumentRtException.checkTrue( aTransaction.closeTime() == 0 );
    long duration = aTransaction.closeTime() - aTransaction.openTime();
    for( int index = longTimes.size() - 1; index >= 0; index-- ) {
      ITransactionInfo info = longTimes.get( index );
      long nextDuration = info.closeTime() - info.openTime();
      if( nextDuration > duration ) {
        // Дальше искать нет смысла.
        if( index == LONG_HISTORY_SIZE - 1 ) {
          // Список уже полный
          return;
        }
        // Добавляем новую транзакцию
        longTimes.insert( index + 1, new S5TransactionInfo( aTransaction ) );
        // Контролируем размер списка
        if( longTimes.size() > LONG_HISTORY_SIZE ) {
          longTimes.removeByIndex( longTimes.size() - 1 );
        }
        return;
      }
    }
    // Все транзакции в списке оказались с временем меньшим чем указанная транзакция. Добавляем ее в "голову"
    longTimes.insert( 0, new S5TransactionInfo( aTransaction ) );
    // Контролируем размер списка
    if( longTimes.size() > LONG_HISTORY_SIZE ) {
      longTimes.removeByIndex( longTimes.size() - 1 );
    }
  }

  /**
   * Рассылает всем слушателям сообщение об изменении состояния транзакции
   *
   * @param aTransaction {@link S5Transaction} - транзакция
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void fireTransactionEvent( S5Transaction aTransaction, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aTransaction, aLogger );
    IList<IS5TransactionListener> listeners = aTransaction.getListeners();
    for( int index = 0, n = listeners.size(); index < n; index++ ) {
      IS5TransactionListener listener = listeners.get( index );
      try {
        listener.changeTransactionStatus( aTransaction );
      }
      catch( Throwable e ) {
        aLogger.error( e, MSG_ERR_UNXEXPECTED_LISTENER_ERROR, listener, cause( e ) );
      }
    }
  }

  /**
   * Вызов проверяет ресурсы транзакции на предмет ее завершения
   * <p>
   * Метод не гарантирует, что транзакция после этого будет завершена и используется только для предупреждающего
   * сообщения (бизнес-ошибки состояния), до вызова процесса завершения транзакции
   *
   * @param aTransaction {@link S5Transaction} транзакция изменившая свой статус
   * @param aLogger {@link ILogger} журнал
   * @throws TsIllegalArgumentRtException бизнес-ошибка по которой невозможно завершить транзакцию
   */
  private static void checkCommitResources( S5Transaction aTransaction, ILogger aLogger ) {
    TsNullArgumentRtException.checkNull( aTransaction );
    IList<IS5TransactionListener> listeners = aTransaction.getListeners();
    for( int index = 0, n = listeners.size(); index < n; index++ ) {
      IS5TransactionListener listener = listeners.get( index );
      try {
        listener.checkCommitResources( aTransaction );
      }
      catch( TsIllegalArgumentRtException e ) {
        // Бизнес-ошибка завершения транзакции
        throw e;
      }
      catch( Throwable e ) {
        aLogger.error( e, MSG_ERR_UNXEXPECTED_CHECK_COMMIT_ERROR, listener, cause( e ) );
      }
    }
  }
}
