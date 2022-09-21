package org.toxsoft.uskat.base.gui.km5.sgw;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.base.gui.km5.sgw.ISkResources.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * M5-mode base for all entities extending {@link IDtoClassPropInfoBase}.
 *
 * @author hazard157
 * @param <T> - modelled entity type
 */
public class SgwDtoPropInfoM5ModelBase<T extends IDtoClassPropInfoBase>
    extends M5Model<T>
    implements ISkConnected {

  /**
   * Attribute {@link T#id()}.
   */
  public final IM5AttributeFieldDef<T> ID = new M5AttributeFieldDef<>( FID_ID, DDEF_IDPATH ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PROP_ID, STR_D_PROP_ID );
      setFlags( M5FF_READ_ONLY | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( T aEntity ) {
      return avStr( aEntity.id() );
    }

  };

  /**
   * Attribute {@link T#nmName()}.
   */
  public final IM5AttributeFieldDef<T> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PROP_NAME, STR_D_PROP_NAME );
      setFlags( M5FF_READ_ONLY | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( T aEntity ) {
      return avStr( aEntity.nmName() );
    }

  };

  /**
   * Attribute {@link T#description()}.
   */
  public final IM5AttributeFieldDef<T> DESCRIPTION = new M5AttributeFieldDef<>( FID_DESCRIPTION, DDEF_DESCRIPTION ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PROP_DESCRIPTION, STR_D_PROP_DESCRIPTION );
      setFlags( M5FF_READ_ONLY | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( T aEntity ) {
      return avStr( aEntity.description() );
    }

  };

  private final ISkConnection conn;

  /**
   * Constructor.
   *
   * @param aId String - model ID
   * @param aEntityClass {@link Class}&lt;T&gt; - modelled entity type
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   * @throws TsItemAlreadyExistsRtException model with specified ID already exists in domain
   */
  protected SgwDtoPropInfoM5ModelBase( String aId, Class<T> aEntityClass, ISkConnection aConn ) {
    super( aId, aEntityClass );
    conn = TsNullArgumentRtException.checkNull( aConn );
    addFieldDefs( ID, NAME, DESCRIPTION );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return conn;
  }

}
