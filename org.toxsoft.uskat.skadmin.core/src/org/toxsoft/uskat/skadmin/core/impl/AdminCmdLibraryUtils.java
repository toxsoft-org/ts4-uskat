package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.uskat.skadmin.core.impl.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.plugins.IAdminCmdLibraryPlugin.*;

import java.io.File;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedBundleList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.core.tslib.utils.plugins.IPluginInfo;
import org.toxsoft.core.tslib.utils.plugins.IPluginsStorage;
import org.toxsoft.core.tslib.utils.plugins.impl.PluginUtils;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.plugins.IAdminCmdLibraryPlugin;

/**
 * Точка входа в библиотеку
 *
 * @author mvk
 */
public class AdminCmdLibraryUtils {

  /**
   * Журнал
   */
  private static ILogger logger = getLogger( AdminCmdLibraryUtils.class );

  /**
   * Создать исполнителя команд библиотеки s5admin
   *
   * @param aInitLibraries {@link IList}&lt;{@link IAdminCmdLibrary}&gt; - список библиотек подключаемых прямым вызовом
   * @return {@link IAdminCmdLibrary} исполнитель команд
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException ошибка конфигурирования плагинов
   * @throws TsItemAlreadyExistsRtException команда плагина уже существует в библиотеке
   */
  public static IAdminCmdLibrary createAdminLibrary( IList<IAdminCmdLibrary> aInitLibraries ) {
    return new AdminCmdLibraryManager( aInitLibraries );
  }

  /**
   * Загружает из плагинов библиотеки команд для s5admin
   *
   * @param aPluginDir String каталог размещения плагинов.
   * @param aCheckExist boolean <b>true</b> проверять существование каталога. <b>false</b> не проверять существование
   *          каталога.
   * @return {@link IList}&lt;{@link IAdminCmdLibrary}&gt; список загруженных библиотек
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException не найден java-класс реализации плагина библиотеки
   */
  public static IList<IAdminCmdLibrary> loadPluginLibraries( String aPluginDir, boolean aCheckExist ) {
    TsNullArgumentRtException.checkNull( aPluginDir );
    IListEdit<IAdminCmdLibrary> libraries = new ElemLinkedBundleList<>();
    File pluginDir = new File( aPluginDir );
    if( !aCheckExist && !pluginDir.exists() ) {
      // Задан режим не выдавать ошибки при отсутствии каталога
      return libraries;
    }
    IPluginsStorage storage = PluginUtils.createPluginStorage( CMD_LIBRARY_PLUGIN_TYPE, pluginDir );
    for( IPluginInfo pluginInfo : storage.listPlugins() ) {
      try {
        // Загрузка плагина
        IAdminCmdLibraryPlugin plugin = (IAdminCmdLibraryPlugin)storage.createPluginInstance( pluginInfo.pluginId() );
        plugin.initPlugin( pluginInfo );
        libraries.add( plugin );
        logger.debug( MSG_LIBRARY_LOAD, plugin.getName() );
      }
      catch( Throwable e ) {
        throw new TsIllegalArgumentRtException( e, ERR_LIBRARY_LOAD, pluginInfo.pluginId(), pluginInfo.pluginType() );
      }
    }
    return libraries;
  }

  /**
   * Создать дочерний контекст
   * <p>
   * При создании все параметры родительского контекста копируются в дочерний контекст
   *
   * @param aParent {@link IAdminCmdContext} родительский контекст
   * @return {@link IAdminCmdContext} дочерний контекст
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IAdminCmdContext createChildContext( IAdminCmdContext aParent ) {
    TsNullArgumentRtException.checkNull( aParent );
    return new AdminCmdContext( aParent );
  }

  /**
   * Обновить параметры родительского контекста значениями параметров дочернего контекста
   * <p>
   * Обновлению подлежат только параметры с именами ИД-путь. Другие параметры игнорируются.
   * <p>
   * Если в родительском контексте нет параметра из дочернего контекста, то он игнорируется
   *
   * @param aParent {@link IAdminCmdContext} родительский контекст
   * @param aChild {@link IAdminCmdContext} дочерний контекс
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException контекст должен быть создан с помощью фабрики {@link AdminCmdLibraryUtils}.
   */
  public static void updateParentContext( IAdminCmdContext aParent, IAdminCmdContext aChild ) {
    TsNullArgumentRtException.checkNulls( aParent, aChild );
    if( !(aParent instanceof AdminCmdContext) ) {
      throw new TsIllegalArgumentRtException( ERR_WRONG_PARENT_IMPL );
    }
    // Обновление всех параметров без оповещения
    ((AdminCmdContext)aParent).update( aChild, false );
  }

