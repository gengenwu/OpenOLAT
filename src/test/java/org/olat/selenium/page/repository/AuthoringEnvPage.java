/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.repository;

import java.io.File;

import org.junit.Assert;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseWizardPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page to control the author environnment.
 * 
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEnvPage {
	
	public static final By createMenuCaretBy = By.cssSelector("a.o_sel_author_create");
	public static final By createMenuBy = By.cssSelector("ul.o_sel_author_create");
	
	private WebDriver browser;
	
	public AuthoringEnvPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check that the segment for the "Search" in author environment is selected.
	 * 
	 * @return
	 */
	public AuthoringEnvPage assertOnGenericSearch() {
		By genericSearchBy = By.xpath("//div[contains(@class,'o_segments')]//a[contains(@class,'btn-primary')][contains(@onclick,'search.generic')]");
		OOGraphene.waitElement(genericSearchBy, 5, browser);
		WebElement genericSearchSegment = browser.findElement(genericSearchBy);
		Assert.assertTrue(genericSearchSegment.isDisplayed());
		return this;
	}
	
	public RepositoryEditDescriptionPage createCP(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.cp)
			.fillCreateForm(title)
			.assertOnGeneralTab();
	}
	
	public RepositoryEditDescriptionPage createWiki(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.wiki)
			.fillCreateForm(title)
			.assertOnGeneralTab();
	}
	
	public RepositoryEditDescriptionPage createCourse(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnGeneralTab();
	}
	
	public RepositoryEditDescriptionPage createPortfolioBinder(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.portfolio)
			.fillCreateForm(title)
			.assertOnGeneralTab();
	}
	
	public RepositoryEditDescriptionPage createQTI21Test(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.qti21Test)
			.fillCreateForm(title)
			.assertOnGeneralTab();
	}
	
	/**
	 * Open the drop-down to create a new resource.
	 * @return
	 */
	public AuthoringEnvPage openCreateDropDown() {
		WebElement createMenuCaret = browser.findElement(createMenuCaretBy);
		Assert.assertTrue(createMenuCaret.isDisplayed());
		createMenuCaret.click();
		OOGraphene.waitElement(createMenuBy, 5, browser);
		return this;
	}

	/**
	 * Click the link to create a learning resource in the create drop-down
	 * @param type
	 * @return
	 */
	public AuthoringEnvPage clickCreate(ResourceType type) {
		WebElement createMenu = browser.findElement(createMenuBy);
		Assert.assertTrue(createMenu.isDisplayed());
		WebElement createLink = createMenu.findElement(By.className("o_sel_author_create-" + type.type()));
		Assert.assertTrue(createLink.isDisplayed());
		createLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Fill the create form and submit
	 * @param displayName
	 * @return
	 */
	public RepositoryEditDescriptionPage fillCreateForm(String displayName) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		By submitBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(RepositoryEditDescriptionPage.generaltabBy, browser);
		return new RepositoryEditDescriptionPage(browser)
				.assertOnGeneralTab();
	}
	
	/**
	 * Fill the create form and start the wizard
	 * @param displayName
	 * @return
	 */
	public CourseWizardPage fillCreateFormAndStartWizard(String displayName) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		By createBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_wizard");
		browser.findElement(createBy).click();
		OOGraphene.waitBusy(browser);
		return CourseWizardPage.getWizard(browser);
	}
	
	/**
	 * Short cut to create quickly a course
	 * @param title
	 */
	public void quickCreateCourse(String title) {
		RepositoryEditDescriptionPage editDescription = openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnGeneralTab();
			
		//from description editor, back to details and launch the course
		editDescription
			.clickToolbarBack();
	}
	
	public AuthoringEnvPage uploadResource(String title, File resource) {
		WebElement importLink = browser.findElement(By.className("o_sel_author_import"));
		Assert.assertTrue(importLink.isDisplayed());
		importLink.click();
		OOGraphene.waitBusy(browser);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, resource, browser);
		OOGraphene.waitElement(By.className("o_sel_author_imported_name"), browser);
		
		By titleBy = By.cssSelector(".o_sel_author_imported_name input");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.sendKeys(title);
		
		//save
		By saveBy = By.cssSelector("div.o_sel_repo_save_details button.btn-primary");
		WebElement saveButton = browser.findElement(saveBy);
		saveButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AuthoringEnvPage assertOnResourceType() {
		By typeEl = By.cssSelector(".o_sel_author_type");
		OOGraphene.waitElement(typeEl, 5, browser);
		return this;
	}
	
	public void selectResource(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_coursetable')]//a[contains(text(),'" + title + "')]");
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	public void editResource(String title) {
		By editBy = By.xpath("//div[contains(@class,'o_coursetable')]//tr[//a[contains(text(),'" + title + "')]]//a[contains(@href,'edit')]");
		browser.findElement(editBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	/**
	 * Click back from the editor
	 * 
	 * @return
	 */
	public CoursePageFragment clickToolbarRootCrumb() {
		OOGraphene.closeBlueMessageWindow(browser);
		By toolbarBackBy = By.xpath("//li[contains(@class,'o_breadcrumb_back')]/following-sibling::li/a");
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return new CoursePageFragment(browser);
	}
	
	public enum ResourceType {
		course("CourseModule"),
		cp("FileResource.IMSCP"),
		wiki("FileResource.WIKI"),
		portfolio("BinderTemplate"),
		qti21Test("FileResource.IMSQTI21");
		
		private final String type;
		
		private ResourceType(String type) {
			this.type = type;
		}
		
		public String type() {
			return type;
		}
	}
}
