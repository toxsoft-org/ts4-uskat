package org.toxsoft.uskat.s5.utils.collections;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Карта у которой ключом и значением являются идентификаторы объектов {@link Skid}
 *
 * @author mvk
 */
public class SkidMap
    extends ElemMap<Skid, Skid>
    implements ISkidMap {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "skidsBySkids"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<ISkidMap> KEEPER =
      new AbstractEntityKeeper<>( ISkidMap.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkidMap aEntity ) {
          aSw.writeChar( CHAR_SET_BEGIN );
          for( int i = 0, count = aEntity.size(); i < count; i++ ) {
            Skid key = aEntity.keys().get( i );
            Skid.KEEPER.write( aSw, key );
            aSw.writeSeparatorChar();
            Skid.KEEPER.write( aSw, aEntity.getByKey( key ) );
            if( i < count - 1 ) {
              aSw.writeSeparatorChar();
            }
          }
          aSw.writeChar( CHAR_SET_END );
        }

        @Override
        protected ISkidMap doRead( IStrioReader sr ) {
          SkidMap retValue = new SkidMap();
          if( sr.readSetBegin() ) {
            do {
              Skid key = Skid.KEEPER.read( sr );
              sr.ensureSeparatorChar();
              retValue.put( key, Skid.KEEPER.read( sr ) );
            } while( sr.readSetNext() );
          }
          return retValue;
        }
      };

  /**
   * Конструктор, создающий карту с емкостью хеш-таблицы по умолчанию.
   */
  public SkidMap() {
    super();
  }

  /**
   * Конструктор копирования.
   *
   * @param aSrc {@link IMap}&lt; {@link Skid}, {@link Skid}&gt; - карта с добавляемыми элементами
   */
  public SkidMap( IMap<Skid, Skid> aSrc ) {
    super();
    putAll( aSrc );
  }

}
