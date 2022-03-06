package org.toxsoft.uskat.s5.utils.indexes.impl;

import java.util.HashMap;

import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.coll.primtypes.ILongMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.LongMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.indexes.*;

/**
 * Методы формирования, восстановления бинарных индексов
 * <p>
 * По результатам тестирования определено, что производительность индекса выше (почти в два раза) чем {@link HashMap}
 * при наборах размером до 10000 элементов. При размерах близким к 10000 производительность примерно одинакова. При
 * размерах более 10000 {@link HashMap} выигрывает в производительности в линейной зависимости от размера набора.
 * Высокая производительность индекса в наборах до 10000 элементов объясняется отстутствием необходимости
 * использовавания boxing для примитивных типов представляющих ключи коллекции.
 *
 * @author mvk
 */
public class S5BinaryIndexUtils {

  // ------------------------------------------------------------------------------------
  // IIntIntIndex
  //
  /**
   * Формирование индекса целочисленного значения по целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @return {@link IIntIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static IIntIntIndexEdit createIntIntIndex( int[] aKeys, int[] aValues ) {
    return new S5IntIntIndexImpl( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса целочисленного значения по уникальному целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @return {@link IIntIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static IIntIntIndexEdit createUniqueIntIntIndex( int[] aKeys, int[] aValues ) {
    return new S5IntIntIndexImpl( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса целочисленного значения по целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link IIntIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static IIntIntIndexEdit restoreIntIntIndex( int[] aKeys, int[] aValues, boolean aCheck ) {
    IIntIntIndexEdit index = new S5IntIntIndexImpl( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса целочисленного значения по уникальному целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link IIntIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static IIntIntIndexEdit restoreUniqueIntIntIndex( int[] aKeys, int[] aValues, boolean aCheck ) {
    IIntIntIndexEdit index = new S5IntIntIndexImpl( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // IIntLongIndex
  //
  /**
   * Формирование индекса длинного целочисленного значения по целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues long[] массив значений индекса
   * @return {@link IIntLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static IIntLongIndexEdit createIntLongIndex( int[] aKeys, long[] aValues ) {
    return new S5IntLongIndexImpl( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса длинного целочисленного значения по уникальному целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues long[] массив значений индекса
   * @return {@link IIntLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static IIntLongIndexEdit createUniqueIntLongIndex( int[] aKeys, long[] aValues ) {
    return new S5IntLongIndexImpl( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса длинного целочисленного значения по целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues long[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link IIntLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static IIntLongIndexEdit restoreIntLongIndex( int[] aKeys, long[] aValues, boolean aCheck ) {
    IIntLongIndexEdit index = new S5IntLongIndexImpl( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса целочисленного значения по уникальному целочисленному ключу
   *
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link IIntLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static IIntLongIndexEdit restoreUniqueIntLongIndex( int[] aKeys, long[] aValues, boolean aCheck ) {
    IIntLongIndexEdit index = new S5IntLongIndexImpl( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // IIntElemIndex
  //
  /**
   * Формирование индекса произвольного значения по целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @return {@link IIntElemIndex} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static <V> IIntElemIndexEdit<V> createIntElemIndex( int[] aKeys, V[] aValues ) {
    return new S5IntElemIndexImpl<>( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса произвольного значения по уникальному целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @return {@link IIntElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static <V> IIntElemIndexEdit<V> createUniqueIntElemIndex( int[] aKeys, V[] aValues ) {
    return new S5IntElemIndexImpl<>( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса произвольного значения по целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link IIntElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static <V> IIntElemIndexEdit<V> restoreIntElemIndex( int[] aKeys, V[] aValues, boolean aCheck ) {
    IIntElemIndexEdit<V> index = new S5IntElemIndexImpl<>( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса произвольного значения по уникальному целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys int[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link IIntElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static <V> IIntElemIndexEdit<V> restoreUniqueIntElemIndex( int[] aKeys, V[] aValues, boolean aCheck ) {
    IIntElemIndexEdit<V> index = new S5IntElemIndexImpl<>( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // ILongByteIndex
  //
  /**
   * Формирование индекса байтового значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues byte[] массив значений индекса
   * @return {@link ILongByteIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongByteIndexEdit createLongByteIndex( long[] aKeys, byte[] aValues ) {
    return new S5LongByteIndexImpl( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса байтового значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues byte[] массив значений индекса
   * @return {@link ILongByteIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongByteIndexEdit createUniqueLongBooleanIndex( long[] aKeys, byte[] aValues ) {
    return new S5LongByteIndexImpl( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса байтового значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues byte[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongByteIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongByteIndexEdit restoreLongByteIndex( long[] aKeys, byte[] aValues, boolean aCheck ) {
    ILongByteIndexEdit index = new S5LongByteIndexImpl( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса байтового значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues byte[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongByteIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongByteIndexEdit restoreUniqueLongByteIndex( long[] aKeys, byte[] aValues, boolean aCheck ) {
    ILongByteIndexEdit index = new S5LongByteIndexImpl( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // ILongBooleanIndex
  //
  /**
   * Формирование индекса логического значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues boolean[] массив значений индекса
   * @return {@link ILongBooleanIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongBooleanIndexEdit createLongBooleanIndex( long[] aKeys, boolean[] aValues ) {
    return new S5LongBooleanIndexImpl( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса логического значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues boolean[] массив значений индекса
   * @return {@link ILongBooleanIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongBooleanIndexEdit createUniqueLongBooleanIndex( long[] aKeys, boolean[] aValues ) {
    return new S5LongBooleanIndexImpl( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса логического значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues boolean[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongBooleanIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongBooleanIndexEdit restoreLongBooleanIndex( long[] aKeys, boolean[] aValues, boolean aCheck ) {
    ILongBooleanIndexEdit index = new S5LongBooleanIndexImpl( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса логического значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues boolean[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongBooleanIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongBooleanIndexEdit restoreUniqueLongBooleanIndex( long[] aKeys, boolean[] aValues, boolean aCheck ) {
    ILongBooleanIndexEdit index = new S5LongBooleanIndexImpl( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // ILongDoubleIndex
  //
  /**
   * Формирование индекса вещественного(double) значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues double[] массив значений индекса
   * @return {@link ILongDoubleIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongDoubleIndexEdit createLongDoubleIndex( long[] aKeys, double[] aValues ) {
    return new S5LongDoubleIndexImpl( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса вещественного(double) значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues double[] массив значений индекса
   * @return {@link ILongDoubleIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongDoubleIndexEdit createUniqueLongDoubleIndex( long[] aKeys, double[] aValues ) {
    return new S5LongDoubleIndexImpl( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса вещественного(double) значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues double[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongDoubleIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongDoubleIndexEdit restoreLongDoubleIndex( long[] aKeys, double[] aValues, boolean aCheck ) {
    ILongDoubleIndexEdit index = new S5LongDoubleIndexImpl( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса вещественного(double) значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues double[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongDoubleIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongDoubleIndexEdit restoreUniqueLongDoubleIndex( long[] aKeys, double[] aValues, boolean aCheck ) {
    ILongDoubleIndexEdit index = new S5LongDoubleIndexImpl( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // ILongIntIndex
  //
  /**
   * Формирование индекса целочисленного значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @return {@link ILongIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongIntIndexEdit createLongIntIndex( long[] aKeys, int[] aValues ) {
    return new S5LongIntIndexImpl( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса целочисленного значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @return {@link ILongIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongIntIndexEdit createUniqueLongIntIndex( long[] aKeys, int[] aValues ) {
    return new S5LongIntIndexImpl( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса целочисленного значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongIntIndexEdit restoreLongIntIndex( long[] aKeys, int[] aValues, boolean aCheck ) {
    ILongIntIndexEdit index = new S5LongIntIndexImpl( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса целочисленного значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongIntIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongIntIndexEdit restoreUniqueLongIntIndex( long[] aKeys, int[] aValues, boolean aCheck ) {
    ILongIntIndexEdit index = new S5LongIntIndexImpl( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // ILongLongIndex
  //
  /**
   * Формирование индекса длинного целочисленного значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues long[] массив значений индекса
   * @return {@link ILongLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongLongIndexEdit createLongLongIndex( long[] aKeys, long[] aValues ) {
    return new S5LongLongIndexImpl( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса длинного целочисленного значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues long[] массив значений индекса
   * @return {@link ILongLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static ILongLongIndexEdit createUniqueLongLongIndex( long[] aKeys, long[] aValues ) {
    return new S5LongLongIndexImpl( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса длинного целочисленного значения по длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues long[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongLongIndexEdit restoreLongLongIndex( long[] aKeys, long[] aValues, boolean aCheck ) {
    ILongLongIndexEdit index = new S5LongLongIndexImpl( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса длинного целочисленного значения по уникальному длинному целочисленному ключу
   *
   * @param aKeys long[] массив ключей индекса
   * @param aValues long[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongLongIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static ILongLongIndexEdit restoreUniqueLongLongIndex( long[] aKeys, long[] aValues, boolean aCheck ) {
    ILongLongIndexEdit index = new S5LongLongIndexImpl( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // ILongElemIndex
  //
  /**
   * Формирование индекса произвольного значения по длинному целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys long[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @return {@link ILongElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static <V> ILongElemIndexEdit<V> createLongElemIndex( long[] aKeys, V[] aValues ) {
    return new S5LongElemIndexImpl<>( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса произвольного значения по уникальному длинному целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys long[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @return {@link ILongElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static <V> ILongElemIndexEdit<V> createUniqueLongElemIndex( long[] aKeys, V[] aValues ) {
    return new S5LongElemIndexImpl<>( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса произвольного значения по длинному целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys long[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static <V> ILongElemIndexEdit<V> restoreLongElemIndex( long[] aKeys, V[] aValues, boolean aCheck ) {
    ILongElemIndexEdit<V> index = new S5LongElemIndexImpl<>( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса произвольного значения по уникальному длинному целочисленному ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys long[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static <V> ILongElemIndexEdit<V> restoreUniqueLongElemIndex( long[] aKeys, V[] aValues, boolean aCheck ) {
    ILongElemIndexEdit<V> index = new S5LongElemIndexImpl<>( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // ------------------------------------------------------------------------------------
  // IStringElemIndex
  //
  /**
   * Формирование индекса произвольного значения по строковому ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys String[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @return {@link IStringElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static <V> IStringElemIndexEdit<V> createStringElemIndex( String[] aKeys, V[] aValues ) {
    return new StringElemIndexImpl<>( false, aKeys, aValues, false );
  }

  /**
   * Формирование индекса произвольного значения по уникальному строковому ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys String[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @return {@link IStringElemIndexEdit} индекс с возможностью редактирования
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   */
  public static <V> IStringElemIndexEdit<V> createUniqueStringElemIndex( String[] aKeys, V[] aValues ) {
    return new StringElemIndexImpl<>( true, aKeys, aValues, false );
  }

