package org.toxsoft.uskat.sysext.refbooks.impl;

import static org.toxsoft.uskat.sysext.refbooks.ISkRefbookServiceHardConstants.*;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.uskat.sysext.refbooks.*;

import ru.uskat.core.common.skobject.ISkObjectCreator;
import ru.uskat.core.impl.SkObject;

/**
 * {@link ISkRefbookItem} implementation.
 *
 * @author goga
 */
public class SkRefbookItem
    extends SkObject
    implements ISkRefbookItem {

  static final ISkObjectCreator<SkRefbookItem> CREATOR = aSkid -> new SkRefbookItem( aSkid );

  protected SkRefbookItem( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkRefbookItem
  //

  @Override
  public ISkRefbook refbook() {
    String refbookId = makeRefbookIdFromItemClassId( classId() );
    ISkRefbookService rs = coreApi().getService( ISkRefbookService.SERVICE_ID );
    ISkRefbook rb = rs.findRefbook( refbookId );
    TsInternalErrorRtException.checkNull( rb );
    return rb;
  }

}
