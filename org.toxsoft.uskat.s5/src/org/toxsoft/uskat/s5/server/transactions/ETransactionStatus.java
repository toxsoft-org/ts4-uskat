package org.toxsoft.uskat.s5.server.transactions;

import static org.toxsoft.uskat.s5.server.transactions.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Статусы состояния транзакции
 *
 * @author mvk
 */
public enum ETransactionStatus
    implements IStridable {

  /**
   * Транзакция открыта.
   */
  ACTIVE( MSG_TX_STATUS_ACTIVE, true ),

  /**
   * Транзакция имеет признак отмены.
   */
  MARKED_ROLLBACK( MSG_TX_STATUS_MARKED_ROLLBACK, true ),

  /**
   * Транзакция готова для завершения.
   */
  PREPARED( MSG_TX_STATUS_PREPARED, true ),

  /**
   * Транзакция завершена.
   */
  COMMITED( MSG_TX_STATUS_COMMITED, false ),

  /**
   * Транзакция отменена.
   */
  ROLLEDBACK( MSG_TX_STATUS_ROLLEDBACK, false ),

  /**
   * Неизвестное состояние.
   */
  UNKNOWN( MSG_TX_STATUS_UNKNOWN, false ),

  /**
   * Нет транзакции.
   */
  NO_TRANSACTION( MSG_TX_STATUS_NO_TRANSACTION, false ),

  /**
   * Подготовка тразнакции для завершения.
   */
  PREPARING( MSG_TX_STATUS_PREPARING, true ),

  /**
   * Транзакция в состоянии завершения.
   */
  COMMITTING( MSG_TX_STATUS_COMMITTING, true ),

  /**
   * Транзакция в состоянии отмены.
   */
  ROLLING_BACK( MSG_TX_STATUS_ROLLING_BACK, true ),;

  private final String  id;
  private final String  description;
  private final boolean open;

  /**
   * Создать константу с зданием всех инвариантов.
   *
   * @param aDescr String - отображаемое описание параметра
   * @param aOpen boolean <b>true</b> транзакция открыта; <b>false</b> транзакция закрыта
   */
  ETransactionStatus( String aDescr, boolean aOpen ) {
    id = name();
    description = aDescr;
    open = aOpen;
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return TsLibUtils.EMPTY_STRING;
  }

  @Override
  public String description() {
    return description;
  }

  // ----------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает признак того, что транзакция открыта
   * <p>
   * Если транзакция открыта, то нельзя начинать новую транзакцию, и наборот, можно начинать новую транзакцию если нет
   * текущей (или она закрыта)
   *
   * @return <b>true</b> транзакция открыта; <b>false</b> транзакция закрыта
   */
  public boolean isOpen() {
    return open;
  }

  // ----------------------------------------------------------------------------------
  // Методы проверки
  //
  /**
   * Определяет, существует ли константа с заданным идентификатором.
   *
   * @param aId String - идентификатор {@link #id()} константы
   * @return boolean - <b>true</b> - да, есть константа с таким идентификатором;<br>
   *         <b>false</b> - нет такой константы.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemById( String aId ) {
    return findByIdOrNull( aId ) != null;
  }

  /**
   * Определяет, существует ли константа с заданным описанием.
   *
   * @param aDescription String - описание {@link #id()} константы
   * @return boolean - <b>true</b> - да, есть константа с таким описанием;<br>
   *         <b>false</b> - нет такой константы.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemByDescription( String aDescription ) {
    return findByDescriptionOrNull( aDescription ) != null;
  }

  // ----------------------------------------------------------------------------------
  // Методы поиска
  //

  /**
   * Находит константу с заданным идентификатором, а если нет такой константы, возвращает null.
   *
   * @param aId String - идентификатор {@link #id()} константы
   * @return ETransactionStatus - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ETransactionStatus findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( ETransactionStatus item : values() ) {
      if( item.id.equals( aId ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Находит константу с заданным идентификатором, а если нет такой константы, выбрасывает исключение.
   *
   * @param aId String - идентификатор {@link #id()} константы
   * @return ETransactionStatus - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static ETransactionStatus findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Находит константу с заданным описанием, а если нет такой константы, возвращает null.
   *
   * @param aDescription String - описание {@link #description()} константы
   * @return ETransactionStatus - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ETransactionStatus findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( ETransactionStatus item : values() ) {
      if( item.description.equals( aDescription ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Находит константу с заданным описанием, а если нет такой константы, выбрасывает исключение.
   *
   * @param aDescription String - описание {@link #description()} константы
   * @return ETransactionStatus - найденная константа или null
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static ETransactionStatus findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

}
