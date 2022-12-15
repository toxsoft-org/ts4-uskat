package org.toxsoft.uskat.core.devapi.clobdb;

import org.toxsoft.core.tslib.coll.primtypes.*;

public interface IClobDb {

  IStringList listSectionIds();

  IClobDbSection defineSection( String aServiceId );

  void removeSection( String aSectionId );

}
