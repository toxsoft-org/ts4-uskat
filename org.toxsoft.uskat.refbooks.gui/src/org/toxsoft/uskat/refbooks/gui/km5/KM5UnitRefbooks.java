package org.toxsoft.uskat.refbooks.gui.km5;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.refbooks.lib.*;

/**
 * M5 models contributor for {@link ISkRefbookService} entities: refbook itself and refbook items classes.
 *
 * @author hazard157
 * @author dima // ts4 transition
 */
public class KM5UnitRefbooks
    extends KM5AbstractModelManagementUnit {

  protected KM5UnitRefbooks( String aUnitId ) {
    super( aUnitId );
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void doInitModels() {
    // модель самого понятия "справочник"
    m5().addModel( new SkRefbookKM5Model( skConn() ) );
    // модели элемента каждого справочника
    ISkRefbookService rs = coreApi().getService( ISkRefbookService.SERVICE_ID );
    IList<ISkRefbook> rbList = rs.listRefbooks();
    for( ISkRefbook rb : rbList ) {
      m5().addModel( new SkRefbookItemKM5Model( rb, skConn() ) );
    }
  }

}
