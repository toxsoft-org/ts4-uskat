package org.toxsoft.uskat.s5.server.sessions;

import static java.lang.String.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.*;

/**
 * Идентифицирующая сессию информация.
 * <p>
 * Это неизменяемый класс.
 *
 * @author hazard157
 * @author mvk
 */
public final class S5SessionIdentity {

  /**
   * Идентификатор для регистрации {@link #KEEPER}-а .
   */
  public static final String KEEPER_ID = "S5SessionIdentity"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя объектов типа {@link S5SessionIdentity}.
   */
  public static final IEntityKeeper<S5SessionIdentity> KEEPER = new Keeper();

  // mvk 2019-03-24
  // static {
  // // автоматическая регистрация хранителя
  // // TODO посмотреть, возможна ли ситуация обращения к сохраненному значению ДО загрузки этого класса
  // ValobjUtils.registerKeeperIfNone( KEEPER_ID, KEEPER );
  // }

  /**
   * Формат вывода сессии в текстовом виде
   */
  private static final String SESSION_TO_STRING_FORMAT = "[%s, login = %s, addr = %s:%d]"; //$NON-NLS-1$

  private final Skid       sessionID;
  private final String     login;
  private final long       openTime;
  private final String     remoteIp;
  private final int        remotePort;
  private final IOptionSet features;

  /**
   * Конструктор.
   *
   * @param aSessionID {@link Skid} - идентификатор сессии {@link ISkSession}
   * @param aLogin String - логин пользователя
   * @param aOpenTime long - момент времени создания этой сессии
   * @param aRemoteIp String - IP-адрес клиента (цифрового вида "127.0.0.1" или домен типа "localhost")
   * @param aRemotePort int - номер порта, с которого поделючился клиент
   * @param aFeatures {@link IOptionSet} список фич(особенностей) программы или сессии клиента
   */
  public S5SessionIdentity( Skid aSessionID, String aLogin, long aOpenTime, String aRemoteIp, int aRemotePort,
      IOptionSet aFeatures ) {
    TsNullArgumentRtException.checkNulls( aSessionID );
    TsNullArgumentRtException.checkNulls( aLogin );
    TsNullArgumentRtException.checkNulls( aRemoteIp );
    TsNullArgumentRtException.checkNulls( aFeatures );
    sessionID = aSessionID;
    login = aLogin;
    openTime = aOpenTime;
    remoteIp = aRemoteIp;
    remotePort = aRemotePort;
    features = aFeatures;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает внутрисерверный уникальный идентификатор сессии.
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession}
   */
  public Skid sessionID() {
    return sessionID;
  }

  /**
   * Возвращает имя (логин) пользователя.
   *
   * @return String - логин пользователя
   */
  public String getLogin() {
    return login;
  }

  /**
   * Возвращает время создания сессии.
   *
   * @return long - момент времени создания этой сессии
   */
  public long getOpenTime() {
    return openTime;
  }

  /**
   * Возвращает IP-адрес (или название домена) клиента.
   *
   * @return String - IP-адрес клиента (цифрового вида "127.0.0.1" или домен типа "localhost")
   */
  public String getRemoteIp() {
    return remoteIp;
  }

  /**
   * Возвращает номер порта клиента.
   *
   * @return int - номер порта, с которого поделючился клиент
   */
  public int getRemotePort() {
    return remotePort;
  }

  /**
   * Возвращает набор фич(особенностей) программы или сессии клиента
   *
   * @return {@link IOptionSet} набор особенностей
   */
  public IOptionSet getFeatures() {
    return features;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    String ip = (remoteIp.equals( TsLibUtils.EMPTY_STRING ) ? "???.???.???.???" : remoteIp); //$NON-NLS-1$
    Integer port = Integer.valueOf( remotePort );
    return format( SESSION_TO_STRING_FORMAT, sessionID, login, ip, port );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + sessionID.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    return (sessionID == ((S5SessionIdentity)aObject).sessionID());
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  static class Keeper
      extends AbstractEntityKeeper<S5SessionIdentity> {

    Keeper() {
      super( S5SessionIdentity.class, EEncloseMode.ENCLOSES_BASE_CLASS, null );
    }

    @Override
    public Class<S5SessionIdentity> entityClass() {
      return S5SessionIdentity.class;
    }

    @Override
    protected void doWrite( IStrioWriter aSw, S5SessionIdentity aEntity ) {
      Skid.KEEPER.write( aSw, aEntity.sessionID() );
      aSw.writeSeparatorChar();
      aSw.writeQuotedString( aEntity.getLogin() );
      aSw.writeSeparatorChar();
      aSw.writeTimestamp( aEntity.getOpenTime() );
      aSw.writeSeparatorChar();
      aSw.writeQuotedString( aEntity.getRemoteIp() );
      aSw.writeSeparatorChar();
      aSw.writeInt( aEntity.getRemotePort() );
      aSw.writeSeparatorChar();
      OptionSetKeeper.KEEPER.write( aSw, aEntity.getFeatures() );
    }

    @Override
    protected S5SessionIdentity doRead( IStrioReader aSr ) {
      Skid sessionID = Skid.KEEPER.read( aSr );
      aSr.ensureSeparatorChar();
      String login = aSr.readQuotedString();
      aSr.ensureSeparatorChar();
      long openTime = aSr.readTimestamp();
      aSr.ensureSeparatorChar();
      String repoteIp = aSr.readQuotedString();
      aSr.ensureSeparatorChar();
      int remotePort = aSr.readInt();
      aSr.ensureSeparatorChar();
      IOptionSet features = OptionSetKeeper.KEEPER.read( aSr );
      return new S5SessionIdentity( sessionID, login, openTime, repoteIp, remotePort, features );
    }

  }

}
