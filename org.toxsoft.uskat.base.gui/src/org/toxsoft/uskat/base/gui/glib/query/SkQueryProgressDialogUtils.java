package org.toxsoft.uskat.base.gui.glib.query;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.api.hqserv.ESkQueryState;
import org.toxsoft.uskat.core.api.hqserv.ISkAsynchronousQuery;

/**
 * GUI-поддержка выполнения запросов {@link ISkAsynchronousQuery} в
 *
 * @author mvk
 */
public class SkQueryProgressDialogUtils {

  /**
   * Объединяет списки темпоральных значений в один список
   *
   * @param aInputs {@link IList}&lt;{@link ITimedList}&gt; входные списки темпоральных значений.
   * @param <T> тип занчений
   * @return {@link ITimedList} выходной список
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static <T extends ITemporal<T>> TimedList<T> uniteTimeporalLists( IList<ITimedList<T>> aInputs ) {
    TsNullArgumentRtException.checkNull( aInputs );
    int size = 0;
    ElemArrayList<TemporalListWrapper<T>> wrappers = new ElemArrayList<>();
    for( ITimedList<T> list : aInputs ) {
      size = list.size();
      wrappers.add( new TemporalListWrapper<>( list ) );
    }
    int bundleSize = getBundleCapacity( size );
    TimedList<T> retValue = new TimedList<>( bundleSize );
    // Объединение значений по времени
    for( T value = nextValueOrNull( wrappers ); value != null; value = nextValueOrNull( wrappers ) ) {
      retValue.add( value );
    }
    return retValue;
  }

  /**
   * Выполняет запрос с монитором процесса выполнения.
   *
   * @param aQuery {@link ISkAsynchronousQuery} запрос для выполнения.
   * @param aInterval {@link IQueryInterval} интервал времени запроса
   * @param aMonitor {@link IProgressMonitor} монитор выполнения
   * @param aCancelProducer {@link ISkProgressCancelProducer} механизм отмены запроса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void execQueryWithProgress( ISkAsynchronousQuery aQuery, IQueryInterval aInterval,
      IProgressMonitor aMonitor, ISkProgressCancelProducer aCancelProducer ) {
    TsNullArgumentRtException.checkNulls( aQuery, aInterval, aMonitor, aCancelProducer );
    // Установка обработчика отмены выполнения запроса
    ISkProgressCancelHandler prevCancelHandler = aCancelProducer.setCancelHandler( () -> aQuery.cancel() );
    try {
      // Установка слушателя выполнения запроса
      aQuery.genericChangeEventer().addListener( aSource -> {
        if( !aQuery.equals( aSource ) ) {
          return;
        }
        aMonitor.setTaskName( aQuery.stateMessage() );
        LoggerUtils.defaultLogger().info( "query message: %s", aQuery.stateMessage() ); //$NON-NLS-1$
        if( aQuery.state() == ESkQueryState.READY || aQuery.state() == ESkQueryState.FAILED ) {
          synchronized (aQuery) {
            aQuery.notifyAll();
          }
        }
      } );
      try {
        synchronized (aQuery) {
          aQuery.exec( aInterval );
          aQuery.wait();
        }
      }
      catch( InterruptedException e ) {
        LoggerUtils.errorLogger().error( e );
      }
    }
    finally {
      // Восстановление обработчика отмены операции
      aCancelProducer.setCancelHandler( prevCancelHandler );
    }
  }

  /**
   * Выполняет запрос в прогресс-диалоге с возможностью отмены выполения.
   *
   * @param aShell {@link Shell} родительское окно
   * @param aDialogName String - имя диалога ожидания.
   * @param aQuery {@link ISkAsynchronousQuery} запрос для выполнения.
   * @param aInterval {@link IQueryInterval} интервал времени запроса
   * @param aTimeout long таймаут (мсек) максимальное время выполнения запроса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  // public static void execQueryByProgressDialog( Shell aShell, String aDialogName, ISkAsynchronousQuery aQuery,
  // IQueryInterval aInterval, long aTimeout ) {
  // TsNullArgumentRtException.checkNulls( aShell, aDialogName, aQuery, aInterval );
  //
  // final ProgressMonitorDialog dialog = new ProgressMonitorDialog( aShell ) {
  //
  // @Override
  // protected Control createDialogArea( Composite aParent ) {
  //
  // Control c = super.createDialogArea( aParent );
  // c.getShell().setText( aDialogName );
  // return c;
  // }
  //
  // @Override
  // protected void cancelPressed() {
  // aQuery.cancel();
  // }
  //
  // @Override
  // public int open() {
  // Thread thread = new Thread( () -> {
  // aShell.getDisplay().asyncExec( () -> {
  // if( aTimeout >= 0 ) {
  // progressIndicator.beginTask( 1000 );
  // }
  // else {
  // progressIndicator.beginAnimatedTask();
  // }
  // } );
  // if( aTimeout >= 0 ) {
  // long sleep = aTimeout / 1000;
  // for( int index = 0; index < 1000; index++ ) {
  // aShell.getDisplay().asyncExec( () -> {
  // if( progressIndicator.isDisposed() ) {
  // return;
  // }
  // progressIndicator.worked( 1 );
  // } );
  // try {
  // Thread.sleep( sleep );
  // }
  // catch( InterruptedException e ) {
  // LoggerUtils.errorLogger().error( e );
  // }
  // }
  // }
  // } );
  // thread.start();
  // return super.open();
  // }
  // };
  //
  // try {
  // dialog.run( true, true, aMonitor -> {
  // aQuery.genericChangeEventer().addListener( aSource -> {
  // if( !aQuery.equals( aSource ) ) {
  // return;
  // }
  // aMonitor.setTaskName( aQuery.stateMessage() );
  // LoggerUtils.defaultLogger().info( "query message: %s", aQuery.stateMessage() ); //$NON-NLS-1$
  // if( aQuery.state() == ESkQueryState.READY || aQuery.state() == ESkQueryState.FAILED ) {
  // synchronized (aQuery) {
  // aQuery.notifyAll();
  // }
  // }
  // } );
  // synchronized (aQuery) {
  // aQuery.exec( aInterval );
  // aQuery.wait();
  // }
  // } );
  // }
  // catch( InvocationTargetException | InterruptedException e ) {
  // LoggerUtils.errorLogger().error( e );
  // }
  // }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Проводит расчет максимального размера фрагмента (bundle capacity) для коллекций {@link ElemLinkedBundleList} для
   * эффективного доступа по индексу.
   *
   * @param aCollectionSize int размер коллекции
   * @return int размер фрагмента
   */
  private static int getBundleCapacity( int aCollectionSize ) {
    return Math.max( TsCollectionsUtils.MIN_BUNDLE_CAPACITY,
        Math.min( TsCollectionsUtils.MAX_BUNDLE_CAPACITY, aCollectionSize ) );
  }

  @SuppressWarnings( "unchecked" )
  private static <T extends ITemporal<T>> T nextValueOrNull( ElemArrayList<TemporalListWrapper<T>> aWrappers ) {
    TsNullArgumentRtException.checkNull( aWrappers );
    int foundIndex = -1;
    T foundValue = null;
    for( int index = 0, n = aWrappers.size(); index < n; index++ ) {
      TemporalListWrapper<T> wrapper = aWrappers.get( index );
      T value = (T)wrapper.value();
      if( value == null ) {
        continue;
      }
      if( foundValue != null && foundValue.timestamp() < value.timestamp() ) {
        continue;
      }
      foundValue = value;
      foundIndex = index;
    }
    if( foundValue != null ) {
      aWrappers.get( foundIndex ).next();
    }
    return foundValue;
  }

  private static class TemporalListWrapper<T extends ITemporal<T>> {

    private final Iterator<T> it;
    private T                 value;

    TemporalListWrapper( ITimedList<T> aList ) {
      TsNullArgumentRtException.checkNull( aList );
      it = aList.iterator();
      next();
    }

    ITemporal<T> value() {
      return value;
    }

    void next() {
      value = null;
      if( it.hasNext() ) {
        value = it.next();
      }
    }
  }
}
