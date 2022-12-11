package org.toxsoft.uskat.alarms.lib;

import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.filters.*;
import org.toxsoft.uskat.alarms.lib.impl.SkAlarmUtils;

/**
 * Интерфейс поддержки работы со слушателями службы тревог.
 * <p>
 * Интерфейс публикует поддержку слушателей для клиентов служб, а реализациям служб следует расширить
 * {@link AbstractTsEventer} и воспользоваться дополнительным API класса.
 *
 * @author mvk
 */
public interface ISkAlarmEventer
    extends ITsEventer<ISkAlarmServiceListener> {

  /**
   * Добавляет слушатель.
   * <p>
   * Вообще говоря, один и тот же слушатель можно зарегистрировать несколько раз, с разным набором тревог. Но это не
   * рекомендуется, поскольку при удалении методом {@link #removeListener(ISkAlarmServiceListener)} будут удалены все
   * вхождения этого слушателя.
   * <p>
   * Аргумент aFilter определяется параметрами единичных фильтров фильтрами тревог:
   * <ul>
   * <li>{@link SkAlarmFilterByAuthor};</li>
   * <li>{@link SkAlarmFilterByDefId};</li>
   * <li>{@link SkAlarmFilterByLevel};</li>
   * <li>{@link SkAlarmFilterByMessage};</li>
   * <li>{@link SkAlarmFilterByPriority};</li>
   * <li>{@link SkAlarmFilterByHistory};</li>
   * <li>{@link SkAlarmFilterByTimestamp}.</li>
   * </ul>
   * Для упрощения можно использовать утилитные методы создания параметров фильтра из {@link SkAlarmUtils}. *
   *
   * @param aListener &lt;L&gt; - слушатель
   * @param aFilter {@link ITsCombiFilterParams} фильтр определяющий тревоги получаемые слушателем.
   *          {@link ITsCombiFilterParams#ALL} - все тревоги
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addListener( ISkAlarmServiceListener aListener, ITsCombiFilterParams aFilter );
}
