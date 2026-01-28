package org.toxsoft.uskat.core.gui.valed.ugwi;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.gui.valed.ugwi.ISkResources.*;
import static org.toxsoft.uskat.core.gui.valed.ugwi.ValedUgwiSelector.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.ugwis.*;

/**
 * Ugwi selector VALED.
 *
 * @author hazard157
 * @author dima
 */
class ValedUgwiSelectorTextAndButton
    extends AbstractValedTextAndButton<Ugwi> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".ValobjUgwiSelectorTextAndButton"; //$NON-NLS-1$

  /**
   * Package-private factory singleton for {@link ValedAvUgwiSelectorTextAndButton} constructor.
   * <p>
   * This factory is <b>not</b> intended to be registered.
   */
  @SuppressWarnings( "unchecked" )
  static final IValedControlFactory FACTORY = new AbstractValedControlFactory( FACTORY_NAME ) {

    @Override
    protected IValedControl<Ugwi> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedUgwiSelectorTextAndButton( aContext );
    }

    @Override
    protected IValedControl<Ugwi> doCreateSingleLine( ITsGuiContext aContext ) {
      return new ValedUgwiSelectorTextAndButton( aContext );
    }
  };

  ValedUgwiSelectorTextAndButton( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_TRUE );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Returns UGWI created from the canonical string contained in the widget {@link #getTextControl()}.
   * <p>
   * If text control contains an empty string, returns {@link Ugwi#NONE}. If text control contains invalid canonical
   * string or an UGWI not of allowed kind, returns <code>null</code>.
   *
   * @return {@link Ugwi} - UGWI from text or <code>null</code>
   */
  private Ugwi retrieveUgwiFromText() {
    String s = getTextControl().getText();
    if( s.isEmpty() ) {
      return Ugwi.NONE;
    }
    Ugwi u = null;
    if( !Ugwi.validateCanonicalString( s ).isError() ) {
      u = Ugwi.fromCanonicalString( s );
    }
    if( u != null ) {
      if( !allowedUgwiKindIds().hasElem( u.kindId() ) ) {
        return null;
      }
    }
    return u;
  }

  /**
   * Returns UGWI kind IDs allowed for the edited UGWI.
   * <p>
   * The IDs list is determined by the current values of the options {@link ValedUgwiSelector#OPDEF_SINGLE_UGWI_KIND_ID}
   * and {@link ValedUgwiSelector#OPDEF_UGWI_KIND_IDS_LIST}.
   *
   * @return {@link IStringList} - allowed kind IDs
   */
  private IStringList allowedUgwiKindIds() {
    IStridablesList<AbstractUgwiKind<?>> knownKinds = SkUgwiUtils.listUgwiKinds();
    // use single kind ID if valid value is specified
    if( tsContext().params().hasValue( OPDEF_SINGLE_UGWI_KIND_ID ) ) {
      String kindId = tsContext().params().getStr( OPDEF_SINGLE_UGWI_KIND_ID );
      if( knownKinds.hasKey( kindId ) ) {
        return new SingleStringList( kindId );
      }
    }
    // use kind IDs list if valid options values are specified
    if( tsContext().params().hasValue( OPDEF_UGWI_KIND_IDS_LIST ) ) {
      IStringList optionKindIds = OPDEF_UGWI_KIND_IDS_LIST.getValue( tsContext().params() ).asValobj();
      IStringListEdit ll = new StringArrayList();
      for( String s : optionKindIds ) {
        if( knownKinds.keys().hasElem( s ) ) {
          ll.add( s );
        }
      }
      if( !ll.isEmpty() ) {
        return ll;
      }
    }
    return knownKinds.ids();
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedTextAndButton
  //

  @Override
  protected boolean doProcessButtonPress() {
    // prepare for selection
    Ugwi currValue = retrieveUgwiFromText();
    IStringList kindIds = allowedUgwiKindIds();
    TsInternalErrorRtException.checkTrue( kindIds.isEmpty() ); // can't happen empty kinds in SkUgwiUtils
    // select UGWI
    ITsGuiContext ctx = new TsGuiContext( tsContext() );
    ctx.params().setBool( OPDEF_IS_SINGLE_LINE_UI, false );
    Ugwi selUgwi;
    if( kindIds.size() == 1 ) {
      selUgwi = PanelUgwiSelector.selectUgwiSingleKind( ctx, currValue, kindIds.first() );
    }
    else {
      selUgwi = PanelUgwiSelector.selectUgwiListKinds( ctx, currValue, kindIds );
    }
    if( selUgwi == null ) {
      return false;
    }
    if( !selUgwi.equals( Ugwi.NONE ) ) {
      getTextControl().setText( selUgwi.canonicalString() );
    }
    else {
      getTextControl().setText( EMPTY_STRING );
    }
    return true;
  }

  @Override
  public ValidationResult doCanGetValue() {
    Ugwi u = retrieveUgwiFromText();
    if( u != null ) {
      return ValidationResult.SUCCESS;
    }
    return ValidationResult.error( MSG_ERR_INVALID_UGWI_CANONOCAL_STRING );
  }

  @Override
  protected void doDoSetUnvalidatedValue( Ugwi aValue ) {
    getTextControl().setText( aValue.canonicalString() );
  }

  @Override
  protected Ugwi doGetUnvalidatedValue() {
    return retrieveUgwiFromText();
  }

}
