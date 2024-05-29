package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link ISkLoggedUserInfo} implementation.
 *
 * @author mvk
 */
public class SkLoggedUserInfo
    implements ISkLoggedUserInfo {

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SkLoggedUserInfo"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<SkLoggedUserInfo> KEEPER =
      new AbstractEntityKeeper<>( SkLoggedUserInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, SkLoggedUserInfo aEntity ) {
          Skid.KEEPER.write( aSw, aEntity.userSkid );
          aSw.writeSeparatorChar();
          Skid.KEEPER.write( aSw, aEntity.roleSkid );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.authentificationType.id() );
        }

        @Override
        protected SkLoggedUserInfo doRead( IStrioReader aSr ) {
          Skid userSkid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          Skid roleSkid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          ESkAuthentificationType type = ESkAuthentificationType.getById( aSr.readQuotedString() );
          return new SkLoggedUserInfo( userSkid, roleSkid, type );
        }
      };

  private final Skid                    userSkid;
  private final Skid                    roleSkid;
  private final ESkAuthentificationType authentificationType;

  /**
   * Constructor.
   *
   * @param aUserSkid {@link Skid} user id.
   * @param aRoleSkid {@link Skid} role id.
   * @param aAuthentificationType {@link ESkAuthentificationType} authentification type used to login.
   * @throws TsNullArgumentRtException any arguments = null
   */
  public SkLoggedUserInfo( Skid aUserSkid, Skid aRoleSkid, ESkAuthentificationType aAuthentificationType ) {
    userSkid = TsNullArgumentRtException.checkNull( aUserSkid );
    roleSkid = TsNullArgumentRtException.checkNull( aRoleSkid );
    authentificationType = TsNullArgumentRtException.checkNull( aAuthentificationType );
  }

  // ------------------------------------------------------------------------------------
  // ISkLoggedUserInfo
  //
  @Override
  public Skid userSkid() {
    return userSkid;
  }

  @Override
  public Skid roleSkid() {
    return roleSkid;
  }

  @Override
  public ESkAuthentificationType authentificationType() {
    return authentificationType;
  }

}
