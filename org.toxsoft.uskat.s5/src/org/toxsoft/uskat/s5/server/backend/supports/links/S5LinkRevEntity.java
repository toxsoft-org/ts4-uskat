package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;

import java.io.Serializable;
import java.sql.ResultSet;

import javax.persistence.*;

import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkRev;

/**
 * Реализация интерфейса {@link IDtoLinkRev} способная маппироваться на таблицу базы данных
 *
 * @author mvk
 */
@MappedSuperclass
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
public abstract class S5LinkRevEntity
    implements IDtoLinkRev, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: список идентификаторов левых объектов связи в текстовом виде
   */
  public static final String FIELD_LEFT_SKIDS_STRING = "leftSkidsString"; //$NON-NLS-1$

  /**
   * Первичный составной (classId,strid,linkClassId,linkId) ключ
   */
  @EmbeddedId
  private S5LinkID id;

  // /**
  // * Правый объект связи (определяется в наследнике)
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
  // foreignKey = @ForeignKey( name = "S5DefaultLinkRevEntity_ManyToOne_S5DefaultObjectEntity" ) )
  // protected S5ObjectEntity rightObj;

  /**
   * Список левых объектов в связи в строковом виде.
   */
  @Lob
  @Column( name = FIELD_LEFT_SKIDS_STRING, //
      nullable = false,
      insertable = true,
      updatable = true,
      unique = false )
  private String leftSkidsString;

  /**
   * Lazy
   */
  private transient Skid      skid;
  private transient ISkidList leftSkids;
  private transient Gwid      gwid = null;

  /**
   * Конструктор с заданными параметрами (для сохранения связи объекта в базу данных)
   *
   * @param aRightSkid {@link Skid} идентификатор правого объекта связи
   * @param aLinkClassId String идентификатор класса в котором определена связь
   * @param aLinkId String строковый идентификатор связи
   * @param aLeftSkids {@link ISkidList} список идентификаторов левых объектов связи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5LinkRevEntity( Skid aRightSkid, String aLinkClassId, String aLinkId, ISkidList aLeftSkids ) {
    TsNullArgumentRtException.checkNulls( aRightSkid, aLinkClassId, aLinkId, aLeftSkids );
    id = new S5LinkID( aRightSkid, aLinkClassId, aLinkId );
    setLeftSkids( aLeftSkids );
  }

  /**
   * Конструктор копирования (для сохранения связи объекта в базу данных)
   *
   * @param aSource {@link IDtoLinkRev} исходная связь с объектами
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5LinkRevEntity( IDtoLinkRev aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    id = new S5LinkID( aSource.rightSkid(), aSource.classId(), aSource.linkId() );
    setLeftSkids( aSource.leftSkids() );
  }

  /**
   * Конструктор связи объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5LinkRevEntity( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      id = new S5LinkID( aResultSet );
      leftSkidsString = aResultSet.getString( FIELD_LEFT_SKIDS_STRING );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка чтения данных jdbc-курсора
      throw new TsInternalErrorRtException( e, MSG_ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  protected S5LinkRevEntity() {
    leftSkidsString = TsLibUtils.EMPTY_STRING;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Установить список левых объектов связи
   *
   * @param aLeftSkids {@link ISkidList} список идентификаторов левых объектов связи
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setLeftSkids( ISkidList aLeftSkids ) {
    TsNullArgumentRtException.checkNull( aLeftSkids );
    leftSkidsString = SkidListKeeper.KEEPER.ent2str( aLeftSkids );
    leftSkids = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDtoLinkRev
  //
  @Override
  public Gwid gwid() {
    if( gwid == null ) {
      gwid = Gwid.createLink( classId(), rightSkid().strid(), linkId() );
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
  public Skid rightSkid() {
    if( skid == null ) {
      skid = new Skid( id.classId(), id.strid() );
    }
    return skid;
  }

  @Override
  public ISkidList leftSkids() {
    if( leftSkids == null ) {
      leftSkids = SkidListKeeper.KEEPER.str2ent( leftSkidsString );
    }
    return leftSkids;
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
    if( aThat instanceof S5LinkRevEntity that ) {
      return id.equals( that.id ) && leftSkids().equals( that.leftSkids() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id.hashCode();
    result = TsLibUtils.PRIME * result + leftSkids().hashCode();
    return result;
  }
}
