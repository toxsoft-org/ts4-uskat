package org.toxsoft.uskat.core.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.km5.ISkKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Base class of all KM5 modeled Sk-objects.
 * <p>
 * This class only declares {@link ISkObject} attributes and introduces an {@link ISkConnected} implementation
 * {@link #skConn()}. This class assumes that the Sk-connection, the same as used in domain must be supplied in
 * constructor to allow easily write initialization code in subclass constructors. This class does <b>not</b> adds any
 * M5-field definition, lifecycle manager or custom GUI panels. Single purpose is to be the very base of other
 * implementations.
 * <p>
 * This class is intended to create M5-models of service classes in {@link M5AbstractCollectionPanel} implementation
 * like {@link ISkUser} or refbooks, etc. <i>Service classes</i> means classes claimed and owned by Sk-services other
 * that {@link ISkSysdescr}. See {@link ISkSysdescr#determineClassClaimingServiceId(String)}.
 * <p>
 * For subject area classes claimed by {@link ISkSysdescr} use {@link KM5ModelGeneric}.
 *
 * @see ISkSysdescr#determineClassClaimingServiceId(String)
 * @see KM5ModelGeneric
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class KM5ModelBasic<T extends ISkObject>
    extends KM5ConnectedModelBase<T> {

  /**
   * Attribute {@link ISkObject#classId()} - {@link ISkHardConstants#AID_CLASS_ID}.
   */
  public final KM5AttributeFieldDef<T> SKID = new KM5AttributeFieldDef<>( ISkHardConstants.AID_SKID, DDEF_VALOBJ, //
      TSID_NAME, STR_N_FDEF_SKID, //
      TSID_DESCRIPTION, STR_D_FDEF_SKID, //
      M5_OPDEF_FLAGS, avInt( M5FF_HIDDEN | M5FF_INVARIANT ) //
  );

  /**
   * Attribute {@link ISkObject#classId()} - {@link ISkHardConstants#AID_CLASS_ID}.
   */
  public final KM5AttributeFieldDef<T> CLASS_ID = new KM5AttributeFieldDef<>( AID_CLASS_ID, DDEF_IDPATH, //
      TSID_NAME, STR_N_FDEF_CLASS_ID, //
      TSID_DESCRIPTION, STR_D_FDEF_CLASS_ID, //
      M5_OPDEF_FLAGS, avInt( M5FF_INVARIANT ) //
  );

  /**
   * Attribute {@link ISkObject#strid()} - {@link ISkHardConstants#AID_STRID}.
   */
  public final KM5AttributeFieldDef<T> STRID = new KM5AttributeFieldDef<>( AID_STRID, DDEF_IDPATH, //
      TSID_NAME, STR_N_FDEF_STRID, //
      TSID_DESCRIPTION, STR_D_FDEF_STRID, //
      M5_OPDEF_FLAGS, avInt( M5FF_COLUMN | M5FF_INVARIANT ) //
  );

  /**
   * Attribute {@link ISkObject#nmName()} - {@link ISkHardConstants#AID_NAME}.
   */
  public final KM5AttributeFieldDef<T> NAME = new KM5AttributeFieldDef<>( AID_NAME, DDEF_NAME, //
      TSID_NAME, STR_N_FDEF_NAME, //
      TSID_DESCRIPTION, STR_D_FDEF_NAME, //
      M5_OPDEF_FLAGS, avInt( M5FF_COLUMN ) //
  ) {

    @Override
    protected String doGetFieldValueName( T aEntity ) {
      String s = super.doGetFieldValueName( aEntity );
      if( !s.isBlank() ) {
        return s;
      }
      return aEntity.id();
    }

  };

  /**
   * Attribute {@link ISkObject#description()} - {@link ISkHardConstants#AID_DESCRIPTION}.
   */
  public final KM5AttributeFieldDef<T> DESCRIPTION = new KM5AttributeFieldDef<>( AID_DESCRIPTION, DDEF_DESCRIPTION, //
      TSID_NAME, STR_N_FDEF_DESCRIPTION, //
      TSID_DESCRIPTION, STR_D_FDEF_DESCRIPTION, //
      M5_OPDEF_FLAGS, avInt( M5FF_DETAIL ) //
  );

  /**
   * Constructor.
   *
   * @param aId String - the model ID mostly the same as {@link ISkObject#classId()}
   * @param aModelledClass {@link Class} - concrete java type of objects
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5ModelBasic( String aId, Class<T> aModelledClass, ISkConnection aConn ) {
    super( aId, aModelledClass, aConn );
    setNameAndDescription( STR_N_KM5M_OBJECT, STR_D_KM5M_OBJECT );
  }

  @Override
  protected IM5LifecycleManager<T> doCreateDefaultLifecycleManager() {
    return getLifecycleManager( this.skConn() );
  }

}
