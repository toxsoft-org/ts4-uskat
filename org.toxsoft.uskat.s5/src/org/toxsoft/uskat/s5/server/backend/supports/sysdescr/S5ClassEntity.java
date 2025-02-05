package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassesSQL.*;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;

/**
 * Реализация интерфейса {@link IDtoClassInfo} способная маппироваться на таблицу базы данных
 *
 * @author mvk
 */
@NamedQueries( { @NamedQuery( name = QUERY_NAME_GET_CLASSES, query = QUERY_GET_CLASSES ), } )
@Entity
public class S5ClassEntity
    extends S5DtoClassPropInfoBaseEntity {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя внешнего ключа: parentId -> S5ClassEntity
   */
  public static final String FK_PARENTID_TO_CLASS = "S5ClassEntity_parentId_to_S5ClassEntity_fk"; //$NON-NLS-1$

  /**
   * Родительский класс. null если его нет (внешний ключ)
   */
  @ManyToOne
  @JoinColumn( name = "parentId",
      referencedColumnName = "id",
      nullable = true,
      unique = false,
      foreignKey = @ForeignKey( name = FK_PARENTID_TO_CLASS ) )
  private S5ClassEntity parent;

  /**
   * Уникальный идентификатор родительского класса
   */
  @Column( nullable = true,
      insertable = false,
      updatable = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ")" )
  private String parentId;

  /**
   * Описание расширенных атрибутов класса
   */
  @Column( nullable = false, //
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String attrString;

  /**
   * Описание расширенных склепок класса
   */
  @Column( nullable = false, //
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String rivetString;

  /**
   * Описание расширенных связей класса (парсируемый буфер формата ...) формат
   */
  @Column( nullable = false, //
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String linkString;

  /**
   * Описание данных класса
   */
  @Column( nullable = false, //
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String rtdataString;

  /**
   * Описание команд класса
   */
  @Column( nullable = false, //
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String cmdString;

  /**
   * Описание событий класса
   */
  @Column( nullable = false, //
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String eventString;

  /**
   * Описание clob-ов класса
   */
  @Column( nullable = false, //
      columnDefinition = IS5ImplementConstants.LOB_TEXT_TYPE //
  )
  private String clobString;

  /**
   * Lazy
   */
  transient private IStridablesListEdit<IDtoAttrInfo>   attrInfos;
  transient private IStridablesListEdit<IDtoRivetInfo>  rivetInfos;
  transient private IStridablesListEdit<IDtoLinkInfo>   linkInfos;
  transient private IStridablesListEdit<IDtoRtdataInfo> rtdataInfos;
  transient private IStridablesListEdit<IDtoCmdInfo>    cmdInfos;
  transient private IStridablesListEdit<IDtoEventInfo>  eventInfos;
  transient private IStridablesListEdit<IDtoClobInfo>   clobInfos;
  transient private IStridablesListEdit<IDtoAttrInfo>   allAttrInfos;
  transient private IStridablesListEdit<IDtoRivetInfo>  allRivetInfos;
  transient private IStridablesListEdit<IDtoLinkInfo>   allLinkInfos;
  transient private IStridablesListEdit<IDtoRtdataInfo> allDataInfos;
  transient private IStridablesListEdit<IDtoCmdInfo>    allCmdInfos;
  transient private IStridablesListEdit<IDtoEventInfo>  allEventInfos;
  transient private IStridablesListEdit<IDtoClobInfo>   allClobInfos;

  /**
   * Конструктор корневого класса или первичного ключа {@link #createPrimaryKey(String)}
   *
   * @param aId String идентификатор класса
   * @param aName String имя
   * @param aDescription String описание
   * @throws TsNullArgumentRtException aId = null
   */
  S5ClassEntity( String aId, String aName, String aDescription ) {
    super( aId, aName, aDescription );
    parent = null;
    parentId = TsLibUtils.EMPTY_STRING;
    attrString = DtoAttrInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    rivetString = DtoRivetInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    linkString = DtoLinkInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    rtdataString = DtoRtdataInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    cmdString = DtoCmdInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    eventString = DtoEventInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    clobString = DtoClobInfo.KEEPER.coll2str( IStridablesList.EMPTY );
  }

  /**
   * Конструктор копирования
   *
   * @param aParent {@link S5ClassEntity} описание родительского класса
   * @param aSource {@link IDtoClassInfo} исходное описание класса
   * @throws TsNullArgumentRtException aSource = null
   */
  S5ClassEntity( S5ClassEntity aParent, IDtoClassInfo aSource ) {
    super( aSource.id(), aSource.params() );
    parent = aParent;
    parentId = (aParent == null ? TsLibUtils.EMPTY_STRING : aParent.id());
    attrString = DtoAttrInfo.KEEPER.coll2str( aSource.attrInfos() );
    rivetString = DtoRivetInfo.KEEPER.coll2str( aSource.rivetInfos() );
    linkString = DtoLinkInfo.KEEPER.coll2str( aSource.linkInfos() );
    rtdataString = DtoRtdataInfo.KEEPER.coll2str( aSource.rtdataInfos() );
    cmdString = DtoCmdInfo.KEEPER.coll2str( aSource.cmdInfos() );
    eventString = DtoEventInfo.KEEPER.coll2str( aSource.eventInfos() );
    clobString = DtoClobInfo.KEEPER.coll2str( aSource.clobInfos() );
  }

  /**
   * Конструктор по умолчанию (требование hibernate/JPA)
   */
  S5ClassEntity() {
    this( TsLibUtils.EMPTY_STRING, TsLibUtils.EMPTY_STRING, TsLibUtils.EMPTY_STRING );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает описание родительского класса
   *
   * @return {@link S5ClassEntity} описание класса. null: нет родительского класса
   */
  public S5ClassEntity parentClass() {
    return parent;
  }

  /**
   * Возвращает список ВСЕХ атрибутов с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoAttrInfo}&gt; список описаний атрибутов
   */
  public IStridablesList<IDtoAttrInfo> allAttrInfos() {
    if( allAttrInfos == null ) {
      allAttrInfos = new StridablesList<>( attrInfos() );
      if( parent != null ) {
        allAttrInfos.addAll( parent.allAttrInfos() );
      }
    }
    return allAttrInfos;
  }

  /**
   * Возвращает список ВСЕХ склепок с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoRivetInfo}&gt; список описаний склепок
   */
  public IStridablesList<IDtoRivetInfo> allRivetInfos() {
    if( allRivetInfos == null ) {
      allRivetInfos = new StridablesList<>( rivetInfos() );
      if( parent != null ) {
        allRivetInfos.addAll( parent.allRivetInfos() );
      }
    }
    return allRivetInfos;
  }

  /**
   * Возвращает список ВСЕХ связей с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoLinkInfo}&gt; список описаний связей
   */
  public IStridablesList<IDtoLinkInfo> allLinkInfos() {
    if( allLinkInfos == null ) {
      allLinkInfos = new StridablesList<>( linkInfos() );
      if( parent != null ) {
        allLinkInfos.addAll( parent.allLinkInfos() );
      }
    }
    return allLinkInfos;
  }

  /**
   * Возвращает список ВСЕХ данных реального времени с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoRtdataInfo}&gt; список описаний данных реального времени
   */
  public IStridablesList<IDtoRtdataInfo> allRtdataInfos() {
    if( allDataInfos == null ) {
      allDataInfos = new StridablesList<>( rtdataInfos() );
      if( parent != null ) {
        allDataInfos.addAll( parent.allRtdataInfos() );
      }
    }
    return allDataInfos;
  }

  /**
   * Возвращает список ВСЕХ команд с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoCmdInfo}&gt; список описаний команд
   */
  public IStridablesList<IDtoCmdInfo> allCmdInfos() {
    if( allCmdInfos == null ) {
      allCmdInfos = new StridablesList<>( cmdInfos() );
      if( parent != null ) {
        allCmdInfos.addAll( parent.allCmdInfos() );
      }
    }
    return allCmdInfos;
  }

  /**
   * Возвращает список ВСЕХ событий с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoEventInfo}&gt; список описаний событий
   */
  public IStridablesList<IDtoEventInfo> allEventInfos() {
    if( allEventInfos == null ) {
      allEventInfos = new StridablesList<>( eventInfos() );
      if( parent != null ) {
        allEventInfos.addAll( parent.allEventInfos() );
      }
    }
    return allEventInfos;
  }

  /**
   * Возвращает список ВСЕХ clob-ов с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoClobInfo}&gt; список clob-ов
   */
  public IStridablesList<IDtoClobInfo> allClobInfos() {
    if( allClobInfos == null ) {
      allClobInfos = new StridablesList<>( clobInfos() );
      if( parent != null ) {
        allClobInfos.addAll( parent.allClobInfos() );
      }
    }
    return allClobInfos;
  }

  /**
   * Обновление данных
   *
   * @param aSource {@link IDtoClassInfo} исходное описание
   * @throws TsNullArgumentRtException аргумент = null
   */
  @Override
  public void update( IDtoClassInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    super.update( aSource );
    parentId = aSource.parentId();
    attrString = DtoAttrInfo.KEEPER.coll2str( aSource.attrInfos() );
    rivetString = DtoRivetInfo.KEEPER.coll2str( aSource.rivetInfos() );
    linkString = DtoLinkInfo.KEEPER.coll2str( aSource.linkInfos() );
    rtdataString = DtoRtdataInfo.KEEPER.coll2str( aSource.rtdataInfos() );
    cmdString = DtoCmdInfo.KEEPER.coll2str( aSource.cmdInfos() );
    eventString = DtoEventInfo.KEEPER.coll2str( aSource.eventInfos() );
    clobString = DtoClobInfo.KEEPER.coll2str( aSource.clobInfos() );

    attrInfos = null;
    rivetInfos = null;
    linkInfos = null;
    rtdataInfos = null;
    cmdInfos = null;
    eventInfos = null;
    clobInfos = null;
    allAttrInfos = null;
    allRivetInfos = null;
    allLinkInfos = null;
    allDataInfos = null;
    allCmdInfos = null;
    allEventInfos = null;
    allClobInfos = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDtoClassInfo
  //
  @Override
  public String parentId() {
    return (parent == null ? TsLibUtils.EMPTY_STRING : parentId);
  }

  @Override
  public IStridablesList<IDtoAttrInfo> attrInfos() {
    if( attrInfos == null ) {
      attrInfos = new StridablesList<>( DtoAttrInfo.KEEPER.str2coll( attrString ) );
    }
    return attrInfos;
  }

  @Override
  public IStridablesList<IDtoRivetInfo> rivetInfos() {
    if( rivetInfos == null ) {
      rivetInfos = new StridablesList<>( DtoRivetInfo.KEEPER.str2coll( rivetString ) );
    }
    return rivetInfos;
  }

  @Override
  public IStridablesList<IDtoLinkInfo> linkInfos() {
    if( linkInfos == null ) {
      linkInfos = new StridablesList<>( DtoLinkInfo.KEEPER.str2coll( linkString ) );
    }
    return linkInfos;
  }

  @Override
  public IStridablesList<IDtoRtdataInfo> rtdataInfos() {
    if( rtdataInfos == null ) {
      rtdataInfos = new StridablesList<>( DtoRtdataInfo.KEEPER.str2coll( rtdataString ) );
    }
    return rtdataInfos;
  }

  @Override
  public IStridablesList<IDtoCmdInfo> cmdInfos() {
    if( cmdInfos == null ) {
      cmdInfos = new StridablesList<>( DtoCmdInfo.KEEPER.str2coll( cmdString ) );
    }
    return cmdInfos;
  }

  @Override
  public IStridablesList<IDtoEventInfo> eventInfos() {
    if( eventInfos == null ) {
      eventInfos = new StridablesList<>( DtoEventInfo.KEEPER.str2coll( eventString ) );
    }
    return eventInfos;
  }

  @Override
  public IStridablesList<IDtoClobInfo> clobInfos() {
    if( clobInfos == null ) {
      clobInfos = new StridablesList<>( DtoClobInfo.KEEPER.str2coll( clobString ) );
    }
    return clobInfos;
  }

  @Override
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  public <T extends IDtoClassPropInfoBase> IStridablesList<T> propInfos( ESkClassPropKind aKind ) {
    TsNullArgumentRtException.checkNull( aKind );
    return switch( aKind ) {
      case ATTR -> (IStridablesList)attrInfos();
      case RIVET -> (IStridablesList)rivetInfos();
      case RTDATA -> (IStridablesList)rtdataInfos();
      case LINK -> (IStridablesList)linkInfos();
      case CMD -> (IStridablesList)cmdInfos();
      case EVENT -> (IStridablesList)eventInfos();
      case CLOB -> (IStridablesList)clobInfos();
    };
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public boolean equals( Object aThat ) {
    if( super.equals( aThat ) ) {
      if( aThat instanceof IDtoClassInfo that ) {
        return //
        parentId.equals( that.parentId() ) && //
            attrInfos().equals( that.attrInfos() ) && //
            rivetInfos().equals( that.rivetInfos() ) && //
            rtdataInfos().equals( that.rtdataInfos() ) && //
            linkInfos().equals( that.linkInfos() ) && //
            cmdInfos().equals( that.cmdInfos() ) && //
            eventInfos().equals( that.eventInfos() ) && //
            clobInfos().equals( that.clobInfos() );
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = TsLibUtils.PRIME * result + parentId.hashCode();
    result = TsLibUtils.PRIME * result + attrInfos().hashCode();
    result = TsLibUtils.PRIME * result + rivetInfos().hashCode();
    result = TsLibUtils.PRIME * result + rtdataInfos().hashCode();
    result = TsLibUtils.PRIME * result + linkInfos().hashCode();
    result = TsLibUtils.PRIME * result + cmdInfos().hashCode();
    result = TsLibUtils.PRIME * result + eventInfos().hashCode();
    result = TsLibUtils.PRIME * result + clobInfos().hashCode();
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
   * @param aClassId String идентификатор класса
   * @return {@link S5ClassEntity} PRIMARY-ключ
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5ClassEntity createPrimaryKey( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    return new S5ClassEntity( aClassId, aClassId, aClassId );
  }

  /**
   * Проверяет наличие дубль-описаний в классе и его родителе
   *
   * @param aParent {@link S5ClassEntity} описание родительского класса. null: нет родительского класса, игнорирование
   * @param aClassInfo {@link IDtoClassInfo} описание класса
   * @throws TsNullArgumentRtException aClassInfo = null
   * @throws TsItemAlreadyExistsRtException описание уже существует
   */
  public static void checkDuplicateInfos( S5ClassEntity aParent, IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    if( aParent == null ) {
      return;
    }
    String classId = aClassInfo.id();
    String parentId = aParent.id();
    // Проверка атрибутов
    for( IDtoAttrInfo info : aClassInfo.attrInfos() ) {
      if( aParent.allAttrInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_ATTR_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка склепок
    for( IDtoRivetInfo info : aClassInfo.rivetInfos() ) {
      if( aParent.allRivetInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_RIVET_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка связей
    for( IDtoLinkInfo info : aClassInfo.linkInfos() ) {
      if( aParent.allLinkInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_LINK_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка данных
    for( IDtoRtdataInfo info : aClassInfo.rtdataInfos() ) {
      if( aParent.allRtdataInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_DATA_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка команд
    for( IDtoCmdInfo info : aClassInfo.cmdInfos() ) {
      if( aParent.allCmdInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_CMD_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка событий
    for( IDtoEventInfo info : aClassInfo.eventInfos() ) {
      if( aParent.allEventInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_EVENT_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка clob-ов
    for( IDtoClobInfo info : aClassInfo.clobInfos() ) {
      if( aParent.allClobInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_CLOB_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
  }

  /**
   * Из представленного списка описаний классов возвращает список описаний классов-наследников указанного класса
   *
   * @param aClassInfos {@link IStridablesList}&lt;{@link S5ClassEntity}&gt;список описаний классов
   * @param aClassId String идентификатор класса
   * @return {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; список описаний классов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IStridablesList<IDtoClassInfo> getDescendantClasses( IStridablesList<S5ClassEntity> aClassInfos,
      String aClassId ) {
    TsNullArgumentRtException.checkNulls( aClassInfos, aClassId );
    IStridablesListEdit<IDtoClassInfo> retValue = new StridablesList<>();
    for( S5ClassEntity classEntity : aClassInfos ) {
      if( hasParent( classEntity, aClassId ) ) {
        retValue.add( classEntity );
      }
    }
    return retValue;
  }

  /**
   * Определяет, есть ли указанного класса (aClassInfo) предок с идентификатором aParentClassId
   *
   * @param aClassInfo S5ClassEntity - описание класса
   * @param aParentClassId String - идентификатор родительского класса
   * @return <b>true</b> есть предок; <b>false</b> нет предка.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean hasParent( S5ClassEntity aClassInfo, String aParentClassId ) {
    TsNullArgumentRtException.checkNulls( aClassInfo, aParentClassId );
    for( S5ClassEntity parent = aClassInfo.parentClass(); parent != null; parent = parent.parentClass() ) {
      String parentClassId = parent.id();
      if( parentClassId.equals( aParentClassId ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Определяет, есть ли указанный класс (aClassInfo) или его предок в списке с aParentClassIds
   *
   * @param aParentClassIds {@link IStringList} - список идентификаторов родительских классов
   * @param aClassInfo {@link S5ClassEntity} - описание класса
   * @return <b>true</b> есть предок; <b>false</b> нет предка.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean hasAcceptable( IStringList aParentClassIds, S5ClassEntity aClassInfo ) {
    TsNullArgumentRtException.checkNulls( aClassInfo, aParentClassIds );
    for( String parentClassId : aParentClassIds ) {
      // Поиск по всей иерархии классов
      if( hasParent( aClassInfo, parentClassId ) ) {
        return true;
      }
      String classId = aClassInfo.id();
      if( classId.equals( parentClassId ) ) {
        return true;
      }
    }
    return false;
  }
}
