package org.toxsoft.uskat.mnemo.gui.tmp_mws_plugin;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Интерфейс исполнителя внешнего действия.
 * <p>
 * В некоторых подсистемах tsgui требуется выполнить действие, которое запрашивается "изнутры" подсистемы. Этот
 * интерфейс используется дл обработки таких запросов.
 *
 * @author goga
 */
public interface ITsExternalActionHandler {

  /**
   * Реализация должна выполнить запрошенное действие и вернуть результат выполнения.
   * <p>
   * Интерпретация содержимого идентификатора запроса, аргументв и результата зависят от конкретного применения и должны
   * быть описаны в документации к нему.
   *
   * @param aSource Object - источник запроса
   * @param aActionId String - идентификатор (ИД-путь) запроса
   * @param aArgs {@link IOptionSet} - аргументы запроса
   * @return {@link IOptionSet} - результат выполнения запроса, не допускается null
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  IOptionSet handleExtenralAction( Object aSource, String aActionId, IOptionSet aArgs );

}
