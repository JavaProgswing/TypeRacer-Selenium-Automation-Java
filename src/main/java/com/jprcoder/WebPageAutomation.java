package com.jprcoder;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v127.emulation.Emulation;
import org.openqa.selenium.devtools.v127.emulation.model.ScreenOrientation;
import org.openqa.selenium.devtools.v127.emulation.model.UserAgentBrandVersion;
import org.openqa.selenium.devtools.v127.emulation.model.UserAgentMetadata;
import org.openqa.selenium.devtools.v127.network.Network;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.jprcoder.SpeechToText.createAndShowGUI;

public class WebPageAutomation {
    public static final Logger logger = LoggerFactory.getLogger(WebPageAutomation.class);
    private WebElement challengeTextArea;

    public static void main(String[] args) {
        WebPageAutomation ob = new WebPageAutomation();
        logger.info("Testing Speech To Text UI");
        SpeechToText STT = createAndShowGUI(ob);
        while (!STT.getTestStatus()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        logger.info("Testing completed!");
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe");
        options.addArguments("--disable-gpu");
        options.addArguments("--touch-events=enabled");


        ChromeDriver driver = new ChromeDriver(options);

        DevTools devTools = driver.getDevTools();
        devTools.createSession();

        try {
            devTools.send(Emulation.setDeviceMetricsOverride(
                    1024, 648, 1.0, false,
                    Optional.of(1), Optional.of(1920), Optional.of(1080),
                    Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(new ScreenOrientation(ScreenOrientation.Type.LANDSCAPEPRIMARY, 0)), Optional.empty(), Optional.empty(),
                    Optional.empty()
            ));
            devTools.send(Emulation.setTouchEmulationEnabled(true, Optional.of(10)));
            List<UserAgentBrandVersion> brands = new ArrayList<>();
            brands.add(new UserAgentBrandVersion("Google Chrome", "107"));
            brands.add(new UserAgentBrandVersion("Chromium", "107"));
            brands.add(new UserAgentBrandVersion("Not=A?Brand", "24"));

            List<UserAgentBrandVersion> fullVersionList = new ArrayList<>();
            brands.add(new UserAgentBrandVersion("Google Chrome", "107.0.5304.88"));
            brands.add(new UserAgentBrandVersion("Chromium", "107.0.5304.88"));
            brands.add(new UserAgentBrandVersion("Not=A?Brand", "24.0.0.0"));

            devTools.send(Network.setUserAgentOverride(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36",
                    Optional.of("en-US"),
                    Optional.of("Win32"),
                    Optional.of(new UserAgentMetadata(Optional.of(brands), Optional.of(fullVersionList), Optional.of("107.0.5304.88"), "Windows", "10.0.0", "x86", "", false, Optional.of("64"), Optional.of(false)))
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        RaceType raceType = RaceType.NORMAL_RACE;
        while (true) {
            String[] raceOptions = {"New Race", "Custom Race"};
            int choice = JOptionPane.showOptionDialog(null, "Choose the type of race:", "Race Chooser", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, raceOptions, raceOptions[0]);

            // If the user closes the dialog, the choice will be -1
            if (choice == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(null, "Please make a choice.");
                continue;
            }

            // Handle the choice in a switch case
            switch (choice) {
                case 0:
                    System.out.println("New Race selected.");
                    raceType = RaceType.NORMAL_RACE;
                    break;
                case 1:
                    System.out.println("Custom Race selected.");
                    raceType = RaceType.CUSTOM_RACE;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
            // Exit the loop after a valid choice is made
            break;
        }
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        if (raceType == RaceType.NORMAL_RACE) driver.get("https://play.typeracer.com/");
        else {
            String customRaceUrl = null;
            while (customRaceUrl == null || customRaceUrl.trim().isEmpty()) {
                customRaceUrl = JOptionPane.showInputDialog(null, "Enter the Custom Race URL:");
                if (customRaceUrl == null || customRaceUrl.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "URL cannot be empty. Please enter a valid URL.");
                } else if (!customRaceUrl.startsWith("https://play.typeracer.com?rt=") && !customRaceUrl.startsWith("https://play.typeracer.com/?rt=")) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid typeRacer URL.");
                    customRaceUrl = null;
                }
            }
            driver.get(customRaceUrl);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("trPopupDialog")));
            if (isAlertPresent(driver)) {
                Alert alert = driver.switchTo().alert();
                System.out.println("Alert message: " + alert.getText());
                alert.accept();
            } else {
                System.out.println("No alert present");
            }
            return;
        }
        logger.info("Opened TypeRacer site.");
        final String userName = System.getenv("USER_NAME"), userPass = System.getenv("USER_PASS");
        if (userName == null || userPass == null) {
            logger.warn("USER_NAME/USER_PASS not found in env, skipping sign-in.");
        } else {
            try {
                WebElement anchor = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[class='promptBtn signIn']")));
                anchor.click();
                logger.info("Opened sign-in popup.");
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for signIn button, Exiting...");
                System.exit(0);
                return;
            }
            WebElement editUserPopup;
            try {
                editUserPopup = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("editUserPopup")));
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for editUserPrompt popup, Exiting...");
                System.exit(0);
                return;
            }
            try {
                WebElement usernameInput = editUserPopup.findElement(By.className("gwt-TextBox"));
                wait.until(ExpectedConditions.elementToBeClickable(usernameInput));
                usernameInput.sendKeys(userName);
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for username textbox(gwt-TextBox), Exiting...");
                System.exit(0);
                return;
            }
            try {
                WebElement userpassInput = editUserPopup.findElement(By.className("gwt-PasswordTextBox"));
                wait.until(ExpectedConditions.elementToBeClickable(userpassInput));
                userpassInput.sendKeys(userPass);
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for userpass textbox(gwt-PasswordTextBox), Exiting...");
                System.exit(0);
                return;
            }
            try {
                WebElement signInButton = editUserPopup.findElement(By.className("gwt-Button"));//By.cssSelector("button[title='Sign In']"));
                wait.until(ExpectedConditions.elementToBeClickable(signInButton));
                signInButton.click();
                logger.info("Sign-in completed!");
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for sign-in button(gwt-Button), Exiting...");
                System.exit(0);
                return;
            }
        }
        try {
            WebElement upgradeAccountPromptDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("UpgradeAccountPromptDialog")));
            WebElement closeButton = upgradeAccountPromptDialog.findElement(By.className("xButton"));
            closeButton.click();
            logger.info("upgradeAccountPromptDialog closed!");
        } catch (TimeoutException e) {
            logger.warn("Timed out while waiting for upgrade prompt, ignoring...");
        }
        if (raceType == RaceType.NORMAL_RACE) try {
            WebElement enterTypingRace = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[title='Keyboard shortcut: Ctrl+Alt+I']")));
            enterTypingRace.click();
            logger.info("Entered a new race!");
        } catch (TimeoutException e) {
            logger.error("Timed out while waiting for enterTypingRace button, exiting...");
            System.exit(0);
            return;
        }
        else {
            try {
                WebElement enterTypingRace = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[title='Join race']")));
                enterTypingRace.click();
                logger.info("Entered the custom race!");
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for customEnterTypingRace button, exiting...");
                System.exit(0);
                return;
            }
        }
        logger.info("Waiting for countdownPopup.");
        while (driver.findElements(By.className("countdownPopup")).isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        logger.info("countdownPopup found, waiting for count-down to finish.");
        while (!driver.findElements(By.className("countdownPopup")).isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        logger.info("countdownPopup finished.");
        final String text;
        try {
            WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.inputPanel")));
            WebElement tbody = table.findElement(By.tagName("tbody"));
            text = tbody.getText().split("\n")[0];
        } catch (TimeoutException e) {
            logger.error("Timed out while waiting for inputPanel, exiting...");
            System.exit(0);
            return;
        }
        logger.info("Text Found: {}", text);
        WebElement inputDiv;
        try {
            inputDiv = driver.findElement(By.className("txtInput"));
        } catch (TimeoutException e) {
            logger.error("Timed out while waiting for txtInput div, exiting...");
            System.exit(0);
            return;
        }
        long start = System.currentTimeMillis();
        int accuracyRandomizerCount = getRandomNumber(1, (int) (text.length() * 0.03)), index = 1;
        List<Integer> randomizedIndexes = generateRandomNumbers(accuracyRandomizerCount, text.length() - 1);
        logger.info("Generated {} typos, indexes: {}", accuracyRandomizerCount, randomizedIndexes);
        for (char ch : text.toCharArray()) {
            inputDiv.sendKeys(ch + "");
            try {
                Thread.sleep(getRandomNumber(40, 100));
            } catch (InterruptedException ignored) {
            }
            if (randomizedIndexes.contains(index++)) {
                char randomLetter;
                if (getRandomBoolean()) {
                    randomLetter = (char) (64 + getRandomNumber(1, 26));
                } else {
                    randomLetter = (char) (96 + getRandomNumber(1, 26));
                }
                logger.info("RANDOMIZER Typing: {}", randomLetter);
                inputDiv.sendKeys(randomLetter + "");
                try {
                    Thread.sleep(getRandomNumber(70,150));
                } catch (InterruptedException ignored) {
                }
                inputDiv.sendKeys(Keys.BACK_SPACE);
            }
        }
        logger.info("Typing the text took {} ms.", System.currentTimeMillis() - start);
        WebElement challengePrompt;
        try {
            challengePrompt = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("challengePromptDialog")));
            logger.info("Found challenge popup, opening challenge image.");
        } catch (TimeoutException e) {
            logger.warn("Timed out while waiting for challengePromptDialog popup, this is normal when your WPM is less than 100. Exiting...");
            System.exit(0);
            return;
        }
        try {
            WebElement beginTestButton = challengePrompt.findElement(By.className("gwt-Button"));//By.cssSelector("button[title='Begin Test']"));
            wait.until(ExpectedConditions.elementToBeClickable(beginTestButton));
            STT.simulateRecordButtonPress();
            beginTestButton.click();
        } catch (TimeoutException e) {
            logger.error("Timed out while waiting for beginTest button, exiting...");
            System.exit(0);
            return;
        }

        WebElement typingChallengeDialog;
        try {
            typingChallengeDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("typingChallengeDialog")));
        } catch (TimeoutException e) {
            logger.error("Timed out while waiting for typingChallengeDialog popup, exiting...");
            System.exit(0);
            return;
        }
        try {
            WebElement challengeImg = typingChallengeDialog.findElement(By.className("challengeImg"));
            String imgSrc = challengeImg.getAttribute("src");
            logger.info("Challenge Image URL: {}", imgSrc);
        } catch (TimeoutException e) {
            logger.warn("Timed out while waiting for challengeImg, ignoring...");
            System.exit(0);
            return;
        }
        try {
            ob.challengeTextArea = typingChallengeDialog.findElement(By.className("challengeTextArea"));
        } catch (TimeoutException e) {
            logger.error("Timed out while waiting for challengeTextArea, exiting...");
            System.exit(0);
            return;
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        do {
            WebElement typingChallengeResultDialog;
            try {
                typingChallengeResultDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("typingChallengeResultDialog")));
            } catch (TimeoutException e) {
                logger.warn("Timed out while waiting for typingChallengeResultDialog popup, this could mean the challenge was successful!. Exiting...");
                System.exit(0);
                return;
            }
            try {
                WebElement beginRetestButton = typingChallengeResultDialog.findElement(By.className("gwt-Button"));//By.cssSelector("button[title='Begin Re-test']"));
                wait.until(ExpectedConditions.elementToBeClickable(beginRetestButton));
                STT.simulateRecordButtonPress();
                beginRetestButton.click();
            } catch (NoSuchElementException e) {
                logger.error("Couldn't find beginRetest button, exiting...");
                System.exit(0);
                return;
            }
            try {
                typingChallengeDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("typingChallengeDialog")));
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for typingChallengeDialog popup, exiting...");
                System.exit(0);
                return;
            }
            try {
                WebElement challengeImg = typingChallengeDialog.findElement(By.className("challengeImg"));
                String imgSrc = challengeImg.getAttribute("src");
                logger.info("Challenge Image URL: {}", imgSrc);
            } catch (TimeoutException e) {
                logger.warn("Timed out while waiting for challengeImg, ignoring...");
                System.exit(0);
                return;
            }
            try {
                ob.challengeTextArea = typingChallengeDialog.findElement(By.className("challengeTextArea"));
            } catch (TimeoutException e) {
                logger.error("Timed out while waiting for challengeTextArea, exiting...");
                break;
            }
        } while (true);

        /*WebElement submitButton = typingChallengeDialog.findElement(By.className("gwt-Button"));//By.cssSelector("button[title='Submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submitButton.click();*/
        /*
        <span class="time" title="Time remaining">1:34</span>
         */
        /*
        <div class="countdownPopup horizontalCountdownPopup" style="left: 64px; top: 316px; visibility: visible; position: absolute; clip: rect(auto, auto, auto, auto); overflow: visible; opacity: 0.994482;"><div class="popupContent"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="left" style="vertical-align: top;"><table cellspacing="0" cellpadding="0" style="width: 100%;"><tbody><tr><td align="left" height="65px" width="165px" style="vertical-align: middle;"><img src="https://play.typeracer.com/com.typeracer.redesign.Redesign/clear.cache.gif" style="width: 165px; height: 65px; background: url(&quot;https://play.typeracer.com/com.typeracer.redesign.Redesign/2FCE318B4838CB06D044EE4455C65A01.cache.png&quot;) 0px 0px no-repeat;" border="0" class="trafficLight"></td><td align="left" style="vertical-align: middle;"><div class="lightLabel">It's the final countdown!</div></td><td align="right" style="vertical-align: middle;"><div class="timeDisplay" style=""><span class="time">:02</span></div></td></tr></tbody></table></td></tr></tbody></table></div></div>
         */
        /*
        <div aria-hidden="true" style="display: none;"></div>
         */
        /*
        <tbody><tr><td align="left" style="vertical-align: top;"><div><div class="GBjyUEEZ UrgRlaFC" style="font-size: 20px; font-family: monospace;"><span unselectable="on" class="aJtcuIrb IKKYPDAM">A</span><span unselectable="on" class="aJtcuIrb">s</span><span unselectable="on"> the netted bag says, this potato was grown in Idaho, that onion came from a farm in Texas. Move over to Meat, though, and the chain grows longer and less comprehensible: The label doesn't mention that that rib-eye steak came from a steer born in South Dakota and fattened in a Kansas feedlot on grain grown in Iowa.</span></div><div id="smoothCaret" class="visible is-idle" style="left: -2px; top: 0px; height: 24px;"></div></div></td></tr><tr><td align="right" style="vertical-align: top;"><a class="gwt-Anchor display-format-trigger" href="javascript:;">change display format</a></td></tr></tbody>
         */
        /*
        <input type="text" class="txtInput" autocorrect="off" autocapitalize="off" maxlength="9">
         */
        /*
        <div class="DialogBox trPopupDialog typingChallengeDialog" style="left: 280px; top: 231px; visibility: visible; position: absolute; overflow: visible;"><div class="popupContent"><div><div class="Caption CaptionWithIcon"><img src="https://play.typeracer.com/com.typeracer.redesign.Redesign/clear.cache.gif" style="width:24px;height:24px;background:url(https://play.typeracer.com/com.typeracer.redesign.Redesign/B7496B103318F476B179891EF1D2ED36.cache.png) no-repeat -256px 0px;" border="0" class="icon"><span class="text">Typing Challenge</span></div><div class="dialogContent"><div><div class="bodyWidgetHolder"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="right" style="vertical-align: top;"><div class="challengeClockPanel"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="left" style="vertical-align: top;"><div class="challengeTimePrompt">Go!</div></td><td align="left" style="vertical-align: top;"><div class="timeDisplay" style=""><span class="time">:09</span></div></td></tr></tbody></table></div></td></tr><tr><td align="left" style="vertical-align: top;"><div class="challengePrompt">Type this text (it's okay to skip letters you don't understand):</div></td></tr><tr><td align="left" style="vertical-align: top;"><img src="challenge?id=1720755121387guest:165439244259352" class="challengeImg"></td></tr><tr><td align="left" style="vertical-align: top;"><textarea class="challengeTextArea" rows="4"></textarea></td></tr><tr><td align="right" style="vertical-align: top;"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="left" style="vertical-align: top;"><div class="loadingMessage" aria-hidden="true" style="padding-left: 20px; display: none;">Submitting...</div></td><td align="left" style="vertical-align: top;"><button type="button" class="gwt-Button">Submit</button></td></tr></tbody></table></td></tr></tbody></table></div></div></div></div></div></div>
         */
        /*
        <img src="challenge?id=1720755121387guest:165439244259352" class="challengeImg">
         */
        /*
        <textarea class="challengeTextArea" rows="4"></textarea>
         */
        /*
        <div class="DialogBox trPopupDialog challengePromptDialog" style="left: 65px; top: 245px; visibility: visible; position: absolute; overflow: visible;"><div class="popupContent"><div><div class="Caption CaptionWithIcon"><img src="https://play.typeracer.com/com.typeracer.redesign.Redesign/clear.cache.gif" style="width:24px;height:24px;background:url(https://play.typeracer.com/com.typeracer.redesign.Redesign/B7496B103318F476B179891EF1D2ED36.cache.png) no-repeat -256px 0px;" border="0" class="icon"><span class="text">Typing Challenge</span></div><div class="dialogContent"><div><div class="bodyWidgetHolder"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="left" style="vertical-align: top;"><div class="challengePrompt">Congrats, you just typed 196 wpm! We have to ask everyone who gets over 100 wpm in a race to take a short typing test. This is done to discourage cheaters.  <a href="http://blog.typeracer.com/2008/05/19/new-speedometer-and-improved-cheat-protection/" target="_blank">Why?</a></div></td></tr><tr><td align="left" style="vertical-align: top;"><div class="challengePrompt">If you pass this test, you will not be asked to take it again until your speed improves by another 25% as long as you remain logged in.  Please create an account to make it permanent.</div></td></tr><tr><td align="left" style="vertical-align: top;"><div class="challengePrompt">Press "Begin Test" when you are ready to start.  You will need to type at least 147 wpm to pass. Don't worry about accuracy - some mistakes are allowed.</div></td></tr><tr><td align="center" style="vertical-align: top;"><button type="button" class="gwt-Button">Begin Test</button></td></tr></tbody></table></div></div></div></div></div></div>
         */
        /*
        <button type="button" class="gwt-Button">Begin Test</button>
         */
        /*
        <div class="DialogBox trPopupDialog typingChallengeDialog" style="left: 125px; top: 40px; visibility: visible; position: absolute; overflow: visible;"><div class="popupContent"><div><div class="Caption CaptionWithIcon"><img src="https://play.typeracer.com/com.typeracer.redesign.Redesign/clear.cache.gif" style="width:24px;height:24px;background:url(https://play.typeracer.com/com.typeracer.redesign.Redesign/B7496B103318F476B179891EF1D2ED36.cache.png) no-repeat -256px 0px;" border="0" class="icon"><span class="text">Typing Challenge</span></div><div class="dialogContent"><div><div class="bodyWidgetHolder"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="right" style="vertical-align: top;"><div class="challengeClockPanel"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="left" style="vertical-align: top;"><div class="challengeTimePrompt">Go!</div></td><td align="left" style="vertical-align: top;"><div class="timeDisplay" style=""><span class="time">:07</span></div></td></tr></tbody></table></div></td></tr><tr><td align="left" style="vertical-align: top;"><div class="challengePrompt">Type this text (it's okay to skip letters you don't understand):</div></td></tr><tr><td align="left" style="vertical-align: top;"><img src="challenge?id=1720758102390guest:12310499665294" class="challengeImg"></td></tr><tr><td align="left" style="vertical-align: top;"><textarea class="challengeTextArea" rows="4"></textarea></td></tr><tr><td align="right" style="vertical-align: top;"><table cellspacing="0" cellpadding="0"><tbody><tr><td align="left" style="vertical-align: top;"><div class="loadingMessage" aria-hidden="true" style="padding-left: 20px; display: none;">Submitting...</div></td><td align="left" style="vertical-align: top;"><button type="button" class="gwt-Button">Submit</button></td></tr></tbody></table></td></tr></tbody></table></div></div></div></div></div></div>
         */
        /*
        <img src="challenge?id=1720758102390guest:12310499665294" class="challengeImg">
         */
        /*
        <button type="button" class="gwt-Button">Submit</button>
         */
    }

    public static List<Integer> generateRandomNumbers(int n, int len) {
        if (n > len) {
            throw new IllegalArgumentException("n cannot be greater than len.");
        } else if (n == 0) {
            return new ArrayList<>();
        }

        Random random = new Random();
        Set<Integer> numbers = new HashSet<>();

        while (numbers.size() < n) {
            numbers.add(random.nextInt(len) + 1);
        }

        return new ArrayList<>(numbers);
    }

    private static boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    private static int getRandomNumber(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static boolean isAlertPresent(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    public void overwriteCaptcha(final String captcha) {
        if (challengeTextArea == null) return;

        challengeTextArea.clear();
        challengeTextArea.sendKeys(captcha);
    }

    /*
    public void appendCaptcha(final String captcha) {
        if (challengeTextArea == null)
            return;

        this.captcha += captcha;
        challengeTextArea.clear();
        challengeTextArea.sendKeys(captcha);
    }

    public void eraseCaptcha() {
        if (challengeTextArea == null)
            return;

        this.captcha = "";
        challengeTextArea.clear();
        challengeTextArea.sendKeys(captcha);
    }*/

}
