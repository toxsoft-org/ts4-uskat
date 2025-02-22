package org.toxsoft.uskat.s5.server.transactions;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.server.transactions.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.lang.reflect.*;

import javax.ejb.*;
import javax.transaction.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Транзакция s5-сервера
 *
 * @author mvk
 */
final class S5Transaction
    implements IS5Transaction, Synchronization {

  private final S5TransactionManager              txManager;
  private final String                            principal;
  private final Object                            key;
  private final Object                            owner;
  private final Method                            method;
  private final Object[]                          methodArgs;
  private final String                            description;
  private volatile ETransactionStatus             status    = ETransactionStatus.ACTIVE;
  private final long                              openTime;
  private volatile long                           statusTime;
  private volatile long                           closeTime;
  private final IListEdit<IS5TransactionListener> listeners = new ElemArrayList<>( false );
  private final IStringMapEdit<Object>            resources = new StringMap<>();
  private final S5Lockable                        lock      = new S5Lockable();

  /**
   * Конструктор
   *
   * @param aTxManager {@link S5TransactionManager} менеджер транзакций
   * @param aPrincipal String - имя владельца сессии открывшей транзакцию {@link SessionContext#getCallerPrincipal()}
   * @param aKey Object ключ(идентификатор) транзакции
   * @param aOwner Object собственник (компонент) транзакции
   * @param aMethod {@link Method} метод собственника открывший транзакцию
   * @param aMethodArgs Object[] аргументы метода открывшего транзакцию
   * @param aDescription String описание к транзакции
   */
  S5Transaction( S5TransactionManager aTxManager, String aPrincipal, Object aKey, Object aOwner, Method aMethod,
      Object aMethodArgs[], String aDescription ) {
    TsNullArgumentRtException.checkNulls( aTxManager, aPrincipal, aKey, aOwner, aMethod, aMethodArgs );
    txManager = aTxManager;
    principal = aPrincipal;
    key = aKey;
    owner = aOwner;
    method = aMethod;
    methodArgs = aMethodArgs;
    description = aDescription;
    openTime = System.currentTimeMillis();
    statusTime = openTime;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5Transaction
  //
  @Override
  public String getPrincipal() {
    return principal;
  }

  @Override
  public Object getKey() {
    return key;
  }

  @Override
  public Object getOwner() {
    return owner;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public Object[] getMethodArgs() {
    return methodArgs;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public ETransactionStatus getStatus() {
    lockRead( lock );
    try {
      return status;
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  public long openTime() {
    return openTime;
  }

  @Override
  public long statusTime() {
    lockRead( lock );
    try {
      return statusTime;
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  public long closeTime() {
    lockRead( lock );
    try {
      return closeTime;
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getResource( String aResourceId ) {
    StridUtils.checkValidIdPath( aResourceId );
    lockRead( lock );
    try {
      return (T)resources.getByKey( aResourceId );
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getResource( IStridable aResourceId ) {
    TsNullArgumentRtException.checkNull( aResourceId );
    lockRead( lock );
    try {
      return (T)resources.getByKey( aResourceId.id() );
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getResource( String aResourceId, T aDefaultValue ) {
    TsNullArgumentRtException.checkNull( aResourceId );
    lockRead( lock );
    try {
      T retValue = (T)resources.findByKey( aResourceId );
      return (retValue != null ? retValue : aDefaultValue);
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getResource( IStridable aResourceId, T aDefaultValue ) {
    TsNullArgumentRtException.checkNull( aResourceId );
    lockRead( lock );
    try {
      T retValue = (T)resources.findByKey( aResourceId.id() );
      return (retValue != null ? retValue : aDefaultValue);
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T findResource( String aResourceId ) {
    StridUtils.checkValidIdPath( aResourceId );
    lockRead( lock );
    try {
      return (T)resources.findByKey( aResourceId );
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T findResource( IStridable aResourceId ) {
    TsNullArgumentRtException.checkNull( aResourceId );
    lockRead( lock );
    try {
      return (T)resources.findByKey( aResourceId.id() );
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T putResource( String aResourceId, Object aResource ) {
    StridUtils.checkValidIdPath( aResourceId );
    TsNullArgumentRtException.checkNull( aResource );
    lockWrite( lock );
    try {
      return (T)resources.put( aResourceId, aResource );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T putResource( IStridable aResourceId, Object aResource ) {
    TsNullArgumentRtException.checkNulls( aResource, aResource );
    lockWrite( lock );
    try {
      return (T)resources.put( aResourceId.id(), aResource );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T removeResource( String aResourceId ) {
    StridUtils.checkValidIdPath( aResourceId );
    lockWrite( lock );
    try {
      return (T)resources.removeByKey( aResourceId );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T removeResource( IStridable aResourceId ) {
    TsNullArgumentRtException.checkNull( aResourceId );
    lockWrite( lock );
    try {
      return (T)resources.removeByKey( aResourceId.id() );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public void addListener( IS5TransactionListener aListener ) {
    TsNullArgumentRtException.checkNull( aListener );
    lockWrite( lock );
    try {
      listeners.add( aListener );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public void removeListener( IS5TransactionListener aListener ) {
    TsNullArgumentRtException.checkNull( aListener );
    lockWrite( lock );
    try {
      listeners.remove( aListener );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public IList<IS5TransactionListener> getListeners() {
    lockRead( lock );
    try {
      return new ElemArrayList<>( listeners );
    }
    finally {
      unlockRead( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса Synchronization
  //
  @Override
  public void beforeCompletion() {
    txManager.beforeCompletion( this );
  }

  @Override
  public void afterCompletion( int aStatus ) {
    txManager.afterCompletion( this, aStatus );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Устанавливает текущий статус транзакции
   * <p>
   * Если текущий статус транзакции стал соответствовать не активной транзакции, то проводится оповещение слушателей
   *
   * @param aStatus {@link ETransactionStatus} - статус транзакции
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setStatus( ETransactionStatus aStatus ) {
    TsNullArgumentRtException.checkNull( aStatus );
    lockWrite( lock );
    try {
      if( status != aStatus ) {
        // Время изменения статуса
        statusTime = System.currentTimeMillis();
      }
      status = aStatus;
      if( status == ETransactionStatus.COMMITED || status == ETransactionStatus.ROLLEDBACK ) {
        // Время завершения транзакции
        closeTime = statusTime;
      }
    }
    finally {
      unlockWrite( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Переопределение Object
  //
  @Override
  public String toString() {
    return format( MSG_TRANSACTION, ownerName( owner ), method.getName(), status, principal, key, description );
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals( Object obj ) {
    if( this == obj ) {
      return true;
    }
    if( obj == null ) {
      return false;
    }
    if( getClass() != obj.getClass() ) {
      return false;
    }
    S5Transaction other = (S5Transaction)obj;
    return key.equals( other.key );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает имя класса собственника транзакции
   *
   * @param aOwner Object бин. null: незарегистрирован
   * @return имя класса
   */
  private static String ownerName( Object aOwner ) {
    return aOwner.getClass().getSimpleName();
  }
}
