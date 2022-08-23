package org.toxsoft.uskat.base.gui.km5.models;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.base.gui.km5.models.ISkResources.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Базовый класс моделирования Sk-объектов.
 * <p>
 * Этот класс низкоуровневый - не добавляет никакие поля в модель, не имеет менеджера ЖЦ и т.п. Чаще всего, если нужно
 * уточнить только некторые аспекты M5-моделирования конкретного класса предметной области, следует унаследоваться и
 * уточнить поведение класса {@link KM5GenericM5Model}. Этот класс имеет смысл использовать при создании библотечных
 * моделей, например, для использования со служебными тематическими моделями {@link KM5AbstractContributor}
 * справочников, НСИ и т.п.
 *
 * @author goga
 * @param <T> - конкретный класс моделируемой сущности
 */
public class KM5BasicModel<T extends ISkObject>
    extends M5Model<T>
    implements ISkConnected {

  /**
   * Атрибут {@link ISkObject#classId()} - {@link ISkHardConstants#AID_CLASS_ID}.
   */
  public final KM5AttributeFieldDef<T> SKID = new KM5AttributeFieldDef<>( ISkHardConstants.AID_SKID, DDEF_VALOBJ ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_FDEF_SKID, STR_D_FDEF_SKID );
      setFlags( M5FF_HIDDEN | M5FF_INVARIANT );
    }

  };

  /**
   * Атрибут {@link ISkObject#classId()} - {@link ISkHardConstants#AID_CLASS_ID}.
   */
  public final KM5AttributeFieldDef<T> CLASS_ID = new KM5AttributeFieldDef<>( AID_CLASS_ID, DDEF_IDPATH ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_FDEF_CLASS_ID, STR_D_FDEF_CLASS_ID );
      setFlags( M5FF_HIDDEN | M5FF_INVARIANT );
    }

  };

  /**
   * Атрибут {@link ISkObject#strid()} - {@link ISkHardConstants#AID_STRID}.
   */
  public final KM5AttributeFieldDef<T> STRID = new KM5AttributeFieldDef<>( AID_STRID, DDEF_IDPATH ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_FDEF_STRID, STR_D_FDEF_STRID );
      setFlags( M5FF_INVARIANT );
    }

  };

  /**
   * Атрибут {@link ISkObject#nmName()} - {@link ISkHardConstants#AID_NAME}.
   * <p>
   * Никогда не возвращает неотображаемую пустую строку, если имя пустое, то возвращает идентификатор.
   */
  public final KM5AttributeFieldDef<T> NAME = new KM5AttributeFieldDef<>( AID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_FDEF_NAME, STR_D_FDEF_NAME );
      setFlags( M5FF_COLUMN );
    }

    @Override
    protected String doGetFieldValueName( T aEntity ) {
      String s = super.doGetFieldValueName( aEntity );
      if( !s.isBlank() ) {
        return s;
      }
      return aEntity.id();
    }

  };

  /**
   * Атрибут {@link ISkObject#description()} - {@link ISkHardConstants#AID_DESCRIPTION}.
   */
  public final KM5AttributeFieldDef<T> DESCRIPTION = new KM5AttributeFieldDef<>( AID_DESCRIPTION, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_FDEF_DESCRIPTION, STR_D_FDEF_DESCRIPTION );
      setFlags( M5FF_DETAIL );
    }

  };

  private final ISkConnection conn;

  /**
   * Конструктор.
   *
   * @param aId String - идентификатор модели (чаще всего, идентификатор {@link ISkObject#classId()}
   * @param aModelledClass {@link Class} - класс (тип) моделированной S5-сущности
   * @param aConn {@link ISkConnection} - соединение с сервером, используемое моделью
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   */
  public KM5BasicModel( String aId, Class<T> aModelledClass, ISkConnection aConn ) {
    super( aId, aModelledClass );
    conn = TsNullArgumentRtException.checkNull( aConn );
    setNameAndDescription( STR_N_KM5M_OBJECT, STR_D_KM5M_OBJECT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkStdContextReferences
  //

  /**
   * Возвращает используемое соединение с сервером.
   *
   * @return {@link ISkConnection} - соединение с сервером, не бывает <code>null</code>
   */
  @Override
  public ISkConnection skConn() {
    return conn;
  }

}
