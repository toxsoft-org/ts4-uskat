package org.toxsoft.uskat.skadmin.core;

import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.IAdminResources.*;

import org.toxsoft.core.pas.client.PasClient;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.linkserv.ISkLinkService;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.s5.client.remote.connection.IS5Connection;
import org.toxsoft.uskat.s5.common.S5Host;

/**
 * Известные имена параметров контекста выполнения команд {@link IAdminCmdContext}.
 * <p>
 * Этот класс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный идентификатор параметра (ИД-имя);</li>
 * <li><b>nmName</b>() - краткое название параметра;</li>
 * <li><b>description</b>() - удобочитаемое описание параметра.</li>
 * </ul>
 *
 * @author mvk
 */
public enum EAdminCmdContextNames
    implements IAdminCmdContextParam {

  /**
   * Каталоги в которых могут находится плагины для s5admin.
   * <p>
   * Значение устанавливается следующими способами (в порядке приоритета):
   * <ul>
   * <li>Через jvm-аргумент "ru.uskat.s5.admin.plugin.paths. Например: <i>java
   * -Dru.uskat.s5.admin.plugin.paths="{path1,path2,path3}"</i>";</li>
   * <li>Текущий каталог файловой системы.</li>
   * </ul>
   */
  CTX_PLUGIN_PATHS( "ru.uskat.s5.admin.plugin.paths", E_CN_D_PLUGIN_PATHS, E_CN_N_PLUGIN_PATHS, PT_SINGLE_VALOBJ ), // $NON-NLS-1$ //$NON-NLS-1$

  /**
   * Каталог относительного которого конечное приложение формирует свою файловую систему.
   * <p>
   * Значение устанавливается следующими способами (в порядке приоритета):
   * <ul>
   * <li>Через JVM-аргумент "org.toxsoft.uskat.skadmin.core.application.home. Например: <i>java
   * -Dru.toxsoft.s5.admin.application.home=" application_dir "</i>";</li>
   * <li>Через текстовый файл конфигурации <b>s5admin.cfg</b> который размещается в текущем каталоге. Файл должен иметь
   * формат {@link OptionSetKeeper} и должен определять параметр
   * <b>org.toxsoft.uskat.skadmin.core.application.home</b>;</li>
   * <li>Текущий каталог файловой системы.</li>
   * </ul>
   */
  CTX_APPLICATION_DIR( "ru.uskat.s5.admin.application.home", E_CN_D_APPLICATION_DIR, E_CN_N_APPLICATION_DIR, //$NON-NLS-1$
      PT_SINGLE_STRING ),

  /**
   * Соединение с сервером.
   * <p>
   * Cоединение всегда размещается в контексте как неактивное (нет связи с сервером и не делаются попытки установить эту
   * связь).
   * <p>
   * Соединие всегда удаляется из контекста как неактивное. То есть, если соединение в данный момент активно (есть связь
   * с сервером или делаются попытки установить с ним связь) , то сначала соединение переводится в неактивное состояние,
   * а потом удаляется из контекста.
   */
  CTX_CONNECTION( "connection", E_CN_D_CONNECTION, E_CN_N_CONNECTION, IS5Connection.class, false ), //$NON-NLS-1$

  /**
   * Соединение с skat-s5-сервером.
   * <p>
   * Cоединение всегда размещается в контексте как неактивное (нет связи с сервером и не делаются попытки установить эту
   * связь).
   * <p>
   * Соединие всегда удаляется из контекста как неактивное. То есть, если соединение в данный момент активно (есть связь
   * с сервером или делаются попытки установить с ним связь) , то сначала соединение переводится в неактивное состояние,
   * а потом удаляется из контекста.
   */
  CTX_SK_CONNECTION( "skConnection", E_CN_D_SK_CONNECTION, E_CN_N_SK_CONNECTION, ISkConnection.class, false ), //$NON-NLS-1$

  /**
   * Клиент сервера PAS (Public Access Server).
   */
  CTX_PAS_CLIENT( "pasClient", E_CN_D_PAS_CLIENT, E_CN_N_PAS_CLIENT, PasClient.class, false ), //$NON-NLS-1$

  /**
   * Реализация API сервера s5.
   */
  CTX_SK_CORE_API( "coreApi", E_CN_D_CORE_API, E_CN_N_CORE_API, ISkCoreApi.class, false ), //$NON-NLS-1$

  /**
   * Адрес подключенного сервера.
   */
  CTX_SK_HOSTS( "skHost", E_CN_D_HOST, E_CN_N_HOST, S5Host.class, false ), //$NON-NLS-1$

  /**
   * Реализация службы управления классами сервера s5.
   */
  CTX_SK_CLASS_SERVICE( ISkSysdescr.SERVICE_ID, E_CN_D_SK_CLASS_SERVICE, E_CN_N_SK_CLASS_SERVICE, ISkSysdescr.class,
      false ),

  /**
   * Реализация службы управления объектами сервера s5.
   */
  CTX_SK_OBJECT_SERVICE( ISkObjectService.SERVICE_ID, E_CN_D_SK_OBJECT_SERVICE, E_CN_N_SK_OBJECT_SERVICE,
      ISkObjectService.class, false ), // $NON-NLS-1$

  /**
   * Реализация службы управления связями между объектами сервера s5.
   */
  CTX_SK_LINK_SERVICE( ISkLinkService.SERVICE_ID, E_CN_D_SK_LINK_SERVICE, E_CN_N_SK_LINK_SERVICE, ISkLinkService.class,
      false ),

  /**
   * Атомарное значение данного (IAtomicValue).
   */
  CTX_SK_ATOMIC_VALUE( "atomicValue", E_CN_D_SK_VALUE, E_CN_N_SK_VALUE, IAtomicValue.class, false ), //$NON-NLS-1$

  ;

  private static IStridablesListEdit<EAdminCmdContextNames> list = null;
  private final String                                      id;
  private final String                                      description;
  private final String                                      nmName;
  private final IPlexyType                                  type;

  /**
   * Создать константу с заданием всех инвариантов.
   *
   * @param aId String - идентифицирующее название константы
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое удобовчитаемое название
   * @param aType {@link IPlexyType} - тип значения
   */
  EAdminCmdContextNames( String aId, String aDescr, String aName, IPlexyType aType ) {
    id = aId;
    description = aDescr;
    nmName = aName;
    type = aType;
  }

  /**
   * Создать константу с типом значения объектной ссылки
   *
   * @param aId String - идентифицирующее название константы
   * @param aDescr String - отображаемое описание константы
   * @param aName String - краткое удобовчитаемое название
   * @param aRefType Class - тип объектной ссылки
   * @param aIsList boolean <b>true</b> список объектных ссылок; <b>false</b> одиночная объектная ссылка
   */
  EAdminCmdContextNames( String aId, String aDescr, String aName, Class<?> aRefType, boolean aIsList ) {
    this( aId, aDescr, aName, (aIsList ? ptRefList( aRefType ) : ptSingleRef( aRefType )) );
  }

  // --------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmdContextParam
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public String nmName() {
    return nmName;
  }

  @Override
  public IPlexyType type() {
    return type;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методв класса Object
  //

  @Override
  public String toString() {
    return id + " " + nmName; //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // API класса
  //

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IStridablesList}&lt; {@link EAdminCmdContextNames} &gt; - список всех констант
   */
  public static IStridablesList<EAdminCmdContextNames> asStridablesList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Возвращает все константы в виде списка.
   *
   * @return {@link IList}&lt; {@link EAdminCmdContextNames} &gt; - список всех констант
   */
  public static IList<EAdminCmdContextNames> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list.values();
  }

  // ----------------------------------------------------------------------------------
  // Методы проверки
  //

  /**
   * Определяет, существует ли константа перечисления с заданным идентификатором.
   *
   * @param aId String - идентификатор искомой константы
   * @return boolean - признак существования константы <br>
   *         <b>true</b> - константа с заданным идентификатором существует;<br>
   *         <b>false</b> - неет константы с таким идентификатором.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemById( String aId ) {
    return findByIdOrNull( aId ) != null;
  }

  /**
   * Определяет, существует ли константа перечисления с заданным описанием.
   *
   * @param aDescription String - описание искомой константы
   * @return boolean - признак существования константы <br>
   *         <b>true</b> - константа с заданным описанием существует;<br>
   *         <b>false</b> - неет константы с таким описанием.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemByDescription( String aDescription ) {
    return findByDescriptionOrNull( aDescription ) != null;
  }

  /**
   * Определяет, существует ли константа перечисления с заданным именем.
   *
   * @param aName String - имя (название) искомой константы
   * @return boolean - признак существования константы <br>
   *         <b>true</b> - константа с заданным именем существует;<br>
   *         <b>false</b> - неет константы с таким именем.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isItemByName( String aName ) {
    return findByNameOrNull( aName ) != null;
  }

  // ----------------------------------------------------------------------------------
  // Методы поиска
  //

  /**
   * Возвращает константу по идентификатору или null.
   *
   * @param aId String - идентификатор искомой константы
   * @return EHistDataExtraInfo - найденная константа, или null если нет константы с таимк идентификатором
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EAdminCmdContextNames findByIdOrNull( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( EAdminCmdContextNames item : values() ) {
      if( item.id.equals( aId ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает константу по идентификатору или выбрасывает исключение.
   *
   * @param aId String - идентификатор искомой константы
   * @return EHistDataExtraInfo - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким идентификатором
   */
  public static EAdminCmdContextNames findById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

  /**
   * Возвращает константу по описанию или null.
   *
   * @param aDescription String - описание искомой константы
   * @return EHistDataExtraInfo - найденная константа, или null если нет константы с таким описанием
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EAdminCmdContextNames findByDescriptionOrNull( String aDescription ) {
    TsNullArgumentRtException.checkNull( aDescription );
    for( EAdminCmdContextNames item : values() ) {
      if( item.description.equals( aDescription ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает константу по описанию или выбрасывает исключение.
   *
   * @param aDescription String - описание искомой константы
   * @return EHistDataExtraInfo - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким описанием
   */
  public static EAdminCmdContextNames findByDescription( String aDescription ) {
    return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
  }

  /**
   * Возвращает константу по имени или null.
   *
   * @param aName String - имя искомой константы
   * @return EHistDataExtraInfo - найденная константа, или null если нет константы с таким именем
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static EAdminCmdContextNames findByNameOrNull( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EAdminCmdContextNames item : values() ) {
      if( item.nmName.equals( aName ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает константу по имени или выбрасывает исключение.
   *
   * @param aName String - имя искомой константы
   * @return EHistDataExtraInfo - найденная константа
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет константы с таким именем
   */
  public static EAdminCmdContextNames findByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
  }

}