  /**
   * Восстановление индекса произвольного значения по строковому ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys String[] массив ключей индекса
   * @param aValues V[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link IStringElemIndexEdit} индекс
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static <V> IStringElemIndexEdit<V> restoreStringElemIndex( String[] aKeys, V[] aValues, boolean aCheck ) {
    IStringElemIndexEdit<V> index = new StringElemIndexImpl<>( false, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  /**
   * Восстановление индекса произвольного значения по уникальному строковому ключу
   *
   * @param <V> тип значения индекса
   * @param aKeys String[] массив ключей индекса
   * @param aValues int[] массив значений индекса
   * @param aCheck boolean <b>true</b> проверить целостность индекса; <b>false</b> не проверять целостность индекса
   * @return {@link ILongElemIndexEdit} индекс
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException пустой массив ключей индекса
   * @throws TsIllegalArgumentRtException массив значений не равен массиву ключей индекса
   * @throws TsIllegalStateRtException нарушение целостности индекса
   */
  public static <V> ILongElemIndexEdit<V> restoreUniqueStringElemIndex( long[] aKeys, V[] aValues, boolean aCheck ) {
    ILongElemIndexEdit<V> index = new S5LongElemIndexImpl<>( true, aKeys, aValues, true );
    if( aCheck ) {
      index.check();
    }
    return index;
  }

