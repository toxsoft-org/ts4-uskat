package org.toxsoft.uskat.classes;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Сессия соединения ползователя с системой Skat.
 * <p>
 * При каждом установлении соединения {@link ISkConnection} создается новый экземпляр этого объекта. При завершении
 * соединения, работа с объектом завершается, и он остается в системе для исключительно для нужд отчетности и
 * статистики.
 * <p>
 * TODO где-то желательно иметь метод {@link IList}&lt;{@link ISkSession}&gt; listCurrentSessions() который перечисляет
 * открытые (может и неактивные) сессии. Кончено, можно получить список <b>всех</b> объектов методом
 * {@link ISkObjectService#listObjs(String, boolean)}, но это будет слишком ресурсоемко - там будем масса объектов
 * сессии, которые давно завершились.
 *
 * @author hazard157
 */
public interface ISkSession
    extends ISkObject {

  /**
   * The {@link ISkSession} class identifier.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".Session"; //$NON-NLS-1$

  /**
   * Идентификатор атирбута "Момент времени устаноления соединения (создания сессии)" {@link #startTime()}.
   */
  String ATRID_STARTTIME = "StartTime"; //$NON-NLS-1$

  /**
   * Идентификатор атирбута "Момент времени закрытия соединения (сессии)" {@link #endTime()}.
   * <p>
   * Пока сессия открыта аторибут имеет значение {@link IAtomicValue#NULL}.
   */
  String ATRID_ENDTIME = "EndTime"; //$NON-NLS-1$

  /**
   * Идентификатор атрибута "Специфичные для бекенда параметры".
   */
  String ATRID_BACKEND_SPECIFIC_PARAMS = "BackendSpecificParams"; //$NON-NLS-1$

  /**
   * Идентификатор атрибута "Параметры создания соединения".
   */
  String ATRID_CONNECTION_CREATION_PARAMS = "ConnectionCreationParams"; //$NON-NLS-1$

  /**
   * Идентификатор связи "Пользователь, который вошел в систему" {@link #getUser()}.
   */
  String LNKID_USER = "lnkUser"; //$NON-NLS-1$

  /**
   * Идентификатор РВ-данного "Состояние соединения"
   */
  String RTDID_STATE = "rtdState"; //$NON-NLS-1$

  /**
   * Returns the connection start time.
   *
   * @return long - the connection start time (millisecons after epoch)
   */
  default long startTime() {
    return attrs().getTime( ATRID_STARTTIME );
  }

  /**
   * Returns the connection end time.
   *
   * @return long - the connection close time (millisecons after epoch)
   */
  default long endTime() {
    return attrs().getTime( ATRID_ENDTIME );
  }

  /**
   * Вовзращает параметры сессии, спцифичные для бекенда.
   *
   * @return {@link IOptionSet} - бекенд-специфичные параметры
   */
  default IOptionSet backendSpecificParams() {
    return attrs().getValobj( ATRID_BACKEND_SPECIFIC_PARAMS );
  }

  /**
   * Вовзращает параметры создания соединения, заданные в {@link ISkConnection#open(ITsContextRo)}.
   * <p>
   * В возвращаемом наборе отсутствует информация о пароле, с которым вошел пользователь.
   *
   * @return {@link IOptionSet} - параметры создания соединения
   */
  default IOptionSet connectionCreationParams() {
    return attrs().getValobj( ATRID_CONNECTION_CREATION_PARAMS );
  }

  /**
   * Возвращает пользователя, который открыл сессию.
   *
   * @return {@link ISkUser} - пользователь
   */
  default ISkUser getUser() {
    return getSingleLinkObj( LNKID_USER );
  }

  /**
   * Возвращает состояние соединения.
   *
   * @return {@link ESkConnState} - состояние соединения
   */
  // default ESkConnState state() {
  // // TODO этот код а) должен переехать в ISkObject, б) плха практика (надо канал создавать не каждый, а один раз)
  // ISkReadCurrDataChannel channel = null;
  // try {
  // IGwidList gwids = new GwidList( Gwid.createRtdata( classId(), strid(), RTDID_STATE ) );
  // IMap<Gwid, ISkReadCurrDataChannel> map = coreApi().rtDataService().createReadCurrDataChannels( gwids );
  // channel = map.values().first();
  // return channel.getValue().asValobj();
  // }
  // finally {
  // if( channel != null ) {
  // channel.close();
  // }
  // }
  // }

}
