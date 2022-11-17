package org.toxsoft.uskat.dataquality.s5.addons;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.dataquality.lib.*;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaDataQuality} implementation.
 *
 * @author mvk
 */
public final class S5BaDataQualityRemote
    extends S5AbstractBackendAddonRemote<IS5BaDataQualitySession>
    implements IBaDataQuality {

  /**
   * Данные конфигурации фронтенда для {@link IBaDataQuality}
   */
  private final S5BaDataQuality baData = new S5BaDataQuality();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaDataQualityRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkDataQualityServiceHardConstants.BAINF_DATA_QUALITY, IS5BaDataQualitySession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaDataQuality.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IBaDataQuality
  //
  @Override
  public IOptionSet getResourceMarks( Gwid aResource ) {
    TsNullArgumentRtException.checkNull( aResource );
    return session().getResourceMarks( aResource );
  }

  @Override
  public IMap<Gwid, IOptionSet> getResourcesMarks( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    return session().getResourcesMarks( aResources );
  }

  @Override
  public IMap<Gwid, IOptionSet> queryMarkedUgwies( ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNull( aQueryParams );
    return session().queryMarkedUgwies( aQueryParams );
  }

  @Override
  public IGwidList getConnectedResources() {
    return session().getConnectedResources();
  }

  @Override
  public void addConnectedResources( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    session().addConnectedResources( aResources );
  }

  @Override
  public void removeConnectedResources( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    session().removeConnectedResources( aResources );
  }

  @Override
  public IGwidList setConnectedResources( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    return session().setConnectedResources( aResources );
  }

  @Override
  public void setMarkValue( String aTicketId, IAtomicValue aValue, IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aTicketId, aValue, aResources );
    session().setMarkValue( aTicketId, aValue, aResources );
  }

  @Override
  public IStridablesList<ISkDataQualityTicket> listTickets() {
    return session().listTickets();
  }

  @Override
  public ISkDataQualityTicket defineTicket( String aTicketId, String aName, String aDescription, IDataType aDataType ) {
    TsNullArgumentRtException.checkNulls( aTicketId, aName, aDescription, aDataType );
    return session().defineTicket( aTicketId, aName, aDescription, aDataType );
  }

  @Override
  public void removeTicket( String aTicketId ) {
    TsNullArgumentRtException.checkNull( aTicketId );
    session().removeTicket( aTicketId );
  }
}
