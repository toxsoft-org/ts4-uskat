package org.toxsoft.uskat.core.impl.dto;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoClassPropInfoBase} implementation.
 *
 * @author hazard157
 */
public abstract class DtoAbstractClassPropInfoBase
    extends StridableParameterizedSer
    implements IDtoClassPropInfoBase {

  private static final long serialVersionUID = 157157L;

  /**
   * Constructor.
   *
   * @param aId String - identifier (IDpath)
   * @param aParams {@link IOptionSet} - parameters values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  protected DtoAbstractClassPropInfoBase( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public abstract ESkClassPropKind kind();

  // ------------------------------------------------------------------------------------
  // API
  //

  @Override
  public void setName( String aName ) {
    super.setName( aName );
  }

  @Override
  public void setDescription( String aDescription ) {
    super.setDescription( aDescription );
  }

  @Override
  public void setNameAndDescription( String aName, String aDescription ) {
    super.setNameAndDescription( aName, aDescription );
  }

}
