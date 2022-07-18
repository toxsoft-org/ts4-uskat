package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * Base implementation of {@link IBackendAddon} for memtext backend.
 *
 * @author hazard157
 */
public abstract class MtbAbstractAddon
    extends BackendAddonBase<MtbAbstractBackend>
    implements ITsClearable, ICloseable, IKeepableEntity {

  /**
   * TODO list for memtext addon:
   * <ol>
   * <li>some saved data need to be explicitly removed as sysdescr changes, like CLOBs or history data;</li>
   * <li>xxx;</li>
   * <li>zzz.</li>
   * </ol>
   */

  /**
   * Constructor for subclasses.
   * <p>
   * Addon may store it's data in textual representation as named section (described
   * {@link StrioUtils#readInterbaceContent(IStrioReader)}). If <code>aSectName</code> is an empty string no data will
   * be stored for this addon and {@link #doRead(IStrioReader)} and {@link #doWrite(IStrioWriter)} never will be called.
   * <p>
   * The addon is used as the section name in the textual representation.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @param aInfo {@link IStridable} - the addon info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException section name is not an IDpath
   */
  protected MtbAbstractAddon( MtbAbstractBackend aOwner, IStridable aInfo ) {
    super( aOwner, aInfo );
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
    StrioUtils.writeKeywordHeader( aSw, id(), true );
    aSw.writeChar( CHAR_SET_BEGIN );
    aSw.incNewLine();
    doWrite( aSw );
    aSw.decNewLine();
    aSw.writeChar( CHAR_SET_END );
    aSw.writeEol();
  }

  @Override
  final public void read( IStrioReader aSr ) {
    StrioUtils.ensureKeywordHeader( aSr, id() );
    aSr.ensureChar( CHAR_SET_BEGIN );

    /**
     * TODO reorganize read process in 2 steps:
     * <ul>
     * <li>each addon reads from steam, holds read data but NOT updates it's content;</li>
     * <li>if all addons read without exceptions, each addon updates it's content freom read data.</li>
     * </ul>
     * Such algorithm ensureas data integrity even if read fails.
     */

    doRead( aSr );
    aSr.ensureChar( CHAR_SET_END );
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  /**
   * Implementation must remove entitites related to the specified class IDs from the memory.
   * <p>
   * This method is called immediately before last save to the storage and implements MemText backend ability not to
   * store garbage objects in the premanent storage (like connection session objects). However, saves during normal
   * works will save all objects.
   * <p>
   * Method in base class does nothing there is no need to call superclass method when overriding.
   *
   * @param aClassIds {@link IStringList} - list of entities class IDs to be removed
   */
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // API for descendans
  //

  /**
   * Simply calls {@link MtbAbstractBackend#internalCheck()},
   * <p>
   * This method must be the first call in any API calls of the addon.
   */
  protected void internalCheck() {
    owner().internalCheck();
  }

  /**
   * Simply calls {@link MtbAbstractBackend#setChanged()},
   */
  protected void setChanged() {
    owner().setChanged();
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

  /**
   * Subclass may override to store it's data to the permanent storage.
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
   * In base class does nothing there is no need to call superclass method when overriding.
   *
   * @param aSr {@link IStrioReader} - textual reader, never is <code>null</code>
   */
  protected void doRead( IStrioReader aSr ) {
    // nop
  }

}
