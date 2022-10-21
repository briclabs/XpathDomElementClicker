package net.briclabs.xdec;

import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.thucydides.core.annotations.Steps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * A "test" which opens URLs and clicks target elements based on the content of the {@link #INPUT_FILE_NAME}.
 */
@ExtendWith(SerenityJUnit5Extension.class)
@SuppressWarnings("ClassNamingConvention")
public class XdecRunner {

    /** The name of the input file to be used by this program. */
    private static final String INPUT_FILE_NAME = "input-file.txt";
    /** File location must be in the root of the "test/resources" folder. */
    private static final URL INPUT_FILE = XdecRunner.class.getClassLoader().getResource(INPUT_FILE_NAME);

    /** The first line of the file is expected to contain the XPath of the element to be clicked. */
    private static final int XPATH_LINE_INDEX = 0;
    /** File is expected to have at least two lines: one containing the XPath, and subsequent lines for each URL. */
    private static final int MINIMUM_FILE_LINES = 2;

    private static final UrlValidator URL_VALIDATOR = UrlValidator.getInstance();

    private static final String ERROR_INVALID_FILE = "File found was either empty, or did not contain both the XPath and at least one URL.";
    private static final String ERROR_INVALID_URL = "URL [%s] was invalid.";
    private static final String ERROR_UNCLICKABLE_ELEMENT = "A clickable element could not be found with xpath [%s] on URL [%s].";

    @Steps
    XdecStepLibrary xdecStepLibrary;

    /**
     * The first line must be the XPath of the target element to be clicked.
     * The second line and onwards must be a{@link UrlValidator#isValid(String)} URL.
     *
     * @throws IOException in the event the file can't be read.
     */
    @Test
    public void runXdec() throws IOException {
        List<String> fileLines = INPUT_FILE == null
                ? Collections.emptyList()
                : readInputFile(INPUT_FILE.getFile());

        failOnInvalidFile(fileLines);
        runXdec(fileLines);
    }

    private void runXdec(List<String> fileLines) {
        final String xpath = pullOutXpathLine(fileLines);
        fileLines.forEach(url -> runXdecOnUrl(xpath, url));
    }

    private void runXdecOnUrl(String xpath, String url) {
        failOnInvalidUrl(url);

        xdecStepLibrary.openUrl(url);

        WebElementFacade targetElement = xdecStepLibrary.findTargetElement(xpath);
        failOnUnclickableElement(targetElement, xpath, url);

        xdecStepLibrary.clickElement(targetElement);

        xdecStepLibrary.waitOnPage().withTimeout(Duration.ofSeconds(30));
    }

    private static String pullOutXpathLine(List<String> fileLines) {
        final String xpath = fileLines.get(XPATH_LINE_INDEX);
        fileLines.remove(XPATH_LINE_INDEX);
        return xpath;
    }

    private List<String> readInputFile(String fileNameAndPath) throws IOException {
        return FileUtils.readLines(new File(fileNameAndPath), StandardCharsets.UTF_8);
    }

    private void failOnInvalidFile(List<String> fileLines) {
        if (CollectionUtils.isEmpty(fileLines) || fileLines.size() < MINIMUM_FILE_LINES) {
            Assertions.fail(ERROR_INVALID_FILE);
        }
    }

    private void failOnInvalidUrl(String url) {
        if (!URL_VALIDATOR.isValid(url)) {
            Assertions.fail(String.format(ERROR_INVALID_URL, url));
        }
    }

    private void failOnUnclickableElement(WebElementFacade targetElement, String xpath, String url) {
        if (targetElement == null || !targetElement.isClickable()) {
            Assertions.fail(String.format(ERROR_UNCLICKABLE_ELEMENT, xpath, url));
        }
    }
}
