package org.toxsoft.uskat.s5.server.sequences.cluster;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5ClusterHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.sequences.impl.S5AbstractSequenceWriter;

/**
 * Обработчик команды кластера: всем узлам разблокировать доступ записи значений последовательностей в БД к указанным
 * данным
 *
 * @author mvk
 */
public final class S5ClusterCommandSequeneceUnlockGwids
    implements IS5ClusterCommandHandler {

  /**
   * Идентификатор команды вызова метода: {@link S5AbstractSequenceWriter#remoteUnlockGwids(IList)}
   */
  private final String REMOTE_UNLOCKS_GWIDS_METHOD;

  /**
   * Параметр команды: список идентификаторов данных
   * <p>
   * Тип: {@link IList}&lt;{@link Gwid}&gt;
   */
  private static final String GWIDS = "gwids"; //$NON-NLS-1$

  /**
   * Писатель последовательностей
   */
  private final S5AbstractSequenceWriter<?, ?> writer;

  /**
   * Журнал работы
   */
  @SuppressWarnings( "unused" )
  private final ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aFactoryId String идентификатор фабрики формирования последовательности значений
   * @param aWriter {@link S5AbstractSequenceWriter} писатель последовательности значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandSequeneceUnlockGwids( String aFactoryId, S5AbstractSequenceWriter<?, ?> aWriter ) {
    writer = TsNullArgumentRtException.checkNull( aWriter );
    REMOTE_UNLOCKS_GWIDS_METHOD = CLUSTER_METHOD_PREFIX + aFactoryId + ".remoteUnlockGwids"; //$NON-NLS-1$
  }

  /**
   * Возвращает имя метода команды
   *
   * @return String имя метода
   */
  public String method() {
    return REMOTE_UNLOCKS_GWIDS_METHOD;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных
   * @return {@link IS5ClusterCommand} созданная команда
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public IS5ClusterCommand createCommand( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return REMOTE_UNLOCKS_GWIDS_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        IStringMapEdit<ITjValue> params = new StringMap<>();
        params.put( GWIDS, TjUtils.createString( Gwid.KEEPER.coll2str( aGwids ) ) );
        return params;
      }
    };
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterCommandHandler
  //
  @Override
  public ITjValue handleClusterCommand( IS5ClusterCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    if( !aCommand.method().equals( REMOTE_UNLOCKS_GWIDS_METHOD ) ) {
      return TjUtils.NULL;
    }
    IGwidList gwids = new GwidList( Gwid.KEEPER.str2coll( aCommand.params().getByKey( GWIDS ).asString() ) );
    boolean result = writer.remoteUnlockGwids( gwids );
    return (result ? TjUtils.TRUE : TjUtils.FALSE);
  }
}
