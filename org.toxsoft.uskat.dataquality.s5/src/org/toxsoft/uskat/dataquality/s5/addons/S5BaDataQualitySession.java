package org.toxsoft.uskat.dataquality.s5.addons;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.dataquality.lib.*;
import org.toxsoft.uskat.dataquality.s5.supports.IS5BackendDataQualitySingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;

/**
 * Сессия реализации расширения бекенда {@link IBaDataQuality}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@SuppressWarnings( "unused" )
public class S5BaDataQualitySession
    extends S5AbstractBackendAddonSession
    implements IS5BaDataQualitySession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка бекенда службы качества данных
   */
  @EJB
  private IS5BackendDataQualitySingleton dataQualitySupport;

  /**
   * Пустой конструктор.
   */
  public S5BaDataQualitySession() {
    super( ISkDataQualityServiceHardConstants.BAINF_DATA_QUALITY );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaDataQualitySession> doGetSessionView() {
    return IS5BaDataQualitySession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaDataQualityData baData = new S5BaDataQualityData();
    frontend().frontendData().setBackendAddonData( IBaDataQuality.ADDON_ID, baData );
  }

  @Override
  protected void doBeforeClose() {
    S5BaDataQualityData baData =
        frontend().frontendData().findBackendAddonData( IBaDataQuality.ADDON_ID, S5BaDataQualityData.class );
    // // Список идентификаторов открытых запросов
    // IStringList queryIds;
    // synchronized (baData) {
    // queryIds = new StringArrayList( baData.openQueries.keys() );
    // }
    // // Завершение работы открытых запросов
    // for( String queryId : queryIds ) {
    // queriesSupport.close( frontend(), queryId );
    // }
  }

  // ------------------------------------------------------------------------------------
  // IS5BaDataQualitySession
  //
  @Override
  public IOptionSet getResourceMarks( Gwid aResource ) {
    TsNullArgumentRtException.checkNull( aResource );
    return dataQualitySupport.getResourceMarks( aResource );
  }

  @Override
  public IMap<Gwid, IOptionSet> getResourcesMarks( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    return dataQualitySupport.getResourcesMarks( aResources );
  }

  @Override
  public IMap<Gwid, IOptionSet> queryMarkedUgwies( ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNull( aQueryParams );
    return dataQualitySupport.queryMarkedUgwies( aQueryParams );
  }

  @Override
  public IGwidList getConnectedResources() {
    return dataQualitySupport.getConnectedResources( sessionID() );
  }

  @Override
  public void addConnectedResources( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    dataQualitySupport.addConnectedResources( sessionID(), aResources );
  }

  @Override
  public void removeConnectedResources( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    dataQualitySupport.removeConnectedResources( sessionID(), aResources );
  }

  @Override
  public IGwidList setConnectedResources( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    return dataQualitySupport.setConnectedResources( sessionID(), aResources );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void setMarkValue( String aTicketId, IAtomicValue aValue, IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aTicketId, aValue, aResources );
    dataQualitySupport.setMarkValue( aTicketId, aValue, aResources );
  }

  @Override
  public IStridablesList<ISkDataQualityTicket> listTickets() {
    return dataQualitySupport.listTickets();
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public ISkDataQualityTicket defineTicket( String aTicketId, String aName, String aDescription, IDataType aDataType ) {
    TsNullArgumentRtException.checkNulls( aTicketId, aName, aDescription, aDataType );
    return dataQualitySupport.defineTicket( aTicketId, aName, aDescription, aDataType );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void removeTicket( String aTicketId ) {
    TsNullArgumentRtException.checkNull( aTicketId );
    dataQualitySupport.removeTicket( aTicketId );
  }
}
