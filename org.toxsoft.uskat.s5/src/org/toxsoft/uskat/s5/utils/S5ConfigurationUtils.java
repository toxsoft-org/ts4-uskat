package org.toxsoft.uskat.s5.utils;

import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;

import java.util.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Вспомогательные методы для работы с параметрами конфигурации
 *
 * @author mvk
 */
public class S5ConfigurationUtils {

  /**
   * Чтение значений параметров конфигурации подсистемы из системного окружения {@link System#getProperties()}.
   *
   * @param aConfigurationPath String ИД-путь параметров конфигурации
   * @return {@link IOptionSet} считанные параметры конфигурации
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException один из путей не является ИД-путем
   */
  public static IOptionSet readSystemConfiguraion( String aConfigurationPath ) {
    return readSystemConfiguraion( new StringArrayList( aConfigurationPath ) );
  }

  /**
   * Чтение значений параметров конфигурации подсистемы из системного окружения {@link System#getProperties()}.
   *
   * @param aConfigurationPaths {@link IStringList} список ИД-путей конфигурации с которых должны начинаться
   *          идентификаторы параметров
   * @return {@link IOptionSet} считанные параметры конфигурации
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException один из путей не является ИД-путем
   */
  public static IOptionSet readSystemConfiguraion( IStringList aConfigurationPaths ) {
    TsNullArgumentRtException.checkNull( aConfigurationPaths );
    // Параметры окружения
    Properties systemProperties = System.getProperties();
    // Подготовка набора параметров доступных только для чтения
    IOptionSetEdit roEdit = new OptionSet();
    for( Object key : systemProperties.keySet() ) {
      if( !(key instanceof String propId) ) {
        continue;
      }
      if( !isValidIdPath( propId ) ) {
        continue;
      }
      for( String pathId : aConfigurationPaths ) {
        checkValidIdPath( pathId );
        if( startsWithIdPath( propId, pathId ) ) {
          String value = systemProperties.getProperty( propId );
          if( value != null ) {
            StrioReader sr = new StrioReader( new CharInputStreamString( value, 0 ) );
            roEdit.put( propId, AtomicValueReaderUtils.readAtomicValueOrAsString( sr ) );
          }
        }
      }
    }
    return roEdit;
  }

}
