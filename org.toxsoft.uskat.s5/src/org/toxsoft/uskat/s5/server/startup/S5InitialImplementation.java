package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.classes.IS5ClassNode.*;
import static org.toxsoft.uskat.classes.IS5ClassServer.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.av.utils.IParameterizedEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.s5.common.S5Module;
import org.toxsoft.uskat.s5.common.S5ModuleList;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonCreator;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.entities.*;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceImplementation;

/**
 * Базовая реализация {@link IS5InitialImplementation}.
 * <p>
 * Наследник обязан обеспечить наличие открытого конструктора без параметров.
 *
 * @author mvk
 */
public abstract class S5InitialImplementation
    implements IS5InitialImplementation {

  /**
   * Параметры конфигурации
   */
  private final IOptionSetEdit params;

  /**
   * Построители расширений бекенда предоставляемые сервером
   */
  private IStridablesList<IS5BackendAddonCreator> baCreators;

  /**
   * Конструктор.
   *
   * @param aModule {@link S5Module} описание программного модуля представляющего сервер
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException невалидный ИД-путь
   */
  protected S5InitialImplementation( S5Module aModule ) {
    TsNullArgumentRtException.checkNull( aModule );
    // Список зависимостей модуля s5-сервера
    S5ModuleList depends = aModule.params().getValobj( S5Module.DDEF_DEPENDS );
    depends.add( new S5Module( S5_SERVER_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, S5_SERVER_NAME, //
        TSID_DESCRIPTION, S5_SERVER_DESCR, //
        S5Module.DDEF_VERSION, version //
    ) ) );
    // TODO: добавить описание tslib, wildfly, mariadb

    aModule.params().setValobj( S5Module.DDEF_DEPENDS, depends );

    params = new OptionSet();
    params.setStr( TSID_ID, aModule.id() );
    params.setStr( TSID_NAME, aModule.nmName() );
    params.setStr( TSID_DESCRIPTION, aModule.description() );

    Skid serverId = new Skid( CLASS_SERVER, aModule.id() );
    Skid nodeId = Skid.NONE;
    if( System.getProperty( JBOSS_NODE_NAME ) != null ) {
      nodeId = new Skid( CLASS_NODE, System.getProperty( JBOSS_NODE_NAME ).replaceAll( "-", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    OP_BACKEND_SERVER_ID.setValue( params, avValobj( serverId ) );
    OP_BACKEND_NODE_ID.setValue( params, avValobj( nodeId ) );
    OP_BACKEND_VERSION.setValue( params, avValobj( version ) );
    OP_BACKEND_MODULE.setValue( params, avValobj( aModule ) );
    OP_BACKEND_CURRENT_TIME.setValue( params, avTimestamp( System.currentTimeMillis() ) );
  }

  // ------------------------------------------------------------------------------------
  // IS5InitialImplementation
  //
  @Override
  public final IOptionSet params() {
    IOptionSetEdit retValue = new OptionSet( params );
    retValue.addAll( doProjectSpecificParams() );
    return retValue;
  }

  @Override
  public final IParameterized projectSpecificCreateClassParams( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    IParameterizedEdit retValue = new StridableParameterized( aClassId );
    retValue.params().addAll( doProjectSpecificCreateClassParams( aClassId ) );
    if( !retValue.params().hasValue( OP_OBJECT_IMPL_CLASS ) ) {
      // Определение по умолчанию таблиц хранения объектов пользователй и сессии
      if( ISkUser.CLASS_ID.equals( aClassId ) ) {
        // Класс пользователя ISkUser
        OP_OBJECT_IMPL_CLASS.setValue( retValue.params(), avStr( S5UserEntity.class.getName() ) );
        OP_FWD_LINK_IMPL_CLASS.setValue( retValue.params(), avStr( S5UserLinkFwdEntity.class.getName() ) );
        OP_REV_LINK_IMPL_CLASS.setValue( retValue.params(), avStr( S5UserLinkRevEntity.class.getName() ) );
      }
      if( ISkSession.CLASS_ID.equals( aClassId ) ) {
        // Класс сессии пользователя ISkSession
        OP_OBJECT_IMPL_CLASS.setValue( retValue.params(), avStr( S5SessionEntity.class.getName() ) );
        OP_FWD_LINK_IMPL_CLASS.setValue( retValue.params(), avStr( S5SessionLinkFwdEntity.class.getName() ) );
        OP_REV_LINK_IMPL_CLASS.setValue( retValue.params(), avStr( S5SessionLinkRevEntity.class.getName() ) );
      }
    }
    return retValue;
  }

  @Override
  public final IS5SequenceImplementation findHistDataImplementation( Gwid aGwid, EAtomicType aType, boolean aSync ) {
    return doFindHistDataImplementation( aGwid, aType, aSync );
  }

  @Override
  public final IList<IS5SequenceImplementation> getHistDataImplementations() {
    return doGetHistDataImplementations();
  }

  // ------------------------------------------------------------------------------------
  // ISkExtServicesProvider
  //
  @Override
  public final IStridablesList<IS5BackendAddonCreator> baCreators() {
    if( baCreators == null ) {
      baCreators = doProjectSpecificBaCreators();
    }
    return baCreators;
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Возвращает cпецифичные для проекта параметры {@link ISkClassInfo#params()}
   *
   * @return {@link IParameterizedEdit} параметры создания класса с возможностью редактирования. {@link IOptionSet#NULL}
   *         нет специфичных параметров
   */
  protected IOptionSet doProjectSpecificParams() {
    return IOptionSet.NULL;
  }

  /**
   * Возвращает список построителей расширений бекенда предоставляемых сервером проекта
   * <p>
   * Порядок расширений во возращаемом списке имеет значение - в начале списка должны идти расширения которые независят
   * от других расширений, в конце списка расширения зависящие от расширений в начале списка.
   *
   * @return {@link IList}&lt;{@link IS5BackendAddonCreator}&gt; список расширений бекенда.
   */
  protected IStridablesList<IS5BackendAddonCreator> doProjectSpecificBaCreators() {
    return IStridablesList.EMPTY;
  }

  /**
   * Возвращает cпецифичные для проекта параметры создания класса {@link ISkClassInfo#params()}
   * <p>
   * Проектно-специфичные параметры класса используются только при создании класса и впоследствии могут быть изменены
   * средствами {@link ISkSysdescr}.
   * <p>
   * Конкретные (проектные) реализации {@link IS5BackendCoreSingleton} могут переопределять значения свойств более
   * приемлимых для реализации проекта, например:
   * <ul>
   * <li>Класс реализации объектов - {@link IS5ServerHardConstants#OP_OBJECT_IMPL_CLASS};</li>
   * <li>Класс реализации прямой связи объектов - {@link IS5ServerHardConstants#OP_FWD_LINK_IMPL_CLASS};</li>
   * <li>Класс реализации обратной связи объектов - {@link IS5ServerHardConstants#OP_REV_LINK_IMPL_CLASS}.</li>
   * </ul>
   *
   * @param aClassId {@link String} идентификатор класса
   * @return {@link IOptionSet} параметры создания класса. {@link IOptionSet#NULL}: нет специфичных параметров
   */
  protected IOptionSet doProjectSpecificCreateClassParams( String aClassId ) {
    return IOptionSet.NULL;
  }

  /**
   * Возвращает описание реализации хранения указанного хранимого данного
   *
   * @param aGwid {@link Gwid} идентификатор хранимого данного
   * @param aType {@link EAtomicType} тип хранигого данного
   * @param aSync <b>true</b> синхронное данное; <b>false</b> асинхронное значение
   * @return {@link IS5SequenceImplementation} описание хранения. null: неопределяется в проектной конфигурации и
   *         выбирается по умолчанию
   */
  protected IS5SequenceImplementation doFindHistDataImplementation( Gwid aGwid, EAtomicType aType, boolean aSync ) {
    // Выбирается по умолчанию
    return null;
  }

  /**
   * Возвращает список всех реализаций хранения данных
   *
   * @return {@link IList}&lt;{@link IS5SequenceImplementation}&gt; список описаний (одно табличные и/или
   *         многотабличные). Пустой список: нет проектно-зависимых реализаций данных
   */
  protected IList<IS5SequenceImplementation> doGetHistDataImplementations() {
    // Нет проектно-зависимых реализаций данных
    return IList.EMPTY;
  }
}
