package org.toxsoft.uskat.virtdata;

/**
 * Localizable resources.
 *
 * @author mvk
 */
interface ISkResources {

  String ERR_NOT_FOUND_READ_DATA  = Messages.getString( "ERR_NOT_FOUND_READ_DATA" );  //$NON-NLS-1$
  String ERR_NOT_FOUND_WRITE_DATA = Messages.getString( "ERR_NOT_FOUND_WRITE_DATA" ); //$NON-NLS-1$

  String ERR_UNASSIGNED_INPUTS = "%s = IAtomicValue.NULL due to unassigned inputs = %s"; //$NON-NLS-1$
}
