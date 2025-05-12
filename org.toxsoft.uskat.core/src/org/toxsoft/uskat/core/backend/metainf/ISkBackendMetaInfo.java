package org.toxsoft.uskat.core.backend.metainf;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Meta-information about backend usage and creation arguments.
 * <p>
 * The {@link #id()} must be the same as the {@link ISkBackendInfo#id()}. The name {@link #nmName()} and
 * {@link #description()} is used simply to distinguish backends in the multi-backend environments such as SkIDE.
 *
 * @author hazard157
 */
public sealed interface ISkBackendMetaInfo
    extends IStridable
    permits SkBackendMetaInfo {

  /**
   * Returns the options used as the arguments of the backend creation.
   *
   * @return {@link IStridablesList}&lt;{@link IDataDef}&gt; - list of the argument option definitions
   */
  IStridablesList<IDataDef> argOps();

  /**
   * Returns the references used as the arguments of the backend creation.
   *
   * @return {@link IStringMap}&lt;{@link ITsContextRefDef}&gt; - map "ref key" - "ref definition"
   */
  IStringMap<ITsContextRefDef<?>> argRefs();

  /**
   * Returns authentification type reqiured by the backend.
   *
   * @return {@link ESkAuthentificationType} - authentification type
   */
  ESkAuthentificationType getAuthentificationType();

  /**
   * Checks the argument options of the Sk-connection opening by the method {@link ISkConnection#open(ITsContextRo)}.
   *
   * @param aArgOptions {@link IOptionSet} - the arguments options
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult checkOptions( IOptionSet aArgOptions );

  /**
   * Checks the arguments of the Sk-connection opening by the method {@link ISkConnection#open(ITsContextRo)}.
   * <p>
   * In addition to {@link #checkOptions(IOptionSet)} also checks references in the context, if needed.
   *
   * @param aArgs {@link ITsContextRo} - the arguments
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult checkArguments( ITsContextRo aArgs );

}
