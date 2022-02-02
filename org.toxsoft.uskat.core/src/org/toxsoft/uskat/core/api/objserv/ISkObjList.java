package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;

/**
 * Read-ponly interface of the list of objects {@link ISkObject}.
 *
 * @author hazard157
 */
public interface ISkObjList {

  IList<ISkObject> objs();

  ISkidList skids();

  IStringList classIds();

  ISkidList listObjIdsOfClass( String aClassId );

  IList<ISkObject> listObjsOfClass( String aClassId );

  Skid findDuplicateSkid();

  ISkObject findDuplicate();

}
