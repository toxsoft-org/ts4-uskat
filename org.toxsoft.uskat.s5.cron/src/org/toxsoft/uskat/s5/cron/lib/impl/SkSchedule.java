package org.toxsoft.uskat.s5.cron.lib.impl;

import static org.toxsoft.uskat.s5.cron.lib.ISkCronHardConstants.*;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.gw.ugwi.IUgwiList;
import org.toxsoft.core.tslib.gw.ugwi.UgwiList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.objserv.ISkObjectCreator;
import org.toxsoft.uskat.core.impl.SkObject;
import org.toxsoft.uskat.s5.cron.lib.ISkSchedule;

/**
 * Реализация {@link ISkSchedule}
 *
 * @author mvk
 */
class SkSchedule
    extends SkObject
    implements ISkSchedule {

  /**
   * The creator singleton.
   */
  static final ISkObjectCreator<SkSchedule> CREATOR = SkSchedule::new;

  /**
   * Конструктор.
   *
   * @param aSkid {@link Skid} - идентификатор объекта
   */
  SkSchedule( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkSchedule
  //
  @Override
  public String seconds() {
    return attrs().getValue( ATRID_SECONDS ).asString();
  }

  @Override
  public String minutes() {
    return attrs().getValue( ATRID_MINUTES ).asString();
  }

  @Override
  public String hours() {
    return attrs().getValue( ATRID_HOURS ).asString();
  }

  @Override
  public String daysOfMonth() {
    return attrs().getValue( ATRID_DAYS_OF_MONTH ).asString();
  }

  @Override
  public String months() {
    return attrs().getValue( ATRID_MONTHS ).asString();
  }

  @Override
  public String daysOfWeek() {
    return attrs().getValue( ATRID_DAYS_OF_WEEK ).asString();
  }

  @Override
  public String years() {
    return attrs().getValue( ATRID_YEARS ).asString();
  }

  @Override
  public String timezone() {
    return attrs().getValue( ATRID_TIMEZONE ).asString();
  }

  @Override
  public long start() {
    return attrs().getValue( ATRID_START ).asLong();
  }

  @Override
  public long end() {
    return attrs().getValue( ATRID_END ).asLong();
  }

  @Override
  public IUgwiList ugwis() {
    return UgwiList.KEEPER.str2ent( getClob( CLBID_UGWIS, UgwiList.KEEPER.ent2str( IUgwiList.EMPTY ) ) );
  }

  @Override
  public ITimedList<SkEvent> getHistory( IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    Gwid gwid = Gwid.createEvent( classId(), strid(), Gwid.STR_MULTI_ID );
    return coreApi().eventService().queryObjEvents( aInterval, gwid );
  }

}
