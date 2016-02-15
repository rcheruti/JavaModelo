
package br.eng.rcc.framework.utils;

import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PersistenceUtilsTest {
  
  public PersistenceUtilsTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public 'void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of nullifyLazy method, of class PersistenceUtils.
   */
  @Test
  public void testNullifyLazy_3args() {
    System.out.println("nullifyLazy");
    EntityManager em = null;
    Object[] lista = null;
    String[] params = null;
    PersistenceUtils.nullifyLazy(em, lista, params);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of nullifyLazy method, of class PersistenceUtils.
   */
  @Test
  public void testNullifyLazy_4args() {
    System.out.println("nullifyLazy");
    EntityManager em = null;
    Object[] lista = null;
    String[] params = null;
    int secureLevel = 0;
    PersistenceUtils.nullifyLazy(em, lista, params, secureLevel);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of constainsInArray method, of class PersistenceUtils.
   */
  @Test
  public void testConstainsInArray() {
    System.out.println("constainsInArray");
    Object[] arr = null;
    Object obj = null;
    boolean expResult = false;
    boolean result = PersistenceUtils.constainsInArray(arr, obj);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of parseQueryString method, of class PersistenceUtils.
   */
  @Test
  public void testParseQueryString() {
    System.out.println("parseQueryString");
    String uriQuery = "";
    String[][] expResult = null;
    String[][] result = PersistenceUtils.parseQueryString(uriQuery);
    assertArrayEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
}
