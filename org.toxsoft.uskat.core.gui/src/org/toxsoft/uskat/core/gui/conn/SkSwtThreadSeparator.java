package org.toxsoft.uskat.core.gui.conn;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.*;
import static org.toxsoft.uskat.core.gui.conn.ISkResources.*;

import org.eclipse.swt.widgets.Display;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRefDef;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.impl.ISkCoreConfigConstants;
import org.toxsoft.uskat.core.impl.SkAbstractThreadSeparator;

/**
 * Служба: разделение потоков.
 * <p>
 * Решает задачи разделения доступа к данным между потоками системы и SWT.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class SkSwtThreadSeparator
    extends SkAbstractThreadSeparator {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<SkAbstractThreadSeparator> CREATOR = SkSwtThreadSeparator::new;

  /**
   * Mandotary context parameter: Display
   * <p>
   * Тип: {@link Display}
   */
  public static final ITsContextRefDef<Display> REF_DISPLAY = create( SERVICE_ID + ".Display", Display.class, //$NON-NLS-1$
      TSID_NAME, "Display", // N_DISPLAY,
      TSID_DESCRIPTION, "Display", // D_DISPLAY,
      TSID_IS_NULL_ALLOWED, AV_FALSE );

  private Display display;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkSwtThreadSeparator( IDevCoreApi aCoreApi ) {
    super( aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // SkAbstractThreadSeparator
  //
  @Override
  protected void doInitSeparator( ITsContextRo aArgs ) {
    display = REF_DISPLAY.getRef( aArgs, null );
    if( display == null ) {
      // Неопределен дисплей
      throw new TsIllegalArgumentRtException( ERR_GUI_DISPLAY_UNDEF );
    }
    Thread thread = ISkCoreConfigConstants.REFDEF_API_THREAD.getRef( aArgs, null );
    if( thread != display.getThread() ) {
      // Неверный поток доступа к API
      throw new TsIllegalArgumentRtException( ERR_WRONG_GUI_THREAD );
    }
  }

  @Override
  public void asyncExec( Runnable aRunnable ) {
    display.asyncExec( aRunnable );
  }

  @Override
  public void syncExec( Runnable aRunnable ) {
    display.syncExec( aRunnable );
  }

  @Override
  public void timerExec( int aMilliseconds, Runnable aRunnable ) {
    display.timerExec( aMilliseconds, aRunnable );
  }

}
