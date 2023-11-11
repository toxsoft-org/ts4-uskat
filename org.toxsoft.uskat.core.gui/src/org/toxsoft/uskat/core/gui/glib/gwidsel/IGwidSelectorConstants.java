package org.toxsoft.uskat.core.gui.glib.gwidsel;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.glib.gwidsel.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Constants of the GWID selector panels and VALEDs.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IGwidSelectorConstants {

  String GWIDSEL_ID = SK_ID + ".GwidSelector"; //$NON-NLS-1$

  IDataDef OPDEF_CLASS_PROP_KIND = DataDef.create( GWIDSEL_ID + ".ClassPropKind", VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_CLASS_PROP_KIND, //
      TSID_DESCRIPTION, STR_CLASS_PROP_KIND_D, //
      TSID_KEEPER_ID, ESkClassPropKind.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( ESkClassPropKind.RTDATA ) //
  );

}
