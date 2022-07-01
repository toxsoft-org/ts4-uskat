package org.toxsoft.uskat.s5.server.backend.supports.clobs;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.clobs.IS5Resources.*;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.persistence.*;

import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Реализация хранения lob-данных.
 *
 * @author mvk
 */
@Entity
public class S5ClobEntity
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Шаблон формирования toString для узлов {@link S5ClobEntity}
   */
  private static final String TO_STRING_FORMAT = "%s [id=%s, size=%d, zip=%d]"; //$NON-NLS-1$

  /**
   * Минимальный размер сериализованных данных, выше которого сжимаются данные
   */
  private static final int ZIP_SIZE_MIN = 4096;

  /**
   * Первичный ключ
   */
  @Id
  @Column( insertable = true,
      updatable = false,
      nullable = false,
      columnDefinition = "varchar(" + STRID_LENGTH_MAX + ") character set utf8 collate utf8_bin not null" )
  private String id;

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
  public S5ClobEntity() {
  }

  /**
   * Конструктор без параметров
   *
   * @param aId String идентификатор данного
   * @param aBlob byte[] данные lob
   */
  public S5ClobEntity( String aId, byte[] aBlob ) {
    id = TsNullArgumentRtException.checkNull( aId );
    setBlob( aBlob );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает идентификатор lob (первичный ключ).
   *
   * @return String идентификатор lob: неопределен (новое данное)
   */
  public String id() {
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
    if( !(aObj instanceof S5ClobEntity) ) {
      return false;
    }
    S5ClobEntity other = (S5ClobEntity)aObj;
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
      throw new TsInternalErrorRtException( MSG_ERR_ZIP_UNEXPECTED, this, cause( e ) );
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
      throw new TsInternalErrorRtException( MSG_ERR_UNZIP_UNEXPECTED, this, cause( e ) );
    }
  }
}