  // --------------------------------------------------------------------------
  // Методы формирования коллекций tslib
  //
  /**
   * Формирование карты значений с целочисленными ключами на основе индекса
   *
   * @param aIndex {@link IIntElemIndex} индекс доступа к значениям
   * @return {@link IIntMap}&lt;T&gt; карта значений. Ключ: ключ индекса. Значение: значение индекса
   * @param <T> тип значений
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException в индексе доступа есть ключи имеющие одинаковые значения
   */
  public static <T> IIntMap<T> createIntMap( IIntElemIndex<T> aIndex ) {
    TsNullArgumentRtException.checkNull( aIndex );
    return new S5IntElemMap<>( aIndex );
  }

  /**
   * Формирование карты значений с длинными целочисленными ключами на основе индекса
   *
   * @param aIndex {@link ILongElemIndex} индекс доступа к значениям
   * @return {@link ILongMap}&lt;T&gt; карта значений. Ключ: ключ индекса. Значение: значение индекса
   * @param <T> тип значений
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException в индексе доступа есть ключи имеющие одинаковые значения
   */
  public static <T> ILongMap<T> createLongMap( ILongElemIndex<T> aIndex ) {
    TsNullArgumentRtException.checkNull( aIndex );
    return new S5LongElemMap<>( aIndex );
  }

