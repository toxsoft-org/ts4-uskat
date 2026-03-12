package org.toxsoft.uskat.skadmin.cli.parsers;

import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdLexicalParser.*;

/**
 * Константы, локализуемые ресурсы парсеров консоли.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminResources {

  /**
   * Формат представления даты-времени в строковом представлении
   */
  String DATETIME_FORMAT = Messages.getString( "DATETIME_FORMAT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // AdminCmdParser
  //
  String ERR_MSG_NOT_INITED                 = Messages.getString( "ERR_MSG_NOT_INITED" );                 //$NON-NLS-1$
  String ERR_MSG_WRONG_FORMAT               = Messages.getString( "ERR_MSG_WRONG_FORMAT" );               //$NON-NLS-1$
  String ERR_MSG_NOT_CANONIC                = Messages.getString( "ERR_MSG_NOT_CANONIC" );                //$NON-NLS-1$
  String ERR_MSG_CMD_NOT_FOUND              = Messages.getString( "ERR_MSG_CMD_NOT_FOUND" );              //$NON-NLS-1$
  String ERR_MSG_INVALID_CMD_ID             = Messages.getString( "ERR_MSG_INVALID_CMD_ID" );             //$NON-NLS-1$
  String ERR_MSG_ARG_ID_EXPECTED            = Messages.getString( "ERR_MSG_ARG_ID_EXPECTED" );            //$NON-NLS-1$
  String ERR_MSG_INVALID_ARG_ID             = Messages.getString( "ERR_MSG_INVALID_ARG_ID" );             //$NON-NLS-1$
  String ERR_MSG_ARG_NOT_FOUND              = Messages.getString( "ERR_MSG_ARG_NOT_FOUND" );              //$NON-NLS-1$
  String ERR_MSG_ARG_ALREADY_EXIST          = Messages.getString( "ERR_MSG_ARG_ALREADY_EXIST" );          //$NON-NLS-1$
  String ERR_MSG_VALUE_EXPECTED             = Messages.getString( "ERR_MSG_VALUE_EXPECTED" );             //$NON-NLS-1$
  String ERR_MSG_REF_ARG_UNEXPECTED         = Messages.getString( "ERR_MSG_REF_ARG_UNEXPECTED" );         //$NON-NLS-1$
  String ERR_MSG_NAMED_ARG_UNEXPECTED       = Messages.getString( "ERR_MSG_NAMED_ARG_UNEXPECTED" );       //$NON-NLS-1$
  String ERR_MSG_ARG_FLAG_INPOSSIBLE        = Messages.getString( "ERR_MSG_ARG_FLAG_INPOSSIBLE" );        //$NON-NLS-1$
  String ERR_MSG_VALUE_UNEXPECTED           = Messages.getString( "ERR_MSG_VALUE_UNEXPECTED" );           //$NON-NLS-1$
  String ERR_MSG_VALUE_MUST_BE_SINGLE       = Messages.getString( "ERR_MSG_VALUE_MUST_BE_SINGLE" );       //$NON-NLS-1$
  String ERR_MSG_VALUE_MUST_BE_LIST         = Messages.getString( "ERR_MSG_VALUE_MUST_BE_LIST" );         //$NON-NLS-1$
  String ERR_MSG_VALUE_MUST_BE_NAMED        = Messages.getString( "ERR_MSG_VALUE_MUST_BE_NAMED" );        //$NON-NLS-1$
  String ERR_MSG_UNPOSSIBLE_VALUE           = Messages.getString( "ERR_MSG_UNPOSSIBLE_VALUE" );           //$NON-NLS-1$
  String ERR_MSG_INVALID_TYPE               = Messages.getString( "ERR_MSG_INVALID_TYPE" );               //$NON-NLS-1$
  String ERR_MSG_INVALID_VALUE              = Messages.getString( "ERR_MSG_INVALID_VALUE" );              //$NON-NLS-1$
  String ERR_MSG_INVALID_PARAM_VALUE        = Messages.getString( "ERR_MSG_INVALID_PARAM_VALUE" );        //$NON-NLS-1$
  String ERR_MSG_NEED_STRING                = Messages.getString( "ERR_MSG_NEED_STRING" );                //$NON-NLS-1$
  String ERR_MSG_INVALID_QUOTED             = Messages.getString( "ERR_MSG_INVALID_QUOTED" );             //$NON-NLS-1$
  String ERR_MSG_NOT_DEFINED_CONTEXT        = Messages.getString( "ERR_MSG_NOT_DEFINED_CONTEXT" );        //$NON-NLS-1$
  String ERR_MSG_NOT_FOUND_CONTEXT          = Messages.getString( "ERR_MSG_NOT_FOUND_CONTEXT" );          //$NON-NLS-1$
  String ERR_MSG_NOT_FOUND_NAMED_VALUE      = Messages.getString( "ERR_MSG_NOT_FOUND_NAMED_VALUE" );      //$NON-NLS-1$
  String ERR_MSG_CTX_READONLY               = Messages.getString( "ERR_MSG_CTX_READONLY" );               //$NON-NLS-1$
  String ERR_MSG_CTX_STATEMENT_EXPECTED     = Messages.getString( "ERR_MSG_CTX_STATEMENT_EXPECTED" );     //$NON-NLS-1$
  String ERR_MSG_DOUBLE_EQ                  = Messages.getString( "ERR_MSG_DOUBLE_EQ" );                  //$NON-NLS-1$
  String MSG_ERR_SUPER_QUOTE_EXPECTED       = Messages.getString( "MSG_ERR_SUPER_QUOTE_EXPECTED" );       //$NON-NLS-1$
  String MSG_ERR_READ_SUPER_QUOTE           = Messages.getString( "MSG_ERR_READ_SUPER_QUOTE" );           //$NON-NLS-1$
  String MSG_ERR_CLOSE_SUPER_QUOTE_EXPECTED = Messages.getString( "MSG_ERR_CLOSE_SUPER_QUOTE_EXPECTED" ); //$NON-NLS-1$

  // FIXME L10n
  String ERR_MSG_CTX_WRONG_STATEMENT =
      "В позиции %d оператор утверждения '%s' не может быть использован для параметров контекста. Только '"
          + STATEMENT_WRITE + "' или '" + STATEMENT_APPLY + "'";

}
