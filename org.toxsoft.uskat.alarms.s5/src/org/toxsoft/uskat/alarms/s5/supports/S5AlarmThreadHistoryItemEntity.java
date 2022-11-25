package org.toxsoft.uskat.alarms.s5.supports;

import java.io.Serializable;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.uskat.alarms.lib.ISkAlarmThreadHistoryItem;

/**
 * Реализация этапа истории обработки аларма.
 *
 * @author dima
 */
@Entity
@Table( name = "S5AlarmThreadHistoryItemEntity" )
public class S5AlarmThreadHistoryItemEntity
    implements ISkAlarmThreadHistoryItem, Serializable {

  private static final long serialVersionUID = 157157L;

  @Id
  @GeneratedValue
  @Column( name = "item_id" )
  private Long itemId;

  /**
   * Метка времени этапа истории
   */
  @Column( name = "timestamp" )
  private Long timestamp;

  /**
   * идентификатор (ИД-путь) нитки извещения
   */
  @Column( name = "thread_Id" )
  private String threadId;

  /**
   * Параметры выполнения этапа.
   */
  @Column( name = "params" )
  private String params;

  /**
   * Создает элемент истории обработки аларма.
   *
   * @param aTimestamp Long - метка времени
   * @param aThreadId идентификатор (ИД-путь) нитки извещения
   * @param aParams параметры - параметры выполнения этапа
   */
  public S5AlarmThreadHistoryItemEntity( Long aTimestamp, String aThreadId, IOptionSet aParams ) {
    timestamp = aTimestamp;
    threadId = aThreadId;
    params = OptionSetKeeper.KEEPER.ent2str( aParams );
  }

  /**
   * Конструктор без параметров
   */
  protected S5AlarmThreadHistoryItemEntity() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarmThreadHistoryItem
  //

  /**
   * Создает элемент истории обработки аларма.
   *
   * @param aItem легковесная сущность
   */
  public S5AlarmThreadHistoryItemEntity( ISkAlarmThreadHistoryItem aItem ) {
    timestamp = Long.valueOf( aItem.timestamp() );
    threadId = aItem.announceThreadId();
    params = OptionSetKeeper.KEEPER.ent2str( aItem.params() );
  }

  @Override
  public long timestamp() {
    return timestamp.longValue();
  }

  @Override
  public String announceThreadId() {
    return threadId;
  }

  @Override
  public IOptionSet params() {
    IOptionSet result = OptionSetKeeper.KEEPER.str2ent( params );
    return result;
  }
}
