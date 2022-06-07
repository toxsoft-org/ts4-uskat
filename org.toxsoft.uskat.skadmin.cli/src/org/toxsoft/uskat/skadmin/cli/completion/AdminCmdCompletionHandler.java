package org.toxsoft.uskat.skadmin.cli.completion;

import java.io.IOException;
import java.util.List;

import scala.tools.jline.console.ConsoleReader;
import scala.tools.jline.console.CursorBuffer;
import scala.tools.jline.console.completer.CandidateListCompletionHandler;

/**
 * Обработчик автодополнения ввода
 *
 * @author mvk
 */
public class AdminCmdCompletionHandler
    extends CandidateListCompletionHandler {

  // ------------------------------------------------------------------------------------
  // Переопределение CandidateListCompletionHandler
  //
  @Override
  public boolean complete( final ConsoleReader reader, final List<CharSequence> candidates, final int pos )
      throws IOException {
    CursorBuffer buf = reader.getCursorBuffer();

    // if there is only one completion, then fill in the buffer
    if( candidates.size() == 1 ) {
      String candidate = (String)candidates.get( 0 );
      // Вырезаем escape-последовательности
      CharSequence value = ConsoleReader.stripAnsi( candidate );

      // fail if the only candidate is the same as the current buffer
      if( value.equals( buf.toString() ) ) {
        return false;
      }
      setBuffer( reader, value, pos );
      return true;
    }
    if( candidates.size() > 1 ) {
      String value = getUnambiguousCompletions( candidates );
      setBuffer( reader, value, pos );
    }
    printCandidates( reader, candidates );
    // redraw the current console buffer
    reader.drawLine();
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Returns a root that matches all the {@link String} elements of the specified {@link List}, or null if there are no
   * commonalities. For example, if the list contains <i>foobar</i>, <i>foobaz</i>, <i>foobuz</i>, the method will
   * return <i>foob</i>.
   *
   * @param aCandidates {@link List} candidates list
   * @return String unambiguous completions
   */
  private static String getUnambiguousCompletions( final List<CharSequence> aCandidates ) {
    if( aCandidates == null || aCandidates.isEmpty() ) {
      return null;
    }

    // convert to an array for speed
    String[] strings = aCandidates.toArray( new String[aCandidates.size()] );
    for( int index = 0, n = strings.length; index < n; index++ ) {
      strings[index] = ConsoleReader.stripAnsi( strings[index] );
    }
    String first = strings[0];
    StringBuilder candidate = new StringBuilder();

    for( int i = 0; i < first.length(); i++ ) {
      if( startsWith( first.substring( 0, i + 1 ), strings ) ) {
        candidate.append( first.charAt( i ) );
      }
      else {
        break;
      }
    }

    return candidate.toString();
  }

  /**
   * @param aStarts String start
   * @param aCandidates String[] candidates array
   * @return true is all the elements of <i>candidates</i> start with <i>starts</i>
   */
  private static boolean startsWith( final String aStarts, final String[] aCandidates ) {
    for( String candidate : aCandidates ) {
      if( !candidate.startsWith( aStarts ) ) {
        return false;
      }
    }

    return true;
  }

}
