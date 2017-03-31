/*
 * Property of Instanetwork
 *
 * Algorithm using Phantomjs to like photos on a given list
 * of hashtags with Instagram
 */
package com.mycompany.instagramautolike;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

/**
 * Instanetwork Version 19.0
 */
public class InstaLikeNonGUI {
    PhantomJSDriver driver;  //Ghostdriver
    private String ip = "127.0.0.1";
    private String port = "8080";
    private String proxyUser = "none";
    private String proxyPass = "none";
    private final String instagramLink = "https://www.instagram.com/accounts/login/";
    List<String> likeUsers;
    private final List<String> list;                      //List of all Hashtags
    private final long hourTime = 3600000;               //1 hour in miliseconds
    private String username = "";                         //Username used to login
    private String password = "";                         //Password used to login
    private String userEmail = "";                        //email used for first time authorization
    private int hashtagCount = 0;                         //Count of what hashtag is current
    private int hashtagLikeSleep = 0;                     //Sleep after like in hashtag (MiliSeconds)
    private int betweenHashtagSleep = 0;                  //sleep between hashtags (MiliSeconds)
    private int hashtagLikeLimit = 0;                     //Number of likes per hashtag
    private int hashtagsBetweenSitesLimit = 0;            //How many hashtags cycled through before site switch
    private int instaTotalLikes = 0;                      //Counter of total likes
    private int instagramCounter = 0;                     //Instagram likes/hour Counter
    private int insMaxLikes = 0;                          //X likes per hour for Instagram
    private int highBetweenLikesSleepThreshold = 0;       //high limit to randomize to for sleep between likes
    private int highBetweenHashtagsSleepThreshold = 0;    //high limit to randomize to for sleep between likes
    private long timeOffsetInstagramTotal = 0;            //time in miliseconds used to keep track of likes/hour for All access points
    private boolean possiblePasswordReset = false;        //Possible Reset - Flag for further investigation
    private final int photoPerHourResetThres = 0;         //If photos liked per hour are less than thresh, flag account
    private final int spamFilterFollowerCount = 125;      //Count of followers per user for spam detection
    private final int spamFilterFollowingCount = 100;     //Count of following per user for spam detection
    private final String followerCountXpath = "//li[2]/a/span[contains(@class,'_bkw5z')]";
    private final String followingCountXpath = "//li[3]/a/span[contains(@class,'_bkw5z')]";
    private final int tagLimit = 20;                      //tag limit that can be attached to a photo
    private Actions actions;                              //action library to interact with driver
    private List<WebElement> pictures;

    //Constructor - Saves all variables sent in from Jar
    public InstaLikeNonGUI(String iProt, String iport, String pUser, String pPass, String user, String pass, String email, int insMax, int tagLikeSleep, int betweenSleep, int tagLikeLimit, int tagBetweenSite, List<String> l) {
        ip = iProt;
        port = iport;
        proxyUser = pUser;
        proxyPass = pPass;
        username = user;
        password = pass;
        userEmail = email;
        insMaxLikes = insMax;
        hashtagLikeSleep = tagLikeSleep;
        betweenHashtagSleep = betweenSleep;
        hashtagLikeLimit = tagLikeLimit;
        hashtagsBetweenSitesLimit = tagBetweenSite;
        highBetweenLikesSleepThreshold = hashtagLikeSleep + 10;
        highBetweenHashtagsSleepThreshold = betweenHashtagSleep + 15;
        list = l;
        likeUsers = new ArrayList<>();
        Date date = new Date();
        System.out.println("Start Time " + date.toString());
        likeAlgo();
        driver.quit();
    }

    /*webdriver that uses ghostdriver from phantomjs to scrape pages
     headlessly *Custom Settings*/
    private void loadLightWeightDriverCustom(boolean noPic) {
        File PHANTOMJS_EXE = new File("//home/innwadmin/phantomjs/bin/phantomjs");  // Linux File
        // File PHANTOMJS_EXE = new File("C:/Users/stephen/Documents/Instanetwork/Instagram AutoLike/InstagramAutoLike/phantomjs-2.0.0-windows/bin/phantomjs.exe"); // Windows File
         // File PHANTOMJS_EXE = new File("/Users/stephen.hyde/repositories/phantomjs-2.1.1-macosx/bin/phantomjs");  // Linux File
        ArrayList<String> cliArgsCap = new ArrayList<>();
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("phantomjs.binary.path",
                PHANTOMJS_EXE.getAbsolutePath());
        caps.setJavascriptEnabled(true);
        cliArgsCap.add("--proxy=" + ip + ":" + port); //8080 for tinyproxy
        if (!proxyUser.equalsIgnoreCase("none")) {
             cliArgsCap.add("--proxy-auth=" + proxyUser + ":" + proxyPass);
        }
        cliArgsCap.add("--max-disk-cache-size=0");
        cliArgsCap.add("--disk-cache=false");
        cliArgsCap.add("--webdriver-loglevel=NONE");
        if (noPic) {
            cliArgsCap.add("--load-images=false");
        }
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
        driver = new PhantomJSDriver(caps);
        driver.manage().window().maximize();
        actions = new Actions(driver);
    }

