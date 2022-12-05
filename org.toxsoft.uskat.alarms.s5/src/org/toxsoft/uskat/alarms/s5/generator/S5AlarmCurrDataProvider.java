package org.toxsoft.uskat.alarms.s5.generator;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Поставщик текущих значений
 *
 * @author mvk
 */
public class S5AlarmCurrDataProvider
    extends S5AbstractAlarmDataProvider
    implements ISkCurrDataChangeListener {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор поставщика текущих данных
   */
  public static final String ALARM_CURRDATA_PROVIDER = "alarm.currdata"; //$NON-NLS-1$

  /**
   * Служба объектов системы
   */
  private final ISkRtdataService rtdataService;

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
   * @param aCurrDataService {@link ISkRtdataService} служба реальных данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5AlarmCurrDataProvider( ISkRtdataService aCurrDataService ) {
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
    Gwid gwid = Gwid.createRtdata( aObjId.classId(), aObjId.strid(), aDataId );
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
  public void onCurrData( IMap<Gwid, IAtomicValue> aNewValues ) {
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
