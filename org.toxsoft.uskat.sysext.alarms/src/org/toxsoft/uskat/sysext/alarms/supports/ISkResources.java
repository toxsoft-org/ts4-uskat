package org.toxsoft.uskat.sysext.alarms.supports;

/**
 * Локализуемые ресурсы.
 *
 * @author mvk
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface ISkResources {

  String STR_D_BACKEND_ALARMS         = "Поддержка расширения бекенда для службы тревог ISkAlarmsSerivce";
  String ERR_FORCE_UNREG_LISTENER = "Подписчик %s принудительно отключается от службы. Причина: %s";
  String ERR_ROLLEDBACK_LISTENER  =
      "Подписчик %s отказался обрабатывать аларм потому что проводится отмена транзакции. Причина: %s";

}
