package net.briclabs.xdec;

import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.core.steps.UIInteractions;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;

public class XdecStepLibrary extends UIInteractions {

    @Step
    public void clickElement(WebElementFacade targetElement) {
        targetElement.click();
    }

    @Step
    public WebElementFacade findTargetElement(String xpath) {
        return findAll(By.xpath(xpath)).get(0);
    }
}
