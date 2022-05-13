package org.toxsoft.uskat.skadmin.core.impl;

import static org.toxsoft.uskat.skadmin.core.impl.IAdminResources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringLinkedBundleList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.legacy.plexy.impl.PlexyValueKeeper;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContext;

/**
 * Хранитель параметров контекста {@link IPlexyValue}.
 * <p>
 * Обратите внимание, что невозможно соханить значение вида {@link EPlexyKind#isReference()}, если объекты класса
 * {@link IPlexyType#refClass()} не являются сериализуемым (не реализует {@link Serializable}).
 *
 * @author mvk
 */
public class AdminCmdContextKeeper
    extends AbstractEntityKeeper<IAdminCmdContext> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static AdminCmdContextKeeper KEEPER = new AdminCmdContextKeeper();

  private AdminCmdContextKeeper() {
    super( IAdminCmdContext.class, EEncloseMode.ENCLOSES_BASE_CLASS, new AdminCmdContext() );
  }

  @Override
  protected void doWrite( IStrioWriter aSw, IAdminCmdContext aEntity ) {
    // Поток записи объектов
    IStringList paramNames = aEntity.paramNames();
    // Определяем карту сохраняемых параметров
    IStringListEdit writingParams = new StringLinkedBundleList();
    for( String paramName : paramNames ) {
      IPlexyValue value = aEntity.paramValue( paramName );
      IPlexyType type = value.type();
      if( type.kind().isReference() && !Serializable.class.isAssignableFrom( type.refClass() ) ) {
        // Тип ссылки не является наследником интерфейса сериализации, и значение(я) не могут быть сохранены
        continue;
      }
      writingParams.add( paramName );
    }
    // Запись количества сохраняемых параметров
    aSw.writeInt( writingParams.size() );
    aSw.writeSeparatorChar();
    aSw.writeEol();
    // Сохранение параметров
    for( int index = 0, count = writingParams.size(); index < count; index++ ) {
      String paramName = writingParams.get( index );
      IPlexyValue paramValue = aEntity.paramValue( paramName );
      // Сохранение имени
      aSw.writeAsIs( paramName );
      aSw.writeChar( '=' );
      // Сохранение значения
      PlexyValueKeeper.KEEPER.write( aSw, paramValue );
      if( index + 1 < count ) {
        aSw.writeSeparatorChar();
      }
      aSw.writeEol();
    }
  }

  @Override
  protected IAdminCmdContext doRead( IStrioReader aSr ) {
    AdminCmdContext context = new AdminCmdContext();
    // Чтение количества читаемых параметров
    int count = aSr.readInt();
    aSr.ensureSeparatorChar();
    for( int index = 0; index < count; index++ ) {
      // Чтение имени параметра
      String paramName = null;
      try {
        paramName = aSr.readIdPath();
        aSr.ensureChar( '=' );
        // Чтение значения
        IPlexyValue paramValue = PlexyValueKeeper.KEEPER.read( aSr );
        // Добавление параметра в контекст
        context.setParamValue( paramName, paramValue );
        if( index + 1 < count ) {
          aSr.ensureSeparatorChar();
        }
      }
      catch( RuntimeException e ) {
        throw new TsIllegalStateRtException( ERR_READ_CONTEXT, Long.valueOf( index ),
            (paramName != null ? paramName : "???"), //$NON-NLS-1$
            e.getLocalizedMessage() );
      }
    }
    return context;
  }
}
