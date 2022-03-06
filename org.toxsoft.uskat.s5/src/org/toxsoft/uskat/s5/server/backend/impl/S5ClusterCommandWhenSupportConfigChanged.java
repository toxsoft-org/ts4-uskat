package org.toxsoft.uskat.s5.server.backend.impl;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5ClusterHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;

/**
 * Обработчик команды кластера: всем узлам сформировать событие {@link S5SingletonBase#onConfigChanged}
 *
 * @author mvk
 */
abstract class S5ClusterCommandWhenSupportConfigChanged
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link S5SingletonBase#onConfigChanged}
   */
  public static final String WHEN_SUPPORT_CONFIG_CHANGED_METHOD = CLUSTER_METHOD_PREFIX + "whenSupportConfigChanged"; //$NON-NLS-1$

  /**
   * Предыдущая конфигурация
   */
  private static final String PREV_CONFIG = "prevConfig"; //$NON-NLS-1$

  /**
   * Новая конфигурация
   */
  private static final String NEW_CONFIG = "newConfig"; //$NON-NLS-1$

  /**
   * Журнал работы
   */
  // private final ILogger logger = l4jLogger( getClass() );

  /**
   * Конструктор
   */
  public S5ClusterCommandWhenSupportConfigChanged() {
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @param aPrevConfig {@link IOptionSet} предыдущая конфигурация синглетона поддержки расширения бекенда
   * @param aNewConfig {@link IOptionSet} новая конфигурация синглетона поддержки расширения бекенда
   * @return {@link IS5ClusterCommand} созданная команда
   */
  public static IS5ClusterCommand whenSupportConfigChangedCommand( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
    TsNullArgumentRtException.checkNulls( aPrevConfig, aNewConfig );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return WHEN_SUPPORT_CONFIG_CHANGED_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        IStringMapEdit<ITjValue> params = new StringMap<>();
        params.put( PREV_CONFIG, createString( OptionSetKeeper.KEEPER.ent2str( aPrevConfig ) ) );
        params.put( NEW_CONFIG, createString( OptionSetKeeper.KEEPER.ent2str( aNewConfig ) ) );
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
    if( !aCommand.method().equals( WHEN_SUPPORT_CONFIG_CHANGED_METHOD ) ) {
      return TjUtils.NULL;
    }
    IOptionSet prevConfig = OptionSetKeeper.KEEPER.str2ent( aCommand.params().getByKey( PREV_CONFIG ).asString() );
    IOptionSet newConfig = OptionSetKeeper.KEEPER.str2ent( aCommand.params().getByKey( NEW_CONFIG ).asString() );
    doWhenSupportConfigChanged( prevConfig, newConfig );
    return TjUtils.TRUE;
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Вызывается при получении события: изменилась конфигурация поддержки расширения бекенда
   *
   * @param aPrevConfig {@link IOptionSet} предыдущая конфигурация
   * @param aNewConfig {@link IOptionSet} новая конфигурация
   */
  protected abstract void doWhenSupportConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig );
}
