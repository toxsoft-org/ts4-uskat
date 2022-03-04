package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.toxsoft.core.log4j.Logger;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.s5.server.transactions.S5TransactionManager;

/**
 * Начало выполнения кода сервера S5 - второй (после {@link S5TransactionManager}) стартующий синглтон.
 *
 * @author mvk
 */
@Startup
@Singleton
@DependsOn( { //
    TRANSACTION_MANAGER_SINGLETON//
} )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
public class S5InitialSingleton
    implements IS5InitialSingleton {

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String INITIAL_ID = "S5InitialSingleton"; //$NON-NLS-1$

  /**
   * Менеджер постоянства, обеспечениват доступ к БД.
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Пустой конструктор.
   */
  public S5InitialSingleton() {
    LoggerUtils.setDefaultLogger( Logger.getLogger( "out" ) ); //$NON-NLS-1$
    LoggerUtils.setErrorLogger( Logger.getLogger( "error" ) ); //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5InitialSingletonLocal
  //

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Lock( LockType.READ )
  @Override
  public IOptionSet loadServiceConfig( String aServiceId ) {
    TsNullArgumentRtException.checkNull( aServiceId );
    S5ServiceConfigEntity sc = entityManager.find( S5ServiceConfigEntity.class, aServiceId );
    if( sc == null ) {
      return null;
    }
    return OptionSetKeeper.KEEPER.str2ent( sc.getServiceConfig() );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Lock( LockType.WRITE )
  @Override
  public void saveServiceConfig( String aServiceId, IOptionSet aServiceConfig ) {
    TsNullArgumentRtException.checkNull( aServiceId );
    String cfgString = OptionSetKeeper.KEEPER.ent2str( aServiceConfig );
    S5ServiceConfigEntity sc = entityManager.find( S5ServiceConfigEntity.class, aServiceId );
    if( sc == null ) {
      sc = new S5ServiceConfigEntity();
      sc.setServiceId( aServiceId );
      sc.setServiceConfig( cfgString );
      entityManager.persist( sc );
    }
    else {
      sc.setServiceConfig( cfgString );
      entityManager.merge( sc );
    }
  }

}
