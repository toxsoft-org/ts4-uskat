package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import javax.persistence.EntityManager;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.IDpuSdTypeInfo;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;
import ru.uskat.core.common.helpers.sysdescr.SkSysdescrReader;

/**
 * Реализация {@link ISkSysdescrReader} для работы в сервере
 *
 * @author mvk
 */
final class S5BackendSysdescrReader
    extends SkSysdescrReader
    implements IS5TypesInterceptor, IS5ClassesInterceptor {

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
   * @param aEntityManager {@link EntityManager} менджер постоянства
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
  // Реализация IS5TypesInterceptor
  //
  @Override
  public void beforeCreateType( IDpuSdTypeInfo aTypeInfo ) {
    // nop
  }

  @Override
  public void afterCreateType( IDpuSdTypeInfo aTypeInfo ) {
    setTypeInfos( detach( entityManager, backendSysdescr.readTypeInfos() ) );
  }

  @Override
  public void beforeUpdateType( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    // nop
  }

  @Override
  public void afterUpdateType( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    setTypeInfos( detach( entityManager, backendSysdescr.readTypeInfos() ) );
  }

  @Override
  public void beforeDeleteType( IDpuSdTypeInfo aTypeInfo ) {
    // nop
  }

  @Override
  public void afterDeleteType( IDpuSdTypeInfo aTypeInfo ) {
    setTypeInfos( detach( entityManager, backendSysdescr.readTypeInfos() ) );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClassesInterceptor
  //
  @Override
  public void beforeCreateClass( IDpuSdClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void afterCreateClass( IDpuSdClassInfo aClassInfo ) {
    setClassInfos( detach( entityManager, backendSysdescr.readClassInfos() ) );
  }

  @Override
  public void beforeUpdateClass( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    // nop
  }

  @Override
  public void afterUpdateClass( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    setClassInfos( detach( entityManager, backendSysdescr.readClassInfos() ) );
  }

  @Override
  public void beforeDeleteClass( IDpuSdClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void afterDeleteClass( IDpuSdClassInfo aClassInfo ) {
    setClassInfos( detach( entityManager, backendSysdescr.readClassInfos() ) );
  }

  /**
   * Отсоединение entity-сущностей от JPA
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
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
