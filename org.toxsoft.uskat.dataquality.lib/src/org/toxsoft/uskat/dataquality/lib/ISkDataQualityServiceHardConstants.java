package org.toxsoft.uskat.dataquality.lib;

import static org.toxsoft.uskat.dataquality.lib.ISkResources.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;

/**
 * Unchangeable constants of the data quality service.
 *
 * @author mvk
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface ISkDataQualityServiceHardConstants {

  // ------------------------------------------------------------------------------------
  // IBaDataQuality
  //
  String BAID_DATA_QUALITY = ISkBackendHardConstant.SKB_ID + ".DataQuality";

  IStridable BAINF_DATA_QUALITY = new Stridable( BAID_DATA_QUALITY, STR_N_BA_DATA_QUALITY, STR_D_BA_DATA_QUALITY );
}
