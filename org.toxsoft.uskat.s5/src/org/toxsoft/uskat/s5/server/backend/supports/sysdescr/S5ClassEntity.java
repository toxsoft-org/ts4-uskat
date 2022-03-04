package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassesSQL.*;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsItemAlreadyExistsRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectEntity;

import ru.uskat.common.dpu.*;
import ru.uskat.common.dpu.impl.*;

/**
 * Реализация интерфейса {@link IDpuSdClassInfo} способная маппироваться на таблицу базы данных
 *
 * @author mvk
 */
@NamedQueries( { @NamedQuery( name = QUERY_NAME_GET_CLASSES, query = QUERY_GET_CLASSES ), } )
@Entity
public class S5ClassEntity
    extends S5DpuBaseEntity
    implements IDpuSdClassInfo {

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
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin default null" )
  private String parentId;

  /**
   * Описание расширенных атрибутов класса
   */
  @Lob
  @Column( nullable = false )
  private String attrString;

  /**
   * Описание расширенных связей класса (парсируемый буфер формата ...) формат
   */
  @Lob
  @Column( nullable = false )
  private String linkString;

  /**
   * Описание данных класса
   */
  @Lob
  @Column( nullable = false )
  private String rtdataString;

  /**
   * Описание команд класса
   */
  @Lob
  @Column( nullable = false )
  private String cmdString;

  /**
   * Описание событий класса
   */
  @Lob
  @Column( nullable = false )
  private String eventString;

  /**
   * Lazy
   */
  transient private IStridablesListEdit<IDpuSdAttrInfo>   attrInfos;
  transient private IStridablesListEdit<IDpuSdLinkInfo>   linkInfos;
  transient private IStridablesListEdit<IDpuSdRtdataInfo> rtdataInfos;
  transient private IStridablesListEdit<IDpuSdCmdInfo>    cmdInfos;
  transient private IStridablesListEdit<IDpuSdEventInfo>  eventInfos;
  transient private IStridablesListEdit<IDpuSdAttrInfo>   allAttrInfos;
  transient private IStridablesListEdit<IDpuSdLinkInfo>   allLinkInfos;
  transient private IStridablesListEdit<IDpuSdRtdataInfo> allDataInfos;
  transient private IStridablesListEdit<IDpuSdCmdInfo>    allCmdInfos;
  transient private IStridablesListEdit<IDpuSdEventInfo>  allEventInfos;

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
    attrString = DpuSdAttrInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    linkString = DpuSdLinkInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    rtdataString = DpuSdRtdataInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    cmdString = DpuSdCmdInfo.KEEPER.coll2str( IStridablesList.EMPTY );
    eventString = DpuSdEventInfo.KEEPER.coll2str( IStridablesList.EMPTY );
  }

  /**
   * Конструктор копирования
   *
   * @param aParent {@link S5ClassEntity} описание родительского класса
   * @param aSource {@link IDpuSdClassInfo} исходное описание класса
   * @throws TsNullArgumentRtException aSource = null
   */
  S5ClassEntity( S5ClassEntity aParent, IDpuSdClassInfo aSource ) {
    super( aSource.id(), aSource.params() );
    parent = aParent;
    parentId = (aParent == null ? TsLibUtils.EMPTY_STRING : aParent.id());
    attrString = DpuSdAttrInfo.KEEPER.coll2str( aSource.attrInfos() );
    linkString = DpuSdLinkInfo.KEEPER.coll2str( aSource.linkInfos() );
    rtdataString = DpuSdRtdataInfo.KEEPER.coll2str( aSource.rtdataInfos() );
    cmdString = DpuSdCmdInfo.KEEPER.coll2str( aSource.cmdInfos() );
    eventString = DpuSdEventInfo.KEEPER.coll2str( aSource.eventInfos() );
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
   * @return {@link IStridablesList}&lt;{@link IDpuSdAttrInfo}&gt; список описаний атрибутов
   */
  public IStridablesList<IDpuSdAttrInfo> allAttrInfos() {
    if( allAttrInfos == null ) {
      allAttrInfos = new StridablesList<>( attrInfos() );
      if( parent != null ) {
        allAttrInfos.addAll( parent.allAttrInfos() );
      }
    }
    return allAttrInfos;
  }

  /**
   * Возвращает список ВСЕХ связей с учетом родительских классов
   *
   * @return {@link IStridablesList}&lt;{@link IDpuSdLinkInfo}&gt; список описаний связей
   */
  public IStridablesList<IDpuSdLinkInfo> allLinkInfos() {
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
   * @return {@link IStridablesList}&lt;{@link IDpuSdRtdataInfo}&gt; список описаний данных реального времени
   */
  public IStridablesList<IDpuSdRtdataInfo> allRtdataInfos() {
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
   * @return {@link IStridablesList}&lt;{@link IDpuSdCmdInfo}&gt; список описаний команд
   */
  public IStridablesList<IDpuSdCmdInfo> allCmdInfos() {
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
   * @return {@link IStridablesList}&lt;{@link IDpuSdEventInfo}&gt; список описаний событий
   */
  public IStridablesList<IDpuSdEventInfo> allEventInfos() {
    if( allEventInfos == null ) {
      allEventInfos = new StridablesList<>( eventInfos() );
      if( parent != null ) {
        allEventInfos.addAll( parent.allEventInfos() );
      }
    }
    return allEventInfos;
  }

  /**
   * Обновление данных
   *
   * @param aSource {@link IDpuSdClassInfo} исходное описание
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void update( IDpuSdClassInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    super.update( aSource );
    attrString = DpuSdAttrInfo.KEEPER.coll2str( aSource.attrInfos() );
    linkString = DpuSdLinkInfo.KEEPER.coll2str( aSource.linkInfos() );
    rtdataString = DpuSdRtdataInfo.KEEPER.coll2str( aSource.rtdataInfos() );
    cmdString = DpuSdCmdInfo.KEEPER.coll2str( aSource.cmdInfos() );
    eventString = DpuSdEventInfo.KEEPER.coll2str( aSource.eventInfos() );

    attrInfos = null;
    linkInfos = null;
    rtdataInfos = null;
    cmdInfos = null;
    eventInfos = null;
    allAttrInfos = null;
    allLinkInfos = null;
    allDataInfos = null;
    allCmdInfos = null;
    allEventInfos = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDpuSdClassInfo
  //
  @Override
  public String parentId() {
    return (parent == null ? TsLibUtils.EMPTY_STRING : parentId);
  }

  @Override
  public IStridablesList<IDpuSdAttrInfo> attrInfos() {
    if( attrInfos == null ) {
      // attrInfos = str2StridablesList( attrString, DpuSdAttrInfo.KEEPER );
      attrInfos = new StridablesList<>( DpuSdAttrInfo.KEEPER.str2coll( attrString ) );
    }
    return attrInfos;
  }

  @Override
  public IStridablesList<IDpuSdLinkInfo> linkInfos() {
    if( linkInfos == null ) {
      linkInfos = new StridablesList<>( DpuSdLinkInfo.KEEPER.str2coll( linkString ) );
    }
    return linkInfos;
  }

  @Override
  public IStridablesList<IDpuSdRtdataInfo> rtdataInfos() {
    if( rtdataInfos == null ) {
      rtdataInfos = new StridablesList<>( DpuSdRtdataInfo.KEEPER.str2coll( rtdataString ) );
    }
    return rtdataInfos;
  }

  @Override
  public IStridablesList<IDpuSdCmdInfo> cmdInfos() {
    if( cmdInfos == null ) {
      cmdInfos = new StridablesList<>( DpuSdCmdInfo.KEEPER.str2coll( cmdString ) );
    }
    return cmdInfos;
  }

  @Override
  public IStridablesList<IDpuSdEventInfo> eventInfos() {
    if( eventInfos == null ) {
      eventInfos = new StridablesList<>( DpuSdEventInfo.KEEPER.str2coll( eventString ) );
    }
    return eventInfos;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public boolean equals( Object aThat ) {
    if( super.equals( aThat ) ) {
      if( aThat instanceof IDpuSdClassInfo that ) {
        return attrInfos().equals( that.attrInfos() ) && rtdataInfos().equals( that.rtdataInfos() )
            && linkInfos().equals( that.linkInfos() ) && cmdInfos().equals( that.cmdInfos() )
            && eventInfos().equals( that.eventInfos() );
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = TsLibUtils.PRIME * result + attrInfos().hashCode();
    result = TsLibUtils.PRIME * result + rtdataInfos().hashCode();
    result = TsLibUtils.PRIME * result + linkInfos().hashCode();
    result = TsLibUtils.PRIME * result + cmdInfos().hashCode();
    result = TsLibUtils.PRIME * result + eventInfos().hashCode();
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
   * @param aClassInfo {@link IDpuSdClassInfo} описание класса
   * @throws TsNullArgumentRtException aClassInfo = null
   * @throws TsItemAlreadyExistsRtException описание уже существует
   */
  public static void checkDuplicateInfos( S5ClassEntity aParent, IDpuSdClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    if( aParent == null ) {
      return;
    }
    String classId = aClassInfo.id();
    String parentId = aParent.id();
    // Проверка атрибутов
    for( IDpuSdAttrInfo info : aClassInfo.attrInfos() ) {
      if( aParent.allAttrInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_ATTR_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка связей
    for( IDpuSdLinkInfo info : aClassInfo.linkInfos() ) {
      if( aParent.allLinkInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_LINK_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка данных
    for( IDpuSdRtdataInfo info : aClassInfo.rtdataInfos() ) {
      if( aParent.allRtdataInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_DATA_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка команд
    for( IDpuSdCmdInfo info : aClassInfo.cmdInfos() ) {
      if( aParent.allCmdInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_CMD_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
    // Проверка событий
    for( IDpuSdEventInfo info : aClassInfo.eventInfos() ) {
      if( aParent.allEventInfos().hasKey( info.id() ) ) {
        throw new TsItemAlreadyExistsRtException( MSG_ERR_EVENT_ALREADY_EXIST, info.id(), classId, parentId );
      }
    }
  }

  /**
   * Из представленного списка описаний классов возвращает список описаний классов которые зависят от представленного
   * типа
   *
   * @param aClassInfos {@link IStridablesList}&lt;{@link S5ClassEntity}&gt;список описаний классов
   * @param aTypeId String идентификатор типа
   * @return {@link IStridablesList}&lt;{@link IDpuSdClassInfo}&gt; список описаний классов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IStridablesList<IDpuSdClassInfo> getClassesDependsFromType( IStridablesList<S5ClassEntity> aClassInfos,
      String aTypeId ) {
    TsNullArgumentRtException.checkNulls( aClassInfos, aTypeId );
    IStridablesListEdit<IDpuSdClassInfo> retValue = new StridablesList<>();
    for( S5ClassEntity classEntity : aClassInfos ) {
      if( isClassDependsFromType( classEntity, aTypeId ) ) {
        retValue.add( classEntity );
      }
    }
    return retValue;
  }

  /**
   * Возвращает признак того что представленное описание класса зависит от представленного типа
   *
   * @param aClassInfo {@link S5ClassEntity} описание класса
   * @param aTypeId String идентификатор типа
   * @return boolean <b>true</b> класс зависит от типа; <b>false</b> класс не зависит от типа
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static boolean isClassDependsFromType( S5ClassEntity aClassInfo, String aTypeId ) {
    TsNullArgumentRtException.checkNulls( aClassInfo, aTypeId );
    IStridablesList<IDpuSdAttrInfo> attrInfos = aClassInfo.allAttrInfos();
    for( IDpuSdAttrInfo attrInfo : attrInfos ) {
      if( attrInfo.typeId().equals( aTypeId ) ) {
        return true;
      }
    }
    IStridablesList<IDpuSdRtdataInfo> rtdataInfos = aClassInfo.allRtdataInfos();
    for( IDpuSdRtdataInfo rtdataInfo : rtdataInfos ) {
      if( rtdataInfo.typeId().equals( aTypeId ) ) {
        return true;
      }
    }
    IStridablesList<IDpuSdCmdInfo> cmdInfos = aClassInfo.allCmdInfos();
    for( IDpuSdCmdInfo cmdInfo : cmdInfos ) {
      for( IDpuStridableDataDef argDef : cmdInfo.argDefs() ) {
        if( argDef.typeId().equals( aTypeId ) ) {
          return true;
        }
      }
    }
    IStridablesList<IDpuSdEventInfo> eventInfos = aClassInfo.allEventInfos();
    for( IDpuSdEventInfo eventInfo : eventInfos ) {
      for( IDpuStridableDataDef paramDef : eventInfo.paramDefs() ) {
        if( paramDef.typeId().equals( aTypeId ) ) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Из представленного списка описаний классов возвращает список описаний классов-наследников указанного класса
   *
   * @param aClassInfos {@link IStridablesList}&lt;{@link S5ClassEntity}&gt;список описаний классов
   * @param aClassId String идентификатор класса
   * @return {@link IStridablesList}&lt;{@link IDpuSdClassInfo}&gt; список описаний классов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IStridablesList<IDpuSdClassInfo> getDescendantClasses( IStridablesList<S5ClassEntity> aClassInfos,
      String aClassId ) {
    TsNullArgumentRtException.checkNulls( aClassInfos, aClassId );
    IStridablesListEdit<IDpuSdClassInfo> retValue = new StridablesList<>();
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
