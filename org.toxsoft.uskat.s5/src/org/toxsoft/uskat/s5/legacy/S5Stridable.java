package org.toxsoft.uskat.s5.legacy;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * {@link IStridable} base implementation.
 * <p>
 * This class may be subclasses (including immatable classes) or used directly.
 *
 * @author hazard157
 */
public class S5Stridable
    implements IStridable {

  private final String id;

  private String name        = EMPTY_STRING;
  private String description = EMPTY_STRING;

  /**
   * Constructor.
   *
   * @param aId String - entity ID (an IDpath)
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public S5Stridable( String aId ) {
    this( aId, EMPTY_STRING, EMPTY_STRING, true );
  }

  /**
   * Constructor.
   *
   * @param aId String - entity ID (an IDpath)
   * @param aName String - short name
   * @param aDescription String - description
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public S5Stridable( String aId, String aName, String aDescription ) {
    this( aId, aName, aDescription, true );
  }

  /**
   * Constructor.
   *
   * @param aId String - entity ID (an IDpath)
   * @param aName String - short name
   * @param aDescription String - description
   * @param aAllowIdPath boolean - <code>true</code> to allow IDpath as ID, not only IDname
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath or IDname, depending on aAllowIdPath value
   */
  public S5Stridable( String aId, String aName, String aDescription, boolean aAllowIdPath ) {
    if( aId == null || aName == null || aDescription == null ) {
      throw new TsNullArgumentRtException();
    }
    if( aAllowIdPath ) {
      StridUtils.checkValidIdPath( aId );
    }
    else {
      StridUtils.checkValidIdName( aId );
    }
    id = aId;
    name = aName;
    description = aDescription;
  }

  /**
   * Copy constructor.
   *
   * @param aSource {@link IStridable} - the source
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5Stridable( IStridable aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    id = StridUtils.checkValidIdPath( aSource.id() );
    name = aSource.nmName();
    description = aSource.description();
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return name;
  }

  @Override
  public String description() {
    return description;
  }

  // ------------------------------------------------------------------------------------
  // Proptected editing API may be public in mutable or remain protected in immutable final classes
  //

  /**
   * Sets {@link #nmName()}.
   *
   * @param aName String - short name
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected void setName( String aName ) {
    name = TsNullArgumentRtException.checkNull( aName );
  }

  /**
   * Sets {@link #description()}.
   *
   * @param aDescription String - description
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected void setDescription( String aDescription ) {
    description = TsNullArgumentRtException.checkNull( aDescription );
  }

  /**
   * Sets {@link #nmName()} and {@link #description()}.
   *
   * @param aName String - short name
   * @param aDescription String - description
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected void setNameAndDescription( String aName, String aDescription ) {
    if( aName == null || aDescription == null ) {
      throw new TsNullArgumentRtException();
    }
    name = aName;
    description = aDescription;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return id + " - " + name; //$NON-NLS-1$
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof IStridable obj ) {
      return id.equals( obj.id() ) && name.equals( obj.nmName() ) && description.equals( obj.description() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id.hashCode();
    result = TsLibUtils.PRIME * result + name.hashCode();
    result = TsLibUtils.PRIME * result + description.hashCode();
    return result;
  }

}
