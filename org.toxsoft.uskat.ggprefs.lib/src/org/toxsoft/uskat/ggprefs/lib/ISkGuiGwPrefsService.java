package org.toxsoft.uskat.ggprefs.lib;

import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;

/**
 * Служба поддержки настроек для GUI приложения, связанные с предметной областью.
 *
 * @author goga
 */
public interface ISkGuiGwPrefsService
    extends ISkService {

  /**
   * The service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_SYSEXT_SERVICE_ID_PREFIX + ".GuiGwPreferences"; //$NON-NLS-1$

  /**
   * Возвращает описание всех известных разделов.
   * <p>
   * Список описаний хранится персистентно.
   *
   * @return {@link IStridablesList}&lt;{@link IDpuGuiGwPrefsSectionDef}&gt; - список описаний разделов
   */
  IStridablesList<IDpuGuiGwPrefsSectionDef> listSections();

  /**
   * Создает новый или редактирует сущсетвущий.
   * <p>
   * Все параметры аргумента {@link IDpuGuiGwPrefsSectionDef#params()} сохраняются в сервере. Сервис
   * {@link ISkGuiGwPrefsService} параметры не использует, но GUI часть программы может использовать такие параметры
   * описания раздела как {@link IAvMetaConstants#TSID_NAME} (то есть {@link IDpuGuiGwPrefsSectionDef#nmName()}),
   * {@link IAvMetaConstants#TSID_DESCRIPTION} (то есть {@link IDpuGuiGwPrefsSectionDef#description()}),
   * {@link IAvMetaConstants#TSID_ICON_ID} и другие, на усмотрение разработчика.
   *
   * @param aSectionDef {@link IDpuGuiGwPrefsSectionDef} - описание раздела
   * @return {@link IGuiGwPrefsSection} - созданный раздел
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IGuiGwPrefsSection defineSection( IDpuGuiGwPrefsSectionDef aSectionDef );

  /**
   * Возвращает раздел по идентификатору.
   *
   * @param aSectionId String - идентификатор раздела
   * @return {@link IGuiGwPrefsSection} - разел настроек
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого раздела
   */
  IGuiGwPrefsSection getSection( String aSectionId );

  /**
   * Удаляет раздел со всеми сохраненными значениями.
   * <p>
   * Внимание: нельза удалить раздел, который уже открыт методом {@link #defineSection(IDpuGuiGwPrefsSectionDef)} или
   * {@link #getSection(String)}.
   * <p>
   * Если такой раздел не существует, метод ничего не делает.
   *
   * @param aSectionId String - идентификатор удаляемого раздела
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException раздел открыт, нельзя удалять
   */
  void removeSection( String aSectionId );

}
