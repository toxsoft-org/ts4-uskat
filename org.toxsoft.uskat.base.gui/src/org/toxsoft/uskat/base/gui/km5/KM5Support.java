package org.toxsoft.uskat.base.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Binding between Sk-connection and M5-domain.
 * <p>
 * Constructor creates new domain and binds to the connection. Bind domain is "listening" to the connection. At
 * connection opening creates M5-models by the {@link #initializeDomain()} method. When {@link ISkSysdescr} changes
 * {@link KM5Support} updates M5-models of the changed entities by the {@link #updateDomain(ECrudOp, String)} method.
 * When connection closes domain is cleared by the {@link #clearDomain()} method.
 * <p>
 * Note: activation/deactivation of connection does not changes domain, only connection open/close matters.
 * <p>
 * Binding also means that connection and domain know about each other. {@link ISkConnection#scope()} contains reference
 * to the {@link KM5Support} and {@link IM5Domain} while {@link IM5Domain#tsContext()} holds references to the
 * {@link ISkConnection} and to the {@link KM5Support}.
 * <p>
 * <b>Important note:</b> created domain {@link #m5()} is not intended to have models other than created by this class.
 * All other models may be removed at random modemt of time. If other models are needed, they may be added either in the
 * parent domain or by the registered {@link KM5AbstractContributor}.
 *
 * @author hazard157
 */
public final class KM5Support
    implements ISkConnected {

  /**
   * Prefix of {@link IM5Domain#id()} to be created in constructor.
   */
  private static final String M5_DOMAIN_ID_PREFIX = "km5"; //$NON-NLS-1$

  // --- Log messages does not need to be localized.
  private static final String FMT_LOG_WARN_DUP_CLASS_CONTRIBUTION = "Duplicate KM5 contribution of M5-model %s"; //$NON-NLS-1$
  // ---

  /**
   * Listens to the connection to initialize/clear M5-domain.
   */
  private final ISkConnectionListener skConnectionListener = ( aSource, aOldState ) -> {
    switch( aSource.state() ) {
      case ACTIVE:
        if( aOldState == ESkConnState.CLOSED ) {
          initializeDomain();
        }
        break;
      case CLOSED:
        clearDomain();
        break;
      case INACTIVE:
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  };

  /**
   * Listens to the Sysdescr to {@link #updateDomain(ECrudOp, String)} as needed.
   * <p>
   * Listener is added in {@link #initializeDomain()} but never removed because there is no need to remove listener.
   */
  private final ISkSysdescrListener classServiceListener = ( core, op, cid ) -> updateDomain( op, cid );

  private static final IStridGenerator domainIdGenerator = new SynchronizedStridGeneratorWrapper( //
      new SimpleStridGenaretor( SimpleStridGenaretor.createState( M5_DOMAIN_ID_PREFIX, 0, 4 ) ) //
  );

  private final ISkConnection skConn;
  private final IM5Domain     m5;

  private final IList<KM5AbstractContributor> contributorsList;

  /**
   * Constructor.
   * <p>
   * The child M5-domain will be created as child of <code>aParentDomain</code> and share the same context.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aParentDomain {@link IM5Domain} - the parent domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException the context does not have reference to the parent {@link IM5Domain}
   */
  public KM5Support( ISkConnection aConn, IM5Domain aParentDomain ) {
    TsNullArgumentRtException.checkNulls( aConn, aParentDomain );
    skConn = aConn;
    m5 = aParentDomain.createChildDomain( domainIdGenerator.nextId(), aParentDomain.tsContext() );
    // inform connection and domain about each other
    skConn.scope().put( IM5Domain.class, m5 );
    skConn.scope().put( KM5Support.class, this );
    m5.tsContext().put( ISkConnection.class, skConn );
    m5.tsContext().put( KM5Support.class, this );
    // create contributors
    IListEdit<KM5AbstractContributor> ll = new ElemArrayList<>();
    for( IKM5ContributorCreator cc : KM5Utils.listContributorCreators() ) {
      KM5AbstractContributor contributor = cc.create( skConn, m5 );
      ll.add( contributor );
    }
    // ... and add last contributor
    ll.add( new BuiltinLastContributor( skConn, m5 ) );
    contributorsList = ll;
    // init domain for opened connection
    if( skConn.state().isActive() ) {
      initializeDomain();
    }
    // now we'll listen to the connection
    skConn.addConnectionListener( skConnectionListener );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Creates all models when connection opens.
   */
  void initializeDomain() {
    // create root Sk-class model
    M5Model<ISkObject> rootModel = new KM5RootClassModel( skConn );
    m5.addModel( rootModel );
    //
    // create contributed M5-models
    IStringListEdit contributedModelIds = new StringLinkedBundleList();
    for( KM5AbstractContributor u : contributorsList ) {
      IStringList ll = u.papiCreateModels();
      for( String modelId : ll ) {
        if( !contributedModelIds.hasElem( modelId ) ) {
          contributedModelIds.add( modelId );
        }
        else {
          LoggerUtils.errorLogger().warning( FMT_LOG_WARN_DUP_CLASS_CONTRIBUTION, modelId );
        }
      }
    }
    // now listen to the Sysdescr
    skSysdescr().eventer().addListener( classServiceListener );
  }

  /**
   * Updates model when sysdescr changes.
   * <p>
   * Arguments are directly passed from {@link ISkSysdescrListener#onClassInfosChanged(ISkCoreApi, ECrudOp, String)}.
   *
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aClassId String - affected class ID or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  void updateDomain( ECrudOp aOp, String aClassId ) {
    // on batch changes reinitialize whole domain like connection closes and then opens
    // OPTIMIZE determine changed classes and process only changed ones
    if( aOp == ECrudOp.LIST ) {
      clearDomain();
      initializeDomain();
      return;
    }
    // allow contributors to process changes
    for( KM5AbstractContributor u : contributorsList ) {
      if( u.papiUpdateModel( aOp, aClassId ) ) {
        // change was processed, nothing to be done
        return;
      }
    }
  }

  void clearDomain() {
    // remove all models
    for( String modelId : m5.selfModels().keys() ) {
      m5.removeModel( modelId );
    }
    // inform contributors on connection close
    for( KM5AbstractContributor u : contributorsList ) {
      u.papiAfterConnectionClose();
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return skConn;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the bind domain created in constructor.
   * <p>
   * <b>Important note:</b> created domain {@link #m5()} is not intended to have models other than created by this
   * class. All other models may be removed at random modemt of time. If other models are needed, they may be added
   * either in the parent domain or by the registered {@link KM5AbstractContributor}.
   *
   * @return {@link IM5Domain} - the domain bind to the connection {@link #skConn()}
   */
  public IM5Domain m5() {
    return m5;
  }

}
