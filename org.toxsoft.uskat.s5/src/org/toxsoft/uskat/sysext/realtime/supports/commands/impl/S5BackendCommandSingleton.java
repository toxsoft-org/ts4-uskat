package org.toxsoft.uskat.sysext.realtime.supports.commands.impl;

import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;
import static org.toxsoft.uskat.sysext.realtime.addon.S5RealtimeUtils.*;
import static org.toxsoft.uskat.sysext.realtime.supports.commands.IS5CommandHardConstants.*;
import static org.toxsoft.uskat.sysext.realtime.supports.commands.impl.IS5Resources.*;
import static ru.uskat.common.dpu.rt.cmds.ESkCommandState.*;
import static ru.uskat.core.impl.SkGwidUtils.*;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.impl.S5BackendSequenceSupportSingleton;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceFactory;
import org.toxsoft.uskat.s5.server.transactions.S5TransactionUtils;
import org.toxsoft.uskat.s5.utils.collections.S5FixedCapacityTimedList;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;
import org.toxsoft.uskat.sysext.realtime.addon.S5RealtimeFrontendData;
import org.toxsoft.uskat.sysext.realtime.supports.commands.IS5BackendCommandSingleton;
import org.toxsoft.uskat.sysext.realtime.supports.commands.sequences.IS5CommandSequence;
import org.toxsoft.uskat.sysext.realtime.supports.commands.sequences.IS5CommandSequenceEdit;

import ru.uskat.backend.messages.*;
import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.rt.cmds.*;
import ru.uskat.common.dpu.rt.events.SkCommandStateChangeInfoList;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

