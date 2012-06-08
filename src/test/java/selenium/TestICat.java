package selenium;

//Report problems: https://bmir-gforge.stanford.edu/

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
import com.thoughtworks.selenium.SeleniumException;

public class TestICat {
	private Selenium selenium;
	private Configuration config;

	@BeforeClass
	public void startSelenium() {

		try {
			config = new PropertiesConfiguration("src/main/resources/config/configuration.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		this.selenium = new DefaultSelenium("localhost", 4444, 
				config.getString("browser"), 
				config.getString("host") + ":"+ 
				config.getString("port"));
		
		this.selenium.start();
	}

	@Test
	public void openSite() throws InterruptedException {
		Thread.sleep(2000);
		try {
			selenium.open(config.getString("url"));
		} catch (Exception e) {
			Thread.sleep(50000);
			e.printStackTrace();
			throw new InterruptedException("CJ " + e.getMessage());
		}
		Thread.sleep(2000);
	}

	@Test(dependsOnMethods = { "openSite" })
	public void checkLoginWindow() throws InterruptedException {
		Thread.sleep(2000);
		String target = "//a[text()='Forgot username or password']";
		Assert.assertTrue(selenium.isElementPresent(target));
		Thread.sleep(2000);
	}

	@Test(dependsOnMethods = { "checkLoginWindow" })
	@Parameters({ "username", "password" })
	public void login(String username, String password) throws InterruptedException {
		System.out.println("Will enter user name and password");
		Thread.sleep(2000);

		// Enter user name and password
		String usernamelocator = "//input[1]";
		String passwordlocator = "//input[@type='password']"; //input[2] didn't work
		selenium.type(usernamelocator, username);
		selenium.type(passwordlocator, password);

		// press "Sign in" button
		System.out.println("Will now sign in");
		Thread.sleep(2000);
		String buttonLocator = "//button";
		selenium.click(buttonLocator);
		System.out.println("Just have clicked login");
		Thread.sleep(2000);
	}

	@Test(dependsOnMethods = { "login" })
	public void clickFeedback() {
		System.out.println("After login: Waiting 10secs for the window to load");
		String target = "//span[text()='Welcome to ICD 11 Revision']";
		while(selenium.isElementPresent("css=x-progress-text")){
			System.out.println("laden...");
		}
		//selenium.click("feedback");
	}
	
	@Test(dependsOnMethods = { "clickFeedback" })
	public void clickICDContent() throws InterruptedException {
		String target = "//span[text()='ICD Content']";
	
			Thread.sleep(10000);
	
		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);
		System.out.println("Just have clicked in on tab register " + target + " and will wait for 10secs");
		
			Thread.sleep(20000);
		
		
		target = "//span[text()='Details for   ICD Categories']";
		Assert.assertTrue(selenium.isElementPresent(target));
	
			Thread.sleep(2000);
		
	}
	
	@Test (dependsOnMethods = {"clickICDContent"})
	public void openICDTree() throws InterruptedException {
		//Now click on '+'-sign in tree to expand
		String target = "//li[@class='x-tree-node']/ul/li[2]/div/img[@class='x-tree-ec-icon x-tree-elbow-plus']";
		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);
		Thread.sleep(2000);
		
		//also open child element
		target="//span[text()=' D10-D36 Benign neoplasms']";
		Assert.assertTrue(selenium.isElementPresent(target));
		Thread.sleep(2000);
		selenium.click(target);
		Thread.sleep(4000);
		
		target="//span[text()='Details for  D10-D36 Benign neoplasms']";
		Assert.assertTrue(selenium.isElementPresent(target));
		
		Thread.sleep(2000);
	}
	
	@Test (dependsOnMethods = {"openICDTree"})
	@Parameters({"description"})
	public void addShortDescription(String description) throws InterruptedException {
		//Find and click command link "+ Add new value" under "short description" is available
		String target="//div[@class='x-column-inner']/table/tbody/tr[2]/td/a/img[@src='images/add.png']";
		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);
		Thread.sleep(2000);

		//enter sth.	x-form-textarea x-form-field 
		target="//div[@class='x-layer x-editor x-small-editor x-grid-editor']/textarea";
		selenium.type(target, description);
		target="//input[@title='A string that can be used to sort the children of a category. This is not the ICD code.']";
		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);


		//now click somewhere else in the tree
		target="//span[text()=' D00-D09 In situ neoplasms ']";
		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);

		
		//and click back to our tree
		target="//span[text()=' D10-D36 Benign neoplasms']";
		Assert.assertTrue(selenium.isElementPresent(target));
	
		selenium.click(target);
		
		
		//verify that text actually is there
		target = "//div[@class='x-grid3-row ']/table/tbody/tr/td/div[@class='x-grid3-cell-inner x-grid3-col-gwt-uid-7']";
		String result = selenium.getText(target);
		System.out.println("Result = >" + result + "<");
		selenium.doubleClick(target);
		
//		String prefix = ".x-grid3-cell-inner, .x-grid3-hd-inner { white-space:normal !important; } ";
//		target="//*[text()='" + prefix + description +"']";
		Assert.assertTrue(selenium.isElementPresent(target));
	}
	
	@Test(dependsOnMethods = {"addShortDescription"})
	@Parameters({"description"})
	public void deleteShortDescription(String description) throws InterruptedException {
		System.out.println("Will now try to delete new entry");
		
		String target="//div[@class='x-grid3-row ']/table/tbody/tr/td[3]/div/img[@src='images/delete.png']";

		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);
		Thread.sleep(5000);
		System.out.println("Waiting for popup");
		
		//confirm modal popup window
		System.out.println("confirm pop-up");

		//1. attempt: delete by clicking on it
		System.out.println("Attempt 1 to kill the popup");
		selenium.click("//*[text()='Yes']");
		Thread.sleep(2000);
	}

	@Test(dependsOnMethods={"deleteShortDescription"}) 
	@Parameters({"description"})
	public void testHistory(String description) throws InterruptedException {
		//Switch to change history tab
		System.out.println("Check that element apperas in change history");
		String target = "//span/span[text()='Change History']";
		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);
		
		//open in tree
		//Now click on '+'-sign in tree to expand
		target = "//li[@class='x-tree-node']/ul/li[2]/div/img[@class='x-tree-ec-icon x-tree-elbow-plus']";
		Assert.assertTrue(selenium.isElementPresent(target));
		selenium.click(target);
		Thread.sleep(2000);
		
		//also open child element
		target="//*[text()=' D10-D36 Benign neoplasms']";
		Assert.assertTrue(selenium.isElementPresent(target));
		Thread.sleep(2000);
		selenium.click(target);
		Thread.sleep(4000);
		
		//here the description should show up
		target="//*[text()='" + description +"']";
		//Assert.assertTrue(selenium.isElementPresent(target));
		//Thread.sleep(2000);
	}


	@AfterClass(alwaysRun = true)
	public void stopSelenium() throws InterruptedException{
		System.out.println("Going to shutdown Firefox");
		Thread.sleep(5000);
		this.selenium.stop();
	}

}
