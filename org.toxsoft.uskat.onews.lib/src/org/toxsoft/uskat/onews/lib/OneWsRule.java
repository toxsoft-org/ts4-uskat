package org.toxsoft.uskat.onews.lib;

import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.onews.lib.ITsResources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.*;
import org.toxsoft.core.tslib.bricks.filter.std.paramed.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The rule for granting access to the abilities (components) of a single workstation.
 * <p>
 * The rule works like this:
 * <ul>
 * <li>an instance of {@link ITsFilter} for {@link IParameterized} objects is creates based on {@link #filter()}. Note:
 * filter considers {@link IOneWsAbility} as {@link IParameterized} type ;</li>
 * <li>if filter acepts some ability, then {@link #permission()} is granted;</li>
 * <li>filter {@link #name()} is used only for GUI profile editor, not by OneWS service</li>
 * <li></li>
 * </ul>
 * This is immutable class.
 *
 * @author hazard157
 */
public final class OneWsRule
    implements Serializable {

  private static final long serialVersionUID = -3621459383857939797L;

  /**
   * Singleton of the rule denying everything.
   */
  public static final OneWsRule RULE_DENY_ALL =
      new OneWsRule( STR_N_RULE_DENY_ALL, ITsCombiFilterParams.ALL, EOwsPermission.DENY );

  /**
   * Singleton of the rule allowing everything.
   */
  public static final OneWsRule RULE_ALLOW_ALL =
      new OneWsRule( STR_N_RULE_ALLOW_ALL, ITsCombiFilterParams.ALL, EOwsPermission.ALLOW );

  /**
   * Registered keeper ID.
   */
  public static final String KEEPER_ID = SK_ID + "OneWsRule"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<OneWsRule> KEEPER =
      new AbstractEntityKeeper<>( OneWsRule.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, OneWsRule aEntity ) {
          aSw.writeQuotedString( aEntity.name() );
          aSw.writeSeparatorChar();
          TsCombiFilterParamsKeeper.KEEPER.write( aSw, aEntity.filterParams() );
          aSw.writeSeparatorChar();
          EOwsPermission.KEEPER.write( aSw, aEntity.permission() );
        }

        @Override
        protected OneWsRule doRead( IStrioReader aSr ) {
          String name = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          ITsCombiFilterParams filterParams = TsCombiFilterParamsKeeper.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          EOwsPermission permission = EOwsPermission.KEEPER.read( aSr );
          return new OneWsRule( name, filterParams, permission );
        }
      };

  /**
   * Factories registry used for rules filters.
   */
  public static final ITsFilterFactoriesRegistry<IParameterized> FILTER_FACTORIES_REGISTRY =
      new TsFilterFactoriesRegistry<>( IParameterized.class );

  static {
    // FIXME FILTER_FACTORIES_REGISTRY.register( StdFilterParamedOpVsConst.FACTORY );
    // FIXME FILTER_FACTORIES_REGISTRY.register( StdFilterParamedOpVsOp.FACTORY );
    // FIXME FILTER_FACTORIES_REGISTRY.register( StdFilterParamedStringOpMatcher.FACTORY );
    FILTER_FACTORIES_REGISTRY.register( StdFilterOptionVsConst.FACTORY );
  }

  private final String               name;
  private final ITsCombiFilterParams filterParams;
  private final EOwsPermission       permission;

  private transient ITsFilter<IParameterized> filter = null;

  /**
   * Constructor.
   *
   * @param aName String - human-readable short name
   * @param aParams {@link ITsCombiFilterParams} - params to create filter for {@link IParameterized} objects
   * @param aPermission {@link EOwsPermission} - permission level for accepted abilities
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException name is an empty string
   */
  public OneWsRule( String aName, ITsCombiFilterParams aParams, EOwsPermission aPermission ) {
    TsErrorUtils.checkNonEmpty( aName );
    TsNullArgumentRtException.checkNulls( aParams, aPermission );
    name = aName;
    filterParams = aParams;
    permission = aPermission;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  // TODO TRANSLATE

  /**
   * Returns abilities filter based on {@link #filterParams}.
   *
   * @return {@link ITsFilter}&lt;{@link IParameterized}&gt; - the abilities filter of this rule
   */
  public ITsFilter<IParameterized> filter() {
    if( filter == null ) {
      filter = TsCombiFilter.create( filterParams, FILTER_FACTORIES_REGISTRY );
    }
    return filter;
  }

  /**
   * Returns rule name.
   *
   * @return String - human-readable short name
   */
  public String name() {
    return name;
  }

  /**
   * Returns filter parameters to create {@link #filter()}.
   *
   * @return {@link ITsCombiFilterParams} - filter parameters
   */
  public ITsCombiFilterParams filterParams() {
    return filterParams;
  }

  /**
   * Returns permission level for abilities accepted by {@link #filter()}.
   *
   * @return {@link EOwsPermission} - permission to ability assigned by this rule
   */
  public EOwsPermission permission() {
    return permission;
  }

}
