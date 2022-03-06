package org.toxsoft.uskat.s5.server.transactions;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.basis.ITsCollection;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.s5.common.info.ITransactionInfo;
import org.toxsoft.uskat.s5.common.info.ITransactionsInfos;

/**
 * Информация о транзакциях выполняемых s5-сервером
 *
 * @author mvk
 */
public class S5TransactionInfos
    implements ITransactionsInfos, Serializable {

  private static final long  serialVersionUID = 157157L;
  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID        = "S5TransactionInfos"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5TransactionInfos> KEEPER =
      new AbstractEntityKeeper<>( S5TransactionInfos.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @SuppressWarnings( "unchecked" )
        @Override
        protected void doWrite( IStrioWriter aSw, S5TransactionInfos aEntity ) {
          aSw.writeLong( aEntity.commitCount() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.rollbackCount() );
          aSw.writeSeparatorChar();
          S5TransactionInfo.KEEPER.writeColl( aSw, (ITsCollection<S5TransactionInfo>)(Object)aEntity.openInfos(),
              false );
          S5TransactionInfo.KEEPER.writeColl( aSw, (ITsCollection<S5TransactionInfo>)(Object)aEntity.commitedInfos(),
              false );
          S5TransactionInfo.KEEPER.writeColl( aSw, (ITsCollection<S5TransactionInfo>)(Object)aEntity.rollbackedInfos(),
              false );
          S5TransactionInfo.KEEPER.writeColl( aSw, (ITsCollection<S5TransactionInfo>)(Object)aEntity.longTimeInfos(),
              false );
        }

        @SuppressWarnings( "unchecked" )
        @Override
        protected S5TransactionInfos doRead( IStrioReader aSr ) {
          IListEdit<S5TransactionInfo> openInfos = new ElemLinkedList<>();
          IListEdit<S5TransactionInfo> commitedInfos = new ElemLinkedList<>();
          IListEdit<S5TransactionInfo> rollbackedInfos = new ElemLinkedList<>();
          IListEdit<S5TransactionInfo> longTimeInfos = new ElemLinkedList<>();
          long commitCount = aSr.readLong();
          aSr.ensureSeparatorChar();
          long rollbackCount = aSr.readLong();
          aSr.ensureSeparatorChar();
          S5TransactionInfo.KEEPER.readColl( aSr, openInfos );
          S5TransactionInfo.KEEPER.readColl( aSr, commitedInfos );
          S5TransactionInfo.KEEPER.readColl( aSr, rollbackedInfos );
          S5TransactionInfo.KEEPER.readColl( aSr, longTimeInfos );
          return new S5TransactionInfos( commitCount, rollbackCount, //
              (ITsCollection<ITransactionInfo>)(Object)openInfos, //
              (ITsCollection<ITransactionInfo>)(Object)commitedInfos, //
              (ITsCollection<ITransactionInfo>)(Object)rollbackedInfos, //
              (ITsCollection<ITransactionInfo>)(Object)longTimeInfos );
        }
      };

  private final long                    commitCount;
  private final long                    rollbackCount;
  private final IList<ITransactionInfo> openInfos;
  private final IList<ITransactionInfo> commitedInfos;
  private final IList<ITransactionInfo> rollbackedInfos;
  private final IList<ITransactionInfo> longTimeInfos;

  /**
   * Конструктор
   *
   * @param aCommitCount long - количество подтвержденных транзакций
   * @param aRollbackCount long - количество отмененных транзакций
   * @param aOpenInfos {@link ITsCollection}&lt;{@link ITransactionInfo}&gt; - список описаний открытых транзакций
   * @param aCommitedInfos {@link ITsCollection}&lt;{@link ITransactionInfo}&gt; - список описаний последних завершенных
   *          транзакций
   * @param aRollbackedInfos {@link ITsCollection}&lt;{@link ITransactionInfo}&gt; - список описаний последних отменных
   *          транзакций
   * @param aLongTimeInfos {@link ITsCollection}&lt;{@link ITransactionInfo}&gt; - список описаний самых длительных
   *          транзакций с момента запуска сервера
   */
  public S5TransactionInfos( long aCommitCount, long aRollbackCount, ITsCollection<ITransactionInfo> aOpenInfos,
      ITsCollection<ITransactionInfo> aCommitedInfos, ITsCollection<ITransactionInfo> aRollbackedInfos,
      ITsCollection<ITransactionInfo> aLongTimeInfos ) {
    TsNullArgumentRtException.checkNulls( aOpenInfos, aCommitedInfos, aRollbackedInfos, aLongTimeInfos );
    commitCount = aCommitCount;
    rollbackCount = aRollbackCount;
    openInfos = new ElemArrayList<>( aOpenInfos );
    commitedInfos = new ElemArrayList<>( aCommitedInfos );
    rollbackedInfos = new ElemArrayList<>( aRollbackedInfos );
    longTimeInfos = new ElemArrayList<>( aLongTimeInfos );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ITransactionsInfos
  //
  @Override
  public long commitCount() {
    return commitCount;
  }

  @Override
  public long rollbackCount() {
    return rollbackCount;
  }

  @Override
  public IList<ITransactionInfo> openInfos() {
    return openInfos;
  }

  @Override
  public IList<ITransactionInfo> commitedInfos() {
    return commitedInfos;
  }

  @Override
  public IList<ITransactionInfo> rollbackedInfos() {
    return rollbackedInfos;
  }

  @Override
  public IList<ITransactionInfo> longTimeInfos() {
    return longTimeInfos;
  }

}
