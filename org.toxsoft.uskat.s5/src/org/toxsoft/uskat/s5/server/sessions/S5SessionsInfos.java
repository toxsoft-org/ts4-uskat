package org.toxsoft.uskat.s5.server.sessions;

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
import org.toxsoft.uskat.s5.common.info.IS5SessionsInfos;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;

/**
 * Реализация IS5SessionsInfos
 *
 * @author mvk
 */
public class S5SessionsInfos
    implements IS5SessionsInfos, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5SessionsInfos"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5SessionsInfos> KEEPER =
      new AbstractEntityKeeper<>( S5SessionsInfos.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @SuppressWarnings( "unchecked" )
        @Override
        protected void doWrite( IStrioWriter aSw, S5SessionsInfos aEntity ) {
          S5SessionInfo.KEEPER.writeColl( aSw, (ITsCollection<S5SessionInfo>)(Object)aEntity.openInfos(), false );
          S5SessionInfo.KEEPER.writeColl( aSw, (ITsCollection<S5SessionInfo>)(Object)aEntity.closeInfos(), false );
        }

        @SuppressWarnings( "unchecked" )
        @Override
        protected S5SessionsInfos doRead( IStrioReader aSr ) {
          IListEdit<S5SessionInfo> openSessions = new ElemLinkedList<>();
          IListEdit<S5SessionInfo> closeSessions = new ElemLinkedList<>();
          S5SessionInfo.KEEPER.readColl( aSr, openSessions );
          S5SessionInfo.KEEPER.readColl( aSr, closeSessions );
          return new S5SessionsInfos( (ITsCollection<IS5SessionInfo>)(Object)openSessions,
              (ITsCollection<IS5SessionInfo>)(Object)closeSessions );
        }
      };

  private final IList<IS5SessionInfo> openInfos;
  private final IList<IS5SessionInfo> closeInfos;

  /**
   * @param aOpenSessionInfos {@link ITsCollection}&lt; {@link IS5SessionInfo}&gt; список открытых сессий пользователя
   * @param aCloseSessionInfos {@link ITsCollection}&lt; {@link IS5SessionInfo}&gt; список закрытых сессий пользователя
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SessionsInfos( ITsCollection<IS5SessionInfo> aOpenSessionInfos,
      ITsCollection<IS5SessionInfo> aCloseSessionInfos ) {
    TsNullArgumentRtException.checkNulls( aOpenSessionInfos, aCloseSessionInfos );
    openInfos = new ElemArrayList<>( aOpenSessionInfos );
    closeInfos = new ElemArrayList<>( aCloseSessionInfos );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionsInfos
  //
  @Override
  public IList<IS5SessionInfo> openInfos() {
    return openInfos;
  }

  @Override
  public IList<IS5SessionInfo> closeInfos() {
    return closeInfos;
  }
}
