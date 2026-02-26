package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.memtext.ISkResources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.files.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.backend.metainf.*;

/**
 * {@link MtbAbstractBackend} implementation which stores data in {@link File}.
 *
 * @author hazard157
 */
public class MtbBackendToFile
    extends MtbAbstractBackend {

  /**
   * The backend provider singleton.
   */
  public static final ISkBackendProvider PROVIDER = new ISkBackendProvider() {

    @Override
    public ISkBackendMetaInfo getMetaInfo() {
      return MtbBackendToFileMetaInfo.INSTANCE;
    }

    @Override
    public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
      return new MtbBackendToFile( aFrontend, aArgs );
    }
  };

  /**
   * ID of this backend returned as {@link ISkBackendInfo#id()}.
   */
  public static final String BACKEND_ID = IBackendMemtextConstants.SKB_ID_MEMTEXT + ".file"; //$NON-NLS-1$

  /**
   * Backend arg option: path to the file.
   */
  public static final IDataDef OPDEF_FILE_PATH =
      DataDef.create( MtbBackendToFile.class.getSimpleName() + ".FilePath", STRING, //$NON-NLS-1$
          TSID_NAME, STR_OP_FILE_PATH, //
          TSID_DESCRIPTION, STR_OP_FILE_PATH_D, //
          // this option must match constants from TSGUI
          "org.toxsoft.valed.option.EditorFactoryName", "ts.valed.AvStringFile", //$NON-NLS-1$//$NON-NLS-2$
          "org.toxsoft.valed.option.File.IsOpenDialog", AV_TRUE, //
          TSID_IS_MANDATORY, AV_TRUE //
      );

  /**
   * Backend arg option: seconds between automatic save to file. <= 0 - no autosave.
   * <p>
   * Note: passed time between saves is determined at each backend API call in
   * {@link MtbAbstractBackend#internalCheck()} method. If backend is not called for a long time no autosave will be
   * performed. However, content will be saved at backend close.
   */
  public static final IDataDef OPDEF_AUTO_SAVE_SECS =
      DataDef.create( MtbBackendToFile.class.getSimpleName() + ".AutoSaveSecs", INTEGER, //$NON-NLS-1$
          TSID_DEFAULT_VALUE, avInt( 20 ), //
          TSID_NAME, STR_OP_AUTO_SAVE_SECS, //
          TSID_DESCRIPTION, STR_OP_AUTO_SAVE_SECS_D //
      );

  /**
   * Moment of time when data was stored last time.
   * <p>
   * Is set in {@link #saveToFile()}. after successful write.
   */
  private long lastSaveTime = 0L;

  /**
   * Constructor.
   *
   * @param aFrontend {@link ISkFrontendRear} - the backside of the frontend
   * @param aArgs {@link ITsContextRo} - backend argument options and references
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBackendToFile( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    super( aFrontend, aArgs, BACKEND_ID, IOptionSet.NULL );
    if( !argContext().params().hasValue( OPDEF_FILE_PATH ) ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_NO_FILE_NAME_SPECIFIED, OPDEF_FILE_PATH.id() );
    }
    String fileName = OPDEF_FILE_PATH.getValue( argContext().params() ).asString();
    File f = new File( fileName );
    TsFileUtils.checkFileAppendable( f );
    // non-existing and zero-length files are considered as new file creation
    if( f.exists() && f.length() > 0 ) {
      loadFromFile();
    }
    else {
      saveToFile(); // save an empty file with necessary formating
    }
    setBackendInfoParamsOpton( OPDEF_FILE_PATH.id(), AvUtils.avStr( f.getAbsolutePath() ) );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractBackend
  //

  /**
   * Checks last save time and if it exceeds {@link #OPDEF_AUTO_SAVE_SECS} option value then saves content to file.
   */
  @Override
  protected void doInternalCheck() {
    if( isChanged() ) {
      long interval = OPDEF_AUTO_SAVE_SECS.getValue( argContext().params() ).asLong() * 1000L;
      if( interval > 0 ) {
        long t0 = System.currentTimeMillis();
        if( t0 - lastSaveTime > interval ) {
          saveToFile();
        }
      }
    }
  }

  @Override
  protected void doClose() {
    saveToFile();
  }

  // ------------------------------------------------------------------------------------
  // API класса
  //

  /**
   * Loads the content of the file specified as {@link #OPDEF_FILE_PATH} option.
   */
  public void loadFromFile() {
    File f = new File( OPDEF_FILE_PATH.getValue( argContext().params() ).asString() );
    try( ICharInputStreamCloseable chIn = new CharInputStreamFile( f ) ) {
      IStrioReader dr = new StrioReader( chIn );
      read( dr );
    }
  }

  /**
   * Saves the current contetn to the file specified as {@link #OPDEF_FILE_PATH} option.
   */
  public void saveToFile() {
    File f = new File( OPDEF_FILE_PATH.getValue( argContext().params() ).asString() );
    try( ICharOutputStreamCloseable chOut = new CharOutputStreamFile( f ) ) {
      IStrioWriter dw = new StrioWriter( chOut );
      write( dw );
      lastSaveTime = System.currentTimeMillis();
    }
  }

}
