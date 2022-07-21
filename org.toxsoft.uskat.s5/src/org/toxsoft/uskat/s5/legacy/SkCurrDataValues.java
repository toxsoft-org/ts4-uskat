package org.toxsoft.uskat.s5.legacy;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AtomicValueKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IIntList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Карта значений текущих данных
 * <p>
 * Ключ: целочисленный индекс текущего данного в системе; Значение: атомарное значение текущего данного
 * {@link IAtomicValue}
 *
 * @author mvk
 */
public final class SkCurrDataValues
    extends ElemMap<Gwid, IAtomicValue> {

  private static final long serialVersionUID = 4820728049991582042L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SkCurrDataValues"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<SkCurrDataValues> KEEPER =
      new AbstractEntityKeeper<>( SkCurrDataValues.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, SkCurrDataValues aEntity ) {
          GwidList.KEEPER.write( aSw, new GwidList( aEntity.keys() ) );
          AtomicValueKeeper.KEEPER.writeColl( aSw, aEntity.values(), false );
        }

        @Override
        protected SkCurrDataValues doRead( IStrioReader aSr ) {
          IIntList indexes = IntListKeeper.KEEPER.read( aSr );
          IList<IAtomicValue> values = AtomicValueKeeper.KEEPER.readColl( aSr );
          SkCurrDataValues retValue = new SkCurrDataValues();
          for( int index = 0, n = indexes.size(); index < n; index++ ) {
            retValue.put( indexes.get( index ), values.get( index ) );
          }
          return retValue;
        }
      };

}
