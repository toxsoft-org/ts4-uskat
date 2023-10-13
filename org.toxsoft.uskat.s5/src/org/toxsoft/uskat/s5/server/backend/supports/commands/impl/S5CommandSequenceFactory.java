package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.commands.impl.IS5Resources.*;
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
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.supports.commands.IS5CommandHardConstants;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceFactory;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementation;

/**
 * Фабрика формирования последовательности блоков команд
 *
 * @author mvk
 */
public class S5CommandSequenceFactory
    extends S5SequenceFactory<IDtoCompletedCommand> {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор фабрики
   */
  private static final String ID = "commands"; //$NON-NLS-1$

  /**
   * Конструктор
   *
   * @param aInitialConfig {@link IS5InitialImplementation} Начальная, неизменяемая, проектно-зависимая конфигурация
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5CommandSequenceFactory( IS5InitialImplementation aInitialConfig, ISkSysdescrReader aSysdescrReader ) {
    super( ID, STR_D_COMMANDS_FACTORY, aInitialConfig, aSysdescrReader );
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
        tableNames( S5CommandBlock.class, S5CommandBlob.class ) );
  }

  @Override
  public IParameterized doTypeInfo( Gwid aGwid ) {
    // Идентификатор класса
    String classId = aGwid.classId();
    // Описание класса
    ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
    // Для описания типа данного используются параметры класса объекта
    IParameterizedEdit typeInfo = new StridableParameterized( classId, classInfo.params() );
    IOptionSetEdit params = typeInfo.params();
    // Установка значений неизменяемых опций("защита от дурака")
    IS5SequenceHardConstants.OP_IS_SYNC.setValue( params, avBool( false ) );
    IS5SequenceHardConstants.OP_SYNC_DT.setValue( params, avInt( 1 ) );
    // Установка значений опций значениями подсистемы
    copyValue( params, IS5CommandHardConstants.OP_BLOCK_IMPL_CLASS, IS5SequenceHardConstants.OP_BLOCK_IMPL_CLASS );
    copyValue( params, IS5CommandHardConstants.OP_BLOB_IMPL_CLASS, IS5SequenceHardConstants.OP_BLOB_IMPL_CLASS );
    copyValue( params, IS5CommandHardConstants.OP_BLOCK_SIZE_MAX, IS5SequenceHardConstants.OP_BLOCK_SIZE_MAX );
    copyValue( params, IS5CommandHardConstants.OP_VALUE_SIZE_MAX, IS5SequenceHardConstants.OP_VALUE_SIZE_MAX );
    return typeInfo;
  }

  @Override
  public IS5SequenceEdit<IDtoCompletedCommand> doCreateSequence( Gwid aGwid, IQueryInterval aInterval,
      Iterable<IS5SequenceBlockEdit<IDtoCompletedCommand>> aCommands ) {
    return new S5CommandSequence( this, aGwid, aInterval, aCommands );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public IDtoCompletedCommand[] doCreateValueArray( IParameterized aTypeInfo, int aSize ) {
    return new IDtoCompletedCommand[aSize];
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
