package org.toxsoft.uskat.legacy.plugins;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Компонента управления подключаемыми модулами (плагинами).
 * <p>
 * Компонента отслеживает перечень плагинов заданного типа ( {@link IPluginManagerOps#PLUGIN_TYPE_ID}) в директориях
 * размещения aDirs (метода {@link #init(IOptionSet, Object...)}). Каждые {@link IPluginManagerOps#DIR_CHECK_INTERVAL}
 * миллисекунд компонента проверят наличие изменений в составе и версиях файлов в заданных директориях, и обнаружив
 * изменения, вызывает слушателя, заданного методом {@link #setPluginsChangeListener(IPluginsChangeListener)}. Кроме
 * того, поддерживает в актуальном состоянии список найденных плагинов (метод {@link #listPlugins()}).
 * <p>
 * <p>
 * Компонента должна быть инициализирована до и завершена после использовани (реализует интерфейс {@link IInitializable}
 * . Кроме того, она выполняет работу в режиме кооперативной многозадачности ({@link ICooperativeMultiTaskable}), то
 * есть, не запускат свои поток выполнения. Надо не забыть, достаточно часто вызывать {@link #doJob()}.
 * <p>
 * Внимание! при большом количестве файлов плагинов в директориях, периодические проверки могут достаточно долго
 * зедарживаться в методе {@link #doJob()}.
 * <p>
 * Реализация не является потоко-безопасной.
 *
 * @author goga
 */
public interface IPluginManagerComponent
    extends IInitializable, ICooperativeMultiTaskable {

  /**
   * Возвращает идентификатор типа подключаемого модуля (плагина).
   * <p>
   * Пустая строка (то есть, строка нулевой длины) означает, что обрабатываюся все типы плагинов в заданных директориях.
   *
   * @return String - идентификатор типа плагинов или пустая строка
   * @throws TsIllegalStateRtException компонента не инициализирована
   */
  String pluginTypeId();

  /**
   * Возвращает текущий перечень плагинов, находящихся в заданных директориях.
   * <p>
   * Внимание! Этот метод НЕ производит повторное сканирование директории, а использует список модулей, сформированный
   * во время последней проверки директорий.
   *
   * @return IList&lt;IPluginInfo&gt; - список всех плагинов
   * @throws TsIllegalStateRtException компонента не инициализирована
   */
  IList<IPluginInfo> listPlugins();

  /**
   * Создать экземпляр класса подключаемого модуля.
   * <p>
   * Создает класс вызовом class.forName() и использованием корректного загрузчика классов из JAR-файла модуля.
   * Производит проверку зависимостей, и если они не разрешимы, выбрасывает исключение.
   *
   * @param aPluginId - идентификатор плагина
   * @return Object - созданный класс (экземпляр) плагина
   * @throws TsIllegalStateRtException компонента не инициализирована
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException нет плагина с таким идентификатором
   * @throws TsIoRtException ошибка работы с файлом плагина
   * @throws TsItemNotFoundRtException нельзя разрешить зависимости или отсутствет файл класса в JAR-файле модуля
   */
  Object createPluginInstance( String aPluginId );

  /**
   * Задает слушателя изменений в файлах плагинов.
   * <p>
   * Для отмены прослушки, следует в качестве аргумента указать {@link IPluginsChangeListener#NULL}.
   *
   * @param aChangeListener IPluginsChangeListener - слушатель изменений в плагинах
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setPluginsChangeListener( IPluginsChangeListener aChangeListener );

  /**
   * Метод интерфейса {@link IInitializable}.
   * <p>
   * Добавляемые директории aDirs включаются <b>без</b> подкаталогов.
   * <p>
   * После вызова этого метода {@link #listPlugins()} возвращает актуальный список плагинов.
   *
   * @param aOps {@link IOptionSet} - параметры из перечня {@link IPluginManagerOps}
   * @param aDirs Object[] - (должны быть File[]) директории расположения файлов плагинов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException должен быть указан хотя бы один каталог плагинов
   * @throws TsIllegalArgumentRtException в aDirs есть не-File объекты
   * @throws TsIllegalArgumentRtException в aOps не хватает необходимой опции
   */
  @Override
  void init( IOptionSet aOps, Object... aDirs );

}
