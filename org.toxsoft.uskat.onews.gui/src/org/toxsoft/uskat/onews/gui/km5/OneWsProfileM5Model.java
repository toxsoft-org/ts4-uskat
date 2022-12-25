package org.toxsoft.uskat.onews.gui.km5;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.fields.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * M5-model of entities {@link IOneWsProfile}.
 *
 * @author hazard157
 */
public class OneWsProfileM5Model
    extends KM5ModelBasic<IOneWsProfile> {

  /**
   * The model ID.
   */
  public static final String MODEL_ID = SK_ID + ".km5.OneWsProfile"; //$NON-NLS-1$

  /**
   * Field {@link IOneWsProfile#id()}
   */
  public final M5AttributeFieldDef<IOneWsProfile> ID = new M5StdFieldDefId<>();

  /**
   * Конструктор.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public OneWsProfileM5Model( ISkConnection aConn ) {
    super( MODEL_ID, IOneWsProfile.class, aConn );
    addFieldDefs( ID, NAME, DESCRIPTION );

  }

  @Override
  protected IM5LifecycleManager<IOneWsProfile> doCreateLifecycleManager( Object aMaster ) {
    ISkConnectionSupplier connSup = OneWsProfileM5Model.this.eclipseContext().get( ISkConnectionSupplier.class );
    ISkConnection conn = connSup.defConn();

    return new OneWsProfileM5LifecycleManager( this, conn );
  }

}
