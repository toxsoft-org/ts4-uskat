package org.toxsoft.uskat.core.utils.ugwi;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.utils.ugwi.l10n.ITsUgwiSharedResources.*;

import java.io.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * UGWI stands for <b>U</b>Skat <b>G</b>reen <b>W</b>orld <b>I</b>dentifier.
 * <p>
 * UGWI is an address pointing to (referring to) some content, an entity in the Green World. UGWI implicitly assumes
 * existence of the addressed entity in the Green World (but not necessarily in the real Red World). Moreover, by
 * definition each and every GW entity has an UGWI identifier. Some entities may have several UGWIs of maybe different
 * kinds.
 * <p>
 * UGWI at a glance: <i>kindID</i><b>:/</b><i>namespace</i><b>/</b><i>essence</i>
 * <p>
 * Example of UGWI:<br>
 * <code><br>
 * none://<br>
 * rri:/com.example.uskat.server/sectionID/classId[objSTRID]$attr(attrID)<br>
 * url://http://example.com/cgi-bin/book.php?id="ISBN 0-486-27557-4"<br>
 * uri://ssh:admin@127.0.0.1:25252/home/user/.bashrc<br>
 * gwid:/ru.rusal.saz/pot[no243]$cmd(anode_up).arg(duration_secs)<br>
 * </code>
 * <p>
 * Ugwi consist of three main parts:
 * <ul>
 * <li><i>kindId</i> - the only mandatory part of the UGWI determines the kind of UGWI. The <i>kindId</i> must be an
 * IDpath. It is assumed that somewhere exists the UGWI kinds registry;</li>
 * <li><i>namespace</i> - determines optional context (domain) where to find entity pointed by the UGWI. Simplest
 * implementation of a <i>namespace</i> is the USkat server ID;</li>
 * <li><i>essence</i> - string is interpreted by the respective UGWI kind implementation. {@link Ugwi} does not knows
 * what the <i>essence</i> means. Note that leading and trailing spaces are not allowed for essence.</li>
 * </ul>
 * <p>
 *
 * @author hazard157
 */