    /*Main Algorithm to rotate between all access points specified
     on Interface and call their respective like modules*/
    private void likeAlgo() {
        while (!Thread.currentThread().isInterrupted()) {
            checkHourTime();
            if (insMaxLikes > 0 && instagramCounter < insMaxLikes) {
                System.out.println("Initialize Instagram ");
                InitializeLikeFunction(instagramLink, false);
            }
        }
    }

    /*Initialize Like Function by initializing phantomjs driver,
     call like method and increment hashtag*/
    private void InitializeLikeFunction(String link, boolean pic) {
        loadLightWeightDriverCustom(pic);
        try {
            if (login(link, username, password)) {
                for (int i = 0; i < hashtagsBetweenSitesLimit; i++) {
                    checkHourTime();
                    if (instagramCounter >= insMaxLikes) {
                        break;
                    }
                    likeHashtagInstagram(list.get(hashtagCount));
                    hashtagCount = incrementHashCount(hashtagCount);
                    sleepBetweenLikesHashtags(highBetweenHashtagsSleepThreshold, betweenHashtagSleep);
                }
            }
            driver.quit();
        } catch (org.openqa.selenium.remote.UnreachableBrowserException e) {
            System.out.println("Remote Browser Unreachable Exception!");
        }
    }

    //Hash Counter to keep track of position in list
    private int incrementHashCount(int hashCount) {
        int hCount = hashCount + 1;
        if (hCount >= list.size()) {
            hCount = 0;
        }
        return hCount;
    }

    //Login to an access point (Called from loginAccessPoints)
    private boolean login(String link, String username, String password) throws org.openqa.selenium.remote.UnreachableBrowserException {
        driver.get(link);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        List<WebElement> user = driver.findElements(By.xpath("//input[@name='username']"));
        List<WebElement> pass = driver.findElements(By.xpath("//input[@name='password']"));
        List<WebElement> login = driver.findElements(By.xpath("//span[1]/button[contains(@class, '_ah57t')]"));
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        if (user.size() > 0 && pass.size() > 0 && login.size() > 0) {
            user.get(0).sendKeys(username);
            pass.get(0).sendKeys(password);
            sleepExtraPageLoad();
            login.get(0).click();
            sleepExtraPageLoad();
        } else {
            System.out.println("Unable to login!");
            return false;
        }

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        List<WebElement> name = driver.findElements(By.xpath("//a[contains(@class, '_soakw')]"));
        Boolean result = name.size() > 0;

        System.out.println("User Login was " + result);

        return result;

    }

