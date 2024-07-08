/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.toxsoft.uskat.core.gui.km5.sded.sded.opc_ua_server;

import static com.google.common.collect.Lists.*;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.*;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.*;

import org.bouncycastle.jce.provider.*;
import org.eclipse.milo.opcua.sdk.server.*;
import org.eclipse.milo.opcua.sdk.server.api.config.*;
import org.eclipse.milo.opcua.sdk.server.identity.*;
import org.eclipse.milo.opcua.sdk.server.util.*;
import org.eclipse.milo.opcua.stack.core.*;
import org.eclipse.milo.opcua.stack.core.security.*;
import org.eclipse.milo.opcua.stack.core.transport.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.eclipse.milo.opcua.stack.core.util.*;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.*;
import org.slf4j.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

public class ExampleServer {

  private static final int TCP_BIND_PORT   = 12686;
  private static final int HTTPS_BIND_PORT = 8443;

  static {
    // Required for SecurityPolicy.Aes256_Sha256_RsaPss
    Security.addProvider( new BouncyCastleProvider() );

    try {
      NonceUtil.blockUntilSecureRandomSeeded( 10, TimeUnit.SECONDS );
    }
    catch( ExecutionException | InterruptedException | TimeoutException e ) {
      e.printStackTrace();
      System.exit( -1 );
    }
  }

  // public static void main( String[] args )
  // throws Exception {
  // ExampleServer server = new ExampleServer();
  //
  // server.startup().get();
  //
  // final CompletableFuture<Void> future = new CompletableFuture<>();
  //
  // Runtime.getRuntime().addShutdownHook( new Thread( () -> future.complete( null ) ) );
  //
  // future.get();
  // }

  private final OpcUaServer      server;
  private final ExampleNamespace exampleNamespace;

  public ExampleServer( ISkConnection aISkConnection, ISkClassInfo aSel )
      throws Exception {
    Path securityTempDir = Paths.get( System.getProperty( "java.io.tmpdir" ), "server", "security" );
    Files.createDirectories( securityTempDir );
    if( !Files.exists( securityTempDir ) ) {
      throw new Exception( "unable to create security temp dir: " + securityTempDir );
    }

    File pkiDir = securityTempDir.resolve( "pki" ).toFile();

    LoggerFactory.getLogger( getClass() ).info( "security dir: {}", securityTempDir.toAbsolutePath() );
    LoggerFactory.getLogger( getClass() ).info( "security pki dir: {}", pkiDir.getAbsolutePath() );

    KeyStoreLoader loader = new KeyStoreLoader().load( securityTempDir );

    DefaultCertificateManager certificateManager =
        new DefaultCertificateManager( loader.getServerKeyPair(), loader.getServerCertificateChain() );

    DefaultTrustListManager trustListManager = new DefaultTrustListManager( pkiDir );

    DefaultServerCertificateValidator certificateValidator = new DefaultServerCertificateValidator( trustListManager );

    KeyPair httpsKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair( 2048 );

    SelfSignedHttpsCertificateBuilder httpsCertificateBuilder = new SelfSignedHttpsCertificateBuilder( httpsKeyPair );
    httpsCertificateBuilder.setCommonName( HostnameUtil.getHostname() );
    HostnameUtil.getHostnames( "0.0.0.0" ).forEach( httpsCertificateBuilder::addDnsName );
    X509Certificate httpsCertificate = httpsCertificateBuilder.build();

    UsernameIdentityValidator identityValidator = new UsernameIdentityValidator( true, authChallenge -> {
      String username = authChallenge.getUsername();
      String password = authChallenge.getPassword();

      boolean userOk = "user".equals( username ) && "password1".equals( password );
      boolean adminOk = "admin".equals( username ) && "password2".equals( password );

      return userOk || adminOk;
    } );

    X509IdentityValidator x509IdentityValidator = new X509IdentityValidator( c -> true );

    // If you need to use multiple certificates you'll have to be smarter than this.
    X509Certificate certificate = certificateManager.getCertificates().stream().findFirst()
        .orElseThrow( () -> new UaRuntimeException( StatusCodes.Bad_ConfigurationError, "no certificate found" ) );

    // The configured application URI must match the one in the certificate(s)
    String applicationUri = CertificateUtil.getSanUri( certificate )
        .orElseThrow( () -> new UaRuntimeException( StatusCodes.Bad_ConfigurationError,
            "certificate is missing the application URI" ) );

    Set<EndpointConfiguration> endpointConfigurations = createEndpointConfigurations( certificate );

    OpcUaServerConfig serverConfig = OpcUaServerConfig.builder().setApplicationUri( applicationUri )
        .setApplicationName( LocalizedText.english( "OPC UA USkat Server" ) ).setEndpoints( endpointConfigurations )
        .setBuildInfo( new BuildInfo( "urn:toxsoft:uskat:opcua-server", "ToxSoft Ltd.", "ToxSoft USkat OPC UA server",
            OpcUaServer.SDK_VERSION, "", DateTime.now() ) )
        .setCertificateManager( certificateManager ).setTrustListManager( trustListManager )
        .setCertificateValidator( certificateValidator ).setHttpsKeyPair( httpsKeyPair )
        // .setHttpsCertificateChain( new X509Certificate[] { httpsCertificate } )
        .setIdentityValidator( new CompositeValidator( identityValidator, x509IdentityValidator ) )
        .setProductUri( "urn:toxsoft:uskat:opcua-server" ).build();

    server = new OpcUaServer( serverConfig );

    exampleNamespace = new ExampleNamespace( server, aISkConnection, aSel );
    exampleNamespace.startup();
  }

