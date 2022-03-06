package org.toxsoft.uskat.s5.server.transactions;

import java.lang.reflect.Method;

import javax.ejb.SessionContext;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Транзакция сервера s5
 *
 * @author mvk
 */
public interface IS5Transaction {

  /**
   * Возвращает имя владельца сессии в которой была открыта транзакция
   *
   * @return String имя владельца сессии {@link SessionContext#getCallerPrincipal()}
   */
  String getPrincipal();

  /**
   * Возвращает ключ(идентификатор) транзакции
   *
   * @return Object идентификтор транзакции
   */
  Object getKey();

  /**
   * Возвращает собственника (компонент) транзакции
   *
   * @return Object собственника (компонент)
   */
  Object getOwner();

  /**
   * Возвращает метод собственника открывший транзакцию
   *
   * @return {@link Method} метод
   */
  Method getMethod();

  /**
   * Возвращает значения аргументов метода открывшего транзакцию
   *
   * @return Object[] значения аргументов
   */
  Object[] getMethodArgs();

  /**
   * Возвращает описание транзакции
   *
   * @return String описание
   */
  String getDescription();

  /**
   * Возвращает текущий статус транзакции
   *
   * @return {@link ETransactionStatus} статус транзакции
   */
  ETransactionStatus getStatus();

  /**
   * Возвращает время (мсек с начала эпохи) открытия транзакции
   *
   * @return long время
   */
  long openTime();

  /**
   * Возвращает время (мсек с начала эпохи) последнего изменения статуса транзакции
   *
   * @return long время
   */
  long statusTime();

  /**
   * Возвращает время (мсек с начала эпохи) последнего завершения транзакции
   *
   * @return long время
   */
  long closeTime();

  // ------------------------------------------------------------------------------------
  // Управление ресурсами
  //
  /**
   * Возвращает ресурс транзакции
   *
   * @param <T> - тип ресурса
   * @param aResourceId String - идентификатор ресурса
   * @return Object ресурс транзакции
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException нет текущей транзакции
   * @throws TsItemNotFoundRtException ресурса нет в транзакции
   */
  <T> T getResource( String aResourceId );

  /**
   * Возвращает ресурс транзакции
   *
   * @param <T> - тип ресурса
   * @param aResourceId {@link IStridable} - идентификатор ресурса
   * @return Object ресурс транзакции
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException нет текущей транзакции
   * @throws TsItemNotFoundRtException ресурса нет в транзакции
   */
  <T> T getResource( IStridable aResourceId );

  /**
   * Проводит поиск и возвращает ресурс транзакции
   *
   * @param <T> - тип ресурса
   * @param aResourceId String - идентификатор ресурса
   * @return Object ресурс транзакции. null: ресурс не найден
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  <T> T findResource( String aResourceId );

  /**
   * Проводит поиск и возвращает ресурс транзакции
   *
   * @param <T> - тип ресурса
   * @param aResourceId {@link IStridable} - идентификатор ресурса
   * @return Object ресурс транзакции. null: ресурс не найден
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  <T> T findResource( IStridable aResourceId );

  /**
   * Добавляет ресурс в транзакцию
   * <p>
   * Если ресурс уже есть в транзакции, то заменяется на новый
   *
   * @param <T> - тип ресурса
   * @param aResourceId String - идентификатор ресурса
   * @param aResource Object - ресурс доступный в рамках текущей транзакции
   * @return Object ресурс который ранее находился в транзации. null: ресурса не было
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  <T> T putResource( String aResourceId, Object aResource );

  /**
   * Добавляет ресурс в транзакцию
   * <p>
   * Если ресурс уже есть в транзакции, то заменяется на новый
   *
   * @param <T> - тип ресурса
   * @param aResourceId {@link IStridable} - идентификатор ресурса
   * @param aResource Object - ресурс доступный в рамках текущей транзакции
   * @return Object ресурс который ранее находился в транзации. null: ресурса не было
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  <T> T putResource( IStridable aResourceId, Object aResource );

  /**
   * Удаляет ресурс из транзакции
   * <p>
   * Если ресурса нет, то ничего не делает
   *
   * @param <T> - тип ресурса
   * @param aResourceId String - идентификатор ресурса
   * @return Object удаленный ресурс. null: ресурс не найден.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  <T> T removeResource( String aResourceId );

  /**
   * Удаляет ресурс из транзакции
   * <p>
   * Если ресурса нет, то ничего не делает
   *
   * @param <T> - тип ресурса
   * @param aResourceId {@link IStridable} - идентификатор ресурса
   * @return Object удаленный ресурс. null: ресурс не найден.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  <T> T removeResource( IStridable aResourceId );

  // ------------------------------------------------------------------------------------
  // Управление ресурсами
  //
  /**
   * Добавляет слушателя изменения статуса транзакции.
   * <p>
   * Если такой слушатель уже зарегистрирован, метод ничего не делает.
   *
   * @param aListener {@link IS5TransactionListener} - слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addListener( IS5TransactionListener aListener );

  /**
   * Удаляет слушателя изменения статуса транзакции.
   * <p>
   * Если такой слушатель не зарегистрирован, то метод ничего не делает.
   *
   * @param aListener {@link IS5TransactionListener} - слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeListener( IS5TransactionListener aListener );

  /**
   * Возвращает список слушателей транзакции
   *
   * @return {@link IList}&lt;{@link IS5TransactionListener}&gt; - список слушателей
   */
  IList<IS5TransactionListener> getListeners();
}
