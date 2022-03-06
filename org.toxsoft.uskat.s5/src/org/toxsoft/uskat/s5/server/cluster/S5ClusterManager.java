package org.toxsoft.uskat.s5.server.cluster;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5ClusterListener.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.cluster.S5ClusterCommand.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;
import org.wildfly.clustering.Registration;
import org.wildfly.clustering.dispatcher.*;
import org.wildfly.clustering.group.*;

/**
 * Управление кластером s5-сервера
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    PROJECT_INITIAL_IMPLEMENT_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@Lock( LockType.READ )
public class S5ClusterManager
    extends S5SingletonBase
    implements IS5ClusterManager, IS5ServerJob, GroupListener {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String CLUSTER_MANAGER_ID = "S5ClusterManager"; //$NON-NLS-1$

  /**
   * Таймаут(мсек) между выполнением фоновых задач синглетона
   */
  private static final long DO_JOB_TIMEOUT = 1000;

  /**
   * Группа в которой работает узел кластера s5-сервера
   */
  @Resource( lookup = CLUSTER_GROUP )
  private Group group;

  /**
   * Фабрика диспетчера команд выполняемых узлами кластера
   */
  @Resource( lookup = CLUSTER_COMMAND_DISPATCHER_FACTORY )
  private CommandDispatcherFactory commandFactory;

  /**
   * Диспетчер команд выполняемых узлами кластера
   */
  private CommandDispatcher<S5ClusterManager> commandDispatcher;

  /**
   * Текущий координатор кластера
   */
  private Node coordinator;

  /**
   * Слушатель кластера
   */
  private Registration listenerRegistration;

  /**
   * Внешние слушатели событий кластера
   */
  private IListEdit<IS5ClusterListener> listeners = new ElemArrayList<>( false );

  /**
   * Карта обработчиков команд
   * <p>
   * Ключ: имя метода {@link IS5ClusterCommand#method()};<br>
   * Значение: список обработчиков метода {@link IS5ClusterCommandHandler}.
   */
  private IStringMapEdit<IListEdit<IS5ClusterCommandHandler>> commandHandlers = new StringMap<>();

  /**
   * Блокировка доступа к данным менеджера кластера
   */
  private S5Lockable lock = new S5Lockable();

  /**
   * Тип массива узлов кластера (оптимизация)
   */
  private static final Node[] NODE_ARRAY_TYPE = {};

  /**
   * Конструктор
   */
  public S5ClusterManager() {
    super( CLUSTER_MANAGER_ID, STR_D_CLUSTER_MANAGER );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5SingletonBase
  //
  @Override
  protected void doInit() {
    S5ClusterManager localView = sessionContext().getBusinessObject( S5ClusterManager.class );
    // Инициализация диспетчера команд
    commandDispatcher = commandFactory.createCommandDispatcher( getClass().getName(), this );
    // Текущий координатор кластера
    coordinator = group.getMembership().getCoordinator();
    // Регистрация слушателя кластера
    listenerRegistration = group.register( localView );
    // Запуск фоновой задачи
    addOwnDoJob( DO_JOB_TIMEOUT );
  }

  @Override
  protected void doClose() {
    // Дерегистрация слушателя кластера
    listenerRegistration.close();
    // Завершение работы диспетчера команд
    commandDispatcher.close();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterManager
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public Group group() {
    return group;
  }

  @Override
  public IStringMap<ITjValue> sendSyncCommand( IS5ClusterCommand aCommand, boolean aRemoteOnly, boolean aPrimaryOnly ) {
    try {
      TsNullArgumentRtException.checkNull( aCommand );
      // Список узлов исключенных из рассылки
      IList<Node> excludes = getExcludeNodes( group, aRemoteOnly, aPrimaryOnly );
      // Формирование команды
      S5ClusterCommand notify = new S5ClusterCommand( aCommand.method(), aCommand.params() );
      // Отправляется команда кластера
      logger().info( MSG_SEND_COMMAND, notify.method(), nodesString( excludes ) );
      // Метка времени начала выполнения команды
      long startTime = System.currentTimeMillis();
      // Выполнение
      Map<Node, CompletionStage<String>> responses =
          commandDispatcher.executeOnGroup( notify, excludes.toArray( NODE_ARRAY_TYPE ) );
      // Формирование результата
      IStringMapEdit<ITjValue> retValue = new StringMap<>();
      for( Node node : responses.keySet() ) {
        CompletionStage<String> cs = responses.get( node );
        if( !(cs instanceof CompletableFuture) ) {
          // Результат не может быть получен, возвращается пустой результат (NULL)
          retValue.put( node.getName(), TjUtils.NULL );
          continue;
        }
        // Блокирующий вызов получения результата выполнения команды на узле кластера
        String resultString = ((CompletableFuture<String>)cs).get();
        retValue.put( node.getName(), resultFromString( resultString ) );
      }
      // Выполнена команда кластера
      Long executingTime = Long.valueOf( System.currentTimeMillis() - startTime );
      logger().info( MSG_EXECUTED_COMMAND, notify.method(), nodesString( excludes ), executingTime );
      return retValue;
    }
    catch( CommandDispatcherException | InterruptedException | ExecutionException e ) {
      // Неожиданная ошибка выполнения команды
      throw new TsInternalErrorRtException( e );
    }
  }

  @Asynchronous
  @Override
  public void sendAsyncCommand( IS5ClusterCommand aCommand, boolean aRemoteOnly, boolean aPrimaryOnly ) {
    try {
      TsNullArgumentRtException.checkNull( aCommand );
      // Список узлов исключенных из рассылки
      IList<Node> excludes = getExcludeNodes( group, aRemoteOnly, aPrimaryOnly );
      // Формирование уведомления(команды)
      S5ClusterCommand notify = new S5ClusterCommand( aCommand.method(), aCommand.params() );
      // Передача уведомления
      commandDispatcher.executeOnGroup( notify, excludes.toArray( NODE_ARRAY_TYPE ) );
      // Отправлено уведомление кластера
      logger().info( MSG_SEND_NOTICE, notify.method(), nodesString( excludes ) );
    }
    catch( CommandDispatcherException e ) {
      // Неожиданная ошибка выполнения команды
      throw new TsInternalErrorRtException( e );
    }
  }

  @Override
  public void addCommandHandler( String aMethod, IS5ClusterCommandHandler aCommandHandler ) {
    TsNullArgumentRtException.checkNulls( aMethod, aCommandHandler );
    lockWrite( lock );
    try {
      IListEdit<IS5ClusterCommandHandler> handlers = commandHandlers.findByKey( aMethod );
      if( handlers == null ) {
        // false: дубли запрещены
        handlers = new ElemArrayList<>( false );
        commandHandlers.put( aMethod, handlers );
      }
      handlers.add( aCommandHandler );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public void removeCommandHandler( String aMethod, IS5ClusterCommandHandler aCommandHandler ) {
    TsNullArgumentRtException.checkNulls( aMethod, aCommandHandler );
    lockWrite( lock );
    try {
      IListEdit<IS5ClusterCommandHandler> handlers = commandHandlers.findByKey( aMethod );
      if( handlers == null ) {
        // Нет обработчиков этого метода
        return;
      }
      handlers.remove( aCommandHandler );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void addClusterListener( IS5ClusterListener aListener ) {
    TsNullArgumentRtException.checkNull( aListener );
    lockWrite( lock );
    try {
      listeners.add( aListener );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void removeClusterListener( IS5ClusterListener aListener ) {
    TsNullArgumentRtException.checkNull( aListener );
    lockWrite( lock );
    try {
      listeners.remove( aListener );
    }
    finally {
      unlockWrite( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация GroupListener
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void membershipChanged( Membership aPrevious, Membership aCurrent, boolean aMerged ) {
    List<Node> previousMembers = aPrevious.getMembers();
    List<Node> currentMembers = aCurrent.getMembers();
    List<Node> joiners =
        currentMembers.stream().filter( member -> !previousMembers.contains( member ) ).collect( Collectors.toList() );
    Membership membership = group.getMembership();
    if( !joiners.isEmpty() ) {
      // Подключение новых узлов кластера
      logger().info( MSG_WELCOME, joiners, membership.getCoordinator(), membership.getMembers() );
    }
    List<Node> leavers =
        previousMembers.stream().filter( member -> !currentMembers.contains( member ) ).collect( Collectors.toList() );
    if( !leavers.isEmpty() ) {
      // Отключение узлов кластера
      logger().info( MSG_GOODBYE, leavers, membership.getCoordinator(), membership.getMembers() );
    }
    // Оповещение о событии
    callMembershipChanged( listeners(), membership, membership, aMerged );

    // Слежение за текущим координатором кластера
    Node prevCoordinator = coordinator;
    Node newCoordinator = aCurrent.getCoordinator();
    if( !newCoordinator.equals( prevCoordinator ) ) {
      // Изменение координатора кластера
      coordinator = newCoordinator;
      logger().info( MSG_CHANGE_COORDINATOR, prevCoordinator, newCoordinator );
      callCoordinatorChanged( listeners(), prevCoordinator, newCoordinator, aMerged );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerJob
  //
  @Override
  public void doJob() {
    // TODO:
  }

  @Override
  public boolean completed() {
    return isClosed();
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает список слушателей кластера
   *
   * @return {@link IList}&lt;{@link IS5ClusterListener}&gt; список слушателей
   */
  IList<IS5ClusterListener> listeners() {
    lockRead( lock );
    try {
      return new ElemArrayList<>( listeners );
    }
    finally {
      unlockRead( lock );
    }
  }

  /**
   * Обработать команду кластера
   *
   * @param aCommand {@link IS5ClusterCommand} команда кластера
   * @return {@link ITjValue} результат выполнения команды
   * @throws TsNullArgumentRtException аргумент = null
   */
  ITjValue handleCommand( IS5ClusterCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    // Принята команда кластера
    logger().info( MSG_RECV_COMMAND, aCommand.method() );
    // Список обработчиков команд
    IList<IS5ClusterCommandHandler> handlers = IList.EMPTY;
    lockRead( lock );
    try {
      handlers = commandHandlers.findByKey( aCommand.method() );
    }
    finally {
      unlockRead( lock );
    }
    if( handlers == null ) {
      // Не найдены обработчики команды. Возвращается пустой результат (NULL)
      return TjUtils.NULL;
    }
    for( IS5ClusterCommandHandler handler : handlers ) {
      ITjValue result = handler.handleClusterCommand( aCommand );
      if( result != TjUtils.NULL ) {
        // Команда обработана
        return result;
      }
    }
    // Обработчик не найден
    return TjUtils.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает список узлов кластера которые должны быть исключены из рассылки сообщения
   *
   * @param aGroup {@link Group} группа кластера
   * @param aRemoteOnly boolean <b>true</b> отправлять только удаленным узлам;<b>false</b> отправлять всем
   * @param aPrimaryOnly boolean <b>true</b> отправлять только первичному узлу;<b>false</b> отправлять всем
   * @return {@link IList}&lt;{@link Node}&gt; список узлов кластера
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IList<Node> getExcludeNodes( Group aGroup, boolean aRemoteOnly, boolean aPrimaryOnly ) {
    TsNullArgumentRtException.checkNull( aGroup );
    Membership membership = aGroup.getMembership();
    List<Node> nodes = membership.getMembers();
    Node localNode = aGroup.getLocalMember();
    Node primaryNode = membership.getCoordinator();
    IListEdit<Node> retValue = new ElemArrayList<>( nodes.size() );
    for( Node node : nodes ) {
      if( aRemoteOnly && localNode.equals( node ) ) {
        // Команда НЕ посылается локальному узлу
        retValue.add( node );
      }
      if( aPrimaryOnly && !primaryNode.equals( node ) ) {
        // Команда посылается только первичному узлу
        retValue.add( node );
      }
    }
    return retValue;
  }

  /**
   * Возвращает список узлов кластера которые должны быть исключены из рассылки сообщения
   *
   * @param aNodes {@link IList}
   * @return String текстовое
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String nodesString( IList<Node> aNodes ) {
    TsNullArgumentRtException.checkNull( aNodes );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aNodes.size(); index < n; index++ ) {
      sb.append( aNodes.get( index ) );
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    return sb.toString();
  }
}
