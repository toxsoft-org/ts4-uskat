package org.toxsoft.uskat.base.gui.km5;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link IKM5Support} implementation.
 *
 * @author hazard157
 */
class KM5Support
    implements IKM5Support, ISkConnected {

  private final ISkConnectionListener skConnectionListener = ( aSource, aOldState ) -> {
    switch( aSource.state() ) {
      case ACTIVE:
        if( aOldState == ESkConnState.CLOSED ) {
          initializeDomain();
        }
        break;
      case CLOSED:
        clearDomain();
        break;
      case INACTIVE:
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  };

  private final ISkSysdescrListener classServiceListener =
      ( core, op, cid ) -> updateDomain( op, new SingleStringList( cid ) );

  private static final IStridGenerator domainIdGenerator = new UuidStridGenerator();

  /**
   * IDs of classes with M5-models created here, not by KM5 MMUs.
   */
  private final IStringListEdit managedClassIds = new StringLinkedBundleList();

  private IM5Domain     m5      = null; // m5 != null ==> признак наличия привязки m5-ck
  private ISkConnection skConn1 = null;

  /**
   * Constructor.
   */
  public KM5Support() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  public IList<KM5AbstractModelManagementUnit> internalListModelManagementUnits() {
    return getModelManagementUnits().values();
  }

  void initializeDomain() {
    IStringList modelIdsBeforeOp = new StringArrayList( m5.models().ids() );
    try {
      // создаем модель корневого класса
      M5Model<ISkObject> rootModel = new SkObjectKM5Model( skConn() );
      m5.addModel( rootModel );
      // все модули создают свои классы
      IList<KM5AbstractModelManagementUnit> mmUnits = internalListModelManagementUnits();
      for( KM5AbstractModelManagementUnit u : mmUnits ) {
        try {
          u.papiInitModels( m5 );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
      // определим перечень "бесхозных" классов, для которых не были созданы модели
      IStridablesList<ISkClassInfo> allClasses = skSysdescr().listClasses();
      IStringListEdit undoneClassIds = new StringLinkedBundleList( allClasses.ids() );
      undoneClassIds.remove( IGwHardConstants.GW_ROOT_CLASS_ID ); // модель корневого класса уже есть
      for( String mid : m5.models().ids() ) {
        undoneClassIds.remove( mid );
      }
      // оставшимся "бесхозным" классам назначим модели по уолчанию
      for( String cid : undoneClassIds ) {
        ISkClassInfo cinf = allClasses.getByKey( cid );
        M5Model<? extends ISkObject> model = new KM5GenericM5Model<>( cinf, skConn() );
        m5.addModel( model );
      }
      skSysdescr().eventer().addListener( classServiceListener );
    }
    finally {
      IStringListEdit modelIdsAfterOp = new StringLinkedBundleList( m5.models().ids() );
      TsCollectionsUtils.subtract( modelIdsAfterOp, modelIdsBeforeOp );
    }
  }

  void updateDomain( ECrudOp aOp, IStringList aClassIds ) {
    // пока делаем грубо - при массовом изменении переинициализируем всё
    if( aOp == ECrudOp.LIST ) {
      clearDomain();
      initializeDomain();
      return;
    }
    IStringList modelIdsBeforeOp = new StringArrayList( m5.models().ids() );
    try {
      IList<KM5AbstractModelManagementUnit> mmUnits = internalListModelManagementUnits();
      // пусть изменения обработают модули
      IStringListEdit yetUndoneClassIds = new StringLinkedBundleList( aClassIds );
      for( KM5AbstractModelManagementUnit u : mmUnits ) {
        try {
          IStringList moduleDoneClassIds = u.papiUpdateModel( aOp, aClassIds );
          // обработанные классы уберем из yetUndoneClassIds
          for( String cid : moduleDoneClassIds ) {
            yetUndoneClassIds.remove( cid );
          }
          if( yetUndoneClassIds.isEmpty() ) {
            break;
          }
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
      // неотработанные классы обработаем по умолчанию
      for( String cid : yetUndoneClassIds ) {
        switch( aOp ) {
          case CREATE: {
            M5Model<?> model = new KM5GenericM5Model<>( skSysdescr().getClassInfo( cid ), skConn() );
            m5.addModel( model );
            break;
          }
          case EDIT: {
            M5Model<?> model = new KM5GenericM5Model<>( skSysdescr().getClassInfo( cid ), skConn() );
            m5.replaceModel( model );
            break;
          }
          case REMOVE: {
            m5.removeModel( cid );
            break;
          }
          // $CASES-OMITTED$
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
    }
    finally {
      IStringListEdit idsAfter = new StringLinkedBundleList( m5.models().ids() );
      // сначала отработаем удаление моделей
      for( String cid : modelIdsBeforeOp ) {
        if( !idsAfter.hasElem( cid ) ) {
          managedClassIds.remove( cid );
        }
      }
      // теперь - добавление моделей
      for( String cid : idsAfter ) {
        if( !managedClassIds.hasElem( cid ) ) {
          managedClassIds.add( cid );
        }
      }
    }
  }

  void clearDomain() {
    if( skConn().state() == ESkConnState.ACTIVE ) {
      skConn().coreApi().sysdescr().eventer().removeListener( classServiceListener );
    }
    // удалим все ранее созданные нами модели из домена
    for( String cid : managedClassIds ) {
      m5.removeModel( cid );
    }
    managedClassIds.clear();
    // пусть отработают все модули
    IList<KM5AbstractModelManagementUnit> mmUnits = internalListModelManagementUnits();
    for( KM5AbstractModelManagementUnit u : mmUnits ) {
      try {
        u.papiClear();
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    TsIllegalStateRtException.checkNull( skConn1 );
    return skConn1;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IKM5Support
  //

  @Override
  public IM5Domain bind( ISkConnection aSkConn, ITsGuiContext aDomainContext ) {
    TsNullArgumentRtException.checkNulls( aSkConn, aDomainContext );
    TsIllegalStateRtException.checkTrue( isBind() );
    // создание и првязка KM5
    IM5Domain parentM5 = aDomainContext.get( IM5Domain.class );
    m5 = parentM5.createChildDomain( domainIdGenerator.nextId(), aDomainContext );
    skConn1 = aSkConn;
    // установка ссылок друг на друга...
    skConn().scope().put( IM5Domain.class, m5 );
    skConn().scope().put( IKM5Support.class, this );
    m5.tsContext().put( ISkConnection.class, skConn() );
    m5.tsContext().put( IKM5Support.class, this );
    // инициализируем модели сразу, если нужно
    if( skConn().state() == ESkConnState.ACTIVE ) {
      initializeDomain();
    }
    // и теперь спокойно слушаем соединение
    skConn().addConnectionListener( skConnectionListener );
    return m5;
  }

  @Override
  public boolean isBind() {
    return m5 != null;
  }

  @Override
  public void unbind() {
    if( m5 != null ) {
      skConn().removeConnectionListener( skConnectionListener );
      skConn().scope().remove( IM5Domain.class );
      m5.tsContext().remove( ISkConnection.class );
      clearDomain();
      m5 = null;
      skConn1 = null;
    }
  }

  @Override
  public IM5Domain m5() {
    TsIllegalStateRtException.checkNull( m5 );
    return m5;
  }

  @Override
  public IStringMap<KM5AbstractModelManagementUnit> getModelManagementUnits() {
    TsIllegalStateRtException.checkNull( m5 );
    KM5AbstractModelManagementUnit.UnitsList ul =
        skConn().scope().find( KM5AbstractModelManagementUnit.UnitsList.class );
    if( ul == null ) {
      return IStringMap.EMPTY;
    }
    return ul.units();
  }

}
