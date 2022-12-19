package org.toxsoft.uskat.s5.server.backend.supports.gwiddb;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.gwiddb.IS5Resources.*;

import java.io.Serializable;
import java.sql.ResultSet;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.bricks.strid.more.IdPair;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Первичный составной ключ {@link S5GwidDbEntity}.
 *
 * @author mvk
 */
/**
 * @author mvk
 */
@Embeddable
public class S5GwidDbID
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: идентификатор класса объекта
   */
  public static final String FIELD_SECTION = "section"; //$NON-NLS-1$

  /**
   * Поле таблицы: строковый идентификатор объекта, уникальный в классе
   */
  public static final String FIELD_GWID = "gwid"; //$NON-NLS-1$

  /**
   * Строковый идентификатор секции параметров
   */
  @Column( name = FIELD_SECTION, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin" )
  private String sectionId;

  /**
   * Строковый представление {@link Gwid}.
   */
  @Column( name = FIELD_GWID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin" )
  private String gwid;

  private transient int hashCode = 0;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aSectionId {@link IdChain} идентификатор секции
   * @param aGwid {@link Gwid} идентификатор парамтера
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5GwidDbID( IdChain aSectionId, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aSectionId, aGwid );
    sectionId = IdChain.KEEPER.ent2str( aSectionId );
    gwid = Gwid.KEEPER.ent2str( aGwid );
  }

  /**
   * Конструктор идентификатора из текущей записи курсора jdbc (чтение идентификатора из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5GwidDbID( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      sectionId = aResultSet.getString( FIELD_SECTION );
      gwid = aResultSet.getString( FIELD_GWID );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка чтения данных jdbc-курсора
      throw new TsInternalErrorRtException( e, ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5GwidDbID() {
    sectionId = TsLibUtils.EMPTY_STRING;
    gwid = TsLibUtils.EMPTY_STRING;
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает идентификатор класса объекта
   *
   * @return String идентификатор класса
   */
  IdPair sectionId() {
    TsInternalErrorRtException.checkTrue( sectionId == null || sectionId.length() == 0 );
    return IdPair.KEEPER.str2ent( sectionId );
  }

  /**
   * Возвращает строковый идентификатор объекта
   *
   * @return String идентификатор объекта
   */
  Gwid gwid() {
    TsInternalErrorRtException.checkTrue( gwid == null || gwid.length() == 0 );
    return Gwid.KEEPER.str2ent( gwid );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //
  @Override
  public String toString() {
    return sectionId + '[' + gwid + ']';
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof S5GwidDbID that ) {
      return this.sectionId.equals( that.sectionId ) && this.gwid.equals( that.gwid );
    }
    return false;
  }

  @Override
  public int hashCode() {
    if( hashCode == 0 ) {
      int result = TsLibUtils.INITIAL_HASH_CODE;
      result = TsLibUtils.PRIME * result + sectionId.hashCode();
      result = TsLibUtils.PRIME * result + gwid.hashCode();
      hashCode = result;
    }
    return hashCode;
  }

}
