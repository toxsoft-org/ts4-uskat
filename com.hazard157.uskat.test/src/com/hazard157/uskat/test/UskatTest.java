package com.hazard157.uskat.test;

import static org.toxsoft.core.tslib.utils.TsTestUtils.*;

import org.toxsoft.uskat.core.impl.*;

/**
 * COnsole test app.
 *
 * @author hazard157
 */
@SuppressWarnings( { "javadoc", "nls" } )
public class UskatTest {

  public static void main( String[] aArgs ) {
    pl( "USkat test #1." );

    SkUtils.initialize();

    Test1 t = new Test1();
    t.run();

    pl( "Test finished." );
    nl();
  }

}
