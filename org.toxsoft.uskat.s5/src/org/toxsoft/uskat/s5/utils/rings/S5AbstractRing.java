package org.toxsoft.uskat.s5.utils.rings;

import java.io.Serializable;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Абстрактная реализация кольцевого буфера значений
 *
 * @author mvk
 */
public class S5AbstractRing
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Максимально возможное количество элементов кольца ( мощность) кольца
   */
  private int capacity;

  /**
   * Текущее количество элементов в кольце
   */
  private int size;

  /**
   * Индекс в кольце последнего добавленного элемента
   */
  private int last;

  /**
   * Конструктор
   *
   * @param aCapacity int максимально возможное количество элементов кольца ( мощность) кольца
   * @throws TsIllegalArgumentRtException aCapacity <= 0
   */
  protected S5AbstractRing( int aCapacity ) {
    setCapacity( aCapacity );
    setSize( 0 );
    setLast( 0 );
    // Нельзя вызывать clear() так как clear() может переопределяться в наследниках
    last = -1;
    size = 0;
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link S5AbstractRing} кольцо-источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5AbstractRing( S5AbstractRing aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    setCapacity( aSource.capacity );
    setSize( aSource.size );
    setLast( aSource.last );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает максимально возможное количество элементов кольца ( мощность) кольца
   *
   * @return int мощность кольца
   */
  public final int capacity() {
    return capacity;
  }

  /**
   * Возвращает текущее количество элементов в кольце
   *
   * @return текущее количество элементов
   */
  public final int size() {
    return size;
  }

  /**
   * Удалить все значения из буфера
   */
  void clear() {
    last = -1;
    size = 0;
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает индекс в кольце последнего добавленного элемента
   *
   * @return int индекс в кольце
   */
  protected final int last() {
    return last;
  }

  /**
   * Установить максимально возможное количество элементов кольца ( мощность) кольца
   *
   * @param aCapacity мощность кольца
   * @throws TsIllegalArgumentRtException aCapacity <= 0
   */
  protected final void setCapacity( int aCapacity ) {
    TsIllegalArgumentRtException.checkTrue( aCapacity <= 0 );
    capacity = aCapacity;
  }

  protected final void setSize( int aSize ) {
    size = aSize;
  }

  protected final void setLast( int aLast ) {
    last = aLast;
  }

  /**
   * Преобразует индекс пользователя в индекс кольца
   *
   * @param aUserIndex int aUserIndex индекс пользователя
   * @return int индекс кольца
   * @throws TsIllegalArgumentRtException недопустимый индекс пользователя
   */
  protected final int index( int aUserIndex ) {
    TsIllegalArgumentRtException.checkTrue( aUserIndex < 0 || aUserIndex >= capacity );
    if( last >= aUserIndex ) {
      return (last - aUserIndex);
    }
    return (capacity - (aUserIndex - last));
  }

  /**
   * Создает и возвращает индекс по которому будет произведено добавление нового элемента
   *
   * @return возвращает индекс(в кольце) для нового элемента
   */
  protected final int newIndex() {
    try {
      if( last >= capacity - 1 ) {
        return 0;
      }
      return (last + 1);
    }
    finally {
      size++;
      if( ++last >= capacity ) {
        last = 0;
      }
      size = Math.min( size, capacity );
    }
  }
}
