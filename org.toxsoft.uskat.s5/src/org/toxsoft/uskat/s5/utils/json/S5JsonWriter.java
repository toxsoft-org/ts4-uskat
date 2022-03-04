package org.toxsoft.uskat.s5.utils.json;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static ru.uskat.common.ISkHardConstants.*;

import org.toxsoft.core.pas.tj.ITjObject;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.links.ISkLinkFwd;
import ru.uskat.core.api.links.ISkLinkService;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.common.skobject.ISkObject;
import ru.uskat.core.connection.ESkConnState;
import ru.uskat.core.connection.ISkConnection;

/**
 * Писатель JSON
 *
 * @author mvk
 */
public class S5JsonWriter {

  /**
   * Имя поля: Идентификатор класса
   */
  public static final String CLASSID = "classId"; //$NON-NLS-1$

  /**
   * Имя поля: Идентификатор объекта
   */
  public static final String OBJID = "objId"; //$NON-NLS-1$

  /**
   * Имя поля: Идентификатор атрибута
   */
  public static final String ATTRID = "attrId"; //$NON-NLS-1$

  /**
   * Имя поля: Идентификатор связи
   */
  public static final String LINKID = "linkId"; //$NON-NLS-1$

  /**
   * Имя поля: Идентификатор родительской сущности
   */
  public static final String PARENT = "parent"; //$NON-NLS-1$

  /**
   * Имя поля: параметры сущности
   */
  public static final String PARAMS = "params"; //$NON-NLS-1$

  /**
   * Имя поля: список классов
   */
  public static final String CLASSES = "classes"; //$NON-NLS-1$

  /**
   * Имя поля: список атрибутов
   */
  public static final String ATTRS = "attrs"; //$NON-NLS-1$

  /**
   * Имя поля: список связей
   */
  public static final String LINKS = "links"; //$NON-NLS-1$

  /**
   * Имя поля: список объектов
   */
  public static final String OBJECTS = "objects"; //$NON-NLS-1$

  /**
   * Создает объект {@link ITjObject} для содержимого соединения
   *
   * @param aConnection {@link ISkConnection} соединение
   * @param aExcludeClassIds {@link IStringList} список идентификаторов классов для которых не требуется проводить
   *          экспорт. Внимание!: Если из экспорта исключается базовый класс, то всего его наследники также исключаются
   *          из эскпорта.
   * @return {@link ITjObject} объект представляющий данные в формате JSON
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неактивное соединение
   */
  public static ITjObject write( ISkConnection aConnection, IStringList aExcludeClassIds ) {
    TsNullArgumentRtException.checkNulls( aConnection, aExcludeClassIds );
    TsIllegalArgumentRtException.checkFalse( aConnection.state() == ESkConnState.ACTIVE );
    ISkCoreApi coreApi = aConnection.coreApi();
    ISkClassInfoManager cm = coreApi.sysdescr().classInfoManager();
    ISkObjectService os = coreApi.objService();
    ISkLinkService ls = coreApi.linkService();
    ITjObject retValue = createTjObject();

    // Составление списка классов для исключения которые есть в соединении
    IStringListEdit excludedClassIds = new StringArrayList();
    for( String id : aExcludeClassIds ) {
      if( cm.findClassInfo( id ) != null ) {
        excludedClassIds.add( id );
      }
    }

    // Классы
    IListEdit<ITjValue> classes = new ElemLinkedList<>();
    for( ISkClassInfo classInfo : cm.listClasses() ) {
      String classId = classInfo.id();
      // Проверка того, что класс не находится в списке исключенных
      if( isExcludedClass( cm, excludedClassIds, classId ) ) {
        // Класс в blacklist
        continue;
      }
      // Идентификатор, имя и описание атрибута находится в params
      ITjObject tjoClass = createTjObject();
      // Идентификатор класса
      tjoClass.fields().put( CLASSID, createString( classInfo.id() ) );
      // Родительский класс
      tjoClass.fields().put( PARENT, createString( classInfo.parentId() ) );
      // Параметры класса
      tjoClass.fields().put( PARAMS, writeOptionSet( classInfo.params(), IStringList.EMPTY ) );
      // Описание родительского класса. null: его не существует
      ISkClassInfo parentClassInfo =
          (!classId.equals( GW_ROOT_CLASS_ID ) ? cm.getClassInfo( classInfo.parentId() ) : null);
      // Атрибуты класса
      IListEdit<ITjValue> attrs = new ElemLinkedList<>();
      for( ISkAttrInfo attrInfo : classInfo.attrInfos() ) {
        if( parentClassInfo != null && parentClassInfo.attrInfos().hasKey( attrInfo.id() ) ) {
          // Атрибут определен в базовом классе
          continue;
        }
        // Идентификатор, имя и описание атрибута находится в params
        ITjObject tjoAttr = createTjObject();
        // Идентификатор атрибута
        tjoAttr.fields().put( ATTRID, createString( attrInfo.id() ) );
        // Параметры атрибута
        tjoAttr.fields().put( PARAMS, writeOptionSet( attrInfo.params(), IStringList.EMPTY ) );
        // Добавление атрибута в список атрибутов класса
        attrs.add( createObject( tjoAttr ) );
      }
      tjoClass.fields().put( ATTRS, createArray( attrs ) );

      // Связи класса
      IListEdit<ITjValue> links = new ElemLinkedList<>();
      for( ISkLinkInfo linkInfo : classInfo.linkInfos() ) {
        if( parentClassInfo != null && parentClassInfo.linkInfos().hasKey( linkInfo.id() ) ) {
          // Связь определена в базовом классе
          continue;
        }
        // Идентификатор, имя, описание, cписок классов правых объектов и ограничения находится в params
        ITjObject tjoLink = createTjObject();
        // Идентификатор связи
        tjoLink.fields().put( LINKID, createString( linkInfo.id() ) );
        // Параметры связи.
        tjoLink.fields().put( PARAMS, writeOptionSet( linkInfo.params(), IStringList.EMPTY ) );
        // Добавление связи в список связей класса
        links.add( createObject( tjoLink ) );
      }
      tjoClass.fields().put( LINKS, createArray( links ) );

      // TODO: опционально: rt-данные, события, команды,...
      // Добавление объекта класса в общий список
      classes.add( createObject( tjoClass ) );
    }
    retValue.fields().put( CLASSES, createArray( classes ) );

    // Объекты
    IListEdit<ITjValue> objects = new ElemLinkedList<>();
    // Список атрибутов исключенных из обработки (избыточная информация)
    IStringList excludedAttrs = new StringArrayList( AID_SKID, AID_CLASS_ID, AID_STRID );
    for( ISkClassInfo classInfo : cm.listClasses() ) {
      String classId = classInfo.id();
      // Проверка того, что класс не находится в списке исключенных
      if( isExcludedClass( cm, excludedClassIds, classId ) ) {
        // Класс в blacklist
        continue;
      }
      // Список объектов класса. aIncludeDescendants = false
      IList<ISkObject> objs = os.listObjs( classId, false );
      // Список объектов класса в формате ITjValue
      IListEdit<ITjValue> classObjects = new ElemLinkedList<>();
      for( ISkObject obj : objs ) {
        ITjObject tjoObj = createTjObject();
        tjoObj.fields().put( OBJID, createString( obj.skid().strid() ) );
        tjoObj.fields().put( ATTRS, writeOptionSet( obj.attrs(), excludedAttrs ) );
        tjoObj.fields().put( LINKS, writeLinks( ls, classInfo, obj ) );
        classObjects.add( createObject( tjoObj ) );
      }
      // Идентификатор, имя и описание атрибута находится в params
      ITjObject tjoClass = createTjObject();
      tjoClass.fields().put( classId, createArray( classObjects ) );
      objects.add( createObject( tjoClass ) );
    }
    retValue.fields().put( OBJECTS, createArray( objects ) );

    // TODO: что нужно еще ???

    return retValue;
  }

