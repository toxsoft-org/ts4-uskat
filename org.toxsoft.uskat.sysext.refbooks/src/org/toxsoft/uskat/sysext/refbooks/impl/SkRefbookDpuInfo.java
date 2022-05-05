package org.toxsoft.uskat.sysext.refbooks.impl;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.refbooks.ISkRefbookDpuInfo;

import ru.uskat.common.dpu.IDpuSdAttrInfo;
import ru.uskat.common.dpu.IDpuSdLinkInfo;
import ru.uskat.common.dpu.impl.DpuSdAttrInfo;
import ru.uskat.common.dpu.impl.DpuSdLinkInfo;

/**
 * {@link ISkRefbookDpuInfo} implementation.
 *
 * @author goga
 */
public final class SkRefbookDpuInfo
    extends Stridable
    implements ISkRefbookDpuInfo {

  /**
   * Singleton keeper.
   */
  public static final IEntityKeeper<ISkRefbookDpuInfo> KEEPER =
      new AbstractEntityKeeper<>( ISkRefbookDpuInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkRefbookDpuInfo aEntity ) {
          aSw.writeAsIs( aEntity.id() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.nmName() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.description() );
          aSw.writeSeparatorChar();
          aSw.writeEol();
          // aIndented = false
          DpuSdAttrInfo.KEEPER.writeColl( aSw, aEntity.itemAttrInfos(), false );
          aSw.writeSeparatorChar();
          aSw.writeEol();
          // aIndented = false
          DpuSdLinkInfo.KEEPER.writeColl( aSw, aEntity.itemLinkInfos(), false );
        }

        @Override
        protected ISkRefbookDpuInfo doRead( IStrioReader aSr ) {
          String id = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          String name = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          String description = aSr.readQuotedString();
          SkRefbookDpuInfo rinf = new SkRefbookDpuInfo( id, name, description );
          aSr.ensureSeparatorChar();
          DpuSdAttrInfo.KEEPER.readColl( aSr, rinf.itemAttrInfos() );
          aSr.ensureSeparatorChar();
          DpuSdLinkInfo.KEEPER.readColl( aSr, rinf.itemLinkInfos() );
          return rinf;
        }
      };

  private final IStridablesListEdit<IDpuSdAttrInfo> itemAttrInfos = new StridablesList<>();
  private final IStridablesListEdit<IDpuSdLinkInfo> itemLinkInfos = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aId String - refbook identifier
   * @param aName String - refbook name
   * @param aDescription String - refbook description
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public SkRefbookDpuInfo( String aId, String aName, String aDescription ) {
    super( aId, aName, aDescription );
  }

  // ------------------------------------------------------------------------------------
  // ISkRefbookDpuInfo
  //

  @Override
  public IStridablesListEdit<IDpuSdAttrInfo> itemAttrInfos() {
    return itemAttrInfos;
  }

  @Override
  public IStridablesListEdit<IDpuSdLinkInfo> itemLinkInfos() {
    return itemLinkInfos;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + id();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof SkRefbookDpuInfo that ) {
      return super.equals( that ) && itemAttrInfos.equals( that.itemAttrInfos )
          && itemLinkInfos.equals( that.itemLinkInfos );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = TsLibUtils.PRIME * result + itemAttrInfos.hashCode();
    result = TsLibUtils.PRIME * result + itemLinkInfos.hashCode();
    return result;
  }

}
