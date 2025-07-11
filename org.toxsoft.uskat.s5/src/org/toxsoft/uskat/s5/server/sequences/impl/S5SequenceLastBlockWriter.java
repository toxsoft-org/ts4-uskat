package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceUtils.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import java.util.*;

import javax.naming.*;
import javax.persistence.*;
import javax.transaction.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.server.sequences.writer.*;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.threads.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Реализация писателя значений последовательностей данных {@link IS5SequenceWriter}
 * <p>
 * Писатель использует тот факт, что чаще всего поступают новые значения данных, а не редактируются уже имеющиеся. Для
 * этого он хранит для каждого данного блок с последними записанными значениями и при поступлении новых данных
 * использует его, чтобы не обращаться к dbms. Если поступают значения которые редактируют "старые" значения, то
 * вызывается логика записи базового класса
 * <p>
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
class S5SequenceLastBlockWriter<S extends IS5Sequence<V>, V extends ITemporal<?>>
    extends S5SequenceLazyWriter<S, V> {

  /**
   * Карта последних блоков.<br>
   * Ключ: идентификатор данного.<br>
   * Значение: блок {@link IS5SequenceBlock}.
   */
  private final IMapEdit<Gwid, IS5SequenceBlock<V>> lastBlocks = new SynchronizedMap<>( new ElemMap<>() );

  /**
   * Карта последних блоков в транзакции.<br>
   * Ключ: идентификатор данного.<br>
   * Значение: блок {@link IS5SequenceBlock}.
   */
  private final IMapEdit<Gwid, IS5SequenceBlock<V>> txLastBlocks = new SynchronizedMap<>( new ElemMap<>() );

  /**
   * Создает писатель последовательностей
   *
   * @param aOwnerName String имя владельца писателя
   * @param aBackendCore {@link IS5BackendCoreSingleton} ядро бекенда сервера
   * @param aSequenceFactory {@link IS5SequenceFactory} фабрика последовательностей блоков
   * @param aConfiguration {@link IOptionSet} конфигурация подсистемы хранения данных/команд/событий.
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SequenceLastBlockWriter( String aOwnerName, IS5BackendCoreSingleton aBackendCore,
      IS5SequenceFactory<V> aSequenceFactory, IOptionSet aConfiguration ) {
    super( aOwnerName, aBackendCore, aSequenceFactory, aConfiguration );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Проводит запись последовательности данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства используемый для записи
   * @param aSequence S последовательность
   * @param aStatistics {@link S5SequenceWriteStat} редактируемая статистика
   * @param aThreadIndex int индекс записи (индекс потока или просто порядковый номер, для журнала)
   * @return {@link IS5SequenceBlock}&lt;V&gt; блок последних значений записаных в dbms. null: неопределен
   */
  @SuppressWarnings( "unchecked" )
  @Override
  protected IS5SequenceBlock<V> writeSequence( EntityManager aEntityManager, S aSequence,
      S5SequenceWriteStat aStatistics, int aThreadIndex ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSequence, aStatistics );

    // TODO: mvkd
    // if( aInfo.id().longValue() != 35 ) {
    // return null;
    // }

    // Статистика по dbms
    S5DbmsStatistics dbmsStat = aStatistics.dbmsStatistics();
    // Индекс потока. -1: запись без использования потоков
    Integer threadIndex = Integer.valueOf( aThreadIndex );

    // Вывод полученных блоков в журнал
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      List<IS5SequenceBlock<V>> blocks = new ArrayList<>();
      for( int index = 0, n = aSequence.blocks().size(); index < n; index++ ) {
        blocks.add( aSequence.blocks().get( index ) );
      }
      printBlocks( logger(), format( MSG_BLOCKS_RECEVIED, threadIndex, aSequence ), blocks );
    }
    // Дефрагментация блоков последовательности
    int unioned = ((IS5SequenceEdit<V>)aSequence).uniteBlocks().size();
    // Вывод блоков после дефрагментации в журнал
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) && unioned > 0 ) {
      List<IS5SequenceBlock<V>> blocks = new ArrayList<>();
      for( int index = 0, n = aSequence.blocks().size(); index < n; index++ ) {
        blocks.add( aSequence.blocks().get( index ) );
      }
      printBlocks( logger(), format( MSG_BLOCKS_UNIONED, threadIndex, aSequence, Integer.valueOf( unioned ) ), blocks );
    }

    // Идентификатор данного
    Gwid gwid = aSequence.gwid();
    // Параметризованное описание типа
    IParameterized typeInfo = aSequence.typeInfo();
    // Интервал последовательности для записи
    ITimeInterval sourceInterval = aSequence.interval();
    // Последний блок сохраненный в dmbs. null: неопределенно
    IS5SequenceBlock<V> lastBlock = lastBlocks.findByKey( gwid );
    if( lastBlock == null ) {
      long traceStartTime = System.currentTimeMillis();
      lastBlock = S5SequenceSQL.findLastBlock( aEntityManager, sequenceFactory(), gwid );
      int loadCount = (lastBlock != null ? 1 : 0);
      int loadTime = (int)(System.currentTimeMillis() - traceStartTime);
      // Статистика по загрузке блоков
      dbmsStat.addLoaded( loadCount, loadTime );
      Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
      if( lastBlock != null ) {
        lastBlocks.put( gwid, lastBlock );
        logger().warning( MSG_LAST_BLOCK_LOADED, threadIndex, gwid, lastBlock, time );
      }
      if( lastBlock == null ) {
        // Попытка загрузки последнего блока. Блок НЕ НАЙДЕН
        logger().warning( MSG_LAST_BLOCK_NOT_FOUND, threadIndex, gwid, time );
      }
    }
    if( lastBlock == null ) {
      logger().warning( MSG_NOT_LAST_BLOCK, threadIndex, gwid );
      // Нет последнего блока (это может быть если в базе еще не было значений этого данного)
      IS5SequenceBlock<V> retValue = super.writeSequence( aEntityManager, aSequence, aStatistics, aThreadIndex );
      if( retValue != null ) {
        // Сохраняем блок последних знаений в тразнакции
        txLastBlocks.put( gwid, retValue );
      }
      return retValue;
    }
    // Признак того, что запись проводится ДО последнего блока
    boolean writeBeforeLastBlock = (sourceInterval.endTime() < lastBlock.startTime());
    // Признак того, что запись проводится ПОСЛЕ последнего блока
    boolean writeAfterLastBlock = (lastBlock.endTime() < sourceInterval.startTime());
    // Признак того, что новые данные обновляют данные последнего блока
    boolean writeMerged = (!writeBeforeLastBlock && !writeAfterLastBlock && //
        lastBlock.startTime() <= sourceInterval.startTime());
    // Признак того, что последовательность новых значений начинается со значений которые есть в последнем блоке
    boolean writeMergeWithRepeatValues = false;
    // Проверка того, что значения в начале последовательности повторяют значения в последнем блоке (перехлест)
    if( writeMerged ) {
      IVrList resultList = S5SequenceUtils.sequenceStartWithValues( aSequence, lastBlock );
      writeMergeWithRepeatValues = (resultList.getWorstType() == EValidationResultType.OK);
      if( !writeMergeWithRepeatValues && logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        // Вывод журнала по измененным значениям последнего блока
        StringBuilder sb = new StringBuilder();
        for( VrlItem item : resultList.items() ) {
          sb.append( item.vr() + "\n" ); //$NON-NLS-1$
        }
        logger().debug( MSG_DETECT_CHANGES_LAST_VALUES, threadIndex, gwid, sb.toString() );
      }
    }
    if( writeMergeWithRepeatValues && lastBlock.endTime() == sourceInterval.endTime() ) {
      // Получены ТОЛЬКО те значения которые уже были сохранены в dbms
      int size = aSequence.blocks().size();
      aStatistics.addWriteBlocks( sourceInterval, size );
      return null;
    }
    if( writeAfterLastBlock || writeMergeWithRepeatValues ) {
      if( writeMergeWithRepeatValues ) {
        // Удаление из последовательности значений которые уже были в блоке последних значений
        // Признак того, что последовательность содержит синхронные значения
        boolean isSync = OP_IS_SYNC.getValue( typeInfo.params() ).asBool();
        // Интервал (мсек) синхронных значений. Для асинхронных: 1
        long syncDT = (isSync ? OP_SYNC_DT.getValue( typeInfo.params() ).asLong() : 1);
        // Новый интервал последовательности новых значений
        IQueryInterval newInterval = new QueryInterval( CSCE, lastBlock.endTime() + syncDT, sourceInterval.endTime() );
        // Вырезаем значения которые уже есть в последнем блоке и сохранены в dbms
        ((IS5SequenceEdit<V>)aSequence).setInterval( newInterval );
        logger().debug( MSG_REMOVE_REPEAT_VALUES, threadIndex, gwid, sourceInterval, newInterval );
        // Сохранение интервала
        sourceInterval = newInterval;
      }
      // Последний блок является предыдущим для новых значений. Добавляются только новые блоки данных
      logger().debug( MSG_ADD_NEW_BLOCKS, threadIndex, gwid, sourceInterval, lastBlock );
      int sbc = aSequence.blocks().size();
      if( sbc == 0 ) {
        // В целевой последовательности нет блоков
        aStatistics.addWriteBlocks( sourceInterval, 0 );
        // Значения не были записаны в dbms
        return null;
      }
      ElemArrayList<IS5SequenceBlock<V>> addedBlocks = new ElemArrayList<>( sbc );
      for( int index = 0; index < sbc; index++ ) {
        addedBlocks.add( aSequence.blocks().get( index ) );
      }
      int writedCount = aSequence.blocks().size();
      // Добавление/обновление блоков в dbms
      writeBlocksToDbms( aEntityManager, aSequence.blocks(), logger(), dbmsStat );
      // Вывод журнал
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Integer ac = Integer.valueOf( addedBlocks.size() );
        printBlocks( logger(), format( MSG_DBMS_ADDED, threadIndex, gwid, ac ), addedBlocks );
      }
      // Формирование статистики
      aStatistics.addWriteBlocks( sourceInterval, writedCount );
      // Блок последних значений сохраненных в dbms
      IS5SequenceBlock<V> retValue = aSequence.blocks().last();
      txLastBlocks.put( gwid, retValue );
      return retValue;
    }
    if( writeMerged ) {
      // Данные последнего блока редактируются новыми значениями, возможно добавляя новые блоки
      int sbc = aSequence.blocks().size();
      // Использование ElemArrayList из-за prepareDbmsBlocks которому это необходимо для производительности
      ElemArrayList<IS5SequenceBlock<V>> addedBlocks = new ElemArrayList<>( sbc );
      ElemArrayList<IS5SequenceBlock<V>> mergedBlocks = new ElemArrayList<>( sbc );
      ElemArrayList<IS5SequenceBlock<V>> removedBlocks = new ElemArrayList<>( sbc );
      ElemArrayList<IS5SequenceBlock<V>> blocks = new ElemArrayList<>();
      blocks.add( lastBlock );

      long st = lastBlock.startTime();
      long et = sourceInterval.endTime();
      IQueryInterval targetInterval = new QueryInterval( CSCE, st, et );
      Iterable<IS5SequenceBlockEdit<V>> targetBlocks = (Iterable<IS5SequenceBlockEdit<V>>)(Object)blocks;
      IS5SequenceEdit<V> target = sequenceFactory().createSequence( gwid, targetInterval, targetBlocks );
      // Редактирование последовательности
      target.edit( aSequence, removedBlocks );
      // Объединение последовательности (не требуется, так как растет последний блок и перегружает dbms)
      // removedBlocks.addAll( (IList<IS5SequenceBlock<V>>)(Object)target.uniteBlocks() );
      int writedCount = aSequence.blocks().size();

      // Синхронизация с dbms: удаление блоков
      removeBlocksFromDbms( aEntityManager, removedBlocks, dbmsStat );
      // Синхронизация с dbms: добавление/обновление блоков
      writeBlocksToDbms( aEntityManager, target.blocks(), logger(), dbmsStat );
      // Вывод журнал
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Integer ac = Integer.valueOf( addedBlocks.size() );
        Integer mc = Integer.valueOf( mergedBlocks.size() );
        Integer rc = Integer.valueOf( removedBlocks.size() );
        printBlocks( logger(), format( MSG_DBMS_ADDED, threadIndex, gwid, ac ), addedBlocks );
        printBlocks( logger(), format( MSG_DBMS_MERGED, threadIndex, gwid, mc ), mergedBlocks );
        printBlocks( logger(), format( MSG_DBMS_REMOVED, threadIndex, gwid, rc ), removedBlocks );
      }

      // Формирование статистики
      aStatistics.addWriteBlocks( sourceInterval, writedCount );
      // Блок последних значений сохраненных в dbms
      IS5SequenceBlock<V> retValue = target.blocks().last();
      txLastBlocks.put( gwid, retValue );
      return retValue;
    }
    // Новые данные начинаются до блока последних значений. Используем базовый алгоритм
    logger().warning( MSG_CANT_USE_LAST_BLOCK, threadIndex, gwid, sourceInterval, lastBlock );
    IS5SequenceBlock<V> retValue = super.writeSequence( aEntityManager, aSequence, aStatistics, aThreadIndex );
    // Удаляем последний блок, чтобы его запросить при следующей записи
    if( !writeBeforeLastBlock ) {
      IS5SequenceBlock<V> removedLastBlock = lastBlocks.removeByKey( gwid );
      logger().warning( MSG_REMOVE_LAST_BY_BEFORE, gwid, aSequence, removedLastBlock );
    }
    // TODO: планирование дефрагментации

    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение методов базового класса
  //
  @Override
  public void doWrite( EntityManager aEntityManager, IList<S> aSequences, S5SequenceWriteStat aStatistics ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSequences, aStatistics );
    if( aSequences.size() == 0 ) {
      // Частный случай. Нечего делать
      return;
    }
    // TODO: сделать опцию позволяющую менять режим записи (с потоками или без)
    // @formatter:off
    /*
    // Журнал для потоков
    ILogger logger = new Logger( Logger.getLogger( LastBlockWriteThread.class ) );
    // Фабрика менеджеров постоянства
    EntityManagerFactory entityManagerFactory = entityManagerFactory();
    // Список созданных менджеров постоянства
    IListEdit<AbstractSkObjectManager> ems = new ElemArrayList<>();
    try {
      // Исполнитель s5-потоков проверки данных
      S5WriteThreadExecutor executor = new S5WriteThreadExecutor( writeExecutor(), logger );
      for( int index = 0, n = aInfoes.size(); index < n; index++ ) {
        // Описания данного
        I info = aInfoes.get( index );
        // Последовательность
        S sequence = aSequences.get( index );
        // Менеджер постоянства
        AbstractSkObjectManager em = entityManagerFactory.createEntityManager();
        // Присоединение менджера к текущей транзакции
        em.joinTransaction();
        // Размещение в списке для последующего завершения
        ems.add( em );
        // Поток
        IS5WriteThread thread = new LastBlockWriteThread( em, info, sequence, aStatistics, logger );
        // Регистрация потока
        executor.add( thread );
      }
      // Запуск потоков (с ожиданием, с поднятием исключений на ошибках потоков)
      executor.run( true, true );
      addUnionCandidates( aInfoes );
    }
    finally {
      for( AbstractSkObjectManager em : ems ) {
        em.close();
      }
    }
    */
    //ВАРИАНТ 2
