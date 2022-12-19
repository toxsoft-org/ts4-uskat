package org.toxsoft.uskat.s5.server.backend.supports.gwiddb;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.gwiddb.IS5Resources.*;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.persistence.*;

import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Sectioned key-value (GWID-STRING) database entity.
 *
 * @author mvk
 */
@Entity
public class S5GwidDbEntity
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Шаблон формирования toString для узлов {@link S5GwidDbEntity}
   */
  private static final String TO_STRING_FORMAT = "%s [id=%s, size=%d, zip=%d]"; //$NON-NLS-1$

  /**
   * Минимальный размер сериализованных данных, выше которого сжимаются данные
   */
  private static final int ZIP_SIZE_MIN = 4096;

  /**
   * Первичный составной (classId,strid) ключ
   */
  @EmbeddedId
  private S5GwidDbID id;

  /**
   * Данные
   */
  @Column( insertable = true, updatable = true, nullable = false, unique = false, length = Integer.MAX_VALUE )
  @Lob
  private byte[] data;

  /**
   * Размер(байт) данных
   */
  @Column( insertable = true, updatable = true, nullable = false, unique = false )
  private Integer size;

  /**
   * Размер сжатых данных (zip). null: данные не сжаты.
   */
  @Column( insertable = true, updatable = true, nullable = true, unique = false )
  private Integer zip;

  /**
   * Значения blob-данного в виде массива байт. null: еще не определено после десериализации
   */
  private transient byte[] blob;

  /**
   * Конструктор без параметров
   */
  public S5GwidDbEntity() {
  }

  /**
   * Конструктор без параметров
   *
   * @param aId {@link S5GwidDbID} идентификатор данного
   * @param aBlob byte[] данные lob
   */
  public S5GwidDbEntity( S5GwidDbID aId, byte[] aBlob ) {
    TsNullArgumentRtException.checkNulls( aId, aBlob );
    id = TsNullArgumentRtException.checkNull( aId );
    setBlob( aBlob );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает идентификатор значения (первичный ключ).
   *
   * @return {@link S5GwidDbID} идентификатор lob: неопределен (новое данное)
   */
  public S5GwidDbID id() {
    return id;
  }

  /**
   * Возвращает данные lob ввиде массива байт
   *
   * @return byte[] массив байт
   */
  public byte[] getBlob() {
    if( blob == null ) {
      blob = data;
      if( zip != null ) {
        // Восстановление
        blob = unzip( data );
      }
    }
    return blob;
  }

  /**
   * Возвращает размер(байт) данных узла
   *
   * @return Integer размер данных узла
   */
  public Integer size() {
    return size;
  }

  /**
   * Возвращает размер(байт) сжатых данных (zip)
   *
   * @return Integer размер сжатых данных . null: нет сжатия
   */
  public Integer zipOrNull() {
    return size;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Устанавливает данные lob
   *
   * @param aBlob byte[] данные lob
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setBlob( byte[] aBlob ) {
    TsNullArgumentRtException.checkNull( aBlob );
    blob = aBlob;
    data = aBlob;
    size = Integer.valueOf( data.length );
    zip = null;
    if( size.intValue() > ZIP_SIZE_MIN ) {
      // Сжатие
      data = zip( data );
      zip = Integer.valueOf( data.length );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return format( TO_STRING_FORMAT, getClass().getSimpleName(), id, size, zip );
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( !(aObj instanceof S5GwidDbEntity other) ) {
      return false;
    }
    if( !id.equals( other.id() ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Сжатие массива байт
   *
   * @param aData byte[] исходный массив байт
   * @return byte[] сжатый массив байт
   * @throw TsNullArgumentRtException аргумен = null
   */
  private byte[] zip( byte[] aData ) {
    TsNullArgumentRtException.checkNull( aData );
    try {
      int length = aData.length;
      try( ByteArrayOutputStream baos = new ByteArrayOutputStream( length );
          GZIPOutputStream zos = new GZIPOutputStream( baos, length ) ) {
        zos.write( aData );
        zos.finish();
        zos.flush();
        return baos.toByteArray();
      }
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( ERR_ZIP_UNEXPECTED, this, cause( e ) );
    }
  }

  /**
   * Восстановление массива байт
   *
   * @param aData byte[] сжатый массив байт
   * @return byte[] восстановленный массив байт
   * @throws TsNullArgumentRtException аргумен = null
   */
  private byte[] unzip( byte[] aData ) {
    TsNullArgumentRtException.checkNull( aData );
    int length = size.intValue();
    byte[] retValue = new byte[length];
    try {
      try( ByteArrayInputStream bais = new ByteArrayInputStream( aData );
          GZIPInputStream zis = new GZIPInputStream( bais, length ); ) {
        zis.read( retValue, 0, length );
        zis.available();
        return retValue;
      }
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( ERR_UNZIP_UNEXPECTED, this, cause( e ) );
    }
  }
}
