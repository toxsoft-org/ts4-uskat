package org.toxsoft.uskat.s5.server.backend.supports.objects;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций (создание, удаление, изменение значений атрибутов) над s5-объектами системы.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5ObjectsInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5ObjectsInterceptor} должны быть иметь аннатоцию:
 * &#064;TransactionAttribute( TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5ObjectsInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#findObject(Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkid {@link Skid} - идентификатор объекта
   * @param aObj {@link IDtoObject} объект найденный ранее интерсепторами
   * @return {@link ISkObject} - найденный объект или <code>null</code> если нет такого
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#findObject(Skid)}
   */
  IDtoObject beforeFindObject( Skid aSkid, IDtoObject aObj );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#findObject(Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkid {@link Skid} - идентификатор объекта
   * @param aObj {@link IDtoObject} объект найденный ранее службой или интерсепторами
   * @return {@link ISkObject} - найденный объект или <code>null</code> если нет такого
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#findObject(Skid)}
   */
  IDtoObject afterFindObject( Skid aSkid, IDtoObject aObj );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassIds {@link IStringList} список идентификаторов классов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  void beforeReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassIds {@link IStringList} список идентификаторов классов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее службой и/или
   *          интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  void afterReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkids {@link ISkidList} список идентификаторов объектов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkids {@link ISkidList} список идентификаторов объектов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее службой и/или
   *          интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs );

  /**
   * Вызывается ДО выполнения метода
   * {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aRemovedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта
   *          удаляемых объектов из базы данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список удаляемых объектов класса.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние , {@link Pair#right()} - новое состояние.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список создаваемых объектов класса.
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)}
   */
  void beforeWriteObjects( //
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)}, но до
   * завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aRemovedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта
   *          удаляемых объектов из базы данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список удаляемых объектов класса.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние , {@link Pair#right()} - новое состояние.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список создаваемых объектов класса.
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)} (откат
   *           транзакции)
   */
  void afterWriteObjects( //
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#beforeFindObject(Skid, IDtoObject)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSkid {@link Skid} - идентификатор объекта
   * @return {@link IDtoObject} - найденный объект или <code>null</code> если нет такого
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#findObject(Skid)}
   */
  static IDtoObject callBeforeFindObject( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport,
      Skid aSkid ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSkid );
    IDtoObject retValue = null;
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeFindObject( aSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#afterFindObject(Skid, IDtoObject)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSkid {@link Skid} - идентификатор объекта
   * @param aObj <T> объект найденный ранее службой или интерсепторами
   * @return {@link IDtoObject} - найденный объект или <code>null</code> если нет такого
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#findObject(Skid)}
   */
  static IDtoObject callAfterFindObject( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport, Skid aSkid,
      IDtoObject aObj ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSkid );
    IDtoObject retValue = aObj;
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterFindObject( aSkid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#beforeReadObjects(IStringList, IListEdit)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aClassIds {@link IStringList} список идентификаторов классов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее интерсепторами
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  static void callBeforeReadObjects( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport,
      IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassIds, aObjs );
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeReadObjects( aClassIds, aObjs );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#afterReadObjects(IStringList, IListEdit)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aClassIds {@link IStringList} список идентификаторов классов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее интерсепторами
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  static void callAfterReadObjects( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport,
      IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassIds, aObjs );
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterReadObjects( aClassIds, aObjs );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#beforeReadObjectsByIds(ISkidList, IListEdit)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSkids {@link ISkidList} список идентификаторов объектов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее интерсепторами
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendObjectsSingleton#readObjectsByIds(ISkidList)}
   */
  static void callBeforeReadObjectsByIds( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport,
      ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSkids, aObjs );
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeReadObjectsByIds( aSkids, aObjs );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#afterReadObjectsByIds(ISkidList, IListEdit)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSkids {@link ISkidList} список идентификаторов объектов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее службой и/или
   *          интерсепторами
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendObjectsSingleton#readObjectsByIds(ISkidList)}
   */
  static void callAfterReadObjectsByIds( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport,
      ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSkids, aObjs );
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterReadObjectsByIds( aSkids, aObjs );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#beforeWriteObjects(IMap, IMap, IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aRemovedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта
   *          удаляемых объектов из базы данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список удаляемых объектов класса.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние , {@link Pair#right()} - новое состояние.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список создаваемых объектов класса.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)}
   */
  static void callBeforeWriteObjectsInterceptors( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport,
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aRemovedObjs, aUpdatedObjs, aCreatedObjs );
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeWriteObjects( aRemovedObjs, aUpdatedObjs, aCreatedObjs );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ObjectsInterceptor#afterWriteObjects(IMap, IMap, IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ObjectsInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aRemovedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта
   *          удаляемых объектов из базы данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список удаляемых объектов класса.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние , {@link Pair#right()} - новое состояние.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список создаваемых объектов класса.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)} (откат
   *           транзакции)
   */
  static void callAfterWriteObjectsInterceptors( S5InterceptorSupport<IS5ObjectsInterceptor> aInterceptorSupport,
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aRemovedObjs, aUpdatedObjs, aCreatedObjs );
    for( IS5ObjectsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterWriteObjects( aRemovedObjs, aUpdatedObjs, aCreatedObjs );
    }
  }

}
