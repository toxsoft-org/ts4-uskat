package org.toxsoft.uskat;

import java.nio.charset.Charset;

import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import io.netty.util.CharsetUtil;

/**
 * Конвертер текстового представления системного описания uskat.
 *
 * @author mvk
 */
@SuppressWarnings( "unused" )
public class UskatSQLFormatConverter {

  /**
   * Кодировка ISO_8859_1 игнорирует наборы символов кодовых позиций 0—31 и 127—159. Это позволяет без ошибок читать и
   * сохранять текстовые файлы содержащие бинарные данные(как есть), например LOB
   */
  private static final Charset CHARSET = CharsetUtil.ISO_8859_1;

  /**
   * sk.link.RightClassIds=\"{s5.Server}\" => sk.link.RightClassIds=@StringList{{s5.Server}}
   */
  private static final String LOOKUP_TEXT1 = "sk.link.RightClassIds=\\\""; //$NON-NLS-1$

  /**
   * sk.link.LinkConstraint=\"1,0x7\" => sk.link.LinkConstraint=@CollConstraint{1,0x7}
   */
  private static final String LOOKUP_TEXT2 = "sk.link.LinkConstraint=\\\""; //$NON-NLS-1$

  /**
   * sk.TypeId=\"VALOBJ\" => sk.TypeId=@AtomicType{VALOBJ}
   */
  private static final String LOOKUP_TEXT3 = "sk.AtomicType=\\\""; //$NON-NLS-1$

  /**
   * ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=16 =>
   */
  private static final String LOOKUP_TEXT4 = "ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=16"; //$NON-NLS-1$

  /**
   * {gdpStatus,{sk.TypeId=\"VALOBJ\",ts.Name=\"Статус\",ts.Description=\"Статус разработки\"}},
   * <p>
   * (в кодировке ISO_8859_1)
   */
  private static final String LOOKUP_TEXT5 =
      "{gdpStatus,{sk.TypeId=\\\"VALOBJ\\\",ts.Name=\\\"Ð¡ÑÐ°ÑÑÑ\\\",ts.Description=\\\"Ð¡ÑÐ°ÑÑÑ ÑÐ°Ð·ÑÐ°Ð±Ð¾ÑÐºÐ¸\\\"}},"; //$NON-NLS-1$

  /**
   * {noChat,{sk.TypeId="Boolean",ts.Name="Запрет на чат",ts.Description="Запрет на ввод текстовых сообщений"}},
   * <p>
   * (в кодировке ISO_8859_1)
   */
  private static final String APPEND_TEXT5 =
      "{noChat,{sk.TypeId=\\\"Boolean\\\",ts.Name=\\\"ÐÐ°Ð¿ÑÐµÑ Ð½Ð° ÑÐ°Ñ\\\",ts.Description=\\\"ÐÐ°Ð¿ÑÐµÑ Ð½Ð° Ð²Ð²Ð¾Ð´ ÑÐµÐºÑÑÐ¾Ð²ÑÑ ÑÐ¾Ð¾Ð±ÑÐµÐ½Ð¸Ð¹\\\"}},"; //$NON-NLS-1$

