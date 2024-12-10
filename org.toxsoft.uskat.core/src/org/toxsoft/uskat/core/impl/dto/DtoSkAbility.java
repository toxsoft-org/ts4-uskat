package org.toxsoft.uskat.core.impl.dto;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.api.users.ability.*;

/**
 * {@link IDtoSkAbility} editable implementation.
 *
 * @author hazard157
 */
public class DtoSkAbility
    extends StridableParameterizedSer
    implements IDtoSkAbility {

  private static final long serialVersionUID = 3399859012711798595L;

  private static final String OPID_KIND_ID = "ability.kind.id"; //$NON-NLS-1$

  /**
   * Constructor.
   *
   * @param aAbilityId String - the ability ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public DtoSkAbility( String aAbilityId ) {
    super( aAbilityId );
  }

  /**
   * Static constructor.
   *
   * @param aAbilityId String - the ability ID
   * @param aKindId String - the ability kind ID
   * @param aName String - the name
   * @param aDescription String - the description
   * @return {@link DtoSkAbility} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public static DtoSkAbility create( String aAbilityId, String aKindId, String aName, String aDescription ) {
    DtoSkAbility d = new DtoSkAbility( aAbilityId );
    d.setKindId( aKindId );
    d.setNameAndDescription( aName, aDescription );
    return d;
  }

  // ------------------------------------------------------------------------------------
  // IDtoSkAbility
  //

  @Override
  public String kindId() {
    return params().getStr( OPID_KIND_ID, ISkUserServiceHardConstants.ABILITY_KIND_ID_UNDEFINED );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets the {@link #kindId()}.
   *
   * @param aKindId String - the ability kind ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public void setKindId( String aKindId ) {
    StridUtils.checkValidIdPath( aKindId );
    params().setStr( OPID_KIND_ID, aKindId );
  }

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
    if( aThat instanceof IDtoSkAbility that ) {
      return id().equals( that.id() ) && this.params().equals( that.params() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
