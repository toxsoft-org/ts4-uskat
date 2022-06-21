package org.toxsoft.uskat.core.api.cmdserv;

import static org.toxsoft.uskat.core.api.cmdserv.ISkResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The command state.
 * <p>
 * <h2>Изменение состояния в течение жизненного цикла команды</h2>
 * <ul>
 * <li>Команда инициируется методом ISkCommandService.sendCommand(), где создается экземпляр ISkCommand и он сразу
 * получает состояние SENDING;</li>
 * <li>Далее команда через бекенд идет на сервер. Сервер ищет адресата (Sk-объект) и исполнителя. Если исполнитель или
 * адресат не найден, выставляется состояние UNHANDLED и выполнение команды прекращается. Если исполнитель найден, то
 * команде выставляется состояние EXECUTING и она отправляется исполнителю. Сразу после отправки команды исполнителю
 * сервер начинает отсчет времени таймаута команды;</li>
 * <li>Исполнитель получает команду и начинает его исполнять. По мере исполнения состояние EXECUTING может обновлятся, с
 * уточнением процесса выполнения в параметрах состояния. В зависимости от команды, состояния исполнителя, внешнего
 * оборудования и других факторов команда может быть исполнена или исполнение невозможно. В первом случае исполнитель
 * выставляет состояние SUCCESS, а во втором FAILED, и на этом исполнение команды завершается. Следует отметить, что от
 * момента поступления команды исполнителю и до завершения (с любым результатом) ее исполнения может пройти большое
 * время. Например, команда «двинуть анод на 8 секунд вверх» может занять 8 секунд;</li>
 * <li>Команда при прохождении с начала до сервера имеет состояние SENDING, от сервера и в исполнителе EXECUTING.
 * Исполнение команды может быть завершено сервером состоянием UNHANDLED, а исполнителем состоянием FAILED. Сервер также
 * может завершить прохождение EXECUTING команды состоянием TIMOUTED;</li>
 * <li>Таким образом, есть только следующие возможные цепочки состояния команды в течение его жизненного цикла:
 * <ul>
 * <li>SENDING > UNHANDLED — команда не имеет исполнителя и/или адресата;</li>
 * <li>SENDING > EXECUTING > FAILED — не удалось выполнить команду;</li>
 * <li>SENDING > EXECUTING > SUCCESS — команда успешно выполнена;</li>
 * <li>SENDING > EXECUTING > TIMEOUTED — система не дождалась выполнения команды.</li></li>
 * </ul>
 * <p>
 * <h2>Примечания:</h2>
 * <ul>
 * <li>Все состояния, кроме SENDING выставляются с помощью методов ISkCommandStateEditor. Состояние SENDING выставляется
 * в момент создания экземпляра ISkCommand в сервисе ISkCommandService;</li>
 * <li>Команда имеет состояния SENDING и EXECUTING только во время выполнения, после завершения он имеет одно из
 * терминальных состоянии. Терминальными являются TIMEOUTED, SUCCESS и FAILED;</li>
 * <li>Хотя формально после TIMEOUTED и может прилететь состояние SUCCESS или FAILED от исполнителя, система не изменит
 * состояние завершенной команды (команды в терминальном состоянии), а в журнале занесет сообщение о том, что TIMEOUTED
 * команда получила обновление состояния. Однако, данное поведение может быть изменено в дальнейшем, и возможно
 * состояние TIMEOUTED может стать изменяемым;</li>
 * <li>состояние {@link #EXECUTING} может повторяться несколько раз, ведь исполнитель может по мере реального исполнения
 * выдавать уточняющую информцию в параметрах состояния {@link SkCommandState#params()};</li>
 * <li>В базу данных (то есть, в историю) команды попадают попадают только и сразу после перехода в терминальное
 * состояние.</li>
 * <li></li>
 * <li></li>
 * </ul>
 *
 * @author hazard157
 */
public enum ESkCommandState
    implements IStridable {

  /**
   * Команда создана и отправлена на сервер для передачи исполнителю.
   * <p>
   * Это начальное сосотяние, которое устанавливается реализацией сервиса <code>ISkCommandService</code> при создании
   * экземпляра <code>ISkCommand</code> в методе <code>ISkCommandService#sendCommand(Gwid, Skid, IOptionSet)</code>.
   */
  SENDING( "Sending", STR_D_SENDING, STR_N_SENDING, false ), //$NON-NLS-1$

  /**
   * Команда дошла до адресата (исполнителя) и выполняется.
   * <p>
   * Это состояние может несколько раз обновляться с уточнением процесса исполнения в параметрах
   * {@link SkCommandState#params()}.
   */
  EXECUTING( "Executing", STR_D_EXECUTING, STR_N_EXECUTING, false ), //$NON-NLS-1$

  /**
   * Некому выполнить команду - адресат не найден.
   * <p>
   * Это терминальное состояние (то есть, жизненный цикл команды завершен).
   */
  UNHANDLED( "Unhandled", STR_D_UNHANDLED, STR_N_UNHANDLED, true ), //$NON-NLS-1$

  /**
   * Исполнителю не удалось выполнить команду.
   * <p>
   * Это терминальное состояние (то есть, жизненный цикл команды завершен).
   */
  FAILED( "Failed", STR_D_FAILED, STR_N_FAILED, true ), //$NON-NLS-1$

  /**
   * Таймаут - от исполнителя слишком долго нет отчета о выполнении команды.
   * <p>
   * Это терминальное состояние (то есть, жизненный цикл команды завершен), с той оговоркой, что после таймаута
   * теоретическ могут поступить состояния {@link #FAILED} или {@link #SUCCESS}, но они будут игнорированы. Только в
   * журнале можно будет найти информацию об этом.s
   */
  TIMEOUTED( "Timeouted", STR_D_TIMEOUTED, STR_N_TIMEOUTED, true ), //$NON-NLS-1$

  /**
   * Выполнение команды успешно завершено.
   * <p>
   * Это терминальное состояние (то есть, жизненный цикл команды завершен).
   */
  SUCCESS( "Success", STR_D_SUCCESS, STR_N_SUCCESS, true ); //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<ESkCommandState> KEEPER = new StridableEnumKeeper<>( ESkCommandState.class );

  private static IStridablesList<ESkCommandState> list = null;

  private final String  id;
  private final String  nmName;
  private final String  description;
  private final boolean finished;

  /**
   * Constructor.
   *
   * @param aId String - identifier (IDpath)
   * @param aName - short name
   * @param aDescription String - description
   * @param aIsFinished boolean - the command processing finshed flag
   */
  ESkCommandState( String aId, String aName, String aDescription, boolean aIsFinished ) {
    id = aId;
    nmName = aName;
    description = aDescription;
    finished = aIsFinished;
  }

  // --------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return nmName;
  }

  @Override
  public String description() {
    return description;
  }

  // ----------------------------------------------------------------------------------
  // Additional API
  //

  /**
   * Determines if command processing is finished.
   *
   * @return boolean - the command processing finshed flag
   */
  public boolean isComplete() {
    return finished;
  }

  /**
   * Returns all constants as list.
   *
   * @return {@link IStridablesList}&lt;{@link ESkCommandState}&gt; - list of all constants
   */
  public static IStridablesList<ESkCommandState> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  // ----------------------------------------------------------------------------------
  // Find and get
  //

  /**
   * Finds the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ESkCommandState} - found constant or <code>null</code> there is no constant with specified
   *         identifier
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ESkCommandState findById( String aId ) {
    return asList().findByKey( aId );
  }

  /**
   * Returns the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ESkCommandState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no constant with specified identifier
   */
  public static ESkCommandState getById( String aId ) {
    return asList().getByKey( aId );
  }

}
