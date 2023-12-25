package org.toxsoft.uskat.core.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * M5-field for the multi rivet of {@link ISkObject}.
 *
 * @author hazard157
 */
public class KM5MultiRivetFieldDef
    extends M5MultiLookupFieldDef<ISkObject, ISkObject>
    implements ISkConnected {

  /**
   * Riveted object lookup provider.
   */
  final IM5LookupProvider<ISkObject> lookupProvider = new IM5LookupProvider<>() {

    public IList<ISkObject> listItems() {
      if( skConn().state().isActive() ) {
        return skObjServ().listObjs( rivetInfo.rightClassId(), true );
      }
      return IList.EMPTY;
    }
  };

  private final IDtoRivetInfo rivetInfo;

  /**
   * Constructor.
   *
   * @param aRivetInfo {@link IDtoRivetInfo} - modeled rivet info
   */
  public KM5MultiRivetFieldDef( IDtoRivetInfo aRivetInfo ) {
    super( aRivetInfo.id(), aRivetInfo.rightClassId() );
    rivetInfo = aRivetInfo;
    TsIllegalArgumentRtException.checkTrue( aRivetInfo.count() == 1 );
    String name = rivetInfo.nmName().isEmpty() ? rivetInfo.id() : rivetInfo.nmName();
    setNameAndDescription( name, rivetInfo.description() );
    setFlags( M5FF_DETAIL );
  }

  // ------------------------------------------------------------------------------------
  // M5MultiLookupFieldDef
  //

  @Override
  protected IList<ISkObject> doGetFieldValue( ISkObject aEntity ) {
    return aEntity.getMultiRivets( rivetInfo.id() );
  }

  @Override
  protected String doGetFieldValueName( ISkObject aEntity ) {
    IList<ISkObject> rivetedObjs = doGetFieldValue( aEntity );
    return SkHelperUtils.makeObjsListReadableName( rivetedObjs );
  }

  @Override
  public IM5LookupProvider<ISkObject> lookupProvider() {
    return lookupProvider;
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return tsContext().get( ISkConnection.class );
  }

}
