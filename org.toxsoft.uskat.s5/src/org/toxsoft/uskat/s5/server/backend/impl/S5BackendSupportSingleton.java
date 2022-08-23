package org.toxsoft.uskat.s5.server.backend.impl;

import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.impl.S5ClusterCommandWhenSupportConfigChanged.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.IS5ClassBackend;
import org.toxsoft.uskat.classes.IS5ClassHistorableBackend;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.server.transactions.IS5Transaction;
import org.toxsoft.uskat.s5.server.transactions.IS5TransactionListener;

/**
 * Базовая реализация синглетона поддержки бекенда предоставляемого s5-сервером {@link IS5BackendSupportSingleton}
 *
 * @author mvk
 */
public abstract class S5BackendSupportSingleton
    extends S5SingletonBase
    implements IS5BackendSupportSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Менеджер кластера в котором работает сервер
   */
  @EJB
  private IS5ClusterManager clusterManager;

  /**
   * Поддержка синглетонов (контейнер)
   */
  @EJB
  private IS5BackendCoreSingleton backendCoreSingleton;

  /**
   * Конструктор для наследников.
   *
   * @param aId String идентификатор синглетона
   * @param aDescription String описание синглетона
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5BackendSupportSingleton( String aId, String aDescription ) {
    super( aId, aDescription );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5SingletonBase
  //
  /**
   * {@link #doInit()} запрещено переопределять в наследниках. JPA не позволяет использовать спецификатор final
   * <p>
   * В наследниках должно использоваться переопределение {@link #doInitSupport()}
   */
  @Override
  protected void doInit() {
    backendCoreSingleton.add( id(), sessionContext().getBusinessObject( getClass() ) );
    doInitSupport();
    clusterManager.addCommandHandler( WHEN_SUPPORT_CONFIG_CHANGED_METHOD,
        new S5ClusterCommandWhenSupportConfigChanged() {

          @SuppressWarnings( { "synthetic-access" } )
          @Override
          protected void doWhenSupportConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
            // Обработка получения сообщения от другого узла кластера об изменении конфигурации
            onConfigChanged( aPrevConfig, aNewConfig );
          }
        } );
  }

  /**
   * {@link #doClose()} запрещено переопределять в наследниках. JPA не позволяет использовать спецификатор final
   * <p>
   * В наследниках должно использоваться переопределение {@link #doCloseSupport()}
   */
  @Override
  protected void doClose() {
    doCloseSupport();
    backendCoreSingleton.remove( id() );
  }

  // ------------------------------------------------------------------------------------
  // Шаблонные методы для наследников
  //
  /**
   * Вызывается при инициализации синглтона из метода {@link #doInit()}.
   * <p>
   * {@link #doInitSupport()} выполняется в открытой транзакции
   * <p>
   * Наследники в этом методе должны провести инициализцаию работы синглетона. К моменту вызова этого метода существует
   * конфигурация синглетона {@link #configuration()}, либо созданная по умолчанию из описания типа CONFIG_TYPE, либо
   * считанная из БД.
   */
  protected void doInitSupport() {
    // nop
  }

  /**
   * Вызывается перед уничтожением синглтона из метода {@link #doClose()}.
   * <p>
   * {@link #doCloseSupport()} выполняется в открытой транзакции
   * <p>
   * Наследники в этом методе должны освободить ресурсы, и завершить то, что было занято и начато в {@link #doInit()}.
   * Фактически, этот метод вызвается при завершении работы синглетона.
   * <p>
   * В классе {@link S5SingletonBase} ничего не делает, при переопределении вызывать не нужно.
   */
  protected void doCloseSupport() {
    // nop
  }

  /**
   * Возвращает идентификатор класса бекенда
   *
   * @return String класс бекенда, {@link IS5ClassBackend} или его наследник, например
   *         {@link IS5ClassHistorableBackend}.
   */
  protected String doBackendClassId() {
    return IS5ClassBackend.CLASS_ID;
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает backendCoreSingleton
   *
   * @return {@link IS5BackendCoreSingleton} backendCoreSingleton
   */
  protected final IS5BackendCoreSingleton backend() {
    return backendCoreSingleton;
  }

  /**
   * Возвращает менеджер кластера в котором работает сервер
   *
   * @return {@link IS5ClusterManager} менеджер кластера
   */
  protected final IS5ClusterManager clusterManager() {
    return clusterManager;
  }

  /**
   * Возвращает идентификатор узла сервера
   *
   * @return {@link Skid} идентификатор узла
   */
  protected final Skid nodeId() {
    // Информация о бекенде
    ISkBackendInfo info = backend().getInfo();
    // Идентификатор узла сервера
    Skid nodeId = OP_BACKEND_NODE_ID.getValue( info.params() ).asValobj();
    return nodeId;
  }

  /**
   * Возвращает идентификатор бекенда с учетом имени узла сервера
   *
   * @return {@link Skid} идентификатор бекенда
   */
  protected final Skid backendId() {
    // Идентификатор узла сервера
    Skid nodeId = nodeId();
    // Полный (с именем узла) идентификатор backendCoreSingleton
    return new Skid( doBackendClassId(), nodeId.strid() + '.' + id() );
  }

  // ------------------------------------------------------------------------------------
  // Переопределение методов S5SingletonBase
  //

  /**
   * JPA запрещает делать методы View финальными (final)
   */
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void saveConfiguration( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    // Предыдущая конфигурация
    IOptionSet prevConfig = configuration();
    // Вызов обработки базового класса
    super.saveConfiguration( aConfiguration );
    // Регистрация обработки события завершения транзакции
    IS5Transaction tx = transactionManager().findTransaction();
    if( tx != null ) {
      tx.putResource( TX_SAVE_SUPPORT_PREV_CONFIG, prevConfig );
      tx.putResource( TX_SAVE_SUPPORT_NEW_CONFIG, aConfiguration );
      tx.addListener( new IS5TransactionListener() {

        @Override
        public void changeTransactionStatus( IS5Transaction aTransaction ) {
          IOptionSet txPrevConfig = aTransaction.findResource( TX_SAVE_SUPPORT_PREV_CONFIG );
          IOptionSet txNewConfig = aTransaction.findResource( TX_SAVE_SUPPORT_NEW_CONFIG );
          if( txPrevConfig != null && txNewConfig != null ) {
            // Оповещение удаленных узлов кластера: boolean remoteOnly = true; boolean primaryOnly = false;
            clusterManager().sendAsyncCommand( whenSupportConfigChangedCommand( txPrevConfig, txNewConfig ), true,
                false );
          }
        }
      } );
    }
  }
}
