package org.toxsoft.uskat.sysext.alarms.api.generator.currdata;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.impl.SkAbstractAlarmDataProvider;

import ru.uskat.core.api.rtdata.*;

/**
 * Поставщик текущих значений
 *
 * @author mvk
 */
public class SkAlarmCurrDataProvider
    extends SkAbstractAlarmDataProvider
    implements ISkCurrDataChangeListener {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор поставщика текущих данных
   */
  public static final String ALARM_CURRDATA_PROVIDER = "alarm.currdata"; //$NON-NLS-1$

  /**
   * Служба объектов системы
   */
  private final ISkRtDataService rtdataService;

  /**
   * Карта текущих данных поставщика
   * <p>
   * Ключ: идентификатор данного;<br>
   * Значение: канал {@link ISkReadCurrDataChannel} чтения значений
   */
  private final IMapEdit<Gwid, ISkReadCurrDataChannel> currdata = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aCurrDataService {@link ISkRtDataService} служба реальных данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkAlarmCurrDataProvider( ISkRtDataService aCurrDataService ) {
    super( ALARM_CURRDATA_PROVIDER );
    TsNullArgumentRtException.checkNull( aCurrDataService );
    rtdataService = aCurrDataService;
    rtdataService.eventer().addListener( this );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает значение текущего данного
   *
   * @param aObjId long идентификатор объекта
   * @param aDataId String строковый идентификатор данного
   * @return {@link IAtomicValue} значение текущего данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException не существющее данное
   */
  IAtomicValue getCurrDataValue( Skid aObjId, String aDataId ) {
    TsNullArgumentRtException.checkNull( aDataId );
    Gwid gwid = Gwid.create( aObjId.classId(), aObjId.strid(), aDataId, null, null, null );
    ISkReadCurrDataChannel channel = currdata.findByKey( gwid );
    if( channel == null ) {
      channel = rtdataService.createReadCurrDataChannels( new GwidList( gwid ) ).values().first();
      currdata.put( gwid, channel );
      update();
    }
    return channel.getValue();
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkCurrDataChangeListener
  //

  @Override
  public void onCurrData( IMap<Gwid, ISkReadCurrDataChannel> aRtdMap ) {
    update();
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICloseable
  //
  @Override
  public void close() {
    for( ISkReadCurrDataChannel channel : currdata.values() ) {
      channel.close();
    }
    currdata.clear();
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Синхронизация данных поставщика с текущими данными набора
   */
  private void update() {
    fireUpdateEvent();
  }
}
