package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.io.*;
import java.sql.*;

import javax.persistence.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;

/**
 * Реализация интерфейса {@link IDtoObject} способная маппироваться на таблицу базы данных
 *
 * @author mvk
 */
@MappedSuperclass
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
public class S5ObjectEntity
    implements IDtoObject, Serializable {

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
   * Поле таблицы: значения склепок объекта в строковом формате
   */
  protected static final String FIELD_RIVERTS_STRING = "rivertsString"; //$NON-NLS-1$

  /**
   * Поле таблицы: значения обратных склепок объектов на целевой объект в строковом формате
   */
  protected static final String FIELD_RIVET_REVS_STRING = "rivetRevsString"; //$NON-NLS-1$

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
  @Column( name = FIELD_ATTRS_STRING, //
      nullable = false,
      insertable = true,
      updatable = true,
      unique = false,
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String attrsString;

  /**
   * Значения всех склепок.
   */
  @Column( name = FIELD_RIVERTS_STRING, //
      nullable = false,
      insertable = true,
      updatable = true,
      unique = false,
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String rivetsString;

  /**
   * Значения всех обратных склепок .
   */
  @Column( name = FIELD_RIVET_REVS_STRING, //
      nullable = false,
      insertable = true,
      updatable = true,
      unique = false,
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String rivetRevsString;

  /**
   * Lazy
   */
  private transient Skid                     skid;
  private transient IOptionSet               attrs;
  private transient IMappedSkids             rivets;
  private transient IStringMap<IMappedSkids> rivetRevs;
  private transient ILogger                  logger;

  /**
   * Конструктор копирования (для сохранения объекта в базу данных)
   *
   * @param aSource {@link IDtoObject} исходные данные объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5ObjectEntity( IDtoObject aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    id = new S5ObjectID( aSource.skid() );
    classInfo = S5ClassEntity.createPrimaryKey( aSource.classId() );
    setAttrs( aSource.attrs() );
    setRivets( aSource.rivets() );
    setRivetRevs( aSource.rivetRevs() );
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
      rivetsString = aResultSet.getString( FIELD_RIVERTS_STRING );
      rivetRevsString = aResultSet.getString( FIELD_RIVET_REVS_STRING );
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
    setAttrs( IOptionSet.NULL );
    setRivets( IMappedSkids.EMPTY );
    setRivetRevs( IStringMap.EMPTY );
  }

  /**
   * Конструктор по умолчанию для JPA (PojoInstantiator)
   */
  protected S5ObjectEntity() {
    id = null;
    classInfo = null;
    setAttrs( IOptionSet.NULL );
    setRivets( IMappedSkids.EMPTY );
    setRivetRevs( IStringMap.EMPTY );
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    if( logger == null ) {
      logger = LoggerWrapper.getLogger( getClass() );
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

  /**
   * Установить значение всех склепок
   *
   * @param aRivets {@link IOptionSet} карта атрибутов и их значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setRivets( IMappedSkids aRivets ) {
    TsNullArgumentRtException.checkNull( aRivets );
    try {
      rivetsString = MappedSkids.KEEPER.ent2str( aRivets );
    }
    catch( Throwable e ) {
      logger().error( e, "setRivets(...). cause: %s", cause( e ) ); //$NON-NLS-1$
      throw e;
    }
    rivets = null;
  }

  /**
   * Установить значение всех обратных склепок
   *
   * @param aRivetRevs {@link IStringMap}&lt;{@link MappedSkids};&gt; SKIDs map of reverse rivets where: <br>
   *          - {@link IStringMap} key is "rivet class ID";<br>
   *          - {@link IMappedSkids} key is "rivet ID";<br>
   *          - {@link IMappedSkids} values are "SKIDs list of the left objects which have this object riveted".
   * @throws TsNullArgumentRtException аргумент = null
   */
  @SuppressWarnings( "boxing" )
  void setRivetRevs( IStringMap<IMappedSkids> aRivetRevs ) {
    TsNullArgumentRtException.checkNull( aRivetRevs );
    try {
      StringBuilder sb = new StringBuilder();
      ICharOutputStream chOut = new CharOutputStreamAppendable( sb );
      IStrioWriter sw = new StrioWriter( chOut );
      StrioUtils.writeStringMap( sw, EMPTY_STRING, aRivetRevs, MappedSkids.KEEPER, true );
      String newRivetRevsString = sb.toString();
      if( newRivetRevsString.length() > IS5ImplementConstants.LOB_TEXT_TYPE_MAX_SIZE ) {
        // Data too long for column rivetRevsString
        throw new TsIllegalArgumentRtException( ERR_RIVET_REVS_TOO_LONG, id, newRivetRevsString.length(),
            IS5ImplementConstants.LOB_TEXT_TYPE_MAX_SIZE, newRivetRevsString );
      }
      rivetRevsString = newRivetRevsString;
    }
    catch( Throwable e ) {
      logger().error( e, "setRivetRevs(...). cause: %s", cause( e ) ); //$NON-NLS-1$
      throw e;
    }
    rivetRevs = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDtoObject
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

  @Override
  public IMappedSkids rivets() {
    if( rivets == null ) {
      rivets = MappedSkids.KEEPER.str2ent( rivetsString );
    }
    return rivets;
  }

  @Override
  public IStringMap<IMappedSkids> rivetRevs() {
    if( rivetRevs == null ) {
      ICharInputStream chIn = new CharInputStreamString( rivetRevsString );
      IStrioReader sr = new StrioReader( chIn );
      rivetRevs = StrioUtils.readStringMap( sr, EMPTY_STRING, MappedSkids.KEEPER );
    }
    return rivetRevs;
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
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + id.hashCode();
    result = PRIME * result + attrs().hashCode();
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
