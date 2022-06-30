package org.toxsoft.uskat.s5.server.sessions.pas;

import java.io.ObjectStreamException;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;

/**
 * Процессор очереди однотипных сообщений
 * <p>
 * Сообщения добавляются в очередь на обработку. В процессе обработки, данное сообщение (или сообщения в очереди) могут
 * быть изменены, заменены другими или пропущены(игнорированы).
 *
 * @author mvk
 */
public interface IS5MessageProcessor {

  /**
   * Пустая (нет связи с сервером) информация о сессии пользователя
   */
  IS5MessageProcessor NULL = new NullMessageProcessor();

  /**
   * Добавить новое сообщение на обработку.
   * <p>
   * Поступающие сообщения могут быть переформатированы, объединены или удалены(игнорированы)
   *
   * @param aMessage {@link GtMessage}
   * @return boolean <b>true</b> сообщение было обработано процессором;<b>false</b> процессор игнорировал сообщение.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean processMessage( GtMessage aMessage );

  /**
   * Возвращает сообщение из очереди обработанных сообщений или null если очередь пуста.
   *
   * @return &lt;{@link GtMessage}&gt; - следующее сообщение из очереди или null если очередь пуста
   */
  GtMessage getHeadOrNull();
}

/**
 * Реализация пустой сессии пользователя
 *
 * @author mvk
 */
class NullMessageProcessor
    implements IS5MessageProcessor {

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5MessageProcessor#NULL}.
   *
   * @return Object объект {@link IS5MessageProcessor#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5MessageProcessor.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5MessageProcessor
  //
  @Override
  public boolean processMessage( GtMessage aMessage ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public GtMessage getHeadOrNull() {
    return null;
  }
}
