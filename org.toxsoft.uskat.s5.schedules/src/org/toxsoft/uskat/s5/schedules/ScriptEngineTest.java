package org.toxsoft.uskat.s5.schedules;

import javax.script.*;

import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;

public class ScriptEngineTest {

  @SuppressWarnings( "nls" )
  public static void main( String[] args ) {
    ScriptEngineManager sm = new ScriptEngineManager();
    ScriptEngine graalEngine = sm.getEngineByName( "graal.js" );
    try {
      graalEngine.eval( "print('Hello World!');" );
    }
    catch( ScriptException ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
  }
}
