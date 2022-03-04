package org.toxsoft.uskat.s5.client.remote.connection;

/**
 * Информация об узле кластера
 *
 * @author mvk
 */
public interface IS5ClusterNodeInfo {

  /**
   * Возвращает имя кластера в который входит узел
   *
   * @return String имя кластера
   */
  String clusterName();

  /**
   * Возвращает имя узла кластера
   *
   * @return String имя узла
   */
  String nodeName();

  /**
   * Возвращает сетевое имя или IP-адрес узла кластера
   *
   * @return String адрес узла
   */
  String address();

  /**
   * Возвращает порт узла кластера
   *
   * @return int порт узла
   */
  int port();
}
