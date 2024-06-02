package org.toxsoft.uskat.demo.skatlet;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.uskat.core.impl.AbstractSkatlet;

/**
 * Demo skatlet.
 *
 * @author mvk
 */
public class DemoSkatlet
    extends AbstractSkatlet {

  /**
   * Constructor.
   */
  public DemoSkatlet() {
    super( "Demo", OptionSetUtils.createOpSet( //$NON-NLS-1$
        TSID_NAME, "Demo skatlet", //$NON-NLS-1$
        TSID_DESCRIPTION, "This is a demo skatlet" //$NON-NLS-1$
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkatlet
  //
  @Override
  protected ValidationResult doInit( ITsContextRo aEnviron ) {
    logger().info( "%s doInit(). connection = %s", id(), connection() ); //$NON-NLS-1$
    return ValidationResult.SUCCESS;
  }

  @Override
  public void start() {
    logger().info( "%s: start()", id() ); //$NON-NLS-1$
  }

  @Override
  public void doJob() {
    logger().info( "%s: doJob()", id() ); //$NON-NLS-1$
  }

  @Override
  public boolean queryStop() {
    logger().info( "%s: queryStop()", id() ); //$NON-NLS-1$
    return true;
  }

  @Override
  public boolean isStopped() {
    return true;
  }

  @Override
  public void destroy() {
    logger().info( "%s: destroy()", id() ); //$NON-NLS-1$
  }

}
