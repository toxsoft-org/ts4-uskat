package org.toxsoft.uskat.s5.utils.datasets;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.utils.datasets.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.coll.primtypes.IIntMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.sessions.S5RemoteSession;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

import ru.uskat.common.dpu.rt.events.SkCurrDataValues;

/**
 * Поддержка работы с наборами данных
 *
 * @author mvk
 */
public final class S5DatasetSupport
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Текущая карта набора данных
   * <p>
   * Ключ: индекс РВ данного в наборе<br>
   * Значение: идентификатор данного
   * <p>
   * TODO: Запрещено использовать текущую реализацию IntMap(!) - из-за проблемы ее сериализации в рамках сохранения
   * сессии {@link S5RemoteSession}.
   */
  private final IIntMapEdit<Gwid> gwids = new IntMap<>();

  /**
   * Блокировка доступа (lazy)
   */
  private transient S5Lockable lock;

  /**
   * Журнал работы
   */
  private transient ILogger logger = getLogger( getClass() );

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Конфигурирует набор - какие РВданные хочет читать клиент.
   * <p>
   * Внимание: пустой список или <code>null</code> в качестве первого аргумента <code>aToRemove</code> имеют совершенно
   * разный смысл! Пустой список означает, что никакие РВданные не удаляются из списка интересующих клиента, в то время,
   * как <code>null</code> означает, что <b>все</b> до этого интересующие РВданные более не нужны, и должны быть удалены
   * из списка интересующих клиента.
   * <p>
   * Метод возвращает карту. Ключами в карте являются уникальные int-ключи, назначаемыйе сервером запрошенному
   * РВданному. Значением в карте является {@link Gwid} идентификатор <code>всех</code> запрошенных клиентом данных. То
   * есть, значения {@link IIntMap#values()} в карте, это список все РВданных, сформированный согласно запросу - ранее
   * запрошенные данные минус <code>aToRemove</code> плюс <code>aToAdd</code>. При этом в карте отсутствуют
   * повторяющейся РВ данные.
   * <p>
   * Обратите внимание, что сохранение значения ключенй между вызовами метода не гарантируется. Один и тотже
   * {@link Gwid} может иметь разный ключ после каждого вызова.
   *
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @param aParentGwids {@link IMap}&lt;{@link Gwid},Integer&gt; карта данных родительского набора;<br>
   *          {@link Gwid} идентификатор РВданного;<br>
   *          Integer индекс РВданного в родительском наборе.
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; - карта "уникальный ключ" - "GWID РВданного"
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  public IIntMap<Gwid> reconfigure( IGwidList aToRemove, IGwidList aToAdd, IMap<Gwid, Integer> aParentGwids ) {
    TsNullArgumentRtException.checkNulls( aToAdd, aParentGwids );
    lockWrite( lock() );
    try {
      if( aToRemove == null ) {
        gwids.clear();
      }
      if( aToRemove != null ) {
        for( Gwid removeGwid : aToRemove ) {
          for( int index : gwids.keys() ) {
            if( gwids.getByKey( index ).equals( removeGwid ) ) {
              gwids.removeByKey( index );
              break;
            }
          }
        }
      }
      for( Gwid addGwid : aToAdd ) {
        Integer parentIndex = aParentGwids.findByKey( addGwid );
        if( parentIndex == null ) {
          // Данное не существует
          logger().error( ERR_RTDATA_NOT_FOUND, addGwid );
          throw new TsIllegalArgumentRtException( ERR_RTDATA_NOT_FOUND, addGwid );
        }
        if( parentIndex.intValue() < 0 ) {
          // Недопустимый индекс данного
          logger().error( ERR_RTDATA_WRONG_INDEX, addGwid );
          throw new TsIllegalArgumentRtException( ERR_RTDATA_WRONG_INDEX, addGwid );
        }
        gwids.put( parentIndex.intValue(), addGwid );
      }
      return dataset();
    }
    finally {
      unlockWrite( lock() );
    }
  }

  /**
   * Возвращает текущую конфигурацию набора данных
   *
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; набора данных
   *         <p>
   *         Ключ: индекс РВ данного в наборе<br>
   *         Значение: идентификатор данного
   */
  public IIntMap<Gwid> dataset() {
    lockRead( lock() );
    try {
      IIntMapEdit<Gwid> retValue = new IntMap<>();
      retValue.putAll( gwids );
      return retValue;
    }
    finally {
      unlockRead( lock() );
    }
  }

  /**
   * Возвращает текущую конфигурацию набора данных в которой представлены только указанные данные
   * <p>
   * Если какого-либо данного нет в наборе, то оно игнорируется
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; набора данных
   *         <p>
   *         Ключ: индекс РВ данного в наборе<br>
   *         Значение: идентификатор данного
   */
  public IIntMap<Gwid> dataset( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    lockRead( lock() );
    try {
      IIntMapEdit<Gwid> retValue = new IntMap<>();
      for( int index : gwids.keys() ) {
        Gwid gwid = gwids.getByKey( index );
        if( aGwids.hasElem( gwid ) ) {
          retValue.put( index, gwid );
        }
      }
      return retValue;
    }
    finally {
      unlockRead( lock() );
    }
  }

  /**
   * Фильтрует указанные значения по индексам возращая только те значения индексы РВданных которых представлены в
   * целевой карте
   *
   * @param aValues {@link IIntMap}&lt;{@link IAtomicValue}&gt; значения для фильтрации
   * @return SkCurrDataValues отфильтрованные значения
   * @throws TsNullArgumentRtException аргумент = null
   */
  public SkCurrDataValues filter( IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    SkCurrDataValues retValue = new SkCurrDataValues();
    lockRead( lock() );
    try {
      for( int index : aValues.keys() ) {
        if( !gwids.hasKey( index ) ) {
          continue;
        }
        retValue.put( index, aValues.getByKey( index ) );
      }
      return retValue;
    }
    finally {
      unlockRead( lock() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает блокировку класса
   *
   * @return {@link S5Lockable} блокировка класса
   */
  private synchronized S5Lockable lock() {
    if( lock == null ) {
      lock = new S5Lockable();
    }
    return lock;
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  private ILogger logger() {
    if( logger == null ) {
      logger = getLogger( getClass() );
    }
    return logger;
  }

}
