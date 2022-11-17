package org.toxsoft.uskat.dataquality.lib.impl;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.dataquality.lib.ISkDataQualityTicket;

/**
 * Реализация {@link ISkDataQualityTicket}
 *
 * @author mvk
 */
public final class SkDataQualityTicket
    implements ISkDataQualityTicket, Serializable {

  private static final long serialVersionUID = 157157L;

  private static final String TO_STRING_FORMAT = "%s [%s] builtIn = %b"; //$NON-NLS-1$

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "SkDataQualityTicket"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<ISkDataQualityTicket> KEEPER =
      new AbstractEntityKeeper<>( ISkDataQualityTicket.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkDataQualityTicket aEntity ) {
          aSw.writeAsIs( aEntity.id() );
          aSw.writeChar( CHAR_ITEM_SEPARATOR );
          aSw.writeQuotedString( aEntity.nmName() );
          aSw.writeChar( CHAR_ITEM_SEPARATOR );
          aSw.writeQuotedString( aEntity.description() );
          aSw.writeChar( CHAR_ITEM_SEPARATOR );
          DataType.KEEPER.write( aSw, aEntity.dataType() );
          aSw.writeChar( CHAR_ITEM_SEPARATOR );
          aSw.writeBoolean( aEntity.isBuiltin() );
        }

        @Override
        protected ISkDataQualityTicket doRead( IStrioReader aSr ) {
          String id = aSr.readIdPath();
          aSr.ensureChar( CHAR_ITEM_SEPARATOR );
          String name = aSr.readQuotedString();
          aSr.ensureChar( CHAR_ITEM_SEPARATOR );
          String descr = aSr.readQuotedString();
          aSr.ensureChar( CHAR_ITEM_SEPARATOR );
          IDataType dataType = DataType.KEEPER.read( aSr );
          aSr.ensureChar( CHAR_ITEM_SEPARATOR );
          boolean builtIn = aSr.readBoolean();
          return new SkDataQualityTicket( id, name, descr, dataType, builtIn );
        }
      };

  private final String    id;
  private final String    name;
  private final String    descr;
  private final IDataType type;
  private final boolean   builtIn;

  /**
   * Создает тикет со всеми инвариантами.
   *
   * @param aId String - идентификатор
   * @param aDescription String - описание
   * @param aName String - название
   * @param aDataType {@link IDataType} - тип данных
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aId не ИД-путь
   */
  public SkDataQualityTicket( String aId, String aName, String aDescription, IDataType aDataType ) {
    this( aId, aName, aDescription, aDataType, false );
  }

  /**
   * Создает тикет со всеми инвариантами.
   *
   * @param aId String - идентификатор
   * @param aDescription String - описание
   * @param aName String - название
   * @param aDataType {@link IDataType} - тип данных
   * @param aBuiltIn boolean <b>true</b> встроенный/системный тикет; <b>false</b> пользовательский тикет
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aId не ИД-путь
   * @throws TsIllegalArgumentRtException тикет должен иметь значение по умолчанию
   */
  public SkDataQualityTicket( String aId, String aName, String aDescription, IDataType aDataType, boolean aBuiltIn ) {
    TsNullArgumentRtException.checkNulls( aId, aName, aDescription, aDataType );
    id = StridUtils.checkValidIdPath( aId );
    name = aName;
    descr = aDescription;
    type = aDataType;
    builtIn = aBuiltIn;
  }

  /**
   * Создает тикет со всеми инвариантами.
   *
   * @param aId String - идентификатор
   * @param aDataType {@link IDataType} - тип данных
   * @param aBuiltIn boolean <b>true</b> встроенный/системный тикет; <b>false</b> пользовательский тикет
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aId не ИД-путь
   * @throws TsIllegalArgumentRtException тикет должен иметь значение по умолчанию
   */
  public SkDataQualityTicket( String aId, IDataType aDataType, boolean aBuiltIn ) {
    this( aId, TsLibUtils.EMPTY_STRING, TsLibUtils.EMPTY_STRING, aDataType, aBuiltIn );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkDataQualityTicket
  //
  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return name;
  }

  @Override
  public String description() {
    return descr;
  }

  @Override
  public IDataType dataType() {
    return type;
  }

  @Override
  public boolean isBuiltin() {
    return builtIn;
  }

  @Override
  public IAtomicValue getMarkValue( IOptionSet aMarks ) {
    TsNullArgumentRtException.checkNull( aMarks );
    IAtomicValue value = aMarks.findValue( id() );
    return (value != null ? value : defaultValue());
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return String.format( TO_STRING_FORMAT, id(), dataType(), Boolean.valueOf( builtIn ) );
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = TsLibUtils.PRIME * result + (builtIn ? 1 : 0);
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( !(aObject instanceof ISkDataQualityTicket other) ) {
      return false;
    }
    if( !super.equals( aObject ) ) {
      return false;
    }
    if( builtIn != other.isBuiltin() ) {
      return false;
    }
    return true;
  }

}