  // TODO: реализация формирований коллекций tslib на основе индексов доступа
  // 1. редактируемые карты IIntMapEdit, ILongMapEdit
  // 2. нередактируемые и редактируемые списки IIntList(Edit), ILongList(Edit) с уникальным ключом и без

  /**
   * @param aArgs аргументы командной строки
   */
  @SuppressWarnings( { "boxing", "nls" } )
  public static void main( String[] aArgs ) {
    // ВНИМАНИЕ!!! Для адекватного измерения на малых количествах (до 10000) может понадобится внешняя загрузка
    // процессора холостыми задачами. Например, на lenovo sl500 эти данные были получены без запуска холостых задач, а
    // на desktop с CPU 3GHz потребовалось параллельно запустить два youtube-ролика )))
    //
    // int MAX = 1000; // elemIndex = 0 ms, hashmap = 2 ms, longmap = 5 ms
    // int MAX = 10000; // elemIndex = 3 ms, hashmap = 3 ms, longmap = 211 ms
    // int MAX = 100000; // elemIndex = 27 ms, hashmap = 7 ms, longmap = 20516 ms
    int MAX = 10000; //
    int UNUSED = 100 * MAX;
    long[] longKeys = new long[MAX];
    String[] strKeys = new String[MAX];
    Long[] unuseKeys = new Long[UNUSED];
    Integer[] elemValues = new Integer[MAX];
    for( int index = 0; index < MAX; index++ ) {
      longKeys[index] = getKey( index );
      strKeys[index] = "key" + getKey( index );
      elemValues[index] = index / 2;
    }
    for( int index = 0; index < UNUSED; index++ ) {
      unuseKeys[index] = Long.valueOf( getKey( MAX + index ) );
    }
    ILongElemIndexEdit<Integer> elemIndex =
        createLongElemIndex( new long[longKeys.length], new Integer[longKeys.length] );
    elemIndex.add( longKeys, elemValues );

    HashMap<Long, Integer> hashmap = new HashMap<>( MAX );
    for( int index = 0; index < MAX; index++ ) {
      hashmap.put( longKeys[index], elemValues[index] );
    }

    LongMap<Integer> longmap = new LongMap<>();
    for( int index = 0; index < MAX; index++ ) {
      longmap.put( longKeys[index], elemValues[index] );
    }

    IStringElemIndexEdit<Integer> strIndex =
        createStringElemIndex( new String[longKeys.length], new Integer[longKeys.length] );
    strIndex.add( strKeys, elemValues );

    HashMap<String, Integer> strmap = new HashMap<>();
    for( int index = 0; index < MAX; index++ ) {
      strmap.put( strKeys[index], elemValues[index] );
    }

    System.out.println( "START TEST..." );

    int sum = 0;
    sum = testLongElemIndex( MAX, elemIndex, sum );

    sum = testHashMap( MAX, hashmap, sum );

    sum = testLongMap( MAX, longmap, sum );

    sum = testStringElemIndex( MAX, strIndex, sum );

    sum = testStringMap( MAX, strmap, sum );

    System.out.println( "sum: " + sum );

    System.out.println( "unused keys size: = " + unuseKeys.length );
  }

