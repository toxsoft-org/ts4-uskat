package org.toxsoft.uskat.core.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Описание моделированного поля - одночной связи объекта {@link ISkObject}.
 *
 * @author hazard157
 */
public class KM5SingleLinkFieldDef
    extends M5SingleLookupFieldDef<ISkObject, ISkObject>
    implements ISkConnected {

  /**
   * Поставщик связуемых объектов.
   */
  final IM5LookupProvider<ISkObject> lookupProvider = new IM5LookupProvider<>() {

    public IList<ISkObject> listItems() {
      ISkConnection conn = skConn();
      if( conn.state() != ESkConnState.ACTIVE ) {
        return IList.EMPTY;
      }
      ISkObjectService os = conn.coreApi().objService();
      IListEdit<ISkObject> lookupObjs = new ElemLinkedBundleList<>();
      for( String rightClassId : linkInfo.rightClassIds() ) {
        lookupObjs.addAll( os.listObjs( rightClassId, true ) );
      }
      return lookupObjs;
    }
  };

  final IDtoLinkInfo linkInfo;

  /**
   * Конструктор.
   *
   * @param aLinkInfo {@link IDtoLinkInfo} - описание моделируемой связи
   */
  public KM5SingleLinkFieldDef( IDtoLinkInfo aLinkInfo ) {
    super( aLinkInfo.id(), IGwHardConstants.GW_ROOT_CLASS_ID ); // ИД модели уточняется в specifyItemModelId()
    TsIllegalArgumentRtException.checkTrue( aLinkInfo.linkConstraint().maxCount() != 1 );
    linkInfo = aLinkInfo;
    TsInternalErrorRtException.checkTrue( linkInfo.linkConstraint().maxCount() != 1 );
    String name = linkInfo.nmName().isEmpty() ? linkInfo.id() : linkInfo.nmName();
    setNameAndDescription( name, linkInfo.description() );
    setFlags( M5FF_DETAIL );
  }

  @Override
  protected String specifyItemModelId() {
    return skSysdescr().hierarchy().findCommonRootClassId( linkInfo.rightClassIds() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация M5SingleLookupFieldDef
  //

  @Override
  protected ISkObject doGetFieldValue( ISkObject aEntity ) {
    ISkLinkService ls = coreApi().linkService();
    ISkObjectService os = coreApi().objService();
    IDtoLinkFwd lf = ls.getLinkFwd( aEntity.skid(), linkInfo.id() );
    if( lf.rightSkids().isEmpty() ) {
      return null;
    }
    return os.get( lf.rightSkids().first() );
  }

  @Override
  protected String doGetFieldValueName( ISkObject aEntity ) {
    ISkObject linkedObj = doGetFieldValue( aEntity );
    if( linkedObj != null ) {
      return linkedObj.readableName();
    }
    return TsLibUtils.EMPTY_STRING;
  }

  @Override
  public boolean canUserSelectNull() {
    return !linkInfo.linkConstraint().isExactCount();
  }

  @Override
  public IM5LookupProvider<ISkObject> lookupProvider() {
    return lookupProvider;
  }

  // ------------------------------------------------------------------------------------
  // ISkStdContextReferences
  //

  @Override
  public ISkConnection skConn() {
    return tsContext().get( ISkConnection.class );
  }

}
