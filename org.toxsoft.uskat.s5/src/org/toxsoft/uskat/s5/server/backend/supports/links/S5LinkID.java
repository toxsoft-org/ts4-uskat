package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.io.Serializable;
import java.sql.ResultSet;

import javax.enterprise.concurrent.SkippedException;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Первичный составной ключ связи объекта
 *
 * @author mvk
 */
/**
 * @author mvk
 */
@Embeddable
class S5LinkID
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: строковый идентификатор класса в котором определена связь объекта
   */
  protected static final String FIELD_LINK_CLASSID = "linkClassId"; //$NON-NLS-1$

  /**
   * Поле таблицы: строковый идентификатор связи объекта
   */
  protected static final String FIELD_LINKID = "linkId"; //$NON-NLS-1$

  /**
   * Строковый идентификатор класса объекта
   */
  @Column( name = FIELD_CLASSID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin" )
  private String classId;

  /**
   * Строковый идентификатор объекта уникальный в классе
   */
  @Column( name = FIELD_STRID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin" )
  private String strid;

  /**
   * Строковый идентификатор класса в котором определена связь
   */
  @Column( name = FIELD_LINK_CLASSID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin" )
  private String linkClassId;

  /**
   * Строковый идентификатор связи объекта уникальный в классе
   */
  @Column( name = FIELD_LINKID, //
      nullable = false,
      insertable = true,
      updatable = false,
      unique = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin" )
  private String linkId;

  private transient int hashCode = 0;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aSkid {@link SkippedException}
   * @param aLinkClassId String идентификатор класса в котором определена связь
   * @param aLinkId String текстовый идентификатор связи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5LinkID( Skid aSkid, String aLinkClassId, String aLinkId ) {
    TsNullArgumentRtException.checkNulls( aSkid, aLinkClassId, aLinkClassId );
    classId = aSkid.classId();
    strid = aSkid.strid();
    linkClassId = aLinkClassId;
    linkId = aLinkId;
  }

  /**
   * Конструктор идентификатора из текущей записи курсора jdbc (чтение идентификатора из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5LinkID( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      classId = aResultSet.getString( FIELD_CLASSID );
      strid = aResultSet.getString( FIELD_STRID );
      linkClassId = aResultSet.getString( FIELD_LINK_CLASSID );
      linkId = aResultSet.getString( FIELD_LINKID );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка чтения данных jdbc-курсора
      throw new TsInternalErrorRtException( e, ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  S5LinkID() {
    classId = TsLibUtils.EMPTY_STRING;
    strid = TsLibUtils.EMPTY_STRING;
    linkClassId = TsLibUtils.EMPTY_STRING;
    linkId = TsLibUtils.EMPTY_STRING;
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

  /**
   * Возвращает строковый идентификатор класса в котором определена связь
   *
   * @return String идентификатор класса
   */
  String linkClassId() {
    return linkClassId;
  }

  /**
   * Возвращает строковый идентификатор связи
   *
   * @return String идентификатор связи
   */
  String linkId() {
    return linkId;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //
  @Override
  public String toString() {
    return classId + '[' + strid + ']' + '.' + linkClassId + '(' + linkId + ')';
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof S5LinkID that ) {
      return this.classId.equals( that.classId ) && this.strid.equals( that.strid )
          && this.linkClassId.equals( that.linkClassId ) && this.linkId.equals( that.linkId );
    }
    return false;
  }

  @Override
  public int hashCode() {
    if( hashCode == 0 ) {
      int result = TsLibUtils.INITIAL_HASH_CODE;
      result = TsLibUtils.PRIME * result + classId.hashCode();
      result = TsLibUtils.PRIME * result + strid.hashCode();
      result = TsLibUtils.PRIME * result + linkClassId.hashCode();
      result = TsLibUtils.PRIME * result + linkId.hashCode();
      hashCode = result;
    }
    return hashCode;
  }

}