/**
 * Реализация синглетона {@link IS5BackendCommandSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_EVENTS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendCommandSingleton
    extends S5BackendSequenceSupportSingleton<IS5CommandSequence, IDpuCompletedCommand>
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
   * Ключ: идентификатор команды, {@link IDpuCommand#id()} ;<br>
   * Значение:
   * <ul>
   * <li>{@link Pair#left()} = {@link IDpuCommand} команда;</li>
   * <li>{@link Pair#right()} = {@link ITimedListEdit} история состояний.</li>
   * </ul>
   * .
   */
  @Resource( lookup = INFINISPAN_CACHE_CMD_STATES )
  private Cache<String, Pair<IDpuCommand, ITimedListEdit<SkCommandState>>> executingCommandsCache;

  /**
   * Идентификатор узла кластера сервера
   */
  private String nodeId;

  /**
   * Карта исполнителей команд:
   * <p>
   * Ключ: идентификатор конкретной команды, конкретного объекта;<br>
   * Значение: исполнитель
   */
  private final IMapEdit<Gwid, IS5FrontendRear> executors = new ElemMap<>();

  /**
   * Блокировка доступа к {@link #executors}.
   */
  private final S5Lockable executorsLock = new S5Lockable();

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
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IGwidList getExecutableCommandGwids() {
    // 2020-08-05 mvk
    // GwidList retValue = new GwidList();
    // for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
    // S5BaCommandsFrontendData frontendData = getRealtimeFrontendData( frontend );
    // if( frontendData == null ) {
    // // Фроненд не поддерживает реальное время
    // continue;
    // }
    // retValue.addAll( frontendData.commands.gwids() );
    // }
    lockRead( executorsLock );
    try {
      return new GwidList( executors.keys() );
    }
    finally {
      unlockRead( executorsLock );
    }
  }

  @Override
  // 2020-11-20 mvk
  // @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  public void setExecutableCommandGwids( IS5FrontendRear aFrontend, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aNeededGwids );
    // Время начала запроса
    long traceStartTime = System.currentTimeMillis();
    // TODO: 2020-08-05 mvk ??? что делать с кластеризацией + executors ???
    // TODO: 2020-08-05 mvk ??? отработка изменения системного описания (классы, объекты, команды) ???
    IListEdit<Gwid> addGwids = new ElemLinkedList<>();
    for( Gwid gwid : aNeededGwids ) {
      // Замена групповых идентификаторов на конкретные
      addGwids.addAll( expandMultiGwid( sysdescrReader(), objectsBackend(), gwid ) );
    }
    // Количество ранее существующих команд исполнителя
    int prevCmdCount = 0;
    // Новое количество команд исполнителя
    int newCmdCount = 0;
    // Общее количество команд всех исполнителей
    int allCmdCount = 0;
    // Список всех идентификаторов команд на которые зарегистрированы исполнители
    IGwidList allCmdGwids = null;
    lockWrite( executorsLock );
    try {
      // Удаление старого списка исполнителей фронтенда
      for( Gwid gwid : new ElemArrayList<>( executors.keys() ) ) {
        IS5FrontendRear frontend = executors.getByKey( gwid );
        if( frontend.equals( aFrontend ) ) {
          executors.removeByKey( gwid );
          prevCmdCount++;
        }
      }
      // Проверка конфликтов с другими исполнителями
      for( Gwid gwid : addGwids ) {
        IS5FrontendRear frontend = executors.findByKey( gwid );
        if( frontend != null && frontend.equals( aFrontend ) ) {
          // Повторное указание команды в списке исполнителя
          logger().warning( ERR_EXECUTOR_DOUBLE_REGISTER, gwid, frontend );
          continue;
        }
        if( frontend != null ) {
          // Для команды был зарегистрирован другой исполнитель
          logger().error( ERR_EXECUTOR_EXIST, aFrontend, gwid, frontend );
          // 2020-08-15 mvk есть предположение о формировании deadlock
          // throw new TsIllegalArgumentRtException( ERR_EXECUTOR_EXIST, aFrontend, gwid, frontend );
        }
      }
      // Изменение карты исполнителей
      for( Gwid addGwid : addGwids ) {
        executors.put( addGwid, aFrontend );
      }
      // Список всех зарегистрированных команд
      allCmdGwids = new GwidList( executors.keys() );
      newCmdCount = addGwids.size();
      allCmdCount = allCmdGwids.size();
      // Вывод в журнал исполнителей команд
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        for( Gwid gwid : allCmdGwids ) {
          logger().debug( MSG_CMD_EXECUTOR, gwid, executors.getByKey( gwid ) );
        }
      }
    }
    finally {
      unlockWrite( executorsLock );
    }

    // Список идентификаторов команд поддерживаемых всеми исполнителями
    // GwidList allFrontendGwids = new GwidList( aNeededGwids );
    // TODO: 2020-07-24 mvkd код вызывает overhead на запуске сервера и массового подключения клиентов
    // TODO: 2020-07-24 mvkd ТРЕБУЕТСЯ переработка!
    // for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
    // if( frontend.equals( aFrontend ) == true ) {
    // // Свой frontend пропускаем
    // continue;
    // }
    // S5BaCommandsFrontendData frontendData = getRealtimeFrontendData( frontend );
    // if( frontendData == null ) {
    // // Фроненд не поддерживает реальное время
    // continue;
    // }
    // for( Gwid neededGwid : aNeededGwids ) {
    // IGwidList frontendGwids = frontendData.commands.gwids();
    // allFrontendGwids.addAll( frontendGwids );
    // for( Gwid frontendGwid : frontendGwids ) {
    // if( acceptableCmd( sysdescrBackend(), neededGwid, frontendGwid ) == false
    // && acceptableCmd( sysdescrBackend(), frontendGwid, neededGwid ) == false ) {
    // // Исполнители команд не пересекаются по выбранным gwid-идентификаторам
    // continue;
    // }
    // // Для команды уже зарегистрирован исполнитель
    // throw new TsIllegalArgumentRtException( ERR_EXECUTOR_EXIST, neededGwid, frontend, frontendGwid );
    // }
    // }
    // }
    S5RealtimeFrontendData frontendData = getRealtimeFrontendData( aFrontend );
    if( frontendData == null ) {
      // Недопустимый usecase
      throw new TsInternalErrorRtException();
    }
    // 2020-08-05 mvk
    // // Регистрация команд исполнителя
    // ValidationResult result = frontendData.commands.setExcutableCommandGwids( aFrontend.toString(), aNeededGwids );
    // // Запись в журнал результата установки списка исполняемых команд
    // Logger.resultToLog( logger(), result );
    // Время начала запроса
    long traceSendEventTime = System.currentTimeMillis();
    // Сообщение frontend
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      frontendData = getRealtimeFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Оповещение фронтенда
      SkMessageWhenCommandExecutorsChanged.send( frontend, allCmdGwids );
    }
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Журналирование
    Integer pcc = Integer.valueOf( prevCmdCount );
    Integer ncc = Integer.valueOf( newCmdCount );
    Integer acc = Integer.valueOf( allCmdCount );
    Long registerTime = Long.valueOf( traceSendEventTime - traceStartTime );
    Long eventTime = Long.valueOf( currTime - traceSendEventTime );
    logger().info( MSG_SET_EXECUTABLE_CMDS, aFrontend, pcc, ncc, acc, registerTime, eventTime );
  }

  @Override
  @Asynchronous
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void sendCommand( IS5FrontendRear aFrontend, IDpuCommand aCommand ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aCommand );
    TsIllegalArgumentRtException.checkTrue( executingCommandsCache.containsKey( aCommand.id() ) );
    try {
      // Текущее время
      long currTime = System.currentTimeMillis();
      // Gwid-идентификатор команды
      Gwid cmdGwid = aCommand.cmdGwid();
      // Данные фронтенда отправляющего команду
      S5RealtimeFrontendData frontendData = getRealtimeFrontendData( aFrontend );
      if( frontendData == null ) {
        // Недопустимый usecase
        throw new TsInternalErrorRtException();
      }
      // Фиксируется факт передачи команды через целевой frontend
      ValidationResult addResult = frontendData.commands.addExecutingCmd( aCommand.id() );
      // Запись в журнал результата добавления команды в очередь ожидания
      LoggerWrapper.resultToLog( logger(), addResult );
      // Поиск исполнителя и передача ему команды
      // 2020-08-05 mvk
      IS5FrontendRear cmdFrontend = null;
      lockRead( executorsLock );
      try {
        cmdFrontend = executors.findByKey( cmdGwid );
      }
      finally {
        unlockRead( executorsLock );
      }

      // 2020-08-06 mvk: try fix concurrent modification of command states
      // if( cmdFrontend != null ) {
      // // Отправляем команду на исполнение
      // SkMessageExecuteCommand.send( cmdFrontend, aCommand );
      // // Изменение состояния на "исполняется"
      // changeCommandState( aCommand, new TimedList<>(), createState( currTime, EXECUTING, REASON_EXEC_BY_QUERY ) );
      // return;
      // }
      if( cmdFrontend != null ) {
        // Изменение состояния на "исполняется"
        changeCommandState( aCommand, new TimedList<>(), createState( currTime, EXECUTING, REASON_EXEC_BY_QUERY ) );
        // Отправляем команду на исполнение
        SkMessageExecuteCommand.send( cmdFrontend, aCommand );
        return;
      }

      // Не найден исполнитель команды
      changeCommandState( aCommand, new TimedList<>(), createState( currTime, UNHANDLED, REASON_EXECUTOR_NOT_FOUND ) );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка обработки
      logger().error( e );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void changeCommandState( IS5FrontendRear aFrontend, DpuCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aStateChangeInfo );
    // Поиск выполняемой команды
    Pair<IDpuCommand, ITimedListEdit<SkCommandState>> cmd = executingCommandsCache.get( aStateChangeInfo.cmdId() );
    if( cmd == null ) {
      // Получение изменения состояния несуществующей команды
      logger().error( ERR_COMMAND_NOT_FOUND, aStateChangeInfo.cmdId(), aStateChangeInfo.state() );
      return;
    }
    // Изменение состояния. false: установка состояния уже выполняемой команды
    changeCommandState( cmd.left(), cmd.right(), aStateChangeInfo.state() );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public ITimedList<IDpuCompletedCommand> queryCommands( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    long traceStartTime = System.currentTimeMillis();
    // Подготовка списка идентификаторов запрашиваемых объектов. false: без повторов
    GwidList objIds = new GwidList();
    for( Gwid gwid : aNeededGwids ) {
      if( gwid.kind() != EGwidKind.GW_CMD ) {
        // По контракту идентификаторы не команды молча игнорируются
        continue;
      }
      if( !gwid.isAbstract() ) {
        // Определен идентификатор объекта
        objIds.add( Gwid.createObj( gwid.classId(), gwid.strid() ) );
        continue;
      }
      // Все объекты указанного класса. true: включая наследников
      IStringList classIds = sysdescrReader().getClassInfos( gwid.classId() ).keys();
      if( classIds.size() == 0 ) {
        continue;
      }
      IList<IDpuObject> objs = objectsBackend().readObjects( classIds );
      for( IDpuObject obj : objs ) {
        objIds.add( Gwid.createObj( obj.classId(), obj.strid() ) );
      }
    }
    // Чтение истории команд
    long traceReadStartTime = System.currentTimeMillis();
    IList<IS5CommandSequence> sequences = readSequences( objIds, aInterval, ACCESS_TIMEOUT_DEFAULT );
    long traceReadEndTime = System.currentTimeMillis();

    // Фильтрация команд и формирование сводного(по объектам) результата запроса
    TimedList<IDpuCompletedCommand> commands = new TimedList<>();
    for( IS5CommandSequence sequence : sequences ) {
      for( ISequenceBlock<IDpuCompletedCommand> block : sequence.blocks() ) {
        for( int index = 0, n = block.size(); index < n; index++ ) {
          IDpuCompletedCommand command = block.getValue( index );
          for( Gwid gwid : aNeededGwids ) {
            if( acceptableCmd( sysdescrBackend(), gwid, command.cmd().cmdGwid() ) ) {
              commands.add( command );
            }
          }
        }
      }
    }

    // Формирование результата. aAllowDuplicates = true
    ITimedListEdit<IDpuCompletedCommand> retValue = new S5FixedCapacityTimedList<>( commands.size(), true );
    retValue.addAll( commands );

    long traceResultTime = System.currentTimeMillis();
    // Формирование журнала
    Integer gc = Integer.valueOf( objIds.size() );
    Integer rc = Integer.valueOf( retValue.size() );
    Long pt = Long.valueOf( traceReadStartTime - traceStartTime );
    Long rt = Long.valueOf( traceReadEndTime - traceReadStartTime );
    Long ft = Long.valueOf( traceResultTime - traceReadEndTime );
    Long at = Long.valueOf( traceResultTime - traceStartTime );
    // Завершено чтение истории команд
    logger().info( MSG_READ_COMMANDS, gc, aInterval, rc, at, pt, rt, ft );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ServerJob
  //
  @Override
  public void doJob() {
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Количество команд на выполении
    int count = 0;
    try( CloseableIterator<Pair<IDpuCommand, ITimedListEdit<SkCommandState>>> iterator =
        executingCommandsCache.values().iterator() ) {
      while( iterator.hasNext() ) {
        Pair<IDpuCommand, ITimedListEdit<SkCommandState>> cmdPair = iterator.next();
        IDpuCommand cmd = cmdPair.left();
        // 2020-03-21, mvk, клиент может не иметь доступа к первичному узлу кластера - узел работает в другой сети и у
        // клиента нет к нему доступа (пример: Тбилиси, локальные сервера станции, клиенты программа НУ).
        // if( clusterManager.isPrimary() == true && //
        // currTime - cmd.timestamp() >= getCmdTimeout( sysdescrReader(), cmd ) ) {
        if( currTime - cmd.timestamp() >= getCmdTimeout( sysdescrReader(), cmd ) ) {
          // Завершение выполнения команды по таймауту
          changeCommandState( cmd, cmdPair.right(), createState( currTime, TIMEOUTED, REASON_CANCEL_BY_TIMEOUT ) );
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
  protected ISequenceFactory<IDpuCompletedCommand> doCreateFactory() {
    return new S5CommandSequenceFactory( backend().initialConfig().impl(), sysdescrReader() );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSequenceSupportSingleton
  //
  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterUpdateClass( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    if( aPrevClassInfo.cmdInfos().size() > 0 && aNewClassInfo.cmdInfos().size() == 0 ) {
      // Удаление идентификаторов данных объектов у которых больше нет команд
      IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
      // Список объектов изменившихся классов
      IList<IDpuObject> objs = S5TransactionUtils.txUpdatedClassObjs( transactionManager(), objectsBackend(),
          aNewClassInfo.id(), aDescendants );
      for( IDpuObject obj : objs ) {
        String classId = obj.classId();
        ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
        if( classInfo.cmdInfos().size() == 0 ) {
          gwidsEditor.removeByKey( Gwid.createObj( classId, obj.strid() ) );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doBeforeDeleteClass( IDpuSdClassInfo aClassInfo ) {
    String classId = aClassInfo.id();
    // Удаление идентификаторов данных объектов удаленного класса
    if( aClassInfo.cmdInfos().size() > 0 ) {
      // Удаление идентификаторов данных объектов удаленного класса
      IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
      for( Gwid gwid : new ElemArrayList<>( gwidsEditor.keys() ) ) {
        if( gwid.classId().equals( classId ) ) {
          gwidsEditor.removeByKey( gwid );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterWriteObjects( IMap<ISkClassInfo, IList<IDpuObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDpuObject>> aCreatedObjs ) {
    // Удаление идентификаторов данных удаленных объектов
    IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      if( classInfo.cmdInfos().size() == 0 ) {
        // У класса объекта нет команд
        continue;
      }
      IList<IDpuObject> objs = aRemovedObjs.getByKey( classInfo );
      for( IDpuObject obj : objs ) {
        gwidsEditor.removeByKey( Gwid.createObj( obj.classId(), obj.strid() ) );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Установка нового состояния команды
   *
   * @param aCommand {@link IDpuCommand} команда {@link IDpuCommand#id()}
   * @param aStates {@link ITimedListEdit}&lt;{@link SkCommandState}&gt; история состояния команды
   * @param aState {@link SkCommandState} новое состояние команды
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый переход состояния команды
   */
  private void changeCommandState( IDpuCommand aCommand, ITimedListEdit<SkCommandState> aStates,
      SkCommandState aState ) {
    TsNullArgumentRtException.checkNulls( aCommand, aStates, aState );
    // Предыдущее состояние команды
    SkCommandState prevState = aStates.last();
    // Проверка допустимых переходов состояния выполнения команды
    switch( aState.state() ) {
      case SENDING:
        if( prevState != null && prevState.state() != SENDING ) {
          throw new TsIllegalArgumentRtException();
        }
        break;
      case EXECUTING:
        if( prevState == null ) {
          // Команда одновременно послана и запущена на выполнение
          prevState = createState( aState.timestamp(), SENDING, REASON_SEND_AND_EXEC );
          aStates.add( prevState );
        }
        if( prevState.state() != SENDING && prevState.state() != EXECUTING ) {
          // Недопустимый переход состояния команды
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, aState );
        }
        break;
      case UNHANDLED:
        if( prevState == null ) {
          // Команда одновременно послана и отменена так как нет ее исполнителя
          prevState = createState( aState.timestamp(), SENDING, REASON_SEND_AND_CANCEL );
          aStates.add( prevState );
        }
        if( prevState.state() != SENDING ) {
          // Недопустимый переход состояния команды
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, aState );
        }
        break;
      case TIMEOUTED:
      case SUCCESS:
      case FAILED:
        if( prevState == null || prevState.state() != EXECUTING ) {
          // Недопустимый переход состояния команды
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, aState );
        }
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Добавление нового состояния в историю
    aStates.add( aState );
    // Журнал: изменение состояния команды
    logger().info( MSG_NEW_STATE, aCommand, prevState, aState );

    // Идентификатор команды
    String cmdId = aCommand.id();
    if( !aState.state().isFinished() ) {
      // Продолжение выполнения команды. Обновление кэша
      executingCommandsCache.put( cmdId, new Pair<>( aCommand, aStates ) );
    }
    if( aState.state().isFinished() ) {
      // Завершение выполнения команды. Удаление команды из кэша
      executingCommandsCache.remove( cmdId );
      // Запись истории в базу данных
      writeCommand( new DpuCompletedCommand( aCommand, aStates ) );
    }
    // Формирование результата
    ITimedListEdit<DpuCommandStateChangeInfo> dpuStates = new TimedList<>();
    for( SkCommandState state : aStates ) {
      dpuStates.add( new DpuCommandStateChangeInfo( cmdId, state ) );
    }
    // Сообщение frontend
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5RealtimeFrontendData frontendData = getRealtimeFrontendData( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает реальное время
        continue;
      }
      // Поддержка выполнения команд
      S5CommandSupport commandsSupport = frontendData.commands;
      // Список изменений состояний команд переданных через целевой frontend
      S5CommandStateChangeInfoList states = commandsSupport.updateExecutingCmds( dpuStates );
      if( states.size() == 0 ) {
        // В текущем фронтенде нет состояний команд ожидающих выполнения
        continue;
      }
      // Оповещение фронтенда
      SkMessageWhenCommandsStateChanged.send( frontend, states );
    }
  }

  /**
   * Реализация сохранения завершенной команды в истории команд
   *
   * @param aCommand {@link IDpuCompletedCommand} команда для сохранения
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void writeCommand( IDpuCompletedCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    // Формирование последовательностей истории команд по объектам
    Skid skid = aCommand.cmd().cmdGwid().skid();
    TimedList<IDpuCompletedCommand> commands = new TimedList<>( aCommand );
    Gwid objId = Gwid.createObj( skid.classId(), skid.strid() );
    IQueryInterval interval = new QueryInterval( CSCE, commands.first().timestamp(), commands.last().timestamp() );
    try {
      IS5CommandSequenceEdit sequence = new S5CommandSequence( factory(), objId, interval, IList.EMPTY );
      sequence.set( commands );
      // Cохранение событий в базе данных
      IS5BackendCommandSingleton sequenceWriter =
          sessionContext().getBusinessObject( IS5BackendCommandSingleton.class );
      sequenceWriter.writeSequences( new ElemArrayList<IS5CommandSequence>( sequence ) );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
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
    Gwid author = Gwid.createObj( "skat.backend.server", nodeId ); //$NON-NLS-1$
    return new SkCommandState( aTimestamp, aState, aCause, author );
  }

  /**
   * Возвращает таймаут выполнения команды после которого команда переводится в состояние
   * {@link ESkCommandState#TIMEOUTED}
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aCommand {@link IDpuCommand} команда
   * @return long long максимальное время (msec) ожидания завершения выполнения команды
   * @throws TsNullArgumentRtException любой аргумент = гдд
   */
  private static long getCmdTimeout( ISkSysdescrReader aSysdescrReader, IDpuCommand aCommand ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aCommand );
    Gwid cmdGwid = aCommand.cmdGwid();
    ISkClassInfo classInfo = aSysdescrReader.findClassInfo( cmdGwid.classId() );
    if( classInfo != null ) {
      String cmdId = cmdGwid.propId();
      ISkCmdInfo cmdInfo = classInfo.cmdInfos().findByKey( cmdId );
      if( cmdInfo != null ) {
        return OP_EXECUTION_TIMEOUT.getValue( cmdInfo.params() ).asLong();
      }
    }
    // Класс или описание команды не найдено. Значение по умолчанию
    return OP_EXECUTION_TIMEOUT.defaultValue().asLong();
  }

  /**
   * Раскрывает мульти-Gwid в список одиночных GWid-ов, существующих в системе.
   * <p>
   * Если аргумент не мульти-Gwid, то метод вернет список из одного элемента - аргумента <code>aGwid</code>, а для
   * несуществующего {@link Gwid} - пустой список.
   * <p>
   * TODO: на уровне реализации должен быть связан с {@link ISkGwidManager#expandMultiGwid(Gwid)}
   *
   * @param aSysdescr {@link ISkSysdescrReader} читатель системного описания
   * @param aObjService {@link IS5BackendObjectsSingleton} служба объекты
   * @param aGwid {@link Gwid} - раскрываемый {@link Gwid}
   * @return {@link IGwidList} - список Gwid-ов, раскрывающий аргумент
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IGwidList expandMultiGwid( ISkSysdescrReader aSysdescr, IS5BackendObjectsSingleton aObjService,
      Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aSysdescr, aObjService, aGwid );
    if( !aGwid.isMulti() ) {
      return new GwidList( aGwid );
    }
    // TODO: реализовать IGwidList expandMultiGwid(Gwid)
    GwidList retValue = new GwidList();
    return retValue;
  }
}
