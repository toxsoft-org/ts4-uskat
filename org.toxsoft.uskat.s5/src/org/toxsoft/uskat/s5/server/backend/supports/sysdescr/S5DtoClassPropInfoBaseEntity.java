package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.io.Serializable;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;

/**
 * Реализация интерфейса {@link IDtoClassInfo} способная маппироваться на таблицу базы данных
 * <p>
 *
 * @author mvk
 */
@MappedSuperclass
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
// 2020-05-17 mvk ??? ошибка записи классов в usecase: local-main сервера: @formatter:off
//at java.base/jdk.internal.reflect.Reflection.newIllegalAccessException(Reflection.java:361)
//at java.base/java.lang.reflect.AccessibleObject.checkAccess(AccessibleObject.java:591)
//at java.base/java.lang.reflect.Method.invoke(Method.java:558)
//at org.hibernate@5.3.13.Final//org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor.intercept(ByteBuddyInterceptor.java:56)
//at org.hibernate@5.3.13.Final//org.hibernate.proxy.ProxyConfiguration$InterceptorDispatcher.intercept(ProxyConfiguration.java:95)
//at deployment.ru.uskat.tm.server-deploy.jar//org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassEntity$HibernateProxy$1vW5g3US.id(Unknown Source)
//at deployment.ru.uskat.tm.server-deploy.jar//ru.toxsoft.tslib.strids.stridable.impl.StridablesList.put(StridablesList.java:68)
//at deployment.ru.uskat.tm.server-deploy.jar//ru.toxsoft.tslib.strids.stridable.impl.AbstractStridablesList.add(AbstractStridablesList.java:230)
//at deployment.ru.uskat.tm.server-deploy.jar//ru.toxsoft.tslib.strids.stridable.impl.AbstractStridablesList.add(AbstractStridablesList.java:1)
//at deployment.ru.uskat.tm.server-deploy.jar//org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5BackendSysDescrSingleton.readClassInfos(S5BackendSysDescrSingleton.java:271)
//@formatter:on
// abstract class S5DtoClassPropInfoBaseEntity
public abstract class S5DtoClassPropInfoBaseEntity
    implements IDtoClassInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Уникальный идентификатор типа данных в системе
   */
  @Id
  @Column( insertable = true,
      updatable = false,
      nullable = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin not null" )
  private String id;

  /**
   * Описание дополнительных параметров класса
   */
  @Lob
  @Column( nullable = false )
  private String paramsString;

  /**
   * Lazy
   */
  private transient IOptionSet params;

  /**
   * Конструктор .
   *
   * @param aId String идентификатор
   * @param aName String имя
   * @param aDescription String описание
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5DtoClassPropInfoBaseEntity( String aId, String aName, String aDescription ) {
    TsNullArgumentRtException.checkNulls( aId, aName, aDescription );
    id = aId;
    IOptionSetEdit p = new OptionSet();
    p.setStr( TSID_NAME, aName );
    p.setStr( TSID_DESCRIPTION, aDescription );
    paramsString = OptionSetKeeper.KEEPER.ent2str( p );
  }

  /**
   * Конструктор .
   *
   * @param aId String идентификатор
   * @param aParams {@link IOptionSet} параметры
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5DtoClassPropInfoBaseEntity( String aId, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aId, aParams );
    id = aId;
    paramsString = OptionSetKeeper.KEEPER.ent2str( aParams );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Обновление данных
   *
   * @param aSource {@link IDtoClassInfo} исходное описание
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void update( IDtoClassInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    paramsString = OptionSetKeeper.KEEPER.ent2str( aSource.params() );
    params = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDpuBase
  //
  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return params.getStr( TSID_NAME, TsLibUtils.EMPTY_STRING );
  }

  @Override
  public String description() {
    return params.getStr( TSID_DESCRIPTION, TsLibUtils.EMPTY_STRING );
  }

  @Override
  public IOptionSet params() {
    if( params == null ) {
      params = OptionSetKeeper.KEEPER.str2ent( paramsString );
    }
    return params;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return id();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof StridableParameterized that ) {
      return id().equals( that.id() ) && params().equals( that.params() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id().hashCode();
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
