package org.toxsoft.uskat.s5.utils;

import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.nio.charset.Charset;

import org.toxsoft.core.tslib.bricks.strio.chario.ICharInputStream;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.CharInputStreamString;
import org.toxsoft.core.tslib.utils.errors.TsIoRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Вспомогательные методы для работы с пакетом ru.toxsoft.tslib.strids
 *
 * @author mvk
 */
public class S5StridUtils {

  /**
   * Полностью загрузить файл в поток чтения символов
   *
   * @param aFile {@link File} читаемый файл
   * @return {@link ICharInputStream} поток чтения символьных данных
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException ошибка чтения
   */
  public static ICharInputStream loadCharInputStreamFromFile( File aFile ) {
    return loadCharInputStreamFromFile( aFile, UTF_8 );
  }

  /**
   * Полностью загрузить файл в поток чтения символов
   *
   * @param aFile {@link File} читаемый файл
   * @param aCharset String кодировка читаемых символов
   * @return {@link ICharInputStream} поток чтения символьных данных
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException ошибка чтения
   */
  public static ICharInputStream loadCharInputStreamFromFile( File aFile, Charset aCharset ) {
    TsNullArgumentRtException.checkNulls( aFile, aCharset );
    try {
      try( FileInputStream file = new FileInputStream( aFile ) ) {
        byte[] buffer = new byte[file.available()];
        file.read( buffer );
        String text = new String( buffer, aCharset );
        return new CharInputStreamString( text );
      }
    }
    catch( IOException e ) {
      throw new TsIoRtException( e );
    }
  }
}
