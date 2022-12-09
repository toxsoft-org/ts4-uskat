package org.toxsoft.uskat.ggprefs.lib.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.ggprefs.lib.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.impl.dto.DtoAttrInfo;
import org.toxsoft.uskat.ggprefs.lib.ISkGuiGwPrefsService;

/**
 * {@link ISkGuiGwPrefsService} implementation internal constants.
 *
 * @author goga
 */
interface IServiceInternalConstants {

  String ID_START = ISkHardConstants.SK_ID + ".GuiGwPrefs"; //$NON-NLS-1$

  String CLSID_SECTION = ID_START + ".Section"; //$NON-NLS-1$

  IDtoAttrInfo AINF_SECTTION_DEF_PARAMS = DtoAttrInfo.create2( "SectionDefParams", DDEF_VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_AINF_SECTTION_DEF_PARAMS, //
      TSID_DESCRIPTION, STR_D_AINF_SECTTION_DEF_PARAMS, //
      TSID_DEFAULT_VALUE, avValobj( IOptionSet.NULL ) //
  );

}
