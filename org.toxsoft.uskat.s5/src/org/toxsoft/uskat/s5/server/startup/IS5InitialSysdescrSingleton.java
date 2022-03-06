package org.toxsoft.uskat.s5.server.startup;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.server.IS5ImplementConstants;

/**
 * Начальное, проектно-зависимое, определение системного описания сервера
 * <p>
 * В системе (в конечном проекте) должна существовать одна реализация синглетона этого интерфейса с именем
 * {@link IS5ImplementConstants#PROJECT_INITIAL_SYSDESCR_SINGLETON}. Для упрощения реализации синглетона
 * {@link IS5ImplementConstants#PROJECT_INITIAL_SYSDESCR_SINGLETON} может быть использована абстрактная реализация
 * {@link S5InitialSysdescrSingleton}.
 *
 * @author mvk
 */
@Local
public interface IS5InitialSysdescrSingleton {
  // nop
}
