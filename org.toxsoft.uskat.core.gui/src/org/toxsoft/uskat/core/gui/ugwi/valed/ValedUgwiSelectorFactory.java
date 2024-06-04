package org.toxsoft.uskat.core.gui.ugwi.valed;

import static org.toxsoft.core.tsgui.valed.api.IValedControlConstants.*;
import static org.toxsoft.core.tslib.ITsHardConstants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.ugwi.kinds.*;

/**
 * Ugwi selector VALED.
 *
 * @author hazard157
 * @author dima
 */
public class ValedUgwiSelectorFactory
    extends AbstractValedTextAndButton<Ugwi> {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = VALED_EDNAME_PREFIX + ".UgwiSelectorFactory"; //$NON-NLS-1$

  /**
   * selected value
   */
  private Ugwi value = Ugwi.NONE;

  /**
   * ID of option {@link #OPDEF_SINGLE_UGWI_KIND_ID}.
   */
  static String OPID_SINGLE_UGWI_KIND_ID = TS_ID + ".gui.ugwi.valed.UgwiSingleKindId"; //$NON-NLS-1$

  /**
   * ID of option {@link #OPDEF_SINGLE_UGWI_KIND_ID}.
   */
  static String OPID_UGWI_KIND_IDS_LIST = TS_ID + ".gui.ugwi.valed.UgwiSingleKindId"; //$NON-NLS-1$

  /**
   * The reference in the context to initialize {@link ISkCoreApi}.
   */
  public static final ITsContextRefDef<ISkCoreApi> REFDEF_CORE_API =
      new TsGuiContextRefDef<>( TS_FULL_ID + ".gui.ValedUgwiSelectorFactory.RefCoreApi", //$NON-NLS-1$
          ISkCoreApi.class, IOptionSet.NULL );

  /**
   * {@link ValedUgwiSelectorFactory#params()} option: ID of the Ugwi kind .
   */
  public static final IDataDef OPDEF_SINGLE_UGWI_KIND_ID = DataDef.create( OPID_SINGLE_UGWI_KIND_ID, STRING, //
      TSID_DEFAULT_VALUE, UgwiGuiHelperSkSkid.NONE_ID );

  /**
   * {@link ValedUgwiSelectorFactory#params()} option: ID of the Ugwi kind .
   */
  public static final IDataDef OPDEF_UGWI_KIND_IDS_LIST = DataDef.create( OPID_UGWI_KIND_IDS_LIST, VALOBJ, //
      TSID_NAME, "list of Ugwi kind ids", //
      TSID_DESCRIPTION, "List of Ugwi kind ids applyable in that context", //
      TSID_KEEPER_ID, StringListKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY ) );

  /**
   * The factory class.
   *
   * @author dima
   */
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected IValedControl<Ugwi> doCreateEditor( ITsGuiContext aContext ) {
      AbstractValedControl<Ugwi, ?> e = new ValedUgwiSelectorFactory( aContext );
      return e;
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

  ValedUgwiSelectorFactory( ITsGuiContext aContext ) {
    super( aContext );
    setParamIfNull( OPDEF_IS_WIDTH_FIXED, AV_FALSE );
    setParamIfNull( OPDEF_IS_HEIGHT_FIXED, AV_FALSE );
    // TODO if needed to increase height
    // setParamIfNull( OPDEF_VERTICAL_SPAN, new AvIntegerShortImpl( 3 ) );
  }

  @Override
  protected boolean doProcessButtonPress() {
    // FIXME for debug only
    TsNullArgumentRtException.checkNull( tsContext() );
    ISkConnectionSupplier connSupplier = tsContext().get( ISkConnectionSupplier.class );
    ISkConnection conn = connSupplier.defConn();
    IStringListEdit kindIdList = new StringArrayList();
    for( ISkUgwiKind kind : conn.coreApi().ugwiService().listKinds() ) {
      kindIdList.add( kind.id() );
    }
    OPDEF_UGWI_KIND_IDS_LIST.setValue( tsContext().params(), avValobj( kindIdList ) );

    // create and dispaly Ugwi selector
    Ugwi selUgwi = PanelUgwiSelector.selectUgwiListKinds( tsContext(), canGetValue().isOk() ? getValue() : null,
        conn.coreApi(), kindIdList );

    if( selUgwi != null ) {
      doSetUnvalidatedValue( selUgwi );
      value = selUgwi;
      return true;
    }
    return false;
  }

  @Override
  protected void doDoSetUnvalidatedValue( Ugwi aValue ) {
    String txt = TsLibUtils.EMPTY_STRING;
    if( aValue != null ) {
      txt = aValue.toString();
    }
    getTextControl().setText( txt );
  }

  @Override
  protected Ugwi doGetUnvalidatedValue() {
    return value;
  }

}
