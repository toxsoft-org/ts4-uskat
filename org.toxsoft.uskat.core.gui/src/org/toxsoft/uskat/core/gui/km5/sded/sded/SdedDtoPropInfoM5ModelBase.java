package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-mode base for all entities extending {@link IDtoClassPropInfoBase}.
 *
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class SdedDtoPropInfoM5ModelBase<T extends IDtoClassPropInfoBase>
    extends KM5ConnectedModelBase<T> {

  /**
   * Attribute {@link IDtoClassPropInfoBase#id()}.
   */
  public final IM5AttributeFieldDef<T> ID = new M5AttributeFieldDef<>( FID_ID, DDEF_IDPATH ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PROP_ID, STR_D_PROP_ID );
      setFlags( M5FF_INVARIANT | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( T aEntity ) {
      return avStr( aEntity.id() );
    }

  };

  /**
   * Attribute {@link IDtoClassPropInfoBase#nmName()}.
   */
  public final IM5AttributeFieldDef<T> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PROP_NAME, STR_D_PROP_NAME );
      setFlags( M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( T aEntity ) {
      return avStr( aEntity.nmName() );
    }

  };

  /**
   * Attribute {@link IDtoClassPropInfoBase#description()}.
   */
  public final IM5AttributeFieldDef<T> DESCRIPTION = new M5AttributeFieldDef<>( FID_DESCRIPTION, DDEF_DESCRIPTION ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_PROP_DESCRIPTION, STR_D_PROP_DESCRIPTION );
      setFlags( M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( T aEntity ) {
      return avStr( aEntity.description() );
    }

  };

  /**
   * Base class for lifecycle management.
   *
   * @author hazard157
   */
  protected class PropLifecycleManagerBase
      extends M5LifecycleManager<T, Object> {

    public PropLifecycleManagerBase( IM5Model<T> aModel ) {
      super( aModel, true, true, true, false, null );
    }

    @Override
    protected ValidationResult doBeforeCreate( IM5Bunch<T> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      if( !StridUtils.isValidIdPath( id ) ) {
        return ValidationResult.error( FMT_ERR_ID_NOT_IDPATH, id );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    protected ValidationResult doBeforeEdit( IM5Bunch<T> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      if( !StridUtils.isValidIdPath( id ) ) {
        return ValidationResult.error( FMT_ERR_ID_NOT_IDPATH, id );
      }
      return ValidationResult.SUCCESS;
    }

  }

  /**
   * Constructor.
   *
   * @param aId String - model ID
   * @param aEntityClass {@link Class}&lt;T&gt; - modeled entity type
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   * @throws TsItemAlreadyExistsRtException model with specified ID already exists in domain
   */
  protected SdedDtoPropInfoM5ModelBase( String aId, Class<T> aEntityClass, ISkConnection aConn ) {
    super( aId, aEntityClass, aConn );
    addFieldDefs( ID, NAME, DESCRIPTION );
  }

}
