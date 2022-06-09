package org.toxsoft.uskat.skadmin.cli.parsers;

import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Реализация лексемы лексического или синтаксического анализа командной строки
 *
 * @author mvk
 */
public class AdminCmdToken
    implements IAdminCmdToken {

  private ETokenType    type;
  private final int     startIndex;
  private final int     finishIndex;
  private int           listIndex;
  private final String  data;
  private final boolean quoted;

  /**
   * Конструктор
   *
   * @param aType {@link ETokenType} - тип токена
   * @param aStartIndex int - начальный индекс токена в строке
   * @param aFinishIndex int - конечный(включительно) индекс токена в строке
   * @param aData String - данные токена
   * @param aQuoted boolean <b>true</b> данные лексемы находятся в кавычках; <b>false</b> данные находятся вне кавычек.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public AdminCmdToken( ETokenType aType, int aStartIndex, int aFinishIndex, String aData, boolean aQuoted ) {
    TsNullArgumentRtException.checkNull( aType, aData );
    type = aType;
    startIndex = aStartIndex;
    finishIndex = aFinishIndex;
    data = aData;
    quoted = aQuoted;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Установить тип лексемы
   *
   * @param aType {@link ETokenType} - тип лексемы
   */
  public void setType( ETokenType aType ) {
    TsNullArgumentRtException.checkNull( aType );
    type = aType;
  }

  /**
   * Установить индекс лексемы в списке лексем типа {@link ETokenType#LIST_VALUE}
   *
   * @param aIndex int индекс лексемы
   * @throws TsIllegalStateRtException тип лексемы отличается от {@link ETokenType#LIST_VALUE}
   */
  public void setListIndex( int aIndex ) {
    TsIllegalStateRtException
        .checkTrue( !type.equals( ETokenType.LIST_CONTEXT ) && !type.equals( ETokenType.LIST_VALUE ) );
    listIndex = aIndex;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCmdToken
  //
  @Override
  public ETokenType type() {
    return type;
  }

  @Override
  public int startIndex() {
    return startIndex;
  }

  @Override
  public int finishIndex() {
    return finishIndex;
  }

  @Override
  public int listIndex() {
    TsIllegalStateRtException
        .checkTrue( !type.equals( ETokenType.LIST_CONTEXT ) && !type.equals( ETokenType.LIST_VALUE ) );
    return listIndex;
  }

  @Override
  public String data() {
    return data;
  }

  @Override
  public boolean quoted() {
    return quoted;
  }
}
