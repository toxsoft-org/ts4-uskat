package org.toxsoft.uskat.base.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Тематический модуль обслуживания M5-моделей S5 классов.
 * <p>
 * Использование:
 * <ul>
 * <li>создать наследник с уникальным (в рамках соединения {@link ISkConnection}) идентификатором;</li>
 * <li>привязать модель к соединенеию методом {@link #bind(ISkConnection)} ДО открытия соединения;</li>
 * <li>(опционально) отвязать модуль от соединения после использования.</li>
 * </ul>
 * Замечания:
 * <p>
 * 1. Важно сделать привязку того модуля к {@link ISkConnection} до того, как соединение будет открыто методом
 * {@link ISkConnection#open(ITsContextRo)}. Дело в том, что модель создает модели в момент открытия соединения.
 * Следовательно, если привязаться после октрытия соединения, то модуль сработает только при следующем открытии этого же
 * экземпляра соединения.
 * <p>
 * 2. Отвязка от соединения в обычных приложенияах не требуется. Они могут потребоваться в специализированных
 * приложениях, в которых один и тот же экземпляр {@link ISkConnection} в разные моменты может устанавливать соединения
 * с совершенно разными серварами/бекендами с разными предметными областями. Например, административаная утилита (типа
 * s5admin) может иметь один экземпляр {@link ISkConnection}, которым пользователь устанавливает соединение с любым
 * сервером.
 * <p>
 * 3. Обратите внимание, при завершении соединения {@link ISkConnection#close()} тематические модели <b>остаются</b>
 * привязанными к соединению {@link ISkConnection}.
 *
 * @author goga
 */
public abstract class KM5AbstractModelManagementUnit {

  /**
   * Спсиок регистрации модуйлей, экземпляр которого находится в {@link ISkConnection#scope()}.
   * <p>
   * Сделан отдельным классом, чтобы обеспечить уникальность в контексте ссылки по {@link UnitsList#getClass()}.
   *
   * @author goga
   */
  static class UnitsList {

    IStringMapEdit<KM5AbstractModelManagementUnit> units = new StringMap<>();

    UnitsList() {
      // nop
    }

    IStringMapEdit<KM5AbstractModelManagementUnit> units() {
      return units;
    }

  }

  private final String unitId;

  private ISkConnection skConn = null;
  private IM5Domain     m5     = null;

  /**
   * Конструктор для наследников.
   * <p>
   * Вимание: инициализация модуля происходит после конструктора, соответственно, вызывать методы {@link #m5()},
   * {@link #coreApi()} из конструктора нельзя. По большому счету, конструктор наследника должен быть пустой, без
   * аргументов, и содержать только вызвов родительского коснтурктора.
   *
   * @param aUnitId String - идентификатор модуля
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException аргумент не ИД-путь
   */
  protected KM5AbstractModelManagementUnit( String aUnitId ) {
    unitId = StridUtils.checkValidIdPath( aUnitId );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //

  void papiInitModels( IM5Domain aM5 ) {
    TsInternalErrorRtException.checkNoNull( m5 );
    m5 = aM5;
    doInitModels();
  }

  IStringList papiUpdateModel( ECrudOp aOp, IStringList aClassIds ) {
    return doUpdateModels( aOp, aClassIds );
  }

  void papiClear() {
    doClear();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Привязывает этот тематический модуль к соединению.
   *
   * @param aSkConn {@link ISkConnection} - привязываемое соединение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemAlreadyExistsRtException модуль с идентификатором {@link #unitId()} уже привязан
   */
  final public void bind( ISkConnection aSkConn ) {
    TsIllegalStateRtException.checkTrue( isBind() );
    skConn = aSkConn;
    UnitsList ul = skConn.scope().find( UnitsList.class );
    if( ul == null ) {
      ul = new UnitsList();
      skConn.scope().put( UnitsList.class, ul );
    }
    TsItemAlreadyExistsRtException.checkTrue( ul.units().hasKey( unitId ) );
    ul.units().put( unitId, this );
  }

  /**
   * Определяет, привязан ли этот модуль к соединенеию.
   *
   * @return boolean - признак привязки к {@link #skConn()}
   */
  final boolean isBind() {
    return skConn != null;
  }

  /**
   * Снимает привязку с ссоединением {@link #skConn()}.
   * <p>
   * Если привязка не была установлена, метод ничего не делает.
   */
  final public void unbind() {
    if( skConn != null ) {
      UnitsList ul = skConn.scope().find( UnitsList.class );
      if( ul != null ) {
        ul.units().removeByKey( unitId );
        if( ul.units().isEmpty() ) {
          skConn.scope().remove( UnitsList.class );
        }
      }
    }
  }

  /**
   * Возвращает идентификатор модуля.
   *
   * @return String - идентификатор модуля
   */
  final public String unitId() {
    return unitId;
  }

  /**
   * Возвращает домен M5-моделирования, связанный с соединением {@link ISkConnection}.
   *
   * @return {@link IM5Domain} - домен M5-моделирования
   * @throws TsIllegalStateRtException нет привязки {@link #isBind()} = <code>false</code>
   * @throws TsItemNotFoundRtException в контексте приложения {@link ISkConnection#scope()} нет M5-домена
   */
  final public IM5Domain m5() {
    return skConn().scope().get( IM5Domain.class );
  }

  /**
   * Определяет, доступен ли сервер.
   *
   * @return boolean - признак, что модель инициализирован и есть связь с сервером
   */
  final public boolean isActive() {
    if( skConn != null ) {
      if( skConn.state() == ESkConnState.ACTIVE ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Возвращает соединение с сервером.
   *
   * @return {@link ISkConnection} - соединение с сервером
   * @throws TsIllegalStateRtException нет привязки {@link #isBind()} = <code>false</code>
   */
  final public ISkConnection skConn() {
    TsIllegalStateRtException.checkFalse( isBind() );
    return skConn;
  }

  /**
   * Возвращает API ядра соединение с сервером {@link #skConn()}.
   *
   * @return {@link ISkCoreApi} - API ядра
   * @throws TsIllegalStateRtException нет привязки {@link #isBind()} = <code>false</code>
   * @throws TsIllegalStateRtException отсутствует связь с сервером
   */
  final public ISkCoreApi coreApi() {
    TsIllegalArgumentRtException.checkFalse( isActive() );
    return skConn.coreApi();
  }

  // ------------------------------------------------------------------------------------
  // Для переопределения наследниками
  //

  /**
   * Наследник обязан создать модели тех классов, которые его касаются.
   * <p>
   * Вызывается каждый раз, после послу открытия соединения, когда имеется связь с сервером.
   */
  protected abstract void doInitModels();

  /**
   * Наследник может отследить изменения в описании системы и соответственно изменить модели.
   * <p>
   * Точнее, если модели классы этого модуля допускают редактирование во время работы программы, то модель обязан
   * отслеживать изменения, проверив, есть ли среди <code>aClassIds</code> свои классы. Новые модели должны заменить
   * сществующие методом {@link IM5Domain#replaceModel(M5Model)}. Модуль обязан вернуть перечень обработанных классов.
   * <p>
   * В базовом классе просто возвращает {@link IStringList#EMPTY}, при переопределении вызывать родительский метод не
   * нужно.
   *
   * @param aOp {@link ECrudOp} - какого рода изменения случилось с классами
   * @param aClassIds {@link IStringList} - идентификаторы изменившихся классов
   * @return {@link IStringList} - перечень тех классов из аргумента, которые были отработаны модулем
   */
  protected IStringList doUpdateModels( ECrudOp aOp, IStringList aClassIds ) {
    return IStringList.EMPTY;
  }

  /**
   * Наследник может провести дополнительные действия при завершении сеанса связи с сервером.
   * <p>
   * Вызывается после того, как связь с сервером разорвана и все модели удалены и домена. В этом методе
   * {@link #isActive()} = <code>false</code>.
   * <p>
   * В базовом классе ничего не делает, при переопределении вызывать родительский метод не нужно.
   */
  protected void doClear() {
    // nop
  }

}
