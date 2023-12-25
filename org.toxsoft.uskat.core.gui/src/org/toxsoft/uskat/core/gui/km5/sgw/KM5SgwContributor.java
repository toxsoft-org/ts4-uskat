package org.toxsoft.uskat.core.gui.km5.sgw;

import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * Contributes M5-models for SGW entities.
 *
 * @author hazard157
 */
public class KM5SgwContributor
    extends KM5AbstractContributor {

  /**
   * Creator singleton.
   */
  public static final IKM5ContributorCreator CREATOR = KM5SgwContributor::new;

  private static final IStringList CONRTIBUTED_MODEL_IDS = new StringArrayList( //
      MID_SGW_SK_OBJECT, //
      MID_SGW_CLASS_INFO, //
      MID_SGW_RTDATA_INFO, //
      MID_SGW_RIVET_INFO, //
      MID_SGW_CMD_INFO, //
      MID_SGW_EVENT_INFO, //
      MID_SGW_CLOB_INFO, //
      MID_SGW_ATTR_INFO //
  );

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @param aDomain {@link IM5Domain} - connection domain
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5SgwContributor( ISkConnection aConn, IM5Domain aDomain ) {
    super( aConn, aDomain );
  }

  @Override
  protected IStringList papiCreateModels() {
    m5().addModel( new SgwSkObject( skConn() ) );
    m5().addModel( new SgwSkClassInfoM5Model( skConn() ) );
    m5().addModel( new SgwDtoRtdataInfoM5Model( skConn() ) );
    m5().addModel( new SgwDtoRivetInfoM5Model( skConn() ) );
    m5().addModel( new SgwDtoCmdInfoM5Model( skConn() ) );
    m5().addModel( new SgwDtoEventInfoM5Model( skConn() ) );
    m5().addModel( new SgwDtoClobInfoM5Model( skConn() ) );
    m5().addModel( new SgwDtoAttrInfoM5Model( skConn() ) );
    return CONRTIBUTED_MODEL_IDS;
  }

}
