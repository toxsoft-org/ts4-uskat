package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.s5.server.startup.IS5Resources.*;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;
import org.toxsoft.uskat.s5.utils.S5ValobjUtils;

/**
 * Реализация синглтона {@link IS5InitialImplementSingleton}.
 *
 * @author mvk
 */
public abstract class S5InitialImplementSingleton
    extends S5SingletonBase
    implements IS5InitialImplementSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String PROJECT_INITIAL_IMPLEMENT_ID = "ProjectInitialImplementSingleton"; //$NON-NLS-1$

  /**
   * Проектное описание реализации
   */
  private final IS5InitialImplementation implementation;

  /**
   * Статическая инициализация
   */
  static {
    // Регистрация s5-хранителей
    S5ValobjUtils.registerS5Keepers();
  }

  /**
   * Конструктор.
   *
   * @param aProjectImplementation {@link IS5InitialImplementation} проектное описание реализации
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException невалидный ИД-путь
   */
  protected S5InitialImplementSingleton( IS5InitialImplementation aProjectImplementation ) {
    super( PROJECT_INITIAL_IMPLEMENT_ID, STR_D_PROJECT_INITIAL_IMPLEMENT );
    implementation = TsNullArgumentRtException.checkNull( aProjectImplementation );
  }

  // ------------------------------------------------------------------------------------
  // Переопределение S5SingletonBase
  //
  @Override
  protected void doInit() {
    // nop
  }

  @Override
  protected void doClose() {
    super.doClose();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5InitialImplementSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IS5InitialImplementation impl() {
    return implementation;
  }
}
