package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.bricks.validator.*;

/**
 * Генератор идентификаторов команд
 *
 * @author mvk
 */
public class S5CommandIdGenerator
    implements IStridGenerator {

  /**
   * Синглетон
   */
  public static final IStridGenerator INSTANCE = new S5CommandIdGenerator();

  /**
   * Генератор
   */
  private final IStridGenerator generator = new SimpleStridGenerator();

  /**
   * Закрытый конструктор
   */
  private S5CommandIdGenerator() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IStridGenerator2
  //
  @Override
  public String nextId() {
    return generator.nextId();
  }

  @Override
  public IOptionSet getInitialState() {
    return generator.getInitialState();
  }

  @Override
  public IOptionSet getState() {
    return generator.getState();
  }

  @Override
  public void setState( IOptionSet aState ) {
    generator.setState( aState );
  }

  @Override
  public ValidationResult validateState( IOptionSet aState ) {
    return generator.validateState( aState );
  }

}
