package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.panels.lazy.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.txtmatch.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.utils.*;

/**
 * Filter pane to filter out {@link ISkClassInfo} modeled entities.
 *
 * @author hazard157
 */
class SkClassFilterPane
    extends AbstractLazyPanel<Control>
    implements IM5FilterPanel<ISkClassInfo>, ISkGuiContextable {

  /**
   * The implementation of the filter returned by method {@link SkClassFilterPane#getFilter()}.
   *
   * @author hazard157
   */
  class FilterImpl
      implements ITsFilter<ISkClassInfo> {

    private final TextMatcher textMatcher;
    private final boolean     onlyGw;

    public FilterImpl( boolean aIncludeNonGw, String aSearchText ) {
      onlyGw = aIncludeNonGw;
      textMatcher = new TextMatcher( ETextMatchMode.CONTAINS, aSearchText, false );
    }

    @Override
    public boolean accept( ISkClassInfo aEntity ) {
      for( IM5FieldDef<ISkClassInfo, ?> fdef : searchFields ) {
        String fieldValueString = fdef.getter().getName( aEntity );
        if( textMatcher.match( fieldValueString ) ) {
          if( !onlyGw ) {
            return true;
          }
          return isPureGreenWorldClass( aEntity, skConn().coreApi() );
        }
      }
      return false;
    }

  }

  private final IM5Model<ISkClassInfo>                        model;
  private final GenericChangeEventer                          eventer;
  private final IStridablesList<IM5FieldDef<ISkClassInfo, ?>> searchFields;

  /**
   * At pane creation created, filter allows only GW classes, {@link #chbOnlyGw} msut be set to <code>true</code>.
   */
  private ITsFilter<ISkClassInfo> filter = new FilterImpl( true, EMPTY_STRING );

  private Button chbOnlyGw = null;
  private Text   txtString = null;

  public SkClassFilterPane( ITsGuiContext aContext, IM5Model<ISkClassInfo> aModel ) {
    super( aContext );
    model = TsNullArgumentRtException.checkNull( aModel );
    eventer = new GenericChangeEventer( this );
    // list fields to be searched
    IStridablesListEdit<IM5FieldDef<ISkClassInfo, ?>> llColumnFields = new StridablesList<>();
    for( IM5FieldDef<ISkClassInfo, ?> fDef : model().fieldDefs() ) {
      if( fDef.hasFlag( M5FF_COLUMN ) ) {
        llColumnFields.add( fDef );
      }
    }
    searchFields = llColumnFields;
  }

  // ------------------------------------------------------------------------------------
  // MpcAbstractPane
  //

  @Override
  protected Control doCreateControl( Composite aParent1 ) {
    // prepare backplane
    Composite backplane = new Composite( aParent1, SWT.NONE );
    backplane.setLayout( new BorderLayout() );
    // chbOnlyGw
    chbOnlyGw = new Button( backplane, SWT.TOGGLE );
    chbOnlyGw.setLayoutData( new BorderData( SWT.LEFT ) );
    chbOnlyGw.setToolTipText( STR_SHOW_ONLY_GW_CLASSES_D );
    chbOnlyGw.setSelection( true );
    EIconSize iconSize = EIconSize.IS_16X16; // we fix size because need smalles icon
    chbOnlyGw.setImage( iconManager().loadStdIcon( ICONID_COLORED_WORLD_GREEN, iconSize ) );
    // txtString
    txtString = new Text( backplane, SWT.BORDER );
    txtString.setLayoutData( new BorderData( SWT.CENTER ) );
    txtString.setMessage( STR_FILTER_TEXT );
    txtString.setToolTipText( STR_FILTER_TEXT_D );
    // setup
    chbOnlyGw.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent aE ) {
        whenWidgetsContentChanged();
      }

    } );
    txtString.addModifyListener( aEvent -> whenWidgetsContentChanged() );
    updateFilter();
    return backplane;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void updateFilter() {
    String str = txtString.getText();
    boolean onlyGw = chbOnlyGw.getSelection();
    if( !onlyGw && str.isEmpty() ) {
      filter = ITsFilter.ALL;
    }
    else {
      filter = new FilterImpl( onlyGw, str );
    }
  }

  private void whenWidgetsContentChanged() {
    updateFilter();
    eventer.fireChangeEvent();
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //

  @Override
  public IGenericChangeEventer genericChangeEventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // ISkGuiContextable
  //

  @Override
  public ISkConnection skConn() {
    return model.domain().tsContext().get( ISkConnection.class );
  }

  // ------------------------------------------------------------------------------------
  // IM5ModelRelated
  //

  @Override
  public IM5Model<ISkClassInfo> model() {
    return model;
  }

  // ------------------------------------------------------------------------------------
  // IM5FilterPanel
  //

  @Override
  public ITsFilter<ISkClassInfo> getFilter() {
    return filter;
  }

  @Override
  public void reset() {
    eventer.pauseFiring();
    try {
      chbOnlyGw.setSelection( false );
      txtString.setText( EMPTY_STRING );
      filter = ITsFilter.ALL;
    }
    finally {
      eventer.resumeFiring( true );
    }
  }

}
