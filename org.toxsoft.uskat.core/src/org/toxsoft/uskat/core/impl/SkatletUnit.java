package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.wub.WubUnitDiagnostics;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.plugins.impl.IPlugin;
import org.toxsoft.core.tslib.utils.plugins.impl.PluginUnit;
import org.toxsoft.uskat.core.devapi.ISkatlet;

/**
 * Элемент контейнера {@link SkatletBox} представляющий {@link ISkatlet}.
 *
 * @author mvk
 */
public class SkatletUnit
    extends PluginUnit {

  private final ISkatlet skatlet;

  /**
   * Конструктор
   *
   * @param aId String идентификатор компонента
   * @param aParams {@link IOptionSet} параметры компонента
   * @param aPlugin {@link IPlugin} плагин компонента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkatletUnit( String aId, IOptionSet aParams, IPlugin aPlugin ) {
    super( aId, aParams, aPlugin );
    skatlet = aPlugin.instance( ISkatlet.class );
  }

  // ------------------------------------------------------------------------------------
  // PluginUnit
  //
  @Override
  public String iconId() {
    return skatlet.iconId();
  }

  @Override
  protected ValidationResult doInit( ITsContextRo aEnviron ) {
    return skatlet.init( aEnviron );
  }

  @Override
  protected void doDoJob() {
    skatlet.doJob();
  }

  @Override
  protected boolean doQueryStop() {
    return skatlet.queryStop();
  }

  @Override
  public WubUnitDiagnostics diagnosticsEdit() {
    // TODO Auto-generated method stub
    return super.diagnosticsEdit();
  }

  @Override
  protected void doStart() {
    skatlet.start();
  }

  @Override
  protected boolean doStopping() {
    return skatlet.isStopped();
  }

  @Override
  protected void doDestroy() {
    skatlet.destroy();
  }

}
