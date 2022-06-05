package org.toxsoft.uskat.skadmin.dev.lobs;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.lobs.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.lobs.IAdminHardResources.*;

import java.io.*;
import java.util.regex.Pattern;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.lobs.ISkLobService;
import ru.uskat.core.connection.ISkConnection;
import ru.uskat.legacy.IdPair;

/**
 * Команда s5admin: экспорт lob-данных (Large OBject)
 *
 * @author mvk
 */
public class AdminCmdExportClobs
    extends AbstractAdminCmd {

  /**
   * Обратный вызов выполняемой команды
   */
  private static IAdminCmdCallback callback;

  /**
   * Конструктор
   */
  public AdminCmdExportClobs() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Шаблон ({@link java.util.regex.Pattern}) идентификаторов
    addArg( ARG_PATTERN );
    // Выходная директория, в которой сохраняются CLOBы с именем файла IdPair.toString()
    addArg( ARG_OUTDIR );
    // Расширение файлов (без точки)
    addArg( ARG_EXTENSION );
    // Подтверждение по умолчанию
    addArg( ARG_FORCE );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_EXPORT_CLOBS_ID;
  }

  @Override
  public String alias() {
    return CMD_EXPORT_CLOBS_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_EXPORT_CLOBS_NAME;
  }

  @Override
  public String description() {
    return CMD_EXPORT_CLOBS_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    callback = aCallback;
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    try {
      ISkCoreApi coreApi = connection.coreApi();
      IAtomicValue idPattern = argSingleValue( ARG_PATTERN );
      IAtomicValue outdir = argSingleValue( ARG_OUTDIR );
      IAtomicValue extension = argSingleValue( ARG_EXTENSION );
      IAtomicValue force = argSingleValue( ARG_FORCE );
      if( !idPattern.isAssigned() ) {
        idPattern = avStr( ARG_PATTERN_DEFAULT );
      }
      if( !outdir.isAssigned() ) {
        outdir = avStr( ARG_OUTDIR_DEFAULT );
      }
      if( !extension.isAssigned() ) {
        extension = avStr( ARG_EXTENSION_DEFAULT );
      }
      try {
        // Проверка, при необходимости создание, каталога lobs
        File f = new File( ARG_OUTDIR_DEFAULT );
        // Проверка существования каталога
        if( !f.exists() ) {
          f.mkdir();
        }
        // Время начала выполнения команды запроса событий
        long startTime = System.currentTimeMillis();

        ISkLobService service = coreApi.lobService();
        // Список всех идентификаторов
        IList<IdPair> allIds = service.listILobIds();
        // Список идентификаторов загружаемых данных
        IListEdit<IdPair> ids = new ElemArrayList<>();
        for( int index = 0, n = allIds.size(); index < n; index++ ) {
          IdPair id = allIds.get( index );
          if( Pattern.matches( idPattern.asString(), id.pairId() ) ) {
            ids.add( id );
          }
        }
        for( int index = 0, n = ids.size(); index < n; index++ ) {
          IdPair id = ids.get( index );
          String pairId = id.pairId();
          // Соединение к файлу
          f = new File( outdir.asString(), pairId + '.' + extension.asString() );
          // Проверка существования файла
          if( f.exists() ) {
            // Последнее "китайское" предупреждение
            if( (!force.isAssigned() || !force.asBool()) && //
                !queryClientConfirm( warn( STR_FILE_ALREADY_EXIST, f ), false ) ) {
              continue;
            }
          }
          long st = System.currentTimeMillis();
          String clob = service.readClob( id );
          try( FileOutputStream output = new FileOutputStream( f );
              OutputStreamWriter writer = new OutputStreamWriter( output, CHARSET_DEFAULT ); ) {
            writer.write( clob );
          }
          long time = System.currentTimeMillis() - st;
          print( STR_EXPORT_CLOB, //
              TimeUtils.timestampToString( System.currentTimeMillis() ), //
              Integer.valueOf( index + 1 ), Integer.valueOf( n ), //
              getSizeStr( clob.length() ), id, Long.valueOf( time ) );
        }
        long delta = (System.currentTimeMillis() - startTime) / 1000;
        addResultInfo( '\n' + MSG_CMD_TIME, Long.valueOf( delta ) );
        resultOk();
      }
      catch( Throwable e ) {
        addResultError( e );
        resultFail();
      }
    }
    finally {
      connection = null;
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
    if( pxCoreApi == null ) {
      return IList.EMPTY;
    }
    // ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
    // ISkSysdescr sysdescr = coreApi.sysdescr();
    // ISkClassInfoManager classManager = sysdescr.classInfoManager();
    // ISkObjectService objService = coreApi.objService();
    // if( aArgId.equals( ARG_PATTERN.id() ) ) {
    // // Список всех классов
    // IStridablesList<ISkClassInfo> classInfos = classManager.listClasses();
    // // Подготовка списка возможных значений
    // IListEdit<IPlexyValue> values = new ElemArrayList<>( classInfos.size() );
    // // Тип значений
    // IPlexyType type = PT_STRING_NULLABLE;
    // for( int index = 0, n = classInfos.size(); index < n; index++ ) {
    // IDataValue dataValue = DvUtils.dvStr( classInfos.get( index ).id(), type.dataType() );
    // IPlexyValue plexyValue = pvSingleValue( type, dataValue );
    // values.add( plexyValue );
    // }
    // return values;
    // }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Вывести сообщение в callback клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }

  /**
   * Возвращает текстовое представление числа с суффиксами КB, MB, GB.
   *
   * @param aLength int число
   * @return String текстовое представление числа
   */
  @SuppressWarnings( { "boxing", "nls" } )
  static String getSizeStr( int aLength ) {
    if( aLength < 1000 ) {
      return String.valueOf( aLength );
    }
    if( aLength < 10000 ) {
      return String.format( "%.2f", (float)(aLength) / 1000 ) + "kB"; //$NON-NLS-1$
    }
    if( aLength < 100000 ) {
      return String.format( "%.1f", (float)(aLength) / 1000 ) + "kB"; //$NON-NLS-1$
    }
    if( aLength < 1000000 ) {
      return String.format( "%.0f", (float)(aLength) / 1000 ) + "kB"; //$NON-NLS-1$
    }
    if( aLength < 10000000 ) {
      return String.format( "%.2f", (float)(aLength) / 1000000 ) + "mB"; //$NON-NLS-1$
    }
    if( aLength < 100000000 ) {
      return String.format( "%.2f", (float)(aLength) / 1000000 ) + "mB"; //$NON-NLS-1$
    }
    if( aLength < 1000000000 ) {
      return String.format( "%.0f", (float)(aLength) / 1000000 ) + "mB"; //$NON-NLS-1$
    }
    return String.format( "%.3f", (float)(aLength) / 1000000000 ) + "gB"; //$NON-NLS-1$
  }
}
