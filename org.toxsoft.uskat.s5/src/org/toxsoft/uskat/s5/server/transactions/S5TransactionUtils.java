package org.toxsoft.uskat.s5.server.transactions;

import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;

import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.common.dpu.IDpuSdClassInfo;

/**
 * Вспомогательные методы для работы с транзакциями
 *
 * @author mvk
 */
public class S5TransactionUtils {

  /**
   * Возвращает список объектов удаляемого класса
   *
   * @param aTransactionManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} backend управления объектами
   * @param aClassId String идентификатор удаляемого класса
   * @return {@link IList}&lt;{@link IDpuObject}&lt; список объектов изменяющегося класса или его наследников
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IList<IDpuObject> txRemovedClassObjs( IS5TransactionManagerSingleton aTransactionManager,
      IS5BackendObjectsSingleton aObjectsBackend, String aClassId ) {
    TsNullArgumentRtException.checkNulls( aTransactionManager, aObjectsBackend, aClassId );
    // Текущая транзакция
    IS5Transaction tx = aTransactionManager.getTransaction();
    // Поиск ресурса в транзакции
    IList<IDpuObject> retValue = tx.findResource( TX_REMOVED_CLASS_OBJS );
    if( retValue != null ) {
      // Ресурс найден в транзакции
      return retValue;
    }
    // Список объектов удаляемого класса
    retValue = aObjectsBackend.readObjects( new StringArrayList( aClassId ) );
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_REMOVED_CLASS_OBJS, retValue ) );
    return retValue;
  }

  /**
   * Возвращает список объектов изменяющихся классов
   *
   * @param aTransactionManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} backend управления объектами
   * @param aParentClassId String идентификатор меняющегося класса
   * @param aDescendants {@link IStridablesList}&lt;{@link IDpuSdClassInfo}&gt; список описаний классов наследников
   * @return {@link IList}&lt;{@link IDpuObject}&lt; список объектов изменяющегося класса или его наследников
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IList<IDpuObject> txUpdatedClassObjs( IS5TransactionManagerSingleton aTransactionManager,
      IS5BackendObjectsSingleton aObjectsBackend, String aParentClassId,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    TsNullArgumentRtException.checkNulls( aTransactionManager, aObjectsBackend, aParentClassId, aDescendants );
    // Текущая транзакция
    IS5Transaction tx = aTransactionManager.getTransaction();
    // Поиск ресурса в транзакции
    IList<IDpuObject> retValue = tx.findResource( TX_UPDATED_CLASS_OBJS );
    if( retValue != null ) {
      // Ресурс найден в транзакции
      return retValue;
    }
    // Список идентификаторов классов объекты которых могут изменить хранение (добавление/удаление атрибутов)
    IStringListEdit classIds = new StringArrayList( aDescendants.keys() );
    classIds.add( aParentClassId );
    // Список объектов изменяющих хранение
    retValue = aObjectsBackend.readObjects( classIds );
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_UPDATED_CLASS_OBJS, retValue ) );
    return retValue;
  }

}
