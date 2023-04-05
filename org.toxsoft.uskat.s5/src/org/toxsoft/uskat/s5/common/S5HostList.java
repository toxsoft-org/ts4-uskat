package org.toxsoft.uskat.s5.common;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.valobj.*;

/**
 * Список {@link S5Host}
 *
 * @author mvk
 */
public final class S5HostList
    extends ElemLinkedList<S5Host> {

  private static final long serialVersionUID = 157157L;

  /**
   * Value-object registration identifier for {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "HostList"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<S5HostList> KEEPER =
      new AbstractEntityKeeper<>( S5HostList.class, EEncloseMode.ENCLOSES_KEEPER_IMPLEMENTATION, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5HostList aEntity ) {
          S5Host.KEEPER.writeColl( aSw, aEntity, false );
        }

        @Override
        protected S5HostList doRead( IStrioReader aSr ) {
          IList<S5Host> ll = S5Host.KEEPER.readColl( aSr );
          return new S5HostList( ll );
        }
      };

  /**
   * Constructor.
   */
  public S5HostList() {
    // nop
  }

  /**
   * The copy constructor.
   *
   * @param aSource {@link IList}&lt;{@link S5Host}&gt; - the list of hosts
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5HostList( IList<S5Host> aSource ) {
    setAll( aSource );
  }

  /**
   * Возвращает текстовое представление описания хостов
   *
   * @param aHosts {@link S5HostList} описание хостов
   * @param aPort boolean <b>true</b> выводить номера портов; <b>false</b> не выводить номера портов.
   * @return String текстовое представление описания хостов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static String hostsToString( S5HostList aHosts, boolean aPort ) {
    TsNullArgumentRtException.checkNull( aHosts );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aHosts.size(); index < n; index++ ) {
      S5Host host = aHosts.get( index );
      sb.append( host.address() );
      if( aPort ) {
        sb.append( ':' );
        sb.append( host.port() );
      }
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    return sb.toString();
  }
}
