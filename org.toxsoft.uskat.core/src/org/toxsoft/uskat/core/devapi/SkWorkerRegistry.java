package org.toxsoft.uskat.core.devapi;

import static org.toxsoft.uskat.core.devapi.ISkResources.*;

import java.util.*;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Карта зарегистрированных компонент обработки данных uskat.
 * <p>
 * Ключ карты: идентификатор компонента {@link ISkWorker#id()}.<br>
 * Значение: компонент.
 *
 * @author mvk
 */
public final class SkWorkerRegistry
    implements Iterable<ISkWorker> {

  private final IStringMapEdit<ISkWorker> map = new SynchronizedStringMap<>( new StringMap<>() );

  /**
   * Возвращает количество зарегистрированных компонент.
   *
   * @return int количество компонент.
   */
  public int size() {
    return map.size();
  }

  /**
   * Возвращает компонент по его идентификатору.
   *
   * @param aWorkerId String идентификатор компонента
   * @return T найденный компонент
   * @param <T> тип компонента
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException компонент не найден
   */
  public <T extends ISkWorker> T getWorker( String aWorkerId ) {
    T retValue = findWorker( aWorkerId );
    if( retValue == null ) {
      throw new TsItemNotFoundRtException( ERR_WORKER_IS_NOT_FOUND, aWorkerId );
    }
    return retValue;
  }

  /**
   * Возвращает компонент по его идентификатору.
   *
   * @param aWorkerId String идентификатор компонента
   * @return T найденный компонент. null: компонент не найден
   * @param <T> тип компонента
   * @throws TsNullArgumentRtException аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  public <T extends ISkWorker> T findWorker( String aWorkerId ) {
    TsNullArgumentRtException.checkNull( aWorkerId );
    T retValue = (T)map.findByKey( aWorkerId );
    return retValue;
  }

  /**
   * Добавить компонент в карту.
   *
   * @param aWorkerId String идентификатор компонента.
   * @param aWorker {@link ISkWorker} компонент.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemAlreadyExistsRtException компонент уже зарегистрирован
   */
  public void put( String aWorkerId, ISkWorker aWorker ) {
    TsNullArgumentRtException.checkNulls( aWorkerId, aWorker );
    if( findWorker( aWorkerId ) != null ) {
      throw new TsIllegalArgumentRtException( ERR_WORKER_IS_ALREADY_REGISTERED, aWorkerId );
    }
    map.put( aWorkerId, aWorker );
  }

  // ------------------------------------------------------------------------------------
  // Iterable
  //
  @Override
  public Iterator<ISkWorker> iterator() {
    return map.iterator();
  }
}
