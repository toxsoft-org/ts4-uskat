package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.panels.vecboard.*;
import org.toxsoft.core.tsgui.panels.vecboard.impl.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link IM5EntityPanel} implementation to be returned by {@link SdedDtoFullObjectM5EntityPanel}.
 * <p>
 * Entity panel contains:
 * <ul>
 * <li>top part - ladder layout field editors for fields class ID, parent ID, name and description;</li>
 * <li>bottom part - tab folder with separate tab for class properties like: attr, links, RTdata, etc.</li>
 * </ul>
 *
 * @author dima
 */
public class SdedDtoFullObjectM5EntityPanel
    extends M5DefaultEntityEditorPanel<IDtoFullObject> {

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aModel {@link IM5Model} - entity model
   * @param aLifecycleManager {@link IM5LifecycleManager} - optional lifecycle manager, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoFullObjectM5EntityPanel( ITsGuiContext aContext, IM5Model<IDtoFullObject> aModel,
      IM5LifecycleManager<IDtoFullObject> aLifecycleManager ) {
    super( aContext, aModel, aLifecycleManager );
  }

  @Override
  protected void doInitLayout() {
    VecBorderLayout vb = new VecBorderLayout();
    board().setLayout( vb );
    IVecBoard vbNorth = new VecBoard();
    vb.addControl( vbNorth, EBorderLayoutPlacement.NORTH );
    // split editor on upper (north) and tabs (central) lists
    IStringMapEdit<IValedControl<?>> northBoardEditors = new StringMap<>();
    IStringMapEdit<IValedControl<?>> centerBoardEditors = new StringMap<>();
    for( String fieldId : editors().keys() ) {
      switch( fieldId ) {
        case FID_CLASS_ID:
        case FID_PARENT_ID:
        case TSID_NAME:
        case TSID_DESCRIPTION: {
          northBoardEditors.put( fieldId, editors().getByKey( fieldId ) );
          break;
        }
        default: {
          centerBoardEditors.put( fieldId, editors().getByKey( fieldId ) );
          break;
        }
      }
    }
    vbNorth.setLayout( makeLadderLayout( model(), northBoardEditors ) );
    IVecBoard vbCenter = new VecBoard();
    vb.addControl( vbCenter, EBorderLayoutPlacement.CENTER );
    vbCenter.setLayout( makeTabLayout( centerBoardEditors ) );
  }

  /**
   * Puts each editor in the separate tab of the newly created {@link IVecTabLayout}.
   *
   * @param aEditors {@link IStringMap}&lt;{@link IValedControl}&gt; - map "field ID" - "field editor"
   * @return {@link IVecLadderLayout} - created tab layout
   */
  public IVecTabLayout makeTabLayout( IStringMap<IValedControl<?>> aEditors ) {
    IVecTabLayout ll = new VecTabLayout( false );
    for( String fieldId : aEditors.keys() ) {
      IValedControl<?> varEditor = aEditors.getByKey( fieldId );
      IM5FieldDef<?, ?> fd = model().fieldDefs().getByKey( fieldId );
      String label = TsLibUtils.EMPTY_STRING;
      if( !fd.nmName().isEmpty() ) {
        label = "  " + fd.nmName(); //$NON-NLS-1$
      }
      String tooltip = fd.description();
      // IVecTabLayoutData layoutData = new VecTabLayoutData( label, tooltip, fd.iconId(), EIconSize.IS_16X16 );
      IVecTabLayoutData layoutData = new VecTabLayoutData( label, tooltip, "", EIconSize.IS_16X16 );
      ll.addControl( varEditor, layoutData );
    }
    return ll;
  }

}
