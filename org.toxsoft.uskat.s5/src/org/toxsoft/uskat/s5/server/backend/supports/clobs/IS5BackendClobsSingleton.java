package org.toxsoft.uskat.s5.server.backend.supports.clobs;

import javax.ejb.Local;

import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Поддержка сервера для функций доступа к большим объектам (Large OBject - LOB).
 *
 * @author mvk
 */
@Local
public interface IS5BackendClobsSingleton
    extends IS5BackendSupportSingleton {

  /**
   * Читает значение clob-данного из системы
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @return String текстовое представление lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данное не существует в системе
   */
  String readClob( Gwid aGwid );

  /**
   * Сохраняет значение clob-данного в системе
   * <p>
   * Если в системе уже существует указанное данное, то его значение обновляется.
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aValue String текстовое представление значения
   * @return boolean <b>true</b> новое значение;<b>false</b> значение было обновлено.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  boolean writeClob( Gwid aGwid, String aValue );

  /**
   * Копирует значение lob-данного в системе
   * <p>
   * Если в системе уже существует указанное данное, то его значение обновляется.
   *
   * @param aSourceGwid {@link Gwid} идентификатор исходного конкретного clob-данного
   * @param aDestGwid {@link Gwid} идентификатор приемного конкретного clob-данного
   * @return boolean <b>true</b> новое значение;<b>false</b> значение было обновлено.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данное не существует в системе
   */
  boolean copyClob( Gwid aSourceGwid, Gwid aDestGwid );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над большими данными (LOBs).
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5ClobsInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addLobsInterceptor( IS5ClobsInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над над большими данными (LOBs)
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5ClobsInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeLobsInterceptor( IS5ClobsInterceptor aInterceptor );
}
