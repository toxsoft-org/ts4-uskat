package org.toxsoft.uskat.s5.client.remote.connection;

import static java.lang.String.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Реализация {@link IS5ClusterNodeInfo}
 *
 * @author mvk
 */
public final class S5ClusterNodeInfo
    implements IS5ClusterNodeInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5ClusterNodeInfo"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5ClusterNodeInfo> KEEPER =
      new AbstractEntityKeeper<>( S5ClusterNodeInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5ClusterNodeInfo aEntity ) {
          aSw.writeQuotedString( aEntity.clusterName() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.nodeName() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.address() );
          aSw.writeSeparatorChar();
          aSw.writeInt( aEntity.port() );
          aSw.writeSeparatorChar();
        }

        @Override
        protected S5ClusterNodeInfo doRead( IStrioReader aSr ) {
          String clusterName = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          String nodeName = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          String address = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          int port = aSr.readInt();
          aSr.ensureSeparatorChar();
          return new S5ClusterNodeInfo( clusterName, nodeName, address, port );
        }
      };

  private final String clusterName;
  private final String nodeName;
  private final String address;
  private final int    port;

  /**
   * Конструктор
   *
   * @param aClusterName String имя кластера
   * @param aNodeName String имя узла кластера
   * @param aAddress String сетевое имя или IP-адрес узла кластера
   * @param aPort int порт узла кластера
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ClusterNodeInfo( String aClusterName, String aNodeName, String aAddress, int aPort ) {
    clusterName = TsNullArgumentRtException.checkNull( aClusterName );
    nodeName = TsNullArgumentRtException.checkNull( aNodeName );
    address = TsNullArgumentRtException.checkNull( aAddress );
    port = aPort;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterNodeInfo
  //
  @Override
  public String clusterName() {
    return clusterName;
  }

  @Override
  public String nodeName() {
    return nodeName;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public int port() {
    return port;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return toString( this );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + clusterName.hashCode();
    result = TsLibUtils.PRIME * result + nodeName.hashCode();
    result = TsLibUtils.PRIME * result + address.hashCode();
    result = TsLibUtils.PRIME * result + (port ^ (port >>> 32));
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
    if( aObject instanceof IS5ClusterNodeInfo ) {
      return false;
    }
    IS5ClusterNodeInfo other = (IS5ClusterNodeInfo)aObject;
    if( !clusterName.equals( other.clusterName() ) ) {
      return false;
    }
    if( !nodeName.equals( other.nodeName() ) ) {
      return false;
    }
    if( !address.equals( other.address() ) ) {
      return false;
    }
    return port == other.port();
  }

  // ------------------------------------------------------------------------------------
  // Открытые вспомогательные методы
  //
  /**
   * Возвращает текстовое представление информации об узле кластера
   *
   * @param aNode {@link IS5ClusterNodeInfo} информация об узле
   * @return String текстовое представление информации об узле
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static String toString( IS5ClusterNodeInfo aNode ) {
    return format( "%s:%s[%s:%d]", aNode.clusterName(), aNode.nodeName(), aNode.address(), //$NON-NLS-1$
        Integer.valueOf( aNode.port() ) );
  }
}
