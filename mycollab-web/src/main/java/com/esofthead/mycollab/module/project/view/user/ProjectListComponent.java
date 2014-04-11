/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.project.view.user;

import java.util.List;

import org.vaadin.hene.popupbutton.PopupButton;

import com.esofthead.mycollab.core.arguments.SearchField;
import com.esofthead.mycollab.core.arguments.SearchRequest;
import com.esofthead.mycollab.core.arguments.SetSearchField;
import com.esofthead.mycollab.core.arguments.StringSearchField;
import com.esofthead.mycollab.eventmanager.EventBus;
import com.esofthead.mycollab.module.project.CurrentProjectVariables;
import com.esofthead.mycollab.module.project.ProjectStatusConstants;
import com.esofthead.mycollab.module.project.domain.SimpleProject;
import com.esofthead.mycollab.module.project.domain.criteria.ProjectSearchCriteria;
import com.esofthead.mycollab.module.project.events.ProjectEvent;
import com.esofthead.mycollab.module.project.service.ProjectService;
import com.esofthead.mycollab.module.project.view.parameters.ProjectScreenData;
import com.esofthead.mycollab.spring.ApplicationContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.mvp.PageActionChain;
import com.esofthead.mycollab.vaadin.ui.BeanList;
import com.esofthead.mycollab.vaadin.ui.MyCollabResource;
import com.esofthead.mycollab.vaadin.ui.UIConstants;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author MyCollab Ltd.
 * @since 4.0
 * 
 */
public class ProjectListComponent extends VerticalLayout {
	private static final long serialVersionUID = 6930971885172125913L;

	final private PopupButton headerLayout;
	private Label componentHeader;

	private VerticalLayout contentLayout;

	private ProjectPagedList projectList;

	private Label projectDesc;

	public ProjectListComponent() {
		super();
		setWidth("100%");
		setSpacing(true);
		setStyleName("project-list-comp");

		headerLayout = new PopupButton();
		headerLayout.setStyleName("project-list-comp-hdr");
		headerLayout.setWidth("100%");

		componentHeader = new Label();
		componentHeader.setStyleName("h2");

		headerLayout.setIcon(MyCollabResource
				.newResource("icons/project_dropdown.png"));
		addComponent(headerLayout);

		contentLayout = new VerticalLayout();
		contentLayout.setStyleName("project-list-comp-content");
		contentLayout.setWidth("234px");

		projectList = new ProjectPagedList();
		headerLayout.setContent(projectList);

		projectDesc = new Label("", ContentMode.HTML);
		projectDesc.setStyleName("project-description");
		addComponent(projectDesc);
	}

	public void showProjects() {
		if (headerLayout.isPopupVisible()) {
			headerLayout.setPopupVisible(false);
		}
		final ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
		searchCriteria.setInvolvedMember(new StringSearchField(SearchField.AND,
				AppContext.getUsername()));
		searchCriteria.setProjectStatuses(new SetSearchField<String>(
				new String[] { ProjectStatusConstants.OPEN }));
		this.projectList.setSearchCriteria(searchCriteria);
		this.headerLayout.setCaption(CurrentProjectVariables.getProject()
				.getName());
		this.projectDesc.setValue(CurrentProjectVariables.getProject()
				.getDescription());
	}

	private class ProjectPagedList extends
			BeanList<ProjectService, ProjectSearchCriteria, SimpleProject> {
		private static final long serialVersionUID = 1L;
		protected ProjectSearchCriteria currentCriteria;

		public ProjectPagedList() {
			super(null, ApplicationContextUtil
					.getSpringBean(ProjectService.class),
					ProjectRowDisplayHandler.class, contentLayout);
		}

		@Override
		public int setSearchCriteria(ProjectSearchCriteria searchCriteria) {
			currentCriteria = searchCriteria;
			SearchRequest<ProjectSearchCriteria> searchRequest = new SearchRequest<ProjectSearchCriteria>(
					searchCriteria, 0, 3);
			return setSearchRequest(searchRequest);
		}

		@Override
		public void loadItems(List<SimpleProject> currentListData) {
			super.loadItems(currentListData);

			if (searchService.getTotalCount(currentCriteria) > 3) {
				VerticalLayout btnWrap = new VerticalLayout();
				btnWrap.setWidth("100%");
				btnWrap.setMargin(true);

				final MyProjectListWindow projectListWindow = new MyProjectListWindow();

				Button showMoreBtn = new Button("More...",
						new Button.ClickListener() {
							private static final long serialVersionUID = -2178412846807704534L;

							@Override
							public void buttonClick(ClickEvent event) {
								headerLayout.setPopupVisible(false);
								UI.getCurrent().addWindow(projectListWindow);
							}
						});
				showMoreBtn.setStyleName(UIConstants.THEME_BLANK_LINK);
				showMoreBtn.setWidth("100%");
				btnWrap.addComponent(showMoreBtn);
				getContentLayout().addComponent(btnWrap);
			}
		}
	}

	public static class ProjectRowDisplayHandler implements
			BeanList.RowDisplayHandler<SimpleProject> {
		private static final long serialVersionUID = 1L;

		@Override
		public Component generateRow(final SimpleProject obj, int rowIndex) {
			final VerticalLayout layout = new VerticalLayout();
			layout.setWidth("100%");
			layout.setStyleName("project-name");
			layout.setMargin(new MarginInfo(true, false, true, true));

			if (obj.getId() == CurrentProjectVariables.getProject().getId()) {
				layout.addStyleName("current-project");
			}

			Label prjName = new Label(obj.getName());
			layout.addComponent(prjName);

			layout.addLayoutClickListener(new LayoutClickListener() {
				private static final long serialVersionUID = -329135249853828402L;

				@Override
				public void layoutClick(LayoutClickEvent event) {
					EventBus.getInstance().fireEvent(
							new ProjectEvent.GotoMyProject(this,
									new PageActionChain(
											new ProjectScreenData.Goto(obj
													.getId()))));
				}
			});

			return layout;
		}
	}
}