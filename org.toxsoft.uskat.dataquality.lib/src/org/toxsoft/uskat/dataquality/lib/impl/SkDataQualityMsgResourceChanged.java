package org.toxsoft.uskat.dataquality.lib.impl;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.AbstractBackendMessageBuilder;
import org.toxsoft.uskat.dataquality.lib.IBaDataQuality;
import org.toxsoft.uskat.dataquality.lib.ISkDataQualityService;

/**
 * {@link IBaDataQuality} message builder: change resource state notification.
 *
 * @author mvk
 */
public class SkDataQualityMsgResourceChanged
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "QualityDataResourceChanged"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final SkDataQualityMsgResourceChanged INSTANCE = new SkDataQualityMsgResourceChanged();

  private static final String ARGID_RESOURCE_ID = "ResourceId"; //$NON-NLS-1$

  SkDataQualityMsgResourceChanged() {
    super( ISkDataQualityService.SERVICE_ID, MSG_ID );
    defineArgNonValobj( ARGID_RESOURCE_ID, EAtomicType.STRING, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aResourceId String resource ID
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( String aResourceId ) {
    return makeMessageVarargs( ARGID_RESOURCE_ID, aResourceId );
  }

  /**
   * Extracts resource ID argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return String resource ID.
   */
  public String getResourceId( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_RESOURCE_ID ).asString();
  }

}
