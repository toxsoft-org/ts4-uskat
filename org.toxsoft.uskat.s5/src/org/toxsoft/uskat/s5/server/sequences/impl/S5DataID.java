package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;

import java.io.Serializable;
import java.sql.ResultSet;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Первичный составной ключ данного реального времени
 *
 * @author mvk
 */
/**
 * @author mvk
 */
@Embeddable
public class S5DataID
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: НЕабстрактный {@link Gwid}-идентификатор в строковом формате
   */
  public static final String FIELD_GWID = "gwid"; //$NON-NLS-1$

  /**
   * Поле таблицы: время (мсек с начала эпохи) начала данных (включительно)
   */
  public static final String FIELD_START_TIME = "startTime"; //$NON-NLS-1$

  /**
   * НЕабстрактный {@link Gwid}-идентификатор в строковом формате
   */
  @Column( name = FIELD_GWID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin" )
  private String gwidString;

  /**
   * Время (мсек с начала эпохи) начала данных (включительно)
   */
  @Column( name = FIELD_START_TIME, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false )
  private Long startTime;

  private transient Gwid gwid     = null;
  private transient int  hashCode = 0;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long время (мсек с начала эпохи) начала данных (включительно)
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException gwid идентификатор не может быть абстрактным (без объекта)
   */
  S5DataID( Gwid aGwid, long aStartTime ) {
    TsNullArgumentRtException.checkNull( aGwid );
    TsIllegalArgumentRtException.checkTrue( aGwid.isAbstract() );
    gwidString = Gwid.KEEPER.ent2str( aGwid );
    startTime = Long.valueOf( aStartTime );
  }

  /**
   * Конструктор идентификатора из текущей записи курсора jdbc (чтение идентификатора из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5DataID( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      gwidString = aResultSet.getString( FIELD_GWID );
      startTime = Long.valueOf( aResultSet.getLong( FIELD_START_TIME ) );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания идентификатора данного при чтении jdbc-курсора
      throw new TsInternalErrorRtException( e, ERR_READ_RTDATAID_JDBC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5DataID() {
    gwidString = TsLibUtils.EMPTY_STRING;
    startTime = null;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает абстрактный {@link Gwid}-идентификатор в строковом формате
   *
   * @return String идентификатор в строковом формате
   */
  public Gwid gwid() {
    if( gwid == null ) {
      gwid = Gwid.KEEPER.str2ent( gwidString );
    }
    return gwid;
  }

  /**
   * Возвращает время начала данных в блоке данных (включительно)
   *
   * @return Long время (мсек с начала эпохи)
   */
  public Long startTime() {
    return startTime;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //
  @Override
  public String toString() {
    return gwidString + '[' + TimeUtils.timestampToString( startTime.longValue() ) + ']';
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof S5DataID that ) {
      return this.gwidString.equals( that.gwidString ) && this.startTime.equals( that.startTime );
    }
    return false;
  }

  @Override
  public int hashCode() {
    if( hashCode == 0 ) {
      int result = TsLibUtils.INITIAL_HASH_CODE;
      result = TsLibUtils.PRIME * result + gwidString.hashCode();
      result = TsLibUtils.PRIME * result + (int)(startTime.longValue() ^ (startTime.longValue() >>> 32));
      hashCode = result;
    }
    return hashCode;
  }

}
