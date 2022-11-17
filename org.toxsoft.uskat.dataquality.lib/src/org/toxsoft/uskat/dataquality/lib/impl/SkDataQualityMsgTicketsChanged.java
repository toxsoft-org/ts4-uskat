package org.toxsoft.uskat.dataquality.lib.impl;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.AbstractBackendMessageBuilder;
import org.toxsoft.uskat.dataquality.lib.IBaDataQuality;
import org.toxsoft.uskat.dataquality.lib.ISkDataQualityService;

/**
 * {@link IBaDataQuality} message builder: change tickets notification.
 *
 * @author mvk
 */
public class SkDataQualityMsgTicketsChanged
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "QualityDataTicketsChanged"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final SkDataQualityMsgTicketsChanged INSTANCE = new SkDataQualityMsgTicketsChanged();

  SkDataQualityMsgTicketsChanged() {
    super( ISkDataQualityService.SERVICE_ID, MSG_ID );
  }

  /**
   * Creates the message instance.
   *
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage() {
    return makeMessage();
  }
}
