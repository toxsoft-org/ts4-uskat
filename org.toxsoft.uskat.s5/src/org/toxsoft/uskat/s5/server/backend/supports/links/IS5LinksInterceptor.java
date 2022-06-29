package org.toxsoft.uskat.s5.server.backend.supports.links;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
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
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#findLink(String, String, Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkFwd} - связь (м.б. пустая) или <code>null</code> если нет такого класса/связи/объекта
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLink(String, String, Skid)}
   */
  IDtoLinkFwd beforeFindLink( String aClassId, String aLinkId, Skid aLeftSkid, IDtoLinkFwd aLink );

  /**
   * Вызывается ПОСЛЕ выполнения метода {@link IS5BackendLinksSingleton#findLink(String, String, Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее службой или интерсепторами
   * @return {@link IDtoLinkFwd} - связь (м.б. пустая) или <code>null</code> если нет такого класса/связи/объекта
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLink(String, String, Skid)}
   */
  IDtoLinkFwd afterFindLink( String aClassId, String aLinkId, Skid aLeftSkid, IDtoLinkFwd aLink );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#readLink(String, String, Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkFwd} - связь (м.б. пустая) или <code>null</code> если связь не найдена
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readLink(String, String, Skid)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IDtoLinkFwd beforeReadLink( String aClassId, String aLinkId, Skid aLeftSkid, IDtoLinkFwd aLink );

  /**
   * Вызывается ПОСЛЕ выполнения метода {@link IS5BackendLinksSingleton#readLink(String, String, Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее службой или интерсепторами
   * @return {@link IDtoLinkFwd} - связь (м.б. пустая) или <code>null</code> если связь не найдена
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readLink(String, String, Skid)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IDtoLinkFwd afterReadLink( String aClassId, String aLinkId, Skid aLeftSkid, IDtoLinkFwd aLink );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#readReverseLink(String, String, Skid, IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @param aReverseLink {@link IDtoLinkRev} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkRev} - связь (м.б. пустая) или <code>null</code> если связь не найдена
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readReverseLink(String, String, Skid, IStringList)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IDtoLinkRev beforeReadReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds,
      IDtoLinkRev aReverseLink );

  /**
   * Вызывается ПОСЛЕ выполнения метода
   * {@link IS5BackendLinksSingleton#readReverseLink(String, String, Skid, IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @param aReverseLink {@link IDtoLinkRev} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkRev} - связь (м.б. пустая) или <code>null</code> если связь не найдена
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readReverseLink(String, String, Skid, IStringList)}
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IDtoLinkRev afterReadReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds,
      IDtoLinkRev aReverseLink );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLinksSingleton#writeLinks(IList, boolean)}
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
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#writeLinks(IList, boolean)}
   */
  void beforeWriteLinks( IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> aUpdatedLinks );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendLinksSingleton#writeLinks(IList, boolean)}, но до завершения транзакции.
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
   *           {@link IS5BackendLinksSingleton#writeLinks(IList, boolean)} (откат транзакции)
   */
  void afterWriteLinks( IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> aUpdatedLinks );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#beforeFindLink(String, String, Skid, IDtoLinkFwd)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @return {@link IDtoLinkFwd} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLink(String, String, Skid)}
   */
  static IDtoLinkFwd callBeforeFindLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport, String aClassId,
      String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassId, aLinkId, aLeftSkid );
    IDtoLinkFwd retValue = null;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeFindLink( aClassId, aLinkId, aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#afterFindLink(String, String, Skid, IDtoLinkFwd)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее службой или интерсепторами
   * @return {@link IDtoLinkFwd} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#findLink(String, String, Skid)}
   */
  static IDtoLinkFwd callAfterFindLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport, String aClassId,
      String aLinkId, Skid aLeftSkid, IDtoLinkFwd aLink ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassId, aLinkId, aLeftSkid );
    IDtoLinkFwd retValue = aLink;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterFindLink( aClassId, aLinkId, aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#beforeReadLink(String, String, Skid, IDtoLinkFwd)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @return {@link IDtoLinkFwd} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readLink(String, String, Skid)}
   */
  static IDtoLinkFwd callBeforeReadLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport, String aClassId,
      String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassId, aLinkId, aLeftSkid );
    IDtoLinkFwd retValue = null;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeReadLink( aClassId, aLinkId, aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LinksInterceptor#afterReadLink(String, String, Skid, IDtoLinkFwd)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @param aLink {@link IDtoLinkFwd} - связь найденная ранее службой или интерсепторами
   * @return {@link IDtoLinkFwd} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readLink(String, String, Skid)}
   */
  static IDtoLinkFwd callAfterReadLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport, String aClassId,
      String aLinkId, Skid aLeftSkid, IDtoLinkFwd aLink ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassId, aLinkId, aLeftSkid );
    IDtoLinkFwd retValue = aLink;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterReadLink( aClassId, aLinkId, aLeftSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5LinksInterceptor#beforeReadReverseLink(String, String, Skid, IStringList, IDtoLinkRev)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @return {@link IDtoLinkRev} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readReverseLink(String, String, Skid, IStringList)}
   */
  static IDtoLinkRev callBeforeReadReverseLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassId, aLinkId, aRightSkid, aLeftClassIds );
    IDtoLinkRev retValue = null;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeReadReverseLink( aClassId, aLinkId, aRightSkid, aLeftClassIds, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5LinksInterceptor#afterReadReverseLink(String, String, Skid, IStringList, IDtoLinkRev)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LinksInterceptor}&gt; поддержка перехватчиков
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @param aReverseLink {@link IDtoLinkRev} - связь найденная ранее интерсепторами
   * @return {@link IDtoLinkRev} - найденная связь или <code>null</code> если нет такой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendLinksSingleton#readReverseLink(String, String, Skid, IStringList)}
   */
  static IDtoLinkRev callAfterReadReverseLink( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds, IDtoLinkRev aReverseLink ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassId, aLinkId, aRightSkid, aLeftClassIds );
    IDtoLinkRev retValue = aReverseLink;
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterReadReverseLink( aClassId, aLinkId, aRightSkid, aLeftClassIds, retValue );
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
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLinksSingleton#writeLinks(IList, boolean)}
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
   *           {@link IS5BackendLinksSingleton#writeLinks(IList, boolean)} (откат транзакции)
   */
  static void callAfterWriteLinksInterceptors( S5InterceptorSupport<IS5LinksInterceptor> aInterceptorSupport,
      IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> aUpdatedLinks ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aUpdatedLinks );
    for( IS5LinksInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterWriteLinks( aUpdatedLinks );
    }
  }
}
