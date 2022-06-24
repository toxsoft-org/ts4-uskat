package org.toxsoft.uskat.core.api.users;

/**
 * Localizable resources.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link ISkUserServiceHardConstants}
   */
  String STR_N_USER         = "Пользователь";
  String STR_D_USER         = "Пользователь или программный модуль, имеющий право входа в систему";
  String STR_N_PASSORD      = "Пароль";
  String STR_D_PASSORD      = "Пароль пользователя (точнее, хеш-код для проверки)";
  String STR_N_ROLES        = "Роли";
  String STR_D_ROLES        = "Роли, под которым пользователь может войти в систему";
  String STR_N_USER_ENABLED = "Разрешен?";
  String STR_D_USER_ENABLED = "Признак разрешения пользователя на вход в систему";
  String STR_N_USER_HIDDEN  = "Скрыт?";
  String STR_D_USER_HIDDEN  = "Признак скрытия пользоватея в списках обычного администрирования";
  String STR_N_ROLE_ENABLED = "Разрешен?";
  String STR_D_ROLE_ENABLED = "Признак разрешения входа в систему с этой ролью";
  String STR_N_ROLE_HIDDEN  = "Скрыт?";
  String STR_D_ROLE_HIDDEN  = "Признак скрытия пользоватея в списках обычного администрирования";

}
