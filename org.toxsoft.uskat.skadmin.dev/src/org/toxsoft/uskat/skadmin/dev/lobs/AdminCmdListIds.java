package org.toxsoft.uskat.skadmin.dev.lobs;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.lobs.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.lobs.IAdminHardResources.*;

import java.io.*;
import java.util.regex.Pattern;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.lobs.ISkLobService;
import ru.uskat.core.connection.ISkConnection;
import ru.uskat.legacy.IdPair;

/**
 * Команда s5admin: Вывод списка всех имеющихся идентификаторов данных lob (Large OBject)
 *
 * @author mvk
 */
public class AdminCmdListIds
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdListIds() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Шаблон ({@link java.util.regex.Pattern}) идентификаторов
    addArg( ARG_PATTERN );
    // Имя файла в который выводится список идентификаторов
    addArg( ARG_OUTFILE );
    // Подтверждение по умолчанию
    addArg( ARG_FORCE );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_LISTIDS_ID;
  }

  @Override
  public String alias() {
    return CMD_LISTIDS_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_LISTIDS_NAME;
  }

  @Override
  public String description() {
    return CMD_LISTIDS_DESCR;
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
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    try {
      ISkCoreApi coreApi = connection.coreApi();
      IAtomicValue idPattern = argSingleValue( ARG_PATTERN );
      IAtomicValue outfile = argSingleValue( ARG_OUTFILE );
      IAtomicValue yes = argSingleValue( ARG_FORCE );
      if( !idPattern.isAssigned() ) {
        idPattern = avStr( ARG_PATTERN_DEFAULT );
      }
      if( !outfile.isAssigned() ) {
        outfile = avStr( ARG_OUTFILE_DEFAULT );
      }
      try {
        // Проверка, при необходимости создание, каталога lobs
        File dir = new File( ARG_OUTDIR_DEFAULT );
        // Проверка существования каталога
        if( !dir.exists() ) {
          dir.mkdir();
        }
        // Соединение к файлу
        File f = new File( outfile.asString() );
        // Проверка существования файла
        if( f.exists() ) {
          // Последнее "китайское" предупреждение
          if( (!yes.isAssigned() || !yes.asBool()) && //
              !queryClientConfirm( warn( STR_FILE_ALREADY_EXIST, f ), false ) ) {
            // Клиент отказался от продолжения
            addResultInfo( STR_CMD_CANCELLED_BY_USER, id() );
            resultFail();
            return;
          }
        }

        // Время начала выполнения команды запроса событий
        long startTime = System.currentTimeMillis();

        ISkLobService service = coreApi.lobService();
        IList<IdPair> ids = service.listILobIds();
        try( FileOutputStream output = new FileOutputStream( f );
            OutputStreamWriter writer = new OutputStreamWriter( output, CHARSET_DEFAULT ); ) {
          for( int index = 0, n = ids.size(); index < n; index++ ) {
            String pairId = ids.get( index ).pairId();
            if( !Pattern.matches( idPattern.asString(), pairId ) ) {
              // Не совместимо с регулярным выражением
              continue;
            }
            writer.write( pairId );
            addResultInfo( '\n' + pairId );
            if( index + 1 < n ) {
              writer.write( '\n' );
            }
          }
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
}
