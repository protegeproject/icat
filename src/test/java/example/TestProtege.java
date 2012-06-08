package example;

import junit.framework.Assert;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;


public class TestProtege {

private Selenium selenium;
private Configuration config;

  @BeforeClass
  public void startSelenium() {
      
      try {
          config = new PropertiesConfiguration("src/main/resources/config/configuration.properties");
    } catch (ConfigurationException e) {
        e.printStackTrace();
    }
    this.selenium = new DefaultSelenium("localhost", 4444, config.getString("browser"),
        config.getString("host")+":"+config.getString("port"));
    this.selenium.start();
  }

  @Test 
  public void openSite() throws InterruptedException 
  {
	    Thread.sleep(2000);
	    try {
			selenium.open(config.getString("url"));
			} catch (Exception e) {
				Thread.sleep(50000);
				e.printStackTrace();
				throw new InterruptedException("CJ " + e.getMessage());
			}
	    //selenium.open("/WebProtege.html?gwt.codesvr=127.0.0.1:9997");
	    Thread.sleep(2000);
  }
  
  @Test (dependsOnMethods = {"openSite"})
  @Parameters({ "ontologie" })
  public void clickOntologie(String ontologie) throws InterruptedException 
  {
	 selenium.click("link="+ontologie);
	 Thread.sleep(2000);
  }
  
  @Test (dependsOnMethods = {"clickOntologie"})
  public void clickFeedback() 
  {
	  selenium.click("feedback");
  }
  
  @Test (dependsOnMethods = {"clickOntologie"})
  public void clickProperties() 
  {
	  selenium.click("link=Properties");
  }
  
  @Test (dependsOnMethods = {"clickOntologie"})
  public void clickMetaData() 
  {
	  Assert.assertTrue(selenium.isElementPresent("link=Metadata"));
	  selenium.click("link=Metadata");
	  
  }
   
  @Test (dependsOnMethods = {"clickOntologie"})
  public void clickSWRL() 
  {
	  Assert.assertTrue(selenium.isElementPresent("link=SWRL"));
	  selenium.click("link=SWRL");
	  
  }
  
  @Test (dependsOnMethods = {"clickSWRL"})
  public void clickBack()  {
	  selenium.click("link=My WebProtege");
  }
 
  
  @AfterClass(alwaysRun = true)
  public void stopSelenium() {
    this.selenium.stop();
  }

}

