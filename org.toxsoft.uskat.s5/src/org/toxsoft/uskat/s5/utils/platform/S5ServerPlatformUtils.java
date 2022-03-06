package org.toxsoft.uskat.s5.utils.platform;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.utils.platform.IS5Resources.*;

import java.lang.management.ManagementFactory;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Вспомогательные методы работы с информацией о платформе на которой работает сервер с возможностью ограниченного
 * управления
 * <p>
 * Библиотека методов предназначена только для использования на сервере!
 * <p>
 * Источник: http://middlewaremagic.com/jboss/?p=324
 *
 * @author mvk
 */
public class S5ServerPlatformUtils {

  /**
   * Время (мсек) в течении которого параметр loadAverage не обновляется
   */
  private static final long LOAD_AVERAGE_TIMEOUT = 1000;

  /**
   * Соединение с менеджементом системы
   */
  private static MBeanServerConnection managmentConnection;

  /**
   * managment бин доступа к информации о операционной системе
   */
  private static ObjectName osMXBean;

  /**
   * managment бин доступа к информации о распределении памяти
   */
  private static ObjectName memMXBean;

  /**
   * Текущая информация о системе. null: неопределено
   */
  private static S5PlatformInfo lastInfo = null;

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает информацию о системе
   *
   * @return {@link S5PlatformInfo} информация о системе
   */
  @SuppressWarnings( "nls" )
  public static S5PlatformInfo getPlatformInfo() {
    if( lastInfo == null || System.currentTimeMillis() - lastInfo.timestamp() > LOAD_AVERAGE_TIMEOUT ) {
      MBeanServerConnection connection = connection();
      try {
        if( osMXBean == null ) {
          osMXBean = new ObjectName( "java.lang:type=OperatingSystem" );
        }
        if( memMXBean == null ) {
          memMXBean = new ObjectName( "java.lang:type=Memory" );
        }
        // @formatter:off
        AttributeList osAttrs = connection.getAttributes( osMXBean, new String[] { "SystemLoadAverage", "FreePhysicalMemorySize"} );
        double loadAverage = ((Double)((Attribute)osAttrs.get( 0 )).getValue()). doubleValue();
        long freePhysicalMemory = ((Long)((Attribute)osAttrs.get( 1 )).getValue()).longValue();
        AttributeList memAttrs = connection.getAttributes( memMXBean, new String[] { "HeapMemoryUsage", "NonHeapMemoryUsage"} );
        CompositeDataSupport heapMem = (CompositeDataSupport)((Attribute)memAttrs.get( 0 )).getValue();
        CompositeDataSupport nonHeapMem = (CompositeDataSupport)((Attribute)memAttrs.get( 1 )).getValue();
        long heapMax = ((Long)heapMem.get( "max" )).longValue();
        long heapUsed = ((Long)heapMem.get( "used" )).longValue();
        long nonHeapMax = ((Long)nonHeapMem.get( "max" )).longValue();
        long nonHeapUsed = ((Long)nonHeapMem.get( "used" )).longValue();
        // @formatter:on
        lastInfo = new S5PlatformInfo( loadAverage, freePhysicalMemory, heapMax, heapUsed, nonHeapMax, nonHeapUsed );
      }
      catch( Throwable e ) {
        throw new TsInternalErrorRtException( e, MSG_ERR_GET_INFO_UNEXPECTED, cause( e ) );
      }
    }
    return lastInfo;
  }

  /**
   * Возвращает текущее значение loadAverage
   *
   * @return double текущее значение. < 0: параметр неопределяется в системе (windows)
   */
  public static double loadAverage() {
    return getPlatformInfo().loadAverage();
  }

