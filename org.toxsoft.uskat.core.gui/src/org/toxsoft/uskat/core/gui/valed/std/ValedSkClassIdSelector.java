package org.toxsoft.uskat.core.gui.valed.std;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Chooses class ID from the {@link ISkSysdescr#listClasses()}.
 * <p>
 * Value is the {@link String} class ID {@link ISkClassInfo#id()}.
 *
 * @author hazard157
 */
public class ValedSkClassIdSelector
    extends AbstractValedControl<String, Control> {

  private static final String OPID_PREFIX = SK_ID + ".valed.option.ClassIdSelector"; //$NON-NLS-1$

  /**
   * Option: determines tree (<code>true</code>) or list (<code>false</code>) mode at startup.<br>
   * Default value: <code>true</code>
   */
  public static final IDataDef OPDEF_IS_START_MODE_TREE = DataDef.create( OPID_PREFIX + ".IsStartModeTree", BOOLEAN, //$NON-NLS-1$
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Option: {@link ITsCombiFilterParams} to create class Id filter {@link ITsFilter}&lt;String&gt;.<br>
   * Default value: {@link ITsCombiFilterParams#ALL}
   */
  public static final IDataDef OPDEF_CLASS_ID_FILTER_PARAMS =
      DataDef.create( OPID_PREFIX + ".ClassIdFilterParams", BOOLEAN, //$NON-NLS-1$
          TSID_KEEPER_ID, TsCombiFilterParamsKeeper.KEEPER_ID, //
          TSID_DEFAULT_VALUE, TsCombiFilterParamsKeeper.AV_ALL //
      );

  // /**
  // * Option: ???.<br>
  // * Default value: ???
  // */
  // public static final IDataDef OPDEF_ = DataDef.create( OPID_PREFIX + ".IsStartModeTree", BOOLEAN, //$NON-NLS-1$
  // TSID_DEFAULT_VALUE, AV_TRUE //
  // );

  // private final ITsFilter<String> classIdFilter;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the VALED context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ValedSkClassIdSelector( ITsGuiContext aContext ) {
    super( aContext );
    ITsCombiFilterParams fp = OPDEF_CLASS_ID_FILTER_PARAMS.getValue( tsContext().params() ).asValobj();
    // ITsFilter<String> filter = TsCombiFilter.create( fp, ITsFilterFactoriesRegistry. )
  }

  // ------------------------------------------------------------------------------------
  // AbstractValedControl
  //

  @Override
  protected Control doCreateControl( Composite aParent ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doSetEditable( boolean aEditable ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected ValidationResult doCanGetValue() {
    // TODO Auto-generated method stub
    return super.doCanGetValue();
  }

  @Override
  protected String doGetUnvalidatedValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doSetUnvalidatedValue( String aValue ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doClearValue() {
    // TODO Auto-generated method stub
  }

}
