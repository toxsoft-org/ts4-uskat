package org.toxsoft.uskat.s5.server.backend.supports.links;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkRev;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций (создание, удаление, изменение связей) между s5-объектами системы.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5LinksInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5LinksInterceptor} должны быть иметь аннатоцию: &#064;TransactionAttribute(
 * TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5LinksInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#findLinkFwd(Gwid, Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLinkGwid {@link Gwid} - идентификатор связи (идентификатор связи и класса в котором она объявлена)
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkFwd} - связь (м.б. пустая) или <code>null</code> если нет такого класса/связи/объекта
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#findLinkFwd(Gwid, Skid)}
   */
  IDtoLinkFwd beforeFindLink( Gwid aLinkGwid, Skid aLeftSkid, IDtoLinkFwd aLink );

  /**
   * Вызывается ПОСЛЕ выполнения метода {@link IS5BackendLinksSingleton#findLinkFwd(Gwid, Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLinkGwid {@link Gwid} - идентификатор связи (идентификатор связи и класса в котором она объявлена)
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее службой или интерсепторами
   * @return {@link IDtoLinkFwd} - связь (м.б. пустая) или <code>null</code> если нет такого класса/связи/объекта
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#findLinkFwd(Gwid, Skid)}
   */
  IDtoLinkFwd afterFindLink( Gwid aLinkGwid, Skid aLeftSkid, IDtoLinkFwd aLink );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#getAllLinksFwd(Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLinks {@link IList}&lt;{@link IDtoLinkFwd}&gt; - список связей найденных ранее интерсепторами. null: не
   *          найдены
   * @return {@link IList}&lt;{@link IDtoLinkFwd}&gt; - список связей (м.б. пустая) или <code>null</code> если нет
   *         такого класса/объекта
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#findLinkFwd(Gwid, Skid)}
   */
  IList<IDtoLinkFwd> beforeGetAllLinksFwd( Skid aLeftSkid, IList<IDtoLinkFwd> aLinks );

  /**
   * Вызывается ПОСЛЕ выполнения метода {@link IS5BackendLinksSingleton#getAllLinksFwd(Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLinks {@link IList}&lt;{@link IDtoLinkFwd} - список связей найденных ранее службой или интерсепторами.
   *          null: не найдены
   * @return {@link IList}&lt;{@link IDtoLinkFwd} - список связей (м.б. пустая) или <code>null</code> если нет такого
   *         класса/объекта
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#getAllLinksFwd(Skid)}
   */
  IList<IDtoLinkFwd> afterGetAllLinksFwd( Skid aLeftSkid, IList<IDtoLinkFwd> aLinks );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#findLinkRev(Gwid, Skid, IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLinkGwid {@link Gwid} идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @param aReverseLink {@link IDtoLinkRev} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkRev} - связь (м.б. пустая) или <code>null</code> если связь не найдена
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLinkRev(Gwid, Skid, IStringList)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IDtoLinkRev beforeFindLinkRev( Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds, IDtoLinkRev aReverseLink );

  /**
   * Вызывается ПОСЛЕ выполнения метода {@link IS5BackendLinksSingleton#findLinkRev(Gwid, Skid, IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLinkGwid {@link Gwid} идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @param aReverseLink {@link IDtoLinkRev} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkRev} - связь (м.б. пустая) или <code>null</code> если связь не найдена
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLinkRev(Gwid, Skid, IStringList)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IDtoLinkRev afterFindLinkRev( Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds, IDtoLinkRev aReverseLink );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#getAllLinksRev(Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aRightSkid {@link Skid} - идентификатор правого объекта связи
   * @param aReverseLinks {@link IMap}&lt;{@link Gwid},{@link IDtoLinkRev}&gt; карта обратных связей ранее найденная
   *          интерсепторами. null: не найдена
   *          <p>
   *          Ключ: абстрактный идентификатор связи;<br>
   *          Значение: обратная связь
   * @return {@link IMap}&lt;{@link Gwid},{@link IDtoLinkRev}&gt; карта обратных связей ранее найденная интерсепторами.
   *         null: не найдена.
   *         <p>
   *         Ключ: абстрактный идентификатор связи;<br>
   *         Значение: обратная связь
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#getAllLinksRev(Skid)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IMap<Gwid, IDtoLinkRev> beforeGetAllLinksRev( Skid aRightSkid, IMap<Gwid, IDtoLinkRev> aReverseLinks );

  /**
   * Вызывается ПОСЛЕ выполнения метода {@link IS5BackendLinksSingleton#findLinkRev(Gwid, Skid, IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aReverseLinks {@link IMap}&lt;{@link Gwid},{@link IDtoLinkRev}&gt; карта обратных связей ранее найденная
   *          службой или интерсепторами. null: не найдена
   *          <p>
   *          Ключ: абстрактный идентификатор связи;<br>
   *          Значение: обратная связь
   * @return {@link IMap}&lt;{@link Gwid},{@link IDtoLinkRev}&gt; карта обратных связей ранее найденная интерсепторами.
   *         null: не найдена.
   *         <p>
   *         Ключ: абстрактный идентификатор связи;<br>
   *         Значение: обратная связь
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#getAllLinksRev(Skid)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IMap<Gwid, IDtoLinkRev> afterGetAllLinksRev( Skid aRightSkid, IMap<Gwid, IDtoLinkRev> aReverseLinks );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#writeLinksFwd(IList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aUpdatedLinks
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoLinkFwd},{@link IDtoLinkFwd}&gt;&gt;&gt;
   *          карта связей объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса левого объекта связи;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние, {@link Pair#right()} - новое состояние.
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#writeLinksFwd(IList)}
   */
  void beforeWriteLinks( IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> aUpdatedLinks );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendLinksSingleton#writeLinksFwd(IList)}, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aUpdatedLinks
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoLinkFwd},{@link IDtoLinkFwd}&gt;&gt;&gt;
   *          карта связей объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса левого объекта связи;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние, {@link Pair#right()} - новое состояние.
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendLinksSingleton#writeLinksFwd(IList)} (откат транзакции)
   */
  void afterWriteLinks( IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> aUpdatedLinks );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#beforeFindLink(Gwid, Skid, IDtoLinkFwd)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aLinkGwid {@link Gwid} - идентификатор связи (идентификатор связи и класса в котором она объявлена)
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @return {@link IDtoLinkFwd} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#findLinkFwd(Gwid, Skid)}
   */
  static IDtoLinkFwd callBeforeFindLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport, Gwid aLinkGwid,
      Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLinkGwid, aLeftSkid );
    IDtoLinkFwd retValue = null;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeFindLink( aLinkGwid, aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#afterFindLink(Gwid, Skid, IDtoLinkFwd)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aLinkGwid {@link Gwid} - идентификатор связи (идентификатор связи и класса в котором она объявлена)
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее службой или интерсепторами
   * @return {@link IDtoLinkFwd} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#findLinkFwd(Gwid, Skid)}
   */
  static IDtoLinkFwd callAfterFindLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport, Gwid aLinkGwid,
      Skid aLeftSkid, IDtoLinkFwd aLink ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLinkGwid, aLeftSkid );
    IDtoLinkFwd retValue = aLink;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterFindLink( aLinkGwid, aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#beforeGetAllLinksFwd(Skid, IList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @return {@link IList}&lt;{@link IDtoLinkFwd}&gt; - список найденных связей или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#getAllLinksFwd(Skid)}
   */
  static IList<IDtoLinkFwd> callBeforeGetAllLinksFwd( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLeftSkid );
    IList<IDtoLinkFwd> retValue = null;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeGetAllLinksFwd( aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#afterGetAllLinksFwd(Skid, IList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLinks {@link IDtoLinkFwd} - список связей найденных ранее службой или интерсепторами. null: не найдено
   * @return {@link IList}&lt;{@link IDtoLinkFwd}&gt; - список найденных связей или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#getAllLinksFwd(Skid)}
   */
  static IList<IDtoLinkFwd> callAfterGetAllLinksFwd( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      Skid aLeftSkid, IList<IDtoLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLeftSkid );
    IList<IDtoLinkFwd> retValue = aLinks;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterGetAllLinksFwd( aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#beforeFindLinkRev(Gwid, Skid, IStringList, IDtoLinkRev)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aLinkGwid {@link Gwid} идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @return {@link IDtoLinkRev} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLinkRev(Gwid, Skid, IStringList)}
   */
  static IDtoLinkRev callBeforeFindLinkRev( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLinkGwid, aRightSkid, aLeftClassIds );
    IDtoLinkRev retValue = null;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeFindLinkRev( aLinkGwid, aRightSkid, aLeftClassIds, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#afterFindLinkRev(Gwid, Skid, IStringList, IDtoLinkRev)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aLinkGwid {@link Gwid} идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @param aReverseLink {@link IDtoLinkRev} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkRev} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLinkRev(Gwid, Skid, IStringList)}
   */
  static IDtoLinkRev callAfterFindLinkRev( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds, IDtoLinkRev aReverseLink ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLinkGwid, aRightSkid, aLeftClassIds );
    IDtoLinkRev retValue = aReverseLink;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterFindLinkRev( aLinkGwid, aRightSkid, aLeftClassIds, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#beforeGetAllLinksRev(Skid, IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aRightSkid {@link Skid} - идентификатор правого объекта связи
   * @return {@link IMap}&lt;{@link Gwid},{@link IDtoLinkRev}&gt; карта обратных связей ранее найденная интерсепторами.
   *         null: не найдена.
   *         <p>
   *         Ключ: абстрактный идентификатор связи;<br>
   *         Значение: обратная связь
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#getAllLinksRev(Skid)}
   */
  static IMap<Gwid, IDtoLinkRev> callBeforeGetAllLinksRev(
      S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport, Skid aRightSkid ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aRightSkid );
    IMap<Gwid, IDtoLinkRev> retValue = null;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeGetAllLinksRev( aRightSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#afterGetAllLinksRev(Skid, IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aRightSkid {@link Skid} - идентификатор правого объекта связи
   * @param aReverseLinks {@link IMap}&lt;{@link Gwid},{@link IDtoLinkRev}&gt; карта обратных связей ранее найденная
   *          службой или интерсепторами. null: не найдена
   *          <p>
   *          Ключ: абстрактный идентификатор связи;<br>
   *          Значение: обратная связь
   * @return {@link IMap}&lt;{@link Gwid},{@link IDtoLinkRev}&gt; карта обратных связей ранее найденная интерсепторами.
   *         null: не найдена.
   *         <p>
   *         Ключ: абстрактный идентификатор связи;<br>
   *         Значение: обратная связь
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#getAllLinksRev(Skid)}
   */
  static IMap<Gwid, IDtoLinkRev> callAfterGetAllLinksRev( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      Skid aRightSkid, IMap<Gwid, IDtoLinkRev> aReverseLinks ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aRightSkid );
    IMap<Gwid, IDtoLinkRev> retValue = aReverseLinks;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterGetAllLinksRev( aRightSkid, aReverseLinks );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#beforeWriteLinks(IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aUpdatedLinks
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoLinkFwd},{@link IDtoLinkFwd}&gt;&gt;&gt;
   *          карта связей объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса левого объекта связи;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние, {@link Pair#right()} - новое состояние.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#writeLinksFwd(IList)}
   */
  static void callBeforeWriteLinks( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> aUpdatedLinks ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aUpdatedLinks );
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeWriteLinks( aUpdatedLinks );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#afterWriteLinks(IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aUpdatedLinks
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoLinkFwd},{@link IDtoLinkFwd}&gt;&gt;&gt;
   *          карта связей объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса левого объекта связи;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние, {@link Pair#right()} - новое состояние.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendLinksSingleton#writeLinksFwd(IList)} (откат транзакции)
   */
  static void callAfterWriteLinksInterceptors( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> aUpdatedLinks ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aUpdatedLinks );
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterWriteLinks( aUpdatedLinks );
    }
  }
}
