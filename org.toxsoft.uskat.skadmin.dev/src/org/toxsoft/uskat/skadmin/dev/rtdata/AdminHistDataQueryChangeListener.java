package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeListener;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;

import ru.uskat.core.api.rtdata.ISkHistDataQuery;

/**
 * Слушатель выполенения запроса
 *
 * @author mvk
 */
class AdminHistDataQueryChangeListener
    implements IGenericChangeListener {

  /**
   * Выполняемый запрос
   */
  private final ISkHistDataQuery query;

  /**
   * Обратный вызов выполняемого запроса
   */
  private final IAdminCmdCallback callback;

  /**
   * Конструктор
   *
   * @param aQuery {@link ISkHistDataQuery} выполняемый запрос
   * @param aCallback {@link IAdminCmdCallback} обратный вызов выполняемого запроса
   * @throws TsNullArgumentRtException аргумент = null
   */
  AdminHistDataQueryChangeListener( ISkHistDataQuery aQuery, IAdminCmdCallback aCallback ) {
    TsNullArgumentRtException.checkNulls( aQuery, aCallback );
    query = aQuery;
    callback = aCallback;
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeListener
  //
  @SuppressWarnings( "fallthrough" )
  @Override
  public void onGenericChangeEvent( Object aSource ) {
    TsInternalErrorRtException.checkFalse( query == aSource );
    String stateTime = TimeUtils.timestampToString( System.currentTimeMillis() );
    print( "\n" + MSG_CMD_QUERY_STATE, stateTime, query.state() ); //$NON-NLS-1$
    switch( query.state() ) {
      case PREPARED:
      case EXECUTING:
      case UNPREPARED:
        break;
      case READY:
        TimedList<AdminGwidValue> gwidValues = new TimedList<>();
        for( Gwid gwid : query.result().keys() ) {
          ITimedList<ITemporalAtomicValue> values = query.result().getByKey( gwid );
          for( ITemporalAtomicValue value : values ) {
            gwidValues.add( new AdminGwidValue( gwid, value ) );
          }
        }
        // Вывод на экран полученных значений
        for( AdminGwidValue gwidValue : gwidValues ) {
          String valueTime = TimeUtils.timestampToString( gwidValue.timestamp() );
          print( "\n" + MSG_CMD_READ_VALUE, valueTime, gwidValue.gwid(), gwidValue.value() ); //$NON-NLS-1$
        }
      case FAILED:
      case CLOSED:
        synchronized (query) {
          query.notifyAll();
        }
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
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
  private void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }
}
