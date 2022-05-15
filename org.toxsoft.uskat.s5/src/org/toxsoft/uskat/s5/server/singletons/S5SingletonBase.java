package org.toxsoft.uskat.s5.server.singletons;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.singletons.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.transactions.IS5TransactionDetectorSingleton.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import javax.annotation.*;
import javax.ejb.*;
import javax.enterprise.concurrent.ManagedExecutorService;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.startup.IS5InitialSingleton;
import org.toxsoft.uskat.s5.server.transactions.IS5TransactionDetectorSingleton;
import org.toxsoft.uskat.s5.server.transactions.IS5TransactionManagerSingleton;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;
import org.toxsoft.uskat.s5.utils.threads.IS5DoJobThread;
import org.toxsoft.uskat.s5.utils.threads.impl.S5DoJobThread;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Базовый класс для облегчения реализации синглтонов сервера.
 * <p>
 * Замечания по реализации наследников:
 * <ul>
 * <li>Конечные наследники должны определять аннотации
 * класса: @Startup, @Singleton, @LocalBean, @DependsOn, @TransactionManagement, @TransactionAttribute, @ConcurrencyManagement, @AccessTimeout, @Lock;</li>
 * <li>Следует учитывать, что если бизнес-метод некоторого интерфейса реализуется по умолчанию (interface default
 * implementation), то он не попадает под действие аннотаций класса, что может оказаться недопустимым;</li>
 * <li>Для разрешения deadlock возникащих при вызовов бизнес-методов, попробуйте локализовать/определить причину
 * изменив @ConcurrencyManagement с {@link ConcurrencyManagementType#CONTAINER} на
 * {@link ConcurrencyManagementType#BEAN}.</li>
 * </ul>
 *
 * @author mvk
 */
public class S5SingletonBase
    extends Stridable
    implements IS5Singleton, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Ссылка на первый синглтон.
   */
  @EJB
  private IS5InitialSingleton initialSingleton;

  /**
   * Текущий контест вызова
   */
  @Resource
  private SessionContext sessionContext;

  /**
   * Менеджер транзакций
   */
  @EJB
  private IS5TransactionDetectorSingleton transactionDetector;

  /**
   * Менеджер транзакций
   */
  @EJB
  private IS5TransactionManagerSingleton transactionSingleton;

  /**
   * Исполнитель потоков фоновых потоков s5-служб
   */
  private ManagedExecutorService doJobExecutor;

  /**
   * Опции конфигурации
   */
  private IOptionSet configurationOptions;

  /**
   * Поток вызова {@link IS5ServerJob#doJob()}
   */
  private IS5DoJobThread doJobThread;

  /**
   * Блокировка доступа к фоновым задачам
   */
  private final S5Lockable jobsLock = new S5Lockable();

  /**
   * Текущие выполняемые фоновые задачи службы
   */
  private IListEdit<IS5ServerJob> jobs = new ElemArrayList<>();

  /**
   * Фоновая задача сборки мусора
   */
  private final S5GarbageCollectorJob gcJob = new S5GarbageCollectorJob();

  /**
   * Метка времени запуска синглетона
   */
  private final long launchTimestamp;

  /**
   * Признак того, что синглетон завершает свою работу
   */
  private boolean closing;

  /**
   * Признак того, что синглетон завершил свою работу
   */
  private boolean closed;

  /**
   * Журнал
   */
  private final ILogger logger;

  /**
   * Конструктор для наследников.
   *
   * @param aId String идентификатор синглетона
   * @param aName String имя синглетона
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5SingletonBase( String aId, String aName ) {
    super( aId, aName, TsLibUtils.EMPTY_STRING );
    launchTimestamp = System.currentTimeMillis();
    logger = getLogger( getClass() );
  }

  // ------------------------------------------------------------------------------------
  // Определение жизненного цикла
  //
  /**
   * Загрузка синглетона в контейнер. Метод должен осуществлять всю иницилизацию, необходимую синглтону
   */
  @PostConstruct
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  private void load() {
    // Запись в журнал
    logger.info( MSG_INIT_SINGLETON, id() );
    // Попытка явной регистрации текущей транзакции
    registerCurrentTransaction( "load", NO_METHOD_PARAMS ); //$NON-NLS-1$
    // Инициализация фоновых потоков
    doJobExecutor = S5ServiceSingletonUtils.lookupExecutor( DO_JOB_EXECUTOR_JNDI );
    // Инициализация наследника
    doInit();
  }

  /**
   * Выгрузка синглетона из контейнера. Метод должен завершать работу синглтона
   */
  @PreDestroy
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  private void unload() {
    // Запись в журнал
    logger.info( MSG_CLOSE_SINGLETON, id() );
    // Попытка явной регистрации текущей транзакции
    registerCurrentTransaction( "unload", NO_METHOD_PARAMS ); //$NON-NLS-1$
    // Запуск процесса завершения работы
    closing = true;
    try {
      doClose();
    }
    catch( Throwable e ) {
      logger().error( e );
    }
    closed = true;
    closing = false;
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает метку времени запуска синглетона
   *
   * @return long метка времени
   */
  protected long launchTimestamp() {
    return launchTimestamp;
  }

  /**
   * Возвращает признак того, что синглетон завершает свою работу
   *
   * @return <b>true</b> синглетон завершает свою работу; <b>false</b> синглетон в работе или уже остановлен
   */
  protected final boolean isClosing() {
    return closing;
  }

  /**
   * Возвращает признак того, что синглетон завершил свою работу
   *
   * @return <b>true</b> синглетон завершил свою работу; <b>false</b> синглетон не завершил свою работу
   */
  protected final boolean isClosed() {
    return closed;
  }

  /**
   * Вовзращает логер компоненты.
   *
   * @return {@link ILogger} - логер компоненты
   */
  protected final ILogger logger() {
    return logger;
  }

  /**
   * Возвращает текущий контекст вызова метода синглетона
   *
   * @return {@link SessionContext} текущий контекст сессии
   */
  protected final SessionContext sessionContext() {
    return sessionContext;
  }

  /**
   * Возвращает менеджер транзакций
   *
   * @return {@link IS5TransactionManagerSingleton} менеджер транзакций
   */
  protected final IS5TransactionManagerSingleton transactionManager() {
    return transactionSingleton;
  }

  /**
   * Возвращает исполнителя потоков фоновых потоков s5-служб
   *
   * @return {@link ExecutorService} исполнитель потоков
   */
  protected final ExecutorService doJobExecutor() {
    return doJobExecutor;
  }

  /**
   * Установить таймаут выполнения фоновых задач службы
   *
   * @param aJobsTimeout long рекомендуемый таймаут(мсек) между выполнением одной и той же задачи. <= 0: остановить
   *          механизм выполнения фоновых задач
   */
  protected final void setJobsTimeout( long aJobsTimeout ) {
    lockWrite( jobsLock );
    try {
      // Признак того, что до вызова уже был создан таймер
      boolean wasDoJob = (doJobThread != null);
      if( wasDoJob ) {
        doJobThread.close();
        doJobThread = null;
      }
      if( aJobsTimeout <= 0 ) {
        // Требуется остановить выполнение задач
        if( wasDoJob ) {
          logger().debug( MSG_STOP_DO_JOB, id() );
        }
        return;
      }
      // Создание потока вызова doJob
      doJobThread = new S5DoJobThread( id(), doJobExecutor, new IS5ServerJob() {

        @Override
        public void doJobPrepare() {
          // nop
        }

        @Override
        public void doJob() {
          doJobRun();
        }

        @Override
        public boolean completed() {
          return isClosing() || isClosed();
        }

        @Override
        public void close() {
          // nop
        }

      }, aJobsTimeout, logger );
      // Создание нового потока выполнения фоновых задач
      logger().debug( MSG_START_DO_JOB, id(), Long.valueOf( aJobsTimeout ) );
    }
    finally {
      unlockWrite( jobsLock );
    }
  }

  /**
   * Добавить фоновую собственную задачу на выполнение
   * <p>
   * Если задача уже зарегистрирована и выполняется, то ничего не делает
   *
   * @param aJobsTimeout long рекомендуемый таймаут(мсек) между выполнением одной и той же задачи. <= 0: остановить
   *          механизм выполнения фоновых задач
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final void addOwnDoJob( long aJobsTimeout ) {
    setJobsTimeout( aJobsTimeout );
    addJob( sessionContext().getBusinessObject( IS5ServerJob.class ) );
  }

  /**
   * Добавить фоновую задачу службы на выполнение
   * <p>
   * Если задача уже зарегистрирована и выполняется, то ничего не делает
   *
   * @param aJob {@link IS5ServerJob} фоновая задача службы
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final void addJob( IS5ServerJob aJob ) {
    TsNullArgumentRtException.checkNull( aJob );
    lockWrite( jobsLock );
    try {
      // Если таймер задач doJob не запущен, то инициализируем его по умолчанию
      if( doJobThread == null ) {
        setJobsTimeout( DEFAULT_JOB_TIMEOUT );
      }
      if( !jobs.hasElem( aJob ) ) {
        // Добавление новой задачи
        jobs.add( aJob );
        logger().debug( MSG_ADD_SERVER_JOB, aJob );
        return;
      }
      // Задача уже выполняется
      logger().warning( MSG_ERR_SERVER_JOB_ALREADY_EXIST, aJob );
    }
    finally {
      unlockWrite( jobsLock );
    }
  }

  /**
   * Планирование запуска сборки мусора
   *
   * @param aTimeout long время таймаута перед запуском сборки мусора. <=0: немедленный запуск
   */
  protected final void garbageCollectorStart( long aTimeout ) {
    if( aTimeout <= 0 ) {
      System.gc();
      logger.debug( MSG_GC_FINISH );
      return;
    }
    // Признак того, что задача была завершена
    boolean completed = gcJob.completed();
    // Установка нового таймаута запуска
    gcJob.setTimeout( aTimeout );
    if( completed ) {
      // Запуск фоновой задачи сборки мусора
      addJob( gcJob );
    }
  }

  /**
   * Делает попытку регистрации текущей транзакции
   *
   * @param aMethodName имя выполняемого метода
   * @param aParams Objects[] параметры вызываемого метода
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final void registerCurrentTransaction( String aMethodName, Object[] aParams ) {
    TsNullArgumentRtException.checkNull( aMethodName );
    try {
      Method[] methods = getClass().getMethods();
      for( Method method : methods ) {
        if( method.getName().equals( aMethodName ) ) {
          transactionDetector.onCallBusinessMethod( this, method, aParams );
          return;
        }
      }
      methods = getClass().getDeclaredMethods();
      for( Method method : methods ) {
        if( method.getName().equals( aMethodName ) ) {
          transactionDetector.onCallBusinessMethod( this, method, aParams );
          return;
        }
      }
      methods = S5SingletonBase.class.getMethods();
      for( Method method : methods ) {
        if( method.getName().equals( aMethodName ) ) {
          transactionDetector.onCallBusinessMethod( this, method, aParams );
          return;
        }
      }
      methods = S5SingletonBase.class.getDeclaredMethods();
      for( Method method : methods ) {
        if( method.getName().equals( aMethodName ) ) {
          transactionDetector.onCallBusinessMethod( this, method, aParams );
          return;
        }
      }
      // Метод не найден
      throw new TsIllegalArgumentRtException( MSG_ERR_METHOD_NOT_FOUND, aMethodName );
    }
    catch( Exception e ) {
      // Ошибка регистрации транзакции
      logger.error( e, MSG_ERR_REGISTER_TRANSACTION, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Создание конфигурации по умолчанию
   *
   * @return {@link IOptionSet} конфигурация по умолчанию
   */
  protected IOptionSet doCreateConfiguration() {
    return new OptionSet();
  }

  /**
   * Вызывается после изменения конфигурации синглетона.
   * <p>
   * Этот метод служит для отработки изменения конфигурации в синглтоне.
   * <p>
   * В базовом классе {@link S5SingletonBase} ничего не делает. При переопределении, вызвать родительский метод не
   * нужно.
   * <p>
   * Если метод поднимет исключение, от будет произведен откат установки новой конфигурации
   *
   * @param aPrevConfig {@link IOptionSet} - предыдущая конфигурация службы
   * @param aNewConfig {@link IOptionSet} - новая конфигурация службы
   */
  protected void onConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
    // nop
  }

  /**
   * Вызывается при инициализации синглтона из метода {@link #load()}.
   * <p>
   * {@link #doInit()} выполняется в открытой транзакции
   * <p>
   * Наследники в этом методе должны провести инициализцаию работы синглетона. К моменту вызова этого метода существует
   * конфигурация синглетона {@link #configuration()}, либо созданная по умолчанию из описания типа CONFIG_TYPE, либо
   * считанная из БД.
   */
  protected void doInit() {
    // nop
  }

  /**
   * Вызывается перед уничтожением синглтона из метода {@link #unload()}.
   * <p>
   * {@link #doClose()} выполняется в открытой транзакции
   * <p>
   * Наследники в этом методе должны освободить ресурсы, и завершить то, что было занято и начато в {@link #doInit()}.
   * Фактически, этот метод вызвается при завершении работы синглетона.
   * <p>
   * В классе {@link S5SingletonBase} ничего не делает, при переопределении вызывать не нужно.
   */
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Выполнение задач doJob
   */
  final void doJobRun() {
    // Выполнение фоновых задач службы
    lockRead( jobsLock );
    // Список задач перед выполнением
    IList<IS5ServerJob> beforeJobs = null;
    // Список задач после выполнения
    IListEdit<IS5ServerJob> afterJobs = null;
    try {
      // Список задач перед выполнением
      beforeJobs = new ElemArrayList<>( jobs );
      // Список задач после выполнения
      afterJobs = new ElemArrayList<>( jobs.size() );
      // Очищаем список задач чтобы в после прохода в нем остались только вновь добавленные задачи
      jobs.clear();
    }
    finally {
      unlockRead( jobsLock );
    }
    for( IS5ServerJob job : beforeJobs ) {
      if( job.completed() ) {
        // Задача уже завершена
        continue;
      }
      // Выполнение задачи
      try {
        job.doJob();
      }
      catch( Throwable e ) {
        // Ошибка выполнения задачи
        logger.error( e, MSG_ERR_SERVER_JOB, job, cause( e ) );
      }
      if( job.completed() ) {
        // Задача завершена
        logger().debug( MSG_REMOVE_SERVER_JOB, job );
        continue;
      }
      // Задача будет выполнена при следующем событии таймера
      afterJobs.add( job );
    }
    // Установка фоновых задач для следующего события таймера
    lockWrite( jobsLock );
    try {
      // Задачи оставшиеся после прохода находятся в начале. Вновь добавленые на проходе - в конце
      jobs.insertAll( 0, afterJobs );
    }
    finally {
      unlockWrite( jobsLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5Singleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IOptionSet configuration() {
    if( configurationOptions == null ) {
      IOptionSetEdit newConfiguration = new OptionSet( doCreateConfiguration() );
      IOptionSet configuration = initialSingleton.loadServiceConfig( id() );
      if( configuration != null ) {
        newConfiguration.addAll( configuration );
      }
      configurationOptions = newConfiguration;
    }
    try {
      return configurationOptions;
    }
    catch( Throwable e ) {
      // Недопустимый тип конфиграции
      throw new TsIllegalArgumentRtException( MSG_ERR_WRONG_CONFIG_TYPE, id(), cause( e ) );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void saveConfiguration( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    // Предыдущая конфигурация
    IOptionSet prevConfiguration = configurationOptions;
    IOptionSet newConfiguration = aConfiguration;
    // Установка новой конфигурации
    initialSingleton.saveServiceConfig( id(), newConfiguration );
    configurationOptions = newConfiguration;
    // Извещение наследников об изменении конфигурации
    try {
      onConfigChanged( prevConfiguration, newConfiguration );
    }
    catch( RuntimeException e ) {
      // Откат установки конфигурации службы
      logger().error( e, MSG_ERR_SERVER_CONFIG_ROLLBACK, id(), cause( e ) );
      configurationOptions = prevConfiguration;
      throw e;
    }
  }

  // ------------------------------------------------------------------------------------
  // НЕЯВНАЯ Реализация IS5ServerJob
  //
  /**
   * {@link IS5ServerJob#doJobPrepare()}
   */
  // @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  public void doJobPrepare() {
    // Метод может быть переопределен в наследнике с правильной (для наследника) установкой аннотаций:
    // @TransactionAttribute( TransactionAttributeType.??? ) и @Lock( LockType.??? )
  }

  /**
   * {@link IS5ServerJob#doJob()}
   */
  // @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  public void doJob() {
    // Метод может быть переопределен в наследнике с правильной (для наследника) установкой аннотаций:
    // @TransactionAttribute( TransactionAttributeType.??? ) и @Lock( LockType.??? )
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Количество команд на выполнении
      logger().debug( MSG_DOJOB );
    }
  }

  /**
   * {@link IS5ServerJob#completed()}
   *
   * @return <b>true<b> выполнение задачи завершено или прекращено методом {@link #close()}; <b>false</b> задача еще
   *         выполняется.
   */
  // @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  public boolean completed() {
    // Метод может быть переопределен в наследнике с правильной (для наследника) установкой аннотаций:
    // @TransactionAttribute( TransactionAttributeType.??? ) и @Lock( LockType.??? )
    return isClosed() || isClosing();
  }

  /**
   * {@link IS5ServerJob#close()}
   */
  // @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  public void close() {
    // Метод может быть переопределен в наследнике с правильной (для наследника) установкой аннотаций:
    // @TransactionAttribute( TransactionAttributeType.??? ) и @Lock( LockType.??? )
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id().hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( !(aObj instanceof IS5Singleton other) ) {
      return false;
    }
    if( !id().equals( other.id() ) ) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return String.format( MSG_STRING_FORMAT, getClass().getSimpleName(), id() );
  }

  /**
   * Фоновая задача сборки мусора
   *
   * @author mvk
   */
  private class S5GarbageCollectorJob
      implements IS5ServerJob {

    private volatile long startTime = TimeUtils.MAX_TIMESTAMP;

    /**
     * Создание задачи сборки мусора
     */
    S5GarbageCollectorJob() {
    }

    // ------------------------------------------------------------------------------------
    // Открытое API
    //
    /**
     * Установить время следущего запуска задачи
     *
     * @param aTime long время (мсек) следующего запуска задачи
     */
    void setTimeout( long aTime ) {
      startTime = System.currentTimeMillis() + aTime;
    }

    // ------------------------------------------------------------------------------------
    // Реализация IS5ServerJob
    //
    @Override
    public void doJobPrepare() {
      // nop
    }

    @Override
    public void doJob() {
      if( startTime == TimeUtils.MAX_TIMESTAMP ) {
        return;
      }
      long currTime = System.currentTimeMillis();
      if( startTime >= currTime ) {
        return;
      }
      // Запуск сборки мусора
      logger().debug( MSG_GC_START );
      System.gc();
      startTime = TimeUtils.MAX_TIMESTAMP;
      logger().debug( MSG_GC_FINISH, Long.valueOf( System.currentTimeMillis() - currTime ) );
    }

    @Override
    public void close() {
      startTime = TimeUtils.MAX_TIMESTAMP;
    }

    @Override
    public boolean completed() {
      return startTime == TimeUtils.MAX_TIMESTAMP;
    }

    // ------------------------------------------------------------------------------------
    // Реализация Object
    //
    @Override
    public int hashCode() {
      // TODO Auto-generated method stub
      return super.hashCode();
    }

    @Override
    public boolean equals( Object aObj ) {
      // TODO Auto-generated method stub
      return super.equals( aObj );
    }

    @Override
    public String toString() {
      return MSG_GC;
    }
  }
}