  /**
   * Возвращает признак того, что указанный класс должен быть исключен из обработки
   *
   * @param aClassInfoManager {@link ISkClassInfoManager} менеджер классов
   * @param aExcludedClassIds {@link IStringListEdit} список идентификаторов исключаемых классов
   * @param aClassId String проверяемый класс
   * @return boolean <b>true</b> класс исключается из обработки;<b>false</b> класс не исключается из обработки
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static boolean isExcludedClass( ISkClassInfoManager aClassInfoManager, IStringListEdit aExcludedClassIds,
      String aClassId ) {
    TsNullArgumentRtException.checkNulls( aClassInfoManager, aExcludedClassIds, aClassId );
    for( String excludeClassId : aExcludedClassIds ) {
      if( (aClassInfoManager.isAncestor( excludeClassId, aClassId ) || excludeClassId.equals( aClassId )) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Создает объект {@link ITjObject} для содержимого {@link IOptionSet}
   *
   * @param aOptionSet {@link IOptionSet} именованный набор для которого создается объект {@link ITjObject}.
   * @param aExcludedParams {@link IStringList} список имен параметров исключенных из обработки
   * @return {@link ITjValue} значение представляющий данные в формате JSON
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неактивное соединение
   */
  private static ITjValue writeOptionSet( IOptionSet aOptionSet, IStringList aExcludedParams ) {
    TsNullArgumentRtException.checkNulls( aOptionSet, aExcludedParams );
    ITjObject retValue = createTjObject();
    for( String name : aOptionSet.keys() ) {
      if( aExcludedParams.hasElem( name ) ) {
        // Атрибут исключен из обработки
        continue;
      }
      IAtomicValue value = aOptionSet.getValue( name );
      switch( value.atomicType() ) {
        case INTEGER:
          retValue.fields().put( name, createNumber( value.asInt() ) );
          break;
        case FLOATING:
          retValue.fields().put( name, createNumber( value.asDouble() ) );
          break;
        case BOOLEAN:
        case TIMESTAMP:
        case STRING:
        case VALOBJ:
        case NONE:
          retValue.fields().put( name, createString( value.asString() ) );
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    return createObject( retValue );
  }

  /**
   * Создает объект {@link ITjObject} для всех связей объекта {@link IOptionSet}
   *
   * @param aLinkService {@link ISkLinkService} служба связей
   * @param aClassInfo {@link ISkClassInfo} описание класса объекта
   * @param aObj {@link ISkObject} объект связи которого необходимо обработать
   * @return {@link ITjValue} значение представляющий данные в формате JSON
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static ITjValue writeLinks( ISkLinkService aLinkService, ISkClassInfo aClassInfo, ISkObject aObj ) {
    TsNullArgumentRtException.checkNulls( aLinkService, aClassInfo, aObj );
    IListEdit<ITjValue> objectLinks = new ElemLinkedList<>();
    for( ISkLinkInfo linkInfo : aClassInfo.linkInfos() ) {
      String linkId = linkInfo.id();
      ISkLinkFwd linkFwd = aLinkService.getLink( aObj.skid(), linkId );
      IListEdit<ITjValue> link = new ElemLinkedList<>();
      for( Skid rightObjId : linkFwd.rightSkids() ) {
        link.add( createString( rightObjId.toString() ) );
      }
      if( link.size() == 0 ) {
        continue;
      }
      ITjObject tjoLink = createTjObject();
      tjoLink.fields().put( linkId, createArray( link ) );
      objectLinks.add( createObject( tjoLink ) );
    }
    return createArray( objectLinks );
  }

}
