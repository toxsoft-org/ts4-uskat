package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import javax.ejb.*;
import javax.persistence.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.transactions.*;

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
    ILogger defaultLogger = LoggerWrapper.getLogger( S5AbstractBackend.TS_DEFAULT_LOGGER );
    ILogger errorLogger = LoggerWrapper.getLogger( S5AbstractBackend.TS_ERROR_LOGGER );
    LoggerUtils.setDefaultLogger( defaultLogger );
    LoggerUtils.setErrorLogger( errorLogger );
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
