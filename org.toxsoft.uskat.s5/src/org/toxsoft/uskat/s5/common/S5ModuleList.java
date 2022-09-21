package org.toxsoft.uskat.s5.common;

import static org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils.*;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Список {@link S5Module}
 *
 * @author mvk
 */
public final class S5ModuleList
    extends ElemArrayList<S5Module> {

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
          S5Module.KEEPER.writeColl( aSw, aEntity, true );
        }

        @Override
        protected S5ModuleList doRead( IStrioReader aSr ) {
          IListEdit<S5Module> ll = S5Module.KEEPER.readColl( aSr );
          return new S5ModuleList( ll );
        }
      };

  /**
   * Создает список
   *
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ModuleList() {
    // aAllowDuplicates = false
    super( DEFAULT_ARRAY_LIST_CAPACITY, false );
  }

  /**
   * Создает список
   *
   * @param aList {@link IList} - исходный список
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5ModuleList( IList<S5Module> aList ) {
    this();
    addAll( aList );
  }

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
