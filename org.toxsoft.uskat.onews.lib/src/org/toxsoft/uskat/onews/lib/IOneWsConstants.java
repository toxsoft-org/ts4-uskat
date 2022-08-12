package org.toxsoft.uskat.onews.lib;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.onews.lib.ITsResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;

// TODO TRANSLATE

/**
 * Constants f the service {@link ISkOneWsService}.
 *
 * @author hazard157
 */
public interface IOneWsConstants {

  /**
   * Prefix for builtin kinds and abilities.
   */
  String OWS_ID = SK_ID + ".onews"; //$NON-NLS-1$

  /**
   * Identifier of builting root profile.
   * <p>
   * Root profile always allows all abilities and components of OneWS.
   */
  String OWS_ID_PROFILE_ROOT = OWS_ID + ".profile.root"; //$NON-NLS-1$

  /**
   * ID of builting guest profile.
   * <p>
   * Root profile always denies all abilities and components of OneWS.
   */
  String OWS_ID_PROFILE_GUEST = OWS_ID + ".profile.guest"; //$NON-NLS-1$

  /**
   * ID of builtin kind - ability to open perspectives.
   */
  String OWS_KINDID_PERSPECTIVE = OWS_ID + ".kind.perspective"; //$NON-NLS-1$

  /**
   * ID of builtin kind - ability to run an action.
   */
  String OWS_KIND_ID_ACTION = OWS_ID + ".kind.action"; //$NON-NLS-1$

  /**
   * List o builtin kinds.
   */
  IStridablesList<IStridableParameterized> BUILTIN_ABILITY_KINDS = new StridablesList<>( //
      StridableParameterized.create( OWS_KINDID_PERSPECTIVE, //
          TSID_NAME, STR_N_PERSPECTIVE_ABILITY, //
          TSID_DESCRIPTION, STR_D_PERSPECTIVE_ABILITY //
      ), //
      StridableParameterized.create( OWS_KIND_ID_ACTION, //
          TSID_NAME, STR_N_ACTION_ABILITY, //
          TSID_DESCRIPTION, STR_D_ACTION_ABILITY //
      ) //
  );

  /**
   * ID of option {@link #OP_OWS_ABILITY_KIND_ID}.
   */
  String OPID_OWS_ABILITY_KIND_ID = OWS_ID + ".AbilityKindId"; //$NON-NLS-1$

  /**
   * Option of {@link IOneWsAbility#params()}: contains kind ID {@link IOneWsAbility#kindId()}.
   */
  IDataDef OP_OWS_ABILITY_KIND_ID = DataDef.create( OPID_OWS_ABILITY_KIND_ID, STRING, //
      TSID_NAME, STR_N_KIND_ID, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_DESCRIPTION, STR_D_KIND_ID //
  );

}
