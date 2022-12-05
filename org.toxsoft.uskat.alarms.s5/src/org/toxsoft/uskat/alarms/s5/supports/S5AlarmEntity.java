package org.toxsoft.uskat.alarms.s5.supports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.time.ITimedListEdit;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
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
class S5AlarmEntity
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

  @OneToOne( fetch = FetchType.EAGER, cascade = CascadeType.PERSIST )
  @JoinColumn( name = "flacon_id" )
  private S5AlarmFlaconEntity flacon;

  @OneToMany( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
  @JoinColumn( name = "alarm_id" )
  private Collection<S5AlarmThreadHistoryItemEntity> historyItems = new ArrayList<>();

  /**
   * Конструктор без параметров
   */
  protected S5AlarmEntity() {
    // nop
  }

  /**
   * Конструктор.
   *
   * @param aTimestamp long - момент времени, когда была сгенерирована тревога
   * @param aAlarmId long - идентификатор тревоги
   * @param aPriority {@link EAlarmPriority} - важность
   * @param aSublevel byte - уточнение степени важности
   * @param aAuthorId {@link Skid} - идентификатор объекта - автора тревоги
   * @param aUserId {@link Skid} - идентификатор объекта пользователя
   * @param aAlarmDefId String - идентификатор типа тревоги
   * @param aMessage String - текстовое сообщение
   * @param aFlacon {@link S5AlarmFlaconEntity} срез данных вызвавший аларм
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aAlaremDefId не ИД-путь
   */
  S5AlarmEntity( long aTimestamp, long aAlarmId, EAlarmPriority aPriority, byte aSublevel, Skid aAuthorId, Skid aUserId,
      String aAlarmDefId, String aMessage, S5AlarmFlaconEntity aFlacon ) {
    TsNullArgumentRtException.checkNulls( aAuthorId, aUserId, aAlarmDefId, aMessage, aFlacon );
    timestamp = aTimestamp;
    alarmId = Long.valueOf( aAlarmId );
    alarmDefId = aAlarmDefId;
    priority = aPriority.id();
    sublevel = aSublevel;
    message = aMessage;
    authorIdString = Skid.KEEPER.ent2str( aAuthorId );
    userIdString = Skid.KEEPER.ent2str( aUserId );
    flacon = aFlacon;
  }

  /**
   * Добавить элеемнт в историю обработки аларма
   *
   * @param aItem элемент истории обработки аларма
   */
  public void addHistoryItem( ISkAlarmThreadHistoryItem aItem ) {
    historyItems.add( new S5AlarmThreadHistoryItemEntity( aItem ) );
  }

  // ------------------------------------------------------------------------------------
  // ISkAlarm
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

  @Override
  public ISkAlarmFlacon flacon() {
    return flacon;
  }

  @Override
  public ITimedListEdit<ISkAlarmThreadHistoryItem> history() {
    TimedList<ISkAlarmThreadHistoryItem> retVal = new TimedList<>();
    for( ISkAlarmThreadHistoryItem item : historyItems ) {
      retVal.add( new S5AlarmThreadHistoryItemEntity( item ) );
    }
    return retVal;
  }
}
