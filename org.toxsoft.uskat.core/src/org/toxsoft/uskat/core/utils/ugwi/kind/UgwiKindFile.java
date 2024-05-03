package org.toxsoft.uskat.core.utils.ugwi.kind;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.utils.ugwi.kind.ITsResources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * The UGWI kind storing {@link File} text representation.
 * <p>
 * The essence of UGWI is the file path {@link File#getPath()}.
 * <p>
 * Note: {@link Ugwi#namespace()} is not used for this kind.
 *
 * @author hazard157
 */
public class UgwiKindFile
    extends UgwiKind {

  /**
   * The registered kind ID.
   */
  public static final String KIND_ID = "file"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final IUgwiKind INSTANCE = new UgwiKindFile();

  /**
   * Constructor.
   */
  public UgwiKindFile() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_FILE, //
        TSID_DESCRIPTION, STR_UK_FILE_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link File} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link File} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link #validateUgwi(Ugwi)}
   */
  public File getFile( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( validateUgwi( aUgwi ) );
    return new File( aUgwi.essence() );
  }

}
