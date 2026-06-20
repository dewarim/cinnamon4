package com.dewarim.cinnamon.test.integration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UiIntegrationTest extends CinnamonIntegrationTest {

    private static final String BASE_URL   = "http://localhost:" + cinnamonTestPort;
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin";

    private WebDriver    driver;
    private WebDriverWait wait;

    /** IDs tracked across ordered tests */
    private long createdDocOsdId;
    private long uploadedImageOsdId;
    private long copiedDocOsdId;

    // ─── Browser lifecycle ────────────────────────────────────────────────────

    @BeforeAll
    void setUpBrowser() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");
        options.addArguments("--width=1280");
        options.addArguments("--height=900");
        driver = new FirefoxDriver(options);
        wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    void tearDownBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void login(String username, String password) {
        driver.get(BASE_URL + "/ui/login");
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/folders"));
    }

    private void loginAsAdmin() {
        login(ADMIN_USER, ADMIN_PASS);
    }

    /** Wait for HTMX to inject the folder content panel (level div always present). */
    private void waitForContentPanel() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#folder-content-panel .level")));
    }

    /** Wait for HTMX to inject the folder tree panel (aside.menu always present). */
    private void waitForTreePanel() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#folder-tree-panel .menu")));
    }

    /** Accept a native browser confirm() alert. */
    private void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }

    /**
     * Remove HTML5 required/pattern constraints from a form so we can test
     * server-side validation by submitting with empty required fields.
     */
    private void bypassHtml5Validation(WebElement form) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (WebElement el : form.findElements(By.cssSelector("[required]"))) {
            js.executeScript("arguments[0].removeAttribute('required')", el);
        }
        for (WebElement el : form.findElements(By.cssSelector("[pattern]"))) {
            js.executeScript("arguments[0].removeAttribute('pattern')", el);
        }
    }

    /** Extract a numeric query-parameter value from the current URL. */
    private long extractIdFromUrl(String param) {
        String url = driver.getCurrentUrl();
        int idx = url.indexOf(param + "=");
        if (idx < 0) return -1;
        String rest = url.substring(idx + param.length() + 1);
        int end = rest.indexOf('&');
        try {
            return Long.parseLong(end < 0 ? rest : rest.substring(0, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * In the HTMX-loaded content panel, find the "Edit" link in the subfolder row
     * whose folder link href contains the given folder path.
     */
    private WebElement findEditBtnForFolder(String folderPath) {
        String xp = String.format(
                "//a[contains(@href,'folderPath=%s')]/ancestor::tr//a[text()='Edit']",
                folderPath);
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xp)));
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void basicUiTest() {
        loginAsAdmin();

        // Verify 3-column layout panel elements exist immediately after login
        assertNotNull(driver.findElement(By.id("folder-tree-panel")));
        assertNotNull(driver.findElement(By.id("folder-content-panel")));
        assertNotNull(driver.findElement(By.id("osd-meta-panel")));

        // Wait for HTMX to load the folder tree
        waitForTreePanel();

        // Logout and verify redirect to login page
        driver.findElement(By.linkText("Logout")).click();
        wait.until(ExpectedConditions.urlContains("/ui/login"));
        assertTrue(driver.getCurrentUrl().contains("/ui/login"));
    }

    @Test
    @Order(2)
    void createFolderTest() {
        loginAsAdmin();
        waitForContentPanel();

        // Click + Folder button (inside HTMX-loaded content panel)
        WebElement folderBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("+ Folder")));
        folderBtn.click();
        wait.until(ExpectedConditions.urlContains("/ui/folder/create"));
        assertTrue(driver.getCurrentUrl().contains("parentId="));

        // Submit with blank name after bypassing HTML5 validation → server-side error
        WebElement form = driver.findElement(By.cssSelector("form[action='/ui/folder/create']"));
        bypassHtml5Validation(form);
        form.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".notification.is-danger")));

        // Fill name and submit successfully
        driver.findElement(By.name("name")).sendKeys("test-folder");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/folders"));
        assertTrue(driver.getCurrentUrl().contains("test-folder"),
                "Redirect URL should contain folder name; got: " + driver.getCurrentUrl());
    }

    @Test
    @Order(3)
    void editFolderTest() {
        loginAsAdmin();

        // Navigate to home folder so test-folder appears as a subfolder
        driver.get(BASE_URL + "/ui/folders?folderPath=/home/admin");
        waitForContentPanel();

        // Click Edit for test-folder
        findEditBtnForFolder("/home/admin/test-folder").click();
        wait.until(ExpectedConditions.urlContains("/ui/folder/edit"));
        assertTrue(driver.getCurrentUrl().contains("id="));

        // Rename to test-folder-renamed and save
        WebElement nameInput = driver.findElement(By.name("name"));
        nameInput.clear();
        nameInput.sendKeys("test-folder-renamed");
        driver.findElement(By.cssSelector("button.is-primary[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/folder/edit"));

        // Verify updated name shown in the edit form
        assertEquals("test-folder-renamed",
                driver.findElement(By.name("name")).getAttribute("value"));

        // Delete with both checkboxes checked
        driver.findElement(By.cssSelector("input[name='deleteContent']")).click();
        driver.findElement(By.cssSelector("input[name='deleteRecursively']")).click();
        driver.findElement(By.cssSelector("button.is-danger[type='submit']")).click();
        acceptAlert();
        wait.until(ExpectedConditions.urlContains("/ui/folders"));
        waitForContentPanel();

        // Verify test-folder-renamed no longer appears in content panel
        List<WebElement> remaining = driver.findElements(
                By.xpath("//*[contains(@href,'test-folder-renamed')]"));
        assertTrue(remaining.isEmpty(), "Deleted folder should no longer appear");
    }

    @Test
    @Order(4)
    void createDocumentTest() {
        loginAsAdmin();
        waitForContentPanel();

        // Click + Document
        WebElement docBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("+ Document")));
        docBtn.click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/create"));
        assertTrue(driver.getCurrentUrl().contains("parentId="));

        // Submit with blank name → server-side error
        WebElement form = driver.findElement(By.cssSelector("form[action='/ui/osd/create']"));
        bypassHtml5Validation(form);
        form.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".notification.is-danger")));

        // Fill form fields and submit
        driver.findElement(By.name("name")).sendKeys("hello-world");
        new Select(driver.findElement(By.name("formatId")))
                .selectByVisibleText("plaintext (text/plain)");
        driver.findElement(By.name("textContent")).sendKeys("hello world");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/view"));

        createdDocOsdId = extractIdFromUrl("id");
        assertTrue(createdDocOsdId > 0, "Created document ID should be positive");

        // Verify document name in title
        assertEquals("hello-world",
                driver.findElement(By.cssSelector(".title.is-4")).getText());

        // Verify text content loaded via HTMX into <pre>
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector("pre"), "hello world"));
    }

    @Test
    @Order(5)
    void uploadImageTest() {
        loginAsAdmin();
        waitForContentPanel();

        // Click + Document
        WebElement docBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("+ Document")));
        docBtn.click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/create"));

        // Fill in name and format
        driver.findElement(By.name("name")).sendKeys("cinnamon-bun");
        new Select(driver.findElement(By.name("formatId")))
                .selectByVisibleText("image.png (image/png)");

        // Upload the test image
        File imageFile = new File("src/test/resources/examples/cinnamon-bun.png").getAbsoluteFile();
        assertTrue(imageFile.exists(), "Test image must exist: " + imageFile);
        driver.findElement(By.name("file")).sendKeys(imageFile.getAbsolutePath());

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/view"));

        uploadedImageOsdId = extractIdFromUrl("id");
        assertTrue(uploadedImageOsdId > 0, "Uploaded image OSD ID should be positive");

        // Verify image rendered with <img> tag (not a download link)
        WebElement img = driver.findElement(By.cssSelector(".image img"));
        assertNotNull(img);
        assertTrue(img.getAttribute("src").contains("/ui/content/"),
                "img src should point to /ui/content/");

        // Verify content size shown in metadata column
        WebElement sizeCell = driver.findElement(
                By.xpath("//th[text()='Size']/following-sibling::td"));
        assertFalse(sizeCell.getText().isBlank(), "Content size should be shown");
        assertTrue(sizeCell.getText().contains("B"), "Size should include bytes unit");
    }

    @Test
    @Order(6)
    void documentViewTest() {
        assumeTrue(createdDocOsdId > 0, "createDocumentTest must have run");
        loginAsAdmin();

        driver.get(BASE_URL + "/ui/osd/view?id=" + createdDocOsdId);
        wait.until(ExpectedConditions.urlContains("/ui/osd/view"));

        // Verify Edit, Copy, Version, Delete buttons present
        assertNotNull(driver.findElement(By.linkText("Edit")));
        assertNotNull(driver.findElement(By.cssSelector("form[action='/ui/osd/copy'] button")));
        assertNotNull(driver.findElement(By.cssSelector("form[action='/ui/osd/version'] button")));
        assertNotNull(driver.findElement(By.cssSelector("form[action='/ui/osd/delete'] button")));

        // Download button only present when document has content (hello-world has text content)
        List<WebElement> downloadLinks = driver.findElements(
                By.cssSelector("a[href*='/ui/content/']"));
        assertFalse(downloadLinks.isEmpty(), "Download should be present for doc with content");

        // Click Edit → verify redirect to edit page
        driver.findElement(By.linkText("Edit")).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/edit"));
        assertTrue(driver.getCurrentUrl().contains("id="));
    }

    @Test
    @Order(7)
    void documentEditTest() {
        assumeTrue(createdDocOsdId > 0, "createDocumentTest must have run");
        loginAsAdmin();

        driver.get(BASE_URL + "/ui/osd/edit?id=" + createdDocOsdId);
        wait.until(ExpectedConditions.urlContains("/ui/osd/edit"));

        // Verify form pre-filled: name matches
        WebElement nameInput = driver.findElement(By.name("name"));
        assertEquals("hello-world", nameInput.getAttribute("value"));

        // Verify ACL and owner selects are populated
        assertFalse(new Select(driver.findElement(By.name("aclId"))).getOptions().isEmpty());
        assertFalse(new Select(driver.findElement(By.name("ownerId"))).getOptions().isEmpty());

        // Change name and save
        nameInput.clear();
        nameInput.sendKeys("hello-world-edited");
        driver.findElement(By.cssSelector("button.is-primary[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/edit"));

        // Verify updated name in title
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".title.is-4"), "hello-world-edited"));

        // Verify header action buttons: View, Copy, Version, Delete (Download conditional)
        assertNotNull(driver.findElement(By.linkText("View")));
        assertNotNull(driver.findElement(By.cssSelector("form[action='/ui/osd/copy'] button")));
        assertNotNull(driver.findElement(By.cssSelector("form[action='/ui/osd/version'] button")));
        assertNotNull(driver.findElement(By.cssSelector("form[action='/ui/osd/delete'] button")));

        // Verify Details table shows Created and Modified timestamps
        WebElement createdCell = driver.findElement(
                By.xpath("//th[text()='Created']/following-sibling::td"));
        assertFalse(createdCell.getText().isBlank(), "Created timestamp should be shown");

        WebElement modifiedCell = driver.findElement(
                By.xpath("//th[text()='Modified']/following-sibling::td"));
        assertFalse(modifiedCell.getText().isBlank(), "Modified timestamp should be shown");

        // Verify Format row in Details table
        WebElement formatCell = driver.findElement(
                By.xpath("//th[text()='Format']/following-sibling::td"));
        assertTrue(formatCell.getText().contains("text/plain"),
                "Format should show text/plain content type");

        // Restore original name for subsequent tests
        driver.findElement(By.name("name")).clear();
        driver.findElement(By.name("name")).sendKeys("hello-world");
        driver.findElement(By.cssSelector("button.is-primary[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/edit"));
    }

    @Test
    @Order(8)
    void documentCopyTest() {
        assumeTrue(createdDocOsdId > 0, "createDocumentTest must have run");
        loginAsAdmin();

        driver.get(BASE_URL + "/ui/osd/view?id=" + createdDocOsdId);
        wait.until(ExpectedConditions.urlContains("/ui/osd/view"));

        // Click Copy → redirected to edit page of the copy
        driver.findElement(By.cssSelector("form[action='/ui/osd/copy'] button")).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/edit"));
        copiedDocOsdId = extractIdFromUrl("id");
        assertTrue(copiedDocOsdId > 0);
        assertNotEquals(createdDocOsdId, copiedDocOsdId, "Copy must have a different ID");

        // Verify name starts with "Copy_"
        String copyName = driver.findElement(By.cssSelector(".title.is-4")).getText();
        assertTrue(copyName.startsWith("Copy_"),
                "Copied document name should start with 'Copy_', was: " + copyName);
    }

    @Test
    @Order(9)
    void documentVersionTest() {
        assumeTrue(createdDocOsdId > 0, "createDocumentTest must have run");
        loginAsAdmin();

        driver.get(BASE_URL + "/ui/osd/view?id=" + createdDocOsdId);
        wait.until(ExpectedConditions.urlContains("/ui/osd/view"));

        String versionBefore = driver.findElement(By.cssSelector(".subtitle.is-6")).getText();

        // Click Version → accept confirm → redirected to edit page of new version
        driver.findElement(By.cssSelector("form[action='/ui/osd/version'] button")).click();
        acceptAlert();
        wait.until(ExpectedConditions.urlContains("/ui/osd/edit"));

        long newVersionId = extractIdFromUrl("id");
        assertTrue(newVersionId > 0);
        assertNotEquals(createdDocOsdId, newVersionId, "New version must have a different ID");

        // Verify version label changed
        String versionAfter = driver.findElement(By.cssSelector(".subtitle.is-6")).getText();
        assertNotEquals(versionBefore, versionAfter,
                "Version text should have changed after versioning");

        // Navigate to parent folder via subtitle folder link
        driver.findElement(By.cssSelector(".subtitle.is-6 a")).click();
        wait.until(ExpectedConditions.urlContains("/ui/folders"));
        waitForContentPanel();

        // Switch to "All versions" filter (inside HTMX-loaded content)
        WebElement filterEl = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.name("filter")));
        new Select(filterEl).selectByValue("all");
        wait.until(ExpectedConditions.urlContains("filter=all"));
        waitForContentPanel();

        // Verify both versions of hello-world appear
        List<WebElement> versionRows = driver.findElements(
                By.xpath("//td[text()='hello-world']"));
        assertTrue(versionRows.size() >= 2,
                "Expected at least 2 versions of hello-world but found: " + versionRows.size());
    }

    @Test
    @Order(10)
    void deleteDocumentTest() {
        assumeTrue(copiedDocOsdId > 0, "documentCopyTest must have run");
        loginAsAdmin();

        // Delete the copy so the original is preserved for later tests
        driver.get(BASE_URL + "/ui/osd/view?id=" + copiedDocOsdId);
        wait.until(ExpectedConditions.urlContains("/ui/osd/view"));

        driver.findElement(By.cssSelector("form[action='/ui/osd/delete'] button")).click();
        acceptAlert();
        wait.until(ExpectedConditions.urlContains("/ui/folders"));
        waitForContentPanel();

        // Verify redirect landed on a folder view (not an OSD view)
        assertFalse(driver.getCurrentUrl().contains("/ui/osd/view"),
                "Should have redirected to folder view");

        // Verify the deleted copy no longer appears in the folder content
        List<WebElement> copyLinks = driver.findElements(
                By.xpath("//td[starts-with(text(),'Copy_')]"));
        assertTrue(copyLinks.isEmpty(), "Deleted copy should not appear in folder content");
    }

    @Test
    @Order(11)
    void contentStreamingTest() throws Exception {
        assumeTrue(uploadedImageOsdId > 0, "uploadImageTest must have run");
        loginAsAdmin();

        // Get session cookie value from browser
        Cookie sessionCookie = driver.manage().getCookieNamed("cinnamonTicket");
        assertNotNull(sessionCookie, "Session cookie must be set after login");
        String ticketValue = sessionCookie.getValue();

        // Verify authenticated content streaming returns image/png
        URL contentUrl = new URL(BASE_URL + "/ui/content/" + uploadedImageOsdId);
        HttpURLConnection conn = (HttpURLConnection) contentUrl.openConnection();
        conn.setRequestProperty("Cookie", "cinnamonTicket=" + ticketValue);
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        assertEquals(200, conn.getResponseCode(),
                "Authenticated content request should return 200");
        assertTrue(conn.getContentType() != null && conn.getContentType().contains("image/png"),
                "Content-Type should be image/png, was: " + conn.getContentType());
        assertTrue(conn.getContentLength() > 0 || conn.getContentLengthLong() > 0,
                "Response body should not be empty");
        conn.disconnect();

        // Verify unauthenticated access redirects to login
        driver.manage().deleteAllCookies();
        driver.get(BASE_URL + "/ui/content/" + uploadedImageOsdId);
        wait.until(ExpectedConditions.urlContains("/ui/login"));
        assertTrue(driver.getCurrentUrl().contains("/ui/login"),
                "Unauthenticated content access should redirect to login");
    }

    @Test
    @Order(12)
    void errorHandlingTest() {
        loginAsAdmin();
        waitForContentPanel();

        // ── 1. Non-existent OSD redirects to folder view ──────────────────────
        driver.get(BASE_URL + "/ui/osd/view?id=99999");
        wait.until(ExpectedConditions.urlContains("/ui/folders"));
        assertFalse(driver.getCurrentUrl().contains("/ui/osd/view"),
                "Non-existent OSD should redirect to folder view");

        // ── 2. Duplicate folder name shows error ──────────────────────────────
        driver.get(BASE_URL + "/ui/folders?folderPath=/home/admin");
        waitForContentPanel();

        // Create first "dup-folder"
        wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("+ Folder"))).click();
        wait.until(ExpectedConditions.urlContains("/ui/folder/create"));
        driver.findElement(By.name("name")).sendKeys("dup-folder");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/folders"));

        // Navigate back to home and try to create another "dup-folder"
        driver.get(BASE_URL + "/ui/folders?folderPath=/home/admin");
        waitForContentPanel();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("+ Folder"))).click();
        wait.until(ExpectedConditions.urlContains("/ui/folder/create"));
        driver.findElement(By.name("name")).sendKeys("dup-folder");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        // Server rejects duplicate → redirect back to create form with error
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".notification.is-danger")));

        // ── 3. Delete folder with content without "Delete content" shows error ─
        driver.get(BASE_URL + "/ui/folders?folderPath=/home/admin");
        waitForContentPanel();

        // Create "del-content-test" folder
        wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("+ Folder"))).click();
        wait.until(ExpectedConditions.urlContains("/ui/folder/create"));
        driver.findElement(By.name("name")).sendKeys("del-content-test");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("del-content-test"));
        waitForContentPanel();

        // Create a document inside the new folder
        wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("+ Document"))).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/create"));
        driver.findElement(By.name("name")).sendKeys("inner-doc");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/ui/osd/view"));

        // Navigate to home, find and edit del-content-test folder
        driver.get(BASE_URL + "/ui/folders?folderPath=/home/admin");
        waitForContentPanel();
        findEditBtnForFolder("/home/admin/del-content-test").click();
        wait.until(ExpectedConditions.urlContains("/ui/folder/edit"));

        // Try to delete without checking "Delete content"
        driver.findElement(By.cssSelector("button.is-danger[type='submit']")).click();
        acceptAlert();
        // Server fails with FOLDER_IS_NOT_EMPTY → redirect back to edit with error
        wait.until(ExpectedConditions.urlContains("error="));
        assertNotNull(driver.findElement(By.cssSelector(".notification.is-danger")),
                "Error notification should appear when deleting folder with content");
    }
}
