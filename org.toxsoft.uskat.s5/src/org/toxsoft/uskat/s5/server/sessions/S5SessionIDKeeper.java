package org.toxsoft.uskat.s5.server.sessions;

import java.util.Base64;

import org.jboss.ejb.client.SessionID;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Хранитель объектов типа {@link SessionID}.
 *
 * @author mvk
 */
public class S5SessionIDKeeper
    extends AbstractEntityKeeper<SessionID> {

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SessionID"; //$NON-NLS-1$

  /**
   * Неопределенная сессия
   */
  public static final SessionID NULL_SESSION = SessionID.createSessionID( new byte[0] );

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<SessionID> KEEPER = new S5SessionIDKeeper();

  private S5SessionIDKeeper() {
    super( SessionID.class, EEncloseMode.NOT_IN_PARENTHESES, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //
  @Override
  protected void doWrite( IStrioWriter aSw, SessionID aEntity ) {
    if( aEntity == NULL_SESSION ) {
      aSw.writeQuotedString( TsLibUtils.EMPTY_STRING );
      return;
    }
    byte[] encodeForm = aEntity.getEncodedForm();
    String encodeFormString = Base64.getEncoder().encodeToString( encodeForm );
    aSw.writeQuotedString( encodeFormString );
  }

  @Override
  protected SessionID doRead( IStrioReader aSr ) {
    String encodeFormString = aSr.readQuotedString();
    if( TsLibUtils.EMPTY_STRING.equals( encodeFormString ) ) {
      return NULL_SESSION;
    }
    byte[] encodeForm = Base64.getDecoder().decode( encodeFormString );
    return SessionID.createSessionID( encodeForm );
  }
}
