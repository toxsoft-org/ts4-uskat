package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesSingleton;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

/**
 * Реализация синглетона {@link IS5BackendQueriesSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LINKS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendQueriesSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendQueriesSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_QUERIES_ID = "S5BackendQueriesSingleton"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Конструктор.
   */
  public S5BackendQueriesSingleton() {
    super( BACKEND_QUERIES_ID, STR_D_BACKEND_QUERIES );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @SuppressWarnings( "nls" )
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendQueriesSingleton
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public String createQuery( IOptionSet aParams ) {
    TsNullArgumentRtException.checkNull( aParams );
    // TODO Auto-generated method stub
    return null;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void prepareQuery( String aQueryId, IStringMap<IDtoQueryParam> aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aParams );
    // TODO Auto-generated method stub

  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void execQuery( String aQueryId, IQueryInterval aTimeInterval ) {
    TsNullArgumentRtException.checkNulls( aQueryId, aTimeInterval );
    // TODO Auto-generated method stub

  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void cancel( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    // TODO Auto-generated method stub

  }

  @Override
  public void close( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    // TODO Auto-generated method stub

  }

}
