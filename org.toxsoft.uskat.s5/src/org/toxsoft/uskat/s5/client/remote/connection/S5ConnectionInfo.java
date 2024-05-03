package org.toxsoft.uskat.s5.client.remote.connection;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.common.S5Host.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.common.S5HostList;

/**
 * Реализация {@link IS5ConnectionInfo}.
 *
 * @author mvk
 */
public final class S5ConnectionInfo
    implements IS5ConnectionInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Формат текстового представления {@link S5ConnectionInfo}
   */
  private static final String TO_STRING_FORMAT = "%s"; //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<IS5ConnectionInfo> KEEPER =
      new AbstractEntityKeeper<>( IS5ConnectionInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IS5ConnectionInfo aEntity ) {
          S5HostList.KEEPER.write( aSw, aEntity.hosts() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.connectTimeout() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.failureTimeout() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.currDataTimeout() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.histDataTimeout() );
        }

        @Override
        protected IS5ConnectionInfo doRead( IStrioReader aSr ) {
          S5HostList hosts = S5HostList.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          S5ConnectionInfo retValue = new S5ConnectionInfo( hosts );
          retValue.setConnectTimeout( aSr.readLong() );
          aSr.ensureSeparatorChar();
          retValue.setFailureTimeout( aSr.readLong() );
          aSr.ensureSeparatorChar();
          retValue.setCurrDataTimeout( aSr.readLong() );
          aSr.ensureSeparatorChar();
          retValue.setHistDataTimeout( aSr.readLong() );
          return retValue;
        }
      };

  private final S5HostList hosts           = new S5HostList();
  private long             connectTimeout  = 5000;
  private long             failureTimeout  = 3000;
  private long             currDataTimeout = 1000;
  private long             histDataTimeout = 1000;

  /**
   * Конструктор описания
   *
   * @param aHosts {@link S5HostList} список хостов узлов s5-сервера
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aModuleName, aApiBeanName, aApiInterfaceName - пустая строка
   */
  public S5ConnectionInfo( S5HostList aHosts ) {
    TsNullArgumentRtException.checkNull( aHosts );
    hosts.setAll( aHosts );
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link IS5ConnectionInfo} исходные параметры
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ConnectionInfo( IS5ConnectionInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    hosts.setAll( aSource.hosts() );
    setConnectTimeout( aSource.connectTimeout() );
    setFailureTimeout( aSource.failureTimeout() );
    setCurrDataTimeout( aSource.currDataTimeout() );
    setHistDataTimeout( aSource.histDataTimeout() );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Установить список узлов s5-сервера
   *
   * @param aHosts {@link S5HostList} список узлов s5-сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setHosts( S5HostList aHosts ) {
    TsNullArgumentRtException.checkNull( aHosts );
    hosts.setAll( aHosts );
  }

  /**
   * Устанавливает таймаут ожидания соединения с сервером по истечении которого принимается решение, что соединение
   * невозможно
   *
   * @param aConnectTimeout long время (мсек)
   * @throws TsIllegalStateRtException aConnectTimeout <= 0
   */
  public void setConnectTimeout( long aConnectTimeout ) {
    TsIllegalStateRtException.checkTrue( aConnectTimeout <= 0 );
    connectTimeout = aConnectTimeout;
  }

  /**
   * Устанавливает таймаут ожидания ответов сервера по истечении которого принимается решение, что произошел обрыв связи
   * с сервером
   *
   * @param aFailureTimeout long время (мсек)
   * @throws TsIllegalStateRtException aFailureTimeout <= 0
   */
  public void setFailureTimeout( long aFailureTimeout ) {
    TsIllegalStateRtException.checkTrue( aFailureTimeout <= 0 );
    failureTimeout = aFailureTimeout;
  }

  /**
   * Установить рекомендуемый таймаут передачи текущих данных от сервера клиенту.
   * <p>
   * Значение по умолчанию: <b>1000(мсек)</b>
   *
   * @param aTimeout long таймаут (мсек). <=0: отправлять немедленно
   * @throws TsIllegalStateRtException редактор находится в нерабочем состоянии
   */
  public void setCurrDataTimeout( long aTimeout ) {
    currDataTimeout = aTimeout;
  }

  /**
   * Установить рекомендуемый таймаут передачи хранимых данных от сервера клиенту.
   * <p>
   * Значение по умолчанию: <b>10000(мсек)</b>
   *
   * @param aTimeout long таймаут (мсек). <=0: отправлять немедленно
   * @throws TsIllegalStateRtException редактор находится в нерабочем состоянии
   */
  public void setHistDataTimeout( long aTimeout ) {
    histDataTimeout = aTimeout;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ConnectionInfo
  //
  @Override
  public S5HostList hosts() {
    return hosts;
  }

  @Override
  public long connectTimeout() {
    return connectTimeout;
  }

  @Override
  public long failureTimeout() {
    return failureTimeout;
  }

  @Override
  public long currDataTimeout() {
    return currDataTimeout;
  }

  @Override
  public long histDataTimeout() {
    return histDataTimeout;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return format( TO_STRING_FORMAT, hostsToString( hosts, 3 ) );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + hosts.hashCode();
    result = TsLibUtils.PRIME * result + (int)(connectTimeout ^ (connectTimeout >>> 32));
    result = TsLibUtils.PRIME * result + (int)(failureTimeout ^ (failureTimeout >>> 32));
    result = TsLibUtils.PRIME * result + (int)(currDataTimeout ^ (currDataTimeout >>> 32));
    result = TsLibUtils.PRIME * result + (int)(histDataTimeout ^ (histDataTimeout >>> 32));
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
    IS5ConnectionInfo other = (IS5ConnectionInfo)aObject;
    if( !hosts.equals( other.hosts() ) ) {
      return false;
    }
    if( connectTimeout != other.connectTimeout() ) {
      return false;
    }
    if( failureTimeout != other.failureTimeout() ) {
      return false;
    }
    if( currDataTimeout != other.currDataTimeout() ) {
      return false;
    }
    if( histDataTimeout != other.histDataTimeout() ) {
      return false;
    }
    return true;
  }
}
