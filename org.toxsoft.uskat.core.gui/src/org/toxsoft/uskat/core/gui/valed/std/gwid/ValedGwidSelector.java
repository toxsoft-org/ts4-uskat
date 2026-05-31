package org.toxsoft.uskat.core.gui.valed.std.gwid;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.inner.*;

/**
 * Chooses {@link Gwid} from the obejcts provided by {@link ISkCoreApi#objService()}.
 *
 * @author hazard157
 */
public final class ValedGwidSelector {

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
   * The ID of the rt-data outfit.
   */
  public static final String OUTFIT_ID_RTDATA = "rtdata"; //$NON-NLS-1$

  /**
   * The ID of the parameter that indicated is abstract or concrete GWID needed
   */
  public static final String PARAMID_ABSTRACT = "abstract"; //$NON-NLS-1$

  /**
   * The ID of the parameter that determines what kind of property GWID needed
   */
  public static final String PARAMID_PROP_KIND = "propKind"; //$NON-NLS-1$

  /**
   * The factory singleton.
   */
  @SuppressWarnings( "unchecked" )
  public static final IValedControlFactory FACTORY = new AbstractValedControlFactory( FACTORY_NAME ) {

    @Override
    protected IValedControl<Gwid> doCreateEditor( ITsGuiContext aContext ) {
      String outfitId = OPDEF_VALED_UI_OUTFIT_ID.getValue( aContext.params() ).asString();
      return switch( outfitId ) {
        case OUTFIT_ID_OTHER -> new ValedGwidSelectorOther( aContext );
        case OUTFIT_ID_SIMPLE -> new ValedGwidSelectorSimple( aContext );
        case OUTFIT_ID_RTDATA -> new ValedSinglePropGwidSelector( aContext );
        default -> new ValedGwidSelectorSimple( aContext );
      };
    }

    @Override
    protected IValedControl<Gwid> doCreateSingleLine( ITsGuiContext aContext ) {
      String outfitId = OPDEF_VALED_UI_OUTFIT_ID.getValue( aContext.params() ).asString();
      return switch( outfitId ) {
        case OUTFIT_ID_OTHER -> super.doCreateSingleLine( aContext );
        case OUTFIT_ID_SIMPLE -> super.doCreateSingleLine( aContext );
        case OUTFIT_ID_RTDATA -> super.doCreateSingleLine( aContext );
        default -> super.doCreateSingleLine( aContext );
      };
    }

    @Override
    protected IValedControl<Gwid> doCreateViewer( ITsGuiContext aContext ) {
      String outfitId = OPDEF_VALED_UI_OUTFIT_ID.getValue( aContext.params() ).asString();
      return switch( outfitId ) {
        case OUTFIT_ID_OTHER -> super.doCreateViewer( aContext );
        case OUTFIT_ID_SIMPLE -> super.doCreateViewer( aContext );
        case OUTFIT_ID_RTDATA -> super.doCreateViewer( aContext );
        default -> super.doCreateViewer( aContext );
      };
    }

  };

}
