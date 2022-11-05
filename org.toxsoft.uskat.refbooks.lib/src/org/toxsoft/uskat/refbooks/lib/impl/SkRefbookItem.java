package org.toxsoft.uskat.refbooks.lib.impl;

import static org.toxsoft.uskat.refbooks.lib.ISkRefbookServiceHardConstants.*;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.refbooks.lib.*;

/**
 * {@link ISkRefbookItem} implementation.
 *
 * @author hazard157
 */
public class SkRefbookItem
    extends SkObject
    implements ISkRefbookItem {

  static final ISkObjectCreator<SkRefbookItem> CREATOR = SkRefbookItem::new;

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
