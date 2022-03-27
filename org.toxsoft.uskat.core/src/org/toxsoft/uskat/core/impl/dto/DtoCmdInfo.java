package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.impl.dto.IDtoHardConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoCmdInfo} implementation.
 * <p>
 * As {@link StridableParameterized} extension this class stores all fields in {@link #params()}:
 * <ul>
 * <li>{@link #argDefs()} - as option {@link IDtoHardConstants#OPDEF_ARG_DEFS}.</li>
 * </ul>
 *
 * @author hazard157
 */
public final class DtoCmdInfo
    extends DtoAbstractClassPropInfoBase
    implements IDtoCmdInfo {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoCmdInfo> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDtoCmdInfo.class, null ) {

        @Override
        protected IDtoCmdInfo doCreate( String aId, IOptionSet aParams ) {
          return new DtoCmdInfo( aId, aParams );
        }
      };

  /**
   * {@link #argDefs()} cached value, <code>null</code> - not inited yet from {@link #params()}.
   */
  private transient IStridablesList<IDataDef> argDefs = null;

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - parameters values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  private DtoCmdInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aArgDefs {@link IStridablesList}&lt;{@link IDataDef}&gt; - the arguments data defs list
   * @param aParams {@link IOptionSet} - parameters values
   * @return {@link DtoCmdInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public static DtoCmdInfo create1( String aId, IStridablesList<IDataDef> aArgDefs, IOptionSet aParams ) {
    DtoCmdInfo ainf = new DtoCmdInfo( aId, aParams );
    ainf.setProps( aArgDefs );
    return ainf;
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public ESkClassPropKind kind() {
    return ESkClassPropKind.CMD;
  }

  // ------------------------------------------------------------------------------------
  // IDtoCmdInfo
  //

  @Override
  public IStridablesList<IDataDef> argDefs() {
    if( argDefs == null ) {
      String s = OPDEF_ARG_DEFS.getValue( params() ).asString();
      IStrioReader sr = new StrioReader( new CharInputStreamString( s ) );
      argDefs = StrioUtils.readStridablesList( sr, DataDefKeeper.KEEPER );
    }
    return argDefs;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets properties.
   *
   * @param aArgDefs {@link IStridablesList}&lt;{@link IDataDef}&gt; - the arguments data defs list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public void setProps( IStridablesList<IDataDef> aArgDefs ) {
    TsNullArgumentRtException.checkNull( aArgDefs );
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    StrioUtils.writeStridablesList( sw, aArgDefs, DataDefKeeper.KEEPER, false );
    OPDEF_ARG_DEFS.setValue( params(), avStr( sb.toString() ) );
    argDefs = null; // reset cached defs
  }

}
