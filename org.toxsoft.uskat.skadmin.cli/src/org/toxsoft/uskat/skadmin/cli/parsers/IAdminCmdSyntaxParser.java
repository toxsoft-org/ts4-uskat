package org.toxsoft.uskat.skadmin.cli.parsers;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Интерфейс модуля синтаксического анализа вводимых команд, аргументов, данных
 *
 * @author mvk
 */
public interface IAdminCmdSyntaxParser {

  /**
   * Провести анализ строки и сформировать лексемы лексического или синтаксического анализа
   *
   * @param aLine String - строка для анализа
   * @return {@link IList} &lt;{@link IAdminCmdToken}&gt; - список лексем
   * @throws TsNullArgumentRtException аргумент = null
   */
  IList<IAdminCmdToken> parse( String aLine );
}
