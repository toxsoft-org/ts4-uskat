package org.toxsoft.uskat.core.gui.km5.sded.sded.editors;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.km5.sded.sded.editors.ITsResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Allows to select {@link SkidList} by accessing {@link ISkObjectService}.
 *
 * @author hazard157
 * @author dima
 */
public class ValedSkidListEditor
    extends AbstractValedTextAndButton<SkidList> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".SkidListEditor"; //$NON-NLS-1$

  /**
   * The factory class.
   *
   * @author hazard157
   */
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<SkidList> doCreateEditor( ITsGuiContext aContext ) {
      AbstractValedControl<SkidList, ?> e = new ValedSkidListEditor( aContext );
      return e;
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  /**
   * Constructor for subclasses.
   *
   * @param aContext {@link ITsGuiContext} - the valed context
   * @throws TsNullArgumentRtException аргумент = null
   */
  public ValedSkidListEditor( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_TRUE );
    setParamIfNull( OPDEF_VERTICAL_SPAN, AV_1 );

  }

  @Override
  protected boolean doProcessButtonPress() {
    // create and dispaly SkidList selector
    SkidList initVal = canGetValue().isOk() ? getValue() : null;

    SkidList selSkids = (SkidList)PanelSkidListSelector.selectSkids( initVal, tsContext() );

    if( selSkids != null ) {
      doSetUnvalidatedValue( selSkids );
      return true;
    }
    return false;
  }

  @Override
  public ValidationResult canGetValue() {
    try {
      SkidListKeeper.KEEPER.str2ent( getTextControl().getText() );
      return ValidationResult.SUCCESS;
    }
    catch( @SuppressWarnings( "unused" ) Exception ex ) {
      return ValidationResult.error( MSG_ERR_INV_SKID_FORMAT );
    }
  }

  @Override
  protected void doUpdateTextControl() {
    // nop
  }

  @Override
  protected SkidList doGetUnvalidatedValue() {
    return (SkidList)SkidListKeeper.KEEPER.str2ent( getTextControl().getText() );
  }

  @Override
  protected void doDoSetUnvalidatedValue( SkidList aValue ) {
    String txt = TsLibUtils.EMPTY_STRING;
    if( aValue != null ) {
      txt = SkidListKeeper.KEEPER.ent2str( aValue );
    }
    getTextControl().setText( txt );
  }

}
