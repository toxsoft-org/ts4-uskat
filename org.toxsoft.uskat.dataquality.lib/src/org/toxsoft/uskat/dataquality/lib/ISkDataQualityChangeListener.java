package org.toxsoft.uskat.dataquality.lib;

import org.toxsoft.core.tslib.gw.gwid.IGwidList;

/**
 * Слушатель службы {@link ISkDataQualityService}.
 *
 * @author hazard157
 */
public interface ISkDataQualityChangeListener {

  /**
   * Вызывается при любом изменений в списке тикетов {@link ISkDataQualityService#listTickets()}.
   *
   * @param aSource {@link ISkDataQualityService} источник сообщения
   */
  default void onTicketsChanged( ISkDataQualityService aSource ) {
    // nop
  }

  /**
   * Вызывается при любом изменений МЕТОК в наборах отслеживаемых ресурсов (например, установлении или разрыве связи с
   * ресурсом методом {@link ISkDataQualityService#addConnectedResources(IGwidList)}).
   * <p>
   * Поскольку за одну операцию меняется пометка только одним тикетом, имеет смысл сообщить идентификатор этого тикета.
   * <p>
   * Специально не передается список изменившихся ресурсов - он может быть большим, и при этом часто совсем не нужным.
   *
   * @param aSource {@link ISkDataQualityService} источник сообщения
   * @param aTicketId String идентификатор тикета, пометка которым была изменена (ИД-путь)
   */
  default void onResourcesStateChanged( ISkDataQualityService aSource, String aTicketId ) {
    // nop
  }
}
