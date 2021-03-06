package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций изменения s5-классов системы.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5ClassesInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над классами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5ClassesInterceptor} должны быть иметь аннатоцию:
 * &#064;TransactionAttribute( TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5ClassesInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается до создания нового класса в системе
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание создаваемого класса
   * @throws TsIllegalStateRtException запрещено создавать класс
   */
  void beforeCreateClass( IDtoClassInfo aClassInfo );

  /**
   * Вызывается после создания класса в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание созданного класса
   * @throws TsIllegalStateRtException отменить создание класса (откат транзакции)
   */
  void afterCreateClass( IDtoClassInfo aClassInfo );

  /**
   * Вызывается до редактирования существующего класса в системе
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aPrevClassInfo {@link IDtoClassInfo} описание редактируемого класса (старая редакция)
   * @param aNewClassInfo {@link IDtoClassInfo} описание редактируемого класса (новая редакция)
   * @param aDescendants {@link IStridablesList}&lt;IDtoClassInfo&gt; описания классов-потомков изменяемого класса
   * @throws TsIllegalStateRtException запрещено редактировать класс
   */
  void beforeUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants );

  /**
   * Вызывается после редактирования существующего класса в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aPrevClassInfo {@link IDtoClassInfo} описание редактируемого класса (старая редакция)
   * @param aNewClassInfo {@link IDtoClassInfo} описание редактируемого класса (новая редакция)
   * @param aDescendants {@link IStridablesList}&lt;IDtoClassInfo&gt; описания классов-потомков измененного класса
   * @throws TsIllegalStateRtException отменить редактирование класса (откат транзакции)
   */
  void afterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants );

  /**
   * Вызывается до удаления класса из системы
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание удаляемого класса
   * @throws TsIllegalStateRtException запрещено удалять класс
   */
  void beforeDeleteClass( IDtoClassInfo aClassInfo );

  /**
   * Вызывается после удаления класса в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание удаленного класса
   * @throws TsIllegalStateRtException отменить удаление класса (откат транзакции)
   */
  void afterDeleteClass( IDtoClassInfo aClassInfo );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5ClassesInterceptor#beforeCreateClass(IDtoClassInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClassesInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aClassInfo {@link IDtoClassInfo} описание создаваемого класса
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запрещено создавать класс
   */
  static void callBeforeCreateClassInterceptors( S5InterceptorSupport<IS5ClassesInterceptor> aInterceptorSupport,
      IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassInfo );
    IList<IS5ClassesInterceptor> interceptors = aInterceptorSupport.interceptors();
    for( int index = 0, n = interceptors.size(); index < n; index++ ) {
      interceptors.get( index ).beforeCreateClass( aClassInfo );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ClassesInterceptor#afterCreateClass(IDtoClassInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClassesInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aClassInfo {@link IDtoClassInfo} описание созданного класса
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить создание класса (откат транзакции)
   */
  static void callAfterCreateClassInterceptors( S5InterceptorSupport<IS5ClassesInterceptor> aInterceptorSupport,
      IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassInfo );
    IList<IS5ClassesInterceptor> interceptors = aInterceptorSupport.interceptors();
    for( int index = interceptors.size() - 1; index >= 0; index-- ) {
      interceptors.get( index ).afterCreateClass( aClassInfo );
    }
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5ClassesInterceptor#beforeUpdateClass(IDtoClassInfo, IDtoClassInfo, IStridablesList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClassesInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aPrevClassInfo {@link IDtoClassInfo} описание редактируемого класса (старая редакция)
   * @param aNewClassInfo {@link IDtoClassInfo} описание редактируемого класса (новая редакция)
   * @param aDescendants {@link IStridablesList}&lt;IDtoClassInfo&gt; описания классов-потомков изменяемого класса
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запрещено редактировать класс
   */
  static void callBeforeUpdateClassInterceptors( S5InterceptorSupport<IS5ClassesInterceptor> aInterceptorSupport,
      IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo, IStridablesList<IDtoClassInfo> aDescendants ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aPrevClassInfo, aNewClassInfo, aDescendants );
    IList<IS5ClassesInterceptor> interceptors = aInterceptorSupport.interceptors();
    for( int index = 0, n = interceptors.size(); index < n; index++ ) {
      interceptors.get( index ).beforeUpdateClass( aPrevClassInfo, aNewClassInfo, aDescendants );
    }
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5ClassesInterceptor#afterUpdateClass(IDtoClassInfo, IDtoClassInfo, IStridablesList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClassesInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aPrevClassInfo {@link IDtoClassInfo} описание редактируемого класса (старая редакция)
   * @param aNewClassInfo {@link IDtoClassInfo} описание редактируемого класса (новая редакция)
   * @param aDescendants {@link IStridablesList}&lt;IDtoClassInfo&gt; описания классов-потомков изменяемого класса
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить редактирование класса (откат транзакции)
   */
  static void callAfterUpdateClassInterceptors( S5InterceptorSupport<IS5ClassesInterceptor> aInterceptorSupport,
      IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo, IStridablesList<IDtoClassInfo> aDescendants ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aPrevClassInfo, aNewClassInfo, aDescendants );
    IList<IS5ClassesInterceptor> interceptors = aInterceptorSupport.interceptors();
    for( int index = interceptors.size() - 1; index >= 0; index-- ) {
      interceptors.get( index ).afterUpdateClass( aPrevClassInfo, aNewClassInfo, aDescendants );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ClassesInterceptor#beforeDeleteClass(IDtoClassInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClassesInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aClassInfo {@link IDtoClassInfo} описание удаляемого класса
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запрещено удалять класс
   */
  static void callBeforeDeleteClassInterceptors( S5InterceptorSupport<IS5ClassesInterceptor> aInterceptorSupport,
      IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassInfo );
    IList<IS5ClassesInterceptor> interceptors = aInterceptorSupport.interceptors();
    for( int index = 0, n = interceptors.size(); index < n; index++ ) {
      interceptors.get( index ).beforeDeleteClass( aClassInfo );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ClassesInterceptor#afterDeleteClass(IDtoClassInfo)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClassesInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aClassInfo {@link IDtoClassInfo} описание удаленного класса
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить удаление класса (откат транзакции)
   */
  static void callAfterDeleteClassInterceptors( S5InterceptorSupport<IS5ClassesInterceptor> aInterceptorSupport,
      IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aClassInfo );
    IList<IS5ClassesInterceptor> interceptors = aInterceptorSupport.interceptors();
    for( int index = interceptors.size() - 1; index >= 0; index-- ) {
      interceptors.get( index ).afterDeleteClass( aClassInfo );
    }
  }
}