    private void likeHashtagInstagram(String hashtag) throws org.openqa.selenium.remote.UnreachableBrowserException {
        String url = "https://www.instagram.com/explore/tags/".concat(hashtag);
        int count = 0;
        int loopCount = 0;
        boolean firstScroll = false;
        boolean test = true;
        try {
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.get(url);
            while (count < hashtagLikeLimit && instagramCounter < insMaxLikes) {
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                pictures = driver.findElements(By.xpath("//article/div/div/div[position()>=" + loopCount * 5 + "]/a[@class='_8mlbc _vbtk2 _t5r8b']"));
                for (int i = 0; i < pictures.size(); i++) {
                    //Expand picture

                    pictures.get(i).click();

                    //verify picture not liked
                    By likeButton = By.xpath("//a/span[@class='_soakw coreSpriteHeartOpen']");
                    List<WebElement> like = driver.findElements(likeButton);
                    if (like.isEmpty()) {
                        System.out.println("Picture was liked");
                        closePictureWindow(loopCount);
                        continue;
                    }

                    // Verify tag size is within threshold
                    List<WebElement> tags = driver.findElements(By.xpath("//article/div/ul[1]/li[1]/h1[1]/span[1]/a"));
                    if (tags.size() >= tagLimit) {
                        System.out.println("Tag Limit Too High " + tags.size());
                        closePictureWindow(loopCount);
                        continue;
                    }

                    //verify username present
                    List<WebElement> picUsername = driver.findElements(By.xpath("//article/header/div[@class='_f95g7']/a[1]"));
                    if (picUsername.isEmpty()) {
                        System.out.println("Unable to read username");
                        closePictureWindow(loopCount);
                        continue;
                    }

                    //verify user has not been liked already
                    String name = picUsername.get(0).getText();
                    if (userExist(name)) {
                        System.out.println("Already like a photo from user");
                        closePictureWindow(loopCount);
                        continue;
                    }

                    //push user to array list
                    likeUsers.add(name);

                    //Verify follower count is within threshold
                    if (isProfileSpam(name)) {
                        System.out.println("Profile seems like spam " + name);
                        closePictureWindow(loopCount);
                        continue;
                    }

                    //like photo and if stale retry
                    Boolean liked = retryingFindClick(likeButton, like.get(0));

                    //Verify photo was liked
                    if (!liked) {
                        System.out.println("Error while liking photo " + name);
                        closePictureWindow(loopCount);
                        continue;
                    }

                    count = afterLikeIncrement(count);
                    loopCount = 0;
                    closePictureWindow(loopCount);
                    //Break from loop if photo threshold met
                    if (count >= (hashtagLikeLimit) || instagramCounter >= insMaxLikes) {
                        break;
                    }
                    sleepBetweenLikesHashtags(highBetweenLikesSleepThreshold, hashtagLikeSleep);
                }

                //Exit method if count greater then hashtag limit
                if (count >= (hashtagLikeLimit)) {
                    break;
                }

                // Verify if we need to load more photos via button or scroll and if we loaded too many photos
                if (loopCount == 5) {
                    System.out.println("Too Many page searches with no likes on Instagram");
                    break;
                } else {
                    loopCount += 1;
                    if (firstScroll) {
                        actions.sendKeys(Keys.chord(Keys.CONTROL, Keys.END)).perform();
                        continue;
                    }
                    List<WebElement> nextPage = driver.findElements(By.linkText("Load more"));
                    if (nextPage.isEmpty()) {
                        System.out.println("No next page on Instagram");
                        break;
                    }
                    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
                    nextPage.get(0).click();
                    firstScroll = true;
                }
            }
            Date date = new Date();
            System.out.println("Site: Instagram Number of Likes: " + instaTotalLikes + " Hashtag: " + hashtag + " System Date " + date.toString());
        } catch (org.openqa.selenium.WebDriverException e) {
            System.out.println("Exception thrown in Instagram" + e);
        }
    }

    //Creates a new page that goes to the profile of a given user and checks there follower count
    private boolean isProfileSpam(String u) throws org.openqa.selenium.remote.UnreachableBrowserException {
        boolean followingCountSpam = false;
        boolean followerCountSpam = false;

        if (!u.isEmpty()) {
            String mainWindow = driver.getWindowHandle();
            driver.manage().timeouts().implicitlyWait(25, TimeUnit.SECONDS);
            String url = "https://www.instagram.com/" + u;
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            ((JavascriptExecutor) driver).executeScript("window.open('" + url + "', 'instagram')");
            if (driver.getWindowHandles().size() > 1) {
                String profileWindow = (String) driver.getWindowHandles().toArray()[1];
                driver.switchTo().window(profileWindow);
                followerCountSpam = isFollowerCountSpam();
                followingCountSpam = isFollowingCountSpam();
                driver.close();
                driver.switchTo().window(mainWindow);
            }
        }
        return (followingCountSpam || followerCountSpam);
    }

    private void closePictureWindow(int loopCount) throws org.openqa.selenium.remote.UnreachableBrowserException {

        List<WebElement> exit = driver.findElements(By.xpath("//button[@class='_3eajp']"));
        if (!exit.isEmpty()) {
            exit.get(0).click();
        }

        pictures = driver.findElements(By.xpath("//article/div/div/div[position()>=" + loopCount * 5 + "]/a[@class='_8mlbc _vbtk2 _t5r8b']"));
    }

