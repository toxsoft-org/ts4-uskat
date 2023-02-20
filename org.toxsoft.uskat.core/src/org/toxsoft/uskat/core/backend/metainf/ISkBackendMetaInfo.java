package org.toxsoft.uskat.core.backend.metainf;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Meta-information about backend usage and creation arguments.
 * <p>
 * The ID {@link #id()} (as well as {@link #nmName()} and {@link #description()}) is used simply to distinguish backends
 * in the multi-backend environments such as SkIDE.
 *
 * @author hazard157
 */
public sealed interface ISkBackendMetaInfo
    extends IStridable permits SkBackendMetaInfo {

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
   * Checks the arguments of the Sk-connection openening by the method {@link ISkConnection#open(ITsContextRo)}.
   *
   * @param aArgs {@link ITsContext} - the arguments
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult checkArguments( ITsContext aArgs );

  /**
   * Returns reference to the backend provider singleton.
   *
   * @return {@link ISkBackendProvider} - the backend provider
   */
  ISkBackendProvider provider();

}
