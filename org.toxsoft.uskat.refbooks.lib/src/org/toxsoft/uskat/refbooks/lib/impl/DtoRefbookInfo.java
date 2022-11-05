package org.toxsoft.uskat.refbooks.lib.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.refbooks.lib.*;

/**
 * {@link IDtoRefbookInfo} implementation.
 *
 * @author hazard157
 */
public class DtoRefbookInfo
    extends StridableParameterized
    implements IDtoRefbookInfo {

  private final IStridablesListEdit<DtoAttrInfo>  attrInfos  = new StridablesList<>();
  private final IStridablesListEdit<DtoClobInfo>  clobInfos  = new StridablesList<>();
  private final IStridablesListEdit<DtoRivetInfo> rivetInfos = new StridablesList<>();
  private final IStridablesListEdit<DtoLinkInfo>  linkInfos  = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aId String - the refbook ID (IDpath)
   * @param aParams {@link IOptionSet} - {@link #params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public DtoRefbookInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static copy constructor.
   *
   * @param aSource {@link IDtoRefbookInfo} - the source
   * @return {@link DtoRefbookInfo} - deep copy of source
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException invalid source
   */
  public static DtoRefbookInfo createDeepCopy( IDtoRefbookInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    DtoRefbookInfo dto = new DtoRefbookInfo( aSource.id(), aSource.params() );
    dto.attrInfos().setAll( aSource.attrInfos() );
    dto.clobInfos().setAll( aSource.clobInfos() );
    dto.rivetInfos().setAll( aSource.rivetInfos() );
    dto.linkInfos().setAll( aSource.linkInfos() );

    return dto;
  }

  // ------------------------------------------------------------------------------------
  // IDtoRefbookInfo
  //

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoAttrInfo> attrInfos() {
    return (IStridablesListEdit)attrInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoRivetInfo> rivetInfos() {
    return (IStridablesListEdit)rivetInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoLinkInfo> linkInfos() {
    return (IStridablesListEdit)linkInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoClobInfo> clobInfos() {
    return (IStridablesListEdit)clobInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public <T extends IDtoClassPropInfoBase> IStridablesList<T> propInfos( ESkClassPropKind aKind ) {
    TsNullArgumentRtException.checkNull( aKind );
    return switch( aKind ) {
      case ATTR -> (IStridablesList)attrInfos;
      case RIVET -> (IStridablesList)rivetInfos;
      case CLOB -> (IStridablesList)clobInfos;
      case RTDATA -> IStridablesList.EMPTY;
      case LINK -> (IStridablesList)linkInfos;
      case CMD -> IStridablesList.EMPTY;
      case EVENT -> IStridablesList.EMPTY;
    };
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @SuppressWarnings( "boxing" )
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder( super.toString() );
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      IStridablesList<?> cp = propInfos( k );
      sb.append( String.format( ", %s[%d]", k.id(), cp.size() ) ); //$NON-NLS-1$
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = PRIME * result + attrInfos.hashCode();
    result = PRIME * result + rivetInfos.hashCode();
    result = PRIME * result + clobInfos.hashCode();
    result = PRIME * result + linkInfos.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( super.equals( aThat ) ) {
      if( aThat instanceof DtoRefbookInfo that ) {
        return this.attrInfos.equals( that.attrInfos ) && //
            this.rivetInfos.equals( that.rivetInfos ) && //
            this.clobInfos.equals( that.clobInfos ) && //
            this.linkInfos.equals( that.linkInfos );
      }
    }
    return false;
  }

}
