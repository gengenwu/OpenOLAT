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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.ui.main.MemberListTableModel.Cols;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessedBinderSection;
import org.olat.modules.portfolio.model.SharedItemRow;
import org.olat.modules.portfolio.ui.SharedItemsDataModel.ShareItemCols;
import org.olat.modules.portfolio.ui.renderer.AssessmentEntryCellRenderer;
import org.olat.modules.portfolio.ui.renderer.SelectSectionsCellRenderer;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedItemsController extends FormBasicController implements Activateable2 {
	
	protected static final String USER_PROPS_ID = PortfolioHomeController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private SharedItemsDataModel model;
	private final TooledStackedPanel stackPanel;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private BinderController binderCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public SharedItemsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "shared_with_me");
		this.stackPanel = stackPanel;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		initForm(ureq);
		loadModel(null);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ShareItemCols.binderKey, "select"));
		
		SortKey defaultSortKey = null;
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.username));
			defaultSortKey = new SortKey(Cols.username.name(), true);
		}
		// followed by the users fields
		int colPos = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);

			String propName = userPropertyHandler.getName();
			if(defaultSortKey == null && i == 0) {
				defaultSortKey = new SortKey(propName, true);
			} else if(UserConstants.LASTNAME.equals(propName) && !isAdministrativeUser) {
				defaultSortKey = new SortKey(propName, true);
			}
			
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName));
			colPos++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.binderName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.courseName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.recentLaunch));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.openSections, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.selectSections, new SelectSectionsCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ShareItemCols.grading,
				new AssessmentEntryCellRenderer(getTranslator())));
		StaticFlexiCellRenderer selectRenderer = new StaticFlexiCellRenderer(translate("select"), "select");
		selectRenderer.setIconRightCSS("o_icon-sw o_icon_start");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "select", -1, "select", false, null,
				FlexiColumnModel.ALIGNMENT_LEFT, selectRenderer));
		
		model = new SharedItemsDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_binder_shared_items_listing");
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		tableEl.setAndLoadPersistedPreferences(ureq, "shared-items");
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		if(defaultSortKey != null) {
			options.setDefaultOrderBy(defaultSortKey);
		}
		tableEl.setSortSettings(options);
	}
	
	private void loadModel(String searchString) {
		List<AssessedBinder> assessedBinders = portfolioService.searchSharedBindersWith(getIdentity(), searchString);
		List<SharedItemRow> rows = new ArrayList<>(assessedBinders.size());
		for(AssessedBinder assessedBinder:assessedBinders) {
			SharedItemRow row = new SharedItemRow(assessedBinder.getAssessedIdentity(), userPropertyHandlers, getLocale());
			row.setBinderTitle(assessedBinder.getBinderTitle());
			row.setBinderKey(assessedBinder.getBinderKey());
			row.setLastModified(assessedBinder.getLastModified());
			row.setEntryDisplayName(assessedBinder.getEntryDisplayname());
			row.setAssessmentEntry(assessedBinder);
			row.setRecentLaunch(assessedBinder.getRecentLaunch());
			List<AssessedBinderSection> sections = assessedBinder.getSections();
			if(sections != null && sections.size() > 1) {
				Collections.sort(sections, new AssessedBinderSectionComparator());
			}
			row.setSections(sections);
			row.setNumOfOpenSections(assessedBinder.getNumOfOpenSections());
			rows.add(row);
		}
		
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries.isEmpty()) return;

		Long resId = entries.get(0).getOLATResourceable().getResourceableId();
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Binder".equalsIgnoreCase(resName)) {
			SharedItemRow activatedRow = null;
			List<SharedItemRow> rows = model.getObjects();
			for(SharedItemRow row:rows) {
				if(row.getBinderKey().equals(resId)) {
					activatedRow = row;
					break;
				}
			}
			
			if(activatedRow != null) {
				Activateable2 activeateable = doSelectBinder(ureq, activatedRow);
				if(activeateable != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					activeateable.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				SharedItemRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					Activateable2 activeateable = doSelectBinder(ureq, row);
					if(activeateable != null) {
						activeateable.activate(ureq, null, null);
					}
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				loadModel(se.getSearch());
			} else if("ONCLICK".equals(event.getCommand())) {
				String row = ureq.getParameter("select-section");
				String expand = ureq.getParameter("expand-section");
				if(StringHelper.isLong(row)) {
					doSelectSection(ureq);
				} else if(StringHelper.containsNonWhitespace(expand)) {
					doExpandSections(ureq);
				}
				
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doExpandSections(UserRequest ureq) {
		try {
			String row = ureq.getParameter("expand-section");
			int index = Integer.parseInt(row);
			SharedItemRow itemRow = model.getObject(index);
			if(itemRow != null) {
				itemRow.setExpandSections(!itemRow.isExpandSections());
			}
			tableEl.getComponent().setDirty(true);
		} catch (NumberFormatException e) {
			logError("", e);
		}
	}
	
	private void doSelectSection(UserRequest ureq) {
		try {
			String row = ureq.getParameter("select-section");
			int index = Integer.parseInt(row);
			SharedItemRow itemRow = model.getObject(index);
			if(itemRow != null) {
				String sectionParam = ureq.getParameter("section");
				int sectionIndex = Integer.parseInt(sectionParam);
				AssessedBinderSection section = itemRow.getSections().get(sectionIndex);

				Activateable2 activeateable = doSelectBinder(ureq, itemRow);
				if(activeateable != null) {
					List<ContextEntry> entries = new ArrayList<>(2);
					entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance("Entries", 0l)));
					entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance("Section", section.getSectionKey())));
					activeateable.activate(ureq, entries, null);
				}
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	private BinderController doSelectBinder(UserRequest ureq, SharedItemRow row) {
		Binder binder = portfolioService.getBinderByKey(row.getBinderKey());
		if(binder == null) {
			showWarning("warning.portfolio.not.found");
			return null;
		} else {
			removeAsListenerAndDispose(binderCtrl);

			portfolioService.updateBinderUserInformations(binder, getIdentity());
			row.setRecentLaunch(new Date());
			OLATResourceable binderOres = OresHelper.createOLATResourceableInstance("Binder", binder.getKey());
			WindowControl swControl = addToHistory(ureq, binderOres, null);
			List<AccessRights> rights = portfolioService.getAccessRights(binder, getIdentity());
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForCoach(binder, rights);
			BinderConfiguration config = BinderConfiguration.createConfig(binder);
			binderCtrl = new BinderController(ureq, swControl, stackPanel, secCallback, binder, config);
			String displayName = StringHelper.escapeHtml(binder.getTitle());
			stackPanel.pushController(displayName, binderCtrl);
			return binderCtrl;
		}
	}
	
	private static class AssessedBinderSectionComparator implements Comparator<AssessedBinderSection> {

		@Override
		public int compare(AssessedBinderSection o1, AssessedBinderSection o2) {
			boolean ba = (o1 == null);
			boolean bb = (o2 == null);
			int c = ba ? (bb ? 0: -1) : (bb ? 1: 0);
			if(c == 0) {
				int p1 = o1.getPos();
				int p2 = o2.getPos();
				c = Integer.compare(p1, p2);
			}
			return c;
		}
	}
}