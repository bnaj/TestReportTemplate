package reportGenerator;

import Driver.Driver;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;

/**
 * @Author JB.
 */
public class Listener implements IExecutionListener, ITestListener, ISuiteListener, IInvokedMethodListener {

    private int numberofAllSuiteWithTest = 0;
    private int numberOfAllSteps = 0;
    private int numberOfAllStepsFailed = 0;
    private int numberOfAllStepsPassed = 0;
    private int numberOfAllStepsSkiped = 0;

    private ArrayList<String> listWithFailedScenarioToProcentCompute = new ArrayList<>();
    private ArrayList<String> listWithFailedScenario = new ArrayList<>();
    private ArrayList<String> listWithPassedScenario = new ArrayList<>();
    private ArrayList<String> listWithSkippedScenario = new ArrayList<>();
    private ArrayList<ITestResult> resultList = new ArrayList<>();
    private ArrayList<ISuite> listOfSuit = new ArrayList<>();

    /**
     * This is the method that is run at the beginning and generate list of all suite.
     *
     **/
    @Override
    public void onStart(ISuite result) {
        listOfSuit.add(result);
    }

    @Override
    public void onFinish(ISuite result) {
    }

    /**
     * This is the method that is run at the beginning of suite and generate list of all suite with tests.
     *
     **/
    public void onStart(ITestContext result) {
        numberofAllSuiteWithTest++;
    }

    public void onFinish(ITestContext result) {
    }

    /**
     * This is the method used to capture the screen.
     * Method use selenium interface to catch screen.
     * @param driver you must provide webdriver to use TakesScreenshot.
     * @return jpg and save it in base64.
     */
    private String captureScreen(WebDriver driver) {
        TakesScreenshot newScreen = (TakesScreenshot) driver;
        String scnShot = newScreen.getScreenshotAs(OutputType.BASE64);
        return scnShot;
    }

    /**
     * Method used to collect success status on result list.
     * @param result from ITestResult class.
     */
    public void onTestSuccess(ITestResult result) {
        getStatusToList(result);
    }


    /**
     * This is the method used to capture the screen when test is failed.
     * Method add result to result list.
     * Result object is used to take instance of test class and get object from Driver class.
     * If your driver is generated in other class Change class name (Driver) in this place
     * " WebDriver driver = ((Driver) currentClass).driver;" to name of class when you generate WebDriver.
     * @param result from ITestResult class.
     */
    public void onTestFailure(ITestResult result) {
        getStatusToList(result);
        Object currentClass = result.getInstance();
        WebDriver driver = ((Driver) currentClass).driver;
        try {
            Reporter.log("Click right button and open image in new tab" + "<br><img src=\"data:image/png;base64," +
                    captureScreen(driver) + "\"width=\"200px\" height=\"200px\">");
        }catch (Exception e){
            Reporter.log("Driver throw exception test is aborted. " + e.getClass());
            driver.quit();
        }
    }

    public void onTestStart(ITestResult result) {
        }

    /**
     * Method used to collect skipped status on result list.
     * @param result from ITestResult class.
     */
    public void onTestSkipped(ITestResult result) {
        getStatusToList(result);
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
    }

    /**
     * Method used to collect result.
     * @param result from ITestResult class.
     */
    private void getStatusToList(ITestResult result) {
        resultList.add(result);
    }

