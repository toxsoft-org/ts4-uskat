package org.toxsoft.uskat.s5.server.statistics;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.basis.ITsCollection;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;

/**
 * Список интервалов статистической обработки
 *
 * @author mvk
 */
public class S5StatisticIntervalList
    extends StridablesList<IS5StatisticInterval> {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5StatisticIntervalList"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5StatisticIntervalList> KEEPER =
      new AbstractEntityKeeper<>( S5StatisticIntervalList.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5StatisticIntervalList aEntity ) {
          EStatisticInterval.KEEPER.writeColl( aSw, aEntity, false );
        }

        @Override
        protected S5StatisticIntervalList doRead( IStrioReader aSr ) {
          S5StatisticIntervalList retValue = new S5StatisticIntervalList();
          EStatisticInterval.KEEPER.readColl( aSr, retValue );
          return retValue;
        }
      };

  /**
   * Создает пустой список.
   */
  public S5StatisticIntervalList() {
    super();
  }

  /**
   * Создает список с начальным содержимым набора или массива aElems.
   *
   * @param aList ITsReferenceCollection - элементы списка (набор или массив)
   * @throws TsNullArgumentRtException любой элемент = null
   */
  public S5StatisticIntervalList( ITsCollection<IS5StatisticInterval> aList ) {
    super( aList );
  }

  /**
   * Создает список с начальным содержимым набора или массива aElems.
   *
   * @param aElems E... - элементы списка (набор или массив)
   * @throws TsNullArgumentRtException любой элемент = null
   */
  public S5StatisticIntervalList( IS5StatisticInterval... aElems ) {
    super( aElems );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if( size() > 1 ) {
      sb.append( IStrioHardConstants.CHAR_EOL );
    }
    for( int index = 0, n = size(); index < n; index++ ) {
      sb.append( get( index ).toString() );
      if( index + 1 < n ) {
        sb.append( IStrioHardConstants.CHAR_EOL );
      }
    }
    return sb.toString();
  }

}
