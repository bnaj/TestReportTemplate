import Driver.Driver;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class ExampleTest1 extends Driver {

    @Test(description = "Going to site")
    public void step01() {
        System.out.println("metoda1");
       driver.get("http://www.google.pl");
        Reporter.log("report from going to site");
    }

    @Test(description = "Method 2")
    public void step02_method2(){
        System.out.println("method 2 do nothing");
       // "this is method 2"
        Reporter.log("report from method 2");
    }

    @Test(testName = "Method 3")
    public void step03() {
        System.out.println("metoda3");
        boolean abc = true;
        if (abc==true) {
            Reporter.log("report if true");
        }else{
            Reporter.log("report else");
        }
    }
    @Test(description = "true or false")
    public void step04(){
        Assert.assertFalse(true);
        Reporter.log("true");
    }
}