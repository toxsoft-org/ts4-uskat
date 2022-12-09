package org.toxsoft.uskat.alarms.lib.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;

/**
 * Хранение информации о параметрах одного аларма( разнотипные значения сущностей в одном флаконе).
 * <p>
 *
 * @author mvk
 */
public class SkAlarmFlacon
    implements ISkAlarmFlacon, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SkAlarmFlacon"; //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<ISkAlarmFlacon> KEEPER =
      new AbstractEntityKeeper<>( ISkAlarmFlacon.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkAlarmFlacon aEntity ) {
          OptionSetKeeper.KEEPER.write( aSw, aEntity.params() );
        }

        @Override
        protected ISkAlarmFlacon doRead( IStrioReader aSr ) {
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          return new SkAlarmFlacon( params );
        }

      };

  /**
   * параметры аларма
   */
  IOptionSet params;

  @Override
  public IOptionSet params() {
    return params;
  }

  /**
   * Создает флакон по параметрам
   *
   * @param aParams параметры
   */
  public SkAlarmFlacon( IOptionSet aParams ) {
    super();
    params = aParams;
  }

  /**
   * Пустой констурктор для сериализации
   */
  public SkAlarmFlacon() {
    super();
  }
}
