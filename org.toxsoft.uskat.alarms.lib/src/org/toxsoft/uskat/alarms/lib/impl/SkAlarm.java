package org.toxsoft.uskat.alarms.lib.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.alarms.lib.EAlarmPriority;
import org.toxsoft.uskat.alarms.lib.ISkAlarm;

/**
 * Неизменяемая реализация {@link ISkAlarm}.
 *
 * @author goga, dima
 */
public final class SkAlarm
    implements ISkAlarm, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SkAlarm"; //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<ISkAlarm> KEEPER =
      new AbstractEntityKeeper<>( ISkAlarm.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkAlarm aEntity ) {
          aSw.writeTimestamp( aEntity.timestamp() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.alarmId() );
          aSw.writeSeparatorChar();
          Skid.KEEPER.write( aSw, aEntity.authorId() );
          aSw.writeSeparatorChar();
          Skid.KEEPER.write( aSw, aEntity.userId() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.priority().id() );
          aSw.writeSeparatorChar();
          aSw.writeInt( aEntity.sublevel() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.alarmDefId() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.message() );
        }

        @Override
        protected ISkAlarm doRead( IStrioReader aSr ) {
          long timestamp = aSr.readTimestamp();
          aSr.ensureSeparatorChar();
          long alarmId = aSr.readLong();
          aSr.ensureSeparatorChar();
          Skid authorId = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          Skid userId = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          EAlarmPriority priority = EAlarmPriority.findById( aSr.readQuotedString() );
          aSr.ensureSeparatorChar();
          byte sublevel = (byte)aSr.readInt();
          aSr.ensureSeparatorChar();
          String alarmDefId = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          String message = aSr.readQuotedString();

          return new SkAlarm( timestamp, alarmId, priority, sublevel, authorId, userId, alarmDefId, message );
        }

      };

  private final long           timestamp;
  private final long           alarmId;
  private final Skid           authorId;
  private final Skid           userId;
  private final EAlarmPriority priority;
  private final byte           sublevel;
  private final String         alarmDefId;
  private final String         message;

  /**
   * Конструктор.
   *
   * @param aTimestamp long - момент времени, когда была сгенерирована тревога
   * @param aAlarmId long - идентификатор тревоги
   * @param aPriority {@link EAlarmPriority} - важность
   * @param aSublevel byte - уточнение степени важности
   * @param aAuthorId {@link Skid} - идентификатор объекта - автора тревоги
   * @param aUserId {@link Skid} - идентификатор объекта пользователя
   * @param aAlaremDefId String - идентификатор типа тревоги
   * @param aMessage String - текстовое сообщение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aAlaremDefId не ИД-путь
   */
  public SkAlarm( long aTimestamp, long aAlarmId, EAlarmPriority aPriority, byte aSublevel, Skid aAuthorId,
      Skid aUserId, String aAlaremDefId, String aMessage ) {
    TsNullArgumentRtException.checkNulls( aPriority, aMessage );
    StridUtils.checkValidIdPath( aAlaremDefId );
    timestamp = aTimestamp;
    alarmId = aAlarmId;
    authorId = aAuthorId;
    priority = aPriority;
    sublevel = aSublevel;
    alarmDefId = aAlaremDefId;
    userId = aUserId;
    message = aMessage;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarm
  //

  @Override
  public long timestamp() {
    return timestamp;
  }

  @Override
  public long alarmId() {
    return alarmId;
  }

  @Override
  public EAlarmPriority priority() {
    return priority;
  }

  @Override
  public byte sublevel() {
    return sublevel;
  }

  @Override
  public int level() {
    return priority.sublevelBase() + sublevel;
  }

  @Override
  public Skid authorId() {
    return authorId;
  }

  @Override
  public String alarmDefId() {
    return alarmDefId;
  }

  @Override
  public Skid userId() {
    return userId;
  }

  @Override
  public String message() {
    return message;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса Comparable
  //

  @Override
  public int compareTo( ISkAlarm aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    int c = Long.compare( timestamp, aThat.timestamp() );
    if( c != 0 ) {
      return c;
    }
    return -Integer.compare( level(), aThat.level() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return TimeUtils.timestampToString( timestamp ) + " " + priority.id() + ": " + message;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof ISkAlarm that ) {
      return timestamp == that.timestamp() && alarmId == that.alarmId() && authorId == that.authorId()
          && userId == that.userId() && sublevel == that.sublevel() && priority == that.priority()
          && alarmDefId.equals( that.alarmDefId() ) && message.equals( that.message() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + (int)(timestamp ^ (timestamp >>> 32));
    result = TsLibUtils.PRIME * result + (int)(alarmId ^ (alarmId >>> 32));
    result = TsLibUtils.PRIME * result + priority.hashCode();
    result = TsLibUtils.PRIME * result + sublevel;
    result = TsLibUtils.PRIME * result + authorId.hashCode();
    result = TsLibUtils.PRIME * result + userId.hashCode();
    result = TsLibUtils.PRIME * result + alarmDefId.hashCode();
    result = TsLibUtils.PRIME * result + message.hashCode();
    return result;
  }

}
