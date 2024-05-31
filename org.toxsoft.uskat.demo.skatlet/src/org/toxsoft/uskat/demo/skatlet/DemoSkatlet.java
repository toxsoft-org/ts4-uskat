package org.toxsoft.uskat.demo.skatlet;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.devapi.ISkatlet;

/**
 * Demo skatlet.
 *
 * @author mvk
 */
public class DemoSkatlet
    implements ISkatlet {

  private ITsContextRo environ;
  private ILogger      logger = LoggerWrapper.getLogger( getClass() );

  @Override
  public String id() {
    return "Demo"; //$NON-NLS-1$
  }

  @Override
  public String nmName() {
    return "Demo skatlet"; //$NON-NLS-1$
  }

  @Override
  public String description() {
    return "This is a demo skatlet"; //$NON-NLS-1$
  }

  @Override
  public IOptionSet params() {
    return environ.params();
  }

  @Override
  public void start() {
    logger.info( "%s: start()", id() ); //$NON-NLS-1$
  }

  @Override
  public void doJob() {
    logger.info( "%s: doJob()", id() ); //$NON-NLS-1$
  }

  @Override
  public boolean queryStop() {
    logger.info( "%s: queryStop()", id() ); //$NON-NLS-1$
    return true;
  }

  @Override
  public boolean isStopped() {
    return true;
  }

  @Override
  public void destroy() {
    logger.info( "%s: destroy()", id() ); //$NON-NLS-1$
  }

  @Override
  public ValidationResult init( ITsContextRo aEnviron ) {
    environ = aEnviron;
    logger.info( "%s: init(). connection = %s", id(), ISkatlet.REF_SK_CONNECTION.getRef( aEnviron ) ); //$NON-NLS-1$
    return ValidationResult.SUCCESS;
  }

}
