package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.IDpuSdTypeInfo;
import ru.uskat.common.dpu.rt.events.SkEvent;

/**
 * Перехватчик операций изменения s5-типов системы.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5TypesInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над типами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5TypesInterceptor} должны быть иметь аннатоцию: &#064;TransactionAttribute(
 * TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5TypesInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается до создания нового типа в системе
   * <p>
   * Событие формируется в открытой транзакции которая в последствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание создаваемого типа
   * @throws TsIllegalStateRtException запрещено создавать тип
   */
  void beforeCreateType( IDpuSdTypeInfo aTypeInfo );

  /**
   * Вызывается после создания типа в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая в последствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание создаваемого типа
   * @throws TsIllegalStateRtException отменить создание типа (откат транзакции)
   */
  void afterCreateType( IDpuSdTypeInfo aTypeInfo );

  /**
   * Вызывается до редактирования существующего типа в системе
   * <p>
   * Событие формируется в открытой транзакции которая в последствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Событие передает какие классы используют данный тип (через определение атрибутов, реальных данных, параметров
   * событий или аргументов команд). Если класс является базовым классом для других классов, то в списке передается и
   * сам класс и все его классы-наследники.
   *
   * @param aPrevTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (старая редакция)
   * @param aNewTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (новая редакция)
   * @param aDependentClasses {@link IStridablesList}&lt;IDpuSdClassInfo&gt; описаний классов зависимых от данного типа
   * @throws TsIllegalStateRtException запрещено обновлять тип
   */
  void beforeUpdateType( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IStridablesList<IDpuSdClassInfo> aDependentClasses );

  /**
   * Вызывается после редактирования существующего класса в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая в последствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Событие передает какие классы используют данный тип (через определение атрибутов, реальных данных, параметров
   * событий или аргументов команд). Если класс является базовым классом для других классов, то в списке передается и
   * сам класс и все его классы-наследники.
   *
   * @param aPrevTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (старая редакция)
   * @param aNewTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (новая редакция)
   * @param aDependentClasses {@link IStridablesList}&lt;IDpuSdClassInfo&gt; описания классов зависимых от данного типа
   * @throws TsIllegalStateRtException отменить обновление типа (откат транзакции)
   */
  void afterUpdateType( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IStridablesList<IDpuSdClassInfo> aDependentClasses );

  /**
   * Вызывается до удаления типа из системы
   * <p>
   * Событие формируется в открытой транзакции которая в последствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание удаляемого типа
   * @throws TsIllegalStateRtException запрещено удалять тип
   */
  void beforeDeleteType( IDpuSdTypeInfo aTypeInfo );

  /**
   * Вызывается после удаления типа в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая в последствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание удаленного типа
   * @throws TsIllegalStateRtException отменить удаление типа (откат транзакции)
   */
  void afterDeleteType( IDpuSdTypeInfo aTypeInfo );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5TypesInterceptor#beforeCreateType(IDpuSdTypeInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5TypesInterceptor}&gt; поддержка перехватчиков
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание создаваемого типа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запрещено создавать тип
   */
  static void callBeforeCreateTypeInteceptors( S5InterceptorSupport<IS5TypesInterceptor> aInterceptorSupport,
      IDpuSdTypeInfo aTypeInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aTypeInfo );
    for( IS5TypesInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeCreateType( aTypeInfo );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5TypesInterceptor#afterCreateType(IDpuSdTypeInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5TypesInterceptor}&gt; поддержка перехватчиков
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание созданного типа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить создание класса (откат транзакции)
   */
  static void callAfterCreateTypeInteceptors( S5InterceptorSupport<IS5TypesInterceptor> aInterceptorSupport,
      IDpuSdTypeInfo aTypeInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aTypeInfo );
    for( IS5TypesInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterCreateType( aTypeInfo );
    }
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5TypesInterceptor#beforeUpdateType(IDpuSdTypeInfo, IDpuSdTypeInfo, IStridablesList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5TypesInterceptor}&gt; поддержка перехватчиков
   * @param aPrevTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (старая редакция)
   * @param aNewTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (новая редакция)
   * @param aDependentClasses {@link IStridablesList}&lt;IDpuSdClassInfo&gt; описания классов зависимых от данного типа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запрещено редактировать тип
   */
  static void callBeforeUpdateTypeInteceptors( S5InterceptorSupport<IS5TypesInterceptor> aInterceptorSupport,
      IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo, IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aPrevTypeInfo, aNewTypeInfo, aDependentClasses );
    for( IS5TypesInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeUpdateType( aPrevTypeInfo, aNewTypeInfo, aDependentClasses );
    }
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5TypesInterceptor#afterUpdateType(IDpuSdTypeInfo, IDpuSdTypeInfo, IStridablesList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5TypesInterceptor}&gt; поддержка перехватчиков
   * @param aPrevTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (старая редакция)
   * @param aNewTypeInfo {@link IDpuSdTypeInfo} описание редактируемого типа (новая редакция)
   * @param aDependentClasses {@link IStridablesList}&lt;IDpuSdTypeInfo&gt; описания классов зависимых от данного типа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить редактирование типа (откат транзакции)
   */
  static void callAfterUpdateTypeInteceptors( S5InterceptorSupport<IS5TypesInterceptor> aInterceptorSupport,
      IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo, IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aPrevTypeInfo, aNewTypeInfo, aDependentClasses );
    for( IS5TypesInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterUpdateType( aPrevTypeInfo, aNewTypeInfo, aDependentClasses );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5TypesInterceptor#beforeDeleteType(IDpuSdTypeInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5TypesInterceptor}&gt; поддержка перехватчиков
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание удаляемого типа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запрещено удалять тип
   */
  static void callBeforeDeleteTypeInteceptors( S5InterceptorSupport<IS5TypesInterceptor> aInterceptorSupport,
      IDpuSdTypeInfo aTypeInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aTypeInfo );
    for( IS5TypesInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeDeleteType( aTypeInfo );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5TypesInterceptor#afterDeleteType(IDpuSdTypeInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5TypesInterceptor}&gt; поддержка перехватчиков
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание удаленного типа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить удаление типа (откат транзакции)
   */
  static void callAfterDeleteTypeInteceptors( S5InterceptorSupport<IS5TypesInterceptor> aInterceptorSupport,
      IDpuSdTypeInfo aTypeInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aTypeInfo );
    for( IS5TypesInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterDeleteType( aTypeInfo );
    }
  }
}
