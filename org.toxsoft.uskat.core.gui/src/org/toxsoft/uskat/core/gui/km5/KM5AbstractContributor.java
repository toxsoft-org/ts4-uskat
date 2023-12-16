package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Base implementation of M5-model contributor for USkat entities.
 *
 * @author hazard157
 */
public abstract class KM5AbstractContributor
    implements ISkConnected {

  private final ISkConnection skConn;
  private final IM5Domain     m5;

  /**
   * Constructor.
   * <p>
   * Constructor does nothing - simply remembers arguments in internal fields.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5AbstractContributor( ISkConnection aConn, IM5Domain aDomain ) {
    TsNullArgumentRtException.checkNulls( aConn, aDomain );
    skConn = aConn;
    m5 = aDomain;
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return skConn;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the bind domain created in constructor.
   * <p>
   * <b>Important note:</b> created domain {@link #m5()} is not intended to have models other than created by this
   * class. All other models may be removed at random modemt of time. If other models are needed, they may be added
   * either in the parent domain or by the registered {@link KM5AbstractContributor}.
   *
   * @return {@link IM5Domain} - the domain bind to the connection {@link #skConn()}
   */
  public IM5Domain m5() {
    return m5;
  }

  // ------------------------------------------------------------------------------------
  // package API to implement/override
  //

  /**
   * Implementation must create M5- models on connection opening.
   *
   * @return {@link IStringList} - IDs list of the contributed M5-models
   */
  protected abstract IStringList papiCreateModels();

  /**
   * Implementation must update contributed M5-models on sysdescr change if needed.
   * <p>
   * Note: this method is called only when sk-class description was changed while connection is open. Hence if subclass
   * contributor models only unchangeable entities (like {@link IDtoAttrInfo} or Java code defined classes) then
   * subclass does not needs to override this method.
   * <p>
   * In base class does nothing, there is no need to call superclass method when overriding.
   *
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aClassId String - affected class ID or <code>null</code>
   */
  protected void papiUpdateModel( ECrudOp aOp, String aClassId ) {
    // nop
  }

  /**
   * Called after connection is closed and all KM5-created models are removed from domain.
   * <p>
   * This method is intended to perform release resources if they were allocated earlier by this contributor.
   * <p>
   * Note: at the time of call connection is closed so {@link ISkConnection#state()} = {@link ESkConnState#CLOSED}.
   * <p>
   * In base class does nothing, there is no need to call superclass methodwhen overriding.
   */
  protected void papiAfterConnectionClose() {
    // nop
  }

}
