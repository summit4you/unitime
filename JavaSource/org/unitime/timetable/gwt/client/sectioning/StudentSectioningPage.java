/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.client.sectioning.UserAuthentication.UserAuthenticatedEvent;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RootPanel;

public class StudentSectioningPage extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	public StudentSectioningPage() {
		Grid titlePanel = new Grid(1, 3);
		titlePanel.getCellFormatter().setWidth(0, 0, "33%");
		titlePanel.getCellFormatter().setWidth(0, 1, "34%");
		titlePanel.getCellFormatter().setWidth(0, 2, "33%");
		titlePanel.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		titlePanel.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		titlePanel.getCellFormatter().getElement(0, 2).getStyle().setPaddingLeft(10, Unit.PX);
		titlePanel.setHTML(0, 0, "&nbsp;");
		
		final UserAuthentication userAuthentication = new UserAuthentication(true);
		titlePanel.setWidget(0, 1, userAuthentication);
		
		iSectioningService.whoAmI(new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				userAuthentication.authenticate();
			}
			public void onSuccess(String result) {
				userAuthentication.authenticated(result);
			}
		});
		
		final AcademicSessionSelector sessionSelector = new AcademicSessionSelector();
		titlePanel.setWidget(0, 2, sessionSelector);
		
		RootPanel.get("UniTimeGWT:Header").clear();
		RootPanel.get("UniTimeGWT:Header").add(titlePanel);

		final StudentSectioningWidget widget = new StudentSectioningWidget(sessionSelector, userAuthentication);
		
		initWidget(widget);

		userAuthentication.addUserAuthenticatedHandler(new UserAuthentication.UserAuthenticatedHandler() {
			public void onLogIn(UserAuthenticatedEvent event) {
				sessionSelector.selectSession();
			}

			public void onLogOut(UserAuthenticatedEvent event) {
				if (!event.isGuest()) {
					widget.clear();
					sessionSelector.selectSession(null);
				}
				userAuthentication.authenticate();
			}
		});
		
		sessionSelector.addAcademicSessionChangeHandler(new AcademicSessionProvider.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionProvider.AcademicSessionChangeEvent event) {
				widget.clear();
				widget.lastRequest(event.getNewAcademicSessionId());
			}
		});
		
		iSectioningService.lastAcademicSession(new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				if (!userAuthentication.isShowing())
					sessionSelector.selectSession();
			}
			public void onSuccess(String[] result) {
				sessionSelector.selectSession(result);
				widget.lastRequest(sessionSelector.getAcademicSessionId());
			}
		});
	}
}