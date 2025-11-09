package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.uskat.core.api.cmdserv.ESkCommandState.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.commands.IS5CommandHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.commands.impl.IS5Resources.*;

import java.util.concurrent.*;

import javax.annotation.*;
import javax.ejb.*;

import org.infinispan.*;
import org.infinispan.commons.util.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.common.sysdescr.*;
import org.toxsoft.uskat.s5.server.backend.addons.commands.*;
import org.toxsoft.uskat.s5.server.backend.supports.commands.*;
import org.toxsoft.uskat.s5.server.backend.supports.commands.sequences.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.collections.*;
import org.toxsoft.uskat.s5.utils.jobs.*;

/**
 * Реализация синглетона {@link IS5BackendCommandSingleton}
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
public class S5BackendCommandSingleton
    extends S5BackendSequenceSupportSingleton<IS5CommandSequence, IDtoCompletedCommand>
    implements IS5BackendCommandSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_COMMANDS_ID = "S5BackendCommandSingleton"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Кэш команд выполняемых в данный момент
   * <p>
   * Ключ: идентификатор команды, {@link IDtoCommand#instanceId()} ;<br>
   * Значение:
   * <ul>
   * <li>{@link Pair#left()} = {@link IDtoCommand} команда;</li>
   * <li>{@link Pair#right()} = {@link ITimedListEdit} история состояний.</li>
   * </ul>
   * .
   */
  @Resource( lookup = INFINISPAN_CACHE_CMD_STATES )
  private Cache<String, Pair<IDtoCommand, ITimedListEdit<SkCommandState>>> executingCommandsCache;

  /**
   * Идентификатор узла кластера сервера
   */
  private String nodeId;

  /**
   * Конструктор.
   */
  public S5BackendCommandSingleton() {
    super( BACKEND_COMMANDS_ID, STR_D_BACKEND_COMMANDS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @SuppressWarnings( "nls" )
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Идентификатор узла сервера
    nodeId = clusterManager().group().getLocalMember().getName();
    if( nodeId.contains( "-" ) ) {
      // Замена '-' на "_"
      nodeId = nodeId.replaceAll( "-", "_" );
      logger().error( "При формировании nodeId был использован hostname с заменой символов '-' на '_'" );
    }
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendCommandSingleton
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    try {
      // Текущее время
      long currTime = System.currentTimeMillis();
      // Фронтенд исполнителя команды
      IS5FrontendRear frontend = findExecutorFrontend( aCmdGwid );
      // Идентификатор команды
      String instanceId = S5CommandIdGenerator.INSTANCE.nextId();
      // Команда
      DtoCommand command = new DtoCommand( currTime, instanceId, aCmdGwid, aAuthorSkid, aArgs );
      // Добавленые состояния команды
      ITimedListEdit<SkCommandState> newStates = new TimedList<>();
      if( frontend != null ) {
        // Изменение состояния на "исполняется"
        DtoCommandStateChangeInfo changeInfo =
            createChangeInfo( instanceId, currTime, EXECUTING, REASON_EXEC_BY_QUERY );
        changeCommandState( command, newStates, changeInfo );
        // Отправляем команду на исполнение
        frontend.onBackendMessage( BaMsgCommandsExecCmd.INSTANCE.makeMessage( command ) );
        // Команда принята на исполнение. Формирование результата
        SkCommand retValue = new SkCommand( command );
        // Добавление новых состояний (0-SENDING cостояние уже есть в команде)
        for( int index = 1; index < newStates.size(); index++ ) {
          retValue.papiAddState( newStates.get( index ) );
        }
        return retValue;
      }
      // Не найден исполнитель команды
      DtoCommandStateChangeInfo changeInfo =
          createChangeInfo( instanceId, currTime, UNHANDLED, REASON_EXECUTOR_NOT_FOUND );
      changeCommandState( command, newStates, changeInfo );
      // Формирование результата
      SkCommand retValue = new SkCommand( command );
      // Добавление новых состояний (0-SENDING cостояние уже есть в команде)
      for( int index = 1; index < newStates.size(); index++ ) {
        retValue.papiAddState( newStates.get( index ) );
      }
      return retValue;
    }
    catch( Throwable e ) {
      // Неожиданная ошибка обработки
      logger().error( e );
      throw e;
    }
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @Lock( LockType.READ )
  @Override
  public ValidationResult testCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    // Фронтенд исполнителя команды
    IS5FrontendRear frontend = findExecutorFrontend( aCmdGwid );
    // Данные фронтенда
    S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
    if( frontendData == null ) {
      // фронтенд не поддерживает реальное время
      return ValidationResult.error( ERR_FRONTEND_DOES_NOT_SUPPORT_CMD, aCmdGwid );
    }
    // Идентификатор команды
    String instanceId = S5CommandIdGenerator.INSTANCE.nextId();
    // Фиксация факта ожидания выполнения команды
    synchronized (frontendData.commands) {
      // Результат тестирования по умолчанию
      ValidationResult initResult =
          ValidationResult.error( ERR_TEST_FAILED_BY_TIMEOUT, aCmdGwid, Long.valueOf( COMMAND_TEST_TIMEOUT ) );
      // Ожидание выполнения тестирования
      frontendData.commands.defineTestResult( instanceId, initResult );
      // Команда
      DtoCommand command = new DtoCommand( System.currentTimeMillis(), instanceId, aCmdGwid, aAuthorSkid, aArgs );
      // Передача запроса исполнителю
      frontend.onBackendMessage( BaMsgCommandsTestCmd.INSTANCE.makeMessage( command ) );
      try {
        frontendData.commands.wait( COMMAND_TEST_TIMEOUT );
      }
      catch( InterruptedException e ) {
        logger().error( e );
      }
    }
    ValidationResult result = frontendData.commands.removeTestResult( instanceId );

    return result;
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNulls( aGwids );
    // Сообщение frontend
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Оповещение фронтенда об изменении списка команд поддерживаемых исполнителями
      frontend.onBackendMessage( BaMsgCommandsGloballyHandledGwidsChanged.INSTANCE.makeMessage() );
    }
    logger().info( MSG_SET_EXECUTABLE_CMDS, Integer.valueOf( listGloballyHandledCommandGwids().size() ) );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    // Поиск выполняемой команды
    Pair<IDtoCommand, ITimedListEdit<SkCommandState>> cmd = executingCommandsCache.get( aStateChangeInfo.instanceId() );
    if( cmd == null ) {
      // Получение изменения состояния несуществующей команды
      logger().error( ERR_COMMAND_NOT_FOUND, aStateChangeInfo.instanceId(), aStateChangeInfo.state() );
      return;
    }
    // Изменение состояния. false: установка состояния уже выполняемой команды
    changeCommandState( cmd.left(), cmd.right(), aStateChangeInfo );
  }

  @Override
  public void changeTestState( String aInstanceId, ValidationResult aResult ) {
    TsNullArgumentRtException.checkNulls( aInstanceId, aResult );
    // Сообщение frontend
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Поддержка выполнения команд
      synchronized (frontendData.commands) {
        if( frontendData.commands.defineTestResult( aInstanceId, aResult ) == null ) {
          // Получен результат незарегистрированной команды
          frontendData.commands.removeTestResult( aInstanceId );
          // Журнал
          logger().error( ERR_UNKNOWN_TEST_COMMAND, aInstanceId );
          continue;
        }
        // Запуск механизма оповещения фронтенда
        frontendData.commands.notify();
      }
    }
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    GwidList retValue = new GwidList();
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      retValue.addAll( frontendData.commands.getHandledCommandGwids() );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void saveToHistory( IDtoCompletedCommand aCompletedCommand ) {
    TsNullArgumentRtException.checkNull( aCompletedCommand );
    // TODO вопрос на обсуждение: на момент завершения команды, skconnection клиента отправившего команду может
    // оказаться "далеко" и недоступным (например, разрыв связи с клиентом). В текущей реализации бекенд сам
    // обрабатывает состояние команды (смотри changeCommandState) и делает при необходимости вызов:
    // writeCommand( aCompletedCommand );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    long traceStartTime = System.currentTimeMillis();
    // Подготовка списка идентификаторов запрашиваемых объектов. false: без повторов
    if( aGwid.kind() != EGwidKind.GW_CMD || aGwid.isAbstract() ) {
      // По контракту принимаются только команды объектов
      return new TimedList<>();
    }
    // Чтение истории команд
    long traceReadStartTime = System.currentTimeMillis();
    // Идентификаторы объектов команд
    IGwidList objIds = new GwidList( Gwid.createObj( aGwid.skid() ) );
    IQueryInterval interval = new QueryInterval( EQueryIntervalType.CSCE, aInterval.startTime(), aInterval.endTime() );
    IMap<Gwid, IS5CommandSequence> sequences = readSequences( objIds, interval, ACCESS_TIMEOUT_DEFAULT );
    long traceReadEndTime = System.currentTimeMillis();
    // Фильтрация команд и формирование сводного(по объектам) результата запроса
    TimedList<IDtoCompletedCommand> commands = new TimedList<>();
    for( IS5CommandSequence sequence : sequences ) {
      for( IS5SequenceBlock<IDtoCompletedCommand> block : sequence.blocks() ) {
        for( int index = 0, n = block.size(); index < n; index++ ) {
          IDtoCompletedCommand completedCmd = block.getValue( index );
          if( completedCmd.cmd().cmdGwid().equals( aGwid ) ) {
            commands.add( completedCmd );
          }
        }
      }
    }
    // Формирование результата. aAllowDuplicates = true
    ITimedListEdit<IDtoCompletedCommand> retValue = new S5FixedCapacityTimedList<>( commands.size(), true );
    retValue.addAll( commands );

    long traceResultTime = System.currentTimeMillis();
    // Формирование журнала
    Integer rc = Integer.valueOf( retValue.size() );
    Long pt = Long.valueOf( traceReadStartTime - traceStartTime );
    Long rt = Long.valueOf( traceReadEndTime - traceReadStartTime );
    Long ft = Long.valueOf( traceResultTime - traceReadEndTime );
    Long at = Long.valueOf( traceResultTime - traceStartTime );
    // Завершено чтение истории команд
    logger().info( MSG_READ_COMMANDS, aGwid, aInterval, rc, at, pt, rt, ft );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerJob
  //
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void doJob() {
    super.doJob();
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Количество команд на выполении
    int count = 0;
    try( CloseableIterator<Pair<IDtoCommand, ITimedListEdit<SkCommandState>>> iterator =
        executingCommandsCache.values().iterator() ) {
      while( iterator.hasNext() ) {
        Pair<IDtoCommand, ITimedListEdit<SkCommandState>> cmdPair = iterator.next();
        IDtoCommand cmd = cmdPair.left();
        ITimedListEdit<SkCommandState> states = cmdPair.right();
        // 2020-03-21, mvk, клиент может не иметь доступа к первичному узлу кластера - узел работает в другой сети и у
        // клиента нет к нему доступа (пример: Тбилиси, локальные сервера станции, клиенты программа НУ).
        // if( clusterManager.isPrimary() == true && //
        // currTime - cmd.timestamp() >= getCmdTimeout( sysdescrReader(), cmd ) ) {
        // 2025-10-24 mvk ---+++
        // if( currTime - cmd.timestamp() >= getCmdTimeout( sysdescrReader(), cmd ) ) {
        long lastStateTime = (states.size() > 0 ? states.last().timestamp() : cmd.timestamp());
        if( currTime - lastStateTime >= getCmdTimeout( sysdescrReader(), cmd ) ) {
          // Завершение выполнения команды по таймауту
          DtoCommandStateChangeInfo newState =
              createChangeInfo( cmd.instanceId(), currTime, TIMEOUTED, REASON_CANCEL_BY_TIMEOUT );
          changeCommandState( cmd, cmdPair.right(), newState );

          continue;
        }
        // Команда остается на выполнении
        count++;
      }
    }

    // Количество команд на выполнении
    logger().debug( MSG_DOJOB, Integer.valueOf( count ) );
  }

  // ------------------------------------------------------------------------------------
  // Реализация асбтрактных методов S5BackendSequenceSupportSingleton
  //
  @Override
  protected IS5BackendCommandSingleton getBusinessObject() {
    return sessionContext().getBusinessObject( IS5BackendCommandSingleton.class );
  }

  @Override
  protected IS5SequenceFactory<IDtoCompletedCommand> doCreateFactory() {
    return new S5CommandSequenceFactory( backend().initialConfig().impl(), configuration(), sysdescrReader() );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSequenceSupportSingleton
  //
  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    if( aPrevClassInfo.cmdInfos().size() > 0 && aNewClassInfo.cmdInfos().size() == 0 ) {
      // Удаление идентификаторов данных объектов у которых больше нет команд
      S5SequenceFactory factory = ((S5SequenceFactory)factory());
      // Список объектов изменившихся классов
      IList<IDtoObject> objs = S5TransactionUtils.txUpdatedClassObjs( transactionManager(), objectsBackend(),
          aNewClassInfo.id(), aDescendants );
      for( IDtoObject obj : objs ) {
        String classId = obj.classId();
        ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
        if( classInfo.cmds().list().size() == 0 ) {
          factory.removeTypeInfo( Gwid.createObj( classId, obj.strid() ) );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doBeforeDeleteClass( IDtoClassInfo aClassInfo ) {
    // Удаление идентификаторов данных объектов удаленного класса
    if( aClassInfo.cmdInfos().size() > 0 ) {
      // Удаление идентификаторов данных объектов удаленного класса
      ((S5SequenceFactory)factory()).removeTypeInfo( aClassInfo.id() );
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // Удаление идентификаторов данных удаленных объектов
    S5SequenceFactory factory = ((S5SequenceFactory)factory());
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      if( classInfo.cmds().list().size() == 0 ) {
        // У класса объекта нет команд
        continue;
      }
      IList<IDtoObject> objs = aRemovedObjs.getByKey( classInfo );
      for( IDtoObject obj : objs ) {
        factory.removeTypeInfo( Gwid.createObj( obj.classId(), obj.strid() ) );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает фронтенд способный выполнить команду с указанным идентификатором
   *
   * @param aGwid {@link Gwid} идентификатор команды
   * @return {@link IS5FrontendRear} фронтенд исполнителя команды. null: не найден
   * @throws TsNullArgumentRtException аргумент = null
   */
  private IS5FrontendRear findExecutorFrontend( Gwid aGwid ) {
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      if( frontendData.commands.getHandledCommandGwids().hasElem( aGwid ) ) {
        return frontend;
      }
    }
    return null;
  }

  /**
   * Установка нового состояния команды
   *
   * @param aCommand {@link IDtoCommand} команда {@link IDtoCommand#instanceId()}
   * @param aStates {@link ITimedListEdit}&lt;{@link SkCommandState}&gt; история состояния команды
   * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} новое состояние команды
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый переход состояния команды
   */
  private void changeCommandState( IDtoCommand aCommand, ITimedListEdit<SkCommandState> aStates,
      DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNulls( aCommand, aStates, aStateChangeInfo );
    // Предыдущее состояние команды
    SkCommandState prevState = aStates.last();
    // Новое состояние команды
    SkCommandState newCommandState = aStateChangeInfo.state();
    // Проверка допустимых переходов состояния выполнения команды
    switch( newCommandState.state() ) {
      case SENDING:
        if( prevState != null && prevState.state() != SENDING ) {
          throw new TsIllegalArgumentRtException();
        }
        break;
      case EXECUTING:
        if( prevState == null ) {
          // Команда одновременно послана и запущена на выполнение
          prevState = createState( newCommandState.timestamp(), SENDING, REASON_SEND_AND_EXEC );
          aStates.add( prevState );
        }
        if( prevState.state() != SENDING && prevState.state() != EXECUTING ) {
          // Недопустимый переход состояния команды
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, newCommandState );
        }
        break;
      case UNHANDLED:
        if( prevState == null ) {
          // Команда одновременно послана и отменена так как нет ее исполнителя
          prevState = createState( newCommandState.timestamp(), SENDING, REASON_SEND_AND_CANCEL );
          aStates.add( prevState );
        }
        if( prevState.state() != SENDING ) {
          // Недопустимый переход состояния команды
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, newCommandState );
        }
        break;
      case TIMEOUTED:
      case SUCCESS:
      case FAILED:
        if( prevState == null || prevState.state() != EXECUTING ) {
          // Недопустимый переход состояния команды
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, newCommandState );
        }
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Добавление нового состояния в историю
    aStates.add( newCommandState );
    // Журнал: изменение состояния команды
    logger().info( MSG_NEW_STATE, aCommand, prevState, newCommandState );

    // Идентификатор команды
    String cmdId = aCommand.instanceId();
    if( !newCommandState.state().isComplete() ) {
      // Продолжение выполнения команды. Обновление кэша
      executingCommandsCache.put( cmdId, new Pair<>( aCommand, aStates ) );
    }
    if( newCommandState.state().isComplete() ) {
      // Завершение выполнения команды. Удаление команды из кэша
      executingCommandsCache.remove( cmdId );
      // Запись истории в базу данных
      writeCommand( new DtoCompletedCommand( aCommand, aStates ) );
    }
    // Формирование результата
    ITimedListEdit<DtoCommandStateChangeInfo> dpuStates = new TimedList<>();
    for( SkCommandState state : aStates ) {
      dpuStates.add( new DtoCommandStateChangeInfo( cmdId, state ) );
    }
    // Сообщение frontend
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Поддержка выполнения команд
      S5BaCommandsSupport commandsSupport = frontendData.commands;
      // Список изменений состояний команд переданных через целевой frontend
      S5CommandStateChangeInfoList states = commandsSupport.updateExecutingCmds( dpuStates );
      if( states.size() == 0 ) {
        // В текущем фронтенде нет состояний команд ожидающих выполнения
        continue;
      }
      // Оповещение бекенда
      frontend.onBackendMessage( BaMsgCommandsChangeState.INSTANCE.makeMessage( aStateChangeInfo ) );
    }
  }

  /**
   * Реализация сохранения завершенной команды в истории команд
   *
   * @param aCommand {@link IDtoCompletedCommand} команда для сохранения
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void writeCommand( IDtoCompletedCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    // Формирование последовательностей истории команд по объектам
    Skid skid = aCommand.cmd().cmdGwid().skid();
    TimedList<IDtoCompletedCommand> commands = new TimedList<>( aCommand );
    Gwid objId = Gwid.createObj( skid.classId(), skid.strid() );
    IQueryInterval interval = new QueryInterval( CSCE, commands.first().timestamp(), commands.last().timestamp() );
    try {
      IS5CommandSequenceEdit sequence = new S5CommandSequence( factory(), objId, interval, IList.EMPTY );
      sequence.set( commands );
      // Cохранение событий в базе данных
      IS5BackendCommandSingleton sequenceWriter =
          sessionContext().getBusinessObject( IS5BackendCommandSingleton.class );
      sequenceWriter.writeSequences( new ElemArrayList<>( sequence ) );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  /**
   * Создает новое состояние команды
   *
   * @param aInstanceId String идентификатор команды
   * @param aTimestamp long метка времени нового состояния
   * @param aState {@link ESkCommandState} идентификатор состояния
   * @param aCause String причина перехода в новое состояние
   * @return {@link DtoCommandStateChangeInfo} новое состояние
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private DtoCommandStateChangeInfo createChangeInfo( String aInstanceId, long aTimestamp, ESkCommandState aState,
      String aCause ) {
    TsNullArgumentRtException.checkNulls( aInstanceId, aState, aCause );
    SkCommandState commandState = createState( aTimestamp, aState, aCause );
    DtoCommandStateChangeInfo retValue = new DtoCommandStateChangeInfo( aInstanceId, commandState );
    return retValue;
  }

  /**
   * Создает новое состояние команды
   *
   * @param aTimestamp long метка времени нового состояния
   * @param aState {@link ESkCommandState} идентификатор состояния
   * @param aCause String причина перехода в новое состояние
   * @return {@link SkCommandState} новое состояние
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private SkCommandState createState( long aTimestamp, ESkCommandState aState, String aCause ) {
    TsNullArgumentRtException.checkNulls( aState, aCause );
    Gwid author = Gwid.create( "skat.backend.server", nodeId, null, null, null, null ); //$NON-NLS-1$
    return new SkCommandState( aTimestamp, aState, aCause, author );
  }

  /**
   * Возвращает таймаут выполнения команды после которого команда переводится в состояние
   * {@link ESkCommandState#TIMEOUTED}
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aCommand {@link IDtoCommand} команда
   * @return long long максимальное время (msec) ожидания завершения выполнения команды
   * @throws TsNullArgumentRtException любой аргумент = гдд
   */
  private static long getCmdTimeout( ISkSysdescrReader aSysdescrReader, IDtoCommand aCommand ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aCommand );
    Gwid cmdGwid = aCommand.cmdGwid();
    ISkClassInfo classInfo = aSysdescrReader.findClassInfo( cmdGwid.classId() );
    if( classInfo != null ) {
      String cmdId = cmdGwid.propId();
      IDtoCmdInfo cmdInfo = classInfo.cmds().list().findByKey( cmdId );
      if( cmdInfo != null ) {
        return OP_EXECUTION_TIMEOUT.getValue( cmdInfo.params() ).asLong();
      }
    }
    // Класс или описание команды не найдено. Значение по умолчанию
    return OP_EXECUTION_TIMEOUT.defaultValue().asLong();
  }

  /**
   * Возвращает данные фронтенда "команды"
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд
   * @return {@link S5BaCommandsData} данные фронтенда. null: данные не существуют
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static S5BaCommandsData findCommandsFrontendData( IS5FrontendRear aFrontend ) {
    return aFrontend.frontendData().findBackendAddonData( IBaCommands.ADDON_ID, S5BaCommandsData.class );
  }
}
