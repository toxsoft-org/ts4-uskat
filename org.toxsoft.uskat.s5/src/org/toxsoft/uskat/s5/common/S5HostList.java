package org.toxsoft.uskat.s5.common;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

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
          aSw.writeChar( CHAR_SET_BEGIN );
          aSw.writeInt( aEntity.size() );
          aSw.writeChar( CHAR_ARRAY_BEGIN );
          for( int i = 0, n = aEntity.size(); i < n; i++ ) {
            S5Host v = aEntity.get( i );
            S5Host.KEEPER.write( aSw, v );
            if( i < n - 1 ) {
              aSw.writeChar( CHAR_ITEM_SEPARATOR );
            }
            aSw.writeEol();
          }
          aSw.writeChar( CHAR_ARRAY_END );
          aSw.writeChar( CHAR_SET_END );
        }

        @Override
        protected S5HostList doRead( IStrioReader aSr ) {
          aSr.ensureChar( CHAR_SET_BEGIN );
          S5HostList result = new S5HostList();
          if( aSr.readArrayBegin() ) {
            do {
              result.add( S5Host.KEEPER.read( aSr ) );
            } while( aSr.readArrayNext() );
          }
          aSr.ensureChar( CHAR_SET_END );
          return result;
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
