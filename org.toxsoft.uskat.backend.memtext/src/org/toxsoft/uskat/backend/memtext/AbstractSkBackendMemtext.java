package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Basic implementation of {@link ISkBackend} in-memry data permanently stored as text.
 *
 * @author hazard157
 */
public abstract class AbstractSkBackendMemtext
    implements ISkBackend, ITsClearable, IKeepableEntity {

  /**
   * Backend ID prefix for subclass implementations.
   */
  protected static final String SKB_ID_MEMTEXT = ISkBackendHardConstant.SKB_ID + ".memtext"; //$NON-NLS-1$

  private final ISkFrontendRear frontend;

  private final BaClasses            baClasses;
  private final AbstractBackendAddon baObjects = null; // FIXME null
  private final AbstractBackendAddon baLinks   = null; // FIXME null
  private final AbstractBackendAddon baClobs   = null; // FIXME null
  private final AbstractBackendAddon baEvents  = null; // FIXME null

  private final IMapEdit<Class<?>, AbstractBackendAddon> allAddons = new ElemMap<>();

  private final ISkBackendInfo backendInfo;

  /**
   * Constriuctor.
   *
   * @param aFrontend {@link ISkFrontendRear} - the backside of the frontend
   * @param aArgs {@link ITsContextRo} - backend argument options and references
   * @param aBackendId String - backend ID for {@link ISkBackendInfo#id()}
   * @param aBackendInfoValue - backend info params values {@link ISkBackendInfo#params()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public AbstractSkBackendMemtext( ISkFrontendRear aFrontend, ITsContextRo aArgs, String aBackendId,
      IOptionSet aBackendInfoValue ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aArgs );
    frontend = aFrontend;
    baClasses = new BaClasses( this );
    allAddons.put( IBaClasses.class, baClasses );
    //  FIXME ???
    // allAddons.put( IBaObjects.class, baObjects );
    // allAddons.put( IBaLinks.class, baLinks );
    // allAddons.put( IBaClasses.class, baClobs );
    // allAddons.put( IBaEvents.class, baEvents );
    backendInfo = new SkBackendInfo( aBackendId, System.currentTimeMillis(), Skid.NONE, aBackendInfoValue );
  }

  // ------------------------------------------------------------------------------------
  // IKeepableEntity
  //

  @Override
  public void read( IStrioReader aSr ) {
    TsNullArgumentRtException.checkNull( aSr );
    internalClear();
    for( AbstractBackendAddon a : allAddons ) {
      a.read( aSr );
    }
    setChanged();
  }

  @Override
  public void write( IStrioWriter aSw ) {
    TsNullArgumentRtException.checkNull( aSw );
    for( AbstractBackendAddon a : allAddons ) {
      a.write( aSw );
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Осуществляет внутренние проверки при вызове любых методов API.
   */
  private void internalCheck() {
    doInternalCheck();
  }

  private void internalClear() {
    for( int i = allAddons.size() - 1; i >= 0; i-- ) {
      allAddons.values().get( i ).clear();
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    for( int i = allAddons.size() - 1; i >= 0; i-- ) {
      allAddons.values().get( i ).close();
    }
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
  public IBaClasses baClasses() {
    return baClasses;
  }

  @Override
  public IBaObjects baObjects() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IBaLinks baLinks() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IBaEvents baEvents() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IBaClobs baClobs() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkExtServicesProvider getExtServicesProvider() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T findBackendAddon( Class<T> aAddonInterface ) {
    // TODO Auto-generated method stub
    return null;
  }

  // ------------------------------------------------------------------------------------
  // API (mainly for subclasses)
  //

  void setChanged() {
    // TODO AbstractSkBackendMemtext.setChanged()
  }

  /**
   * звращает признак наличия несохраненных изменений.
   * <p>
   * Устанавливается при любых изменениях в {@link #typeInfos}, {@link #classInfos}, {@link #objs} и {@link #linksMap}.
   * Сбрасывается при успешном чтении в {@link #read(IDvReader)} и сохранении в методе {@link #write(IDvWriter)}.
   *
   * @return boolean - признак наличия несохраненных изменений
   */
  // final public boolean isChanged() {
  // return changed;
  // }

  protected abstract void doInternalCheck();

  protected abstract void doClose();

}
