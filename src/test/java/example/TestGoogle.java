package example;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;


public class TestGoogle {

private Selenium selenium;

/*
  @BeforeClass
  public void startSelenium() {
    this.selenium = new DefaultSelenium("localhost", 4444, "*firefox",
        "http://www.google.de");
    this.selenium.start();
   
    
  }

  @Test 
  public void testSequence() throws Exception {
    selenium.open("/");
    selenium.type("q", "HWTG KONSTANZ");
    selenium.click("btnG");
    selenium.waitForPageToLoad("3000");
    assertTrue(selenium.isTextPresent("HTWG"));
    
  }
  
  @AfterClass(alwaysRun = true)
  public void stopSelenium() {
    this.selenium.stop();
  }
*/
}

