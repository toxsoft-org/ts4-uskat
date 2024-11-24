package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;

import java.io.*;
import java.sql.*;

import javax.persistence.*;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Первичный составной ключ объекта
 *
 * @author mvk
 */
/**
 * @author mvk
 */
@Embeddable
public class S5ObjectID
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: идентификатор класса объекта
   */
  public static final String FIELD_CLASSID = "classId"; //$NON-NLS-1$

  /**
   * Поле таблицы: строковый идентификатор объекта, уникальный в классе
   */
  public static final String FIELD_STRID = "strid"; //$NON-NLS-1$

  /**
   * Строковый идентификатор класса объекта
   */
  @Column( name = FIELD_CLASSID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ")" )
  private String classId;

  /**
   * Строковый идентификатор объекта уникальный в классе
   */
  @Column( name = FIELD_STRID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ")" )
  private String strid;

  private transient int hashCode = 0;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aSkid {@link Skid} идентификатор объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ObjectID( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    classId = aSkid.classId();
    strid = aSkid.strid();
  }

  /**
   * Конструктор идентификатора из текущей записи курсора jdbc (чтение идентификатора из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5ObjectID( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      classId = aResultSet.getString( FIELD_CLASSID );
      strid = aResultSet.getString( FIELD_STRID );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка чтения данных jdbc-курсора
      throw new TsInternalErrorRtException( e, ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5ObjectID() {
    classId = TsLibUtils.EMPTY_STRING;
    strid = TsLibUtils.EMPTY_STRING;
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает идентификатор класса объекта
   *
   * @return String идентификатор класса
   */
  String classId() {
    return classId;
  }

  /**
   * Возвращает строковый идентификатор объекта
   *
   * @return String идентификатор объекта
   */
  String strid() {
    return strid;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //
  @Override
  public String toString() {
    return classId + '[' + strid + ']';
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof S5ObjectID that ) {
      return this.classId.equals( that.classId ) && this.strid.equals( that.strid );
    }
    return false;
  }

  @Override
  public int hashCode() {
    if( hashCode == 0 ) {
      int result = TsLibUtils.INITIAL_HASH_CODE;
      result = TsLibUtils.PRIME * result + classId.hashCode();
      result = TsLibUtils.PRIME * result + strid.hashCode();
      hashCode = result;
    }
    return hashCode;
  }

}