  private Set<EndpointConfiguration> createEndpointConfigurations( X509Certificate certificate ) {
    Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();

    List<String> bindAddresses = newArrayList();
    bindAddresses.add( "0.0.0.0" );

    Set<String> hostnames = new LinkedHashSet<>();
    hostnames.add( HostnameUtil.getHostname() );
    hostnames.addAll( HostnameUtil.getHostnames( "0.0.0.0" ) );

    for( String bindAddress : bindAddresses ) {
      for( String hostname : hostnames ) {
        EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder().setBindAddress( bindAddress )
            // .setHostname( hostname ).setPath( "/milo" ).setCertificate( certificate )
            .setHostname( hostname ).setPath( "/uskat" ).setCertificate( certificate )
            .addTokenPolicies( USER_TOKEN_POLICY_ANONYMOUS, USER_TOKEN_POLICY_USERNAME, USER_TOKEN_POLICY_X509 );

        EndpointConfiguration.Builder noSecurityBuilder =
            builder.copy().setSecurityPolicy( SecurityPolicy.None ).setSecurityMode( MessageSecurityMode.None );

        endpointConfigurations.add( buildTcpEndpoint( noSecurityBuilder ) );
        endpointConfigurations.add( buildHttpsEndpoint( noSecurityBuilder ) );

        // TCP Basic256Sha256 / SignAndEncrypt
        endpointConfigurations.add( buildTcpEndpoint( builder.copy().setSecurityPolicy( SecurityPolicy.Basic256Sha256 )
            .setSecurityMode( MessageSecurityMode.SignAndEncrypt ) ) );

        // HTTPS Basic256Sha256 / Sign (SignAndEncrypt not allowed for HTTPS)
        endpointConfigurations.add( buildHttpsEndpoint( builder.copy()
            .setSecurityPolicy( SecurityPolicy.Basic256Sha256 ).setSecurityMode( MessageSecurityMode.Sign ) ) );

        /*
         * It's good practice to provide a discovery-specific endpoint with no security. It's required practice if all
         * regular endpoints have security configured. Usage of the "/discovery" suffix is defined by OPC UA Part 6:
         * Each OPC UA Server Application implements the Discovery Service Set. If the OPC UA Server requires a
         * different address for this Endpoint it shall create the address by appending the path "/discovery" to its
         * base address.
         */

        EndpointConfiguration.Builder discoveryBuilder = builder.copy().setPath( "/milo/discovery" )
            .setSecurityPolicy( SecurityPolicy.None ).setSecurityMode( MessageSecurityMode.None );

        endpointConfigurations.add( buildTcpEndpoint( discoveryBuilder ) );
        endpointConfigurations.add( buildHttpsEndpoint( discoveryBuilder ) );
      }
    }

    return endpointConfigurations;
  }

  private static EndpointConfiguration buildTcpEndpoint( EndpointConfiguration.Builder base ) {
    return base.copy().setTransportProfile( TransportProfile.TCP_UASC_UABINARY ).setBindPort( TCP_BIND_PORT ).build();
  }

  private static EndpointConfiguration buildHttpsEndpoint( EndpointConfiguration.Builder base ) {
    return base.copy().setTransportProfile( TransportProfile.HTTPS_UABINARY ).setBindPort( HTTPS_BIND_PORT ).build();
  }

  public OpcUaServer getServer() {
    return server;
  }

  public CompletableFuture<OpcUaServer> startup() {
    return server.startup();
  }

  public CompletableFuture<OpcUaServer> shutdown() {
    exampleNamespace.shutdown();

    return server.shutdown();
  }

}
