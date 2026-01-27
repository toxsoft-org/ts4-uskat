package org.toxsoft.uskat.core.gui.valed.ugwi;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.valed.ugwi.ISkResources.*;
import static org.toxsoft.uskat.core.inner.ISkCoreGuiInnerSharedConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tsgui.valed.impl.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.uskat.core.api.ugwis.kinds.*;

/**
 * UGWI selector VALED, edits the value of type {@link Ugwi}..
 * <p>
 * Actually it is not a VALED, rather it declares UGWI selector VALED factory and applicable options. The factory
 * dispatches which VALED to create. Option are applicable also UGWI atomic value editors.
 *
 * @author hazard157
 * @author dima
 */
public class ValedUgwiSelector {

  /**
   * The factory name.
   */
  public static final String FACTORY_NAME = SKCGC_VALED_UGWI_SELECTOR;

  /**
   * ID of option {@link #OPDEF_SINGLE_UGWI_KIND_ID}.
   */
  public static final String OPID_SINGLE_UGWI_KIND_ID = SKCGC_VALED_UGWI_SELECTOR_OPID_SINGLE_UGWI_KIND_ID;

  /**
   * ID of option {@link #OPDEF_UGWI_KIND_IDS_LIST}.
   */
  public static final String OPID_UGWI_KIND_IDS_LIST = SKCGC_VALED_UGWI_SELECTOR_OPID_UGWI_KIND_IDS_LIST;

  /**
   * Creation option: ID of the Ugwi kind. <br>
   * This option has priority over {@link #OPDEF_UGWI_KIND_IDS_LIST}, if both are specified, only this option will be
   * used,
   */
  public static final IDataDef OPDEF_SINGLE_UGWI_KIND_ID = DataDef.create( OPID_SINGLE_UGWI_KIND_ID, STRING, //
      // FIXME correct default type id
      TSID_NAME, STR_SINGLE_UGWI_KIND_ID, //
      TSID_DESCRIPTION, STR_SINGLE_UGWI_KIND_ID_D, //
      TSID_DEFAULT_VALUE, avStr( UgwiKindSkAttr.KIND_ID ) //
  );

  /**
   * Creation option: List of ID of the Ugwi kind. <br>
   * If list empty than UGWI of any kind is allowed.
   */
  public static final IDataDef OPDEF_UGWI_KIND_IDS_LIST = DataDef.create( OPID_UGWI_KIND_IDS_LIST, VALOBJ, //
      TSID_NAME, STR_UGWI_KIND_IDS_LIST, //
      TSID_DESCRIPTION, STR_UGWI_KIND_IDS_LIST_D, //
      TSID_KEEPER_ID, StringListKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY ) );

  /**
   * The factory class.
   *
   * @author dima
   */
  @SuppressWarnings( "unchecked" )
  static class Factory
      extends AbstractValedControlFactory {

    protected Factory() {
      super( FACTORY_NAME );
    }

    @Override
    protected IValedControl<Ugwi> doCreateEditor( ITsGuiContext aContext ) {
      return new ValedUgwiSelectorTable( aContext );
    }

    @Override
    protected IValedControl<Ugwi> doCreateSingleLine( ITsGuiContext aContext ) {
      return new ValedUgwiSelectorTextAndButton( aContext );
    }

  }

  /**
   * The factory singleton.
   */
  public static final AbstractValedControlFactory FACTORY = new Factory();

}
