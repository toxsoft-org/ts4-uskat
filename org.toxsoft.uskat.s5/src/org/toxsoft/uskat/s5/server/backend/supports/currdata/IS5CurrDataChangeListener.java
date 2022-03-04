package org.toxsoft.uskat.s5.server.backend.supports.currdata;

import javax.ejb.Local;

import ru.uskat.core.api.rtdata.ISkCurrDataChangeListener;

/**
 * EJB-cлушатель текущих данных
 *
 * @author mvk
 */
@Local
public interface IS5CurrDataChangeListener
    extends ISkCurrDataChangeListener {
  // nop
}
