package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.IKM5Sded2Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr.ISkResources.*;

import org.eclipse.swt.graphics.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.fields.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-model of {@link IDtoClassPropInfoBase} to list all properties in a single list.
 *
 * @author hazard157
 */
public class Sded2DtoPropInfoM5Model
    extends KM5ConnectedModelBase<IDtoClassPropInfoBase> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SDED2_M5_ID + ".DtoPropInfo"; //$NON-NLS-1$

  /**
   * Attribute {@link IDtoClassPropInfoBase#id()}.
   */
  public final IM5AttributeFieldDef<IDtoClassPropInfoBase> ID = new M5StdFieldDefId<>() {
    @Override
    protected void doInit() {
      setNameAndDescription( STR_PROP_ID, STR_PROP_ID_D );
      setFlags( M5FF_COLUMN | M5FF_INVARIANT );
    }

    protected Image doGetFieldValueIcon( IDtoClassPropInfoBase aEntity, EIconSize aIconSize ) {
      return iconManager().loadStdIcon( aEntity.kind().iconId(), aIconSize );
    }

    protected String doGetFieldValueName( IDtoClassPropInfoBase aEntity ) {
      return ' ' + super.doGetFieldValueName( aEntity );
    }

  };

  /**
   * Attribute {@link IDtoClassPropInfoBase#nmName()}.
   */
  public final IM5AttributeFieldDef<IDtoClassPropInfoBase> NAME = new M5StdFieldDefName<>() {
    @Override
    protected void doInit() {
      setNameAndDescription( STR_PROP_NAME, STR_PROP_NAME_D );
      setFlags( M5FF_COLUMN );
    }
  };

  /**
   * Attribute {@link IDtoClassPropInfoBase#description()}.
   */
  public final IM5AttributeFieldDef<IDtoClassPropInfoBase> DESCRIPTION = new M5StdFieldDefDescription<>() {
    @Override
    protected void doInit() {
      setNameAndDescription( STR_PROP_DESCRIPTION, STR_PROP_DESCRIPTION_D );
      setFlags( M5FF_DETAIL );
    }
  };

  /**
   * Attribute {@link IDtoClassPropInfoBase#description()}.
   */
  public final IM5AttributeFieldDef<IDtoClassPropInfoBase> KIND = new M5AttributeFieldDef<>( FID_PROP_KIND, VALOBJ, //
      TSID_NAME, STR_PROP_KIND, //
      TSID_DESCRIPTION, STR_PROP_KIND, //
      TSID_KEEPER_ID, ESkClassPropKind.KEEPER_ID, //
      M5_OPDEF_FLAGS, avInt( M5FF_READ_ONLY | M5FF_HIDDEN ) //
  ) {

    protected IAtomicValue doGetFieldValue( IDtoClassPropInfoBase aEntity ) {
      return avValobj( aEntity.kind() );
    }

    // protected String doGetFieldValueName( IDtoClassPropInfoBase aEntity ) {
    // return aEntity.kind().nmName();
    // }

  };

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public Sded2DtoPropInfoM5Model( ISkConnection aConn ) {
    super( MODEL_ID, IDtoClassPropInfoBase.class, aConn );
    addFieldDefs( KIND, ID, NAME, DESCRIPTION );
  }

}
