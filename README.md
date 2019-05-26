# Test Report Template
Hello all!
If you want see how implement new report style in TestNg to look like below.
I'  prepepared some examples of test method to show how its look.
![ScreenShot](https://repository-images.githubusercontent.com/188700511/e3091000-7fde-11e9-81d9-998116a4f183)


This project is example of custom automation test reports. After generation report takes one xml files.
In this project methods from TestNg listener was override.
Project implement screen capture functionality from Selenium library. 

### Screen capture methods
Screen capture methods is activated when test method is 
end with failed status. **_TakesScreenshot_** save image in 
String base64 in method **captureScreen**. 
Next **captureScreen** is called in Reporter.log line in method **_onTestFailure_**.

```java  
private String captureScreen(WebDriver driver) {
TakesScreenshot newScreen = (TakesScreenshot) driver;
String scnShot = newScreen.getScreenshotAs(OutputType.BASE64);
eturn scnShot;
}
            
public void onTestFailure(ITestResult result) {
getStatusToList(result);
Object currentClass = result.getInstance();
WebDriver driver = ((Driver) currentClass).driver;
Reporter.log(  "Click right button and open image in new tab" + "<br><img src=\"data:image/png;base64," +
captureScreen(driver) + "\"width=\"200px\" height=\"200px\">");
}
```
Pay attention on type of WebDriver. In my code is Driver, because in my test classes I use driver what is extends of Driver class.             
### Report table
Default when you open file with your report, you see only tabs withaut table. You can open table by clicking on the tab with name of test.
Listener fills cells in table using:
Name of step methods to fills name of methods in status cell like:
```...public void step02_method2()...```
Description in @Test adnotation to fills Test steps cell, like:
```...@Test(description = "Going to site")...```
Reporter.log  to fills Test resoult cell, like:
```...Reporter.log("report from going to site");...```
```java 
            @Test(description = "Going to site")           <-test description Test steps cell 
            public void step01() {                         <-name of test step in status cell 
                System.out.println("metoda1");
                driver.get("http://www.google.pl");
                Reporter.log("report from going to site"); <-test resoult cell
                }
```
### Navbar
Report contains navbar with drop down list which is to help you navigate the test. When user scroll down document than navbar follows him.
### Report output
After test new report file is generated in _/src/main/_ 
You can change this in **_onExecutionFinish_** method in line 222.
```java 
    File newHtmlFilePath = new File("src/main/new.html");
            File newHtmlFile = new File(newHtmlFilePath.getAbsolutePath());
            Files.write(Paths.get(newHtmlFile.getAbsolutePath()), htmlString.getBytes());
```
### Configure pom.xml
To use this Listener you must add Listener.class to pom.xml configuration in maven surfire plugin, like below:
```
<plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.14.1</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>src/main/java/testngXml/suite.xml</suiteXmlFile>
                    </suiteXmlFiles>
                    <properties>
                        <property>
                            <name>usedefaultlisteners</name>
                            <value>false</value>
                        </property>
                        <property>
                            <name>listener</name>
                            <value>reportGenerator.Listener</value>
                        </property>
                    </properties>
                </configuration>
            </plugin>
 ```
## Classes used in project
In package Driver: _Driver_ class, where i generate webdriver.
In Java folder:
_ExampleTest1_ and _ExampleTest2_ where I extend _Driver.class_ and write test steps
In packade reportGenerator: _Listener_ where I implements **_IExecutionListener, ITestListener, ISuiteListener, IInvokedMethodListener_**
# Linux driver for selenium
In resources folder you can find driver used in this project to generate report.
Because I use friendly OS like LINUX i have there linux driver. If you use other OS you must download driver for your os and do some change in Driver class.

### thanks for your interest and reading this file bay.
