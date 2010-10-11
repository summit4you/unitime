/*
 * UniTime 3.2 (University Timetabling Application)
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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.client.page.UniTimeMenuBar;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;

import com.google.gwt.user.client.Command;

public enum Triggers {
	gwtHint(new Command() {
		public void execute() {
			GwtHint.createTriggers();
		}}),
	lookup(new Command() {
		public void execute() {
			Lookup.createTriggers();
		}
	}),
	gwtDialog(new Command() {
		public void execute() {
			UniTimeMenuBar.createTriggers();
		}
	}),
	loading(new Command() {
		public void execute() {
			LoadingWidget.createTriggers();
		}
	});
	
	
	private Command iCommand;
	
	Triggers(Command registerCommand) { iCommand = registerCommand; }
	
	public void register() { iCommand.execute(); }

}