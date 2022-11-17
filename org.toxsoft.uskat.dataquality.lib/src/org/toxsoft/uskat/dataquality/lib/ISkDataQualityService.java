package org.toxsoft.uskat.dataquality.lib;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;

/**
 * Data quality support.
 *
 * @author hazard157
 */
public interface ISkDataQualityService
    extends ISkService {

  /**
   * The service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".DataQualities"; //$NON-NLS-1$

  /**
   * Идентификатор тикета(метки): "Нет связи с ресурсом"
   * <p>
   * Значения метки автоматически устанавливается в {@link AvUtils#AV_FALSE} при вызове
   * {@link #addConnectedResources(IGwidList)} для ресурсов передаваемых в параметрах метода.
   * <p>
   * Значения метки автоматически устанавливается в {@link AvUtils#AV_TRUE} при обнаружении завершения сессии ранее
   * переданной в параметрах {@link #addConnectedResources(IGwidList)} для ресурсов передаваемых в параметрах этого же
   * метода или для всех ресурсов сессии при вызове метода {@link #setConnectedResources( IGwidList)}.
   */
  String TICKET_ID_NO_CONNECTION = "NoConnection"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Отслеживание качества ресурсов
  //
  /**
   * Возвращает значения пометок ресурса тикетами.
   * <p>
   * В возвращаемом наборе содержатся значения <b>всех</b> пометок. Вообще-то, может возвращаться пустой набор, но по
   * соглашению {@link ISkDataQualityTicket}, в отсутствие пометки считается, что пометка имеет значение по умолчанию
   * тикета {@link ISkDataQualityTicket#defaultValue()}.
   * <p>
   * Рекомендуется использовать метод {@link ISkDataQualityTicket#getMarkValue(IOptionSet)} для извлечения значения из
   * набора.
   * <p>
   * {@link Gwid} ресурсов должны представлять данные объектов. Абстрактные ({@link Gwid#isAbstract() }== true, без
   * объекта(ов)) и групповые ({@link Gwid#isMulti()} == true, адресация нескольких данных) - НЕ допускаются. Примеры
   * возможных {@link Gwid}:
   * <ul>
   * <li>CtPot[potObj1]$rtdata( alive ).</li>
   * </ul>
   *
   * @param aResource {@link Gwid} запрашиваемый ресурс
   * @return {@link IOptionSet} значения пометок
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException запрет абстрактного {@link Gwid} - должен быть указан объект
   * @throws TsIllegalArgumentRtException запрет группового идентификатора (Gwid.isMulti() == true)
   * @throws TsIllegalArgumentRtException {@link Gwid} не представляют данное {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException {@link Gwid} несуществующего класса, объекта или данного
   */
  IOptionSet getResourceMarks( Gwid aResource );

  /**
   * Возвращает значения пометок для нескольких ресурсов сразу.
   * <p>
   * {@link Gwid} ресурсов должны представлять данные объектов. Абстрактные {@link Gwid#isAbstract()} (без объекта(ов))
   * не допускаются.
   * <p>
   * ДОПУСКАЮТСЯ групповые ({@link Gwid#isMulti()} == true, адресация нескольких данных) идентификаторы.
   * <p>
   * Примеры возможных {@link Gwid}:
   * <ul>
   * <li>CtPot[potObj1]$rtdata( alive ).</li>
   * <li>CtPot[potObj1]$rtdata( * ).</li>
   * <li>CtPot[*]$rtdata( alive ).</li>
   * <li>CtPot[*]$rtdata( * ).</li>
   * </ul>
   *
   * @param aResources {@link IGwidList} список запрашиваемых ресурсов
   * @return IMap&lt;{@link Gwid},{@link IOptionSet}&gt; карта "ресурс" - "значения пометок"
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException запрет абстрактных {@link Gwid} - должен быть указан объект или объекты(*)
   * @throws TsIllegalArgumentRtException {@link Gwid} не представляют данное {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException {@link Gwid} несуществующего класса, объекта или данного
   */
  IMap<Gwid, IOptionSet> getResourcesMarks( IGwidList aResources );

  /**
   * Возвращает ресурсы по запрошенному фильтру.
   * <p>
   * TODO следует разработать набор фильтров: 1) наличие тикета, 2) критерий над значением пометки
   *
   * @param aFilterParams {@link ITsCombiFilterParams} фильтр запроса
   * @return IMap&lt;{@link Gwid},{@link IOptionSet}&gt; состояние ресурсов, удовлетворяющих запросу
   */
  IMap<Gwid, IOptionSet> queryMarkedUgwies( ITsCombiFilterParams aFilterParams );

  /**
   * Возвращает список ресурсов за поставку которых отвечает клиент службы.
   * <p>
   * {@link Gwid} ресурсов представляют данные объектов. Примеры возможных {@link Gwid}:
   * <ul>
   * <li>CtPot[potObj1]$rtdata( alive ).</li>
   * <li>CtPot[potObj1]$rtdata( * ).</li>
   * <li>CtPot[*]$rtdata( alive ).</li>
   * <li>CtPot[*]$rtdata( * ).</li>
   * </ul>
   *
   * @return {@link IGwidList} список ресурсов предоставляемых сессией
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException запрет абстрактных {@link Gwid} - должен быть указан объект или объекты(*)
   * @throws TsIllegalArgumentRtException {@link Gwid} не представляют данное {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException {@link Gwid} несуществующего класса, объекта или данного
   */
  IGwidList getConnectedResources();

  /**
   * Извещает службу о том, что за поставку ресурсов отвечает клиент службы.
   * <p>
   * Метод {@link #addConnectedResources(IGwidList)} может вызываться несколько раз. В этом случае, ресурсы которые уже
   * зарегистрированы молча игнорируются.
   * <p>
   * Метод {@link #addConnectedResources(IGwidList)} устанавливает метку
   * {@link ISkDataQualityService#TICKET_ID_NO_CONNECTION} со значением <code>false</code> для всех ресурсов за поставку
   * которых отвечает клиент.
   * <p>
   * Поробнее о работе тикета смотри комментарий к {@link ISkDataQualityService#TICKET_ID_NO_CONNECTION}.
   * <p>
   * TODO что делать, если заявлленный здесь ресурс уже управляется другим клиентом службы ??? И тут еще и два варианта:
   * повторное подключение ТОГО ЕЖ пользователя, и попытка взять под себя ресурс другим пользователем. ЧТО ДЕЛАТЬ-ТО???
   * <p>
   * {@link Gwid} ресурсов должны представлять данные объектов. Абстрактные {@link Gwid} (без объекта(ов)) не
   * допускаются. Примеры возможных {@link Gwid}:
   * <ul>
   * <li>CtPot[potObj1]$rtdata( alive ).</li>
   * <li>CtPot[potObj1]$rtdata( * ).</li>
   * <li>CtPot[*]$rtdata( alive ).</li>
   * <li>CtPot[*]$rtdata( * ).</li>
   * </ul>
   *
   * @param aResources {@link IGwidList} список ресурсов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException запрет абстрактных {@link Gwid} - должен быть указан объект или объекты(*)
   * @throws TsIllegalArgumentRtException {@link Gwid} не представляют данное {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException {@link Gwid} несуществующего класса, объекта или данного
   */
  void addConnectedResources( IGwidList aResources );

  /**
   * Извещает службу о том, что за поставку указанных ресурсов клиент больше не отвечает.
   * <p>
   * Метод {@link #removeConnectedResources(IGwidList)} может вызываться несколько раз. В этом случае, ресурсы которые
   * уже не зарегистрированы - молча игнорируются.
   * <p>
   * Необходимо учитывать, что дерегистрация ресурсов производится по идентификатору {@link Gwid} без его
   * дополнительного анализа (групповой, негрупповой). Например, если через вызов метода
   * {@link #addConnectedResources(IGwidList)} были зарегистрированы ресурсы: <br>
   * <code>   box[one]$rtdata(online), box[two]$rtdata(online), box[three]$rtdata(online)</code><br>
   * то, попытка их удаления через групповые идентификаторы, например, <br>
   * <code>   box[*]$rtdata(online) или box[three]$rtdata(*) или box[*]$rtdata(*) </code><br>
   * - будет проигнорирована. Для удаления необходимо указать те же идентификаторы, что и при вызове
   * {@link #addConnectedResources(IGwidList)} или воспользователься {@link #setConnectedResources(IGwidList)}.
   * <p>
   * Метод {@link #removeConnectedResources(IGwidList)} устанавливает метку
   * {@link ISkDataQualityService#TICKET_ID_NO_CONNECTION} со значением <code>true</code> для дерегистрируемых ресурсов
   * клиента.
   * <p>
   * Поробнее о работе тикета смотри комментарий к {@link ISkDataQualityService#TICKET_ID_NO_CONNECTION}.
   * <p>
   * TODO что делать, если заявлленный здесь ресурс уже управляется другим клиентом ??? И тут еще и два варианта:
   * повторное подключение ТОГО ЕЖ пользователя, и попытка взять под себя ресурс другим пользователем. ЧТО ДЕЛАТЬ-ТО???
   * <p>
   * {@link Gwid} ресурсов должны представлять данные объектов. Абстрактные {@link Gwid} (без объекта(ов)) не
   * допускаются. Примеры возможных {@link Gwid}:
   * <ul>
   * <li>CtPot[potObj1]$rtdata( alive ).</li>
   * <li>CtPot[potObj1]$rtdata( * ).</li>
   * <li>CtPot[*]$rtdata( alive ).</li>
   * <li>CtPot[*]$rtdata( * ).</li>
   * </ul>
   *
   * @param aResources {@link IGwidList} список ресурсов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException запрет абстрактных {@link Gwid} - должен быть указан объект или объекты(*)
   * @throws TsIllegalArgumentRtException {@link Gwid} не представляют данное {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException {@link Gwid} несуществующего класса, объекта или данного
   */
  void removeConnectedResources( IGwidList aResources );

  /**
   * Извещает службу о том, что необходимо заменить список ресурсов за которые отвечает клиент.
   * <p>
   * Метод {@link #setConnectedResources(IGwidList)} устанавливает метку
   * {@link ISkDataQualityService#TICKET_ID_NO_CONNECTION} со значением <code>true</code> для ресурсов клиента которые
   * были зарегистрированны ранее и значением <code>false</code> для вновь зарегистированных ресурсов.
   *
   * @param aResources {@link IGwidList} список ресурсов
   * @return {@link IGwidList} список ресурсов которые ранее предоставлялись сессией
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException запрет абстрактных {@link Gwid} - должен быть указан объект или объекты(*)
   * @throws TsIllegalArgumentRtException {@link Gwid} не представляют данное {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException {@link Gwid} несуществующего класса, объекта или данного
   */
  IGwidList setConnectedResources( IGwidList aResources );

  /**
   * Задает значения пометок пользовательскими (не встроенными) ярлыками.
   * <p>
   * Если среди aResources есть ресурсы, не присутствующие в списке отслеживаемых (регистрацию ресурсов смотри
   * {@link #addConnectedResources(IGwidList)}), то они молча игнорируются.
   * <p>
   * {@link Gwid} ресурсов должны представлять данные объектов. Абстрактные {@link Gwid} (без объекта(ов)) не
   * допускаются. Примеры возможных {@link Gwid}:
   * <ul>
   * <li>CtPot[potObj1]$rtdata( alive ).</li>
   * <li>CtPot[potObj1]$rtdata( * ).</li>
   * <li>CtPot[*]$rtdata( alive ).</li>
   * <li>CtPot[*]$rtdata( * ).</li>
   * </ul>
   *
   * @param aTicketId String идентификатор тикета (ИД-путь)
   * @param aValue {@link IAtomicValue} значение тикета
   * @param aResources {@link IGwidList} список ресурсов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException идентификатор тикета не ИД-путь
   * @throws TsItemNotFoundRtException нет такого тикета
   * @throws TsIllegalArgumentRtException такет является встроенным
   * @throws AvTypeCastRtException значение имеет тип, несовместимый с заявленным в
   *           {@link ISkDataQualityTicket#dataType()}
   * @throws TsIllegalArgumentRtException запрет абстрактных {@link Gwid} - должен быть указан объект или объекты(*)
   * @throws TsIllegalArgumentRtException {@link Gwid} не представляют данное {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException {@link Gwid} несуществующего класса, объекта или данного
   */
  void setMarkValue( String aTicketId, IAtomicValue aValue, IGwidList aResources );

  // ------------------------------------------------------------------------------------
  // Управление тикетами
  //

  /**
   * Возвращает перечень всех тикетов в системе.
   * <p>
   * Перечень является редактируемым, но персистентным. То есть, сохраняется между перезапусками системы.
   *
   * @return {@link IStridablesList}&lt;{@link ISkDataQualityTicket}&gt; список всех тикетов
   */
  IStridablesList<ISkDataQualityTicket> listTickets();

  /**
   * Создает новый или редактирует существующий тикет.
   * <p>
   * Нельзя редактировать встроенные тикеты, у которых {@link ISkDataQualityTicket#isBuiltin()} - <code>true</code>.
   *
   * @param aTicketId String - идентификатор
   * @param aName String - название
   * @param aDescription String - описание
   * @param aDataType {@link IDataType} тип данных
   * @return {@link ISkDataQualityTicket} созданный или отредактированный пользовательский (не встроенный) тикет
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException попытка редактирования втроенного тикета
   * @throws TsIllegalArgumentRtException у именнованного типа нет значения по умолчанию
   */
  ISkDataQualityTicket defineTicket( String aTicketId, String aName, String aDescription, IDataType aDataType );

  /**
   * Удаляет ранее созданный ярлык.
   * <p>
   * Нельзя удалять встроенные тикеты, у которых {@link ISkDataQualityTicket#isBuiltin()} - <code>true</code>.
   *
   * @param aTicketId String идентификатор удаляемого тикета (ИД-путь)
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException идентификатор тикета не ИД-путь
   * @throws TsItemNotFoundRtException нет такого тикета
   * @throws TsIllegalStateRtException попытка удаления встроенного тикета
   */
  void removeTicket( String aTicketId );

  // ------------------------------------------------------------------------------------
  // Service support
  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkDataQualityChangeListener}&gt; - the service eventer
   */
  ITsEventer<ISkDataQualityChangeListener> eventer();

}
