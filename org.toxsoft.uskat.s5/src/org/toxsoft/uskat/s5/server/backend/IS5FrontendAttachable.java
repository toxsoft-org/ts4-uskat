package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Интерфейс-метка способности присоединять {@link ISkFrontendRear}
 *
 * @author mvk
 */
@Local
public interface IS5FrontendAttachable {

  /**
   * Возвращает список присоединенных frontend
   *
   * @return {@link IList}&lt;{@link IS5FrontendRear}&gt; список присоединенных frontend
   */
  IList<IS5FrontendRear> attachedFrontends();

  /**
   * Сообщает о подсоединении к нему очередного фронтенда.
   * <p>
   * Если фронтенд уже присоединен, то метод ничего не делает
   *
   * @param aFrontend {@link IS5FrontendRear} - подсоединяемый фронтенд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void attachFrontend( IS5FrontendRear aFrontend );

  /**
   * Отсоединяет ранее подсоединенный фронтенд.
   * <p>
   * Если фронтенд не был подсоединен, то метод ничего не делает.
   *
   * @param aFrontend {@link IS5FrontendRear} - отсоединяемый фронтенд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void detachFrontend( IS5FrontendRear aFrontend );

}
