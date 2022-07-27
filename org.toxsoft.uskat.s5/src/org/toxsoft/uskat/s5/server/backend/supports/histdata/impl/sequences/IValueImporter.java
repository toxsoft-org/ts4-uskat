package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.*;

/**
 * TODO: 2022-03-03 mvk: код взят из tslib3. В tslib4 этго интерфейса нет
 * <p>
 * Способ получения (импорта) в Java-код значения данного из реального (красного) мира.
 * <p>
 * Для методов этого интерфейса действуют следующие общие правила по выбрасываемым исключениям:
 * <ul>
 * <li>если данное не может быть представлено в запрашиваемом виде, выбрасывыается исключение
 * {@link AvTypeCastRtException}. Например, попытка получить {@link EAtomicType#VALOBJ} значение методом
 * {@link #asBool()} ;</li>
 * <li>метод {@link #asString()} не выбрасывает вышеприведенных исключений - он всегда старается представить значение в
 * наиболее адкватном строковом виде;</li>
 * <li>если при попытке получения данного теряется информация из-за ограниченности используемого Java-типа, вырасывается
 * исключение {@link AvDataLossRtException}. Например, при попытке полчить число 0x1234567890ABCDEF методом
 * {@link #asInt()};</li>
 * <li>при попытке прочитать отсутствующее значение данного (см. {@link IAtomicValue#isAssigned()}) любым методом,
 * выбрасывается исключение {@link AvUnassignedValueRtException};</li>
 * <li>различные реализации могут добавлять свои непроверяемые исключения, которые должны быть документированы.</li>
 * </ul>
 *
 * @author goga
 */
public interface IValueImporter {

  /**
   * Возвращает булево значение данного.
   *
   * @return boolean - булевое значеное данного
   */
  boolean asBool();

  /**
   * По возможности возвращает значение как 32-битное целое число.
   *
   * @return int - 32-битное целое число
   */
  int asInt();

  /**
   * По возможности возвращает значение как 64-битное целое число.<br>
   *
   * @return long - 64-битное целое число
   */
  long asLong();

  /**
   * По возможности возвращает значение как 32-битное число с плавающей точкой.<br>
   *
   * @return float - 32-битное число с плавающей точкой
   */
  float asFloat();

  /**
   * По возможности возвращает значение как 64-битное число с плавающей точкой.<br>
   *
   * @return double - 64-битное число с плавающей точкой
   */
  double asDouble();

  /**
   * По возможности возвращает представление значения в виде текста.
   * <p>
   * Это единственный метод, который ни для каких данных не выбрасывает исключения, а всегда возвращает значение (даже
   * для неинициализированного значения).
   *
   * @return String - строковое представление значения, не бывает <code>null</code>
   */
  String asString();

  /**
   * Возвращает объект-значение.
   *
   * @param <T> - тип объекта-значения
   * @return &lt;T&gt; - объект-значение, может быть <code>null</code>
   * @throws ClassCastException объект-значение имеет несовместимый с запрошенным тип
   */
  <T> T asValobj();

}
