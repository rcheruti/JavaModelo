
package br.eng.rcc.framework.testes.config;

import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class WeldRunnerBuilder extends RunnerBuilder{
  
  
  @Override
  public Runner runnerForClass(Class c) throws Throwable{
    return new WeldJUnit4Runner(c); 
  }
  
}
