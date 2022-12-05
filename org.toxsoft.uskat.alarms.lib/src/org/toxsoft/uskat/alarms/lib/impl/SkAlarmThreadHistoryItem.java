package org.toxsoft.uskat.alarms.lib.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.alarms.lib.ISkAlarmThreadHistoryItem;

/**
 * Этап истории обработки аларма.
 *
 * @author mvk
 */
public class SkAlarmThreadHistoryItem
    implements ISkAlarmThreadHistoryItem, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SkAlarmThreadHistoryItem"; //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<ISkAlarmThreadHistoryItem> KEEPER =
      new AbstractEntityKeeper<>( ISkAlarmThreadHistoryItem.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkAlarmThreadHistoryItem aEntity ) {
          aSw.writeTimestamp( aEntity.timestamp() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.announceThreadId() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.params() );
        }

        @Override
        protected ISkAlarmThreadHistoryItem doRead( IStrioReader aSr ) {
          long timestamp = aSr.readTimestamp();
          aSr.ensureSeparatorChar();
          String announceThreadId = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          return new SkAlarmThreadHistoryItem( timestamp, announceThreadId, params );
        }
      };

  /**
   * Метка времени этапа истории
   */
  private long timestamp;

  /**
   * идентификатор (ИД-путь) нитки извещения
   */
  private String threadId;

  /**
   * Параметры выполнения этапа.
   */
  private String params;

  /**
   * Создает элемент истории обработки аларма.
   *
   * @param aTimestamp long - метка времени
   * @param aThreadId идентификатор (ИД-путь) нитки извещения
   * @param aParams параметры - параметры выполнения этапа
   */
  public SkAlarmThreadHistoryItem( long aTimestamp, String aThreadId, IOptionSet aParams ) {
    timestamp = aTimestamp;
    threadId = aThreadId;
    params = OptionSetKeeper.KEEPER.ent2str( aParams );
  }

  /**
   * Конструктор без параметров
   */
  protected SkAlarmThreadHistoryItem() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarmThreadHistoryItem
  //

  @Override
  public long timestamp() {
    return timestamp;
  }

  @Override
  public String announceThreadId() {
    return threadId;
  }

  @Override
  public IOptionSet params() {
    IOptionSet result = OptionSetKeeper.KEEPER.str2ent( params );
    return result;
  }

}
