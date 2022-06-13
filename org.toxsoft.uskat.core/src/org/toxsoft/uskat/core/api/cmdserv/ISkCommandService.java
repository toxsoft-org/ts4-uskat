package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Core service: command sending and processing support.
 *
 * @author hazard157
 */
public interface ISkCommandService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Commands"; //$NON-NLS-1$

  /**
   * Отправляет команду по назначению.
   *
   * @param aCmdGwid {@link Gwid} - конкретный {@link Gwid} объект-назначение и идентификатор команды
   * @param aAuthorSkid {@link Gwid} - конкретный {@link Gwid} автора команды
   * @param aArgs {@link IOptionSet} - значения аргументов команды
   * @return {@link ISkCommand} - созданная команда с уникальным идентификатором экземпляра {@link ISkCommand#id()}
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException неправльный {@link Gwid} команды или автора
   * @throws TsValidationFailedRtException неверные аргументы (неполные, или неверный тип и т.п.)
   */
  ISkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs );

  /**
   * Регистрирует исполнителя команды.
   * <p>
   * Повторная регистрация <b>заменяет</b> сущнствующий список выполняемых команд новым.
   * <p>
   * В списке выполняемых команд могут быть только {@link Gwid}-ы следующих типов:
   * <ul>
   * <li>абстрактный {@link EGwidKind#GW_CLASS} - все команды всех объектов указанного класса (и наследников);</li>
   * <li>конкретный {@link EGwidKind#GW_CLASS} - все команды указанного объекта;</li>
   * <li>абстрактный {@link EGwidKind#GW_CMD} - указанная команда всех объектов указанного класса (и наследников);</li>
   * <li>конкретный {@link EGwidKind#GW_CMD} - указанная команда указанного объекта.</li>
   * </ul>
   * Все другие типы {@link Gwid}-ов игнорируются без выбрасывания исключений.
   * <p>
   * Исключение выбрасывается, если какой-либо из запрошенных команд уже имеет зарегистрированного исполнителя. В таком
   * случае, исполнитель не регистрируется ни для одной команды.
   *
   * @param aExecutor {@link ISkCommandExecutor} - регистрируемый исполнитель
   * @param aCmdGwids {@link IGwidList} - список GWID-ов выполняемых команд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemAlreadyExistsRtException какой-либо из запрошенных команд конкретного объекта уже имеет исполнителя
   */
  void registerExecutor( ISkCommandExecutor aExecutor, IGwidList aCmdGwids );

  /**
   * Возвращает все {@link Gwid}-ы, для которых этот экземпляр сервиса является исполнителем команд.
   * <p>
   * Возвращаемы список это "свернутый" список {@link Gwid}-ов, сформированный суммой всех вызовов
   * {@link #registerExecutor(ISkCommandExecutor, IGwidList)} и {@link #unregisterExecutor(ISkCommandExecutor)}, и в
   * общем случае меняется при вызове перечисленных методов. При изменениях генерируется сообщение
   * {@link ISkCommandServiceListener#onExecutableCommandGwidsChanged(IGwidList)}.
   *
   * @return {@link IGwidList} - список GWID-ов выполняемых команд
   */
  IGwidList getExcutableCommandGwids();

  /**
   * Удаляет зарегистрированный исполнитель.
   * <p>
   * Если исполнитель не был зарегистрирован, метод ничего не делает.
   *
   * @param aExecutor {@link ISkCommandExecutor} - удаляемый исполнитель
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void unregisterExecutor( ISkCommandExecutor aExecutor );

  /**
   * Изменяет состояние обработки команды.
   * <p>
   * Вместе с новым состоянием могут быть указана дополнительная информация, например причина
   * {@link SkCommandState#OP_REASON}, {@link Skid} автора изменения {@link SkCommandState#OP_AUTHOR} и/или любые
   * другие, приложение-специфичные данные.
   * <p>
   * Этот интерфейс предназначен для использования исполнителем команды. Соответственно, он может изменить состояние
   * только на следующие:
   * <ul>
   * <li>{@link ESkCommandState#EXECUTING} - исполнение команды продолжается, дополнительная информация содержится в
   * {@link SkCommandState#params()}. Это состояние может передаваться сколько угодно раз, но только до окончания
   * обработки;</li>
   * <li>{@link ESkCommandState#FAILED} - исполнение команды было начато, но закончилось неудачей;</li>
   * <li>{@link ESkCommandState#SUCCESS} - команда успешно выполнениа.</li>
   * </ul>
   *
   * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} - информация о смене состояния команды
   * @throws TsNullArgumentRtException любой аргумент null
   * @throws TsIllegalArgumentRtException такая команда не находится на исполнении
   * @throws TsIllegalArgumentRtException недопустимое состояние команды
   */
  void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo );

  /**
   * Returns the stored completed commands history.
   *
   * @return {@link ITemporalsHistory} - the commands history
   */
  ITemporalsHistory<ISkCommand> history();

  /**
   * Возвращает средство работы с событиями от службы.
   *
   * @return {@link ITsEventer} - средство работы с событиями от службы
   */
  ITsEventer<ISkCommandServiceListener> eventer();

}