  private static long getKey( long aIndex ) {
    return Long.MIN_VALUE + aIndex;
  }

  @SuppressWarnings( { "nls" } )
  private static int testLongElemIndex( int MAX, ILongElemIndexEdit<Integer> elemIndex, int sum ) {
    long startTime;
    int retValue = sum;
    System.gc();
    startTime = System.currentTimeMillis();
    for( long index = 0; index < MAX; index++ ) {
      // int lookup = elemIndex.findIndex( getKey( index ) );
      int lookup = elemIndex.findIndex( getKey( index ) );
      retValue += lookup;
    }
    System.out.println( "by elemIndex:  " + (System.currentTimeMillis() - startTime) + " мсек" );
    return retValue;
  }

  @SuppressWarnings( { "nls", "boxing" } )
  private static int testHashMap( int MAX, HashMap<Long, Integer> hashmap, int sum ) {
    long startTime;
    System.gc();
    int retValue = sum;
    startTime = System.currentTimeMillis();
    for( long index = 0; index < MAX; index++ ) {
      int lookup = hashmap.get( getKey( index ) );
      retValue += lookup;
    }
    System.out.println( "by hashmap: " + (System.currentTimeMillis() - startTime) + " мсек" );
    return retValue;
  }

  @SuppressWarnings( { "nls", "boxing" } )
  private static int testLongMap( int MAX, LongMap<Integer> longmap, int sum ) {
    long startTime;
    int retValue = sum;
    System.gc();
    startTime = System.currentTimeMillis();
    for( long index = 0; index < MAX; index++ ) {
      int lookup = longmap.getByKey( getKey( index ) );
      retValue += lookup;
    }
    System.out.println( "by longmap: " + (System.currentTimeMillis() - startTime) + " мсек" );
    return retValue;
  }

  @SuppressWarnings( "nls" )
  private static int testStringElemIndex( int MAX, IStringElemIndexEdit<Integer> elemIndex, int sum ) {
    long startTime;
    int retValue = sum;
    System.gc();
    startTime = System.currentTimeMillis();
    for( long index = 0; index < MAX; index++ ) {
      // int lookup = elemIndex.findIndex( getKey( index ) );
      int lookup = elemIndex.findIndex( "key" + getKey( index ) );
      retValue += lookup;
    }
    System.out.println( "by strIndex:  " + (System.currentTimeMillis() - startTime) + " мсек" );
    return retValue;
  }

  @SuppressWarnings( { "nls", "boxing" } )
  private static int testStringMap( int MAX, HashMap<String, Integer> hashmap, int sum ) {
    long startTime;
    int retValue = sum;
    System.gc();
    startTime = System.currentTimeMillis();
    for( long index = 0; index < MAX; index++ ) {
      int lookup = hashmap.get( "key" + getKey( index ) );
      retValue += lookup;
    }
    System.out.println( "by strhmap: " + (System.currentTimeMillis() - startTime) + " мсек" );
    return retValue;
  }

}
