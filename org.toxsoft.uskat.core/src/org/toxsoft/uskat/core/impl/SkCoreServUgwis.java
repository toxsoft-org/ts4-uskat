package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkUgwiService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServUgwis
    extends AbstractSkCoreService
    implements ISkUgwiService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServUgwis::new;

  private final IStridablesListEdit<AbstractUgwiKind<?>> kindsList = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  private SkCoreServUgwis( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkUgwiService
  //

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesList<IUgwiKind> listKinds() {
    return (IStridablesList)kindsList;
  }

  @Override
  public boolean isContent( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    AbstractUgwiKind<?> kind = kindsList.findByKey( aUgwi.kindId() );
    if( kind == null ) {
      return false;
    }
    return kind.isContent( aUgwi );
  }

  @Override
  public Object findContent( Ugwi aUgwi ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    AbstractUgwiKind<?> kind = kindsList.findByKey( aUgwi.kindId() );
    if( kind == null ) {
      return null;
    }
    return kind.findContent( aUgwi );
  }

  @Override
  public <T> T findContentAs( Ugwi aUgwi, Class<T> aContentClass ) {
    TsNullArgumentRtException.checkNull( aUgwi );
    AbstractUgwiKind<?> kind = kindsList.findByKey( aUgwi.kindId() );
    if( kind == null ) {
      return null;
    }
    return kind.findContentAs( aUgwi, aContentClass );
  }

  @Override
  public void registerKind( AbstractUgwiKind<?> aUgwiKind ) {
    TsNullArgumentRtException.checkNull( aUgwiKind );
    if( aUgwiKind.papiCanRegister( this ) ) {
      kindsList.put( aUgwiKind );
    }
  }

}
