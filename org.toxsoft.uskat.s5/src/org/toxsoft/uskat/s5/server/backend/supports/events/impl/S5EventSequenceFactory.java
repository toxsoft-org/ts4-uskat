package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.events.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5HardConstants.*;

import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.av.utils.IParameterizedEdit;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5EventHardConstants;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceFactory;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementation;

/**
 * Фабрика формирования последовательности блоков событий
 *
 * @author mvk
 */
public class S5EventSequenceFactory
    extends S5SequenceFactory<SkEvent> {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор фабрики
   */
  private static final String ID = "events"; //$NON-NLS-1$

  /**
   * Конструктор
   *
   * @param aInitialConfig {@link IS5InitialImplementation} начальная, неизменяемая, проектно-зависимая конфигурация
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5EventSequenceFactory( IS5InitialImplementation aInitialConfig, ISkSysdescrReader aSysdescrReader ) {
    super( ID, STR_D_EVENTS_FACTORY, aInitialConfig, aSysdescrReader );
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5SequenceFactory
  //
  @Override
  protected IList<IS5SequenceTableNames> doTableNames() {
    return new ElemArrayList<>( //
        tableNames( S5EventBlock.class, S5EventBlob.class ) );
  }

  @Override
  public IParameterized doTypeInfo( Gwid aGwid ) {
    // Идентификатор класса
    String classId = aGwid.classId();
    // Описание класса
    ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
    // Для описания типа данного используются параметры описания события
    IParameterizedEdit typeInfo = new StridableParameterized( classId, classInfo.params() );
    IOptionSetEdit params = typeInfo.params();
    // Установка значений неизменяемых опций("защита от дурака")
    IS5SequenceHardConstants.OP_IS_SYNC.setValue( params, avBool( false ) );
    IS5SequenceHardConstants.OP_SYNC_DT.setValue( params, avInt( 1 ) );
    // Установка значений опций значениями подсистемы
    copyValue( params, IS5EventHardConstants.OP_BLOCK_IMPL_CLASS, IS5SequenceHardConstants.OP_BLOCK_IMPL_CLASS );
    copyValue( params, IS5EventHardConstants.OP_BLOB_IMPL_CLASS, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS );
    copyValue( params, IS5EventHardConstants.OP_BLOCK_SIZE_MIN, IS5SequenceHardConstants.OP_BLOCK_SIZE_MIN );
    copyValue( params, IS5EventHardConstants.OP_BLOCK_SIZE_MAX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MAX );
    copyValue( params, IS5EventHardConstants.OP_VALUE_SIZE_MAX, IS5SequenceHardConstants.OP_VALUE_SIZE_MAX );
    return typeInfo;
  }

  @Override
  public IS5SequenceEdit<SkEvent> doCreateSequence( Gwid aGwid, IQueryInterval aInterval,
      Iterable<IS5SequenceBlockEdit<SkEvent>> aEvents ) {
    return new S5EventSequence( this, aGwid, aInterval, aEvents );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public SkEvent[] doCreateValueArray( IParameterized aTypeInfo, int aSize ) {
    return new SkEvent[aSize];
  }

  @Override
  public Object doGetSyncDefaultValue( IParameterized aTypeInfo ) {
    // События всегда асинхронные
    throw new TsIllegalStateRtException();
  }

  @Override
  public Object doGetSyncNullValue( IParameterized aTypeInfo ) {
    // События всегда асинхронные
    throw new TsIllegalStateRtException();
  }

}
