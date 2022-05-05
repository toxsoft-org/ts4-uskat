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
 * {@link IDtoEventInfo} implementation.
 * <p>
 * As {@link StridableParameterized} extension this class stores all fields in {@link #params()}:
 * <ul>
 * <li>{@link #paramDefs()} - as option {@link IDtoHardConstants#OPDEF_PARAM_DEFS}.</li>
 * </ul>
 *
 * @author hazard157
 */
public final class DtoEventInfo
    extends DtoAbstractClassPropInfoBase
    implements IDtoEventInfo {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoEventInfo> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDtoEventInfo.class, null ) {

        @Override
        protected IDtoEventInfo doCreate( String aId, IOptionSet aParams ) {
          return new DtoEventInfo( aId, aParams );
        }
      };

  /**
   * {@link #paramDefs()} cached value, <code>null</code> - not inited yet from {@link #params()}.
   */
  private transient IStridablesList<IDataDef> paramDefs = null;

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - parameters values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  private DtoEventInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aIsHist boolean - historical event flag
   * @param aParamDefs {@link IStridablesList}&lt;{@link IDataDef}&gt; - the parameters data defs list
   * @param aParams {@link IOptionSet} - parameters values
   * @return {@link DtoEventInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public static DtoEventInfo create1( String aId, boolean aIsHist, IStridablesList<IDataDef> aParamDefs,
      IOptionSet aParams ) {
    DtoEventInfo ainf = new DtoEventInfo( aId, aParams );
    ainf.setProps( aIsHist, aParamDefs );
    return ainf;
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public ESkClassPropKind kind() {
    return ESkClassPropKind.EVENT;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public DtoEventInfo makeCopy() {
    return new DtoEventInfo( id(), params() );
  }

  // ------------------------------------------------------------------------------------
  // IDtoEventInfo
  //

  @Override
  public boolean isHist() {
    return OPDEF_IS_HIST.getValue( params() ).asBool();
  }

  @Override
  public IStridablesList<IDataDef> paramDefs() {
    if( paramDefs == null ) {
      String s = OPDEF_PARAM_DEFS.getValue( params() ).asString();
      IStrioReader sr = new StrioReader( new CharInputStreamString( s ) );
      paramDefs = StrioUtils.readStridablesList( sr, DataDefKeeper.KEEPER );
    }
    return paramDefs;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets properties.
   *
   * @param aIsHist boolean - historical event flag
   * @param aParamDefs {@link IStridablesList}&lt;{@link IDataDef}&gt; - the parameters data defs list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public void setProps( boolean aIsHist, IStridablesList<IDataDef> aParamDefs ) {
    TsNullArgumentRtException.checkNull( aParamDefs );
    StringBuilder sb = new StringBuilder();
    IStrioWriter sw = new StrioWriter( new CharOutputStreamAppendable( sb ) );
    StrioUtils.writeStridablesList( sw, aParamDefs, DataDefKeeper.KEEPER, false );
    OPDEF_IS_HIST.setValue( params(), avBool( aIsHist ) );
    OPDEF_PARAM_DEFS.setValue( params(), avStr( sb.toString() ) );
    paramDefs = null; // reset cached defs
  }

}
