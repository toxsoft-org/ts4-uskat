package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.uskat.backend.memtext.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.txtproj.lib.*;
import org.toxsoft.core.txtproj.lib.impl.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.backend.metainf.*;

/**
 * {@link MtbAbstractBackend} implementation which stores data as {@link IProjDataUnit} in {@link ITsProject}.
 *
 * @author hazard157
 */
public class MtbBackendToTsProj
    extends MtbAbstractBackend {

  /**
   * The backend provider singleton.
   */
  public static final ISkBackendProvider PROVIDER = new ISkBackendProvider() {

    @Override
    public ISkBackendMetaInfo getMetaInfo() {
      return MtbBackendToTsProjMetaInfo.INSTANCE;
    }

    @Override
    public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
      return new MtbBackendToTsProj( aFrontend, aArgs );
    }
  };

  /**
   * ID of this backend returned as {@link ISkBackendInfo#id()}.
   */
  public static final String BACKEND_ID = IBackendMemtextConstants.SKB_ID_MEMTEXT + ".tsproj"; //$NON-NLS-1$

  /**
   * Default ID of the project unit which stores backend data.
   */
  public static final String DEFAULT_PDU_ID = "USkatData"; //$NON-NLS-1$

  /**
   * Backend arg option: project PDU ID.
   */
  public static final IDataDef OPDEF_PDU_ID = //
      DataDef.create( MtbBackendToTsProj.class.getSimpleName() + ".PduId", STRING, //$NON-NLS-1$
          TSID_NAME, STR_N_OP_PDU_ID, //
          TSID_DESCRIPTION, STR_D_OP_PDU_ID, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, avStr( DEFAULT_PDU_ID ) //
      );

  /**
   * Ссылка на экземпляр проекта {@link ITsProject}.
   */
  public static final ITsContextRefDef<ITsProject> REFDEF_PROJECT = //
      TsContextRefDef.create( MtbBackendToTsProj.class.getSimpleName() + ".Project", ITsProject.class, //$NON-NLS-1$
          TSID_NAME, STR_N_REF_PROJECT, //
          TSID_DESCRIPTION, STR_D_REF_PROJECT, //
          TSID_IS_MANDATORY, AV_TRUE //
      );

  /**
   * Project unit {@link IProjDataUnit} implementation to store backend data.
   *
   * @author hazard157
   */
  class PduBackend
      extends AbstractProjDataUnit
      implements ICloseable {

    public PduBackend() {
      MtbBackendToTsProj.this.genericChangeEventer().addListener( genericChangeEventer() );
    }

    @Override
    public void close() {
      MtbBackendToTsProj.this.genericChangeEventer().removeListener( genericChangeEventer() );
    }

    @Override
    protected void doWrite( IStrioWriter aSw ) {
      aSw.writeChar( CHAR_SET_BEGIN );
      aSw.incNewLine();
      MtbBackendToTsProj.this.write( aSw );
      aSw.decNewLine();
      aSw.writeChar( CHAR_SET_END );
    }

    @Override
    protected void doRead( IStrioReader aSr ) {
      aSr.ensureChar( CHAR_SET_BEGIN );
      MtbBackendToTsProj.this.read( aSr );
      aSr.ensureChar( CHAR_SET_END );
    }

    @Override
    protected void doClear() {
      MtbBackendToTsProj.this.clear();
    }

  }

  private final ITsProject project;
  private final String     pduId;
  private final PduBackend pduBackend;

  /**
   * Constructor.
   *
   * @param aFrontend {@link ISkFrontendRear} - the backside of the frontend
   * @param aArgs {@link ITsContextRo} - backend argument options and references
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBackendToTsProj( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    super( aFrontend, aArgs, BACKEND_ID, IOptionSet.NULL );
    // PDU ID and project
    String tmpId = OPDEF_PDU_ID.getValue( aArgs.params() ).asString();
    if( !StridUtils.isValidIdPath( tmpId ) ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_INV_PDU_ID, OPDEF_PDU_ID.id(), tmpId );
    }
    project = REFDEF_PROJECT.getRef( aArgs );
    pduId = tmpId;
    // создание раздела/чтение данных
    pduBackend = new PduBackend();
    project.registerUnit( pduId, pduBackend, true );
    setBackendInfoParamsOpton( OPDEF_PDU_ID.id(), avStr( pduId ) );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractBackend
  //

  @Override
  protected void doInternalCheck() {
    // nop
  }

  @Override
  protected void doClose() {
    if( project != null && pduId != null ) {
      pduBackend.close();
      project.unregisterUnit( pduId );
    }
  }

}
