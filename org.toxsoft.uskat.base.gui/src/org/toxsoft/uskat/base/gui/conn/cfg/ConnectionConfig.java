package org.toxsoft.uskat.base.gui.conn.cfg;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * An immutable implementation of {@link IConnectionConfig}.
 *
 * @author hazard157
 */
public final class ConnectionConfig
    extends StridableParameterized
    implements IConnectionConfig {

  /**
   * The keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IConnectionConfig> KEEPER =
      new AbstractEntityKeeper<>( IConnectionConfig.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IConnectionConfig aEntity ) {
          aSw.incNewLine();
          // id, providerId
          aSw.writeAsIs( aEntity.id() );
          aSw.writeSeparatorChar();
          aSw.writeAsIs( aEntity.providerId() );
          aSw.writeSeparatorChar();
          aSw.writeEol();
          // params
          OptionSetKeeper.KEEPER_INDENTED.write( aSw, aEntity.params() );
          aSw.writeSeparatorChar();
          aSw.writeEol();
          // opValues
          OptionSetKeeper.KEEPER_INDENTED.write( aSw, aEntity.opValues() );
          aSw.decNewLine();
        }

        @Override
        protected IConnectionConfig doRead( IStrioReader aSr ) {
          String id = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          String providerId = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IOptionSet opVals = OptionSetKeeper.KEEPER.read( aSr );
          return new ConnectionConfig( id, providerId, params, opVals );
        }
      };

  private final String     providerId;
  private final IOptionSet values;

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aProviderId String - the provider ID (IDpath)
   * @param aParams {@link IOptionSet} - values of {@link #params()}
   * @param aValues {@link IOptionSet} - values of {@link #opValues()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public ConnectionConfig( String aId, String aProviderId, IOptionSet aParams, IOptionSet aValues ) {
    super( aId );
    providerId = StridUtils.checkValidIdPath( aProviderId );
    params().addAll( aParams );
    values = new OptionSet( aValues );
  }

  // ------------------------------------------------------------------------------------
  // IConnectionConfig
  //

  @Override
  public String providerId() {
    return providerId;
  }

  @Override
  public IOptionSet opValues() {
    return values;
  }

}
