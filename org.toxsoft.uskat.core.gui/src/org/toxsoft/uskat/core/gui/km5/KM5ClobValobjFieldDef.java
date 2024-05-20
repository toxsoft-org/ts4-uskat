package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * M5-field for {@link ISkObject} CLOB interpreted as atomic value-object.
 * <p>
 * It is assumed that value-object is converted to/from CLOB String using the methods
 * {@link IEntityKeeper#ent2str(Object)} and {@link IEntityKeeper#str2ent(String)} respectively.
 *
 * @author hazard157
 * @param <T> - modeled entity type
 * @param <V> - expected type of the value-object
 */
public class KM5ClobValobjFieldDef<T extends ISkObject, V>
    extends M5FieldDef<T, V> {

  private final IDtoClobInfo     clobInfo;
  private final IEntityKeeper<V> keeper;

  /**
   * Constructor.
   *
   * @param aClobInfo {@link IDtoClobInfo} - the CLOB info
   * @param aKeeper {@link IEntityKeeper}&lt;V&gt; - keeper of the value-object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5ClobValobjFieldDef( IDtoClobInfo aClobInfo, IEntityKeeper<V> aKeeper ) {
    super( TsNullArgumentRtException.checkNull( aClobInfo ).id(), aKeeper.entityClass() );
    clobInfo = aClobInfo;
    keeper = aKeeper;
  }

  // ------------------------------------------------------------------------------------
  // M5FieldDef
  //

  @Override
  protected V doGetFieldValue( T aEntity ) {
    String s = aEntity.getClob( clobInfo.id() );
    return keeper.str2ent( s );
  }

  @Override
  protected String doGetFieldValueName( T aEntity ) {
    // TODO what to returns as text representation?
    return "CLOB " + clobInfo.id(); //$NON-NLS-1$
  }

}