    private boolean isFollowerCountSpam() {
        String pathCount;
        List<WebElement> count = driver.findElements(By.xpath(followerCountXpath));
        if (!count.isEmpty()) {
            pathCount = count.get(0).getAttribute("title").replace(",", "");
            if (IsInt_ByRegex(pathCount)) {
                if (Integer.parseInt(pathCount) <= spamFilterFollowerCount) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFollowingCountSpam() {
        String pathCount;
        List<WebElement> count = driver.findElements(By.xpath(followingCountXpath));
        if (!count.isEmpty()) {
            pathCount = count.get(0).getText().replace(",", "");
            if (IsInt_ByRegex(pathCount)) {
                if (Integer.parseInt(pathCount) <= spamFilterFollowingCount) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean retryingFindClick(By by, WebElement button) throws org.openqa.selenium.remote.UnreachableBrowserException {
        boolean result = false;
        int attempts = 0;
        while (attempts < 3) {
            try {
                button.click();
                result = true;
                break;
            } catch (org.openqa.selenium.WebDriverException e) {
                button = driver.findElement(by);
                System.out.println("Stale Element!!! ");
            }
            attempts++;
        }
        return result;
    }

    //increment counters after like for given accesspoint
    private int afterLikeIncrement(int count) {
        IncrementCounters();
        checkHourTime();
        count = count + 1;
        return count;
    }

    /*Change time if been over an hour from previous time saved for Instagram Total / Access Points
     reset all counters (Total & access points)*/
    private void checkHourTime() {
        Date date = new Date();
        long timeLike = System.currentTimeMillis();
        if (timeLike - timeOffsetInstagramTotal > hourTime) {
            System.out.println("Like period Ended:" + date.toString() + " Total Likes " + instagramCounter);
            if (instagramCounter < photoPerHourResetThres && !possiblePasswordReset && timeOffsetInstagramTotal != 0) {
                flagAccountForReset();
            }
            instagramCounter = 0;
            timeOffsetInstagramTotal = timeLike;
        } else if (instagramCounter >= insMaxLikes) {
            System.out.println("Sleep Program total");
            SleepProgram(timeLike);
            checkHourTime();
        }
    }

    //Increase Access Point and call initialize clock on program inception
    private void IncrementCounters() {
        InitializeClock();
        instaTotalLikes += 1;
        instagramCounter += 1;
    }

    //sleep between likes or hashtags
    private void sleepBetweenLikesHashtags(int high, int sleep) {
        Random rand = new Random();
        int randomNum = (rand.nextInt((high - sleep) + 1) + sleep) * 1000;
        try {
            Thread.sleep(randomNum);
        } catch (InterruptedException e) {
            System.out.println("Interrupted Exception on sleepBetweenLikesHashtags");
        }
    }

    //Sleep prior to webstagram login
    private void sleepExtraPageLoad() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Interrupted Exception on sleepDuringWebstaLogin");
        }
    }

    //when instagram exceeded its like/hour threshold or all access points reach like/hour threshold, sleep program
    private void SleepProgram(long curTime) {
        Date date = new Date();
        if (instagramCounter >= insMaxLikes) {
            try {
                System.out.println("Instagram Threshold Met, Sleeping; Time Remaining: " + (hourTime - (curTime - timeOffsetInstagramTotal)) + " " + date);
                Thread.sleep(hourTime - (curTime - timeOffsetInstagramTotal));
            } catch (InterruptedException e) {
                System.out.println("Error while sleeping");
            }
        }
    }

    //Limited activity check which writes to a file with username as file name and email in contents
    private void flagAccountForReset() {
        try {
            File file = new File("/home/innwadmin/instanetwork/reset/" + username + ".txt");
            if (!file.exists()) {
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(userEmail);
                bw.close();
            }
            possiblePasswordReset = true;
        } catch (IOException e) {
            System.out.println("Flag for reset failed " + e);
        }
    }

    //Upon liking first photo, initialize the time stamp for the associated excess point
    private void InitializeClock() {
        Date date = new Date();
        long time = System.currentTimeMillis();
        if (instaTotalLikes == 0) {
            timeOffsetInstagramTotal = time;
            System.out.println("Instagram Time Inititalized " + date.toString());
        }
    }

    //reset driver
    private void resetDriver() {
        driver.quit();
        loadLightWeightDriverCustom(false);
    }

    //is string an integer by regex
    private boolean IsInt_ByRegex(String str) {
        return str.matches("^-?\\d+$");
    }

    //checks if user had a photo liked
    private boolean userExist(String u) {
        if (!likeUsers.isEmpty()) {
            for (String user  : likeUsers) {
                if (user.equalsIgnoreCase(u)) {
                    return true;
                }
            }
        }
        return false;
    }
}