package org.toxsoft.uskat.alarms.s5.supports;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.io.Serializable;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.EAlarmPriority;
import org.toxsoft.uskat.alarms.lib.ISkAlarmDef;

/**
 * Реализация объекта описания аларма для хранения в БД.
 *
 * @author dima
 * @author mvk
 */
@NamedQueries( {
    @NamedQuery( name = S5AlarmEntitiesUtils.QUERY_GET, query = "SELECT alarmDef FROM S5AlarmDefEntity alarmDef" ), } )
@Entity
public class S5AlarmDefEntity
    implements ISkAlarmDef, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Формат вывода toString()
   */
  private static final String TO_STRING_FORMAT = "%s[%s]: %s"; //$NON-NLS-1$

  /**
   * Уникальный идентификатор описания в системе
   */
  @Id
  private String id;

  /**
   * отображаемое имя
   */
  @Column( nullable = true )
  private String nmName;
  /**
   * описание сущности
   */
  @Column( nullable = true )
  private String description;

  /**
   * приоритет обработки аларма
   */
  @Column( nullable = false )
  private String priority;

  /**
   * текст сообщения аларма
   */
  @Column( nullable = true )
  private String message;

  /**
   * Конструктор без параметров
   */
  protected S5AlarmDefEntity() {
    // nop
  }

  /**
   * Конструктор
   *
   * @param aId String идентификатор аларма
   * @param aMessage String текстовое сообщение для аларма
   */
  public S5AlarmDefEntity( String aId, String aMessage ) {
    TsNullArgumentRtException.checkNulls( aId, aMessage );
    id = StridUtils.checkValidIdPath( aId );
    setPriority( EAlarmPriority.NORMAL );
    setName( EMPTY_STRING );
    setDescription( EMPTY_STRING );
    setMessage( aMessage );
  }

  /**
   * Конструктор по aAlarmDef
   *
   * @param aSkAlarmDef оригинальный аларм
   */
  public S5AlarmDefEntity( ISkAlarmDef aSkAlarmDef ) {
    id = StridUtils.checkValidIdPath( aSkAlarmDef.id() );
    setPriority( aSkAlarmDef.priority() );
    setName( aSkAlarmDef.nmName() );
    setDescription( aSkAlarmDef.description() );
    setMessage( aSkAlarmDef.message() );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Установить имя аларма
   *
   * @param aName String имя аларма
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    nmName = aName;
  }

  /**
   * Установить описание аларма
   *
   * @param aDescription String описание аларма
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setDescription( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    description = aDescription;
  }

  /**
   * Установить имя аларма
   *
   * @param aPriority {@link EAlarmPriority} важность аларма
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setPriority( EAlarmPriority aPriority ) {
    TsNullArgumentRtException.checkNull( aPriority );
    priority = aPriority.id();
  }

  /**
   * Установить сообщение аларма
   *
   * @param aMessage String сообщение аларма
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setMessage( String aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    message = aMessage;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarmDef
  //

  @Override
  public EAlarmPriority priority() {
    return EAlarmPriority.findById( priority );
  }

  @Override
  public String message() {
    return message;
  }

  @Override
  public String nmName() {
    return nmName;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String description() {
    return description;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение Object
  //
  @Override
  public String toString() {
    return String.format( TO_STRING_FORMAT, id, nmName, message );
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    final int prime = PRIME;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( !(aObject instanceof ISkAlarmDef other) ) {
      return false;
    }
    if( !id.equals( other.id() ) ) {
      return false;
    }
    if( !nmName.equals( other.nmName() ) ) {
      return false;
    }
    if( !description.equals( other.description() ) ) {
      return false;
    }
    if( priority() != other.priority() ) {
      return false;
    }
    if( !message.equals( other.message() ) ) {
      return false;
    }
    return true;
  }

}
