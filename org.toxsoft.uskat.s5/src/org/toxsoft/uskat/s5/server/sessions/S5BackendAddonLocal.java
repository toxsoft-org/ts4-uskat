package org.toxsoft.uskat.s5.server.sessions;

import static org.toxsoft.core.log4j.LoggerWrapper.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.local.S5LocalBackend;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

import ru.uskat.core.api.ISkBackend;

/**
 * Абстрактная реализация локального доступа к расширению supports
 *
 * @author mvk
 */
public abstract class S5BackendAddonLocal
    extends Stridable
    implements ICloseable, ICooperativeMultiTaskable, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Локальный бекенд в рамках которого работает расширение
   */
  private S5LocalBackend owner;

  /**
   * Подджержка бекенда предоставляемая сервером
   */
  private IS5BackendCoreSingleton supports;

  /**
   * Журнал работы
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #logger()}
   */
  private transient ILogger logger;

  /**
   * Конструктор для наследников.
   *
   * @param aId String идентификатор расширения
   * @param aName String имя расширения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5BackendAddonLocal( String aId, String aName ) {
    // true: разрешен IDpath
    super( aId, aName, TsLibUtils.EMPTY_STRING );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Конструктор
   *
   * @param aOwner {@link S5LocalBackend} локальный бекенд в рамках которого работает расширение
   * @param aSupports {@link IS5BackendCoreSingleton} поддержка сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  public final void init( S5LocalBackend aOwner, IS5BackendCoreSingleton aSupports ) {
    TsNullArgumentRtException.checkNulls( aOwner, aSupports );
    owner = aOwner;
    supports = aSupports;
    doAfterInit( aOwner, aSupports );
  }

  // ------------------------------------------------------------------------------------
  // IClosable
  //
  @Override
  public final void close() {
    doBeforeClose();
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает поддержку бекенда предоставляемую сервером
   *
   * @return {@link IS5BackendCoreSingleton} поддержка сервера
   */
  protected final IS5BackendCoreSingleton supports() {
    return supports;
  }

  /**
   * Возвращает бекенд предоставляемый сервером
   *
   * @return {@link IS5BackendCoreSingleton} бекенд
   */
  protected final ISkBackend backend() {
    return owner;
  }

  /**
   * Возвращает фронтенд в рамках которого работает расширение
   *
   * @return {@link IS5FrontendRear} фронтенд
   */
  protected final IS5FrontendRear frontend() {
    return owner;
  }

  /**
   * Возвращает общий журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    if( logger == null ) {
      logger = getLogger( getClass() );
    }
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Вызывается в конце метода {@link #init(S5LocalBackend, IS5BackendCoreSingleton)} .
   * <p>
   * Выброшенные методом исключения передаются сессии supports, что приводит к провалу установления связи с сервером.
   *
   * @param aOwner {@link S5LocalBackend} локальный бекенд в рамках которого работает расширение
   * @param aBackend {@link IS5BackendCoreSingleton} бекенд сервера
   */
  protected void doAfterInit( S5LocalBackend aOwner, IS5BackendCoreSingleton aBackend ) {
    // nop
  }

  /**
   * Вызывается из метода {@link #close()} перед началом процедуры отключения сессии (разрыва связи).
   * <p>
   * Выброшенные методом исключения игнорируются.
   */
  protected void doBeforeClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

}
