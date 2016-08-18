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
package org.olat.modules.portfolio.ui.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageRow {
	
	private final Page page;
	private final Section section;
	private final boolean assessable;
	private boolean firstPageOfSection;
	private final AssessmentSection assessmentSection;
	
	private Collection<String> pageCategories;
	private Collection<String> sectionCategories;
	private List<PageAssignmentRow> assignments;
	
	private Link openLink;
	private FormLink openFormLink, newFloatingEntryLink, newEntryLink, newAssignmentLink;
	
	private FormLink closeSectionLink, reopenSectionLink;
	
	private long numOfComments;
	private Link commentLink;
	private FormLink commentFormLink;
	
	private String metaSectionTitle;
	private String metaBinderTitle;
	
	public PageRow(Page page, Section section, AssessmentSection assessmentSection,
			boolean firstPageOfSection, boolean assessable) {
		this.page = page;
		this.section = section;
		this.assessable = assessable;
		this.assessmentSection = assessmentSection;
		this.firstPageOfSection = firstPageOfSection;
	}
	
	public boolean isPage() {
		return page != null;
	}
	
	public Long getKey() {
		return page == null ? null : page.getKey();
	}
	
	public Page getPage() {
		return page;
	}
	
	public String getTitle() {
		return page == null ? null : page.getTitle();
	}
	
	public String getSummary() {
		return page.getSummary();
	}
	
	public Date getLastModified() {
		return page.getLastModified();
	}
	
	public Date getCreationDate() {
		return page.getCreationDate();
	}
	
	public PageStatus getPageStatus() {
		return page == null ? null : page.getPageStatus();
	}
	
	public Date getLastPublicationDate() {
		return page == null ? null : page.getLastPublicationDate();
	}
	
	public String getCssClassStatus() {
		return page.getPageStatus() == null
				? PageStatus.draft.cssClass() : page.getPageStatus().cssClass();
	}
	
	public Section getSection() {
		return section;
	}
	
	public String getSectionTitle() {
		return section == null ? null : section.getTitle();
	}
	
	public boolean isSection () {
		if(section == null) {
			return false;
		}
		return true;
	}
	
	public String getSectionStatusI18nKey() {
		if(section == null || section.getSectionStatus() == null) {
			return SectionStatus.notStarted.i18nKey();
		}
		return section.getSectionStatus().i18nKey();
	}
	
	public String getSectionCssClassStatus() {
		if(section == null) {
			return null;
		}
		return section.getSectionStatus() == null ? SectionStatus.notStarted.cssClass() : section.getSectionStatus().cssClass();
	}
	
	public String getSectionLongTitle() {
		return section.getTitle();
	}
	
	public Date getSectionBeginDate() {
		return section.getBeginDate();
	}
	
	public Date getSectionEndDate() {
		return section.getEndDate();
	}
	
	public String getSectionDescription() {
		return section.getDescription();
	}

	public boolean isFirstPageOfSection() {
		return firstPageOfSection;
	}
	
	public void setFirstPageOfSection(boolean firstPageOfSection) {
		this.firstPageOfSection = firstPageOfSection;
	}
	
	public Collection<String> getPageCategories() {
		return pageCategories;
	}

	public void setPageCategories(Collection<String> pageCategories) {
		this.pageCategories = pageCategories;
	}
	
	public Collection<String> getSectionCategories() {
		return sectionCategories;
	}

	public void setSectionCategories(Collection<String> sectionCategories) {
		this.sectionCategories = sectionCategories;
	}

	public boolean isAssessable() {
		return assessable;
	}
	
	public boolean hasScore() {
		return assessable && assessmentSection != null && assessmentSection.getScore() != null;
	}
	
	public String getScore() {
		if(assessmentSection != null && assessmentSection.getScore() != null) {
			return AssessmentHelper.getRoundedScore(assessmentSection.getScore());
		}
		return "";
	}
	
	public FormLink getNewEntryLink() {
		return newEntryLink;
	}
	
	public void setNewEntryLink(FormLink newEntryLink) {
		this.newEntryLink = newEntryLink;
	}
	
	public FormLink getNewFloatingEntryLink() {
		return newFloatingEntryLink;
	}

	public void setNewFloatingEntryLink(FormLink newFloatingEntryLink) {
		this.newFloatingEntryLink = newFloatingEntryLink;
	}
	
	public FormLink getOpenFormItem() {
		return openFormLink;
	}

	public void setOpenFormLink(FormLink openFormLink) {
		this.openFormLink = openFormLink;
	}
	
	public Link getOpenLink() {
		return openLink;
	}
	
	public void setOpenLink(Link openLink) {
		this.openLink = openLink;
	}

	public long getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(long numOfComments) {
		this.numOfComments = numOfComments;
	}

	public Link getCommentLink() {
		return commentLink;
	}

	public void setCommentLink(Link commentLink) {
		this.commentLink = commentLink;
	}

	public FormLink getCommentFormLink() {
		return commentFormLink;
	}

	public void setCommentFormLink(FormLink commentFormLink) {
		this.commentFormLink = commentFormLink;
	}
	
	public boolean hasMetaBinderAndSectionTitle() {
		return StringHelper.containsNonWhitespace(metaSectionTitle) || StringHelper.containsNonWhitespace(metaBinderTitle);
	}

	public String[] getMetaBinderAndSectionTitles() {
		return new String[]{ metaBinderTitle, metaSectionTitle };
	}

	public void setMetaSectionTitle(String metaSectionTitle) {
		this.metaSectionTitle = metaSectionTitle;
	}
	
	public void setMetaBinderTitle(String metaBinderTitle) {
		this.metaBinderTitle = metaBinderTitle;
	}
	
	public List<PageAssignmentRow> getAssignments() {
		return assignments;
	}

	public void setAssignments(List<PageAssignmentRow> assignments) {
		this.assignments = assignments;
	}
	
	public FormLink getNewAssignmentLink() {
		return newAssignmentLink;
	}
	
	public void setNewAssignmentLink(FormLink newAssignmentLink) {
		this.newAssignmentLink = newAssignmentLink;
	}

	public FormLink getCloseSectionLink() {
		return closeSectionLink;
	}

	public void setCloseSectionLink(FormLink closeSectionLink) {
		this.closeSectionLink = closeSectionLink;
	}

	public FormLink getReopenSectionLink() {
		return reopenSectionLink;
	}

	public void setReopenSectionLink(FormLink reopenSectionLink) {
		this.reopenSectionLink = reopenSectionLink;
	}
}
