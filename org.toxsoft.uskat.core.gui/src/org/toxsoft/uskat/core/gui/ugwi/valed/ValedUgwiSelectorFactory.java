package org.toxsoft.uskat.core.gui.ugwi.valed;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.controls.helpers.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.uskat.core.*;

/**
 * Ugwi selector VALED.
 *
 * @author hazard157
 */
public class ValedUgwiSelectorFactory
    extends AbstractValedTextAndButton<Ugwi> {

  public static final ITsContextRefDef<ISkCoreApi> REFDEF_CORE_API = null;

  public static final IDataDef OPDEF_SINGLE_UGWI_KIND_ID = null; // EAtomicType.STRING

  public static final IDataDef OPDEF_UGWI_KIND_IDS_LIST = null;// EAtomicType.VALOBJ -< IStringList

  ValedUgwiSelectorFactory( ITsGuiContext aContext ) {
    super( aContext );
    // TODO Auto-generated constructor stub
  }

  @Override
  protected boolean doProcessButtonPress() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected void doDoSetUnvalidatedValue( Ugwi aValue ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected Ugwi doGetUnvalidatedValue() {
    // TODO Auto-generated method stub
    return null;
  }

}
