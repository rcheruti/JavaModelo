package br.eng.rcc.framework.testes.config;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class WeldJUnit4Suite extends Suite {

  public WeldJUnit4Suite(Class<Object> clazz, RunnerBuilder builder) 
          throws InitializationError {
    super(clazz,new WeldRunnerBuilder() );
    System.out.printf("-- 1 %s \n", clazz);
  }
  public WeldJUnit4Suite(RunnerBuilder builder, Class<Object>[] clazz) 
          throws InitializationError {
    super(new WeldRunnerBuilder() ,clazz);
    System.out.printf("-- 2 \n");
  }

  
}
