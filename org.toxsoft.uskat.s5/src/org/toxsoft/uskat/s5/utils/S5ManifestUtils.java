package org.toxsoft.uskat.s5.utils;

import static org.toxsoft.core.log4j.Logger.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Manifest;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSession;

/**
 * Вспомогательные методы работы с манифестом
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public class S5ManifestUtils {

  /**
   * Параметры манифества: время сборки библиотеки jar
   */
  private static final String MANIFEST_PARAM_BUILD_DATE = "Built-Date";

  /**
   * Журнал
   */
  private static final ILogger logger = getLogger( S5ManifestUtils.class );

  /**
   * Считывает из манифеста время сборки сервера
   *
   * @return long время (мсек с начала эпохи) сборки сервера. {@link Long#MIN_VALUE} ошибка получения времени
   */
  public static long getBuildTime() {
    // // TODO: mvkd
    // IList<S5ModuleOptionValue> versions = getVersions( EJBContextImpl.class.getClassLoader(),
    // "Implementation-Title",
    // "Implementation-Title", "Implementation-Version", "Build-Timestamp" );
    long buildTime = buildTimeFromManifest( "META-INF/MANIFEST.MF" );
    if( buildTime != Long.MIN_VALUE ) {
      return buildTime;
    }
    buildTime = buildTimeFromProperties( "META-INF/build-timestamp.properties" );
    return buildTime;
  }

  /**
   * Считывает из манифеста описания версий модулей
   *
   * @return long время (мсек с начала эпохи) сборки сервера. {@link Long#MIN_VALUE} ошибка получения времени
   */
  // @SuppressWarnings( { "javadoc" } )
  // public static IList<S5ModuleOptionValue> getVersions( ClassLoader aClassLoader, String aName, String aDescr,
  // String aVersion, String aDate ) {
  // TsNullArgumentRtException.checkNull( aClassLoader );
  // TsNullArgumentRtException.checkNull( aName );
  // TsNullArgumentRtException.checkNull( aDescr );
  // TsNullArgumentRtException.checkNull( aVersion );
  // TsNullArgumentRtException.checkNull( aDate );
  // SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
  // IListEdit<S5ModuleOptionValue> retValue = new ElemArrayList<>();
  // try {
  // Enumeration<URL> resources = aClassLoader.getResources( "META-INF/MANIFEST.MF" );
  // while( resources.hasMoreElements() ) {
  // try( InputStream is = resources.nextElement().openStream() ) {
  // Manifest manifest = new Manifest( is );
  // String id = manifest.getMainAttributes().getValue( aName );
  // String name = "";
  // String descr = manifest.getMainAttributes().getValue( aDescr );
  // String version = manifest.getMainAttributes().getValue( aVersion );
  // String date = manifest.getMainAttributes().getValue( aDate );
  // if( id == null || descr == null || version == null || date == null ) {
  // // Ошибка разбора найденного элемента
  // logger.warning( MSG_ERR_MANIFEST_PARSE, aName, id, aDescr, descr, aVersion, version, aDate, date );
  // continue;
  // }
  // int minorVer = 0;
  // int majorVer = 0;
  // IAtomicValue buildDate = DvUtils.avInt( format.parse( date ).getTime() );
  // IStridablesList<S5ModuleOptionValue> depends = IStridablesList.EMPTY;
  // retValue.add( new S5ModuleOptionValue( id, name, descr, majorVer, minorVer, buildDate, depends ) );
  // }
  // }
  // return retValue;
  // }
  // catch( @SuppressWarnings( "unused" ) IOException | ParseException e ) {
  // return retValue;
  // }
  // }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Чтение метки времени сборки библиотеки из представленного файла манифеста
   *
   * @param aFileName имя файла манифеста
   * @return long метка времени. Long.MIN_VALUE: метка не найдена
   * @throws TsNullArgumentRtException аргумент = null
   */
  @SuppressWarnings( "unused" )
  private static long buildTimeFromManifest( String aFileName ) {
    TsNullArgumentRtException.checkNull( aFileName );
    try {
      ClassLoader classLoader = S5BackendSession.class.getClassLoader();
      Enumeration<URL> resources = classLoader.getResources( aFileName );
      SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
      while( resources.hasMoreElements() ) {
        try( InputStream is = resources.nextElement().openStream() ) {
          Manifest manifest = new Manifest( is );
          String builtDate = manifest.getMainAttributes().getValue( MANIFEST_PARAM_BUILD_DATE );
          if( builtDate == null ) {
            // "Чужой"(возможно даже wildfly) манифест
            continue;
          }
          Date date = format.parse( builtDate );
          return date.getTime();
        }
      }
      return Long.MIN_VALUE;
    }
    catch( IOException | ParseException e ) {
      return Long.MIN_VALUE;
    }
  }

  /**
   * Чтение метки времени сборки библиотеки из представленного файла ресурсов
   *
   * @param aFileName имя файла ресурсов
   * @return long метка времени. Long.MIN_VALUE: метка не найдена
   * @throws TsNullArgumentRtException аргумент = null
   */
  @SuppressWarnings( "unused" )
  private static long buildTimeFromProperties( String aFileName ) {
    TsNullArgumentRtException.checkNull( aFileName );
    try {
      ClassLoader classLoader = S5BackendSession.class.getClassLoader();
      Enumeration<URL> resources = classLoader.getResources( aFileName );
      Properties prop = new Properties();
      try( InputStream inputStream = classLoader.getResourceAsStream( aFileName ) ) {
        if( inputStream == null ) {
          return Long.MIN_VALUE;
        }
        prop.load( inputStream );
        String builtDate = prop.getProperty( MANIFEST_PARAM_BUILD_DATE );
        if( builtDate == null ) {
          // "Чужой"(возможно даже wildfly) манифест
          return Long.MIN_VALUE;
        }
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        Date date = format.parse( builtDate );
        return date.getTime();
      }
    }
    catch( IOException | ParseException e ) {
      return Long.MIN_VALUE;
    }
  }
}