    /**
     * Method used to check status and ad String status to result.
     * @param result to check.
     * @return String status.
     */
    private String printTestResults(ITestResult result) {
        String status = null;
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                status = "Pass";
                break;
            case ITestResult.FAILURE:
                status = "Failed";
                break;
            case ITestResult.SKIP:
                status = "Skipped";
        }
        return status;
    }

    public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1) {
    }

    public void afterInvocation(IInvokedMethod arg0, ITestResult arg1) {
    }

    /**
     * Method used to take description from test method.
     * @param result from which the description will be downloaded.
     * @return description in String. When test method does not have description return "No info found"
     */
    private String stepDescription(ITestResult result) {
        String output = "";
        if ((result.getMethod().getDescription() != null) && (result.getMethod().getDescription().length() >= 0)) {
            output = result.getMethod().getDescription();

        } else {
            output = "No info found";
        }
        return output;
    }

    /**
     * Method used to take report from test method.
     * @param result from which the description will be downloaded.
     * @return description in String. When test method does not have description return "No info found"
     * Report is not returned if test is failed.
     */
    private String stepOutput(ITestResult result) {
        if (CollectionUtils.isNotEmpty(Reporter.getOutput(result))) {
            return StringUtils.defaultIfEmpty(Reporter.getOutput(result).get(0), "No info found");
        } else {
            return "No info found";
        }
    }

    public void onExecutionStart() {
    }

    /**
     * Method used on the end of all test and test suit.
     * This is method with many line of code. Method generate navbar with dropdown list menu and add all to html template.
     * @table is variable where is called generateTable method.
     * @htmlTemplateFilePath this is path to html report template.
     * @newHtmlFilePath this is path where is placed new report.
     * @dropdownMenuWitScenarioLoad method that providing elements to dropdown menus.
     * To add Strings with html to report file is used replace.
     * all tags with $ like $table have own place in html template and there will be placed.
     */
    public void onExecutionFinish() {
        String table = generateTable();
        String failedDropDown = dropdownMenuWitScenarioLoad(eliminateDuplicate(listWithFailedScenario));
        String passDropDown = dropdownMenuWitScenarioLoad(passScenarioList(eliminateDuplicate
                (listWithPassedScenario),eliminateDuplicate(listWithFailedScenario)));
        String skippedDropDown = dropdownMenuWitScenarioLoad(eliminateDuplicate(listWithSkippedScenario));
        File htmlTemplateFilePath = new File("src/main/java/reportGenerator/htmlTemplate/exampleTemplate.html");
        try {
            String htmlString = new String(Files.readAllBytes(Paths.get(htmlTemplateFilePath.getAbsolutePath())));
            htmlString = htmlString.replace("$os", System.getProperty("os.name") + "<br>" + "OS version: "
                    + System.getProperty("os.version") + "<br>" + "Architecture: " +System.getProperty("os.arch"));
            htmlString = htmlString.replace("$dateTime", getDate());
            htmlString = htmlString.replace("$table", table);
            htmlString = htmlString.replace("$FailedScenario", failedDropDown);
            htmlString = htmlString.replace("$passDropDown", passDropDown);
            htmlString = htmlString.replace("$skippedDropDown", skippedDropDown);
            htmlString = htmlString.replace("$percentFailScenario",
                    computePercent(numberofAllSuiteWithTest, numberOfFailedScenario(listWithFailedScenarioToProcentCompute)));
            htmlString = htmlString.replace("$percentPassScenario",
                    computePercent(numberofAllSuiteWithTest, passScenarioList(eliminateDuplicate
                            (listWithPassedScenario),eliminateDuplicate(listWithFailedScenario)).size()));
            htmlString = htmlString.replace("$percentPassSteps",
                    computePercent(numberOfAllSteps, numberOfAllStepsPassed));
            htmlString = htmlString.replace("$percentFailedSteps",
                    computePercent(numberOfAllSteps, numberOfAllStepsFailed));
            htmlString = htmlString.replace("$percentSkippedSteps",
                    computePercent(numberOfAllStepsSkiped, numberOfAllStepsPassed));
            File newHtmlFilePath = new File(getDate()+"newReport.html");
            File newHtmlFile = new File(newHtmlFilePath.getAbsolutePath());
            Files.write(Paths.get(newHtmlFile.getAbsolutePath()), htmlString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("HTML File not found!!!");
        }
    }

    /**
     * Method used to generate html table with test result, steps etc...
     * Element of table is added to variable with StringBuilder and append.
     * @returnString variable with html code of table.
     */
    private String generateTable() {
        String linkToStatImage = "src/main/java/reportGenerator/image/";
        StringBuilder tableWithTestDataStyle = new StringBuilder();
        for (ISuite suit : listOfSuit) {
            if (suit.getXmlSuite().getTests().size() > 0) {
                int i = 0;
                while (i < suit.getXmlSuite().getTests().size()) {
                    tableWithTestDataStyle.append("<table id=\"testResoultTables\" class=\"bodyHidden\">\n" +
                            "\n" +
                            "    <caption onclick=\"hide(event)\">\n" + "<a name=\"" + testScenarioName(suit, i) +
                            "\"></a>" + testScenarioName(suit, i) + "</caption>\n" +
                            "       <tr>\n" +
                            "        <th>Status</th>\n" +
                            "    <th>Test steps</th>\n" +
                            "    <th>Test resoult</th></tr>");
                    for (ITestResult iTestResult : resultList) {
                        if (suit.getXmlSuite().getTests().get(i).getName().equals(iTestResult.getTestContext().getName())) {
                            numberOfAllSteps++;

                            tableWithTestDataStyle.append(" <tr>\n");
                            if (printTestResults(iTestResult).equals("Failed")) {
                                numberOfAllStepsFailed++;
                                listWithFailedScenarioToProcentCompute.add(suit.getName() + iTestResult.getTestContext().getName());
                                listWithFailedScenario.add(iTestResult.getTestContext().getName());
                                tableWithTestDataStyle.append("<td>" + statusImage(new File(linkToStatImage +
                                        "nok.png")) + "  " + stepName(iTestResult) + "</td>\n");
                            } else if (printTestResults(iTestResult).equals("Skipped")) {
                                numberOfAllStepsSkiped++;
                                listWithSkippedScenario.add(iTestResult.getTestContext().getName());
                                tableWithTestDataStyle.append("<td>" + statusImage(new File(linkToStatImage +
                                        "ast.png")) + "  " + stepName(iTestResult) + "</td>\n");
                            } else if (printTestResults(iTestResult).equals("Pass")) {
                                numberOfAllStepsPassed++;
                                listWithPassedScenario.add(iTestResult.getTestContext().getName());
                                tableWithTestDataStyle.append("<td>" + statusImage(new File(linkToStatImage +
                                        "ok.png")) + "  " + stepName(iTestResult) + "</td>\n");
                            }
                            tableWithTestDataStyle.append("<td>" + stepDescription(iTestResult) + "</td>\n" +
                                    "    <td>" + stepOutput(iTestResult) + "</td></tr>");
                        }
                    }
                    i++;
                }
            }
        }
        return tableWithTestDataStyle.toString();
    }

    /**
     * Method used to eliminate result with fail status from list with result with pass status.
     * @param pass ArrayList with pass status.
     * @param fail ArrayList with fail status.
     * @return ArrayList with pass scenario
     */
    private ArrayList<String> passScenarioList(ArrayList<String> pass, ArrayList<String> fail) {
        for (String d : fail) {
            pass.remove(d);
        }
        return pass;
    }

    /**
     * Method what build inside of dropdown menus with links.
     * @param st list with tests scenario to link.
     * @return dropdown menu with link html code.
     */
    public  String dropdownMenuWitScenarioLoad(ArrayList<String> st) {
        StringBuilder dropdownMenu1 = new StringBuilder();
        for (String e : st) {
            dropdownMenu1.append("<li><a href=\"#"+e+"\">"+e+"</a></li>");
        }
        return dropdownMenu1.toString();
    }

    /**
     * Method used to get testScenarioName.
     * @param suit testScenario element.
     * @param i iteration counter.
     * @return name of test scenario in string.
     */
    private String testScenarioName(ISuite suit, int i) {
        return suit.getXmlSuite().getTests().get(i).getName();
    }

    /**
     * Method used to get test step name.
     * @param result of test.
     * @return test step name in string.
     */
    private String stepName(ITestResult result) {
        return result.getMethod().getMethodName();
    }

    /**
     * Method what generate data and format it yyyy.MM.dd HH.mm.ss.
     * On linux system can be : like time separator.
     * @return date.
     */
    private String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Method eliminate duplicate element on list.
     * @param duplicate list to eliminate duplication.
     * @return array list without duplication.
     */
    private ArrayList<String> eliminateDuplicate(ArrayList<String> duplicate){
        return new ArrayList<>(new HashSet<>(duplicate));
    }

    /**
     * Method used to provide number of failed scenarios.
     * @param duplicate list of duplicated failed scenario.
     * @return number of failed scenario in int.
     */
    private int numberOfFailedScenario(ArrayList<String> duplicate) {
        return eliminateDuplicate(duplicate).size();
    }

    /**
     * Method used to compute percent.
     * @param allScnario list.
     * @param listb list.
     * @return computed percent.
     */
    private String computePercent(int allScnario, int listb) {
        try {
            int percent = (listb / allScnario*100);
            return String.valueOf(Math.round((float)listb / (float)allScnario*100) + "%");
        }catch (ArithmeticException e){
            return "0%";
        }
    }

    /**
     * Method used to provide image from file to base64 in format 40px\40px.
     * @param filePath path to image.
     * @return image in String base64.
     */
    private String statusImage(File filePath) {
        Path path = Paths.get(filePath.getAbsolutePath());
        byte[] fileContent = new byte[0];
        try {
            fileContent = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            String file = String.valueOf(e.fillInStackTrace());
            System.out.println("File: " +
                    file.replace("java.io.FileNotFoundException:", "").trim() + " not found!");
        }
        String encodedString = Base64.getEncoder().encodeToString(fileContent);
        return "<img src=\"data:image/png;base64," + encodedString + "\" width=\"40px\" height=\"40px\">";
    }
}