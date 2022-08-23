package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.uskat.skadmin.core.impl.IAdminResources.*;

import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdResult;

/**
 * Результат выполнения команды администрирования
 *
 * @author mvk
 */
public class AdminCmdResult
    implements IAdminCmdResult {

  /**
   * Пустой результат
   */
  public static final AdminCmdResult EMPTY = new AdminCmdResult( IPlexyType.NONE, true );

  /**
   * Общая ошибка выполнения команды
   */
  public static final AdminCmdResult ERROR = new AdminCmdResult( IPlexyType.NONE, false );

  private IPlexyType                  resultType;
  private IPlexyValue                 result      = IPlexyValue.NULL;
  private boolean                     okFlag      = false;
  private IListEdit<ValidationResult> validations = new ElemLinkedList<>();
  private boolean                     finished    = false;

  /**
   * Конструктор
   *
   * @param aResultType {@link IPlexyType} - тип результата. {@link IPlexyType#NONE} - команда не имеет результата
   * @param aOkFlag <b>true</b> успешное завершение команды; <b>false</b> ошибка заверешения команды.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public AdminCmdResult( IPlexyType aResultType, boolean aOkFlag ) {
    TsNullArgumentRtException.checkNull( aResultType );
    resultType = aResultType;
    okFlag = aOkFlag;
  }

  /**
   * Конструктор копирования.
   *
   * @param aSrc {@link IAdminCmdResult} - исходный объект
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdResult( IAdminCmdResult aSrc ) {
    AdminCmdResult src = (AdminCmdResult)TsNullArgumentRtException.checkNull( aSrc );
    resultType = src.resultType;
    result = src.result;
    okFlag = src.okFlag;
    finished = src.finished;
    validations.setAll( src.validations );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IAdminCmdResult
  //
  @Override
  public IPlexyValue result() {
    TsIllegalStateRtException.checkFalse( isOk(), ERR_NOT_RESULT );
    return result;
  }

  @Override
  public boolean isOk() {
    return okFlag;
  }

  @Override
  public IList<ValidationResult> validations() {
    return validations;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает признак того, что команда сформировала окончательный результат
   *
   * @return boolean - <b>true</b> результат сфомирован; <b>false</b> результат не сформирован или сфомирован частично.
   */
  public boolean hasResult() {
    return finished;
  }

  /**
   * Добавляет результат проверки
   *
   * @param aValidationResult {@link ValidationResult} - результат проверки
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  public void addValidation( ValidationResult aValidationResult ) {
    TsNullArgumentRtException.checkNull( aValidationResult );
    TsIllegalStateRtException.checkTrue( finished, ERR_RESULT_ALREADY_FINISHED );
    validations.add( aValidationResult );
  }

  /**
   * Завершить формирования результата выполнения команды с формированием успешного результата
   *
   * @param aValue {@link IPlexyValue} - результат выполнения. Команда без результата {@link IPlexyValue#NULL}
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   * @throws TsIllegalArgumentRtException тип результата не совместим с типом результата команды
   */
  public void ok( IPlexyValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    TsIllegalStateRtException.checkTrue( finished, ERR_RESULT_ALREADY_FINISHED );
    IPlexyType valueType = aValue.type();
    if( valueType != IPlexyType.NONE && resultType.kind() != valueType.kind() ) {
      throw new TsIllegalArgumentRtException( ERR_RESULT_WRONG_TYPE, resultType.kind(), valueType.kind() );
    }
    if( resultType != IPlexyType.NONE ) {
      switch( resultType.kind() ) {
        case SINGLE_VALUE:
        case VALUE_LIST:
          IDataType resultDt = resultType.dataType();
          IDataType valueDt = valueType.dataType();
          if( !resultDt.equals( valueDt ) ) {
            throw new TsIllegalArgumentRtException( ERR_RESULT_WRONG_TYPE, resultDt, valueDt );
          }
          break;
        case OPSET:
          break;
        case SINGLE_REF:
        case REF_LIST:
          Class<?> resultRefClass = resultType.refClass();
          Class<?> valueRefClass = valueType.refClass();
          if( !resultRefClass.isAssignableFrom( valueRefClass ) ) {
            throw new TsIllegalArgumentRtException( ERR_RESULT_WRONG_TYPE, resultRefClass, valueRefClass );
          }
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    okFlag = true;
    result = aValue;
    finished = true;
  }

  /**
   * Завершить формирования результата выполнения команды с формированием результата ошибки
   *
   * @throws TsIllegalStateRtException результат уже сформирован. Коррекция запрещена
   */
  public void fail() {
    TsIllegalStateRtException.checkTrue( finished, ERR_RESULT_ALREADY_FINISHED );
    okFlag = false;
    result = IPlexyValue.NULL;
    finished = true;
  }
}
