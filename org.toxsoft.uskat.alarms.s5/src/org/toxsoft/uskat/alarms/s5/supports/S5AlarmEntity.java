package org.toxsoft.uskat.alarms.s5.supports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.time.ITimedListEdit;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;

/**
 * Реализация объекта аларма.
 *
 * @author dima
 */
@NamedQueries( {

    @NamedQuery( name = "S5AlarmEntity.queryAlarms",
        query = "select alarm from S5AlarmEntity alarm where "
            + " ( :startTime <= alarm.timestamp and :endTime >= alarm.timestamp )" ),

} )
@Entity
public class S5AlarmEntity
    implements ISkAlarm, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор аларма.
   * <p>
   * Необходим для организации уникального ключа на который ссылается S5AlarmThreadHistoryItemEntity
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
  private Collection<S5AlarmThreadHistoryItemEntity> historyItems = new ArrayList<>();

  @OneToOne( fetch = FetchType.EAGER, cascade = CascadeType.PERSIST )
  @JoinColumn( name = "flacon_id" )
  private S5AlarmFlaconEntity flacon;

  /**
   * Конструктор без параметров
   */
  protected S5AlarmEntity() {
    // nop
  }

  /**
   * Конструктор по ISkAlarm
   *
   * @param aSkAlarm оригинальный аларм
   */
  public S5AlarmEntity( ISkAlarm aSkAlarm ) {
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
  public ITimedListEdit<ISkAlarmThreadHistoryItem> history() {
    // TODO пока просто в лоб
    TimedList<ISkAlarmThreadHistoryItem> retVal = new TimedList<>();
    for( ISkAlarmThreadHistoryItem item : historyItems ) {
      retVal.add( new S5AlarmThreadHistoryItemEntity( item ) );
    }
    return retVal;
  }

  /**
   * Добавить элеемнт в историю обработки аларма
   *
   * @param aItem элемент истории обработки аларма
   */
  public void addHistoryItem( ISkAlarmThreadHistoryItem aItem ) {
    historyItems.add( new S5AlarmThreadHistoryItemEntity( aItem ) );
  }

  /**
   * Установить флакон аларма
   *
   * @param aSkAlarmFlacon флакон
   */
  public void setFlacon( ISkAlarmFlacon aSkAlarmFlacon ) {
    flacon = new S5AlarmFlaconEntity( aSkAlarmFlacon );
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