  /**
   * Выполнение сборки мусора через бины управления системой
   */
  @SuppressWarnings( "nls" )
  public static void runGarbageCollection() {
    try {
      ObjectName memoryMXBean = new ObjectName( "java.lang:type=Memory" );
      connection().invoke( memoryMXBean, "gc", null, null );
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, MSG_ERR_DO_GC_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Возвращает строковое представление информации об операционной системе
   *
   * @return String строковое представление информации
   */
  @SuppressWarnings( "nls" )
  public static String printOperatingSystemDetails() {
    try {
      StringBuilder sb = new StringBuilder();
      MBeanServerConnection connection = connection();
      ObjectName operatingSystemMXBean = new ObjectName( "java.lang:type=OperatingSystem" );
      Object systemLoadAverage = connection.getAttribute( operatingSystemMXBean, "SystemLoadAverage" );

      Long freePhysicalMemory = (Long)connection.getAttribute( operatingSystemMXBean, "FreePhysicalMemorySize" );
      Long processCpuTime = (Long)connection.getAttribute( operatingSystemMXBean, "ProcessCpuTime" );
      Long committedVirtualMemorySize =
          (Long)connection.getAttribute( operatingSystemMXBean, "CommittedVirtualMemorySize" );
      Long freeSwapSpaceSize = (Long)connection.getAttribute( operatingSystemMXBean, "FreeSwapSpaceSize" );
      Long totalPhysicalMemorySize = (Long)connection.getAttribute( operatingSystemMXBean, "TotalPhysicalMemorySize" );
      Long totalSwapSpaceSize = (Long)connection.getAttribute( operatingSystemMXBean, "TotalSwapSpaceSize" );

      int mb = 1024 * 1024;
      // @formatter:off
      sb.append( format( "load average               : %2.2f\n", systemLoadAverage ) );
      sb.append( format( "freePhysicalMemory         :  %d MB\n", Long.valueOf( freePhysicalMemory.longValue() / mb ) ) );
      sb.append( format( "processCpuTime             :  %d\n", processCpuTime ) );
      sb.append( format( "committedVirtualMemorySize :  %d -MB\n", Long.valueOf( committedVirtualMemorySize.longValue() / mb )  ) );
      sb.append( format( "freeSwapSpaceSize          :  %d -MB\n", Long.valueOf( freeSwapSpaceSize.longValue() / mb )  ) );
      sb.append( format( "totalPhysicalMemorySize    :  %d -MB\n", Long.valueOf( totalPhysicalMemorySize.longValue() / mb )  ) );
      sb.append( format( "totalSwapSpaceSize         :  %d -MB\n", Long.valueOf( totalSwapSpaceSize.longValue() / mb )  ) );
      // @formatter:on
      return sb.toString();
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, MSG_ERR_GET_INFO_UNEXPECTED, cause( e ) );
    }
  }

  /**
   * Возвращает строковое представление информации об использовании heap памяти
   *
   * @return String строковое представление информации
   */
  @SuppressWarnings( "nls" )
  public static String printHeapMemoryUsage() {
    return printMemoryUsage( "HeapMemoryUsage" );
  }

  /**
   * Возвращает строковое представление информации об использовании non-heap памяти
   *
   * @return String строковое представление информации
   */
  @SuppressWarnings( "nls" )
  public static String printNonHeapMemoryUsage() {
    return printMemoryUsage( "NonHeapMemoryUsage" );
  }

  /**
   * Возвращает значение атрибута конфигурации сервера
   *
   * @param aPath String путь к атрибуту
   * @param aId String идентификатор атрибу
   * @return Object значение атрибута
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException ошибка чтения атрибута
   */
  public static Object readAttribute( String aPath, String aId ) {
    TsNullArgumentRtException.checkNulls( aPath, aId );
    try {
      // ObjectName objectName = new ObjectName( aPath );// $NON-NLS-1$
      // MBeanInfo info = connection().getMBeanInfo( objectName );
      // for( MBeanAttributeInfo attrInfo : info.getAttributes() ) {
      // try {
      // System.out.println( attrInfo.getName() + " = ... " );
      // System.out.println( "..." + connection().getAttribute( objectName, attrInfo.getName() ) );
      // }
      // catch( Throwable e ) {
      // System.err.println( e.getLocalizedMessage() );
      // }
      // }
      return connection().getAttribute( new ObjectName( aPath ), aId );
    }
    catch( Throwable e ) {
      // Ошибка чтения значения атрибута
      throw new TsIllegalArgumentRtException( MSG_READ_ATTRIBUTE_VALUE, aPath, aId, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает соединение с менджементом системы
   *
   * @return {@link MBeanServerConnection} соединение
   */
  private static MBeanServerConnection connection() {
    if( managmentConnection == null ) {
      managmentConnection = ManagementFactory.getPlatformMBeanServer();
    }
    return managmentConnection;
  }

  /**
   * Возвращает строковое представление информации об использовании памяти
   *
   * @param aMemoryMxBeenName String имя бина управления запрашиваемого типа информации
   * @return String строковое представление информации
   * @throws TsNullArgumentRtException аргумент = null
   */
  @SuppressWarnings( "nls" )
  private static String printMemoryUsage( String aMemoryMxBeenName ) {
    TsNullArgumentRtException.checkNull( aMemoryMxBeenName );
    try {
      StringBuilder sb = new StringBuilder();
      ObjectName memoryMXBean = new ObjectName( "java.lang:type=Memory" );
      CompositeDataSupport dataSenders =
          (CompositeDataSupport)connection().getAttribute( memoryMXBean, aMemoryMxBeenName );
      if( dataSenders == null ) {
        sb.append( "error: dataSenders = null\n" );
      }
      if( dataSenders != null ) {
        Long commited = (Long)dataSenders.get( "committed" );
        Long init = (Long)dataSenders.get( "init" );
        Long max = (Long)dataSenders.get( "max" );
        Long used = (Long)dataSenders.get( "used" );
        Long percentage = Long.valueOf( ((used.longValue() * 100) / max.longValue()) );
        int mb = 1024 * 1024;
        sb.append( format( "nnt commited :  %d MB\n", Long.valueOf( commited.longValue() / mb ) ) );
        sb.append( format( "t init       :  %d MB\n", Long.valueOf( init.longValue() / mb ) ) );
        sb.append( format( "t max        :  %d MB\n", Long.valueOf( max.longValue() / mb ) ) );
        sb.append( format( "t used       :  %d MB\n", Long.valueOf( used.longValue() / mb ) ) );
        sb.append( format( "t percentage :  %d\n", percentage ) );
      }
      return sb.toString();
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, MSG_ERR_GET_INFO_UNEXPECTED, cause( e ) );
    }
  }
}
