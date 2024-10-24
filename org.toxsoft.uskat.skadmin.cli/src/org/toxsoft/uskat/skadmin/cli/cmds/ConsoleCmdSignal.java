package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminConsole.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.cli.*;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Команда консоли: 'Управление сигналами'
 *
 * @author mvk
 */
public class ConsoleCmdSignal
    extends AbstractConsoleCmd {

  /**
   * Таймаут(мсек) цикла ожидания
   */
  private static final int SLEEP_TIMEOUT = 1000;

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdSignal( IAdminConsole aConsole ) {
    super( aConsole );
    // Идентификатор сигнала
    addArg( ARG_SIGNAL_ID_ID, ARG_SIGNAL_ID_ALIAS, ARG_SIGNAL_ID_NAME, PT_SINGLE_STRING, ARG_SIGNAL_ID_DESCR );
    // Команда управления сигналом
    addArg( ARG_SIGNAL_CMD_ID, ARG_SIGNAL_CMD_ALIAS, ARG_SIGNAL_CMD_NAME, PT_SINGLE_STRING, ARG_SIGNAL_CMD_DESCR );
    // Значение сигнала
    addArg( ARG_SIGNAL_VALUE_ID, ARG_SIGNAL_VALUE_ALIAS, ARG_SIGNAL_VALUE_NAME,
        createType( STRING, avStr( ARG_SIGNAL_VALUE_DEFAULT ) ), ARG_SIGNAL_VALUE_DESCR );
    // Таймаут(мсек) удержания или ожидания сигнала или его значения
    addArg( ARG_SIGNAL_TIMEOUT_ID, ARG_SIGNAL_TIMEOUT_ALIAS, ARG_SIGNAL_TIMEOUT_NAME,
        createType( INTEGER, avInt( Integer.parseInt( ARG_SIGNAL_TIMEOUT_DEFAULT ) ) ), ARG_SIGNAL_TIMEOUT_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return SIGNAL_CMD_ID;
  }

  @Override
  public String alias() {
    return SIGNAL_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return SIGNAL_CMD_NAME;
  }

  @Override
  public String description() {
    return SIGNAL_CMD_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return PT_SINGLE_STRING;
  }

  @Override
  public String resultDescription() {
    return SIGNAL_RESULT_DESCR;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  protected void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    // Аргументы команды
    String id = argSingleValue( ARG_SIGNAL_ID_ID ).asString();
    ESignalCmd cmd = ESignalCmd.findById( argSingleValue( ARG_SIGNAL_CMD_ID ).asString().toLowerCase() );
    String value = argSingleValue( ARG_SIGNAL_VALUE_ID ).asString();
    long timeout = argSingleValue( ARG_SIGNAL_TIMEOUT_ID ).asInt() * 1000;
    // Проверка существования (если необходимо создание) каталога сигналов
    String signalDir = contextParamValue( CTX_APPLICATION_DIR ) + File.separator + SIGNALS_DIR;
    File dir = new File( signalDir );
    if( !dir.exists() ) {
      // Создание каталога
      dir.mkdir();
    }
    try {
      switch( cmd ) {
        case CREATE:
          // Создание сигнала
          createSignal( signalDir, id, value, timeout );
          break;
        case DELETE:
          // Удаление сигнала
          deleteSignal( signalDir, id, value, timeout );
          break;
        case WAIT:
          value = waitSignal( signalDir, id, value, timeout );
          resultOk( pvsStr( value ) );
          return;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
      resultOk( pvsStr( EMPTY_STRING ) );
    }
    catch( Exception e ) {
      addResultError( e );
      resultFail();
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    if( aArgId.equals( ARG_SIGNAL_CMD_ID ) ) {
      IListEdit<IPlexyValue> values = new ElemArrayList<>( ESignalCmd.values().length );
      for( int index = 0, n = ESignalCmd.values().length; index < n; index++ ) {
        values.add( pvsStr( ESignalCmd.values()[index] ) );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Создание сигнала
   * <p>
   * Если сигнал уже есть и таймаут <= 0 и aValue = пустая строка, то ничего не делает.
   *
   * @param aDir String каталог сигналов
   * @param aId String идентификатор сигнала
   * @param aValue String значение сигнала. Пустая строка: любое значение
   * @param aTimeout long время(мсек) удержания сигнала после его создания. <= 0: бесконечно
   * @throws InterruptedException прерывание потока выполнения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void createSignal( String aDir, String aId, String aValue, long aTimeout )
      throws InterruptedException {
    TsNullArgumentRtException.checkNulls( aDir, aId, aValue );
    File signal = new File( aDir + File.separator + aId );
    if( signal.exists() && aValue.equals( EMPTY_STRING ) ) {
      // Сигнал уже существует, значение может быть любым, ожидание бесконечно.
      return;
    }
    // Переписываем значение сигнала
    writeSignalValue( signal, aValue );
    // Если указан режим удержания, то реализуем его и потом удаляем сигнал
    if( aTimeout > 0 ) {
      Thread.sleep( aTimeout );
      // Удаляем сигнал
      signal.delete();
    }
  }

  /**
   * Удаление сигнала
   * <p>
   * Если сигнала нет, то ничего не делает
   *
   * @param aDir String каталог сигналов
   * @param aId String идентификатор сигнала
   * @param aValue String значение сигнала. Пустая строка: любое значение
   * @param aTimeout long время(мсек) удержания сигнала после его создания. <= 0: без ожидания
   * @throws InterruptedException прерывание потока выполнения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void deleteSignal( String aDir, String aId, String aValue, long aTimeout )
      throws InterruptedException {
    TsNullArgumentRtException.checkNulls( aDir, aId, aValue );
    long wait = 0;
    while( true ) {
      File file = new File( aDir + File.separator + aId );
      if( file.exists() ) {
        // Файл существует
        if( aValue.equals( EMPTY_STRING ) ) {
          // Ждем любой сигнал
          file.delete();
          return;
        }
        // Читаем текущее значение сигнала
        String value = readSignalValue( file );
        if( value.equals( aValue ) ) {
          // Получили требуемый сигнал
          file.delete();
          return;
        }
      }
      if( aTimeout <= 0 ) {
        // Ожидание сигнала не требуется
        return;
      }
      Thread.sleep( SLEEP_TIMEOUT );
      wait += SLEEP_TIMEOUT;
      if( aTimeout > 0 && wait >= aTimeout ) {
        throw new TsIllegalArgumentRtException( ERR_MSG_SIGNAL_DELETE_TIMEOUT, aId, Long.valueOf( aTimeout ) );
      }
    }
  }

  /**
   * Ожидание сигнала или его значения
   *
   * @param aDir String каталог сигналов
   * @param aId String идентификатор сигнала
   * @param aValue String значение сигнала. Пустая строка - любое значение сигнала.
   * @param aTimeout long время(мсек) ожидания сигнала или значения. <= 0: бесконечно
   * @return String значение сигнала в строковом виде
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws InterruptedException прерывание потока выполнения
   * @throws TsIllegalArgumentRtException невозможно получить сигнал с указанным значением
   */
  private static String waitSignal( String aDir, String aId, String aValue, long aTimeout )
      throws InterruptedException {
    TsNullArgumentRtException.checkNulls( aDir, aId, aValue );
    long wait = 0;
    while( true ) {
      File file = new File( aDir + File.separator + aId );
      if( file.exists() ) {
        // Файл существует. Читаем текущее значение сигнала
        String value = readSignalValue( file );
        if( aValue.equals( EMPTY_STRING ) ) {
          // Ждем любой сигнал
          return value;
        }
        if( value.equals( aValue ) ) {
          // Получили требуемый сигнал
          return value;
        }
      }
      Thread.sleep( SLEEP_TIMEOUT );
      wait += SLEEP_TIMEOUT;
      if( aTimeout > 0 && wait >= aTimeout ) {
        throw new TsIllegalArgumentRtException( ERR_MSG_SIGNAL_WAIT_TIMEOUT, aId, Long.valueOf( aTimeout ) );
      }
    }
  }

  /**
   * Читает значение сигнала
   *
   * @param aFile {@link File} файл сигнала
   * @param aValue String значение сигнала
   * @throws TsNullArgumentRtException любой аргумент = null;
   * @throws TsIoRtException ошибка ввода-вывода
   */
  private static void writeSignalValue( File aFile, String aValue ) {
    TsNullArgumentRtException.checkNulls( aFile, aValue );
    try( FileWriter fw = new FileWriter( aFile ) ) {
      ICharOutputStream chOut = new CharOutputStreamWriter( fw );
      IStrioWriter sw = new StrioWriter( chOut );
      sw.writeQuotedString( aValue );
    }
    catch( IOException e ) {
      throw new TsIoRtException( e );
    }
  }

  /**
   * Читает значение сигнала
   *
   * @param aFile {@link File} файл сигнала
   * @return String значение сигнала
   * @throws TsNullArgumentRtException любой аргумент = null;
   */
  private static String readSignalValue( File aFile ) {
    TsNullArgumentRtException.checkNull( aFile );
    try( ICharInputStreamCloseable chIn = new CharInputStreamFile( aFile ) ) {
      IStrioReader sr = new StrioReader( chIn );
      return sr.readQuotedString();
    }
  }

  /**
   * Команда управления сигналом
   */
  enum ESignalCmd
      implements IStridable {

    /**
     * Неизвестный (возможно пока) тип данных.
     */
    CREATE( "create", E_SIGNAL_N_CREATE, E_SIGNAL_D_CREATE ), //$NON-NLS-1$

    /**
     * Булевый тип данных, принимает одно из двух значений: true или false.<br>
     */
    DELETE( "delete", E_SIGNAL_N_DELETE, E_SIGNAL_D_DELETE ), //$NON-NLS-1$

    /**
     * Целое число, без ограничения диапазона.
     */
    WAIT( "wait", E_SIGNAL_N_WAIT, E_SIGNAL_D_WAIT ); //$NON-NLS-1$

    private final String id;
    private final String nmName;
    private final String description;

    /**
     * Создать константу с заданием всех инвариантов.
     *
     * @param aId String - идентифицирующее название константы
     * @param aName String - краткое удобовчитаемое название
     * @param aDescr String - отображаемое описание константы
     */
    ESignalCmd( String aId, String aName, String aDescr ) {
      id = aId;
      nmName = aName;
      description = aDescr;
    }

    // --------------------------------------------------------------------------
    // Реализация интерфейса INameable
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
     * @return ESignalCmd - найденная константа, или null если нет константы с таимк идентификатором
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static ESignalCmd findByIdOrNull( String aId ) {
      TsNullArgumentRtException.checkNull( aId );
      for( ESignalCmd item : values() ) {
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
     * @return EAtomicType - найденная константа
     * @throws TsNullArgumentRtException аргумент = null
     * @throws TsItemNotFoundRtException нет константы с таким идентификатором
     */
    public static ESignalCmd findById( String aId ) {
      return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
    }

    /**
     * Возвращает константу по описанию или null.
     *
     * @param aDescription String - описание искомой константы
     * @return EAtomicType - найденная константа, или null если нет константы с таким описанием
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static ESignalCmd findByDescriptionOrNull( String aDescription ) {
      TsNullArgumentRtException.checkNull( aDescription );
      for( ESignalCmd item : values() ) {
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
     * @return EAtomicType - найденная константа
     * @throws TsNullArgumentRtException аргумент = null
     * @throws TsItemNotFoundRtException нет константы с таким описанием
     */
    public static ESignalCmd findByDescription( String aDescription ) {
      return TsItemNotFoundRtException.checkNull( findByDescriptionOrNull( aDescription ) );
    }

    /**
     * Возвращает константу по имени или null.
     *
     * @param aName String - имя искомой константы
     * @return ESignalCmd - найденная константа, или null если нет константы с таким именем
     * @throws TsNullArgumentRtException аргумент = null
     */
    public static ESignalCmd findByNameOrNull( String aName ) {
      TsNullArgumentRtException.checkNull( aName );
      for( ESignalCmd item : values() ) {
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
     * @return ESignalCmd - найденная константа
     * @throws TsNullArgumentRtException аргумент = null
     * @throws TsItemNotFoundRtException нет константы с таким именем
     */
    public static ESignalCmd findByName( String aName ) {
      return TsItemNotFoundRtException.checkNull( findByNameOrNull( aName ) );
    }

  }

}