public sealed class Ugwi
    implements Serializable, Comparable<Ugwi>
    permits InternalNoneUgwi {

  /**
   * The registered kind ID.
   */
  public static final String KIND_ID_NONE = "none"; //$NON-NLS-1$

  /**
   * The only instance of {@link Ugwi} of kind {@link #KIND_ID_NONE } exists as this singleton.
   */
  public static final Ugwi NONE = new InternalNoneUgwi();

  private static final long serialVersionUID = -4154282468275755540L;

  /**
   * The registered keeper ID.
   */
  public static final String KEEPER_ID = "Ugwi"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<Ugwi> KEEPER =
      new AbstractEntityKeeper<>( Ugwi.class, EEncloseMode.ENCLOSES_BASE_CLASS, NONE ) {

        @Override
        protected void doWrite( IStrioWriter aSw, Ugwi aEntity ) {
          aSw.writeAsIs( aEntity.kindId );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.namespace );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.essence );
        }

        @Override
        protected Ugwi doRead( IStrioReader aSr ) {
          String kindId = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          String namespace = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          String essence = aSr.readQuotedString();
          return new Ugwi( kindId, namespace, essence );
        }
      };

  private static final char CHAR_KIND_SEPARATOR      = ':';
  private static final char CHAR_NAMEPSACE_SEPARATOR = '/';

  // (de)serialize only canonicalString - other fields will be restored in readObject(ObjectInputStream)
  private final String canonicalString;

  private transient String kindId    = null;
  private transient String namespace = null;
  private transient String essence   = null;

  /**
   * Constructor for {@link #NONE} instance.
   */
  protected Ugwi() {
    kindId = KIND_ID_NONE;
    namespace = EMPTY_STRING;
    essence = EMPTY_STRING;
    canonicalString = KIND_ID_NONE;
  }

  private Ugwi( String aKindId, String aNamespace, String aEssence ) {
    kindId = aKindId;
    namespace = aNamespace;
    essence = aEssence;
    StringBuilder sb = new StringBuilder();
    sb.append( kindId );
    sb.append( CHAR_KIND_SEPARATOR );
    sb.append( CHAR_NAMEPSACE_SEPARATOR );
    sb.append( namespace );
    sb.append( CHAR_NAMEPSACE_SEPARATOR );
    sb.append( essence );
    canonicalString = sb.toString();
  }

  // ------------------------------------------------------------------------------------
  // creation
  //

  /**
   * Creates {@link Ugwi} instance.
   * <p>
   * Note: argument <code>aEssence</code> is trimmed using the method {@link String#trim()}.
   *
   * @param aKindId String - the kind ID (an IDpath)
   * @param aNamespace String - the namespace (an IDpath or an empty string)
   * @param aEssence - UGWI essence must be a quoted string
   * @return {@link Ugwi} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException kind is not an IDpath
   * @throws TsIllegalArgumentRtException kind is {@link #KIND_ID_NONE}
   * @throws TsIllegalArgumentRtException namespace is not an IDpath or an empty string
   */
  public static Ugwi of( String aKindId, String aNamespace, String aEssence ) {
    StridUtils.checkValidIdPath( aKindId );
    TsIllegalArgumentRtException.checkTrue( aKindId.equals( KIND_ID_NONE ) );
    TsNullArgumentRtException.checkNulls( aNamespace, aEssence );
    if( !aNamespace.isEmpty() ) {
      StridUtils.checkValidIdPath( aNamespace );
    }
    return new Ugwi( aKindId, aNamespace, aEssence.trim() );
  }

  /**
   * Creates {@link Ugwi} instance with empty namespace.
   *
   * @param aKindId String - the kind ID (an IDpath)
   * @param aEssence - UGWI essence must be a quoted string
   * @return {@link Ugwi} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException kind is {@link #KIND_ID_NONE}
   * @throws TsIllegalArgumentRtException kind is not an IDpath
   */
  public static Ugwi of( String aKindId, String aEssence ) {
    return of( aKindId, EMPTY_STRING, aEssence );
  }

  /**
   * Creates UGWI from canonical string.
   *
   * @param aStr String - the canonical string
   * @return {@link Ugwi} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException canonical string format violation
   */
  public static Ugwi fromCanonicalString( String aStr ) {
    String[] parts = parseCanonicalString( aStr );
    return new Ugwi( parts[0], parts[1], parts[2] );
  }

  private void readObject( ObjectInputStream aIns )
      throws IOException,
      ClassNotFoundException {
    aIns.defaultReadObject(); // only canonicalString is read
    String[] parts = parseCanonicalString( canonicalString );
    kindId = parts[0];
    TsInternalErrorRtException.checkTrue( kindId.equals( KIND_ID_NONE ) ); // NONE must not be here
    namespace = parts[1];
    essence = parts[2];
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * returns the UGWI kind ID.
   *
   * @return String UGWI kind ID, an IDpath
   */
  public String kindId() {
    return kindId;
  }

  /**
   * Returns the namespace of the UGWI.
   *
   * @return String - the namespace or the empty string
   */
  public String namespace() {
    return namespace;
  }

  /**
   * Determines if namespace is specified.
   *
   * @return boolean - <code>true</code> namespace is specified, <code>false</code> {@link #namespace()} is empty string
   */
  public boolean isNamespace() {
    return !namespace.isEmpty();
  }

  /**
   * Returns the essence (content of the UGWI specific to the kind) of the UGWI.
   * <p>
   * Interpretation of the UGWI string depends on the kind.
   *
   * @return String - the UGWI essence, may be an empty string
   */
  public String essence() {
    return essence;
  }

  /**
   * Returns the canonical string representation of the UGWI.
   *
   * @return String - UGWI canonical string
   */
  public String canonicalString() {
    return canonicalString;
  }

  // ------------------------------------------------------------------------------------
  // static API
  //

  /**
   * Checks if the string is a canonical string.
   * <p>
   * Method {@link #parseCanonicalString(String)} will return successfully if and only if this method does not returns
   * error,
   *
   * @param aS String - UGWI represented in the canonical form
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ValidationResult validateCanonicalString( String aS ) {
    TsNullArgumentRtException.checkNull( aS );
    int len = aS.length();
    // empty string stands for Ugwi.NONE
    if( len == 0 ) {
      return ValidationResult.SUCCESS;
    }
    // read kind ID
    int currIndex = 0;
    char ch = aS.charAt( currIndex++ );
    if( !StridUtils.isIdStart( ch ) ) {
      return ValidationResult.error( MSG_ERR_UGWI_KIND_NOT_IDPATH );
    }
    try {
      while( StridUtils.isIdPathPart( ch = aS.charAt( currIndex++ ) ) ) {
        // bypass kindId string
      }
      // here we had read kind ID and ch now must be a CHAR_KIND_SEPARATOR
      if( ch != CHAR_KIND_SEPARATOR ) {
        return ValidationResult.error( MSG_ERR_INV_UGWI_NO_COLON );
      }
      // first CHAR_NAMEPSACE_SEPARATOR after colon
      ch = aS.charAt( currIndex++ );
      if( ch != CHAR_NAMEPSACE_SEPARATOR ) {
        return ValidationResult.error( MSG_ERR_INV_UGWI_NO_FIRST_SLASH );
      }
      // start reading namespace
      ch = aS.charAt( currIndex++ );
      if( !StridUtils.isIdStart( ch ) ) {
        return ValidationResult.error( MSG_ERR_UGWI_NAMESPACE_NOT_IDPATH );
      }
      while( StridUtils.isIdPathPart( ch = aS.charAt( currIndex++ ) ) ) {
        // bypass namespace string
      }
      // here we had read namespace and ch now must be second CHAR_NAMEPSACE_SEPARATOR, after namepsace
      if( ch != CHAR_NAMEPSACE_SEPARATOR ) {
        return ValidationResult.error( MSG_ERR_INV_UGWI_NO_SECOND_SLASH );
      }
      // read essence (may be an empty string) until end of input string
      return ValidationResult.SUCCESS;
    }
    catch( @SuppressWarnings( "unused" ) IndexOutOfBoundsException ex ) {
      return ValidationResult.error( MSG_ERR_INV_UGWI_UNEXPECTED_EOL );
    }
  }

  /**
   * Parses canonical string of the UGWI.
   *
   * @param aS String - UGWI represented in the canonical form
   * @return String[] - parts of UGWI, indexes 0- kindId, 1- namespace, 2- essence
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException canonical form violation encountered
   */
  public static String[] parseCanonicalString( String aS ) {
    TsNullArgumentRtException.checkNull( aS );
    String[] parts = new String[3];
    parts[0] = parts[1] = parts[2] = EMPTY_STRING;
    int len = aS.length();
    // empty string stands for Ugwi.NONE
    if( len == 0 ) {
      parts[0] = KIND_ID_NONE;
      return parts;
    }
    // read kind ID
    StringBuilder sb = new StringBuilder( 2 * len );
    int currIndex = 0;
    char ch = aS.charAt( currIndex++ );
    TsIllegalArgumentRtException.checkFalse( StridUtils.isIdStart( ch ) );
    sb.append( ch );
    while( StridUtils.isIdPathPart( ch = aS.charAt( currIndex++ ) ) ) {
      sb.append( ch );
    }
    // here we had read kind ID and ch now must be a CHAR_KIND_SEPARATOR
    parts[0] = sb.toString();
    sb.setLength( 0 );
    TsIllegalArgumentRtException.checkTrue( ch != CHAR_KIND_SEPARATOR );
    // first CHAR_NAMEPSACE_SEPARATOR after colon
    ch = aS.charAt( currIndex++ );
    TsIllegalArgumentRtException.checkTrue( ch != CHAR_NAMEPSACE_SEPARATOR );
    // start reading namespace
    ch = aS.charAt( currIndex++ );
    TsIllegalArgumentRtException.checkFalse( StridUtils.isIdStart( ch ) );
    sb.append( ch );
    while( StridUtils.isIdPathPart( ch = aS.charAt( currIndex++ ) ) ) {
      sb.append( ch );
    }
    // here we had read namespace and ch now must be second CHAR_NAMEPSACE_SEPARATOR, after namepsace
    parts[1] = sb.toString();
    sb.setLength( 0 );
    TsIllegalArgumentRtException.checkTrue( ch != CHAR_NAMEPSACE_SEPARATOR );
    // read essence (may be an empty string) until end of input string
    while( currIndex < len ) {
      sb.append( aS.charAt( currIndex++ ) );
    }
    String s = sb.toString();
    parts[2] = TsMiscUtils.fromQuotedLine( s );
    return parts;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return canonicalString;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof Ugwi that ) {
      return canonicalString.equals( that.canonicalString );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return canonicalString.hashCode();
  }

  // ------------------------------------------------------------------------------------
  // Comparable
  //

  @Override
  public int compareTo( Ugwi aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    return canonicalString.compareTo( aThat.canonicalString );
  }

}

/**
 * {@link Ugwi#NONE} implementation class.
 *
 * @author hazard157
 */
final class InternalNoneUgwi
    extends Ugwi {

  private static final long serialVersionUID = -5989512487750645909L;

  /**
   * Method correctly deserializes {@link Ugwi#NONE} value.
   *
   * @return {@link ObjectStreamException} - {@link Ugwi#NONE}
   * @throws ObjectStreamException is declared but newer thrown by this method
   */
  @SuppressWarnings( "static-method" )
  private Object readResolve()
      throws ObjectStreamException {
    return Ugwi.NONE;
  }

}
