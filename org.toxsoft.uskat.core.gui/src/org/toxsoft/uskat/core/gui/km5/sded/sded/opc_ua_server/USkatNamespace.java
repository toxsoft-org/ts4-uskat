/*
 * Copyright (c) 2021 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.toxsoft.uskat.core.gui.km5.sded.sded.opc_ua_server;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.eclipse.milo.examples.server.*;
import org.eclipse.milo.opcua.sdk.core.*;
import org.eclipse.milo.opcua.sdk.server.*;
import org.eclipse.milo.opcua.sdk.server.api.*;
import org.eclipse.milo.opcua.sdk.server.nodes.*;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.*;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext.*;
import org.eclipse.milo.opcua.sdk.server.util.*;
import org.eclipse.milo.opcua.stack.core.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.slf4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.skf.rri.lib.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * USkat OPC UA public namespace
 *
 * @author dima
 */
public class USkatNamespace
    extends ManagedNamespaceWithLifecycle {

  /**
   * Get actual attr value from inside OPC UA server inner thread
   *
   * @author dima
   */
  public class AttrHandler
      implements Function<GetAttributeContext, DataValue> {

    private final IDtoAttrInfo attrInfo;
    private IAtomicValue       currVal;
    private final Gwid         attrGwid;

    AttrHandler( Skid aSkid, IDtoAttrInfo aDtoAttrInfo ) {
      attrInfo = aDtoAttrInfo;
      attrGwid = Gwid.createAttr( aSkid, attrInfo.id() );
      currVal = conn.coreApi().objService().get( attrGwid.skid() ).attrs().getValue( attrGwid.propId() );
    }

    @Override
    public DataValue apply( GetAttributeContext aContext ) {
      threadExecutor.syncExec( () -> {
        currVal = conn.coreApi().objService().get( attrGwid.skid() ).attrs().getValue( attrGwid.propId() );
      } );
      Variant val = convertToOpc( currVal, attrInfo.dataType().atomicType() );
      return new DataValue( val );
    }
  }

  /**
   * Get actual rtData value from inside OPC UA server inner thread
   *
   * @author dima
   */
  public class RtDataHandler
      implements Function<GetAttributeContext, DataValue> {

    final IDtoRtdataInfo           dataInfo;
    private IAtomicValue           currVal;
    private ISkReadCurrDataChannel channel;

    /**
     * @return {@link ISkReadCurrDataChannel} rtData channel
     */
    public ISkReadCurrDataChannel getChannel() {
      return channel;
    }

    RtDataHandler( Skid aSkid, IDtoRtdataInfo aDtoRtdataInfo ) {
      dataInfo = aDtoRtdataInfo;
      Gwid rtdGwid = Gwid.createRtdata( aSkid, dataInfo.id() );

      channel = conn.coreApi().rtdService().createReadCurrDataChannels( new GwidList( rtdGwid ) ).values().first();
      currVal = channel.getValue();
    }

    @Override
    public DataValue apply( GetAttributeContext aContext ) {
      threadExecutor.syncExec( () -> {
        currVal = channel.getValue();
      } );
      Variant val = convertToOpc( currVal, dataInfo.dataType().atomicType() );
      return new DataValue( val );
    }
  }

  /**
   * Get actual RRI attribute value from inside OPC UA server inner thread
   *
   * @author dima
   */
  public class RriAttrHandler
      implements Function<GetAttributeContext, DataValue> {

    private IAtomicValue           currVal;
    private final Gwid             rriGwid;
    private final IDtoRriParamInfo rriParamInfo;

    RriAttrHandler( Skid aSkid, IDtoRriParamInfo aDtoRriParamInfo ) {
      rriParamInfo = aDtoRriParamInfo;
      rriGwid = Gwid.createAttr( aSkid, rriParamInfo.id() );

      currVal = rriSection.getAttrParamValue( rriGwid.skid(), rriGwid.propId() );
    }

    @Override
    public DataValue apply( GetAttributeContext aContext ) {
      threadExecutor.syncExec( () -> {
        currVal = rriSection.getAttrParamValue( rriGwid.skid(), rriGwid.propId() );
      } );
      Variant val = convertToOpc( currVal, rriParamInfo.attrInfo().dataType().atomicType() );
      return new DataValue( val );
    }
  }

  // FIXME change to toxsoft:uskat:concrete_project
  // public static final String NAMESPACE_URI = "urn:eclipse:milo:hello-world";
  public static final String NAMESPACE_URI = "urn:toxsoft:uskat:mmk";
  public static final String USKAT_FOLDER  = "USkat";

  private static final Object[][] STATIC_SCALAR_NODES = { { "Boolean", Identifiers.Boolean, new Variant( false ) },
      { "Byte", Identifiers.Byte, new Variant( ubyte( 0x00 ) ) },
      { "SByte", Identifiers.SByte, new Variant( (byte)0x00 ) }, { "Integer", Identifiers.Integer, new Variant( 32 ) },
      { "Int16", Identifiers.Int16, new Variant( (short)16 ) }, { "Int32", Identifiers.Int32, new Variant( 32 ) },
      { "Int64", Identifiers.Int64, new Variant( 64L ) },
      { "UInteger", Identifiers.UInteger, new Variant( uint( 32 ) ) },
      { "UInt16", Identifiers.UInt16, new Variant( ushort( 16 ) ) },
      { "UInt32", Identifiers.UInt32, new Variant( uint( 32 ) ) },
      { "UInt64", Identifiers.UInt64, new Variant( ulong( 64L ) ) },
      { "Float", Identifiers.Float, new Variant( 3.14f ) }, { "Double", Identifiers.Double, new Variant( 3.14d ) },
      { "String", Identifiers.String, new Variant( "string value" ) },
      { "DateTime", Identifiers.DateTime, new Variant( DateTime.now() ) },
      { "Guid", Identifiers.Guid, new Variant( UUID.randomUUID() ) },
      { "ByteString", Identifiers.ByteString, new Variant( new ByteString( new byte[] { 0x01, 0x02, 0x03, 0x04 } ) ) },
      { "XmlElement", Identifiers.XmlElement, new Variant( new XmlElement( "<a>hello</a>" ) ) },
      { "LocalizedText", Identifiers.LocalizedText, new Variant( LocalizedText.english( "localized text" ) ) },
      { "QualifiedName", Identifiers.QualifiedName, new Variant( new QualifiedName( 1234, "defg" ) ) },
      { "NodeId", Identifiers.NodeId, new Variant( new NodeId( 1234, "abcd" ) ) },
      { "Variant", Identifiers.BaseDataType, new Variant( 32 ) },
      { "Duration", Identifiers.Duration, new Variant( 1.0 ) },
      { "UtcTime", Identifiers.UtcTime, new Variant( DateTime.now() ) }, };

  private static final Object[][] STATIC_ARRAY_NODES = { { "BooleanArray", Identifiers.Boolean, false },
      { "ByteArray", Identifiers.Byte, ubyte( 0 ) }, { "SByteArray", Identifiers.SByte, (byte)0x00 },
      { "Int16Array", Identifiers.Int16, (short)16 }, { "Int32Array", Identifiers.Int32, 32 },
      { "Int64Array", Identifiers.Int64, 64L }, { "UInt16Array", Identifiers.UInt16, ushort( 16 ) },
      { "UInt32Array", Identifiers.UInt32, uint( 32 ) }, { "UInt64Array", Identifiers.UInt64, ulong( 64L ) },
      { "FloatArray", Identifiers.Float, 3.14f }, { "DoubleArray", Identifiers.Double, 3.14d },
      { "StringArray", Identifiers.String, "string value" }, { "DateTimeArray", Identifiers.DateTime, DateTime.now() },
      { "GuidArray", Identifiers.Guid, UUID.randomUUID() },
      { "ByteStringArray", Identifiers.ByteString, new ByteString( new byte[] { 0x01, 0x02, 0x03, 0x04 } ) },
      { "XmlElementArray", Identifiers.XmlElement, new XmlElement( "<a>hello</a>" ) },
      { "LocalizedTextArray", Identifiers.LocalizedText, LocalizedText.english( "localized text" ) },
      { "QualifiedNameArray", Identifiers.QualifiedName, new QualifiedName( 1234, "defg" ) },
      { "NodeIdArray", Identifiers.NodeId, new NodeId( 1234, "abcd" ) } };

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  private volatile Thread  eventThread;
  private volatile boolean keepPostingEvents = true;

  private final Random random = new Random();

  private final DataTypeDictionaryManager dictionaryManager;

  private final SubscriptionModel subscriptionModel;

  private final ISkConnection conn;

  private final ITsThreadExecutor    threadExecutor;
  private final IList<RtDataHandler> rtDataHandlers = new ElemArrayList<>();
  private final ISkRriSection        rriSection;

  USkatNamespace( OpcUaServer server, ISkConnection aConnection ) {
    super( server, NAMESPACE_URI );
    conn = aConnection;
    subscriptionModel = new SubscriptionModel( server, this );
    dictionaryManager = new DataTypeDictionaryManager( getNodeContext(), NAMESPACE_URI );
    // get executor to USkat server communicate with
    threadExecutor = SkThreadExecutorService.getExecutor( aConnection.coreApi() );
    // init RRI section
    ISkRegRefInfoService rriService =
        (ISkRegRefInfoService)conn.coreApi().services().getByKey( ISkRegRefInfoService.SERVICE_ID );
    rriSection = rriService.listSections().first();

    getLifecycleManager().addLifecycle( dictionaryManager );
    getLifecycleManager().addLifecycle( subscriptionModel );

    getLifecycleManager().addStartupTask( this::createAndAddNodes );

    // for some specific USkat work
    getLifecycleManager().addLifecycle( new Lifecycle() {

      @Override
      public void startup() {
        // nop
      }

      @Override
      public void shutdown() {
        // close open rtData channels
        for( RtDataHandler rtdHandler : rtDataHandlers ) {
          rtdHandler.getChannel().close();
        }
      }
    } );

  }

  private void createAndAddNodes() {
    // Create a "USkat" folder and add it to the node manager
    NodeId folderNodeId = newNodeId( USKAT_FOLDER );

    UaFolderNode folderNode = new UaFolderNode( getNodeContext(), folderNodeId, newQualifiedName( USKAT_FOLDER ),
        LocalizedText.english( USKAT_FOLDER ) );

    getNodeManager().addNode( folderNode );

    // Make sure our new folder shows up under the server's Objects folder.
    folderNode.addReference(
        new Reference( folderNode.getNodeId(), Identifiers.Organizes, Identifiers.ObjectsFolder.expanded(), false ) );

    // old version save for testing
    // addCustomObjectTypeAndInstance( folderNode );

    IStridablesList<ISkClassInfo> allItems = conn.coreApi().sysdescr().listClasses();
    IStridablesListEdit<ISkClassInfo> items2Public = new StridablesList<>();
    for( ISkClassInfo cinf : allItems ) {
      if( cinf.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
        continue;
      }
      if( cinf.id().startsWith( "sk." ) ) {
        continue;
      }
      String claimerId = conn.coreApi().sysdescr().determineClassClaimingServiceId( cinf.id() );
      if( claimerId.equals( ISkSysdescr.SERVICE_ID ) ) {
        items2Public.add( cinf );
      }
    }
    // public classes and it's objects
    for( ISkClassInfo item : items2Public ) {
      // create classes subfolder
      UaFolderNode subfolderNode = new UaFolderNode( getNodeContext(), newNodeId( USKAT_FOLDER + "/" + item.id() ),
          newQualifiedName( getName4Browse( item ) ), LocalizedText.english( getName4Display( item ) ) );

      getNodeManager().addNode( subfolderNode );
      folderNode.addOrganizes( subfolderNode );

      addClassObjectTypeAndInstance( subfolderNode, item );
    }
  }

  private static NodeId getDataType( IDataType aDataType ) {
    NodeId retVal = Identifiers.Int16;
    retVal = switch( aDataType.atomicType() ) {
      case BOOLEAN -> Identifiers.Boolean;
      case FLOATING -> Identifiers.Float;
      case INTEGER -> Identifiers.Int64;
      case STRING -> Identifiers.String;
      case TIMESTAMP -> Identifiers.Time;
      case NONE, VALOBJ -> throw new TsIllegalStateRtException( "Can't map type %s to OPC UA type",
          aDataType.atomicType().toString() );
      default -> throw new TsIllegalStateRtException( "Can't map type %s to OPC UA type",
          aDataType.atomicType().toString() );
    };
    return retVal;
  }

  private static Variant convertToOpc( IAtomicValue aValue, EAtomicType aTagType ) {
    Variant result = Variant.NULL_VALUE;
    if( aValue.isAssigned() ) {
      switch( aTagType ) {
        case BOOLEAN:
          result = new Variant( Boolean.valueOf( aValue.asBool() ) );
          break;
        case FLOATING:
          result = new Variant( Float.valueOf( aValue.asFloat() ) );
          break;
        case INTEGER: {
          result = new Variant( Integer.valueOf( aValue.asInt() ) );
          break;
        }
        case NONE: {
          result = Variant.NULL_VALUE;
          break;
        }
        case STRING: {
          result = new Variant( aValue.asString() );
          break;
        }
        case TIMESTAMP: {
          result = new Variant( Long.valueOf( aValue.asLong() ) );
          break;
        }
        case VALOBJ: {
          result = new Variant( aValue.asValobj() );
          break;
        }
        default: {
          result = new Variant( aValue.asString() );
        }
      }
    }
    return result;
  }

  @Override
  public void onDataItemsCreated( List<DataItem> dataItems ) {
    subscriptionModel.onDataItemsCreated( dataItems );
  }

  @Override
  public void onDataItemsModified( List<DataItem> dataItems ) {
    subscriptionModel.onDataItemsModified( dataItems );
  }

  @Override
  public void onDataItemsDeleted( List<DataItem> dataItems ) {
    subscriptionModel.onDataItemsDeleted( dataItems );
  }

  @Override
  public void onMonitoringModeChanged( List<MonitoredItem> monitoredItems ) {
    subscriptionModel.onMonitoringModeChanged( monitoredItems );
  }

  private void addClassObjectTypeAndInstance( UaFolderNode aRootFolder, ISkClassInfo aClassInfo ) {
    // Define a new ObjectType
    String typeName = aClassInfo.id();
    UaObjectTypeNode objectTypeNode = UaObjectTypeNode.builder( getNodeContext() ) //
        .setNodeId( newNodeId( "ObjectTypes/" + typeName ) ) //
        .setBrowseName( newQualifiedName( aClassInfo.nmName() ) ) //
        .setDisplayName( LocalizedText.english( typeName ) ).setIsAbstract( false ) //
        .build();

    IListEdit<UaVariableNode> rtDataNodes = new ElemArrayList<>();
    for( IDtoRtdataInfo dataInfo : aClassInfo.rtdata().list() ) {
      UaVariableNode dataNode = UaVariableNode.builder( getNodeContext() )
          .setNodeId( newNodeId( "ObjectTypes/" + typeName + "." + getName4NodeId( dataInfo ) ) ) //
          .setAccessLevel( AccessLevel.READ_WRITE ) //
          .setBrowseName( newQualifiedName( getName4Browse( dataInfo ) ) ) //
          .setDisplayName( LocalizedText.english( getName4Display( dataInfo ) ) ) //
          .setDataType( getDataType( dataInfo.dataType() ) ) //
          .setTypeDefinition( Identifiers.BaseDataVariableType ).build(); //

      dataNode.addReference( new Reference( dataNode.getNodeId(), Identifiers.HasModellingRule,
          Identifiers.ModellingRule_Mandatory.expanded(), true ) );

      objectTypeNode.addComponent( dataNode );
      rtDataNodes.add( dataNode );
    }

    IListEdit<UaVariableNode> rriAttrNodes = new ElemArrayList<>();
    IStridablesList<IDtoRriParamInfo> rriInfoes = rriSection.listParamInfoes( aClassInfo.id() );
    for( IDtoRriParamInfo rriInfo : rriInfoes ) {
      if( rriInfo.isLink() ) {
        continue;
      }
      UaVariableNode rriAttrNode = UaVariableNode.builder( getNodeContext() )
          .setNodeId( newNodeId( "ObjectTypes/" + typeName + "." + rriInfo.id() ) )
          .setAccessLevel( AccessLevel.READ_WRITE ) //
          .setBrowseName( newQualifiedName( getName4Browse( rriInfo ) ) ) //
          .setDisplayName( LocalizedText.english( getName4Display( rriInfo ) ) ) //
          .setDataType( getDataType( rriInfo.attrInfo().dataType() ) ) //
          .setTypeDefinition( Identifiers.BaseDataVariableType ) //
          .build();

      rriAttrNode.addReference( new Reference( rriAttrNode.getNodeId(), Identifiers.HasModellingRule,
          Identifiers.ModellingRule_Mandatory.expanded(), true ) );

      objectTypeNode.addComponent( rriAttrNode );
      rriAttrNodes.add( rriAttrNode );
    }

    // Tell the ObjectTypeManager about our new type.
    // This let's us use NodeFactory to instantiate instances of the type.
    getServer().getObjectTypeManager().registerObjectType( objectTypeNode.getNodeId(), UaObjectNode.class,
        UaObjectNode::new );

    // Add the inverse SubtypeOf relationship.
    objectTypeNode.addReference( new Reference( objectTypeNode.getNodeId(), Identifiers.HasSubtype,
        Identifiers.BaseObjectType.expanded(), false ) );

    // Add type definition and declarations to address space.
    getNodeManager().addNode( objectTypeNode );
    for( UaVariableNode dataNode : rtDataNodes ) {
      getNodeManager().addNode( dataNode );
    }
    for( UaVariableNode rriAttrNode : rriAttrNodes ) {
      getNodeManager().addNode( rriAttrNode );
    }

    // Use NodeFactory to create instance of new type called selClass.id().
    // NodeFactory takes care of recursively instantiating class member nodes
    // as well as adding all nodes to the address space.
    try {
      // get all objects
      IList<ISkObject> objs = conn.coreApi().objService().listObjs( aClassInfo.id(), false );
      for( ISkObject obj : objs ) {
        UaObjectNode objNode = (UaObjectNode)getNodeFactory()
            .createNode( newNodeId( USKAT_FOLDER + "/" + obj.classId() + "/" + obj.id() ), objectTypeNode.getNodeId() );
        objNode.setBrowseName( newQualifiedName( getName4Browse( obj ) ) );
        objNode.setDisplayName( LocalizedText.english( getName4Display( obj ) ) );
        // Add forward and inverse references from the root folder.
        aRootFolder.addOrganizes( objNode );

        objNode.addReference(
            new Reference( objNode.getNodeId(), Identifiers.Organizes, aRootFolder.getNodeId().expanded(), false ) );

        List<UaNode> propNodes = objNode.getComponentNodes().stream().distinct().collect( Collectors.toList() );
        for( UaNode prop : propNodes ) {
          if( prop instanceof UaVariableNode varNode ) {
            String propId = prop.getBrowseName().getName();
            if( aClassInfo.rtdata().list().hasKey( propId ) ) {
              IDtoRtdataInfo dataInfo = aClassInfo.rtdata().list().getByKey( propId );
              varNode.getFilterChain().addLast( new AttributeLoggingFilter(),
                  AttributeFilters.getValue( new RtDataHandler( obj.skid(), dataInfo ) ) );
            }
            else {
              if( rriInfoes.hasKey( propId ) ) {
                IDtoRriParamInfo rriInfo = rriInfoes.getByKey( propId );
                varNode.getFilterChain().addLast( new AttributeLoggingFilter(),
                    AttributeFilters.getValue( new RriAttrHandler( obj.skid(), rriInfo ) ) );
              }
              else {
                LoggerUtils.errorLogger().error(
                    "Class %s, can't map any RtData or rri attribute param to node with display name: %s",
                    obj.classId(), prop.getDisplayName().getText() );
              }
            }
          }
        }
      }
    }
    catch( UaException e ) {
      logger.error( "Error creating MyObjectType instance: {}", e.getMessage(), e );
    }
  }

  private static String getName4Display( IStridable aPropInfo ) {
    return aPropInfo.nmName();
  }

  private static String getName4Browse( IStridable aPropInfo ) {
    return aPropInfo.id();
  }

  private static String getName4NodeId( IStridable aPropInfo ) {
    return aPropInfo.id();
  }

}
