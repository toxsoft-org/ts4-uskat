package org.toxsoft.uskat.mnemo.lib;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Mnemos section is separate mnemoschemes set.
 *
 * @author hazard157
 */
public interface ISkMnemoSection
    extends IStridable {

  /**
   * Returns all mnemoschemes in this section.
   *
   * @return {@link IStridablesList}&lt;{@link ISkMnemoCfg}&gt; - list of section mnemos
   */
  IStridablesList<ISkMnemoCfg> listMnemos();

  /**
   * Finds the mnemoscheme by ID.
   *
   * @param aMnemoId String - the menmo ID
   * @return {@link ISkMnemoCfg} - found menmoscheme or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkMnemoCfg findMnemo( String aMnemoId );

  /**
   * Retruns the mnemoscheme by ID.
   *
   * @param aMnemoId String - the menmo ID
   * @return {@link ISkMnemoCfg} - found menmoscheme
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no mnemo with the specified ID
   */
  ISkMnemoCfg getMnemo( String aMnemoId );

  /**
   * Creates new or updates an existing menmoscheme.
   * <p>
   * For an existing mnemo only attributes are changed while content (config data) remains intact.
   *
   * @param aMnemoId String - the menmo ID
   * @param aAttrs {@link IOptionSet} - new values of {@link ISkObject#attrs()} of menmo
   * @return {@link ISkMnemoCfg} - new or updated mnemo
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no mnemo with the specified ID
   */
  ISkMnemoCfg defineMnemo( String aMnemoId, IOptionSet aAttrs );

  /**
   * Sets the configuration data of the existing menmo.
   *
   * @param aMnemoId String - the menmo ID
   * @param aData String - configuration data is the same as {@link ISkMnemoCfg#cfgData()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no mnemo with the specified ID
   */
  void setMnemoData( String aMnemoId, String aData );

  /**
   * Returns the menmoscheme configuration data.
   *
   * @param aMnemoId String - the menmo ID
   * @return String - configuration data is the same as {@link ISkMnemoCfg#cfgData()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no mnemo with the specified ID
   */
  String getMnemoData( String aMnemoId );

  /**
   * Removes mnemoscheme.
   * <p>
   * Does nothing is menmoscheme does not exists.
   *
   * @param aMnemoId String - the menmo ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeMnemo( String aMnemoId );

}
