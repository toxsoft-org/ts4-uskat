package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

import javax.ejb.Local;

import org.toxsoft.uskat.core.api.rtdserv.ISkCurrDataChangeListener;

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
