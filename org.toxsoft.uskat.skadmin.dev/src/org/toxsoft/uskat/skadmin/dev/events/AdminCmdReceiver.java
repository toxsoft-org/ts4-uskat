package org.toxsoft.uskat.skadmin.dev.events;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardResources.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.impl.SkThreadExecutorService;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import core.tslib.bricks.threadexecutor.ITsThreadExecutor;

/**
 * Команда s5admin: прием событий системы
 *
 * @author mvk
 */
public class AdminCmdReceiver
    extends AbstractAdminCmd
    implements ISkEventHandler {

  /**
   * Обратный вызов выполняемой команды
   */
  private IAdminCmdCallback callback;

  /**
   * Метка получения последнего события
   */
  private long lastEventTimestamp = System.currentTimeMillis();

  /**
   * Конструктор
   */
  public AdminCmdReceiver() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
    // Идентификаторы принимаемых событий
    addArg( ARG_RECV_GWIDS );
    // Таймаут(мсек) ожидания событий
    addArg( ARG_RECV_TIMEOUT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_RECV_ID;
  }

  @Override
  public String alias() {
    return CMD_RECV_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_RECV_NAME;
  }

  @Override
  public String description() {
    return CMD_RECV_DESCR;
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
    // API сервера
    ISkCoreApi coreApi = argSingleRef( CTX_SK_CORE_API );
    ISkEventService eventService = coreApi.eventService();
    // Идентификаторы принимаемых событий
    GwidList gwids = new GwidList();
    for( String gwid : argStrList( ARG_RECV_GWIDS ) ) {
      gwids.add( Gwid.KEEPER.str2ent( gwid ) );
    }
    int timeout = argSingleValue( ARG_RECV_TIMEOUT ).asInt();

    ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( coreApi );
    // Установка обработчика событий
    eventService.registerHandler( gwids, this );
    // Таймер удаления обработчика событий
    threadExecutor.timerExec( timeout, () -> {
      eventService.unregisterHandler( AdminCmdReceiver.this );
      println( MSG_DEREG_EVENT_LISTENER, gwidsToStr( gwids ) );
    } );
    //
    println( MSG_REG_EVENT_LISTENER, Integer.valueOf( timeout ), gwidsToStr( gwids ) );
    // Команда выполняется после выхода еще timeout (msec)
    resultOk();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkEventHandler
  //
  @Override
  public void onEvents( ISkEventList aEvents ) {
    try {
      for( SkEvent event : aEvents ) {
        // Печать принятой команды
        println( MSG_RECV_EVENT, event );
        // Время получения команды
        lastEventTimestamp = System.currentTimeMillis();
      }
    }
    catch( Throwable e ) {
      logger().error( e );
    }
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
  private void println( String aMessage, Object... aArgs ) {
    print( aMessage + '\n', aArgs );
  }

  /**
   * Вывести сообщение в callback клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }

  /**
   * Вывод списка gwid в строку
   *
   * @param aGwids {@link IGwidList} список gwid
   * @return String текстовое представление списка
   */
  private static String gwidsToStr( IGwidList aGwids ) {
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aGwids.size(); index < n; index++ ) {
      sb.append( ' ' );
      sb.append( aGwids.get( index ) );
      sb.append( '\n' );
    }
    return sb.toString();
  }
}
