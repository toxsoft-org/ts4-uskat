package org.toxsoft.uskat.sysext.alarms.impl;

import static org.toxsoft.uskat.sysext.alarms.impl.ISkResources.*;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByAuthorObjId;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByLevel;

/**
 * Реализация {@link ISkAlarmFilterByLevel}.
 *
 * @author goga
 */
public class SkAlarmFilterByAuthorObjId
    extends AbstractSingleFilter
    implements ISkAlarmFilterByAuthorObjId {

  private static final long serialVersionUID = 157157L;

  /**
   * Экземпляр-синглтон фабрики фильтра.
   */
  public static final ISingleFilterFactory FACTORY = new AbstractSingleFilterFactory( FILTER_ID,
      STR_N_FILTER_BY_AUTHOR_OBJ_ID, STR_D_FILTER_BY_AUTHOR_OBJ_ID, AUTHOR_ID_CONST ) {

    private static final long serialVersionUID = 157157L;

    @Override
    protected ISingleFilter doCreateFilter( ISingleFilterParams aFilterInfo ) {
      return new SkAlarmFilterByAuthorObjId( aFilterInfo );
    }

  };

  /**
   * Конструктор из параметров единичного фильтра {@link ISingleFilterParams}.
   *
   * @param aParams {@link ISingleFilterParams} - параметры фильтра
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентифкатор фильтра аргумента не равен {@link IStdStridFilter#FILTER_ID}
   */
  public SkAlarmFilterByAuthorObjId( ISingleFilterParams aParams ) {
    super( aParams );
    TsIllegalArgumentRtException.checkFalse( aParams.typeId().equals( FILTER_ID ) );
  }

  /**
   * Конструктор копирования.
   *
   * @param aSource {@link ISkAlarmFilterByLevel} - источник
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентифкатор фильтра аргумента не равен {@link IStdStridFilter#FILTER_ID}
   */
  public SkAlarmFilterByAuthorObjId( ISkAlarmFilterByLevel aSource ) {
    this( TsNullArgumentRtException.checkNull( aSource ).params() );
    params().params().replaceOpSet( aSource.params().params() );
  }

  @Override
  public boolean accept( ISkAlarm aElement ) {
    if( aElement == null ) {
      throw new TsNullArgumentRtException();
    }
    return aElement.authorId().equals( authorId() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkAlarmFilterByLevel
  //

  @Override
  public Skid authorId() {
    return AUTHOR_ID_CONST.getValue( params().params() ).asValobj();
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return getClass().getSimpleName() + ":  authorObjId == " + authorId();
  }

}
