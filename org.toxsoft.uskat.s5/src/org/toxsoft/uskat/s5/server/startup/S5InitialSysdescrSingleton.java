package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.s5.server.startup.IS5Resources.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.s5.client.local.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.singletons.*;

/**
 * Реализация синглтона {@link IS5InitialSysdescrSingleton}.
 * <p>
 * TODO: Сделать оценку состояния БД и, при необходимости, загрузку через {@link ISkConnection} с бекендом чтения файла
 *
 * @author mvk
 */
public abstract class S5InitialSysdescrSingleton
    extends S5SingletonBase
    implements IS5InitialSysdescrSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String PROJECT_INITIAL_SYSDESCR_ID = "ProjectInitialSysdescrSingleton"; //$NON-NLS-1$

  /**
   * Ядро сервера
   */
  @EJB
  private IS5BackendCoreSingleton backendCore;

  /**
   * Поставщик локальных соединений с сервером
   */
  @EJB
  private IS5LocalConnectionSingleton localConnectionSingleton;

  /**
   * Конструктор.
   */
  protected S5InitialSysdescrSingleton() {
    super( PROJECT_INITIAL_SYSDESCR_ID, STR_D_PROJECT_INITIAL_SYSDESCR );
  }

  // ------------------------------------------------------------------------------------
  // Переопределение S5SingletonBase
  //
  @Override
  protected void doInit() {
    // Подключение к серверу для проверки/создания системного описания
    ISkConnection connection = localConnectionSingleton.open( id() );
    // Проверка (и если необходимо создание) системного описания
    checkSysdescr( connection.coreApi() );
    // Завершение соединения. представляет pure (без расширения skf-функциональностью) и не может полноценно
    // использовано в дальнейшем
    connection.close();
  }

  @Override
  protected void doClose() {
    super.doClose();
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Проверка и если необходимо создание системного описания сервера (классы и объекты)
   *
   * @param aCoreApi {@link ISkCoreApi} API сервера
   */
  protected void doCreateSysdescr( ISkCoreApi aCoreApi ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Проверка и если необходимо создание системного описания сервера
   *
   * @param aCoreApi {@link ISkCoreApi} API сервера
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void checkSysdescr( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    // Создание проектного sysdescr
    doCreateSysdescr( aCoreApi );
  }
}
