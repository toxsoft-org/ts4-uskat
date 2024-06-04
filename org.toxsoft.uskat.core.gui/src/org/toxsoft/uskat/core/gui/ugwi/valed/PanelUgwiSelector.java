package org.toxsoft.uskat.core.gui.ugwi.valed;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tsgui.utils.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.valed.controls.basic.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.gui.ugwi.gui.*;

/**
 * GUI panel for {@link Ugwi} selection.
 * <p>
 *
 * @author dima
 */
public class PanelUgwiSelector
    extends AbstractTsDialogPanel<Ugwi, ITsGuiContext> {

  private Text                        fixedKindText;
  private ValedComboSelector<String>  ugwiKindsCombo;
  private IUgwiKindGuiHelper          ugwiHelper;
  private Control                     selectPanel = null;
  private IGenericSelectorPanel<Ugwi> ugwSelector;
  private ISkCoreApi                  coreApi;

  /**
   * Конструктор панели, предназначенной для вставки в диалог {@link TsDialog}.
   *
   * @param aParent Composite - родительская компонента
   * @param aOwnerDialog TsDialog - родительский диалог
   */
  public PanelUgwiSelector( Composite aParent, TsDialog<Ugwi, ITsGuiContext> aOwnerDialog ) {
    super( aParent, aOwnerDialog );
    coreApi = ValedUgwiSelectorFactory.REFDEF_CORE_API.getRef( environ() );
    TsInternalErrorRtException.checkNull( coreApi );
    Composite bkPanel = new Composite( aParent, SWT.NONE );
    bkPanel.setLayoutData( BorderLayout.CENTER );
    // init Ugwi kind panel
    initKindPanel( bkPanel );
    // init selection of concrete panel
    initSelectPanel( bkPanel );
  }

  private void initKindPanel( Composite aBkPanel ) {
    Composite kindBackPanel = new Composite( aBkPanel, SWT.NONE );
    // place to the nort of parent
    kindBackPanel.setLayoutData( BorderLayout.NORTH );
    // get selection options
    if( environ().params().hasValue( ValedUgwiSelectorFactory.OPDEF_SINGLE_UGWI_KIND_ID ) ) {
      createSingleKindPanel( kindBackPanel );
      return;
    }
    else
      if( environ().params().hasValue( ValedUgwiSelectorFactory.OPDEF_UGWI_KIND_IDS_LIST ) ) {
        createKindComboPanel( kindBackPanel );
        return;
      }
    throw new TsIllegalStateRtException( "No Ugwi kind to select" );
  }

  /**
   * Create panel to select Ugwi kind through combo
   *
   * @param aBkPanel - back panel
   */
  private void createKindComboPanel( Composite aBkPanel ) {
    GridLayout gl = new GridLayout( 2, false );
    aBkPanel.setLayout( gl );
    CLabel l = new CLabel( aBkPanel, SWT.LEFT );
    l.setText( "Типы: " );
    IStringList ugwiKindList = environ().params().getValobj( ValedUgwiSelectorFactory.OPDEF_UGWI_KIND_IDS_LIST );
    ITsVisualsProvider<String> visualsProvider = aItem -> aItem;
    ugwiKindsCombo = new ValedComboSelector<>( tsContext(), ugwiKindList, visualsProvider );
    ugwiKindsCombo.createControl( aBkPanel ).setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    ugwiKindsCombo.setSelectedItem( ugwiKindList.first() );

    ugwiKindsCombo.eventer().addListener( ( aSource, aEditFinished ) -> {
      String ugwiKindId = ugwiKindsCombo.selectedItem();
      if( ugwiKindId != null ) {
        ISkUgwiKind uKind = coreApi.ugwiService().listKinds().findByKey( ugwiKindId );
        if( uKind != null ) {
          if( selectPanel != null ) {
            selectPanel.dispose();
          }
          ugwSelector = ugwiHelper.createUgwiSelectorPanel( environ() );
          selectPanel = ugwSelector.createControl( aBkPanel );
          aBkPanel.layout( true );
        }
      }
    } );
  }

  /**
   * Create panel to display fixed type of Ugwi
   *
   * @param aBackPanel - back panel
   */
  private void createSingleKindPanel( Composite aBackPanel ) {
    String ugwiKindId = environ().params().getValobj( ValedUgwiSelectorFactory.OPDEF_SINGLE_UGWI_KIND_ID );
    GridLayout gl = new GridLayout( 2, false );
    aBackPanel.setLayout( gl );
    CLabel l = new CLabel( aBackPanel, SWT.LEFT );
    l.setText( "Тип: " );
    fixedKindText = new Text( aBackPanel, SWT.BORDER );
    fixedKindText.setEditable( false );
    fixedKindText.setLayoutData( new GridData( SWT.FILL, SWT.LEFT, true, false ) );
    fixedKindText.setText( ugwiKindId );
  }

  protected void initSelectPanel( Composite aParent ) {
    Composite bkPanel = new Composite( aParent, SWT.NONE );
    bkPanel.setLayoutData( BorderLayout.CENTER );
    bkPanel.setLayout( new BorderLayout() );
    IGenericSelectorPanel<Ugwi> selector = ugwiHelper.createUgwiSelectorPanel( environ() );
    selectPanel = selector.createControl( bkPanel );
  }

  @Override
  protected void doSetDataRecord( Ugwi aUgwi ) {
    if( aUgwi != null ) {
      if( environ().params().hasValue( ValedUgwiSelectorFactory.OPDEF_SINGLE_UGWI_KIND_ID ) ) {
        fixedKindText.setText( aUgwi.kindId() );
      }
      else
        if( environ().params().hasValue( ValedUgwiSelectorFactory.OPDEF_UGWI_KIND_IDS_LIST ) ) {
          ugwiKindsCombo.setSelectedItem( aUgwi.kindId() );
        }
      ugwSelector.setSelectedItem( aUgwi );
    }
  }

  @Override
  protected Ugwi doGetDataRecord() {
    return ugwSelector.selectedItem();
  }

  @Override
  protected ValidationResult doValidate() {
    return ValidationResult.SUCCESS;
  }

  // ------------------------------------------------------------------------------------
  // Static methods to show dialog
  //

  /**
   * Выводит диалог выбора Ugwi.
   * <p>
   *
   * @param aContext {@link ITsGuiContext} - контекст
   * @param aInitUgwi {@link Ugwi} для инициализации
   * @param aCoreApi {@link ISkCoreApi} core API
   * @param aSingleUgwiKindId {@link String} kind id
   * @return {@link Ugwi} - выбранный параметр <b>null</b> в случает отказа от редактирования
   */
  public static final Ugwi selectUgwiSingleKind( ITsGuiContext aContext, Ugwi aInitUgwi, ISkCoreApi aCoreApi,
      String aSingleUgwiKindId ) {
    TsNullArgumentRtException.checkNulls( aContext, aCoreApi );
    ValedUgwiSelectorFactory.REFDEF_CORE_API.setRef( aContext, aCoreApi );
    ValedUgwiSelectorFactory.OPDEF_SINGLE_UGWI_KIND_ID.setValue( aContext.params(), avStr( aSingleUgwiKindId ) );

    IDialogPanelCreator<Ugwi, ITsGuiContext> creator = PanelUgwiSelector::new;
    ITsDialogInfo dlgInfo = new TsDialogInfo( aContext, "Ugwi selector", "Select Ugwi and press Ok" );
    TsDialog<Ugwi, ITsGuiContext> d = new TsDialog<>( dlgInfo, aInitUgwi, aContext, creator );
    return d.execData();
  }

  /**
   * Выводит диалог выбора Ugwi.
   * <p>
   *
   * @param aContext {@link ITsGuiContext} - контекст
   * @param aInitUgwi {@link Ugwi} для инициализации
   * @param aCoreApi {@link ISkCoreApi} core API
   * @param aUgwiKindIdList {@link String} kind id
   * @return {@link Ugwi} - выбранный параметр <b>null</b> в случает отказа от редактирования
   */
  public static final Ugwi selectUgwiListKinds( ITsGuiContext aContext, Ugwi aInitUgwi, ISkCoreApi aCoreApi,
      IStringList aUgwiKindIdList ) {
    TsNullArgumentRtException.checkNulls( aContext, aCoreApi );
    ValedUgwiSelectorFactory.REFDEF_CORE_API.setRef( aContext, aCoreApi );
    ValedUgwiSelectorFactory.OPDEF_UGWI_KIND_IDS_LIST.setValue( aContext.params(), avValobj( aUgwiKindIdList ) );

    IDialogPanelCreator<Ugwi, ITsGuiContext> creator = PanelUgwiSelector::new;
    ITsDialogInfo dlgInfo = new TsDialogInfo( aContext, "Ugwi selector", "Select Ugwi and press Ok" );
    TsDialog<Ugwi, ITsGuiContext> d = new TsDialog<>( dlgInfo, aInitUgwi, aContext, creator );
    return d.execData();
  }

}
