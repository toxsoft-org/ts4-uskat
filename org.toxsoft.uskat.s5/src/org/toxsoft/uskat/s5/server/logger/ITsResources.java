package org.toxsoft.uskat.s5.server.logger;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ITsResources {

  // Note: LOG_XXX strings should not be localizes because they are logged as developer-level messages

  String LOG_NO_LOG4J_CFG_FILE_IN_SYS_PROPS =
      "LoggerWrapper.setScanPropertiesTimeout(): Rescan is not working - system property -Dlog4j.configuration not set"; //$NON-NLS-1$

  String LOG_NO_LOG4J_CFG_FILE_FOUND =
      "LoggerWrapper.setScanPropertiesTimeout(): Rescan is not working - system property -Dlog4j.configuration specified non-acessible file: "; //$NON-NLS-1$

  String LOG_LOG4J_CFG_FILE_INV_EXT =
      "LoggerWrapper.setScanPropertiesTimeout(): Rescan is not working - invalid extension (not 'properties' or 'xml') of configuration file: "; //$NON-NLS-1$

}
