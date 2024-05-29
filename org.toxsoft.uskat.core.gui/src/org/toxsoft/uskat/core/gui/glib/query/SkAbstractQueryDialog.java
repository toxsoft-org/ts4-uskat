package org.toxsoft.uskat.core.gui.glib.query;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.api.hqserv.ISkAsynchronousQuery;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants;

/**
 * Диалог прогресса выполнения операции
 *
 * @author mvk
 * @param <T> тип запроса
 */
public abstract class SkAbstractQueryDialog<T extends ISkAsynchronousQuery>
    extends SkProgressDialog {

  private ITsThreadExecutor threadExecutor;
  private T                 query;

  /**
   * Конструктор
   *
   * @param aShell {@link Shell} родительское окно
   * @param aDialogName String заголовок диалога
   * @param aTimeout long таймаут ожидания (мсек). < 0: бесконечно
   * @param aThreadExecutor {@link ITsThreadExecutor} испольнитель запросов в одном потоке
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkAbstractQueryDialog( Shell aShell, String aDialogName, long aTimeout, ITsThreadExecutor aThreadExecutor ) {
    super( aShell, aDialogName, aTimeout );
    threadExecutor = TsNullArgumentRtException.checkNull( aThreadExecutor );
  }

  /**
   * Возвращает запрос
   *
   * @return T запрос
   * @throws TsIllegalStateRtException не выполнялся
   */
  public T query() {
    return query;
  }

  /**
   * Выполнить запрос
   *
   * @param aInterval {@link IQueryInterval} интервал выполнения запроса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public void executeQuery( IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    // Параметры запроса
    IOptionSetEdit options = new OptionSet( OptionSetUtils.createOpSet( //
        ISkHistoryQueryServiceConstants.OP_SK_MAX_EXECUTION_TIME, AvUtils.avInt( timeout() ) //
    ) );
    // Создание запроса
    threadExecutor.syncExec( () -> query = doCreateQuery( options ) );
    try {
      // fork = true, cancelable = true
      super.run( true, true, aMonitor -> {
        execute( query, aInterval );
      } );
    }
    catch( InvocationTargetException | InterruptedException ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    finally {
      threadExecutor.syncExec( () -> query.close() );
    }
  }

  /**
   * Выполняет запрос с монитором процесса выполнения.
   *
   * @param aQuery {@link ISkAsynchronousQuery} запрос для выполнения.
   * @param aInterval {@link IQueryInterval} интервал времени запроса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void execute( T aQuery, IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aQuery, aInterval );
    // Подготовка выполнения запроса
    threadExecutor.syncExec( () -> doPrepareQuery( aQuery ) );
    // Установка обработчика отмены выполнения запроса
    setCancelHandler( () -> aQuery.cancel() );
    // Установка слушателя выполнения запроса
    aQuery.genericChangeEventer().addListener( aSource -> {
      if( !aQuery.equals( aSource ) ) {
        return;
      }
      getProgressMonitor().subTask( aQuery.stateMessage() );
      switch( aQuery.state() ) {
        case READY:
        case FAILED:
          // Завершение выполнения запроса
          synchronized (aQuery) {
            aQuery.notifyAll();
          }
          break;
        case CLOSED:
        case EXECUTING:
        case PREPARED:
        case UNPREPARED:
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
      LoggerUtils.defaultLogger().info( "query message: %s", aQuery.stateMessage() ); //$NON-NLS-1$
    } );
    // Асинхронное(!) выполнение запроса
    threadExecutor.asyncExec( () -> aQuery.exec( aInterval ) );
    // Ожидание выполнения
    synchronized (aQuery) {
      try {
        aQuery.wait();
      }
      catch( InterruptedException ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // abstract methods
  //
  protected abstract T doCreateQuery( IOptionSetEdit aOptions );

  protected abstract void doPrepareQuery( T aQuery );
}
