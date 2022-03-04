package org.toxsoft.uskat.s5.server.backend.supports.lobs;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

import ru.uskat.legacy.IdPair;

/**
 * Поддержка сервера для функций доступа к большим объектам (Large OBject - LOB).
 *
 * @author mvk
 */
@Local
public interface IS5BackendLobsSingleton
    extends IS5BackendSupportSingleton {

  /**
   * Возвращает список идентификаторов lob-данных доступные в системе
   *
   * @return {@link IList}&lt;{@link IdPair}&gt; список идентификаторов
   */
  IList<IdPair> listLobIds();

  /**
   * Проверяет, существует ли в системе lob-данное с указанным идентификатором
   *
   * @param aId {@link IdPair} идентификатор данного
   * @return <b>true</b> данное существует. <b>fasle</b> данное не существует
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean hasLob( IdPair aId );

  /**
   * Читает значение lob-данного из системы
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @return String текстовое представление lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данное не существует в системе
   */
  String readClob( IdPair aId );

  /**
   * Сохраняет значение lob-данного в системе
   * <p>
   * Если в системе уже существует указанное данное, то его значение обновляется.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aValue String текстовое представление значения
   * @return boolean <b>true</b> новое значение;<b>false</b> значение было обновлено.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  boolean writeClob( IdPair aId, String aValue );

  /**
   * Копирует значение lob-данного в системе
   * <p>
   * Если в системе уже существует указанное данное, то его значение обновляется.
   *
   * @param aSourceId {@link IdPair} идентификатор исходного lob-данного
   * @param aDestId {@link IdPair} идентификатор приемного lob-данного
   * @return boolean <b>true</b> новое значение;<b>false</b> значение было обновлено.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данное не существует в системе
   */
  boolean copyClob( IdPair aSourceId, IdPair aDestId );

  /**
   * Удаляет lob-данное из системы
   * <p>
   * Если значения нет в системе, то ничего не делает.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @return <b>true</b> значение было удалено из системы;<b>false</b> значения не было в системе.
   */
  boolean removeClob( IdPair aId );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над большими данными (LOBs).
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5LobsInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addLobsInterceptor( IS5LobsInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над над большими данными (LOBs)
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5LobsInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeLobsInterceptor( IS5LobsInterceptor aInterceptor );
}
