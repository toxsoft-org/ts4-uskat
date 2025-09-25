package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Validates if command can be send.
 *
 * @author hazard157
 */
public interface ISkCommandServiceValidator {

  /**
   * Checks if such command can be send.
   * <p>
   * Implements application-specific strategies t disable some commands.
   *
   * @param aCmdGwid {@link Gwid} - concrete GWID of the command
   * @param aAuthorSkid {@link Skid} - SKID of the command author
   * @param aArgs {@link IOptionSet} - command arguments values
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException illegal or non-existing command GWID
   * @throws TsIllegalArgumentRtException illegal or non-existing author SKID
   * @throws AvTypeCastRtException incompatible argument type
   */
  ValidationResult canSendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs );

}