  /**
   * Точка входа в программу
   *
   * @param aArgs String[] аргументы программы
   */
  public static void main( String[] aArgs ) {
    // File oldFile = new File( "uskat-old.sql" );
    // File newFile = new File( "uskat-new.sql" );
    //
    // if( aArgs.length > 0 ) {
    // oldFile = new File( aArgs[0] );
    // }
    // if( aArgs.length > 1 ) {
    // newFile = new File( aArgs[1] );
    // }
    // if( !oldFile.exists() ) {
    // System.err.println( oldFile + ": file not found" );
    // return;
    // }
    // try {
    // // Чтение файла и запись в сервер
    // StringBuilder sb = new StringBuilder();
    // try( FileInputStream input = new FileInputStream( oldFile );
    // InputStreamReader reader = new InputStreamReader( input, CHARSET ); ) {
    // while( reader.ready() ) {
    // sb.append( (char)reader.read() );
    // }
    // }
    // String text = sb.toString();
    // System.out.println( "size = " + text.length() );
    //
    // int count = 0;
    // try( FileOutputStream output = new FileOutputStream( newFile );
    // OutputStreamWriter writer = new OutputStreamWriter( output, CHARSET ); ) {
    // int fromIndex = 0;
    // while( true ) {
    // String[] lookups = { //
    // LOOKUP_TEXT1, //
    // LOOKUP_TEXT2, //
    // LOOKUP_TEXT3, //
    // LOOKUP_TEXT4, //
    // LOOKUP_TEXT5, //
    // };
    // // Поиск следующей перестановки
    // Pair<Integer, Integer> substitution = findSubstitution( lookups, text, fromIndex );
    // if( substitution == null ) {
    // writer.write( text.substring( fromIndex ) );
    // break;
    // }
    // String lookup = lookups[substitution.left().intValue()];
    // int lookupIndex = substitution.right().intValue();
    // count++;
    // // Замена текста1
    // if( lookup.equals( LOOKUP_TEXT1 ) ) {
    // System.out.println( "replace(1) text at " + lookupIndex );
    // int startIndex = lookupIndex + LOOKUP_TEXT1.length() + 1;
    // int endIndex = text.indexOf( "\\\"", startIndex );
    // StridReader sr = new StridReader( new CharInputStreamString( text, startIndex, endIndex ) );
    // IStringListEdit list = new StringArrayList();
    // for( char c = sr.nextChar(); c != IStridHardConstants.CHAR_EOF; c = sr.nextChar() ) {
    // switch( c ) {
    // case ',':
    // break;
    // case '}':
    // break;
    // default:
    // sr.putCharBack();
    // list.add( sr.readIdPath() );
    // break;
    // }
    // }
    // writer.write( text.substring( fromIndex, startIndex - 3 ) ); // \"
    // writer.write( '@' );
    // writer.write( StringListKeeper.KEEPER_ID );
    // writer.write( '{' );
    // writer.write( StringListKeeper.KEEPER.toStr( list ) );
    // writer.write( '}' );
    // fromIndex = endIndex + 2; // \"
    // }
    // // Замена текста2
    // if( lookup.equals( LOOKUP_TEXT2 ) ) {
    // System.out.println( "replace(2) text at " + lookupIndex );
    // int startIndex = lookupIndex + LOOKUP_TEXT2.length();
    // int endIndex = text.indexOf( "\\\"", startIndex );
    // StridReader sr = new StridReader( new CharInputStreamString( text, startIndex, endIndex ) );
    // int maxCount = sr.readInt();
    // sr.ensureSeparatorChar();
    // int flags = sr.readInt();
    //
    // boolean exactCount = ((flags & 0x0001) != 0);
    // boolean emptyProhibited = ((flags & 0x0002) != 0);
    // boolean duplicatesProhibited = ((flags & 0x0004) != 0);
    //
    // writer.write( text.substring( fromIndex, startIndex - 3 ) ); // \"
    // writer.write( '=' );
    // writer.write( '@' );
    // writer.write( CollConstraintKeeper.KEEPER_ID );
    // writer.write( '{' );
    // writer.write( CollConstraintKeeper.KEEPER
    // .toStr( new CollConstraint( maxCount, exactCount, emptyProhibited, duplicatesProhibited ) ) );
    // writer.write( '}' );
    // fromIndex = endIndex + 2; // \"
    // }
    // // Замена текста3
    // if( lookup.equals( LOOKUP_TEXT3 ) ) {
    // System.out.println( "replace(3) text at " + lookupIndex );
    // int startIndex = lookupIndex + LOOKUP_TEXT3.length();
    // int endIndex = text.indexOf( "\\\"", startIndex );
    // StridReader sr = new StridReader( new CharInputStreamString( text, startIndex, endIndex ) );
    // EAtomicType atomicType = EAtomicType.findById( sr.readIdName() );
    // writer.write( text.substring( fromIndex, startIndex - 3 ) ); // \"
    // writer.write( '=' );
    // writer.write( '@' );
    // writer.write( AtomicTypeKeeper.KEEPER_ID );
    // writer.write( '{' );
    // writer.write( AtomicTypeKeeper.KEEPER.toStr( atomicType ) );
    // writer.write( '}' );
    // fromIndex = endIndex + 2; // \"
    // }
    // // Замена текста4
    // if( lookup.equals( LOOKUP_TEXT4 ) ) {
    // System.out.println( "replace(4) text at " + lookupIndex );
    // int startIndex = lookupIndex;
    // int endIndex = lookupIndex + LOOKUP_TEXT4.length();
    //
    // writer.write( text.substring( fromIndex, startIndex ) );
    // fromIndex = endIndex;
    // }
    // // Замена текста5
    // if( lookup.equals( LOOKUP_TEXT5 ) ) {
    // System.out.println( "replace(5) text at " + lookupIndex );
    // int endIndex = lookupIndex + LOOKUP_TEXT5.length();
    //
    // writer.write( text.substring( fromIndex, endIndex ) );
    // writer.write( APPEND_TEXT5 ); // \"
    // fromIndex = endIndex;
    // }
    // }
    // }
    // System.out.println( "replace count = " + count );
    // }
    // catch( IOException e ) {
    // e.printStackTrace();
    // }
  }

  /**
   * Поиск позиции в указанном тексте указанных строк
   *
   * @param aLookups String[] искомые строки
   * @param aText String текст в котором проводится поиск
   * @param aFromIndex int позиция с которой проводить поиск
   * @return {@link Pair}&lt;Integer, Integer&gt; пара индекcов найденных строк. null: строки не найдены
   *         <p>
   *         {@link Pair#left()} : индекс в aLookups найденной строки<br>
   *         {@link Pair#right()}: индекс в aText найденной строки.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static Pair<Integer, Integer> findSubstitution( String[] aLookups, String aText, int aFromIndex ) {
    TsNullArgumentRtException.checkNulls( aLookups, aText );
    int count = aLookups.length;
    int[] lookupIndexes = new int[count];
    for( int index = 0; index < count; index++ ) {
      lookupIndexes[index] = aText.indexOf( aLookups[index], aFromIndex );
    }

    // if( lookupIndex1 >= 0 && //
    // (lookupIndex2 < 0 || lookupIndex1 < lookupIndex2) && //
    // (lookupIndex3 < 0 || lookupIndex1 < lookupIndex3) && //
    // (lookupIndex4 < 0 || lookupIndex1 < lookupIndex4) && //
    // (lookupIndex5 < 0 || lookupIndex1 < lookupIndex5) ) {
    // lookupIndex = lookupIndex1;
    // }
    topLoop:
    for( int index = 0; index < count; index++ ) {
      int lookupIndex1 = lookupIndexes[index];
      if( lookupIndex1 < 0 ) {
        continue;
      }
      for( int index2 = 0; index2 < count; index2++ ) {
        if( index == index2 ) {
          continue;
        }
        int lookupIndex2 = lookupIndexes[index2];
        if( ((lookupIndex2 >= 0) && (lookupIndex1 >= lookupIndex2)) ) {
          continue topLoop;
        }
      }
      return new Pair<>( Integer.valueOf( index ), Integer.valueOf( lookupIndex1 ) );
    }
    return null;
  }
}
