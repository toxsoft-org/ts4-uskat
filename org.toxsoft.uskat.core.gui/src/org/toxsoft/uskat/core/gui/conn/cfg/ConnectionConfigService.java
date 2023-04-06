package org.toxsoft.uskat.core.gui.conn.cfg;

import java.util.*;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link IConnectionConfigService} implementation.
 * <p>
 * Usage:
 * <ul>
 * <li>create instance of this class and put it to the application context as {@link IConnectionConfigService};</li>
 * <li>add listener which in the {@link IConnectionConfigServiceListener#onConfigsListChanged(IConnectionConfigService)}
 * saves {@link #listConfigs()} to the used storage;</li>
 * <li>somewhere in the application initialization code before first usage of the service load configs from the storage
 * and set them using {@link #setConfigsList(IStridablesList)};</li>
 * <li>optionally custom validators may be added to the service;</li>
 * <li>before first usage of the configurations register all needed providers by calling
 * {@link #registerPovider(IConnectionConfigProvider)}.</li>
 * </ul>
 *
 * @author hazard157
 */
public class ConnectionConfigService
    implements IConnectionConfigService {

  /**
   * {@link IConnectionConfigService#svs()} implementation.
   *
   * @author hazard157
   */
  static class Svs
      extends AbstractTsValidationSupport<IConnectionConfigServiceValidator>
      implements IConnectionConfigServiceValidator {

    @Override
    public ValidationResult canAddConfig( IConnectionConfigService aSource, IConnectionConfig aCfg ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( IConnectionConfigServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canAddConfig( aSource, aCfg ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canReplaceConfig( IConnectionConfigService aSource, IConnectionConfig aCfg,
        IConnectionConfig aOldCfg ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( IConnectionConfigServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canReplaceConfig( aSource, aCfg, aOldCfg ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveConfig( IConnectionConfigService aSource, String aCfgId ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( IConnectionConfigServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveConfig( aSource, aCfgId ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public IConnectionConfigServiceValidator validator() {
      return this;
    }

  }

  /**
   * {@link ConnectionConfigService#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<IConnectionConfigServiceListener> {

    private boolean wasConfigsChange   = false;
    private boolean wasProvidersChange = false;

    @Override
    protected boolean doIsPendingEvents() {
      return wasConfigsChange || wasProvidersChange;
    }

    @Override
    protected void doFirePendingEvents() {
      if( wasConfigsChange ) {
        reallyFireConfigsListChanged();
      }
      if( wasProvidersChange ) {
        reallyFireProvidersListChanged();
      }
    }

    @Override
    protected void doClearPendingEvents() {
      wasConfigsChange = false;
      wasProvidersChange = false;
    }

    private void reallyFireConfigsListChanged() {
      for( IConnectionConfigServiceListener l : listeners() ) {
        l.onConfigsListChanged( ConnectionConfigService.this );
      }
    }

    private void reallyFireProvidersListChanged() {
      for( IConnectionConfigServiceListener l : listeners() ) {
        l.onProvidersListChanged( ConnectionConfigService.this );
      }
    }

    void fireConfigsListChanged() {
      if( isFiringPaused() ) {
        wasConfigsChange = true;
      }
      else {
        reallyFireConfigsListChanged();
      }
    }

    void fireProvidersListChanged() {
      if( isFiringPaused() ) {
        wasProvidersChange = true;
      }
      else {
        reallyFireProvidersListChanged();
      }
    }

  }

  private final IConnectionConfigServiceValidator builtinValidator = new IConnectionConfigServiceValidator() {

    @Override
    public ValidationResult canAddConfig( IConnectionConfigService aSource, IConnectionConfig aCfg ) {
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canReplaceConfig( IConnectionConfigService aSource, IConnectionConfig aCfg,
        IConnectionConfig aOldCfg ) {
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveConfig( IConnectionConfigService aSource, String aCfgId ) {
      return ValidationResult.SUCCESS;
    }

  };

  private final Eventer eventer = new Eventer();
  private final Svs     svs     = new Svs();

  private final IStridablesListEdit<IConnectionConfig>         configsList   = new StridablesList<>();
  private final IStridablesListEdit<IConnectionConfigProvider> providersList = new StridablesList<>();

  /**
   * Constructor.
   */
  public ConnectionConfigService() {
    svs.addValidator( builtinValidator );
  }

  @Override
  public IStridablesList<IConnectionConfig> listConfigs() {
    return configsList;
  }

  @Override
  public void defineConfig( IConnectionConfig aCfg ) {
    TsNullArgumentRtException.checkNull( aCfg );
    IConnectionConfig oldCfg = configsList.findByKey( aCfg.id() );
    if( Objects.equals( aCfg, oldCfg ) ) {
      return;
    }
    if( oldCfg != null ) {
      TsValidationFailedRtException.checkError( svs.canReplaceConfig( this, aCfg, oldCfg ) );
    }
    else {
      TsValidationFailedRtException.checkError( svs.canAddConfig( this, aCfg ) );
    }
    configsList.add( aCfg );
    eventer.fireConfigsListChanged();
  }

  @Override
  public void removeConfig( String aCfgId ) {
    TsNullArgumentRtException.checkNull( aCfgId );
    TsValidationFailedRtException.checkError( svs.canRemoveConfig( this, aCfgId ) );
    configsList.removeById( aCfgId );
    eventer.fireConfigsListChanged();
  }

  @Override
  public IStridablesList<IConnectionConfigProvider> listProviders() {
    return providersList;
  }

  @Override
  public void registerPovider( IConnectionConfigProvider aProvider ) {
    TsNullArgumentRtException.checkNull( aProvider );
    TsItemAlreadyExistsRtException.checkTrue( providersList.hasKey( aProvider.id() ) );
    providersList.add( aProvider );
    eventer.fireProvidersListChanged();
  }

  @Override
  public ITsValidationSupport<IConnectionConfigServiceValidator> svs() {
    return svs;
  }

  @Override
  public ITsEventer<IConnectionConfigServiceListener> eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // class API
  //

  /**
   * Sets {@link #listConfigs()} as a whole.
   * <p>
   * <b>Warning:</b> method does <b>not</b> validates configurations against {@link #svs()} validator because it is
   * designed only to be used for initial loading from the external storage.
   * <p>
   * Generates {@link IConnectionConfigServiceListener#onConfigsListChanged(IConnectionConfigService)} event.
   *
   * @param aConfigs IStridablesList:lt;{@link IConnectionConfig}&gt; - the list of connection configuration
   */
  public void setConfigsList( IStridablesList<IConnectionConfig> aConfigs ) {
    configsList.setAll( aConfigs );
    eventer.fireConfigsListChanged();
  }

}
