package org.toxsoft.uskat.core.gui.valed.std.skid;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.inner.*;

/**
 * Chooses {@link Skid} from the obejvts provided by {@link ISkCoreApi#objService()}.
 *
 * @author hazard157
 */
public final class ValedSkidSelector {

  /**
   * The registered factory ID.
   */
  public static final String FACTORY_NAME = ISkCoreGuiInnerSharedConstants.SKCGC_VALED_SKID_SELECTOR;

  /**
   * The ID of the simple default outfit.
   */
  public static final String OUTFIT_ID_SIMPLE = "simple"; //$NON-NLS-1$

  /**
   * The ID of the TODO ??? some other outfit.
   */
  public static final String OUTFIT_ID_OTHER = "other"; //$NON-NLS-1$

  /**
   * The factory singleton.
   */
  @SuppressWarnings( "unchecked" )
  public static final IValedControlFactory FACTORY = new AbstractValedControlFactory( FACTORY_NAME ) {

    @Override
    protected IValedControl<Skid> doCreateEditor( ITsGuiContext aContext ) {
      String outfitId = OPDEF_VALED_UI_OUTFIT_ID.getValue( aContext.params() ).asString();
      return switch( outfitId ) {
        case OUTFIT_ID_OTHER -> new ValedSkidSelectorOtherKind( aContext );
        case OUTFIT_ID_SIMPLE -> new ValedSkidSelectorSimple( aContext );
        default -> new ValedSkidSelectorSimple( aContext );
      };
    }

    @Override
    protected IValedControl<Skid> doCreateSingleLine( ITsGuiContext aContext ) {
      String outfitId = OPDEF_VALED_UI_OUTFIT_ID.getValue( aContext.params() ).asString();
      return switch( outfitId ) {
        case OUTFIT_ID_OTHER -> super.doCreateSingleLine( aContext );
        case OUTFIT_ID_SIMPLE -> super.doCreateSingleLine( aContext );
        default -> super.doCreateSingleLine( aContext );
      };
    }

    @Override
    protected IValedControl<Skid> doCreateViewer( ITsGuiContext aContext ) {
      String outfitId = OPDEF_VALED_UI_OUTFIT_ID.getValue( aContext.params() ).asString();
      return switch( outfitId ) {
        case OUTFIT_ID_OTHER -> super.doCreateViewer( aContext );
        case OUTFIT_ID_SIMPLE -> super.doCreateViewer( aContext );
        default -> super.doCreateViewer( aContext );
      };
    }

  };

}
