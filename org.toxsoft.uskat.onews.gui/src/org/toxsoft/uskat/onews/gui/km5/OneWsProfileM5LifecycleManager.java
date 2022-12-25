package org.toxsoft.uskat.onews.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * Lifecycle manager for {@link OneWsProfileM5Model}.
 * <p>
 * THis LM does NOT supports rules editing.
 *
 * @author dima
 */
public class OneWsProfileM5LifecycleManager
    extends M5LifecycleManager<IOneWsProfile, ISkConnection> {

  /**
   * Constructor.
   *
   * @param aModel {@link IM5Model} - the model
   * @param aMaster {@link ISkConnection} - master-object, the Sk-connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public OneWsProfileM5LifecycleManager( IM5Model<IOneWsProfile> aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private ISkOneWsService ows() {
    ISkOneWsService ows = (ISkOneWsService)master().coreApi().getService( ISkOneWsService.SERVICE_ID );
    return ows;
  }

  private static IOptionSet makeProfileAttrs( IM5Bunch<IOneWsProfile> aValues ) {
    IOptionSetEdit p = new OptionSet();
    p.setStr( FID_ID, aValues.getAsAv( FID_ID ).asString() );
    p.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
    p.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
    return p;
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected ValidationResult doBeforeCreate( IM5Bunch<IOneWsProfile> aValues ) {
    String id = aValues.getAsAv( FID_ID ).asString();
    IOptionSet attrs = makeProfileAttrs( aValues );
    // by default there is one rule allowing everything
    IList<OneWsRule> rules = new ElemArrayList<>( OneWsRule.RULE_ALLOW_ALL );
    return ows().svs().validator().canCreateProfile( id, attrs, rules );
  }

  @Override
  protected IOneWsProfile doCreate( IM5Bunch<IOneWsProfile> aValues ) {
    String id = aValues.getAsAv( FID_ID ).asString();
    IOptionSet attrs = makeProfileAttrs( aValues );
    // by default there is one rule allowing everything
    IList<OneWsRule> rules = new ElemArrayList<>( OneWsRule.RULE_ALLOW_ALL );
    return ows().defineProfile( id, attrs, rules );
  }

  @Override
  protected ValidationResult doBeforeEdit( IM5Bunch<IOneWsProfile> aValues ) {
    String id = aValues.getAsAv( FID_ID ).asString();
    IOptionSet attrs = makeProfileAttrs( aValues );
    // no changes in rules
    IList<OneWsRule> rules = new ElemArrayList<>( aValues.originalEntity().rules() );
    return ows().svs().validator().canEditProfile( id, attrs, rules, aValues.originalEntity() );
  }

  @Override
  protected IOneWsProfile doEdit( IM5Bunch<IOneWsProfile> aValues ) {
    String id = aValues.getAsAv( FID_ID ).asString();
    IOptionSet attrs = makeProfileAttrs( aValues );
    // no changes in rules
    IList<OneWsRule> rules = new ElemArrayList<>( aValues.originalEntity().rules() );
    return ows().defineProfile( id, attrs, rules );
  }

  @Override
  protected ValidationResult doBeforeRemove( IOneWsProfile aEntity ) {
    return ows().svs().validator().canRemoveProfile( aEntity.id() );
  }

  @Override
  protected void doRemove( IOneWsProfile aEntity ) {
    ows().removeProfile( aEntity.id() );
  }

  @Override
  protected IList<IOneWsProfile> doListEntities() {
    return ows().listProfiles();
  }
}
