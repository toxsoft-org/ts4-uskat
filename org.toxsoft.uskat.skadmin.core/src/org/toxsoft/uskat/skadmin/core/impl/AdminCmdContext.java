package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.uskat.skadmin.core.impl.IAdminResources.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Реализация контекста команды по умолчанию
 *
 * @author mvk
 */
public class AdminCmdContext
    implements IAdminCmdContext {

  /**
   * Карта параметров контекста. Ключ: имя параметра. Значение: значение параметра
   */
  private final IStringMapEdit<IPlexyValue> params = new StringMap<>();

  /**
   * Список параметров значения которых доступны только для чтения
   */
  private final IStringListEdit readOnlyParams = new StringArrayList();

  /**
   * Слушатели изменений контекста
   */
  private final IListEdit<IAdminCmdContextListener> listeners = new ElemArrayList<>();

  /**
   * Конструктор по умолчанию
   */
  public AdminCmdContext() {
    // nop
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link IAdminCmdContext} исходный контекст
   * @throws TsNullArgumentRtException аргумент = null
   */
  public AdminCmdContext( IAdminCmdContext aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    for( String paramName : aSource.paramNames() ) {
      params.put( paramName, aSource.paramValue( paramName ) );
      if( aSource.readOnlyParam( paramName ) ) {
        // Признак только для чтения
        readOnlyParams.add( paramName );
      }
    }
    listeners.addAll( aSource.listeners() );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Обновляет значения параметров целевого контекста значениями тех же параметров из исходного.
   * <p>
   * Обновлению подлежат только параметры с именами ИД-путь. Другие параметры игнорируются.
   * <p>
   * Если в исходном контексте нет какого-либо параметра, то значение этого параметра не обновляется.
   *
   * @param aSource {@link AdminCmdContext} исходный контекст
   * @param aNotify boolean признак необходимости проводить оповещение об изменении параметров контекста
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void update( IAdminCmdContext aSource, boolean aNotify ) {
    TsNullArgumentRtException.checkNull( aSource );
    for( String paramName : aSource.paramNames() ) {
      if( !StridUtils.isValidIdPath( paramName ) ) {
        // Имя параметра не ИД-путь (возможно аргумент сценария)
        continue;
      }
      if( !hasParam( paramName ) ) {
        // В целевом контексте нет данного параметра
        continue;
      }
      // Обновление значения параметра
      if( readOnlyParam( paramName ) ) {
        // Параметр только для чтения. Не может быть изменен
        continue;
      }
      params.put( paramName, aSource.paramValue( paramName ) );
      if( aNotify ) {
        // Формирование события: изменено значение параметра
        fireSetParamValueEvent( paramName );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCmdContext
  //
  @Override
  public IStringList paramNames() {
    return params.keys();
  }

  @Override
  public boolean hasParam( String aParamName ) {
    return params.hasKey( aParamName );
  }

  @Override
  public boolean hasParam( IStridable aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    return params.hasKey( aParamId.id() );
  }

  @Override
  public boolean readOnlyParam( String aParamName ) {
    if( !hasParam( aParamName ) ) {
      // Параметр не найден
      throw new TsIllegalArgumentRtException( ERR_CONTEXT_NOT_FOUND, aParamName );
    }
    return readOnlyParams.hasElem( aParamName );
  }

  @Override
  public boolean readOnlyParam( IStridable aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    return readOnlyParam( aParamId.id() );
  }

  @Override
  public IPlexyValue paramValue( String aParamName ) {
    TsNullArgumentRtException.checkNull( aParamName );
    IPlexyValue value = params.findByKey( aParamName );
    if( value == null ) {
      throw new TsIllegalArgumentRtException( ERR_CONTEXT_NOT_FOUND, aParamName );
    }
    return value;
  }

  @Override
  public IPlexyValue paramValueOrNull( String aParamName ) {
    TsNullArgumentRtException.checkNull( aParamName );
    if( !params.hasKey( aParamName ) ) {
      return null;
    }
    return paramValue( aParamName );
  }

  @Override
  public IPlexyValue paramValueOrNull( IStridable aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    if( !params.hasKey( aParamId.id() ) ) {
      return null;
    }
    return paramValue( aParamId.id() );
  }

  @Override
  public IPlexyValue paramValue( IStridable aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    return paramValue( aParamId.id() );
  }

  @Override
  public IList<IPlexyValue> getContextParams( IStringList aParamNames ) {
    TsNullArgumentRtException.checkNulls( aParamNames );
    IListEdit<IPlexyValue> retValue = new ElemLinkedBundleList<>();
    for( String paramName : aParamNames ) {
      retValue.add( paramValue( paramName ) );
    }
    return retValue;
  }

  @Override
  public IList<IPlexyValue> getContextParams( IStridablesList<?> aParamIds ) {
    TsNullArgumentRtException.checkNulls( aParamIds );
    IListEdit<IPlexyValue> retValue = new ElemLinkedBundleList<>();
    for( IStridable paramId : aParamIds ) {
      retValue.add( paramValue( paramId ) );
    }
    return retValue;
  }

  @Override
  public IList<IPlexyValue> getContextParamByType( IPlexyType aParamType ) {
    TsNullArgumentRtException.checkNull( aParamType );
    EPlexyKind kind = aParamType.kind();
    Class<?> refClass = (kind.isReference() ? aParamType.refClass() : null);
    IListEdit<IPlexyValue> retValue = new ElemLinkedList<>();
    for( IPlexyValue paramValue : params.values() ) {
      IPlexyType valueType = paramValue.type();
      switch( kind ) {
        case SINGLE_VALUE:
        case VALUE_LIST:
        case OPSET:
          if( !aParamType.equals( valueType ) ) {
            // Тип значения не совместим с указанным типом
            continue;
          }
          break;
        case SINGLE_REF:
        case REF_LIST:
          if( kind != paramValue.type().kind() || //
              refClass == null || !refClass.isAssignableFrom( valueType.refClass() ) ) {
            // Тип значения не совместим с указанным типом
            continue;
          }
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
      if( !retValue.hasElem( paramValue ) ) {
        // Значения нет во возвращаемом результате. Добавляем
        retValue.add( paramValue );
      }
    }
    return retValue;
  }

  @Override
  public IList<Object> getContextReferences( Class<?> aRefClass ) {
    TsNullArgumentRtException.checkNull( aRefClass );
    IListEdit<Object> retValue = new ElemLinkedList<>();
    for( IPlexyValue paramValue : params.values() ) {
      IPlexyType valueType = paramValue.type();
      switch( valueType.kind() ) {
        case SINGLE_VALUE:
        case VALUE_LIST:
        case OPSET:
          continue;
        case SINGLE_REF:
          if( aRefClass.isAssignableFrom( valueType.refClass() ) ) {
            // Тип значения не совместим с указанным типом
            if( !retValue.hasElem( paramValue.singleRef() ) ) {
              // Значения нет во возвращаемом результате. Добавляем
              retValue.add( paramValue.singleRef() );
            }
          }
          break;
        case REF_LIST:
          if( aRefClass.isAssignableFrom( valueType.refClass() ) ) {
            IList<Object> refList = paramValue.refList();
            for( Object ref : refList ) {
              // Тип значения не совместим с указанным типом
              if( !retValue.hasElem( ref ) ) {
                // Значения нет во возвращаемом результате. Добавляем
                retValue.add( ref );
              }
            }
          }
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    return retValue;
  }

  @Override
  public void checkWritable( IStringList aContextNames, boolean aCheckExist ) {
    for( String paramName : aContextNames ) {
      if( !hasParam( paramName ) ) {
        // Параметр не существует
        TsIllegalArgumentRtException.checkTrue( aCheckExist, ERR_CONTEXT_NOT_FOUND, paramName );
        continue;
      }
      if( readOnlyParam( paramName ) ) {
        // Параметр с признаком только чтение
        throw new TsIllegalArgumentRtException( ERR_CONTEXT_READONLY, paramName );
      }
    }
  }

  @Override
  public void checkWritable( IStridablesList<?> aContextNames, boolean aCheckExist ) {
    for( IStridable paramId : aContextNames ) {
      String paramName = paramId.id();
      if( !hasParam( paramName ) ) {
        // Параметр не существует
        TsIllegalArgumentRtException.checkTrue( aCheckExist, ERR_CONTEXT_NOT_FOUND, paramName );
        continue;
      }
      if( readOnlyParam( paramName ) ) {
        // Параметр с признаком только чтение
        throw new TsIllegalArgumentRtException( ERR_CONTEXT_READONLY, paramName );
      }
    }
  }

  @Override
  public int referenceCount( IAdminCmdContextParam aParam ) {
    TsNullArgumentRtException.checkNull( aParam );
    return referenceCount( aParam.id() );
  }

  @Override
  public int referenceCount( String aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    Object value = paramValueOrNull( aParamId );
    if( value == null ) {
      return 0;
    }
    int referenceCount = 0;
    for( String paramName : paramNames() ) {
      Object nextValue = paramValue( paramName );
      if( nextValue == value ) {
        referenceCount++;
      }
    }
    return referenceCount;
  }

  @Override
  public void setParamValue( String aParamName, IPlexyValue aParamValue ) {
    setParamValue( aParamName, aParamValue, false );
  }

  @Override
  public void setParamValue( String aParamName, IPlexyValue aParamValue, boolean aReadOnly ) {
    TsNullArgumentRtException.checkNulls( aParamName, aParamValue );
    boolean existParam = hasParam( aParamName );
    if( existParam && readOnlyParam( aParamName ) ) {
      // Значение параметра доступно только для чтения
      throw new TsIllegalArgumentRtException( ERR_CONTEXT_READONLY, aParamName );
    }
    params.put( aParamName, aParamValue );
    if( aReadOnly ) {
      // Значение параметра только для чтения
      readOnlyParams.add( aParamName );
    }
    if( !existParam ) {
      // Формирование события: добавлен новый параметр
      fireAddEvent( aParamName );
    }
    // Формирование события: изменено значение параметра
    fireSetParamValueEvent( aParamName );
  }

  @Override
  public void setParamValue( IStridable aParamId, IPlexyValue aParamValue ) {
    TsNullArgumentRtException.checkNull( aParamId );
    setParamValue( aParamId.id(), aParamValue );
  }

  @Override
  public void setParamValue( IStridable aParamId, IPlexyValue aParamValue, boolean aReadOnly ) {
    TsNullArgumentRtException.checkNull( aParamId );
    setParamValue( aParamId.id(), aParamValue, aReadOnly );
  }

  @Override
  public void removeParam( String aParamName ) {
    if( params.hasKey( aParamName ) ) {
      // Формирование события: удаление параметра из контекста
      fireRemovingEvent( aParamName );
      params.removeByKey( aParamName );
      readOnlyParams.remove( aParamName );
      // Формирование события: удален параметр из контекста
      fireRemovedEvent( aParamName );
    }
  }

  @Override
  public void removeParam( IStridable aParamId ) {
    TsNullArgumentRtException.checkNull( aParamId );
    removeParam( aParamId.id() );
  }

  @Override
  public void addContextListener( IAdminCmdContextListener aListener ) {
    if( !listeners.hasElem( aListener ) ) {
      listeners.add( aListener );
    }
  }

  @Override
  public void removeContextListener( IAdminCmdContextListener aListener ) {
    listeners.remove( aListener );
  }

  @Override
  public IList<IAdminCmdContextListener> listeners() {
    return listeners;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Формирование события: добавлен параметр в контекст
   *
   * @param aParamName String имя параметра
   */
  private void fireAddEvent( String aParamName ) {
    for( IAdminCmdContextListener listener : listeners ) {
      listener.onAddParam( this, aParamName );
    }
  }

  /**
   * Формирование события: удаление параметра из контекста
   *
   * @param aParamName String имя параметра
   */
  private void fireRemovingEvent( String aParamName ) {
    for( IAdminCmdContextListener listener : listeners ) {
      listener.onRemovingParam( this, aParamName );
    }
  }

  /**
   * Формирование события: удален параметра из контекста
   *
   * @param aParamName String имя параметра
   */
  private void fireRemovedEvent( String aParamName ) {
    for( IAdminCmdContextListener listener : listeners ) {
      listener.onRemovedParam( this, aParamName );
    }
  }

  /**
   * Формирование события: установлено значение параметра в контексте
   *
   * @param aParamName String имя параметра
   */
  private void fireSetParamValueEvent( String aParamName ) {
    for( IAdminCmdContextListener listener : listeners ) {
      listener.onSetParamValue( this, aParamName );
    }
  }
}
