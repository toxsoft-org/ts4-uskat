package org.toxsoft.uskat.dataquality.s5.supports;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.dataquality.lib.ISkDataQualityTicket;
import org.toxsoft.uskat.dataquality.lib.impl.SkDataQualityTicket;

/**
 * Список описаний тикетов {@link ISkDataQualityTicket}
 *
 * @author mvk
 */
public final class S5DataQualityTicketList
    extends StridablesList<ISkDataQualityTicket> {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5DataQualityTicketList"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5DataQualityTicketList> KEEPER =
      new AbstractEntityKeeper<>( S5DataQualityTicketList.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5DataQualityTicketList aEntity ) {
          SkDataQualityTicket.KEEPER.writeColl( aSw, aEntity, false );
        }

        @Override
        protected S5DataQualityTicketList doRead( IStrioReader aSr ) {
          S5DataQualityTicketList retValue = new S5DataQualityTicketList();
          SkDataQualityTicket.KEEPER.readColl( aSr, retValue );
          return retValue;
        }
      };

  /**
   * Создает пустой список.
   */
  public S5DataQualityTicketList() {
    super();
  }

  /**
   * Создает список с начальным содержимым набора или массива aElems.
   *
   * @param aElems E... - элементы списка (набор или массив)
   * @throws TsNullArgumentRtException любой элемент = null
   */
  public S5DataQualityTicketList( ISkDataQualityTicket... aElems ) {
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
      sb.append( IStrioHardConstants.CHAR_SPACE );
      sb.append( IStrioHardConstants.CHAR_SPACE );
      sb.append( IStrioHardConstants.CHAR_SPACE );
    }
    for( int index = 0, n = size(); index < n; index++ ) {
      sb.append( get( index ).toString() );
      if( index + 1 < n ) {
        sb.append( IStrioHardConstants.CHAR_EOL );
        sb.append( IStrioHardConstants.CHAR_SPACE );
        sb.append( IStrioHardConstants.CHAR_SPACE );
        sb.append( IStrioHardConstants.CHAR_SPACE );
      }
    }
    return sb.toString();
  }
}
