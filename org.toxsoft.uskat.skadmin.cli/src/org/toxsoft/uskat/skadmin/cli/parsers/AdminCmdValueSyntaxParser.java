package org.toxsoft.uskat.skadmin.cli.parsers;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.AdminCmdParserUtils.*;
import static org.toxsoft.uskat.skadmin.cli.parsers.IAdminResources.*;

import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContext;

/**
 * Синтаксический анализатор значений команд
 *
 * @author mvk
 */
public class AdminCmdValueSyntaxParser
    implements IAdminCmdSyntaxParser {

  /**
   * Лексический анализатор
   */
  private final IAdminCmdSyntaxParser lexicalParser = new AdminCmdLexicalParser();

  /**
   * Контекст выполняемой команды
   */
  private final IAdminCmdContext context;

  /**
   * Тип читаемого значения
   */
  private final IPlexyType type;

  /**
   * Имя читаемого значения
   */
  private final String name;

  /**
   * Список допускаемых значений
   */
  IList<IPlexyValue> possibleValues;

  /**
   * Карта сформированных значений во время последнего анализа. Ключ: имя значения
   */
  private IStringMapEdit<IPlexyValue> values = new StringMap<>();

  /**
   * Список ошибок последнего анализа
   */
  private IListEdit<ValidationResult> errors = new ElemLinkedList<>();

  /**
   * Конструктор
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команды
   * @param aType {@link IPlexyType} - тип читаемого значения
   * @param aName String - имя читаемого значения
   * @param aPossibleValues {@link IList} &lt;{@link IPlexyValue}&gt; - список возможных значений. Пустой список без
   *          ограничений
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public AdminCmdValueSyntaxParser( IAdminCmdContext aContext, IPlexyType aType, String aName,
      IList<IPlexyValue> aPossibleValues ) {
    TsNullArgumentRtException.checkNulls( aContext, aType, aName, aPossibleValues );
    context = aContext;
    type = aType;
    name = aName;
    possibleValues = new ElemArrayList<>( aPossibleValues );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает признак того, значение прочитано
   *
   * @return boolean <b>true</b> значение прочитано без ошибок; <b>false</b> во время чтения произошли ошибки
   */
  public boolean hasValue() {
    return values.hasKey( name );
  }

  /**
   * Возвращает значение команды полученное на последнем анализе
   *
   * @return {@link IPlexyValue} - значение
   */
  public IPlexyValue getValue() {
    return values.getByKey( name );
  }

  /**
   * Возвращает список ошибок последнего анализа
   *
   * @return {@link IList}&lt;{@link ValidationResult}&gt; - список ошибок последнего анализа
   */
  public IList<ValidationResult> getErrors() {
    return errors;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCmdSyntaxParser
  //
  @Override
  public IList<IAdminCmdToken> parse( String aLine ) {
    TsNullArgumentRtException.checkNull( aLine );
    // Очистка результатов предыдущего анализа
    values.clear();
    errors.clear();
    // Лексический анализ строки значений аргументов
    IList<IAdminCmdToken> tokens = lexicalParser.parse( aLine );
    if( tokens.size() == 0 ) {
      // Пустая строка
      return tokens;
    }
    // 0: анализ с первой лексемы
    int lastTokenIndex = argValueParse( context, tokens, 0, type, possibleValues, name, values, errors );
    // Все оставшиеся лексемы после чтения значения нарушают формат
    for( int index = lastTokenIndex + 1, n = tokens.size(); index < n; index++ ) {
      // Преобразования к AdminCmdToken допустимы: AdminCmdLexicalParser агрегирован в класс
      AdminCmdToken token = (AdminCmdToken)tokens.get( index );
      token.setType( ETokenType.UNDEF );
      errors.add( error( ERR_MSG_VALUE_UNEXPECTED, Integer.valueOf( token.startIndex() ), token.data() ) );
    }
    return tokens;
  }
}
