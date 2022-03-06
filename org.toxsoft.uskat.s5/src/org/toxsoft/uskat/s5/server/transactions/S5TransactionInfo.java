package org.toxsoft.uskat.s5.server.transactions;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.s5.common.info.ITransactionInfo;

/**
 * Реализация описания состояния транзакции
 *
 * @author mvk
 */
public class S5TransactionInfo
    implements ITransactionInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5TransactionInfo"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5TransactionInfo> KEEPER =
      new AbstractEntityKeeper<>( S5TransactionInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5TransactionInfo aEntity ) {
          aSw.writeQuotedString( aEntity.session() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.key() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.className() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.methodName() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.methodArgs() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.status().id() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.openTime() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.statusTime() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.closeTime() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.description() );
          aSw.writeSeparatorChar();
        }

        @Override
        protected S5TransactionInfo doRead( IStrioReader aSr ) {
          S5TransactionInfo retValue = new S5TransactionInfo();
          retValue.session = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          retValue.key = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          retValue.className = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          retValue.methodName = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          retValue.methodArgs = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          retValue.status = ETransactionStatus.findById( aSr.readQuotedString() );
          aSr.ensureSeparatorChar();
          retValue.openTime = aSr.readLong();
          aSr.ensureSeparatorChar();
          retValue.statusTime = aSr.readLong();
          aSr.ensureSeparatorChar();
          retValue.closeTime = aSr.readLong();
          aSr.ensureSeparatorChar();
          retValue.description = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          return retValue;
        }
      };

  private String             session     = TsLibUtils.EMPTY_STRING;
  private String             key         = TsLibUtils.EMPTY_STRING;
  private String             className   = TsLibUtils.EMPTY_STRING;
  private String             methodName  = TsLibUtils.EMPTY_STRING;
  private String             methodArgs  = TsLibUtils.EMPTY_STRING;
  private ETransactionStatus status      = ETransactionStatus.UNKNOWN;
  private long               openTime;
  private long               statusTime;
  private long               closeTime;
  private String             description = TsLibUtils.EMPTY_STRING;

  /**
   * Конструктор по умолчанию
   */
  S5TransactionInfo() {
  }

  /**
   * Конструктор
   *
   * @param aTransaction {@link S5Transaction} - транзакция s5-сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5TransactionInfo( S5Transaction aTransaction ) {
    update( aTransaction );
    session = aTransaction.getPrincipal();
    key = aTransaction.getKey().toString();
    className = aTransaction.getOwner().getClass().getName();
    methodName = aTransaction.getMethod().getName();
    StringBuilder sb = new StringBuilder();
    Object argValues[] = aTransaction.getMethodArgs();
    // 2020-12-21 mvk
    if( argValues != null ) {
      for( int index = 0, n = argValues.length; index < n; index++ ) {
        // Убираем возможные переходы строк
        // 2020-12-21 mvk
        // String argValue = argValues[index].toString().replaceAll( "\n", "<EOL>" ); //$NON-NLS-1$ //$NON-NLS-2$
        if( argValues[index] == null ) {
          sb.append( "null" ); //$NON-NLS-1$
        }
        if( argValues[index] != null ) {
          String argValue = argValues[index].toString().replaceAll( "\n", "<EOL>" ); //$NON-NLS-1$ //$NON-NLS-2$
          sb.append( argValue );
        }
        if( index < n - 1 ) {
          sb.append( "," ); //$NON-NLS-1$
        }
      }
    }
    methodArgs = sb.toString();
    openTime = aTransaction.openTime();
  }

  /**
   * Обновить параметры описания в соответствии с указанной транзакцией
   *
   * @param aTransaction {@link S5Transaction} - транзакция s5-сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  void update( S5Transaction aTransaction ) {
    TsNullArgumentRtException.checkNull( aTransaction );
    status = aTransaction.getStatus();
    statusTime = aTransaction.statusTime();
    closeTime = aTransaction.closeTime();
    description = aTransaction.getDescription();
  }

  // ------------------------------------------------------------------------------------
  // Реализация ITransactionInfo
  //
  @Override
  public String session() {
    return session;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public String className() {
    return className;
  }

  @Override
  public String methodName() {
    return methodName;
  }

  @Override
  public String methodArgs() {
    return methodArgs;
  }

  @Override
  public ETransactionStatus status() {
    return status;
  }

  @Override
  public long openTime() {
    return openTime;
  }

  @Override
  public long statusTime() {
    return statusTime;
  }

  @Override
  public long closeTime() {
    return closeTime;
  }

  @Override
  public String description() {
    return description;
  }

}
