package org.toxsoft.uskat.skadmin.dev.batch;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.batch.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.batch.IAdminHardResources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.filter.impl.TsCombiFilterParamsKeeper;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContextParam;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;
import org.toxsoft.uskat.sysext.batchop.ISkBatchOperationService;
import org.toxsoft.uskat.sysext.batchop.impl.SkBatchOperationService;

import ru.uskat.backend.addons.batchops.EOrphanProcessing;
import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.container.DpuContainerKeeper;
import ru.uskat.common.dpu.container.IDpuContainer;
import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.connection.ISkConnection;

/**
 * Команда s5admin: пакетное чтение данных системы в контейнер {@link IDpuContainer}
 * {@link ISkBackendAddonBatchOperations#batchRead(IOptionSet, ITsCombiFilterParams, ITsCombiFilterParams, ITsCombiFilterParams)}
 *
 * @author mvk
 */
public class AdminCmdBatchRead
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdBatchRead() {
    addArg( CTX_SK_CONNECTION );
    addArg( ARG_INCLUDE_SYSTEM_ENTITIES );
    addArg( ARG_INCLUDE_CLASS_INFOS );
    addArg( ARG_INCLUDE_OBJECTS );
    addArg( ARG_INCLUDE_LINKS );
    addArg( ARG_ORPHAN_CLASSES );
    addArg( ARG_ORPHAN_LINKS );
    addArg( ARG_CLASS_FILTER );
    addArg( ARG_TYPE_FILTER );
    addArg( ARG_CLOB_FILTER );
    addArg( ARG_FILE );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_BATCH_READ_ID;
  }

  @Override
  public String alias() {
    return CMD_BATCH_READ_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_BATCH_READ_NAME;
  }

  @Override
  public String description() {
    return CMD_BATCH_READ_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return DPU_CONTAINER_PARAM.type();
  }

  @Override
  public String resultDescription() {
    return DPU_CONTAINER_PARAM.description();
  }

  @Override
  public IStridablesList<IAdminCmdContextParam> resultContextParams() {
    IStridablesListEdit<IAdminCmdContextParam> params = new StridablesList<>();
    params.add( DPU_CONTAINER_PARAM );
    return params;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    try {
      ISkCoreApi coreApi = connection.coreApi();
      ISkBatchOperationService batchService =
          (ISkBatchOperationService)coreApi.services().findByKey( ISkBatchOperationService.SERVICE_ID );
      if( batchService == null ) {
        batchService = coreApi.addService( SkBatchOperationService.CREATOR );
      }
      IAtomicValue includeSystemEntities = argSingleValue( ARG_INCLUDE_SYSTEM_ENTITIES, AV_FALSE );
      IAtomicValue includeClassInfos = argSingleValue( ARG_INCLUDE_CLASS_INFOS, AV_TRUE );
      IAtomicValue includeObjects = argSingleValue( ARG_INCLUDE_OBJECTS, AV_TRUE );
      IAtomicValue includeLinks = argSingleValue( ARG_INCLUDE_LINKS, AV_TRUE );
      IAtomicValue orphanClassesProcessing =
          argSingleValue( ARG_ORPHAN_CLASSES, avStr( EOrphanProcessing.ENRICH.id() ) );
      IAtomicValue orphanLinksProcessing = argSingleValue( ARG_ORPHAN_LINKS, avStr( EOrphanProcessing.REMOVE.id() ) );
      IAtomicValue classIdsFilter = argSingleValue( ARG_CLASS_FILTER, AV_STR_EMPTY );
      IAtomicValue typeIdsFilter = argSingleValue( ARG_TYPE_FILTER, AV_STR_EMPTY );
      IAtomicValue clobIdsFilter = argSingleValue( ARG_CLOB_FILTER, AV_STR_EMPTY );
      IAtomicValue fileName = argSingleValue( ARG_FILE, AV_STR_EMPTY );

      try {
        // Время начала выполнения команды запроса событий
        long startTime = System.currentTimeMillis();

        IOptionSetEdit ops = new OptionSet();
        ops.setValue( ISkBackendAddonBatchOperations.OPDEF_INCLUDE_SYSTEM_ENTITIES, includeSystemEntities );
        ops.setValue( ISkBackendAddonBatchOperations.OPDEF_INCLUDE_CLASS_INFOS, includeClassInfos );
        ops.setValue( ISkBackendAddonBatchOperations.OPDEF_INCLUDE_OBJECTS, includeObjects );
        ops.setValue( ISkBackendAddonBatchOperations.OPDEF_INCLUDE_LINKS, includeLinks );
        ops.setValobj( ISkBackendAddonBatchOperations.OPDEF_ORPHAN_CLASSES,
            EOrphanProcessing.getById( orphanClassesProcessing.toString() ) );
        ops.setValobj( ISkBackendAddonBatchOperations.OPDEF_ORPHAN_LINKS,
            EOrphanProcessing.getById( orphanLinksProcessing.toString() ) );

        IDpuContainer container = batchService.batchRead( ops, //
            getFilter( classIdsFilter.asString(), ITsCombiFilterParams.ALL ), //
            getFilter( typeIdsFilter.asString(), ITsCombiFilterParams.ALL ), //
            getFilter( clobIdsFilter.asString(), ITsCombiFilterParams.NONE ) //
        );
        IPlexyValue dpuContainer = pvSingleRef( container );
        setContextParamValue( DPU_CONTAINER_PARAM, dpuContainer );
        if( fileName.asString().length() > 0 ) {
          // Сохранение контейнера в файле
          File file = new File( fileName.asString() );
          try( FileOutputStream output = new FileOutputStream( file );
              OutputStreamWriter writer = new OutputStreamWriter( output, CHARSET_DEFAULT ); ) {
            writer.write( DpuContainerKeeper.KEEPER.ent2str( container ) );
          }
        }
        long delta = (System.currentTimeMillis() - startTime) / 1000;
        addResultInfo( '\n' + MSG_CMD_TIME, Long.valueOf( delta ) );
        resultOk( dpuContainer );
      }
      catch( Throwable e ) {
        addResultError( e );
        resultFail();
      }
    }
    finally {
      connection = null;
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
    if( pxCoreApi == null ) {
      return IList.EMPTY;
    }
    if( aArgId.equals( ARG_ORPHAN_CLASSES.id() ) || //
        aArgId.equals( ARG_ORPHAN_LINKS.id() ) ) {
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      for( EOrphanProcessing orphan : EOrphanProcessing.values() ) {
        IAtomicValue dataValue = avStr( orphan.id() );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает описание фильтра из атомарного (текстового) значения
   *
   * @param aFilterString String фильтр в текстовом виде. пустая строка: неопределенно
   * @param aDefaultValue {@link ITsCombiFilterParams} значение по умолчанию
   * @return {@link ITsCombiFilterParams} описание фильтра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static ITsCombiFilterParams getFilter( String aFilterString, ITsCombiFilterParams aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aFilterString, aDefaultValue );
    if( aFilterString.length() <= 0 ) {
      return aDefaultValue;
    }
    return TsCombiFilterParamsKeeper.KEEPER.str2ent( aFilterString );
  }
}
