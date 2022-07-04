package org.toxsoft.uskat.s5.legacy;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntLinkedBundleList;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Хранитель объектов типа {@link IIntList}.
 * <p>
 * Считанное зхначение можно безопасно приводить к {@link ILongListEdit}.
 *
 * @author hazard157
 */
public class IntListKeeper
    extends AbstractEntityKeeper<IIntList> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<IIntList> KEEPER = new IntListKeeper();

  /**
   * Текстовое представление пустого списка.
   */
  public static String EMPTY_LIST = KEEPER.ent2str( IIntList.EMPTY );

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "IntList"; //$NON-NLS-1$

  private IntListKeeper() {
    super( IIntList.class, EEncloseMode.ENCLOSES_KEEPER_IMPLEMENTATION, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //

  @Override
  protected void doWrite( IStrioWriter aSw, IIntList aEntity ) {
    aSw.writeChar( CHAR_ARRAY_BEGIN );
    for( int i = 0, n = aEntity.size(); i < n; i++ ) {
      aSw.writeLong( aEntity.getValue( i ) );
      if( i < n - 1 ) {
        aSw.writeSeparatorChar();
      }
    }
    aSw.writeChar( CHAR_ARRAY_END );
  }

  @Override
  protected IIntList doRead( IStrioReader aSr ) {
    IIntListEdit result = new IntLinkedBundleList();
    if( aSr.readArrayBegin() ) {
      do {
        result.add( aSr.readInt() );
      } while( aSr.readArrayNext() );
    }
    return result;
  }

}
