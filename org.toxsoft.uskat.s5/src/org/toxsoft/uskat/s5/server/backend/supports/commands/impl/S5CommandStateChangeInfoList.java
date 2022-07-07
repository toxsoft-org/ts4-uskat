package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.core.api.cmdserv.DtoCommandStateChangeInfo;

/**
 * Список изменений состояний команд
 *
 * @author mvk
 */
public final class S5CommandStateChangeInfoList
    extends TimedList<DtoCommandStateChangeInfo> {

  private static final long serialVersionUID = 7652955374830855600L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SkCommandStateChanges"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5CommandStateChangeInfoList> KEEPER =
      new AbstractEntityKeeper<>( S5CommandStateChangeInfoList.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5CommandStateChangeInfoList aEntity ) {
          DtoCommandStateChangeInfo.KEEPER.writeColl( aSw, aEntity, false );
        }

        @Override
        protected S5CommandStateChangeInfoList doRead( IStrioReader aSr ) {
          S5CommandStateChangeInfoList coll = new S5CommandStateChangeInfoList();
          DtoCommandStateChangeInfo.KEEPER.readColl( aSr, coll );
          return coll;
        }
      };

}
