package org.toxsoft.uskat.core.backend.metainf;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  /**
   * {@link ESkAuthentificationType}
   */
  String STR_N_SAT_NONE   = Messages.getString( "STR_N_SAT_NONE" );   //$NON-NLS-1$
  String STR_D_SAT_NONE   = Messages.getString( "STR_D_SAT_NONE" );   //$NON-NLS-1$
  String STR_N_SAT_SIMPLE = Messages.getString( "STR_N_SAT_SIMPLE" ); //$NON-NLS-1$
  String STR_D_SAT_SIMPLE = Messages.getString( "STR_D_SAT_SIMPLE" ); //$NON-NLS-1$

  /**
   * {@link SkBackendMetaInfo}
   */
  String FMT_ERR_NO_MANDATORY_OP   = Messages.getString( "FMT_ERR_NO_MANDATORY_OP" );   //$NON-NLS-1$
  String FMT_ERR_NO_MANDATORY_REF  = Messages.getString( "FMT_ERR_NO_MANDATORY_REF" );  //$NON-NLS-1$
  String FMT_ERR_OP_TYPE_MISMATCH  = Messages.getString( "FMT_ERR_OP_TYPE_MISMATCH" );  //$NON-NLS-1$
  String FMT_ERR_REF_TYPE_MISMATCH = Messages.getString( "FMT_ERR_REF_TYPE_MISMATCH" ); //$NON-NLS-1$

}
