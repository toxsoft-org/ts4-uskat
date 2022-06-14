package org.toxsoft.uskat.core.api.cmdserv;

import java.io.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Chunk of information about command state single change.
 *
 * @author hazard157
 */
public final class DtoCommandStateChangeInfo
    implements ITimestampable, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<DtoCommandStateChangeInfo> KEEPER =
      new AbstractEntityKeeper<>( DtoCommandStateChangeInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, DtoCommandStateChangeInfo aEntity ) {
          aSw.writeAsIs( aEntity.cmdId() );
          aSw.writeSeparatorChar();
          SkCommandState.KEEPER.write( aSw, aEntity.state() );
        }

        @Override
        protected DtoCommandStateChangeInfo doRead( IStrioReader aSr ) {
          String commandId = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          SkCommandState state = SkCommandState.KEEPER.read( aSr );
          return new DtoCommandStateChangeInfo( commandId, state );
        }
      };

  private final String         commandId;
  private final SkCommandState state;

  /**
   * Конструктор.
   *
   * @param aCommandId String - идентифкатор экземпляра команды
   * @param aState {@link SkCommandState} - состояние команды
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public DtoCommandStateChangeInfo( String aCommandId, SkCommandState aState ) {
    commandId = StridUtils.checkValidIdPath( aCommandId );
    state = TsNullArgumentRtException.checkNull( aState );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ITimestampable
  //

  @Override
  public long timestamp() {
    return state.timestamp();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Возвращает идентифкатор экземпляра команды.
   *
   * @return String - идентифкатор экземпляра команды
   */
  public String cmdId() {
    return commandId;
  }

  /**
   * Возвращает состояние команды.
   *
   * @return {@link SkCommandState} - состояние команды
   */
  public SkCommandState state() {
    return state;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @Override
  public String toString() {
    return commandId;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoCommandStateChangeInfo that ) {
      return commandId.equals( that.commandId ) && state.equals( that.state );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + commandId.hashCode();
    result = TsLibUtils.PRIME * result + state.hashCode();
    return result;
  }

}