//    for( int index = 0, n = aInfoes.size(); index < n; index++ ) {
//      writeSequence( aEntityManager, aInfoes.get( index ), aSequences.get( index ), aStatistics, index );
//    }
    //@formatter:on
    // ВАРИАНТ 3
    // Исполнитель s5-потоков проверки данных
    S5WriteThreadExecutor executor = new S5WriteThreadExecutor( writeExecutor(), logger() );
    // Поток
    IS5WriteThread thread = new LastBlockWriteThread( aSequences, aStatistics, logger() );
    // Регистрация потока
    executor.add( thread );
    // Запуск потоков (с ожиданием, с поднятием исключений на ошибках потоков)
    executor.run( true, true );
    // Добавляем записанные данные на проверку дефрагментации
    addUnionCandidates( aSequences );
  }

  @Override
  protected void onUnionEvent( IOptionSet aArgs, IList<IS5SequenceFragmentInfo> aInfoes, ILogger aLogger ) {
    for( IS5SequenceFragmentInfo info : aInfoes ) {
      // Идентификатор данного
      Gwid gwid = info.gwid();
      // Последний блок значений данного
      IS5SequenceBlock<V> lastBlock = lastBlocks.findByKey( gwid );
      if( lastBlock == null ) {
        // В данный момент для данного не хранится последний блок значений
        continue;
      }
      if( info.interval().endTime() < lastBlock.startTime() ) {
        // Блок значений остается актуальным
        continue;
      }
      // Блок значений мог попасть под дефрагментацию. Удаляем его из своего списка
      lastBlocks.removeByKey( gwid );
      aLogger.warning( MSG_REMOVE_LAST_BY_UNION, info );
    }
  }

  @Override
  protected void onPartitionEvent( IOptionSet aConfiguration, IList<S5PartitionOperation> aOps, ILogger aLogger ) {
    if( lastBlocks == null ) {
      // Вызов операции обработки разделов из конструктора базового класса (до создания класса-наследника)
      return;
    }
    for( S5PartitionOperation op : aOps ) {
      if( op.removePartitions().size() == 0 ) {
        // Нет удаляемых разделов
        continue;
      }
      for( Gwid gwid : op.gwids() ) {
        // Последний блок значений данного
        IS5SequenceBlock<V> lastBlock = lastBlocks.findByKey( gwid );
        if( lastBlock == null ) {
          // В данный момент для данного не хранится последний блок значений
          continue;
        }
        if( op.removePartitions().last().interval().endTime() < lastBlock.startTime() ) {
          // Блок значений остается актуальным
          continue;
        }
        // Блок значений мог попасть под удаление. Удаляем его из своего списка
        lastBlocks.removeByKey( gwid );
        aLogger.warning( MSG_REMOVE_LAST_BY_REMOVE, lastBlock );
      }
    }
  }

  @Override
  public void changeTransactionStatus( IS5Transaction aTransaction ) {
    try {
      try {
        ETransactionStatus txStatus = aTransaction.getStatus();
        if( txStatus == ETransactionStatus.COMMITED || txStatus == ETransactionStatus.ROLLEDBACK ) {
          // Завершение транзакции
          IList<Gwid> gwids = aTransaction.findResource( TX_SEQUENCE_LOCKED_GWIDS );
          // Установка последних блоков связанных с транзакцией
          updateLastBlocksAfterTransaction( gwids, txStatus == ETransactionStatus.COMMITED );
        }
      }
      catch( Throwable e ) {
        // Неожиданная ошибка
        logger().error( e );
      }
    }
    finally {
      // Вызов базового класса
      super.changeTransactionStatus( aTransaction );
    }
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Проводит обновление последних блоков после завершения транзкации
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список идентификаторов записанных данных
   * @param aSuccess boolean <b>true</b> успешное завершение транзакции. <b>false</b> откат транзакции
   * @throws TsNullArgumentRtException аргумент = null
   */
  void updateLastBlocksAfterTransaction( IList<Gwid> aGwids, boolean aSuccess ) {
    TsNullArgumentRtException.checkNulls( aGwids );
    if( !aSuccess ) {
      // Откат транзакции
      for( Gwid gwid : aGwids ) {
        logger().debug( MSG_TX_ROLLBACK, gwid );
        txLastBlocks.removeByKey( gwid );
      }
      return;
    }
    // Успешное завершение транзакции
    for( Gwid gwid : aGwids ) {
      IS5SequenceBlock<V> txLastBlock = txLastBlocks.removeByKey( gwid );
      if( txLastBlock == null ) {
        continue;
      }
      // TODO: 2019-11-08 mvk ???
      // if( ((S5SequenceBlock<?, ?, ?>)txLastBlock).id() == null ) {
      // // Блок транзакции не имеет идентификатор и не может использован как последний блок
      // logger().warning( MSG_CANT_USE_TX_BLOCK, txLastBlock );
      // lastBlocks.remove( gwid );
      // continue;
      // }
      lastBlocks.put( gwid, txLastBlock );
      logger().debug( MSG_TX_COMMIT, gwid, txLastBlock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Асинхронная задача обновления данных последовательности данного
   *
   * @author mvk
   */
  protected class LastBlockWriteThread
      extends S5AbstractWriteThread {

    private final IList<S>            sequences;
    private final S5SequenceWriteStat stat;

    /**
     * Создание асинхронной задачи проверки блоков последовательности данных
     *
     * @param aSequences {@link IList}&lt;{@link S5Sequence}&gt; последовательность значений
     * @param aStatistics {@link S5SequenceWriteStat} статистика выполнения задачи
     * @param aLogger {@link ILogger} журнал
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    protected LastBlockWriteThread( IList<S> aSequences, S5SequenceWriteStat aStatistics, ILogger aLogger ) {
      super( aLogger );
      TsNullArgumentRtException.checkNulls( aSequences, aStatistics );
      sequences = aSequences;
      stat = aStatistics;
    }

    // ------------------------------------------------------------------------------------
    // API для наследников
    //

    // ------------------------------------------------------------------------------------
    // Реализация абстрактных методов S5AbstractThread
    //
    @Override
    protected void doRun() {
      GwidList gwids = new GwidList();
      for( S sequence : sequences ) {
        gwids.add( sequence.gwid() );
      }
      EntityManager em = entityManagerFactory().createEntityManager();
      try {
        // Блокировка доступа к данным (false: без проверки текущий транзакции)
        tryLockGwids( gwids, false );
        try {
          try {
            UserTransaction tx = InitialContext.doLookup( USER_TRANSACTION_JNDI );
            // Открываем транзакцию
            tx.begin();
            try {
              // Присоединение менеджера постоянства к транзкации
              em.joinTransaction();
              // Запись последовательностей
              for( S sequence : sequences ) {
                writeSequence( em, sequence, stat, 0 );
              }
              // Установка времени завершения записи (НЕ транзакции!)
              stat.setEndTime( System.currentTimeMillis() );
              // Завершаем транзакцию
              tx.commit();
              // Обновление последних блоков после успешного завершение транзакции
              updateLastBlocksAfterTransaction( gwids, true );
              // Вывод статистики
              String name = gwidsToString( gwids, 3 );
              Long dt = Long.valueOf( System.currentTimeMillis() - stat.createTime() );
              Long dw = Long.valueOf( stat.endTime() - stat.createTime() );
              Long c = Long.valueOf( stat.dataCount() );
              ITimeInterval wi = stat.writedBlockInterval();
              Long wc = Long.valueOf( stat.writedBlockCount() );
              logger().info( MSG_WRITE_SEQUENCE_TIME, name, dt, dw, c, wi, wc, stat.dbmsStatistics() );
            }
            catch( Throwable e ) {
              // Откат транзакции на любой ошибке
              tx.rollback();
              // Обновление последних блоков после отката транзакции
              updateLastBlocksAfterTransaction( gwids, false );
              throw e;
            }
          }
          catch( Throwable e ) {
            throw new TsInternalErrorRtException( e, ERR_WRITE_TASK, Integer.valueOf( gwids.size() ), cause( e ) );
          }
        }
        finally {
          unlockGwids( gwids );
        }
      }
      finally {
        em.close();
      }
    }

    @Override
    protected void doCancel() {
      // nop
    }

    // ------------------------------------------------------------------------------------
    // Внутренняя реализация
    //
  }
}
