package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkHistoryQueryService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServHistQuery
    extends AbstractSkCoreService
    implements ISkHistoryQueryService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServHistQuery::new;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServHistQuery( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    // TODO close all open queries
  }

  // ------------------------------------------------------------------------------------
  // ISkHistoryQueryService
  //

  @Override
  public ISkQueryRawHistory createHistoricQuery( IOptionSet aOptions ) {
    // TODO реализовать SkCoreServHistQuery.createHistoricQuery()
    throw new TsUnderDevelopmentRtException( "SkCoreServHistQuery.createHistoricQuery()" );
  }

  @Override
  public ISkQueryProcessedData createProcessedQuery( IOptionSet aOptions ) {
    // TODO реализовать SkCoreServHistQuery.createProcessedQuery()
    throw new TsUnderDevelopmentRtException( "SkCoreServHistQuery.createProcessedQuery()" );
  }

  // TODO createLanguageQuery()
  // @Override
  // public ISkQueryStatement createLanguageQuery( IOptionSet aOptions ) {
  // }

  @Override
  public IStringMap<ISkAsynchronousQuery> listOpenQueries() {
    // TODO Auto-generated method stub
    return null;
  }

}
