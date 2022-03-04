package org.toxsoft.uskat.s5.common;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Список {@link S5Module}
 *
 * @author mvk
 */
public final class S5ModuleList
    extends ElemLinkedList<S5Module> {

  private static final long serialVersionUID = 157157L;

  /**
   * Value-object registration identifier for {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "ModuleList"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<S5ModuleList> KEEPER =
      new AbstractEntityKeeper<>( S5ModuleList.class, EEncloseMode.ENCLOSES_KEEPER_IMPLEMENTATION, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5ModuleList aEntity ) {
          aSw.writeChar( CHAR_SET_BEGIN );
          aSw.writeInt( aEntity.size() );
          aSw.writeChar( CHAR_ARRAY_BEGIN );
          for( int i = 0, n = aEntity.size(); i < n; i++ ) {
            S5Module v = aEntity.get( i );
            S5Module.KEEPER.write( aSw, v );
            if( i < n - 1 ) {
              aSw.writeChar( CHAR_ITEM_SEPARATOR );
            }
            aSw.writeEol();
          }
          aSw.writeChar( CHAR_ARRAY_END );
          aSw.writeChar( CHAR_SET_END );
        }

        @Override
        protected S5ModuleList doRead( IStrioReader aSr ) {
          aSr.ensureChar( CHAR_SET_BEGIN );
          S5ModuleList result = new S5ModuleList();
          if( aSr.readArrayBegin() ) {
            do {
              result.add( S5Module.KEEPER.read( aSr ) );
            } while( aSr.readArrayNext() );
          }
          aSr.ensureChar( CHAR_SET_END );
          return result;
        }
      };

  /**
   * Возвращает текствое представление коллекции описания модулей
   *
   * @param aModules {@link S5ModuleList} коллекция модулей
   * @param aCount int максимальное количество модулей выводимое в результат
   * @return String текстовое представление узлов
   */
  public static String modulesToString( S5ModuleList aModules, int aCount ) {
    TsNullArgumentRtException.checkNull( aModules );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aModules.size(); index < n; index++ ) {
      sb.append( aModules.get( index ) );
      if( index + 1 >= aCount ) {
        if( index + 1 < n ) {
          sb.append( ',' );
        }
        sb.append( "...[" + (n - aCount) + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
        break;
      }
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    return sb.toString();
  }
}
