package org.toxsoft.uskat.alarms.lib.flacon;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.av.opset.IOptionSet;

/**
 * Разнотипые значения сущностей в одном флаконе.
 * <p>
 * TODO требуется проработка!
 *
 * @author goga
 */
public interface ISkAlarmFlacon {

  /**
   * "Нулевой" объект.
   */
  ISkAlarmFlacon NULL = new InternalNullFlacon();

  /**
   * @return параметры аларма
   */
  IOptionSet params();

  // ------------------------------------------------------------------------------------
  // ver2
  //

  //
  // // идея: разделы флакона по каждому сервису
  //
  // interface IChapter {
  //
  // }
  //
  // IStringMap<IChapter> chaptersPerSwervice();
  //
  // // идея: карта идентифицируемых данных ИД - UGWI - данные
  //
  // interface IDataset {
  //
  // IUgwi ugwi();
  //
  // }

  // ------------------------------------------------------------------------------------
  // ver1
  //
  //
  // interface ISuitInfo
  // extends IStridable {
  //
  // Class<?> itemClass();
  //
  // boolean isSingle();
  //
  // // переменная часть....
  //
  // IStridablesList<IOptionInfo<?>> params();
  //
  // // ??? доп. инфо, например, об агрегации, о запросе, и т.п.
  //
  // // ITimeInterval timeInterval(); // если применимо???
  //
  // // String queryString();
  //
  // }
  //
  // interface ISuit<T> {
  //
  // IList<T> values();
  //
  // }
  //
  // IStridablesList<ISuitInfo> suitInfoes();
  //
  // ISuit getSuit( String aSuitId );

}

class InternalNullFlacon
    implements ISkAlarmFlacon, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link ISkAlarmFlacon#NULL}.
   *
   * @return Object объект {@link ISkAlarmFlacon#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return ISkAlarmFlacon.NULL;
  }

  @Override
  public IOptionSet params() {
    return IOptionSet.NULL;
  }

}
