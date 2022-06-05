package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.memtext.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Basic implementation of {@link ISkBackend} in-memory data permanently stored as text.
 *
 * @author hazard157
 */
public abstract class MtbAbstractBackend
    implements ISkBackend, ITsClearable, IKeepableEntity, IGenericChangeEventCapable {

  IListEdit<ISkServiceCreator<? extends AbstractSkService>> backendServicesCreators = new ElemArrayList<>();

  private final ITsContextRo    argContext;
  private final ISkFrontendRear frontend;

  private final MtbBaClasses mtbBaClasses;
  private final MtbBaObjects mtbBaObjects;
  private final MtbBaClobs   mtbBaClobs;
  private final MtbBaLinks   baLinks;

  private final IStringMapEdit<MtbAbstractAddon> allAddons = new StringMap<>();

  private final SkBackendInfo        backendInfo;
  private final GenericChangeEventer eventer;

  /**
   * The flag indicates that the content has been changed since the last save..
   * <p>
   *
   * @see #isChanged()
   */
  private boolean changed = false;

  /**
   * Constructor.
   *
   * @param aFrontend {@link ISkFrontendRear} - the backside of the frontend
   * @param aArgs {@link ITsContextRo} - backend argument options and references
   * @param aBackendId String - backend ID for {@link ISkBackendInfo#id()}
   * @param aBackendInfoValue - backend info params values {@link ISkBackendInfo#params()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbAbstractBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs, String aBackendId,
      IOptionSet aBackendInfoValue ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    eventer = new GenericChangeEventer( this );
    frontend = aFrontend;
    argContext = aArgs;
    mtbBaClasses = new MtbBaClasses( this );
    allAddons.put( mtbBaClasses.id(), mtbBaClasses );
    mtbBaObjects = new MtbBaObjects( this );
    allAddons.put( mtbBaObjects.id(), mtbBaObjects );
    mtbBaClobs = new MtbBaClobs( this );
    allAddons.put( mtbBaClobs.id(), mtbBaClobs );
    baLinks = new MtbBaLinks( this );
    allAddons.put( baLinks.id(), baLinks );
    // TODO other addons
    backendInfo = new SkBackendInfo( aBackendId, System.currentTimeMillis(), aBackendInfoValue );
  }

  // ------------------------------------------------------------------------------------
  // IKeepableEntity
  //

  @Override
  public void read( IStrioReader aSr ) {
    TsNullArgumentRtException.checkNull( aSr );
    internalClear();
    for( MtbAbstractAddon a : allAddons ) {
      a.read( aSr );
    }
    setChanged();
  }

  @Override
  public void write( IStrioWriter aSw ) {
    TsNullArgumentRtException.checkNull( aSw );
    for( MtbAbstractAddon a : allAddons ) {
      a.write( aSw );
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void internalClear() {
    for( int i = allAddons.size() - 1; i >= 0; i-- ) {
      allAddons.values().get( i ).clear();
    }
  }

  private void removeObjectsOfNonStoredClassIds( IStringList aNotStoredObjClassIds ) {
    // TODO remove object not to be stored
    // TODO ??? remove right objects of this class from links
    // TODO remove clobs of the removed objects
    // TODO remove links of the removed objects
    // TODO remove rtdata of the removed objects
    // TODO remove events & commands histroy of the removed objects
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    IStringList notStoredObjClassIds = IBackendMemtextConstants.OPDEF_NOT_STORED_OBJ_CLASS_IDS.getValue( argContext().params() ).asValobj();
    if( !notStoredObjClassIds.isEmpty() ) {
      removeObjectsOfNonStoredClassIds( notStoredObjClassIds );
    }
    for( int i = allAddons.size() - 1; i >= 0; i-- ) {
      allAddons.values().get( i ).close();
    }
    doClose();
  }

  // ------------------------------------------------------------------------------------
  // ITsClearable
  //

  @Override
  public void clear() {
    internalClear();
    setChanged();
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //

  @Override
  public IGenericChangeEventer genericChangeEventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public ISkBackendInfo getBackendInfo() {
    return backendInfo;
  }

  @Override
  public ISkFrontendRear frontend() {
    return frontend;
  }

  @Override
  public ITsContextRo openArgs() {
    return argContext;
  }

  @Override
  public MtbBaClasses baClasses() {
    return mtbBaClasses;
  }

  @Override
  public IBaObjects baObjects() {
    return mtbBaObjects;
  }

  @Override
  public IBaLinks baLinks() {
    return baLinks;
  }

  @Override
  public IBaEvents baEvents() {
    // TODO реализовать MtbAbstractBackend.baEvents()
    throw new TsUnderDevelopmentRtException( "MtbAbstractBackend.baEvents()" );
  }

  @Override
  public IBaClobs baClobs() {
    return mtbBaClobs;
  }

  @Override
  public IBaRtdata baRtdata() {
    // TODO реализовать MtbAbstractBackend.baRtdata()
    throw new TsUnderDevelopmentRtException( "MtbAbstractBackend.baRtdata()" );
  }

  @Override
  public IBaCommands baCommands() {
    // TODO реализовать MtbAbstractBackend.baCommands()
    throw new TsUnderDevelopmentRtException( "MtbAbstractBackend.baCommands()" );
  }

  @Override
  public IListEdit<ISkServiceCreator<? extends AbstractSkService>> listBackendServicesCreators() {
    return backendServicesCreators;
  }

  @Override
  public <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aExpectedType );
    Object rawAddon = allAddons.findByKey( aAddonId );
    return aExpectedType.cast( rawAddon );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Performs internal checks when calling any API methods.
   * <p>
   * This method must be the first call in any API calls of the backend and addons.
   */
  public void internalCheck() {
    doInternalCheck();
  }

  /**
   * Sets the {@link #isChanged()} flags and fires the generic change event.
   */
  public void setChanged() {
    changed = true;
    eventer.fireChangeEvent();
  }

  /**
   * Returns the changed content flag.
   * <p>
   * The flag indicates that the content has been changed since the last save..
   * <p>
   * The flag is set by the {@link #setChanged()} method used by addons to indicate content change. The flag is reset by
   * the {@link #write(IStrioWriter)} method and after the successful {@link #read(IStrioReader)} method call.
   *
   * @return boolean - <code>true</code> content was changed via backend API
   */
  public boolean isChanged() {
    return changed;
  }

  /**
   * Returns the context passed as arguments in constructor.
   *
   * @return {@link ITsContextRo} - creation arguments
   */
  public ITsContextRo argContext() {
    return argContext;
  }

  /**
   * Sets the single option of backend info params {@link ISkBackendInfo#params()}.
   *
   * @param aOptionId String - option ID
   * @param aOptionValue {@link IAtomicValue} - option value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException the identifier is not valid IDpath
   */
  public void setBackendInfoParamsOpton( String aOptionId, IAtomicValue aOptionValue ) {
    backendInfo.params().setValue( aOptionId, aOptionValue );
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

  /**
   * Subclass preforms checks and tasks on every API method call.
   */
  protected abstract void doInternalCheck();

  /**
   * Subclass may release resources, save changes and perform any other clean-up tasks before finishing.
   */
  protected abstract void doClose();

}
