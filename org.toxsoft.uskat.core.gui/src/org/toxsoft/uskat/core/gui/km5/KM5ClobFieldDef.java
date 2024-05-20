package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * M5-field for {@link ISkObject} CLOB.
 *
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class KM5ClobFieldDef<T extends ISkObject>
    extends M5FieldDef<T, String> {

  private final IDtoClobInfo clobInfo;

  /**
   * Constructor.
   *
   * @param aClobInfo {@link IDtoClobInfo} - the CLOB info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5ClobFieldDef( IDtoClobInfo aClobInfo ) {
    super( TsNullArgumentRtException.checkNull( aClobInfo ).id(), String.class );
    clobInfo = aClobInfo;
  }

  // ------------------------------------------------------------------------------------
  // M5FieldDef
  //

  @Override
  protected String doGetFieldValue( T aEntity ) {
    String s = aEntity.getClob( clobInfo.id() );
    return s != null ? s : TsLibUtils.EMPTY_STRING; // TODO we need to return an empty string or return null?
  }

  @Override
  protected String doGetFieldValueName( T aEntity ) {
    // TODO what to returns as text representation?
    return "CLOB " + clobInfo.id(); //$NON-NLS-1$
  }

}
