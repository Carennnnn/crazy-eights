package com.example.comp4004f22a3101077008;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = Application.class)
public class AcceptanceTest {
    @Autowired
    GameData gd;
    @Autowired
    GameLogic game;
    @LocalServerPort
    int port;
    private ConfigurableApplicationContext ctx;
    WebDriver[] multiDriver;
    private int numOfPlayers = 4;

    @BeforeEach
    public void setUpDrivers() {
        ctx = SpringApplication.run(Application.class);
        System.setProperty("webdriver.chrome.driver",
                "chromedriver-mac-x64/chromedriver");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        multiDriver = new WebDriver[numOfPlayers];
        for (int i = 0; i < multiDriver.length; i++) {
            multiDriver[i] = new ChromeDriver(options);
            multiDriver[i].get("http://localhost:" + port);
            WebDriverWait wait = new WebDriverWait(multiDriver[i], Duration.ofSeconds(5));
            wait.until(ExpectedConditions.elementToBeClickable(By.id("usernameBtn"))).click();
        }
        sleepForMilliseconds(3000);
    }

    @AfterEach
    public void shutDownDrivers() {
        if (ctx != null) {
            ctx.close();
        }
        for (int i = 0; i < numOfPlayers; i++) {
            if (multiDriver[i] != null) {
                multiDriver[i].quit();
            }
        }
    }

    public void parseCards(String cards) {
        // convert string to array of cards
        String[] cardsStrArr = cards.split(" ");
        ArrayList<Card> cardsArr = new ArrayList<>();
        for (int i = 0; i < cardsStrArr.length; i++) {
            String rank = cardsStrArr[i].substring(0, 1);
            String suit = cardsStrArr[i].substring(1);
            cardsArr.add(new Card(suit, rank));
        }
        // set the deck gd.setCards()
        gd.setCards(cardsArr);
        // set the top card gd.setTopCard()
        Card topCard = cardsArr.remove(0);
        gd.setTopCard(topCard);
        // deal 5 cards to each player
        for(Player p:gd.getPlayers()){
            p.resetCards();
            game.startDealCards(gd.getCards(),gd.getPlayers(),p.getID()-1);
        }
    }

    public void sleepForMilliseconds(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (Exception e) {

        }
    }

