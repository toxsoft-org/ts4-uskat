package org.toxsoft.uskat.s5.server.cluster;

import javax.ejb.Local;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.wildfly.clustering.group.Group;

/**
 * Менеджер кластера s5-сервера
 *
 * @author mvk
 */
@Local
public interface IS5ClusterManager {

  /**
   * Возвращает группу кластера в которой работает локальный узел
   *
   * @return {@link Group} группа кластера
   */
  Group group();

  // ------------------------------------------------------------------------------------
  // Передача уведомлений узлам кластера
  //
  /**
   * Синхронная передача команды узлам кластера
   * <p>
   * Слушатели получают сообщения через зарегистрированные обработчики уведомлений. Если aRemoteOnly = true, то
   * уведомления получают только обработчики удаленных (не локального) узлов кластера.
   *
   * @param aCommand {@link IS5ClusterCommand} команда для узлов кластера
   * @param aRemoteOnly boolean <b>true</b> отправлять только удаленным узлам;<b>false</b> отправлять всем
   * @param aPrimaryOnly boolean <b>true</b> отправлять только первичному узлу;<b>false</b> отправлять всем
   * @return {@link IStringMap}&lt;{@link ITjValue}&gt; список результатов полученных от узлов кластера.
   *         <p>
   *         Ключ: идентификатор узла кластера;<br>
   *         Значение: {@link ITjValue} результат выполнения команды на узле кластера. {@link TjUtils#NULL}: команда не
   *         выполнена.
   * @throws TsNullArgumentRtException аргумент = null
   */
  IStringMap<ITjValue> sendSyncCommand( IS5ClusterCommand aCommand, boolean aRemoteOnly, boolean aPrimaryOnly );

  /**
   * Асинхронная передача команды узлам кластера
   * <p>
   * Слушатели получают сообщения через зарегистрированные обработчики уведомлений. Если aRemoteOnly = true, то
   * уведомления получают только обработчики удаленных (не локального) узлов кластера.
   *
   * @param aCommand {@link IS5ClusterCommand} команда для узлов кластера
   * @param aRemoteOnly boolean <b>true</b> отправлять только удаленным узлам;<b>false</b> отправлять всем
   * @param aPrimaryOnly boolean <b>true</b> отправлять только первичному узлу;<b>false</b> отправлять всем
   * @throws TsNullArgumentRtException аргумент = null
   */
  void sendAsyncCommand( IS5ClusterCommand aCommand, boolean aRemoteOnly, boolean aPrimaryOnly );

  /**
   * Добавляет обработчика команд для указанного метода
   * <p>
   * Если для метода уже существует обработчик, то он добавляется в список уже имеющихся. При получении уведомления
   * будут вызваны все обработчики из списка (в порядке добавления) пока кто-нибудь из них не вернет
   * {@link IS5ClusterCommandHandler#handleClusterCommand(IS5ClusterCommand)} == <b>true</b>.
   *
   * @param aMethod String метод уведомления
   * @param aCommandHandler {@link IS5ClusterCommandHandler} обработчик команд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addCommandHandler( String aMethod, IS5ClusterCommandHandler aCommandHandler );

  /**
   * Удаляет обработчика команд для указанного метода
   * <p>
   * Если для метода этот обработчик не зарегистрирован, то ничего не делает.
   *
   * @param aMethod String метод уведомления
   * @param aCommandHandler {@link IS5ClusterCommandHandler} обработчик команд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void removeCommandHandler( String aMethod, IS5ClusterCommandHandler aCommandHandler );

  // ------------------------------------------------------------------------------------
  // Оповещения
  //
  /**
   * Добавляет слушателя событий кластера
   * <p>
   * Если слушатель уже зарегистрирован, то ничего не делает
   *
   * @param aListener {@link IS5ClusterListener} слушатель событий
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addClusterListener( IS5ClusterListener aListener );

  /**
   * Удаляет слушателя событий кластера
   * <p>
   * Если слушатель незарегистрирован, то ничего не делает
   *
   * @param aListener {@link IS5ClusterListener} слушатель событий
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeClusterListener( IS5ClusterListener aListener );
}
