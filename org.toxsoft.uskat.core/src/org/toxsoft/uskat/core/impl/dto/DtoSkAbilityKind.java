package org.toxsoft.uskat.core.impl.dto;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.users.ability.*;

/**
 * {@link IDtoSkAbilityKind} editable implementation.
 *
 * @author hazard157
 */
public class DtoSkAbilityKind
    extends StridableParameterizedSer
    implements IDtoSkAbilityKind {

  private static final long serialVersionUID = -567148142342794486L;

  /**
   * Constructor.
   *
   * @param aKindId String - the ability kind ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public DtoSkAbilityKind( String aKindId ) {
    super( aKindId );
  }

  /**
   * Static constructor.
   *
   * @param aKindId String - the ability kind ID
   * @param aName String - the name
   * @param aDescription String - the description
   * @return {@link DtoSkAbilityKind} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public static DtoSkAbilityKind create( String aKindId, String aName, String aDescription ) {
    DtoSkAbilityKind d = new DtoSkAbilityKind( aKindId );
    d.setNameAndDescription( aName, aDescription );
    return d;
  }

  // ------------------------------------------------------------------------------------
  // IDtoSkAbilityKind
  //

  // nop

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

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return id();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof IDtoSkAbilityKind that ) {
      return id().equals( that.id() ) && this.params().equals( that.params() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
