package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.ResultSet;

import javax.persistence.*;

import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;

/**
 * Реализация интерфейса {@link IDtoLinkFwd} способная маппироваться на таблицу базы данных
 *
 * @author mvk
 */
@MappedSuperclass
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
public abstract class S5LinkFwdEntity
    implements IDtoLinkFwd, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Несуществующая связь
   */
  public static final IDtoLinkFwd NULL = new InternalNulLinkFwd();

  /**
   * Поле таблицы: первичный составной (classId,strid,linkClassId,linkId) {@link S5LinkID} идентификатор связи
   */
  protected static final String FIELD_ID = "id"; //$NON-NLS-1$

  /**
   * Поле таблицы: список идентификаторов правых объектов связи в текстовом виде
   */
  public static final String FIELD_RIGHT_SKIDS_STRING = "rightSkidsString"; //$NON-NLS-1$

  /**
   * Первичный составной (classId,strid,linkClassId,linkId) ключ
   */
  @EmbeddedId
  private S5LinkID id;

  // /**
  // * Левый объект связи (определяется в наследнике)
  // */
  // @ManyToOne( targetEntity = S5DefaultObjectEntity.class, optional = false, fetch = FetchType.LAZY )
  // @JoinColumns(
  // value = {
  // @JoinColumn( name = FIELD_CLASSID, //
  // insertable = false,
  // updatable = false,
  // nullable = false ),
  // @JoinColumn( name = FIELD_STRID, //
  // insertable = false,
  // updatable = false,
  // nullable = false ) },
  // foreignKey = @ForeignKey( name = "S5DefaultLinkFwdEntity_ManyToOne_S5DefaultObjectEntity" ) )
  // protected S5ObjectEntity leftObj;

  /**
   * Список правых объектов связи в строковом виде.
   */
  @Lob
  @Column( name = FIELD_RIGHT_SKIDS_STRING, //
      nullable = false,
      insertable = true,
      updatable = true,
      unique = false )
  private String rightSkidsString;

  /**
   * Lazy
   */
  private transient Skid      skid;
  private transient ISkidList rightSkids;
  private transient Gwid      gwid = null;

  /**
   * Конструктор копирования (для сохранения связи объекта в базу данных)
   *
   * @param aSource {@link IDtoLinkFwd} исходная связь с объектами
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5LinkFwdEntity( IDtoLinkFwd aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    id = new S5LinkID( aSource.leftSkid(), aSource.classId(), aSource.linkId() );
    setRightSkids( aSource.rightSkids() );
  }

  /**
   * Конструктор связи объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5LinkFwdEntity( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      id = new S5LinkID( aResultSet );
      rightSkidsString = aResultSet.getString( FIELD_RIGHT_SKIDS_STRING );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка чтения данных jdbc-курсора
      throw new TsInternalErrorRtException( e, ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  protected S5LinkFwdEntity() {
    rightSkidsString = TsLibUtils.EMPTY_STRING;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Установить список правых объектов связи
   *
   * @param aRightSkids {@link ISkidList} список идентификаторов правых объектов связи
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setRightSkids( ISkidList aRightSkids ) {
    TsNullArgumentRtException.checkNull( aRightSkids );
    rightSkidsString = SkidListKeeper.KEEPER.ent2str( aRightSkids );
    rightSkids = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDtoLinkFwd
  //
  @Override
  public Gwid gwid() {
    if( gwid == null ) {
      gwid = Gwid.createLink( classId(), leftSkid().strid(), linkId() );
    }
    return gwid;
  }

  @Override
  public String classId() {
    return id.linkClassId();
  }

  @Override
  public String linkId() {
    return id.linkId();
  }

  @Override
  public Skid leftSkid() {
    if( skid == null ) {
      skid = new Skid( id.classId(), id.strid() );
    }
    return skid;
  }

  @Override
  public ISkidList rightSkids() {
    if( rightSkids == null ) {
      rightSkids = SkidListKeeper.KEEPER.str2ent( rightSkidsString );
    }
    return rightSkids;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof S5LinkFwdEntity that ) {
      return id.equals( that.id ) && rightSkids().equals( that.rightSkids() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id.hashCode();
    result = TsLibUtils.PRIME * result + rightSkids().hashCode();
    return result;
  }
}

/**
 * Реализация несуществующего описания соединения {@link S5LinkFwdEntity#NULL}.
 */
class InternalNulLinkFwd
    implements IDtoLinkFwd, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link S5LinkFwdEntity#NULL}.
   *
   * @return Object объект {@link S5LinkFwdEntity#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return S5LinkFwdEntity.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов IDtoLinkFwd
  //
  @Override
  public String classId() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public String linkId() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public Skid leftSkid() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public Gwid gwid() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public ISkidList rightSkids() {
    throw new TsNullObjectErrorRtException();
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //
  @Override
  public int hashCode() {
    return TsLibUtils.INITIAL_HASH_CODE;
  }

  @Override
  public boolean equals( Object obj ) {
    return obj == this;
  }

  @Override
  public String toString() {
    return IDtoLinkFwd.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
