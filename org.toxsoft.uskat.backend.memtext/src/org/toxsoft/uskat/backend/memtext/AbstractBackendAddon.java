package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * Base implementation of {@link IBackendAddon} for memtext backend.
 *
 * @author hazard157
 */
public abstract class AbstractBackendAddon
    implements IBackendAddon, ITsClearable, ICloseable, IKeepableEntity {

  private final AbstractSkBackendMemtext owner;
  private final String                   sectName;

  /**
   * Constructor for subclasses.
   * <p>
   * Addon may store it's data in textual representation as named section (described
   * {@link StrioUtils#readInterbaceContent(IStrioReader)}). If <code>aSectName</code> is an empty string no data will
   * be stored for this addon and {@link #doRead(IStrioReader)} and {@link #doWrite(IStrioWriter)} never will be called.
   *
   * @param aOwner {@link AbstractSkBackendMemtext} - the owner backend
   * @param aSectName String - name of the section in stored text or an ympty string if addon need no storage
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException section name is not an IDpath
   */
  protected AbstractBackendAddon( AbstractSkBackendMemtext aOwner, String aSectName ) {
    TsNullArgumentRtException.checkNulls( aOwner, aSectName );
    owner = aOwner;
    if( !aSectName.isEmpty() ) {
      sectName = StridUtils.checkValidIdPath( aSectName );
    }
    else {
      sectName = TsLibUtils.EMPTY_STRING;
    }
  }

  // ------------------------------------------------------------------------------------
  // IBackendAddon
  //

  @Override
  final public AbstractSkBackendMemtext owner() {
    return owner;
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public abstract void close();

  // ------------------------------------------------------------------------------------
  // ITsClearable
  //

  @Override
  public abstract void clear();

  // ------------------------------------------------------------------------------------
  // IKeepableEntity
  //

  @Override
  final public void write( IStrioWriter aSw ) {
    if( sectName.isEmpty() ) {
      return;
    }
    StrioUtils.writeKeywordHeader( aSw, sectName, true );
    aSw.writeChar( CHAR_SET_BEGIN );
    aSw.incNewLine();
    doWrite( aSw );
    aSw.decNewLine();
    aSw.writeChar( CHAR_SET_END );
  }

  @Override
  final public void read( IStrioReader aSr ) {
    if( sectName.isEmpty() ) {
      return;
    }
    StrioUtils.ensureKeywordHeader( aSr, sectName );
    aSr.ensureChar( CHAR_SET_BEGIN );
    doRead( aSr );
    aSr.ensureChar( CHAR_SET_END );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the section name in the textual representation.
   *
   * @return String - section name or an empty string if addon does not stores any data
   */
  public String getSectionName() {
    return sectName;
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

  /**
   * Subclass may override to store it's data to the permanent storage.
   * <p>
   * The method is <code>not</code> called for addons with an empty {@link #getSectionName()}.
   * <p>
   * In base class does nothing there is no need to call superclass method when overriding.
   *
   * @param aSw {@link IStrioWriter} - textual writer, never is <code>null</code>
   */
  protected void doWrite( IStrioWriter aSw ) {
    // nop
  }

  /**
   * Subclass may override to read it's data from the permanent storage.
   * <p>
   * The method is <code>not</code> called for addons with an empty {@link #getSectionName()}.
   * <p>
   * In base class does nothing there is no need to call superclass method when overriding.
   *
   * @param aSr {@link IStrioReader} - textual reader, never is <code>null</code>
   */
  protected void doRead( IStrioReader aSr ) {
    // nop
  }

}
