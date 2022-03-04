package org.toxsoft.uskat.s5.server.sessions;

import static org.toxsoft.uskat.s5.server.sessions.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Ресурс сессии
 * <p>
 * Абстрактный ресурс сессии реализован в виде класса, чтобы избежать неверной реализации {@link ICloseable} сессиями
 * служб которые могут оказаться невалидными (контейнер вывел бин сессии в пул). Наследники {@link S5SessionResource}
 * должны определять логику освобождения ресурса сессии
 * <p>
 * Ресурс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный (в сессии) идентификатор ресурса(ИД-путь);</li>
 * <li><b>description</b>() - удобочитаемое описание ресурса. Может быть пустым;</li>
 * </ul>
 *
 * @author mvk
 */
public abstract class S5SessionResource
    extends Stridable
    implements ICloseable, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор
   *
   * @param aId String идентификатор ресурса (ИД-путь)
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  protected S5SessionResource( String aId ) {
    // true - разрешен ИД-путь
    super( aId, TsLibUtils.EMPTY_STRING, TsLibUtils.EMPTY_STRING );
  }

  /**
   * Конструктор
   *
   * @param aId String идентификатор ресурса (ИД-путь)
   * @param aName String имя ресурса
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  protected S5SessionResource( String aId, String aName ) {
    // true - разрешен ИД-путь
    super( aId, aName, TsLibUtils.EMPTY_STRING );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICloseable
  //
  /**
   * Наследники должны определять логику освобождения ресурса сессии
   */
  @Override
  public abstract void close();

  // ------------------------------------------------------------------------------------
  // Переопределение Object
  //
  @Override
  public String toString() {
    return String.format( RESOURCE_TOSTRING_FORMAT, getClass().getSimpleName(), super.toString() );
  }

}
