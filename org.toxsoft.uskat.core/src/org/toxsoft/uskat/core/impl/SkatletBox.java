package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.plugins.impl.IPlugin;
import org.toxsoft.core.tslib.utils.plugins.impl.PluginBox;
import org.toxsoft.uskat.core.devapi.ISkatlet;

/**
 * Контейнер скатлетов
 *
 * @author mvk
 */
public class SkatletBox
    extends PluginBox<SkatletUnit> {

  /**
   * Конструктор
   *
   * @param aId String идентификатор контейнера
   * @param aParams {@link IOptionSet} параметры контейнера
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkatletBox( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // PluginBox
  //

  @Override
  protected SkatletUnit doCreateUnit( String aPluginId, IPlugin aPlugin ) {
    return new SkatletUnit( aPluginId, params(), aPlugin );
  }

  @Override
  protected synchronized ValidationResult doInit( ITsContextRo aEnviron ) {
    // Клиент обязан разместить в контексте соединение
    TsIllegalArgumentRtException.checkNull( ISkatlet.REF_SK_CONNECTION.getRef( aEnviron ) );
    return super.doInit( aEnviron );
  }

}
