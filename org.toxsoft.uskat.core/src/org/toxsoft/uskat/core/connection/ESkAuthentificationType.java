package org.toxsoft.uskat.core.connection;

import static org.toxsoft.uskat.core.connection.ISkConnectionConstants.*;
import static org.toxsoft.uskat.core.connection.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The authentification types available for backends.
 *
 * @author hazard157
 */
public enum ESkAuthentificationType
    implements IStridable, ITsValidator<IOptionSet> {

  /**
   * No authentification required.
   */
  NONE( "none", STR_N_SAT_NONE, STR_N_SAT_NONE, //$NON-NLS-1$
      ITsValidator.PASS ),

  /**
   * Simple authentification using login and password.
   */
  SIMPLE( "simple", STR_N_SAT_SIMPLE, STR_N_SAT_SIMPLE, //$NON-NLS-1$
      ops -> OptionSetUtils.validateOptionSet( ops, ALL_SIMPLE_AUTHENTIFICATION_ARGS ) ),

  ;

  /**
   * The keeper ID.
   */
  public static final String KEEPER_ID = "ESkAuthentificationType"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<ESkAuthentificationType> KEEPER =
      new StridableEnumKeeper<>( ESkAuthentificationType.class );

  private static IStridablesListEdit<ESkAuthentificationType> list = null;

  private final String                   id;
  private final String                   name;
  private final String                   description;
  private final ITsValidator<IOptionSet> validator;

  ESkAuthentificationType( String aId, String aName, String aDescription, ITsValidator<IOptionSet> aValidator ) {
    id = aId;
    name = aName;
    description = aDescription;
    validator = aValidator;
  }

  // --------------------------------------------------------------------------
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
  // ITsValidator
  //

  @Override
  public ValidationResult validate( IOptionSet aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    return validator.validate( aValue );
  }

  // ----------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns all constants in single list.
   *
   * @return {@link IStridablesList}&lt; {@link ESkAuthentificationType} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<ESkAuthentificationType> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link ESkAuthentificationType} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static ESkAuthentificationType getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link ESkAuthentificationType} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ESkAuthentificationType findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( ESkAuthentificationType item : values() ) {
      if( item.name.equals( aName ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns the constant by the name.
   *
   * @param aName String - the name
   * @return {@link ESkAuthentificationType} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static ESkAuthentificationType getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}
