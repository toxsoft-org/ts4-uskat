package org.toxsoft.uskat.sysext.alarms.supports;

import java.io.Serializable;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.uskat.sysext.alarms.api.ISkAnnounceThreadHistoryItem;

/**
 * Реализация этапа истории обработки аларма.
 *
 * @author dima
 */
@Entity
@Table( name = "SkAnnounceThreadHistoryItemEntity" )
public class SkAnnounceThreadHistoryItemEntity
    implements ISkAnnounceThreadHistoryItem, Serializable {

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
  public SkAnnounceThreadHistoryItemEntity( Long aTimestamp, String aThreadId, IOptionSet aParams ) {
    timestamp = aTimestamp;
    threadId = aThreadId;
    params = OptionSetKeeper.KEEPER.ent2str( aParams );
  }

  /**
   * Конструктор без параметров
   */
  protected SkAnnounceThreadHistoryItemEntity() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAnnounceThreadHistoryItem
  //

  /**
   * Создает элемент истории обработки аларма.
   *
   * @param aItem легковесная сущность
   */
  public SkAnnounceThreadHistoryItemEntity( ISkAnnounceThreadHistoryItem aItem ) {
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
