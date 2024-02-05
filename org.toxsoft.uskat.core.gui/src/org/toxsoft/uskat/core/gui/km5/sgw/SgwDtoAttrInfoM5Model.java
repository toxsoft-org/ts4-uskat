package org.toxsoft.uskat.core.gui.km5.sgw;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISkResources.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link IDtoAttrInfo}.
 *
 * @author hazard157
 */
public class SgwDtoAttrInfoM5Model
    extends SgwDtoPropInfoM5ModelBase<IDtoAttrInfo> {

  /**
   * Modown field {@link IDtoRtdataInfo#dataType()}.
   */
  public final IM5SingleModownFieldDef<IDtoAttrInfo, IDataType> DATA_TYPE =
      new M5SingleModownFieldDef<>( FID_DATA_TYPE, DataTypeM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_PROP_DATA_TYPE, STR_D_PROP_DATA_TYPE );
          setFlags( M5FF_COLUMN );
        }

        protected IDataType doGetFieldValue( IDtoAttrInfo aEntity ) {
          return aEntity.dataType();
        }

      };

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwDtoAttrInfoM5Model( ISkConnection aConn ) {
    super( MID_SGW_ATTR_INFO, IDtoAttrInfo.class, aConn );
    addFieldDefs( DATA_TYPE );
  }

}
