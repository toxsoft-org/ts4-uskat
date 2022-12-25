package org.toxsoft.uskat.onews.lib.impl;

import static org.toxsoft.uskat.onews.lib.IOneWsConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * {@link IOneWsProfile} implementation.
 *
 * @author hazard157
 */
public class SkOneWsProfile
    extends SkObject
    implements IOneWsProfile {

  static final ISkObjectCreator<SkOneWsProfile> CREATOR = SkOneWsProfile::new;

  SkOneWsProfile( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  public IOptionSet params() {
    return attrs().getValobj( ATRID_PROFILE_PARAMS );
  }

  // ------------------------------------------------------------------------------------
  // IOneWsProfile
  //

  @Override
  public boolean isAllowed( IOneWsAbility aAbility ) {
    TsNullArgumentRtException.checkNull( aAbility );
    IList<OneWsRule> ll = rules();
    for( OneWsRule r : ll ) {
      if( r.filter().accept( aAbility ) ) {
        return r.permission().isAllowed();
      }
    }
    return false;
  }

  @Override
  public boolean isBuiltinProfile() {
    return OWS_BUILTIN_PROFILE_IDS.hasElem( id() );
  }

  @Override
  public IList<OneWsRule> rules() {
    String s = getClob( CLBID_PROFILE_RULES, IEntityKeeper.STR_EMPTY_COLLECTION_REPRESENTATION );
    return OneWsRule.KEEPER.str2coll( s );
  }

  @Override
  public IList<ISkRole> profileRoles() {
    return getLinkObjs( LNKID_ROLES );
  }

}
