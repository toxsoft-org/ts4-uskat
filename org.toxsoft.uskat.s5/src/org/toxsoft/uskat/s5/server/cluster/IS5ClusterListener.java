package org.toxsoft.uskat.s5.server.cluster;

import javax.ejb.Local;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.wildfly.clustering.group.Membership;
import org.wildfly.clustering.group.Node;

/**
 * Слушатель событий кластера s5-сервера
 *
 * @author mvk
 */
@Local
public interface IS5ClusterListener {

  /**
   * Оповещение об изменении координатора в кластере.
   *
   * @param aPrevCoordinator {@link Node} узел предыдущего координатора
   * @param aNewCoordinator {@link Node} узел нового координатора
   * @param aMerged boolean <br>
   *          <b>true</b> смена координатора вызвана процессом merge;<br>
   *          <b>false</b> изменения не связаны с процессом merge.
   */
  default void coordinatorChanged( Node aPrevCoordinator, Node aNewCoordinator, boolean aMerged ) {
    // nop
  }

  /**
   * Оповещение об изменении в кластере.
   *
   * @param aPrevMembership {@link Membership} предыдущее состояние группы
   * @param aNewMembership {@link Membership} новое состояние группы
   * @param aMerged boolean <br>
   *          <b>true</b> изменения вызваны процессом merge;<br>
   *          <b>false</b> изменения не связаны с процессом merge.
   */
  default void membershipChanged( Membership aPrevMembership, Membership aNewMembership, boolean aMerged ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов слушателей событий {@link IS5ClusterListener#coordinatorChanged(Node, Node, boolean)}
   *
   * @param aListeners {@link Iterable}&lt;{@link IS5ClusterListener}&gt; список слушателей
   * @param aPrevCoordinator {@link Node} узел предыдущего координатора.
   * @param aNewCoordinator {@link Node} узел нового координатора
   * @param aMerged boolean <br>
   *          <b>true</b> смена координатора вызвана процессом merge;<br>
   *          <b>false</b> изменения не связаны с процессом merge.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callCoordinatorChanged( Iterable<IS5ClusterListener> aListeners, Node aPrevCoordinator,
      Node aNewCoordinator, boolean aMerged ) {
    TsNullArgumentRtException.checkNulls( aListeners, aNewCoordinator );
    for( IS5ClusterListener listener : aListeners ) {
      listener.coordinatorChanged( aPrevCoordinator, aNewCoordinator, aMerged );
    }
  }

  /**
   * Вызов слушателей событий {@link IS5ClusterListener#membershipChanged(Membership, Membership, boolean)}
   *
   * @param aListeners {@link Iterable}&lt;{@link IS5ClusterListener}&gt; список слушателей
   * @param aPrevMembership {@link Membership} предыдущее состояние группы
   * @param aNewMembership {@link Membership} новое состояние группы
   * @param aMerged boolean <br>
   *          <b>true</b> изменения вызваны процессом merge;<br>
   *          <b>false</b> изменения не связаны с процессом merge.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callMembershipChanged( Iterable<IS5ClusterListener> aListeners, Membership aPrevMembership,
      Membership aNewMembership, boolean aMerged ) {
    TsNullArgumentRtException.checkNulls( aListeners, aPrevMembership, aNewMembership );
    for( IS5ClusterListener listener : aListeners ) {
      listener.membershipChanged( aPrevMembership, aNewMembership, aMerged );
    }
  }
}
