package org.toxsoft.uskat.s5.server.cluster;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;

import org.toxsoft.core.pas.tj.ITjObject;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.wildfly.clustering.dispatcher.Command;

/**
 * Транспорт команды кластера s5-сервера передаваемый узлам
 *
 * @author mvk
 */
class S5ClusterCommand
    implements IS5ClusterCommand, Command<String, S5ClusterManager> {

  private static final long serialVersionUID = 157157L;

  private static final String RET_VALUE_ID = "retValue"; //$NON-NLS-1$

  private final String method;
  private final String paramsString;

  private transient IStringMap<ITjValue> params;

  /**
   * Конструктор
   *
   * @param aMethod String имя команды
   * @param aParams {@link IStringMap} карта параметров команды.<br>
   *          Ключ: строковый идентификатор параметра;<br>
   *          Значение: значение параметра в формате {@link ITjValue}.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5ClusterCommand( String aMethod, IStringMap<ITjValue> aParams ) {
    TsNullArgumentRtException.checkNulls( aMethod, aParams );
    method = aMethod;
    paramsString = obj2str( mapToTjObj( aParams ) );
    params = null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterCommand
  //
  @Override
  public String method() {
    return method;
  }

  @Override
  public IStringMap<ITjValue> params() {
    if( params == null ) {
      params = str2obj( paramsString ).fields();
    }
    return params;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Command
  //
  @Override
  public String execute( S5ClusterManager aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    ITjValue retValue = aContext.handleCommand( this );
    ITjObject obj = TjUtils.createTjObject();
    obj.fields().put( RET_VALUE_ID, retValue );
    return TjUtils.obj2str( obj );
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает результат выполнения команды из текстового представления
   *
   * @param aResultString String текстовое представление результата
   * @return {@link ITjValue} результат выполнения команды
   * @throws TsNullArgumentRtException аргумент = null
   */
  static ITjValue resultFromString( String aResultString ) {
    TsNullArgumentRtException.checkNull( aResultString );
    ITjObject obj = TjUtils.str2obj( aResultString );
    return obj.fields().getByKey( RET_VALUE_ID );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Перенос карты значений в формат {@link ITjObject}
   *
   * @param aParams {@link IStringMap} карта значений
   * @return {@link ITjObject} карта значений в формате {@link ITjObject}
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static ITjObject mapToTjObj( IStringMap<ITjValue> aParams ) {
    TsNullArgumentRtException.checkNull( aParams );
    ITjObject paramsObj = createTjObject();
    paramsObj.fields().putAll( aParams );
    return paramsObj;
  }

}