  /**
   * Загружает параметры контекста выполнения команд из указанного файла.
   *
   * @param aFileName String имя файла
   * @param aContext {@link IAdminCmdContext} контекст.
   * @param aExistError <b>true</b> выдавать ошибку существования файла; <b>false</b> не выдавать ошибку существования
   *          файла.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsValidationFailedRtException ошибка доступа к файлу
   * @throws TsIllegalStateRtException ошибка десериализации параметров контекста
   */
  public static void readContextFromFile( String aFileName, IAdminCmdContext aContext, boolean aExistError ) {
    TsNullArgumentRtException.checkNulls( aFileName, aContext );
    File file = new File( aFileName );
    if( !aExistError && !file.exists() ) {
      // Файл не существует. Сообщать об ошибке не требуется
      return;
    }
    IAdminCmdContext context = AdminCmdContextKeeper.KEEPER.read( file );
    IStringList paramNames = context.paramNames();
    for( int index = 0, count = paramNames.size(); index < count; index++ ) {
      String paramName = paramNames.get( index );
      IPlexyValue paramValue = context.paramValue( paramName );
      aContext.setParamValue( paramName, paramValue );
    }
  }

  /**
   * Сохраняет параметры контекста выполнения команд в указанный файл.
   *
   * @param aFileName String имя файла
   * @param aContext {@link IAdminCmdContext} контекст.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsValidationFailedRtException ошибка доступа к файлу
   */
  public static void writeContextToFile( String aFileName, IAdminCmdContext aContext ) {
    TsNullArgumentRtException.checkNulls( aFileName, aContext );
    AdminCmdContextKeeper.KEEPER.write( new File( aFileName ), aContext );
  }

  /**
   * Возвращает список описаний аргументов объектных ссылок или значений {@link IAtomicValue}
   *
   * @param aArgDefs {@link IList}&lt;{@link IAdminCmdArgDef}&gt; список описаний аргументов
   * @param aReference boolean <b>true</b> возвращать описания аргументов объектных ссылок. <b>false</b> возвращать
   *          описания аргументов {@link IAtomicValue}
   * @return {@link IList}&lt;{@link IAdminCmdArgDef}&gt; список описаний аргументов
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IList<IAdminCmdArgDef> getArgDefs( IList<IAdminCmdArgDef> aArgDefs, boolean aReference ) {
    TsNullArgumentRtException.checkNull( aArgDefs );
    IListEdit<IAdminCmdArgDef> refArgs = new ElemArrayList<>( aArgDefs.size() );
    for( IAdminCmdArgDef argDef : aArgDefs ) {
      EPlexyKind kind = argDef.type().kind();
      if( aReference && kind.isReference() ) {
        refArgs.add( argDef );
      }
      if( !aReference && //
          (kind == EPlexyKind.SINGLE_VALUE || kind == EPlexyKind.VALUE_LIST || kind == EPlexyKind.OPSET) ) {
        refArgs.add( argDef );
      }
    }
    return refArgs;
  }

  /**
   * Ищет в указанном списке описаний команд описание команды указанной через идентификатор {@link IAdminCmdDef#id()}
   * или ее алиас {@link IAdminCmdDef#alias()}.
   *
   * @param aCmdDefs {@link IList}&lt;{@link IAdminCmdDef}&gt; список описаний команд
   * @param aCmdIdOrAlias String иденитфикатор команды или ее алиас
   * @return {@link IAdminCmdDef} описание команды. null: описание не найдено
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IAdminCmdDef findCmdDef( IList<IAdminCmdDef> aCmdDefs, String aCmdIdOrAlias ) {
    TsNullArgumentRtException.checkNull( aCmdDefs, aCmdIdOrAlias );
    for( IAdminCmdDef cmdDef : aCmdDefs ) {
      if( cmdDef.id().equals( aCmdIdOrAlias ) || cmdDef.alias().equals( aCmdIdOrAlias ) ) {
        return cmdDef;
      }
    }
    return null;
  }

  /**
   * Ищет в указанном списке описаний аргументов описание аргумента указанного через идентификатор
   * {@link IAdminCmdArgDef#id()} или его алиас {@link IAdminCmdArgDef#alias()}.
   *
   * @param aArgDefs {@link IList}&lt;{@link IAdminCmdArgDef}&gt; список описаний аргументов
   * @param aArgIdOrAlias String иденитфикатор аргумента или его алиас
   * @return {@link IAdminCmdArgDef} описание аргумента. null: описание не найдено
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IAdminCmdArgDef findArgDef( IList<IAdminCmdArgDef> aArgDefs, String aArgIdOrAlias ) {
    TsNullArgumentRtException.checkNull( aArgDefs, aArgIdOrAlias );
    for( IAdminCmdArgDef argDef : aArgDefs ) {
      if( argDef.id().equals( aArgIdOrAlias ) || argDef.alias().equals( aArgIdOrAlias ) ) {
        return argDef;
      }
    }
    return null;
  }

  /**
   * Возвращает строковое выражение списка строк
   *
   * @param aStringList IStringList - список строк
   * @param aEol boolean <b>true</b> делать переходы строк между элементами списка <b>false</b> не делать переходы
   *          строки
   * @param aIdent int отступ после перехода строки
   * @return String строковое выражение списка строк
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String getStringListStr( IStringList aStringList, boolean aEol, int aIdent ) {
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aStringList.size(); index < n; index++ ) {
      if( aEol ) {
        sb.append( CHAR_EOL );
        for( int ident = 0; ident < aIdent; ident++ ) {
          sb.append( CHAR_SPACE );
        }
      }
      sb.append( aStringList.get( index ) );
      if( index < n - 1 ) {
        sb.append( CHAR_ITEM_SEPARATOR );
      }
    }
    return sb.toString();
  }
}
