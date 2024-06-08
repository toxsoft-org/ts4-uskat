package org.toxsoft.uskat.core.gui.ugwi.valed;

import static org.toxsoft.core.tslib.ITsHardConstants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.ugwi.valed.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;

/**
 * Ugwi selector VALED.
 *
 * @author hazard157
 * @author dima
 */
public class ValedUgwiSelectorFactory {
  // extends AbstractValedTextAndButton<Ugwi> {
  //
  // /**
  // * The factory name.
  // */
  // public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".UgwiSelectorFactory"; //$NON-NLS-1$
  //
  // /**
  // * selected value
  // */
  // private Ugwi value = Ugwi.NONE;

  /**
   * ID of option {@link #OPDEF_SINGLE_UGWI_KIND_ID}.
   */
  public static final String OPID_SINGLE_UGWI_KIND_ID = TS_ID + ".gui.ugwi.valed.UgwiSingleKindId"; //$NON-NLS-1$

  /**
   * ID of option {@link #OPDEF_UGWI_KIND_IDS_LIST}.
   */
  public static final String OPID_UGWI_KIND_IDS_LIST = TS_ID + ".gui.ugwi.valed.UgwiKindIdsList"; //$NON-NLS-1$

  /**
   * {@link ValedUgwiSelectorFactory#params()} option: ID of the Ugwi kind .
   */
  public static final IDataDef OPDEF_SINGLE_UGWI_KIND_ID = DataDef.create( OPID_SINGLE_UGWI_KIND_ID, STRING, //
      // FIXME correct default type id
      TSID_DEFAULT_VALUE, UgwiKindSkAttr.KIND_ID );

  /**
   * {@link ValedUgwiSelectorFactory#params()} option: List of ID of the Ugwi kind.
   */
  public static final IDataDef OPDEF_UGWI_KIND_IDS_LIST = DataDef.create( OPID_UGWI_KIND_IDS_LIST, VALOBJ, //
      TSID_NAME, STR_N_UGWI_KIND_IDS_LIST, //
      TSID_DESCRIPTION, STR_D_UGWI_KIND_IDS_LIST, //
      TSID_KEEPER_ID, StringListKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY ) );

  // /**
  // * The factory class.
  // *
  // * @author dima
  // */
  // static class Factory
  // extends AbstractValedControlFactory {
  //
  // protected Factory() {
  // super( FACTORY_NAME );
  // }
  //
  // @SuppressWarnings( "unchecked" )
  // @Override
  // protected IValedControl<Ugwi> doCreateEditor( ITsGuiContext aContext ) {
  // AbstractValedControl<Ugwi, ?> e = new ValedUgwiSelectorFactory( aContext );
  // return e;
  // }
  //
  // }
  //
  // /**
  // * The factory singleton.
  // */
  // public static final AbstractValedControlFactory FACTORY = new Factory();
  //
  // ValedUgwiSelectorFactory( ITsGuiContext aContext ) {
  // super( aContext );
  // setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
  // setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_FALSE );
  // // TODO if need to increase height
  // // setParamIfNull( OPDEF_VERTICAL_SPAN, new AvIntegerShortImpl( 3 ) );
  // }
  //
  // @Override
  // protected boolean doProcessButtonPress() {
  // Ugwi selUgwi = Ugwi.NONE;
  // TsNullArgumentRtException.checkNull( tsContext() );
  // ISkCoreApi coreApi = REFDEF_CORE_API.getRef( tsContext(), null );
  // TsNullArgumentRtException.checkNull( coreApi );
  // // check what user want
  // if( tsContext().params().hasValue( OPDEF_SINGLE_UGWI_KIND_ID ) ) {
  // String ugwiKindId = tsContext().params().getStr( OPDEF_SINGLE_UGWI_KIND_ID );
  // selUgwi = PanelUgwiSelector.selectUgwiSingleKind( tsContext(), canGetValue().isOk() ? getValue() : null, coreApi,
  // ugwiKindId );
  // }
  // else
  // if( tsContext().params().hasValue( OPDEF_UGWI_KIND_IDS_LIST ) ) {
  // IStringList kindIdList = OPDEF_UGWI_KIND_IDS_LIST.getValue( tsContext().params() ).asValobj();
  // selUgwi = PanelUgwiSelector.selectUgwiListKinds( tsContext(), canGetValue().isOk() ? getValue() : null, coreApi,
  // kindIdList );
  // }
  // else {
  // throw new TsIllegalStateRtException( VALED_ERR_MSG_NO_UGWI_KIND );
  // }
  //
  // if( selUgwi != null && !selUgwi.equals( Ugwi.NONE ) ) {
  // doSetUnvalidatedValue( selUgwi );
  // value = selUgwi;
  // return true;
  // }
  // return false;
  // }
  //
  // @Override
  // protected void doDoSetUnvalidatedValue( Ugwi aValue ) {
  // String txt = TsLibUtils.EMPTY_STRING;
  // if( aValue != null ) {
  // txt = aValue.toString();
  // }
  // getTextControl().setText( txt );
  // }
  //
  // @Override
  // protected Ugwi doGetUnvalidatedValue() {
  // return value;
  // }

}
