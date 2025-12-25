package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Details how Sk-objects of the {@link #classId()} are implemented in USkat core.
 *
 * @author hazard157
 * @param classId String - the Sk-class ID (an IDpath)
 * @param claimingServiceId - claiming service ID (an IDpath)
 * @param objCreator {@link ISkObjectCreator} - factory of the objects, no <code>null</code>
 */
public record SkClassImplementationInfo ( String classId, String claimingServiceId,
    ISkObjectCreator<? extends SkObject> objCreator ) {

  /**
   * Constructor.
   *
   * @param classId String - the Sk-class ID (an IDpath)
   * @param claimingServiceId - claiming service ID (an IDpath)
   * @param objCreator {@link ISkObjectCreator} - factory of the objects, no <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public SkClassImplementationInfo( String classId, String claimingServiceId,
      ISkObjectCreator<? extends SkObject> objCreator ) {
    this.classId = StridUtils.checkValidIdPath( classId );
    this.claimingServiceId = StridUtils.checkValidIdPath( claimingServiceId );
    this.objCreator = TsNullArgumentRtException.checkNull( objCreator );
  }

}
