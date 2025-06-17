package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import javax.persistence.EntityManager;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.common.sysdescr.SkSysdescrReader;

/**
 * Реализация {@link ISkSysdescrReader} для работы в сервере
 *
 * @author mvk
 */
final class S5BackendSysdescrReader
    extends SkSysdescrReader
    implements IS5ClassesInterceptor {

  /**
   * Менеджер постоянства
   */
  private final EntityManager entityManager;

  /**
   * Поддержка чтения системного писания
   */
  private final IS5BackendSysDescrSingleton backendSysdescr;

  /**
   * Конструктор
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менджер постоянства
   * @param aBackendSysdescr {@link IS5BackendSysDescrSingleton} поддержка чтения системного описания
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5BackendSysdescrReader( EntityManager aEntityManager, IS5BackendSysDescrSingleton aBackendSysdescr ) {
    super( aBackendSysdescr );
    TsNullArgumentRtException.checkNulls( aEntityManager, aBackendSysdescr );
    entityManager = aEntityManager;
    backendSysdescr = aBackendSysdescr;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClassesInterceptor
  //
  @Override
  public void beforeCreateClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void afterCreateClass( IDtoClassInfo aClassInfo ) {
    setClassInfos( detach( entityManager, backendSysdescr.readClassInfos() ) );
  }

  @Override
  public void beforeUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    // nop
  }

  @Override
  public void afterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    setClassInfos( detach( entityManager, backendSysdescr.readClassInfos() ) );
  }

  @Override
  public void beforeDeleteClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void afterDeleteClass( IDtoClassInfo aClassInfo ) {
    setClassInfos( detach( entityManager, backendSysdescr.readClassInfos() ) );
  }

  /**
   * Отсоединение entity-сущностей от JPA
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aInfos {@link IStridablesList}&lt;T%gt; список описаний
   * @return {@link IStridablesList}&lt;T%gt; список описаний
   * @param <T> тип сущности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <T extends IStridable> IStridablesList<T> detach( EntityManager aEntityManager, IStridablesList<T> aInfos ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aInfos );
    for( T info : aInfos ) {
      aEntityManager.detach( info );
    }
    return aInfos;
  }
}
