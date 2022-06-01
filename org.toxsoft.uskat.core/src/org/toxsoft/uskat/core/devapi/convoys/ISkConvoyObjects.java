package org.toxsoft.uskat.core.devapi.convoys;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * FIXME the concept of CONVOY objects used for some services like RegRefInfo
 *
 * @author hazard157
 */
public interface ISkConvoyObjects {

  ISkClassInfo defineConvoyClass( String aMasterClass, IDtoClassInfo aClassInfo, boolean aIncludeSubclasses );

  ISkObject ensureConvoyObject( Skid aMasterObject );

  void removeConvoyObject( Skid aObject );

}