    // for a specific driver, wait seconds for a button to be clickable and then click this button by its id
    public void waitButtonToBeClickableAndClick(WebDriver driver, int seconds, String id) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        wait.until(ExpectedConditions.elementToBeClickable(By.id(id))).click();
    }

    // assert button of id is enabled
    public void assertButtonEnabled(WebDriver driver, String id) {
        boolean isEnabled = driver.findElement(By.id(id)).isEnabled();
        assertTrue(isEnabled);
    }

    // assert button of id is disabled
    public void assertButtonDisabled(WebDriver driver, String id) {
        boolean isEnabled = driver.findElement(By.id(id)).isEnabled();
        assertFalse(isEnabled);
    }

    // assert player's turn is expected turn for all players
    public void assertPlayerTurnForAllPlayers(String expectedTurn) {
        for (int i = 0; i < multiDriver.length; i++) {
            String turn = multiDriver[i].findElement(By.id("turnID")).getText();
            assertEquals(expectedTurn, turn);
        }
    }

    // assert top card is topCard for all players
    public void assertTopCardForAllPlayers(String topCard) {
        for (int i = 0; i < multiDriver.length; i++) {
            WebElement img = multiDriver[i].findElement(By.id("topCardCol")).findElement(By.tagName("img"));
            String id = img.getAttribute("id");
            assertEquals(topCard, id);
        }
    }

    // assert the drawn card is as expected
    public void assertDrawnCard(WebDriver driver, String drawnCard) {
        List<WebElement> cards = driver.findElements(By.className("card"));
        boolean isCardFound = false;
        for (WebElement card: cards) {
            String id = card.getAttribute(("id"));
            if (id.equals(drawnCard)) {
                isCardFound = true;
                break;
            }
        }
        assertTrue(isCardFound);
    }

    // player plays card by clicking the card
    public void playerPlaysCard(WebDriver driver, String card) {
        driver.findElement(By.id(card)).click();
        sleepForMilliseconds(200);
    }

    // assert direction is left or right
    public void assertDirection(String expectedDirection) {
        for (int i = 0; i < multiDriver.length; i++) {
            String direction = multiDriver[i].findElement(By.id("direction")).getText();
            assertEquals(expectedDirection, direction);
        }
    }

    // check the choices for suits of player are visible
    public void assertSuitsAreVisible(WebDriver driver) {
        boolean isSpadeVisible = driver.findElement(By.id("spade")).isDisplayed();
        boolean isHeartVisible = driver.findElement(By.id("heart")).isDisplayed();
        boolean isClubVisible = driver.findElement(By.id("club")).isDisplayed();
        boolean isDiamondVisible = driver.findElement(By.id("diamond")).isDisplayed();

        assertTrue(isSpadeVisible);
        assertTrue(isHeartVisible);
        assertTrue(isClubVisible);
        assertTrue(isDiamondVisible);
    }

    // assert alert message is popped up
    public void assertAlertMessage(WebDriver driver, int seconds, String message) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String alertMessage = alert.getText();
        assertEquals(message, alertMessage);
    }

    // player clicks OK button on alert popup
    public void clickOKButton(WebDriver driver) {
        Alert alert = driver.switchTo().alert();
        alert.accept();
    }

    // assert all cards are disabled except one card
    public void assertAllCardsAreDisabledExcept(WebDriver driver, String nonDisabledCard) {
        List<WebElement> cards = driver.findElements(By.className("card"));
        for (WebElement card: cards) {
            String id = card.getAttribute(("id"));
            boolean isDisabled = card.getAttribute("disabled") != null;
            if (id.equals(nonDisabledCard)) {
                assertFalse(isDisabled);
            } else {
                assertTrue(isDisabled);
            }
        }
    }

    @Test
    @DisplayName("Test row 25")
    @DirtiesContext
    public void testRow25() {
        // rig the game before clicking the start game button on player 1
        rigTestRow25();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is AC for all players
        assertTopCardForAllPlayers("AC");

        // player 1 plays 3C
        playerPlaysCard(multiDriver[0], "3C");

        // assert top card is 3C for all players
        assertTopCardForAllPlayers("3C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow25() {
        String rigC = "AC 2C 3C AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD 4C 5C 6C 7C 8C 9C TC JC QC KC AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 27")
    @DirtiesContext
    public void testRow27() {
        // rig the game before clicking the start game button on player 1
        rigTestRow27();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is AC for all players
        assertTopCardForAllPlayers("AC");

        // player 1 plays AH
        playerPlaysCard(multiDriver[0], "AH");

        // assert top card is AH for all players
        assertTopCardForAllPlayers("AH");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is right
        assertDirection("right");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7H
        playerPlaysCard(multiDriver[3], "7H");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // assert direction is right
        assertDirection("right");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");
    }

    public void rigTestRow27() {
        String rigC = "AC AH 2C 3C AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD 7H KD 4C 5C 6C 7C 8C 9C TC JC QC KC 2H 3H 4H 5H 6H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 28")
    @DirtiesContext
    public void testRow28() {
        // rig the game before clicking the start game button on player 1
        rigTestRow28();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is AC for all players
        assertTopCardForAllPlayers("AC");

        // player 1 plays QC
        playerPlaysCard(multiDriver[0], "QC");

        // assert top card is QC for all players
        assertTopCardForAllPlayers("QC");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");
    }

    public void rigTestRow28() {
        String rigC = "AC QC AH 2C 3C AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD 7H KD 4C 5C 6C 7C 8C 9C TC JC KC 2H 3H 4H 5H 6H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 29")
    @DirtiesContext
    public void testRow29() {
        // rig the game before clicking the start game button on player 1
        rigTestRow29();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is AC for all players
        assertTopCardForAllPlayers("AC");

        // player 1 plays 6C
        playerPlaysCard(multiDriver[0], "6C");

        // assert top card is 6C for all players
        assertTopCardForAllPlayers("6C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 4C
        playerPlaysCard(multiDriver[1], "4C");

        // assert top card is 4C for all players
        assertTopCardForAllPlayers("4C");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 5C
        playerPlaysCard(multiDriver[2], "5C");

        // assert top card is 5C for all players
        assertTopCardForAllPlayers("5C");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 3C
        playerPlaysCard(multiDriver[3], "3C");

        // assert top card is 3C for all players
        assertTopCardForAllPlayers("3C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");
    }

    public void rigTestRow29() {
        String rigC = "AC 6C AD 2D 3D 4D 5D 6D 7D 8D 4C 9D TD JD QD 5C 3C 7H KD 8C 7C QC AH 2C 9C TC JC KC 2H 3H 4H 5H 6H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 31")
    @DirtiesContext
    public void testRow31() {
        // rig the game before clicking the start game button on player 1
        rigTestRow31();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is AC for all players
        assertTopCardForAllPlayers("2H");

        // player 1 plays 3H
        playerPlaysCard(multiDriver[0], "3H");

        // assert top card is 3H for all players
        assertTopCardForAllPlayers("3H");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 4H
        playerPlaysCard(multiDriver[1], "4H");

        // assert top card is 4H for all players
        assertTopCardForAllPlayers("4H");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 5H
        playerPlaysCard(multiDriver[2], "5H");

        // assert top card is 5H for all players
        assertTopCardForAllPlayers("5H");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays AH
        playerPlaysCard(multiDriver[3], "AH");

        // assert top card is AH for all players
        assertTopCardForAllPlayers("AH");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is right
        assertDirection("right");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7H
        playerPlaysCard(multiDriver[2], "7H");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is right
        assertDirection("right");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow31() {
        String rigC = "2H 3H AD 2D 3D 4D 4H 5D 6D 7D 8D 5H 7H 9D TD JD QD AH KD AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC 6H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 32")
    @DirtiesContext
    public void testRow32() {
        // rig the game before clicking the start game button on player 1
        rigTestRow32();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is AD for all players
        assertTopCardForAllPlayers("AD");

        // player 1 plays 3D
        playerPlaysCard(multiDriver[0], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3C
        playerPlaysCard(multiDriver[1], "3C");

        // assert top card is 3C for all players
        assertTopCardForAllPlayers("3C");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4C
        playerPlaysCard(multiDriver[2], "4C");

        // assert top card is 4C for all players
        assertTopCardForAllPlayers("4C");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays QC
        playerPlaysCard(multiDriver[3], "QC");

        // assert top card is QC for all players
        assertTopCardForAllPlayers("QC");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow32() {
        String rigC = "AD 3D 2D 4D 5D 6D 3C 7D 8D 9D TD 4C JD QD KD AC QC 2C 5C 6C 7C 8C 9C TC JC KC AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 35")
    @DirtiesContext
    public void testRow35() {
        // rig the game before clicking the start game button on player 1
        rigTestRow35();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is KC for all players
        assertTopCardForAllPlayers("KC");

        // player 1 plays KH
        playerPlaysCard(multiDriver[0], "KH");

        // assert top card is KH for all players
        assertTopCardForAllPlayers("KH");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow35() {
        String rigC = "KC KH AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 36")
    @DirtiesContext
    public void testRow36() {
        // rig the game before clicking the start game button on player 1
        rigTestRow36();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is KC for all players
        assertTopCardForAllPlayers("KC");

        // player 1 plays 7C
        playerPlaysCard(multiDriver[0], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow36() {
        String rigC = "KC 7C AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD AC 2C 3C 4C 5C 6C 8C 9C TC KH JC QC AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 37")
    @DirtiesContext
    public void testRow37() {
        // rig the game before clicking the start game button on player 1
        rigTestRow37();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is KC for all players
        assertTopCardForAllPlayers("KC");

        // player 1 plays 8H
        playerPlaysCard(multiDriver[0], "8H");

        // assert top card is 8H for all players
        assertTopCardForAllPlayers("8H");

        // check the choices for suits are visible
        assertSuitsAreVisible(multiDriver[0]);

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");
    }

    public void rigTestRow37() {
        String rigC = "KC 8H 7C AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD AC 2C 3C 4C 5C 6C 8C 9C TC KH JC QC AH 2H 3H 4H 5H 6H 7H 9H TH JH QH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 38")
    @DirtiesContext
    public void testRow38() {
        // rig the game before clicking the start game button on player 1
        rigTestRow38();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is KC for all players
        assertTopCardForAllPlayers("KC");

        // player 1 plays 5S
        playerPlaysCard(multiDriver[0], "5S");

        // assert alert message popped up
        assertAlertMessage(multiDriver[0], 3, "Invalid Selection");
    }

    public void rigTestRow38() {
        String rigC = "KC 5S 8H 7C AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD AC 2C 3C 4C 5C 6C 8C 9C TC KH JC QC AH 2H 3H 4H 5H 6H 7H 9H TH JH QH AS 2S 3S 4S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 42")
    @DirtiesContext
    public void testRow42() {
        // rig the game before clicking the start game button on player 1
        rigTestRow42();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6D
        playerPlaysCard(multiDriver[0], "6D");

        // assert top card is 6D for all players
        assertTopCardForAllPlayers("6D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9D
        playerPlaysCard(multiDriver[2], "9D");

        // assert top card is 9D for all players
        assertTopCardForAllPlayers("9D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TH
        playerPlaysCard(multiDriver[0], "TH");

        // assert top card is TH for all players
        assertTopCardForAllPlayers("TH");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KH
        playerPlaysCard(multiDriver[1], "KH");

        // assert top card is KH for all players
        assertTopCardForAllPlayers("KH");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4H
        playerPlaysCard(multiDriver[2], "4H");

        // assert top card is 4H for all players
        assertTopCardForAllPlayers("4H");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5H
        playerPlaysCard(multiDriver[3], "5H");

        // assert top card is 5H for all players
        assertTopCardForAllPlayers("5H");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6H
        playerPlaysCard(multiDriver[0], "6H");

        // assert top card is 6H for all players
        assertTopCardForAllPlayers("6H");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 7H
        playerPlaysCard(multiDriver[1], "7H");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9H
        playerPlaysCard(multiDriver[2], "9H");

        // assert top card is 9H for all players
        assertTopCardForAllPlayers("9H");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9C
        playerPlaysCard(multiDriver[3], "9C");

        // assert top card is 9C for all players
        assertTopCardForAllPlayers("9C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 6C
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 6C
        assertDrawnCard(multiDriver[0], "6C");

        // assert all cards are disabled except 6C
        assertAllCardsAreDisabledExcept(multiDriver[0], "6C");

        // assert draw button is disabled
        assertButtonDisabled(multiDriver[0], "draw");

        // player 1 plays 6C
        playerPlaysCard(multiDriver[0], "6C");

        // assert top card is 6C for all players
        assertTopCardForAllPlayers("6C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow42() {
        String rigC = "7C 7D 6D TH 6H 3H 3D KD KH 7H AS 4D 9D 4H 9H 2S 5D TD 5H 9C 3S 6C AC 2C 3C AD 2D 8D JD QD 4C 5C 8C TC JC QC KC AH 2H 8H JH QH 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 43")
    @DirtiesContext
    public void testRow43() {
        // rig the game before clicking the start game button on player 1
        rigTestRow43();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9D
        playerPlaysCard(multiDriver[2], "9D");

        // assert top card is 9D for all players
        assertTopCardForAllPlayers("9D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TH
        playerPlaysCard(multiDriver[0], "TH");

        // assert top card is TH for all players
        assertTopCardForAllPlayers("TH");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KH
        playerPlaysCard(multiDriver[1], "KH");

        // assert top card is KH for all players
        assertTopCardForAllPlayers("KH");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4H
        playerPlaysCard(multiDriver[2], "4H");

        // assert top card is 4H for all players
        assertTopCardForAllPlayers("4H");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5H
        playerPlaysCard(multiDriver[3], "5H");

        // assert top card is 5H for all players
        assertTopCardForAllPlayers("5H");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6H
        playerPlaysCard(multiDriver[0], "6H");

        // assert top card is 6H for all players
        assertTopCardForAllPlayers("6H");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 7H
        playerPlaysCard(multiDriver[1], "7H");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9H
        playerPlaysCard(multiDriver[2], "9H");

        // assert top card is 9H for all players
        assertTopCardForAllPlayers("9H");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9C
        playerPlaysCard(multiDriver[3], "9C");

        // assert top card is 9C for all players
        assertTopCardForAllPlayers("9C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 6D
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 6D
        assertDrawnCard(multiDriver[0], "6D");

        sleepForMilliseconds(200);

        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 5C
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 5C
        assertDrawnCard(multiDriver[0], "5C");

        // assert all cards are disabled except 5C
        assertAllCardsAreDisabledExcept(multiDriver[0], "5C");

        // assert draw button is disabled
        assertButtonDisabled(multiDriver[0], "draw");

        // player 1 plays 5C
        playerPlaysCard(multiDriver[0], "5C");

        // assert top card is 5C for all players
        assertTopCardForAllPlayers("5C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow43() {
        String rigC = "7C 7D JD TH 6H 3H 3D KD KH 7H AS 4D 9D 4H 9H 2S 5D TD 5H 9C 3S 6D 5C 6C AC 2C 3C AD 2D 8D QD 4C 8C TC JC QC KC AH 2H 8H JH QH 4S 5S 6S 7S 8S 9S TS JS QS KS";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 44")
    @DirtiesContext
    public void testRow44() {
        // rig the game before clicking the start game button on player 1
        rigTestRow44();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 playS 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9D
        playerPlaysCard(multiDriver[2], "9D");

        // assert top card is 9D for all players
        assertTopCardForAllPlayers("9D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TS
        playerPlaysCard(multiDriver[0], "TS");

        // assert top card is TS for all players
        assertTopCardForAllPlayers("TS");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KS
        playerPlaysCard(multiDriver[1], "KS");

        // assert top card is KS for all players
        assertTopCardForAllPlayers("KS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4S
        playerPlaysCard(multiDriver[2], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9S
        playerPlaysCard(multiDriver[3], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6S
        playerPlaysCard(multiDriver[0], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays JS
        playerPlaysCard(multiDriver[1], "JS");

        // assert top card is JS for all players
        assertTopCardForAllPlayers("JS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7S
        playerPlaysCard(multiDriver[2], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7C
        playerPlaysCard(multiDriver[3], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 6D
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 6D
        assertDrawnCard(multiDriver[0], "6D");

        sleepForMilliseconds(200);

        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 5S
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 5S
        assertDrawnCard(multiDriver[0], "5S");

        sleepForMilliseconds(200);

        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 7H
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 7H
        assertDrawnCard(multiDriver[0], "7H");

        // assert all cards are disabled except 7H
        assertAllCardsAreDisabledExcept(multiDriver[0], "7H");

        // assert draw button is disabled
        assertButtonDisabled(multiDriver[0], "draw");

        // player 1 plays 7H
        playerPlaysCard(multiDriver[0], "7H");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow44() {
        String rigC = "7S 7D JD TS 6S 3H 3D KD KS JS AD 4D 9D 4S 7S 2D 5D TD 9S 7C AH 6D 5S 7H 8D QD AC 2C 3C 4C 5C 6C 8C 9C TC JC QC KC 2H 4H 5H 6H 8H 9H TH JH QH KH AS 2S 3S 8S";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 45")
    @DirtiesContext
    public void testRow45() {
        // rig the game before clicking the start game button on player 1
        rigTestRow45();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 playS 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9D
        playerPlaysCard(multiDriver[2], "9D");

        // assert top card is 9D for all players
        assertTopCardForAllPlayers("9D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TS
        playerPlaysCard(multiDriver[0], "TS");

        // assert top card is TS for all players
        assertTopCardForAllPlayers("TS");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KS
        playerPlaysCard(multiDriver[1], "KS");

        // assert top card is KS for all players
        assertTopCardForAllPlayers("KS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4S
        playerPlaysCard(multiDriver[2], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9S
        playerPlaysCard(multiDriver[3], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6S
        playerPlaysCard(multiDriver[0], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays JS
        playerPlaysCard(multiDriver[1], "JS");

        // assert top card is JS for all players
        assertTopCardForAllPlayers("JS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7S
        playerPlaysCard(multiDriver[2], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7C
        playerPlaysCard(multiDriver[3], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 6D
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 6D
        assertDrawnCard(multiDriver[0], "6D");

        sleepForMilliseconds(200);

        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 5S
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 5S
        assertDrawnCard(multiDriver[0], "5S");

        sleepForMilliseconds(200);

        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 4H
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 4H
        assertDrawnCard(multiDriver[0], "4H");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");
    }

    public void rigTestRow45() {
        String rigC = "7S 7D JD TS 6S 3H 3D KD KS JS AD 4D 9D 4S 7S 2D 5D TD 9S 7C AH 6D 5S 4H 8H 7H 8D QD AC 2C 3C 4C 5C 6C 8C 9C TC JC QC KC 2H 5H 6H 9H TH JH QH KH AS 2S 3S 8S";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 46")
    @DirtiesContext
    public void testRow46() {
        // rig the game before clicking the start game button on player 1
        rigTestRow46();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 playS 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9D
        playerPlaysCard(multiDriver[2], "9D");

        // assert top card is 9D for all players
        assertTopCardForAllPlayers("9D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TS
        playerPlaysCard(multiDriver[0], "TS");

        // assert top card is TS for all players
        assertTopCardForAllPlayers("TS");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KS
        playerPlaysCard(multiDriver[1], "KS");

        // assert top card is KS for all players
        assertTopCardForAllPlayers("KS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4S
        playerPlaysCard(multiDriver[2], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9S
        playerPlaysCard(multiDriver[3], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6S
        playerPlaysCard(multiDriver[0], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays JS
        playerPlaysCard(multiDriver[1], "JS");

        // assert top card is JS for all players
        assertTopCardForAllPlayers("JS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7S
        playerPlaysCard(multiDriver[2], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7C
        playerPlaysCard(multiDriver[3], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 6D
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 6D
        assertDrawnCard(multiDriver[0], "6D");

        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 8H
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 8H
        assertDrawnCard(multiDriver[0], "8H");

        sleepForMilliseconds(200);

        // assert all cards are disabled except 8H
        assertAllCardsAreDisabledExcept(multiDriver[0], "8H");

        // assert draw button is disabled
        assertButtonDisabled(multiDriver[0], "draw");

        // player 1 plays 8H
        playerPlaysCard(multiDriver[0], "8H");

        // declare new suit spade
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "spade");

        // assert top card is S for all players
        assertTopCardForAllPlayers("S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

    }

    public void rigTestRow46() {
        String rigC = "7S 7D JD TS 6S 3H 3D KD KS JS AD 4D 9D 4S 7S 2D 5D TD 9S 7C AH 6D 8H 5S 4H 7H 8D QD AC 2C 3C 4C 5C 6C 8C 9C TC JC QC KC 2H 5H 6H 9H TH JH QH KH AS 2S 3S 8S";
        parseCards(rigC);
    }


    @Test
    @DisplayName("Test row 51")
    @DirtiesContext
    public void testRow51() {
        // rig the game before clicking the start game button on player 1
        rigTestRow51();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 playS 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 6D
        playerPlaysCard(multiDriver[2], "6D");

        // assert top card is 6D for all players
        assertTopCardForAllPlayers("6D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TS
        playerPlaysCard(multiDriver[0], "TS");

        // assert top card is TS for all players
        assertTopCardForAllPlayers("TS");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KS
        playerPlaysCard(multiDriver[1], "KS");

        // assert top card is KS for all players
        assertTopCardForAllPlayers("KS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4S
        playerPlaysCard(multiDriver[2], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9S
        playerPlaysCard(multiDriver[3], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6S
        playerPlaysCard(multiDriver[0], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays JS
        playerPlaysCard(multiDriver[1], "JS");

        // assert top card is JS for all players
        assertTopCardForAllPlayers("JS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7S
        playerPlaysCard(multiDriver[2], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7C
        playerPlaysCard(multiDriver[3], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 2C
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 2C
        assertDrawnCard(multiDriver[0], "2C");

        // player 1 plays 2C
        playerPlaysCard(multiDriver[0], "2C");

        // assert top card is 2C for all players
        assertTopCardForAllPlayers("2C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // assert drawn card is 6C
        assertDrawnCard(multiDriver[1], "6C");

        // assert drawn card is 9D
        assertDrawnCard(multiDriver[1], "9D");

        sleepForMilliseconds(200);

        // player 2 plays 6C
        playerPlaysCard(multiDriver[1], "6C");

        // assert top card is 6C for all players
        assertTopCardForAllPlayers("6C");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");
    }

    public void rigTestRow51() {
        String rigC = "7H 7D JD TS 6S 5H 3D KD KS JS 4H 4D 6D 4S 7S 2D 5D TD 9S 7C AH 2C 6C 9D 8H 5S 7S 8D QD AC 3H 3C 4C AD 5C 8C 9C TC JC QC KC 5H 6H 9H TH JH QH KH AS 2S 3S 8S";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 52")
    @DirtiesContext
    public void testRow52() {
        // rig the game before clicking the start game button on player 1
        rigTestRow52();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 playS 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 6D
        playerPlaysCard(multiDriver[2], "6D");

        // assert top card is 6D for all players
        assertTopCardForAllPlayers("6D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TS
        playerPlaysCard(multiDriver[0], "TS");

        // assert top card is TS for all players
        assertTopCardForAllPlayers("TS");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KS
        playerPlaysCard(multiDriver[1], "KS");

        // assert top card is KS for all players
        assertTopCardForAllPlayers("KS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4S
        playerPlaysCard(multiDriver[2], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9S
        playerPlaysCard(multiDriver[3], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6S
        playerPlaysCard(multiDriver[0], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays JS
        playerPlaysCard(multiDriver[1], "JS");

        // assert top card is JS for all players
        assertTopCardForAllPlayers("JS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7S
        playerPlaysCard(multiDriver[2], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7C
        playerPlaysCard(multiDriver[3], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 2C
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 2C
        assertDrawnCard(multiDriver[0], "2C");

        // player 1 plays 2C
        playerPlaysCard(multiDriver[0], "2C");

        // assert top card is 2C for all players
        assertTopCardForAllPlayers("2C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // assert drawn card is 6S
        assertDrawnCard(multiDriver[1], "6S");

        // assert drawn card is 9D
        assertDrawnCard(multiDriver[1], "9D");

        sleepForMilliseconds(200);

        // player 2 plays 6S
        playerPlaysCard(multiDriver[1], "6S");

        // assert alert message popped up
        assertAlertMessage(multiDriver[1], 3, "Invalid Selection");

        // click OK button on the alert
        clickOKButton(multiDriver[1]);

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 draws 9H
        waitButtonToBeClickableAndClick(multiDriver[1], 3, "draw");

        // assert drawn card is 9H
        assertDrawnCard(multiDriver[1], "9H");

        // player 2 draws 6C
        waitButtonToBeClickableAndClick(multiDriver[1], 3, "draw");

        // assert drawn card is 6C
        assertDrawnCard(multiDriver[1], "6C");

        // assert all cards are disabled except 6C
        assertAllCardsAreDisabledExcept(multiDriver[1], "6C");

        // assert draw button is disabled
        assertButtonDisabled(multiDriver[1], "draw");

        // player 2 plays 6C
        playerPlaysCard(multiDriver[1], "6C");

        // assert top card is 6C for all players
        assertTopCardForAllPlayers("6C");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

    }

    public void rigTestRow52() {
        String rigC = "7H 7D JD TS 6S 5H 3D KD KS JS 4H 4D 6D 4S 7S 2D 5D TD 9S 7C AH 2C 6S 9D 9H 6C 8H 5S 7S 8D QD AC 3H 3C 4C AD 5C 8C 9C TC JC QC KC 5H 6H TH JH QH KH AS 2S 3S 8S";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 53")
    @DirtiesContext
    public void testRow53() {
        // rig the game before clicking the start game button on player 1
        rigTestRow53();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 playS 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 6D
        playerPlaysCard(multiDriver[2], "6D");

        // assert top card is 6D for all players
        assertTopCardForAllPlayers("6D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TS
        playerPlaysCard(multiDriver[0], "TS");

        // assert top card is TS for all players
        assertTopCardForAllPlayers("TS");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KS
        playerPlaysCard(multiDriver[1], "KS");

        // assert top card is KS for all players
        assertTopCardForAllPlayers("KS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4S
        playerPlaysCard(multiDriver[2], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9S
        playerPlaysCard(multiDriver[3], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6S
        playerPlaysCard(multiDriver[0], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays JS
        playerPlaysCard(multiDriver[1], "JS");

        // assert top card is JS for all players
        assertTopCardForAllPlayers("JS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7S
        playerPlaysCard(multiDriver[2], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7C
        playerPlaysCard(multiDriver[3], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 2C
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 2C
        assertDrawnCard(multiDriver[0], "2C");

        // player 1 plays 2C
        playerPlaysCard(multiDriver[0], "2C");

        // assert top card is 2C for all players
        assertTopCardForAllPlayers("2C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // assert drawn card is 6S
        assertDrawnCard(multiDriver[1], "6S");

        // assert drawn card is 9D
        assertDrawnCard(multiDriver[1], "9D");

        sleepForMilliseconds(200);

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 draws 9H
        waitButtonToBeClickableAndClick(multiDriver[1], 3, "draw");

        // assert drawn card is 9H
        assertDrawnCard(multiDriver[1], "9H");

        // player 2 draws 7S
        waitButtonToBeClickableAndClick(multiDriver[1], 3, "draw");

        // assert drawn card is 7S
        assertDrawnCard(multiDriver[1], "7S");

        // player 2 draws 5H
        waitButtonToBeClickableAndClick(multiDriver[1], 3, "draw");

        // assert drawn card is 5H
        assertDrawnCard(multiDriver[1], "5H");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");
    }

    public void rigTestRow53() {
        String rigC = "7H 7D JD TS 6S 5H 3D KD KS JS 4H 4D 6D 4S 7S 2D 5D TD 9S 7C AH 2C 6S 9D 9H 7S 5H 6C 8H 5S 8D QD AC 3H 3C 4C AD 5C 8C 9C TC JC QC KC 6H TH JH QH KH AS 2S 3S 8S";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 55")
    @DirtiesContext
    public void testRow55() {
        // rig the game before clicking the start game button on player 1
        rigTestRow55();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 7H for all players
        assertTopCardForAllPlayers("7H");

        // player 1 plays 7D
        playerPlaysCard(multiDriver[0], "7D");

        // assert top card is 7D for all players
        assertTopCardForAllPlayers("7D");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 3D
        playerPlaysCard(multiDriver[1], "3D");

        // assert top card is 3D for all players
        assertTopCardForAllPlayers("3D");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 playS 4D
        playerPlaysCard(multiDriver[2], "4D");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 5D
        playerPlaysCard(multiDriver[3], "5D");

        // assert top card is 5D for all players
        assertTopCardForAllPlayers("5D");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays JD
        playerPlaysCard(multiDriver[0], "JD");

        // assert top card is JD for all players
        assertTopCardForAllPlayers("JD");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KD
        playerPlaysCard(multiDriver[1], "KD");

        // assert top card is KD for all players
        assertTopCardForAllPlayers("KD");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 6D
        playerPlaysCard(multiDriver[2], "6D");

        // assert top card is 6D for all players
        assertTopCardForAllPlayers("6D");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays TD
        playerPlaysCard(multiDriver[3], "TD");

        // assert top card is TD for all players
        assertTopCardForAllPlayers("TD");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays TS
        playerPlaysCard(multiDriver[0], "TS");

        // assert top card is TS for all players
        assertTopCardForAllPlayers("TS");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KS
        playerPlaysCard(multiDriver[1], "KS");

        // assert top card is KS for all players
        assertTopCardForAllPlayers("KS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 4S
        playerPlaysCard(multiDriver[2], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 9S
        playerPlaysCard(multiDriver[3], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 6S
        playerPlaysCard(multiDriver[0], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays JS
        playerPlaysCard(multiDriver[1], "JS");

        // assert top card is JS for all players
        assertTopCardForAllPlayers("JS");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 7S
        playerPlaysCard(multiDriver[2], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 7C
        playerPlaysCard(multiDriver[3], "7C");

        // assert top card is 7C for all players
        assertTopCardForAllPlayers("7C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws 2C
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is 2C
        assertDrawnCard(multiDriver[0], "2C");

        // player 1 plays 2C
        playerPlaysCard(multiDriver[0], "2C");

        // assert top card is 2C for all players
        assertTopCardForAllPlayers("2C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // assert drawn card is 2H
        assertDrawnCard(multiDriver[1], "2H");

        // assert drawn card is 9D
        assertDrawnCard(multiDriver[1], "9D");

        // player 2 plays 2H
        playerPlaysCard(multiDriver[1], "2H");

        // assert top card is 2H for all players
        assertTopCardForAllPlayers("2H");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert drawn card is 5S
        assertDrawnCard(multiDriver[2], "5S");

        // assert drawn card is 6D
        assertDrawnCard(multiDriver[2], "6D");

        // assert drawn card is 6H
        assertDrawnCard(multiDriver[2], "6H");

        // assert drawn card is 7C
        assertDrawnCard(multiDriver[2], "7C");

        // player 3 plays 6H
        playerPlaysCard(multiDriver[2], "6H");

        // assert top card is 6H for all players
        assertTopCardForAllPlayers("6H");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");
    }

    public void rigTestRow55() {
        String rigC = "7H 7D JD TS 6S 5H 3D KD KS JS 4H 4D 6D 4S 7S 7D 5D TD 9S 7C AH 2C 2H 9D 5S 6D 6H 7C 9H 7S 5H 6C 8H 8D QD AC 3H 3C 4C AD 5C 8C QC KC 6H TH JH QH KH AS 2S 3S 8S";
        parseCards(rigC);
    }

    @Test
    @DisplayName("Test row 64")
    @DirtiesContext
    public void testRow64() {
        // rig the game before clicking the start game button on player 1
        rigTestRow64();
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "startBtn");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert top card is 4D for all players
        assertTopCardForAllPlayers("4D");

        // player 1 plays 4H
        playerPlaysCard(multiDriver[0], "4H");

        // assert top card is 4H for all players
        assertTopCardForAllPlayers("4H");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 4S
        playerPlaysCard(multiDriver[1], "4S");

        // assert top card is 4S for all players
        assertTopCardForAllPlayers("4S");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9S
        playerPlaysCard(multiDriver[2], "9S");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 draws 2C
        waitButtonToBeClickableAndClick(multiDriver[3], 3, "draw");

        // assert drawn card is 2C
        assertDrawnCard(multiDriver[3], "2C");

        // player 4 plays 2C
        playerPlaysCard(multiDriver[3], "2C");

        // assert alert message popped up
        assertAlertMessage(multiDriver[3], 3, "Invalid Selection");

        // click OK button on the alert
        clickOKButton(multiDriver[3]);

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 draws 3C
        waitButtonToBeClickableAndClick(multiDriver[3], 3, "draw");

        // assert drawn card is 3C
        assertDrawnCard(multiDriver[3], "3C");

        // player 4 plays 3C
        playerPlaysCard(multiDriver[3], "3C");

        // assert alert message popped up
        assertAlertMessage(multiDriver[3], 3, "Invalid Selection");

        // click OK button on the alert
        clickOKButton(multiDriver[3]);

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 draws 4C
        waitButtonToBeClickableAndClick(multiDriver[3], 3, "draw");

        // assert drawn card is 4C
        assertDrawnCard(multiDriver[3], "4C");

        // assert top card is 9S for all players
        assertTopCardForAllPlayers("9S");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 plays 7S
        playerPlaysCard(multiDriver[0], "7S");

        // assert top card is 7S for all players
        assertTopCardForAllPlayers("7S");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays 6S
        playerPlaysCard(multiDriver[1], "6S");

        // assert top card is 6S for all players
        assertTopCardForAllPlayers("6S");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 6C
        playerPlaysCard(multiDriver[2], "6C");

        // assert top card is 6C for all players
        assertTopCardForAllPlayers("6C");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 2C
        playerPlaysCard(multiDriver[3], "2C");

        // assert top card is 2C for all players
        assertTopCardForAllPlayers("2C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");

        // player 1 draws TC
        waitButtonToBeClickableAndClick(multiDriver[0], 3, "draw");

        // assert drawn card is TC
        assertDrawnCard(multiDriver[0], "TC");

        // assert drawn card is JC
        assertDrawnCard(multiDriver[0], "JC");

        // player 1 plays 7C
        playerPlaysCard(multiDriver[0], "7C");

        // assert top card is JC for all players
        assertTopCardForAllPlayers("7C");

        // assert player 2's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 2");

        // assert direction is left
        assertDirection("left");

        // assert player 2's draw button is enabled
        assertButtonEnabled(multiDriver[1], "draw");

        // player 2 plays KC
        playerPlaysCard(multiDriver[1], "KC");

        // assert top card is KC for all players
        assertTopCardForAllPlayers("KC");

        // assert player 3's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 3");

        // assert direction is left
        assertDirection("left");

        // assert player 3's draw button is enabled
        assertButtonEnabled(multiDriver[2], "draw");

        // player 3 plays 9C
        playerPlaysCard(multiDriver[2], "9C");

        // assert top card is 9C for all players
        assertTopCardForAllPlayers("9C");

        // assert player 4's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 4");

        // assert direction is left
        assertDirection("left");

        // assert player 4's draw button is enabled
        assertButtonEnabled(multiDriver[3], "draw");

        // player 4 plays 3C
        playerPlaysCard(multiDriver[3], "3C");

        // assert top card is 3C for all players
        assertTopCardForAllPlayers("3C");

        // assert player 1's turn for all players
        assertPlayerTurnForAllPlayers("Turn: 1");

        // assert direction is left
        assertDirection("left");

        // assert player 1's draw button is enabled
        assertButtonEnabled(multiDriver[0], "draw");
    }

    public void rigTestRow64() {
        String rigC = "4D 4H 7S 5D 6D 9D 4S 6S KC 8H TD 9S 6C 9C JD 3H 7D JH QH KH 5C 2C 3C 4C TC JC 7C AC AD 2D 3D 8D QD 7H KD 8C QC AH 2H 5H 6H 9H TH AS 2S 3S 5S 8S TS JS QS KS";
        parseCards(rigC);
    }
}
