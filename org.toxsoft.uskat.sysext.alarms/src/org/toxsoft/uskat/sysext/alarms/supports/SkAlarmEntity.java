package org.toxsoft.uskat.sysext.alarms.supports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.time.ITimedListEdit;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;

/**
 * Реализация объекта аларма.
 *
 * @author dima
 */
@NamedQueries( {

    @NamedQuery( name = "SkAlarmEntity.queryAlarms",
        query = "select alarm from SkAlarmEntity alarm where "
            + " ( :startTime <= alarm.timestamp and :endTime >= alarm.timestamp )" ),

} )
@Entity
public class SkAlarmEntity
    implements ISkAlarm, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор аларма.
   * <p>
   * Необходим для организации уникального ключа на который ссылается SkAnnounceThreadHistoryItemEntity
   */
  @Id
  @Column( name = "alarm_id" )
  private Long alarmId;

  /**
   * метка времени аларма
   */
  private long timestamp;

  /**
   * приоритет обработки аларма
   */
  private String priority;

  /**
   * уточнение приоритета обработки аларма
   */
  private byte sublevel;

  /**
   * текст сообщения аларма
   */
  private String message;

  /**
   * автор аларма
   */
  private String authorIdString;

  /**
   * пользователь системы создавший аларм
   */
  private String userIdString;

  /**
   * id описания аларма
   */
  private String alarmDefId;

  @OneToMany( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
  @JoinColumn( name = "alarm_id" )
  private Collection<SkAnnounceThreadHistoryItemEntity> historyItems = new ArrayList<>();

  @OneToOne( fetch = FetchType.EAGER, cascade = CascadeType.PERSIST )
  @JoinColumn( name = "flacon_id" )
  private SkFlaconEntity flacon;

  /**
   * Конструктор без параметров
   */
  protected SkAlarmEntity() {
    // nop
  }

  /**
   * Конструктор по ISkAlarm
   *
   * @param aSkAlarm оригинальный аларм
   */
  public SkAlarmEntity( ISkAlarm aSkAlarm ) {
    alarmId = Long.valueOf( aSkAlarm.alarmId() );
    timestamp = aSkAlarm.timestamp();
    alarmDefId = aSkAlarm.alarmDefId();
    priority = aSkAlarm.priority().id();
    sublevel = aSkAlarm.sublevel();
    message = aSkAlarm.message();
    authorIdString = Skid.KEEPER.ent2str( aSkAlarm.authorId() );
    userIdString = Skid.KEEPER.ent2str( aSkAlarm.userId() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarm
  //

  @Override
  public long timestamp() {
    return timestamp;
  }

  @Override
  public int compareTo( ISkAlarm aO ) {
    return Long.valueOf( alarmId.longValue() - aO.alarmId() ).intValue();
  }

  @Override
  public long alarmId() {
    return alarmId.longValue();
  }

  @Override
  public EAlarmPriority priority() {
    return EAlarmPriority.findById( priority );
  }

  @Override
  public byte sublevel() {
    return sublevel;
  }

  @Override
  public int level() {
    return priority().ordinal() + sublevel;
  }

  @Override
  public Skid authorId() {
    return Skid.KEEPER.str2ent( authorIdString );
  }

  @Override
  public Skid userId() {
    return Skid.KEEPER.str2ent( userIdString );
  }

  @Override
  public String alarmDefId() {
    return alarmDefId;
  }

  @Override
  public String message() {
    return message;
  }

  /**
   * Получить историю обработки аларма
   *
   * @return список элементов истории
   */
  public ITimedListEdit<ISkAnnounceThreadHistoryItem> history() {
    // TODO пока просто в лоб
    TimedList<ISkAnnounceThreadHistoryItem> retVal = new TimedList<>();
    for( ISkAnnounceThreadHistoryItem item : historyItems ) {
      retVal.add( new SkAnnounceThreadHistoryItemEntity( item ) );
    }
    return retVal;
  }

  /**
   * Добавить элеемнт в историю обработки аларма
   *
   * @param aItem элемент истории обработки аларма
   */
  public void addHistoryItem( ISkAnnounceThreadHistoryItem aItem ) {
    historyItems.add( new SkAnnounceThreadHistoryItemEntity( aItem ) );
  }

  /**
   * Установить флакон аларма
   *
   * @param aSkAlarmFlacon флакон
   */
  public void setFlacon( ISkAlarmFlacon aSkAlarmFlacon ) {
    flacon = new SkFlaconEntity( aSkAlarmFlacon );
  }

  /**
   * Получить флакон аларма
   *
   * @return ISkAlarmFlacon флакон
   */
  public ISkAlarmFlacon skAlarmFlacon() {
    return flacon;
  }
}
