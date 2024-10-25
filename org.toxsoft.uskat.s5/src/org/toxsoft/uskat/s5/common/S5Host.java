package org.toxsoft.uskat.s5.common;

import static org.toxsoft.uskat.s5.common.IS5Resources.*;

import java.io.*;
import java.net.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.valobj.*;

/**
 * Адресная информация о хосте
 *
 * @author mvk
 */
public final class S5Host
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Value-object registration identifier for {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "Host"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<S5Host> KEEPER =
      new AbstractEntityKeeper<>( S5Host.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5Host aEntity ) {
          aSw.writeQuotedString( aEntity.address );
          aSw.writeSeparatorChar();
          aSw.writeInt( aEntity.port );
        }

        @Override
        protected S5Host doRead( IStrioReader aSr ) {
          String address = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          int port = aSr.readInt();
          return new S5Host( address, port );
        }
      };

  /**
   * Text format {@link S5Host}
   */
  private static final String TO_STRING_FORMAT = "%s:%d"; //$NON-NLS-1$

  private static final int MIN_PORT = 0;
  private static final int MAX_PORT = 65535;

  private final String address;
  private final int    port;

  /**
   * Конструктор
   *
   * @param aAddress String имя или IP-адрес хоста.
   * @param aPort int номер порта.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5Host( String aAddress, int aPort ) {
    TsNullArgumentRtException.checkNull( aAddress );
    address = aAddress;
    port = aPort;
  }

  /**
   * Возвращает имя или IP-адрес хоста.
   *
   * @return String адрес.
   */
  public String address() {
    return address;
  }

  /**
   * Возвращает номер порта.
   *
   * @return int номер порта.
   */
  public int port() {
    return port;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return String.format( TO_STRING_FORMAT, address, Integer.valueOf( port ) );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + address.hashCode();
    result = TsLibUtils.PRIME * result + port ^ (port >>> 32);
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
    S5Host other = (S5Host)aObject;
    if( !address.equals( other.address() ) ) {
      return false;
    }
    if( port != other.port() ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //

  /**
   * Создает текстовое представление списка хостов для ручно правки человекм.
   * <p>
   * Форма представления вида "192.168.0.1:8080; 127.0.0.1:473;localhost:21;...", то есть хосты
   * (IP-адрес-двоеточие-порт) разделенные точками с запятой. IP-адреса могут быть доемнными именами
   * <p>
   * Назначение - использоване в диалогах и панелях для ручного ввода хостов.
   *
   * @param aHosts {@link IList}&lt;{@link S5Host}&gt; - список хостов
   * @return String - текстовое представление
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static String hosts2str( IList<S5Host> aHosts ) {
    TsNullArgumentRtException.checkNull( aHosts );
    StringBuilder sb = new StringBuilder();
    for( S5Host h : aHosts ) {
      sb.append( h.address() );
      sb.append( ':' );
      sb.append( h.port() );
    }
    return sb.toString();
  }

  /**
   * Осуществляет обратный к {@link #hosts2str(IList)} разбор строки.
   *
   * @param aString String - текстовое представление
   * @return {@link IList}&lt;{@link S5Host}&gt; - список хостов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws StrioRtException неверный формат строки
   */
  public static IList<S5Host> str2hosts( String aString ) {
    TsNullArgumentRtException.checkNull( aString );
    if( aString.isBlank() ) {
      return IList.EMPTY;
    }
    IListEdit<S5Host> ll = new ElemArrayList<>();
    IStrioReader sr = new StrioReader( new CharInputStreamString( aString ) );
    while( sr.peekChar( EStrioSkipMode.SKIP_BYPASSED ) != IStrioHardConstants.CHAR_EOF ) {
      String address = sr.readUntilDelimiter();
      sr.ensureChar( ':' );
      int posrt = sr.readInt();
      ll.add( new S5Host( address, posrt ) );
    }
    return ll;
  }

  /**
   * Возвращает текствое представление коллекции описания узлов
   *
   * @param aHosts {@link IList}&lt;{@link S5Host}&gt; коллекция узлов
   * @param aCount int максимальное количество узлов выводимое в результат
   * @return String текстовое представление узлов
   */
  public static String hostsToString( IList<S5Host> aHosts, int aCount ) {
    TsNullArgumentRtException.checkNull( aHosts );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aHosts.size(); index < n; index++ ) {
      sb.append( aHosts.get( index ) );
      if( index + 1 >= aCount ) {
        if( index + 1 < n ) {
          sb.append( ',' );
        }
        sb.append( "...[" + (n - aCount) + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
        break;
      }
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    return sb.toString();
  }

  /**
   * Check arguments for S5Host instance creation.
   *
   * @param aAddress String - the network address of the S5 server host machine (like 127.0.0.1 or acme.com)
   * @param aPort int - the number of the TCP/UDP port of the S5 server host machine (like 8080)
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ValidationResult validateS5HostArgs( String aAddress, int aPort ) {
    TsNullArgumentRtException.checkNull( aAddress );
    if( aAddress.isBlank() ) {
      return ValidationResult.error( MSG_ERR_BLANK_ADDRESS );
    }
    try {
      new URI( "http://" + aAddress ).toURL(); //$NON-NLS-1$
    }
    catch( MalformedURLException | URISyntaxException ex ) {
      return ValidationResult.error( FMT_ERR_INV_ADDRESS_URL, aAddress, ex.getLocalizedMessage() );
    }
    if( aPort < MIN_PORT || aPort > MAX_PORT ) {
      return ValidationResult.error( FMT_ERR_INV_PORN_NO, Integer.valueOf( aPort ), //
          Integer.valueOf( MIN_PORT ), Integer.valueOf( MAX_PORT ) );
    }
    return ValidationResult.SUCCESS;
  }
}
