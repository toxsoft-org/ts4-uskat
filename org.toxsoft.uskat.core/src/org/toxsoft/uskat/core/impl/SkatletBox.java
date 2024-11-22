package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.impl.*;
import org.toxsoft.uskat.core.devapi.*;

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
    // Клиент обязан разместить в контексте следующие параметры
    TsIllegalArgumentRtException.checkNull( ISkatlet.REF_SKATLET_SUPPORT.getRef( aEnviron ) );
    return super.doInit( aEnviron );
  }

}
