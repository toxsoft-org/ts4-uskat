package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.io.Serializable;
import java.sql.ResultSet;

import javax.persistence.*;

import org.toxsoft.core.log4j.Logger;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassEntity;

import ru.uskat.common.dpu.IDpuObject;

/**
 * Реализация интерфейса {@link IDpuObject} способная маппироваться на таблицу базы данных
 *
 * @author mvk
 */
@MappedSuperclass
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
public class S5ObjectEntity
    implements IDpuObject, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя внешнего ключа: classId -> S5ClassEntity
   */
  // public static final String FK_CLASSID_TO_CLASS = "S5Object_classId_to_S5ClassEntity_fk"; //$NON-NLS-1$

  /**
   * Поле таблицы: значения атрибутов объекта в строковом формате
   */
  protected static final String FIELD_ATTRS_STRING = "attrsString"; //$NON-NLS-1$

  /**
   * Первичный составной (classId,strid) ключ
   */
  @EmbeddedId
  private S5ObjectID id;

  /**
   * Идентификатор класса объекта
   */
  @ManyToOne( targetEntity = S5ClassEntity.class, optional = false, fetch = FetchType.LAZY )
  @JoinColumn( name = FIELD_CLASSID, //
      insertable = false,
      updatable = false,
      nullable = false )
  private S5ClassEntity classInfo;

  /**
   * Значения всех расширенных атрибутов.
   */
  @Lob
  @Column( name = FIELD_ATTRS_STRING, //
      nullable = false,
      insertable = true,
      updatable = true,
      unique = false )
  private String attrsString;

  /**
   * Lazy
   */
  private transient Skid       skid;
  private transient IOptionSet attrs;
  private transient ILogger    logger;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aSource {@link IDpuObject} исходные данные объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5ObjectEntity( IDpuObject aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    id = new S5ObjectID( aSource.skid() );
    classInfo = S5ClassEntity.createPrimaryKey( aSource.classId() );
    setAttrs( aSource.attrs() );
  }

  /**
   * Конструктор объекта из текущей записи курсора jdbc (чтение объекта из базы данных)
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5ObjectEntity( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      id = new S5ObjectID( aResultSet );
      classInfo = null;
      attrsString = aResultSet.getString( FIELD_ATTRS_STRING );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка чтения данных jdbc-курсора
      throw new TsInternalErrorRtException( e, ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Конструктор по первичному ключу
   *
   * @param aId {@link S5ObjectID} первичный составной ключ
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5ObjectEntity( S5ObjectID aId ) {
    TsNullArgumentRtException.checkNull( aId );
    id = aId;
    classInfo = null;
    attrsString = TsLibUtils.EMPTY_STRING;
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  protected S5ObjectEntity() {
    id = null;
    classInfo = null;
    attrsString = TsLibUtils.EMPTY_STRING;
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    if( logger == null ) {
      logger = Logger.getLogger( getClass() );
    }
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Установить значение всех атрибутов
   *
   * @param aAttrs {@link IOptionSet} карта атрибутов и их значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setAttrs( IOptionSet aAttrs ) {
    TsNullArgumentRtException.checkNull( aAttrs );
    try {
      attrsString = OptionSetKeeper.KEEPER.ent2str( aAttrs );
    }
    catch( Throwable e ) {
      logger().error( e, "setAttrs(...). cause: %s", cause( e ) ); //$NON-NLS-1$
      throw e;
    }
    attrs = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDpuObject
  //
  @Override
  public Skid skid() {
    if( skid == null ) {
      skid = new Skid( id.classId(), id.strid() );
    }
    return skid;
  }

  @Override
  public IOptionSet attrs() {
    if( attrs == null ) {
      attrs = OptionSetKeeper.KEEPER.str2ent( attrsString );
    }
    return attrs;
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
    if( aThat instanceof S5ObjectEntity that ) {
      return id.equals( that.id ) && attrs().equals( that.attrs() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id.hashCode();
    result = TsLibUtils.PRIME * result + attrs().hashCode();
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Служебные методы
  //
  /**
   * Создание PRIMARY ключа на описание класса в базе данных
   * <p>
   * Ключ используется для организации связанности в базе данных, например, между таблицами {@link S5ObjectEntity} и
   * {@link S5ClassEntity}
   *
   * @param aSkid {@link Skid} идентификатор объекта
   * @return {@link S5ClassEntity} PRIMARY-ключ
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5ObjectEntity createPrimaryKey( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return new S5ObjectEntity( new S5ObjectID( aSkid ) );
  }
}
