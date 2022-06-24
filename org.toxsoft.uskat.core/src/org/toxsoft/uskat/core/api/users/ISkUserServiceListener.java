package org.toxsoft.uskat.core.api.users;

import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.uskat.core.*;

/**
 * Listener to the {@link ISkUserService}.
 *
 * @author hazard157
 */
public interface ISkUserServiceListener {

  /**
   * Called when any change in users occur.
   *
   * @param aCoreApi {@link ISkCoreApi} - the event source
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aLogin String - affected user login or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  void onUsersChanged( ISkCoreApi aCoreApi, ECrudOp aOp, String aLogin );

  /**
   * Called when any change in roles occur.
   *
   * @param aCoreApi {@link ISkCoreApi} - the event source
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aRoleId String - affected role ID or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  void onRolesChanged( ISkCoreApi aCoreApi, ECrudOp aOp, String aRoleId );

}
