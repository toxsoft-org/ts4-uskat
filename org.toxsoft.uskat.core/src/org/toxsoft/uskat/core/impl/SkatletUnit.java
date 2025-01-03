package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.wub.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.impl.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * Элемент контейнера {@link SkatletBox} представляющий {@link ISkatlet}.
 *
 * @author mvk
 */
public class SkatletUnit
    extends PluginUnit {

  private final ISkatlet skatlet;
  private ITsContextRo   environ;

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

  /**
   * Initializes libraries, register types & service creators.
   *
   * @return {@link ValidationResult} - initialization success result
   */
  public ValidationResult initialize() {
    return skatlet.initialize();
  }

  /**
   * Устанавливает контекст скатлета
   *
   * @param aEnviron {@link ITsContext} контекст
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException контекст уже установлен
   */
  public void setContext( ITsContextRo aEnviron ) {
    TsNullArgumentRtException.checkNull( aEnviron );
    TsIllegalStateRtException.checkNoNull( environ );
    environ = aEnviron;
    skatlet.setContext( aEnviron );
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
    // Инициализация скатлетов проводит SkatletBox непосредственно через вызов SkatletUnit.setConter(...)
    // return skatlet.init( aEnviron );
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doDoJob() {
    skatlet.doJob();
  }

  @Override
  protected boolean doQueryStop() {
    return skatlet.queryStop() && super.doQueryStop();
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
