package org.toxsoft.uskat.s5.server.backend.supports.commands;

import javax.ejb.Local;

import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.backend.api.IBaCommands;
import org.toxsoft.uskat.s5.server.backend.supports.commands.sequences.IS5CommandSequence;
import org.toxsoft.uskat.s5.server.sequences.IS5BackendSequenceSupportSingleton;

/**
 * Локальный интерфейс синглетона команд предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendCommandSingleton
    extends IBaCommands, IS5BackendSequenceSupportSingleton<IS5CommandSequence, IDtoCompletedCommand> {

  // /**
  // * Возвращает список поддерживаемых команд
  // *
  // * @return {@link IGwidList} список идентификаторов поддерживаемых команд
  // */
  // IGwidList getExecutableCommandGwids();
  //
  // /**
  // * Регистрация исполнителя указанных команд для указанного frontend
  // * <p>
  // * Идентификаторы команд определяются как {@link ISkCommandService#registerExecutor(ISkCommandExecutor, IGwidList)}
  // *
  // * @param aFrontend {@link IS5FrontendRear} frontend исполнителя
  // * @param aNeededGwids {@link IGwidList} список идентификаторов выполняемых команд
  // * @throws TsNullArgumentRtException аргумент = null
  // */
  // void setExecutableCommandGwids( IS5FrontendRear aFrontend, IGwidList aNeededGwids );
  //
  // /**
  // * Передать команду на выполнение.
  // * <p>
  // * Бекенд ожидает, что команда имеет состояние {@link ESkCommandState#SENDING}.
  // *
  // * @param aFrontend {@link IS5FrontendRear} frontend передающий команду на исполнение
  // * @param aCommand {@link IDtoCommand} команда на выполнение
  // * @throws TsNullArgumentRtException любой аргумент = null
  // */
  // void sendCommand( IS5FrontendRear aFrontend, IDtoCommand aCommand );
  //
  // /**
  // * Изменяет состояние команды на указанное.
  // *
  // * @param aFrontend {@link IS5FrontendRear} frontend исполняющий команду
  // * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} информация о новом состоянии команды
  // * @throws TsNullArgumentRtException любой аргумент = null
  // */
  // void changeCommandState( IS5FrontendRear aFrontend, DtoCommandStateChangeInfo aStateChangeInfo );
  //
  // /**
  // * Запрашивает завершенные команды за заданный период времени.
  // * <p>
  // * В списке интересующих команд допускаются только GWID-ы команд. Все элементы, у которых {@link Gwid#kind()} не
  // равно
  // * {@link EGwidKind#GW_CMD} молча игнорируются.
  // * <p>
  // * В списке могут быть как конкретные (с идентификатором объекта) {@link Gwid}-ы, так и абстрактные. Абстрактный
  // * {@link Gwid} означает запрос указанной команды ко всем объектам. Кроме того, в запросе могут присутствовать
  // * мулти-GWID-ы, у которых {@link Gwid#isMulti()} = <code>true</code>, что означает "все команды к запрошенному
  // * объекту". Получается, что абстрактный мули-GWID запрашивает все команды ко всем объектам класса
  // * {@link Gwid#classId()}.
  // *
  // * @param aInterval {@link IQueryInterval} - запрошенный интервал времени
  // * @param aNeededGwids {@link IGwidList} - список GWID-ов интересующих команд
  // * @return {@link ITimedList}&lt;{@link IDtoCompletedCommand}&gt; - список запрошенных событий
  // */
  // ITimedList<IDtoCompletedCommand> queryCommands( IQueryInterval aInterval, IGwidList aNeededGwids );
}
