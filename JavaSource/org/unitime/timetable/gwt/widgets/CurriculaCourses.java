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
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseGroupInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumStudentsInterface;
import org.unitime.timetable.gwt.widgets.CurriculaClassifications.NameChangedEvent;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CurriculaCourses extends Composite {
	private MyFlexTable iTable = null;
	
	private static NumberFormat NF = NumberFormat.getFormat("##0.0");
	
	public static enum Mode {
		LAST ("Last", "Last-Like Enrollment"),
		PROJ ("Proj", "Projection by Rule"),
		ENRL ("Curr", "Current Enrollment"),
		NONE ("&nbsp;", "NONE");

		private String iAbbv, iName;
		
		Mode(String abbv, String name) { iAbbv = abbv; iName = name; }
		
		public String getAbbv() { return iAbbv; }
		public String getName() { return iName; }
	}
	
	private List<Group> iGroups = new ArrayList<Group>();
	
	private CurriculaClassifications iClassifications;
	
	private CurriculaCourseSelectionBox.CourseSelectionChangeHandler iCourseChangedHandler = null;
	
	private DialogBox iNewGroupDialog;
	private TextBox iGrName;
	private ListBox iGrType;
	private Button iGrAssign, iGrDelete, iGrUpdate;
	private String iGrOldName = null;
	private ClickHandler iGrHandler;
	private boolean iEditable = true;
	
	private static String[] sColors = new String[] {
		"red", "blue", "green", "orange", "yellow", "pink",
		"purple", "teal", "darkpurple", "steelblue", "lightblue",
		"lightgreen", "yellowgreen", "redorange", "lightbrown", "lightpurple",
		"grey", "bluegrey", "lightteal", "yellowgrey", "brown"
	};
	
	private TreeSet<String> iVisibleCourses = null;
	private HashMap<String, CurriculumStudentsInterface[]> iLastCourses = null;
	
	public CurriculaCourses() {
		iTable = new MyFlexTable();
		iTable.setCellPadding(2);
		iTable.setCellSpacing(0);
		initWidget(iTable);
		iCourseChangedHandler = new CurriculaCourseSelectionBox.CourseSelectionChangeHandler() {
			@Override
			public void onChange(CurriculaCourseSelectionBox.CourseSelectionChangeEvent evt) {
				CurriculumStudentsInterface[] c = (iLastCourses == null ? null : iLastCourses.get(evt.getCourse()));
				for (int col = 0; col < iClassifications.getClassifications().size(); col ++) {
					setEnrollmentAndLastLike(evt.getCourse(), col,
							c == null || c[col] == null ? null : c[col].getEnrollment(), 
							c == null || c[col] == null ? null : c[col].getLastLike(),
							c == null || c[col] == null ? null : c[col].getProjection());
				}
				Element td = evt.getSource().getElement();
				while (td != null && !DOM.getElementProperty(td, "tagName").equalsIgnoreCase("td")) {
					td = DOM.getParent(td);
				}
				Element tr = DOM.getParent(td);
			    Element body = DOM.getParent(tr);
			    int row = DOM.getChildIndex(body, tr);
			    if (evt.getCourse().isEmpty()) {
					iTable.getRowFormatter().addStyleName(row, "unitime-NoPrint");
			    } else {
					iTable.getRowFormatter().removeStyleName(row, "unitime-NoPrint");
			    }
			    if (row + 1 == iTable.getRowCount() && !evt.getCourse().isEmpty())
					addBlankLine();
			}
		};
		
		iNewGroupDialog = new DialogBox();
		iNewGroupDialog.setAnimationEnabled(true);
		iNewGroupDialog.setAutoHideEnabled(true);
		iNewGroupDialog.setGlassEnabled(true);
		iNewGroupDialog.setModal(true);
		FlexTable groupTable = new FlexTable();
		groupTable.setCellSpacing(2);
		groupTable.setText(0, 0, "Name:");
		iGrName = new TextBox();
		groupTable.setWidget(0, 1, iGrName);
		groupTable.setText(1, 0, "Type:");
		iGrType = new ListBox();
		iGrType.addItem("No conflict (different students)");
		iGrType.addItem("Conflict (same students)");
		iGrType.setSelectedIndex(0);
		groupTable.setWidget(1, 1, iGrType);
		HorizontalPanel grButtons = new HorizontalPanel();
		grButtons.setSpacing(2);
		iGrAssign = new Button("Assign");
		grButtons.add(iGrAssign);
		iGrUpdate = new Button("Update");
		grButtons.add(iGrUpdate);
		iGrDelete = new Button("Delete");
		grButtons.add(iGrDelete);
		groupTable.setWidget(2, 1, grButtons);
		groupTable.getFlexCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		iNewGroupDialog.add(groupTable);
		
		iGrAssign.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.hide();
				assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
			}
		});
		
		iGrName.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					iNewGroupDialog.hide();
					assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
				}
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					iNewGroupDialog.hide();
				}
			}
		});
		
		iGrUpdate.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.hide();
				assignGroup(iGrOldName, iGrName.getText(), iGrType.getSelectedIndex());
			}
		});
		
		iGrDelete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.hide();
				assignGroup(iGrOldName, null, iGrType.getSelectedIndex());
			}
		});
		
		iGrHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iNewGroupDialog.setText("Edit group");
				iGrOldName = ((Group)event.getSource()).getName();
				iGrName.setText(((Group)event.getSource()).getText());
				iGrType.setSelectedIndex(((Group)event.getSource()).getType());
				iGrAssign.setVisible(false);
				iGrDelete.setVisible(true);
				iGrUpdate.setVisible(true);
				DeferredCommand.addCommand(new Command() {
					@Override
					public void execute() {
						iGrName.setFocus(true);
						iGrName.selectAll();
					}
				});
				iTable.clearHover();
				event.stopPropagation();
				iNewGroupDialog.center();
			}
		};
	}
	
	public void link(CurriculaClassifications cx) {
		iClassifications = cx;
		iClassifications.addExpectedChangedHandler(new CurriculaClassifications.ExpectedChangedHandler() {
			@Override
			public void expectedChanged(CurriculaClassifications.ExpectedChangedEvent e) {
				setVisible(e.getColumn(), e.getExpected() != null);
				if (e.getExpected() != null)
					CurriculaCourses.this.expectedChanged(e.getColumn(), e.getExpected());
			}
		});
		iClassifications.addNameChangedHandler(new CurriculaClassifications.NameChangedHandler() {
			@Override
			public void nameChanged(NameChangedEvent e) {
				((Label)iTable.getWidget(0, 2 + 2 * e.getColumn())).setText(e.getName());
			}
		});
	}
	
	public void populate(CurriculumInterface curriculum) {
		iEditable = curriculum.isEditable();
		for (int row = iTable.getRowCount() - 1; row >= 0; row--) {
			iTable.removeRow(row);
		}
		iTable.clear(true);
		iGroups.clear();
		iTable.setEnabled(curriculum.isEditable());
		
		// header
		final Label groupsLabel = new Label("Group");
		iTable.setWidget(0, 0, groupsLabel);
		iTable.getFlexCellFormatter().setStyleName(0, 0, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, 0, "20px");
		groupsLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iVisibleCourses != null) return;
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				if (iEditable) {
					for (final CurriculaCourses.Group g: getGroups()) {
						menu.addItem(
								new MenuItem(
										DOM.toString(g.getElement()),
										true,
										new Command() {
											@Override
											public void execute() {
												popup.hide();
												assignGroup(null, g.getName(), g.getType());
											}
										}));
					}
					if (!getGroups().isEmpty()) {
						menu.addSeparator();
					}
					if (getSelectedCount() > 0) {
						menu.addItem(new MenuItem("New group...", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								iNewGroupDialog.setText("New group");
								iGrOldName = null;
								iGrName.setText(String.valueOf((char)('A' + getGroups().size())));
								iGrType.setSelectedIndex(0);
								iGrAssign.setVisible(true);
								iGrDelete.setVisible(false);
								iGrUpdate.setVisible(false);
								DeferredCommand.addCommand(new Command() {
									@Override
									public void execute() {
										iGrName.setFocus(true);
										iGrName.selectAll();
									}
								});
								iTable.clearHover();
								iNewGroupDialog.center();
							}
						}));
						menu.addSeparator();
					}
				}
				menu.addItem(new MenuItem("Sort by Group", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(0);
					}
				}));
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		final Label courseLabel = new Label("Course");
		iTable.setWidget(0, 1, courseLabel);
		iTable.getFlexCellFormatter().setStyleName(0, 1, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, 1, "100px");
		courseLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				if (iVisibleCourses != null) {
					menu.addItem(new MenuItem("Show All Courses", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							showAllCourses();
						}
					}));
				}
				if (iEditable) {
					menu.addItem(new MenuItem("Select All Courses", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							for (int i = 1; i < iTable.getRowCount(); i++)
								setSelected(i, !((CurriculaCourseSelectionBox)iTable.getWidget(i, 1)).getCourse().isEmpty());
						}
					}));
					if (getSelectedCount() > 0) {
						if (iVisibleCourses == null) {
							menu.addItem(new MenuItem("Remove Selected Courses", true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									for (int row = iTable.getRowCount() - 1; row > 0; row --) {
										if (!isSelected(row)) continue;
										String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
										if (course.isEmpty() && row + 1 == iTable.getRowCount()) {
											setSelected(row, false);
											continue;
										}
										iTable.removeRow(row);
									}
								}
							}));
						}
						menu.addItem(new MenuItem("Clear Selection", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int i = 1; i < iTable.getRowCount(); i++)
									setSelected(i, false);
							}
						}));
					}
					menu.addSeparator();
				}
				if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
					menu.addItem(new MenuItem("Show Numbers", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setPercent(false);
						}
					}));
				else
					menu.addItem(new MenuItem("Show Percentages", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setPercent(true);
						}
					}));
				for (final Mode m: Mode.values()) {
					if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == m) continue;
					menu.addItem(new MenuItem(m == Mode.NONE ? "Hide " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName() : "Show " + m.getName(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							setMode(m);
						}
					}));
				}
				if (iVisibleCourses == null) {
					menu.addSeparator();
					if (iLastCourses != null) {
						menu.addItem(new MenuItem("Show Empty Courses", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								updateEnrollmentsAndLastLike(iLastCourses);
							}
						}));
					}
					menu.addItem(new MenuItem("Hide Empty Courses", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							boolean selectedOnly = (getSelectedCount() > 0);
							rows: for (int row = iTable.getRowCount() - 1; row > 0; row --) {
								String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
								if (course.isEmpty() && row + 1 == iTable.getRowCount()) continue;
								if (selectedOnly && !isSelected(row)) {
									setSelected(row, false);
									continue;
								}
								for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
									int x = 2 + 2 * c;
									MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
									if (text.getShare() != null) continue rows;
								}
								iTable.removeRow(row);
							}
						}
					}));
					if (iEditable) {
						menu.addSeparator();
						menu.addItem(new MenuItem("Clear Requested Enrollments (All Classifications" + (getSelectedCount() > 0 ? ", Selected Courses Only" : "") + ")", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
									int x = 2 + 2 * c;
									for (int row = 1; row < iTable.getRowCount(); row ++) {
										if (selectedOnly && !isSelected(row)) continue;
										MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
										text.setShare(null);
									}
								}
							}
						}));
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.LAST)
						menu.addItem(new MenuItem("Copy Last-Like &rarr; Requested (All Classifications" + (getSelectedCount() > 0 ? ", Selected Courses Only" : "") + ")", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
									int x = 2 + 2 * c;
									for (int row = 1; row < iTable.getRowCount(); row ++) {
										if (selectedOnly && !isSelected(row)) continue;
										MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
										MyLabel label = (MyLabel)iTable.getWidget(row, x + 1);
										if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
											text.setShare(label.getLastLikePercent());
										else
											text.setExpected(label.getLastLike());
									}
								}
							}
						}));
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL)
						menu.addItem(new MenuItem("Copy Current &rarr; Requested (All Classifications" + (getSelectedCount() > 0 ? ", Selected Courses Only" : "") + ")", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
									int x = 2 + 2 * c;
									for (int row = 1; row < iTable.getRowCount(); row ++) {
										if (selectedOnly && !isSelected(row)) continue;
										MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
										MyLabel label = (MyLabel)iTable.getWidget(row, x + 1);
										if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
											text.setShare(label.getEnrollmentPercent());
										else
											text.setExpected(label.getEnrollment());
									}
								}
							}
						}));		
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.PROJ)
						menu.addItem(new MenuItem("Copy Projection &rarr; Requested (All Classifications" + (getSelectedCount() > 0 ? ", Selected Courses Only" : "") + ")", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
									int x = 2 + 2 * c;
									for (int row = 1; row < iTable.getRowCount(); row ++) {
										if (selectedOnly && !isSelected(row)) continue;
										MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
										MyLabel label = (MyLabel)iTable.getWidget(row, x + 1);
										if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
											text.setShare(label.getProjectionPercent());
										else
											text.setExpected(label.getProjection());
									}
								}
							}
						}));
					}
				}
				if (iVisibleCourses == null) {
					menu.addSeparator();
					menu.addItem(new MenuItem("Sort by Course", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							sort(1);
						}
					}));
				}
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		CurriculaCourseSelectionBox.CourseFinderDialogHandler fx = new CurriculaCourseSelectionBox.CourseFinderDialogHandler() {
			@Override
			public void onOpen(CurriculaCourseSelectionBox.CourseFinderDialogEvent e) {
				iTable.clearHover();
			}
		};
		
		int col = 1;
		for (AcademicClassificationInterface clasf: iClassifications.getClassifications()) {
			col++;
			final Label cl = new Label(clasf.getCode());
			iTable.setWidget(0, col, cl);
			iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
			iTable.getFlexCellFormatter().setWidth(0, col, "60px");
			iTable.getFlexCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_RIGHT);
			final int x = col;
			cl.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new PopupPanel(true);
					MenuBar menu = new MenuBar(true);
					if (iEditable) {
						menu.addItem(new MenuItem("Select All", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int i = 1; i < iTable.getRowCount(); i++)
									setSelected(i, !((MyTextBox)iTable.getWidget(i, x)).getText().isEmpty());
							}
						}));
						if (getSelectedCount() > 0) {
							menu.addItem(new MenuItem("Clear Selection", true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									for (int i = 1; i < iTable.getRowCount(); i++)
										setSelected(i, false);
								}
							}));
						}
						menu.addSeparator();
					}
					if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
						menu.addItem(new MenuItem("Show Numbers", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(false);
							}
						}));
					else
						menu.addItem(new MenuItem("Show Percentages", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(true);
							}
						}));
					for (final Mode m: Mode.values()) {
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == m) continue;
						menu.addItem(new MenuItem(m == Mode.NONE ? "Hide " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName() : "Show " + m.getName(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(m);
							}
						}));
					}
					if (iVisibleCourses == null && iEditable) {
						menu.addSeparator();
						menu.addItem(new MenuItem("Clear Requested Enrollments" + (getSelectedCount() > 0 ? " (Selected Courses Only)" : ""), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int row = 1; row < iTable.getRowCount(); row ++) {
									if (selectedOnly && !isSelected(row)) continue;
									MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
									text.setShare(null);
								}
							}
						}));
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.LAST)
						menu.addItem(new MenuItem("Copy Last-Like &rarr; Requested" + (getSelectedCount() > 0 ? " (Selected Courses Only)" : ""), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int row = 1; row < iTable.getRowCount(); row ++) {
									if (selectedOnly && !isSelected(row)) continue;
									MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
									MyLabel label = (MyLabel)iTable.getWidget(row, x + 1);
									if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
										text.setShare(label.getLastLikePercent());
									else
										text.setExpected(label.getLastLike());
								}
							}
						}));
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL)
						menu.addItem(new MenuItem("Copy Current &rarr; Requested" + (getSelectedCount() > 0 ? " (Selected Courses Only)" : ""), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int row = 1; row < iTable.getRowCount(); row ++) {
									if (selectedOnly && !isSelected(row)) continue;
									MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
									MyLabel label = (MyLabel)iTable.getWidget(row, x + 1);
									if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
										text.setShare(label.getEnrollmentPercent());
									else
										text.setExpected(label.getEnrollment());
								}
							}
						}));
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.PROJ)
						menu.addItem(new MenuItem("Copy Projection &rarr; Requested" + (getSelectedCount() > 0 ? " (Selected Courses Only)" : ""), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								boolean selectedOnly = (getSelectedCount() > 0);
								for (int row = 1; row < iTable.getRowCount(); row ++) {
									if (selectedOnly && !isSelected(row)) continue;
									MyTextBox text = (MyTextBox)iTable.getWidget(row, x);
									MyLabel label = (MyLabel)iTable.getWidget(row, x + 1);
									if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
										text.setShare(label.getProjectionPercent());
									else
										text.setExpected(label.getProjection());
								}
							}
						}));
					}
					if (iVisibleCourses == null) {
						menu.addSeparator();
						menu.addItem(new MenuItem("Sort by " + cl.getText(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								sort(x);
							}
						}));
					}
					menu.setVisible(true);
					popup.add(menu);
					popup.showRelativeTo((Widget)event.getSource());
				}
			});
			col++;
			final HTML m = new HTML(CurriculumCookie.getInstance().getCurriculaCoursesMode().getAbbv());
			iTable.setWidget(0, col, m);
			iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
			iTable.getFlexCellFormatter().setWidth(0, col, "5px");
			iTable.getFlexCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
			final int y = col;
			m.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new PopupPanel(true);
					MenuBar menu = new MenuBar(true);
					menu.addItem(new MenuItem("Select All", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							for (int i = 1; i < iTable.getRowCount(); i++)
								setSelected(i, !((MyLabel)iTable.getWidget(i, y)).getText().isEmpty());
						}
					}));
					if (getSelectedCount() > 0) {
						menu.addItem(new MenuItem("Clear Selection", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int i = 1; i < iTable.getRowCount(); i++)
									setSelected(i, false);
							}
						}));
					}
					menu.addSeparator();
					if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
						menu.addItem(new MenuItem("Show Numbers", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(false);
							}
						}));
					else
						menu.addItem(new MenuItem("Show Percentages", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setPercent(true);
							}
						}));
					for (final Mode m: Mode.values()) {
						if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == m) continue;
						menu.addItem(new MenuItem(m == Mode.NONE ? "Hide " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName() : "Show " + m.getName(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								setMode(m);
							}
						}));
					}
					if (iVisibleCourses == null) {
						menu.addSeparator();
						menu.addItem(new MenuItem("Sort by " + cl.getText() + " " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								sort(y);
							}
						}));
					}
					menu.setVisible(true);
					popup.add(menu);
					popup.showRelativeTo((Widget)event.getSource());
				}
			});

		}
		
		// body
		int row = 0;
		if (curriculum.hasCourses()) {
			for (CourseInterface course: curriculum.getCourses()) {
				row ++;
				HorizontalPanel hp = new HorizontalPanel();
				iTable.setWidget(row, 0, hp);
				
				if (course.hasGroups()) {
					for (CurriculumCourseGroupInterface g: course.getGroups()) {
						Group gr = null;
						for (Group x: iGroups) {
							if (x.getName().equals(g.getName())) { gr = x; break; }
						}
						if (gr == null) {
							gr = new Group(g.getName(), g.getType(), iEditable);
							if (g.getColor() != null) {
								gr.setColor(g.getColor());
							} else {
								colors: for (String c: sColors) {
									for (Group x: iGroups) {
										if (x.getColor().equals(c)) continue colors;
									}
									gr.setColor(c);
									break;
								}
								if (gr.getColor() == null) gr.setColor(sColors[0]);
							}
							iGroups.add(gr);
						}
						hp.add(gr.cloneGroup());
					}
				}
				
				CurriculaCourseSelectionBox cx = new CurriculaCourseSelectionBox(course.getId().toString());
				cx.setCourse(course.getCourseName(), false);
				cx.setWidth("100px");
				cx.addCourseFinderDialogHandler(fx);
				cx.addCourseSelectionChangeHandler(iCourseChangedHandler);
				if (!iEditable) cx.setEnabled(false);
				iTable.setWidget(row, 1, cx);
				
				for (col = 0; col < iClassifications.getClassifications().size(); col++) {
					CurriculumCourseInterface cci = course.getCurriculumCourse(col);
					MyTextBox ex = new MyTextBox(col, cci == null ? null : cci.getShare());
					if (!iEditable) ex.setEnabled(false);
					iTable.setWidget(row, 2 + 2 * col, ex);
					MyLabel note = new MyLabel(col, cci == null ? null : cci.getEnrollment(), cci == null ? null : cci.getLastLike(), cci == null ? null : cci.getProjection());
					iTable.setWidget(row, 3 + 2 * col, note);
					iTable.getFlexCellFormatter().setHorizontalAlignment(row, 3 + 2 * col, HasHorizontalAlignment.ALIGN_RIGHT);
				}
			}
		}
		if (iEditable) addBlankLine();
	}
	
	public boolean saveCurriculum(CurriculumInterface c) {
		boolean ret = true;
		HashSet<String> courses = new HashSet<String>();
		HashMap<String, CurriculumCourseGroupInterface> groups = new HashMap<String, CurriculumCourseGroupInterface>();
		if (c.hasCourses()) c.getCourses().clear();
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (course.isEmpty()) continue;
			if (!courses.add(course)) {
				((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setError("Duplicate course " + course);
				ret = false;
				continue;
			}
			CourseInterface cr = new CourseInterface();
			cr.setCourseName(course);
			for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
				Float share = ((MyTextBox)iTable.getWidget(row, 2 + 2 * i)).getShare();
				if (share == null) continue;
				Integer lastLike = ((MyLabel)iTable.getWidget(row, 3 + 2 * i)).iLastLike;
				CurriculumCourseInterface cx = new CurriculumCourseInterface();
				cx.setShare(share);
				cx.setLastLike(lastLike);
				cx.setCurriculumClassificationId(iClassifications.getClassifications().get(i).getId());
				cr.setCurriculumCourse(i, cx);
			}
			if (!cr.hasCurriculumCourses()) continue;
			HorizontalPanel hp = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < hp.getWidgetCount(); i++) {
				Group g = (Group)hp.getWidget(i);
				CurriculumCourseGroupInterface gr = groups.get(g.getName());
				if (gr == null) {
					gr = new CurriculumCourseGroupInterface();
					gr.setName(g.getName());
					gr.setType(g.getType());
					gr.setColor(g.getColor());
					groups.put(g.getName(), gr);
				}
				cr.addGroup(gr);
			}
			c.addCourse(cr);
		}
		return ret;
	}
	
	public void addBlankLine() {
		int row = iTable.getRowCount();
		HorizontalPanel hp = new HorizontalPanel();
		iTable.setWidget(row, 0, hp);
		if (iVisibleCourses != null) iTable.getCellFormatter().setVisible(row, 0, false);

		CurriculaCourseSelectionBox cx = new CurriculaCourseSelectionBox(null);
		cx.setWidth("100px");
		cx.addCourseSelectionChangeHandler(iCourseChangedHandler);
		cx.addCourseFinderDialogHandler(new CurriculaCourseSelectionBox.CourseFinderDialogHandler() {
			@Override
			public void onOpen(CurriculaCourseSelectionBox.CourseFinderDialogEvent e) {
				iTable.clearHover();
			}
		});
		if (!iEditable) cx.setEnabled(false);
		iTable.setWidget(row, 1, cx);
		if (iVisibleCourses != null) iTable.getCellFormatter().setVisible(row, 1, false);
		
		for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
			MyTextBox ex = new MyTextBox(col, null);
			if (!iEditable) ex.setEnabled(false);
			iTable.setWidget(row, 2 + 2 * col, ex);
			MyLabel note = new MyLabel(col, null, null, null);
			iTable.setWidget(row, 3 + 2 * col, note);
			iTable.getFlexCellFormatter().setHorizontalAlignment(row, 3 + 2 * col, HasHorizontalAlignment.ALIGN_RIGHT);
			if (iClassifications.getExpected(col) == null) {
				iTable.getFlexCellFormatter().setVisible(row, 2 + 2 * col, false);
				iTable.getFlexCellFormatter().setVisible(row, 3 + 2 * col, false);
			}
			if (iVisibleCourses != null) {
				iTable.getCellFormatter().setVisible(row, 2 + 2 * col, false);
				iTable.getCellFormatter().setVisible(row, 3 + 2 * col, false);
			}
		}
		iTable.getRowFormatter().addStyleName(row, "unitime-NoPrint");
	}
	
	public void sort(final int column) {
		Integer[] x = new Integer[iTable.getRowCount() - 1];
		for (int i = 0; i < x.length; i ++) x[i] = i;
		Arrays.sort(x, new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				int cmp = compareTwoRows(column, a, b);
				if (cmp != 0) return cmp;
				if (column > 1) {
					int c = column + 2;
					while (c < 2 + 2 * iClassifications.getClassifications().size()) {
						cmp = compareTwoRows(c, a, b);
						if (cmp != 0) return cmp;
						c += 2;
					}
				}
				return compareTwoRows(1, a, b);
			}
		});
		for (int i = 0; i < x.length; i ++) {
			int j = x[i];
			while (j < i) j = x[j];
			swap(i, j);
		}
	}
	
	private int compareTwoRows(int column, int r0, int r1) {
		boolean e1 = ((CurriculaCourseSelectionBox)iTable.getWidget(r0 + 1, 1)).getCourse().isEmpty();
		boolean e2 = ((CurriculaCourseSelectionBox)iTable.getWidget(r1 + 1, 1)).getCourse().isEmpty();
		if (e1 && !e2) return 1;
		if (e2 && !e1) return -1;
		if (column == 0) {
			HorizontalPanel p0 = (HorizontalPanel)iTable.getWidget(r0 + 1, 0);
			HorizontalPanel p1 = (HorizontalPanel)iTable.getWidget(r1 + 1, 0);
			TreeSet<Group> g0 = new TreeSet<Group>();
			TreeSet<Group> g1 = new TreeSet<Group>();
			for (int i = 0; i < p0.getWidgetCount(); i++) g0.add((Group)p0.getWidget(i));
			for (int i = 0; i < p1.getWidgetCount(); i++) g1.add((Group)p1.getWidget(i));
			Iterator<Group> i0 = g0.iterator();
			Iterator<Group> i1 = g1.iterator();
			while (i0.hasNext() || i1.hasNext()) {
				if (!i0.hasNext()) return 1;
				if (!i1.hasNext()) return -1;
				int cmp = i0.next().compareTo(i1.next());
				if (cmp != 0) return cmp;
			}
			return compareTwoRows(2, r0, r1);
		}
		if (column == 1)
			return ((CurriculaCourseSelectionBox)iTable.getWidget(r0 + 1, 1)).getCourse().compareTo(((CurriculaCourseSelectionBox)iTable.getWidget(r1 + 1, 1)).getCourse());
		if (column % 2 == 0) {
			Float s0 = ((MyTextBox)iTable.getWidget(r0 + 1, column)).getShare();
			Float s1 = ((MyTextBox)iTable.getWidget(r1 + 1, column)).getShare();
			return - (s0 == null ? new Float(0) : s0).compareTo(s1 == null ? new Float(0) : s1);
		} else {
			MyLabel l0 = ((MyLabel)iTable.getWidget(r0 + 1, column));
			MyLabel l1 = ((MyLabel)iTable.getWidget(r1 + 1, column));
			Mode mode = CurriculumCookie.getInstance().getCurriculaCoursesMode();
			Integer i0 = (mode == Mode.ENRL ? l0.iEnrollment : mode == Mode.LAST ? l0.iLastLike : l0.iProjection);
			Integer i1 = (mode == Mode.ENRL ? l1.iEnrollment : mode == Mode.LAST ? l1.iLastLike : l0.iProjection);
			return - (i0 == null ? new Integer(0) : i0).compareTo(i1 == null ? new Integer(0) : i1);
		}
	}
	
	private void swap(int r0, int r1) {
		if (r0 == r1) return;
		String s = iTable.getRowFormatter().getStyleName(1 + r0);
		iTable.getRowFormatter().setStyleName(1 + r0, iTable.getRowFormatter().getStyleName(1 + r1));
		iTable.getRowFormatter().setStyleName(1 + r1, s);
		Widget w = iTable.getWidget(1 + r0, 0);
		iTable.setWidget(1 + r0, 0, iTable.getWidget(1 + r1, 0));
		iTable.setWidget(1 + r1, 0, w);
		CurriculaCourseSelectionBox c0 = (CurriculaCourseSelectionBox)iTable.getWidget(1 + r0, 1);
		CurriculaCourseSelectionBox c1 = (CurriculaCourseSelectionBox)iTable.getWidget(1 + r1, 1);
		String course = c0.getCourse();
		c0.setCourse(c1.getCourse(), false);
		c1.setCourse(course, false);
		if (c0.getCourse().isEmpty())
			iTable.getRowFormatter().addStyleName(1 + r1, "unitime-NoPrint");
		else 
			iTable.getRowFormatter().removeStyleName(1 + r1, "unitime-NoPrint");
		if (c1.getCourse().isEmpty())
			iTable.getRowFormatter().addStyleName(1 + r0, "unitime-NoPrint");
		else 
			iTable.getRowFormatter().removeStyleName(1 + r0, "unitime-NoPrint");
		for (int col = 0; col < iClassifications.getClassifications().size(); col ++) {
			MyTextBox t0 = (MyTextBox)iTable.getWidget(1 + r0, 2 + 2 * col);
			MyTextBox t1 = (MyTextBox)iTable.getWidget(1 + r1, 2 + 2 * col);
			Float share = t0.getShare();
			t0.setShare(t1.getShare());
			t1.setShare(share);
			MyLabel l0 = (MyLabel)iTable.getWidget(1 + r0, 3 + 2 * col);
			MyLabel l1 = (MyLabel)iTable.getWidget(1 + r1, 3 + 2 * col);
			Integer enrl = l0.iEnrollment;
			l0.iEnrollment = l1.iEnrollment;
			l1.iEnrollment = enrl;
			Integer last = l0.iLastLike;
			l0.iLastLike = l1.iLastLike;
			l1.iLastLike = last;
			Integer proj = l0.iProjection;
			l0.iProjection = l1.iProjection;
			l1.iProjection = proj;
			l0.update(); l1.update();
		}
	}

	public int getCourseIndex(String course) {
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (course.equals(c)) return row - 1;
		}
		return -1;
	}
	
	public boolean setEnrollmentAndLastLike(String course, int clasf, Integer enrollment, Integer lastLike, Integer projection) {
		boolean changed = false;
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (!course.equals(c)) continue;
			MyLabel note = ((MyLabel)iTable.getWidget(row, 3 + 2 * clasf));
			note.iEnrollment = enrollment;
			note.iLastLike = lastLike;
			note.iProjection = projection;
			note.update();
			changed = true;
		}
		return changed;
	}
	
	public void updateEnrollmentsAndLastLike(HashMap<String, CurriculumStudentsInterface[]> courses) {
		iLastCourses = courses;
		rows: for (int row = 1; row < iTable.getRowCount() - 1; ) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col ++) {
				MyTextBox text = (MyTextBox)iTable.getWidget(row, 2 + 2 * col);
				if (!text.getText().isEmpty()) {
					row ++;
					continue rows;
				}
			}
			iTable.removeRow(row);
		}
		HashSet<String> updated = new HashSet<String>();
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String c = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (c.isEmpty()) continue;
			updated.add(c);
			CurriculumStudentsInterface[] cc = courses.get(c);
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				MyLabel note = ((MyLabel)iTable.getWidget(row, 3 + 2 * col));
				note.iEnrollment = (cc == null || cc[col] == null ? null : cc[col].getEnrollment());
				note.iLastLike = (cc == null || cc[col] == null ? null : cc[col].getLastLike());
				note.iProjection = (cc == null || cc[col] == null ? null : cc[col].getProjection());
				note.update();
			}
		}
		CurriculumStudentsInterface[] total = courses.get("");
		if (total == null) return;
		int totalEnrollment = 0, totalLastLike = 0;
		for (int i = 0; i < total.length; i++) {
			if (total[i] != null) totalEnrollment += total[i].getEnrollment();
			if (total[i] != null) totalLastLike += total[i].getLastLike();
		}
		TreeSet<Map.Entry<String, CurriculumStudentsInterface[]>> include = new TreeSet<Map.Entry<String,CurriculumStudentsInterface[]>>(new Comparator<Map.Entry<String,CurriculumStudentsInterface[]>>() {
			/*
			private int highestClassification(CurriculumStudentsInterface[] a) {
				int best = a.length;
				int bestVal = -1;
				for (int i = 0; i < a.length; i++) {
					if (a[i] == null) continue;
					if (a[i].getEnrollment() > bestVal) {
						bestVal = a[i].getEnrollment(); best = i;
					}
					if (a[i].getLastLike() > bestVal) {
						bestVal = a[i].getLastLike(); best = i;
					}
				}
				return best;
			}
			*/
			private int firstClassification(CurriculumStudentsInterface[] a) {
				for (int i = 0; i < a.length; i++) {
					if (a[i] == null) continue;
					if (a[i].getEnrollment() > 0) return i;
					if (a[i].getLastLike() > 0) return i;
					if (a[i].getProjection() > 0) return i;
				}
				return a.length;
			}
			public int compare(Map.Entry<String,CurriculumStudentsInterface[]> c0, Map.Entry<String,CurriculumStudentsInterface[]> c1) {
				/*
				int a0 = highestClassification(c0.getValue());
				int a1 = highestClassification(c1.getValue());
				if (a0 < a1) return -1;
				if (a0 > a1) return 1;
				if (a0 < c0.getValue().length) {
					int v0 = (c0.getValue()[a0][0] == null ? 0 : c0.getValue()[a0][0]);
					int v1 = (c1.getValue()[a0][0] == null ? 0 : c1.getValue()[a0][0]);
					int w0 = (c0.getValue()[a0][1] == null ? 0 : c0.getValue()[a0][1]);
					int w1 = (c1.getValue()[a0][1] == null ? 0 : c1.getValue()[a0][1]);
					if (v0 < v1 || w0 < w1) return -1;
					if (v0 > v1 || w0 > w1) return 1;
				}
				*/
				int b0 = firstClassification(c0.getValue());
				int b1 = firstClassification(c1.getValue());
				if (b0 < b1) return -1;
				if (b0 > b1) return 1;
				while (b0 < c0.getValue().length) {
					int v0 = (c0.getValue()[b0] == null ? 0 : c0.getValue()[b0].getEnrollment());
					int v1 = (c1.getValue()[b0] == null ? 0 : c1.getValue()[b0].getEnrollment());
					int w0 = (c0.getValue()[b0] == null ? 0 : c0.getValue()[b0].getLastLike());
					int w1 = (c1.getValue()[b0] == null ? 0 : c1.getValue()[b0].getLastLike());
					if (v0 > v1 || w0 > w1) return -1;
					if (v0 < v1 || w0 < w1) return 1;
					b0++;
				}
				return c0.getKey().compareTo(c1.getKey());
			}
		});
		for (Map.Entry<String, CurriculumStudentsInterface[]> course: courses.entrySet()) {
			if (updated.contains(course.getKey()) || course.getKey().isEmpty()) continue;
			CurriculumStudentsInterface[] cc = course.getValue();
			int enrollment = 0, lastLike = 0;
			for (int i = 0; i < cc.length; i++) {
				if (cc[i] != null) enrollment += cc[i].getEnrollment();
				if (cc[i] != null) lastLike += cc[i].getLastLike();
			}
			if ((totalEnrollment > 0 && 100.0f * enrollment / totalEnrollment > 3.0f) ||
				(totalLastLike > 0 && 100.0f * lastLike / totalLastLike > 3.0f)) {
				include.add(course);
			}
		}
		for (Map.Entry<String, CurriculumStudentsInterface[]> course: include) {
			CurriculumStudentsInterface[] cc = course.getValue();
			int row = iTable.getRowCount() - 1;
			if (!iEditable) row++;
			addBlankLine();
			CurriculaCourseSelectionBox c = (CurriculaCourseSelectionBox)iTable.getWidget(row, 1);
			c.setCourse(course.getKey(), false);
			iTable.getRowFormatter().removeStyleName(row, "unitime-NoPrint");
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				MyLabel note = ((MyLabel)iTable.getWidget(row, 3 + 2 * col));
				note.iEnrollment = (cc == null || cc[col] == null ? null : cc[col].getEnrollment());
				note.iLastLike = (cc == null || cc[col] == null ? null : cc[col].getLastLike());
				note.iProjection = (cc == null || cc[col] == null ? null : cc[col].getProjection());
				note.update();
			}
			if (iVisibleCourses!=null) {
				if (iVisibleCourses.contains(course.getKey())) {
					((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setEnabled(false);
					iTable.getCellFormatter().setVisible(row, 0, true);
					iTable.getCellFormatter().setVisible(row, 1, true);
					for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
						boolean vis = iClassifications.getExpected(i) != null;
						iTable.getCellFormatter().setVisible(row, 2 + 2 * i, vis);
						iTable.getCellFormatter().setVisible(row, 3 + 2 * i, vis);
					}
				} else {
					iTable.getCellFormatter().setVisible(row, 0, false);
					iTable.getCellFormatter().setVisible(row, 1, false);
					for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
						iTable.getCellFormatter().setVisible(row, 2 + 2 * i, false);
						iTable.getCellFormatter().setVisible(row, 3 + 2 * i, false);
					}
				}
			}
		}
	}
	
	public void expectedChanged(int col, int expected) {
		if (!CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
			for (int row = 1; row < iTable.getRowCount(); row++) {
				((MyTextBox)iTable.getWidget(row, 2 + 2 * col)).update();
			}
		}
	}
	
	private void setPercent(boolean percent) {
		if (CurriculumCookie.getInstance().getCurriculaCoursesPercent() == percent) return;
		CurriculumCookie.getInstance().setCurriculaCoursesPercent(percent);
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				((MyTextBox)iTable.getWidget(row, 2 + 2 * col)).update();
				((MyLabel)iTable.getWidget(row, 3 + 2 * col)).update();
			}
		}
	}
	
	private void setMode(Mode mode) {
		CurriculumCookie.getInstance().setCurriculaCoursesMode(mode);
		for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
			((HTML)iTable.getWidget(0, 3 + 2 * col)).setHTML(mode.getAbbv());
		}
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 0; col < iClassifications.getClassifications().size(); col++) {
				((MyLabel)iTable.getWidget(row, 3 + 2 * col)).update();
			}
		}
	}
	
	public void setVisible(int col, boolean visible) {
		for (int row = 0; row < iTable.getRowCount(); row++) {
			String courseName = (row == 0 ? null : ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse());
			if (row == 0 || iVisibleCourses == null || iVisibleCourses.contains(courseName)) {
				iTable.getFlexCellFormatter().setVisible(row, 2 + 2 * col, visible);
				iTable.getFlexCellFormatter().setVisible(row, 3 + 2 * col, visible);
			}
		}
	}
	
	public class MyLabel extends Label {
		private int iColumn;
		private Integer iEnrollment, iLastLike, iProjection;
		
		public MyLabel(int column, Integer enrollment, Integer lastLike, Integer projection) {
			super();
			setStyleName("unitime-Label");
			iColumn = column;
			iEnrollment = enrollment;
			iLastLike = lastLike;
			iProjection = projection;
			update();
		}
		
		public void update() {
			switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
			case NONE: // None
				setText("");
				break;
			case ENRL: // Enrollment
				if (iEnrollment == null || iEnrollment == 0) {
					setText("");
				} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
					Integer total = iClassifications.getEnrollment(iColumn);
					setText(total == null ? "N/A" : NF.format(100.0 * iEnrollment / total) + "%");
				} else {
					setText(iEnrollment.toString());
				}
				break;
			case LAST: // Last-like
				if (iLastLike == null || iLastLike == 0) {
					setText("");
				} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
					Integer total = iClassifications.getLastLike(iColumn);
					setText(total == null ? "N/A" : NF.format(100.0 * iLastLike / total) + "%");
				} else {
					setText(iLastLike.toString());
				}
				break;
			case PROJ: // Projection
				if (iProjection == null || iProjection == 0) {
					setText("");
				} else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent()) {
					Integer total = iClassifications.getProjection(iColumn);
					setText(total == null ? "N/A" : NF.format(100.0 * iProjection / total) + "%");
				} else {
					setText(iProjection.toString());
				}
				break;
			}
		}
		
		public Integer getLastLike() { return (iLastLike == null || iLastLike == 0 ? null : iLastLike); }
		
		public Integer getEnrollment() { return (iEnrollment == null || iEnrollment == 0 ? null : iEnrollment); }

		public Integer getProjection() { return (iProjection == null || iProjection == 0 ? null : iProjection); }

		public Float getLastLikePercent() { 
			if (iLastLike == null || iLastLike == 0) return null;
			Integer total = iClassifications.getLastLike(iColumn);
			if (total == null) return null;
			return ((float)iLastLike) / total;
		}
		
		public Float getEnrollmentPercent() { 
			if (iEnrollment == null || iEnrollment == 0) return null;
			Integer total = iClassifications.getEnrollment(iColumn);
			if (total == null) return null;
			return ((float)iEnrollment) / total;
		}

		public Float getProjectionPercent() { 
			if (iProjection == null || iProjection == 0) return null;
			Integer total = iClassifications.getProjection(iColumn);
			if (total == null) return null;
			return ((float)iProjection) / total;
		}
	}

	public class MyTextBox extends TextBox {
		private int iColumn;
		private Float iShare = null;
		
		public MyTextBox(int column, Float share) {
			super();
			iColumn = column;
			iShare = share;
			setWidth("60px");
			setStyleName("unitime-TextBox");
			setMaxLength(6);
			setTextAlignment(TextBox.ALIGN_RIGHT);
			addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					try {
						if (getText().isEmpty()) {
							iShare = null;
						} else if (getText().endsWith("%")) {
							iShare = Float.valueOf(getText().substring(0, getText().length() - 1)) / 100.0f;
							if (iShare > 1.0f) iShare = 1.0f;
						} else {
							iShare = Float.valueOf(getText()) / iClassifications.getExpected(iColumn);
							if (iShare > 1.0f) iShare = 1.0f;
						}
					} catch (Exception e) {
						iShare = null;
					}
					update();
				}
			});
			update();
		}
		
		public void setShare(Float share) {
			iShare = share;
			update();
		}
		
		public void setExpected(Integer expected) {
			if (expected == null) {
				iShare = null;
			} else {
				Integer total = iClassifications.getExpected(iColumn);
				if (total == null) {
					iShare = null;
				} else {
					iShare = ((float)expected) / total;
				}
			}
			update();
		}
		
		public Float getShare() {
			return iShare;
		}
		
		public void update() {
			if (iShare == null) 
				setText("");
			else if (CurriculumCookie.getInstance().getCurriculaCoursesPercent())
				setText(NF.format(100.0 * iShare) + "%");
			else {
				Integer exp = iClassifications.getExpected(iColumn);
				setText(exp == null ? "N/A" : String.valueOf(Math.round(exp * iShare)));	
			}
		}
		
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			if (enabled) {
				getElement().getStyle().setBorderColor(null);
				getElement().getStyle().setBackgroundColor(null);
			} else {
				getElement().getStyle().setBorderColor("transparent");
				getElement().getStyle().setBackgroundColor("transparent");
			}
		}
	}
	
	public static class CourseChangedEvent {
		private String iCourseName = null;
		public CourseChangedEvent(String courseName) {
			iCourseName = courseName;
		}
		public String getCourseName() { return iCourseName; }
	}
	
	public class Group extends Label implements Comparable<Group> {
		private String iName;
		private int iType;
		private String iColor;
		private boolean iEditable;
		
		public Group(String name, int type, boolean editable) {
			super(name, false);
			iName = name;
			iType = type;
			setStylePrimaryName("unitime-TinyLabel" + (iType == 1 ? "White" : ""));
			iEditable = editable;
			if (iEditable) {
				addClickHandler(iGrHandler);
				getElement().getStyle().setCursor(Cursor.POINTER);
			}
		}
		public String getName() { return iName; }
		public int getType() { return iType; }
		public void setType(int type) {
			iType = type;
			setStylePrimaryName("unitime-TinyLabel" + (iType == 1 ? "White" : ""));
		}
		public void setName(String name) {
			iName = name;
			setText(name);
		}
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Group)) return false;
			return getName().equals(((Group)o).getName());
		}
		public Group cloneGroup() {
			Group g = new Group(iName, iType, iEditable);
			g.setColor(getColor());
			return g;
		}
		public String getColor() {
			return iColor;
		}
		public void setColor(String color) {
			iColor = color;
			addStyleName(color);
		}
		public int compareTo(Group g) {
			return getName().compareTo(g.getName());
		}
	}
	
	public void assignGroup(String oldName, String name, int type) {
		Group g = null;
		for (Group x: iGroups) {
			if (x.getName().equals(oldName == null ? name : oldName)) { g = x; break; }
		}
		if (g == null) {
			if (name == null || name.isEmpty()) return;
			g = new Group(name, type, true);
			colors: for (String c: sColors) {
				for (Group x: iGroups) {
					if (x.getColor().equals(c)) continue colors;
				}
				g.setColor(c);
				break;
			}
			iGroups.add(g);
		} else {
			rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
				HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
				for (int i = 0; i < p.getWidgetCount(); i++) {
					Group x = (Group)p.getWidget(i);
					if (x.equals(g)) {
						if (name == null || name.isEmpty()) {
							p.remove(i);
							continue rows;
						} else {
							x.setName(name); x.setType(type);
						}
					}
				}
			}
			if (name == null || name.isEmpty()) {
				iGroups.remove(g);
				return;
			} else {
				g.setName(name);
				g.setType(type);
			}
		}
		if (oldName != null) return;
		boolean nothing = true;
		boolean hasNoGroup = false;
		rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
			if (!isSelected(row)) continue;
			nothing = false;
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < p.getWidgetCount(); i++) {
				Group x = (Group)p.getWidget(i);
				if (x.equals(g)) continue rows;
			}
			hasNoGroup = true;
			break;
		}
		if (nothing) {
			boolean select = false;
			for (int row = 1; row < iTable.getRowCount(); row++ ) {
				HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
				for (int i = 0; i < p.getWidgetCount(); i++) {
					Group x = (Group)p.getWidget(i);
					if (x.equals(g)) {
						setSelected(row, true);
						select = true;
					}
				}
			}
			if (select) return;
		}
		rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
			if (!isSelected(row)) continue;
			setSelected(row, false);
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < p.getWidgetCount(); i++) {
				Group x = (Group)p.getWidget(i);
				if (x.equals(g)) {
					if (!hasNoGroup) p.remove(i);
					continue rows;
				}
			}
			p.add(g.cloneGroup());
		}
		boolean found = false;
		rows: for (int row = 1; row < iTable.getRowCount(); row++ ) {
			HorizontalPanel p = (HorizontalPanel)iTable.getWidget(row, 0);
			for (int i = 0; i < p.getWidgetCount(); i++) {
				Group x = (Group)p.getWidget(i);
				if (x.equals(g)) {
					found = true; break rows;
				}
			}
		}
		if (!found) iGroups.remove(g);
	}
	
	public List<Group> getGroups() { return iGroups; }
	
	public boolean isSelected(int row) {
		String style = iTable.getRowFormatter().getStyleName(row);
		return "unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style);
	}
	
	public void setSelected(int row, boolean selected) {
		String style = iTable.getRowFormatter().getStyleName(row);
		boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
		iTable.getRowFormatter().setStyleName(row, "unitime-TableRow" + (selected ? "Selected" : "") + (hover ? "Hover" : ""));
	}
	
	public int getSelectedCount() {
		int selected = 0;
		for (int row = 1; row < iTable.getRowCount(); row ++)
			if (isSelected(row)) selected ++;
		return selected;
	}
	
	public class MyFlexTable extends FlexTable {
		private boolean iEnabled = true;
		private StudentsTable iStudentsTable = null;
		private int iLastHoverRow = -1;
		
		public MyFlexTable() {
			super();
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONMOUSEMOVE);
			sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONKEYDOWN);
		}
		
		public void setEnabled(boolean enabled) { iEnabled = enabled; }
		public boolean isEnabled() { return iEnabled; }
		
		private boolean focus(Event event, int oldRow, int oldCol, int row, int col) {
			final Widget w = getWidget(row, col);
			if (getCellFormatter().isVisible(row, col) && w != null && w instanceof Focusable) {
				if (oldCol == 1) {
					((CurriculaCourseSelectionBox)getWidget(oldRow, oldCol)).hideSuggestionList();
				}
				((Focusable)w).setFocus(true);
				if (w instanceof TextBoxBase) {
					DeferredCommand.addCommand(new Command() {
						@Override
						public void execute() {
							((TextBoxBase)w).selectAll();
						}
					});
				}
				event.stopPropagation();
				return true;
			}
			return false;
		}
		
		private boolean swapRow(Event event, int oldRow, int oldCol, int row, int col) {
			Widget w = getWidget(row, col);
			if (getCellFormatter().isVisible(row, col) && w != null && w instanceof Focusable) {
				if (oldCol == 1) {
					((CurriculaCourseSelectionBox)getWidget(oldRow, oldCol)).hideSuggestionList();
				}
				swap(oldRow - 1, row - 1);
				((Focusable)w).setFocus(true);
				event.stopPropagation();
				return true;
			}
			return false;
		}
		
		public void clearHover() {
		    if (iLastHoverRow <= 0 || iLastHoverRow >= getRowCount()) return;
			String style = getRowFormatter().getStyleName(iLastHoverRow);
			boolean selected = ("unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style));
			getRowFormatter().setStyleName(iLastHoverRow, "unitime-TableRow" + (selected ? "Selected" : ""));
		}
		
		public void onBrowserEvent(Event event) {
			if (!iEnabled) return;
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    final Element tr = DOM.getParent(td);
			int col = DOM.getChildIndex(tr, td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    
		    if (row == 0) return;
			String style = getRowFormatter().getStyleName(row);

		    switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				iLastHoverRow = row;
				if ("unitime-TableRowSelected".equals(style))
					getRowFormatter().setStyleName(row, "unitime-TableRowSelectedHover");	
				else
					getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				
				if (iStudentsTable != null && iStudentsTable.isShowing()) {
					iStudentsTable.hide();
					iStudentsTable = null;
				}
				if (canShowStudentsTable(row)) {
					iStudentsTable = new StudentsTable(row);
					if (iStudentsTable.canShow()) {
						final int x = event.getClientX();
						iStudentsTable.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
							@Override
							public void setPosition(int offsetWidth, int offsetHeight) {
								boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 15 + offsetHeight > Window.getClientHeight());
								iStudentsTable.setPopupPosition(
										Math.max(Math.min(x, tr.getAbsoluteRight() - offsetWidth - 15), tr.getAbsoluteLeft() + 15),
//										Math.min(Math.max(x, tr.getAbsoluteLeft() + 15), tr.getAbsoluteRight() - offsetWidth - 15),
										top ? tr.getAbsoluteTop() - offsetHeight - 15 : tr.getAbsoluteBottom() + 15);
							}
						});
					} else {
						iStudentsTable = null;
					}
				}
				break;
			case Event.ONMOUSEMOVE:
				if (iStudentsTable != null && iStudentsTable.isShowing()) {
					boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 15 + iStudentsTable.getOffsetHeight() > Window.getClientHeight());
					iStudentsTable.setPopupPosition(
							Math.max(Math.min(event.getClientX(), tr.getAbsoluteRight() - iStudentsTable.getOffsetWidth() - 15), tr.getAbsoluteLeft() + 15),
							top ? tr.getAbsoluteTop() - iStudentsTable.getOffsetHeight() - 15 : tr.getAbsoluteBottom() + 15);
				}
				break;
			case Event.ONMOUSEOUT:
				if (iStudentsTable != null && iStudentsTable.isShowing()) {
					iStudentsTable.hide();
					iStudentsTable = null;
				}
				if ("unitime-TableRowHover".equals(style))
					getRowFormatter().setStyleName(row, null);	
				else if ("unitime-TableRowSelectedHover".equals(style))
					getRowFormatter().setStyleName(row, "unitime-TableRowSelected");	
				break;
			case Event.ONCLICK:
				Element element = DOM.eventGetTarget(event);
				while (DOM.getElementProperty(element, "tagName").equalsIgnoreCase("div"))
					element = DOM.getParent(element);
				if (DOM.getElementProperty(element, "tagName").equalsIgnoreCase("td")) {
					boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
					boolean selected = !("unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style));
					getRowFormatter().setStyleName(row, "unitime-TableRow" + (selected ? "Selected" : "") + (hover ? "Hover" : ""));
				}
				break;
			case Event.ONKEYDOWN:
				int oldRow = row, oldCol = col;
				if (event.getKeyCode() == KeyCodes.KEY_RIGHT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col++;
						if (col >= getCellCount(row)) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_LEFT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col--;
						if (col < 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && (event.getAltKey() || event.getMetaKey())) {
					do {
						row--;
						if (row <= 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && (event.getAltKey() || event.getMetaKey())) {
					do {
						row++;
						if (row >= getRowCount()) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && event.getCtrlKey()) {
					do {
						row--;
						if (row <= 0) break;
					} while (!swapRow(event, oldRow, oldCol, row, col));
					event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && event.getCtrlKey()) {
					do {
						row++;
						if (row >= getRowCount()) break;
					} while (!swapRow(event, oldRow, oldCol, row, col));
					event.preventDefault();
				}
				break;
			}
		}
	}
	
	public void showOnlyCourses(TreeSet<CourseInterface> courses) {
		iVisibleCourses = new TreeSet<String>();
		for (CourseInterface c: courses) iVisibleCourses.add(c.getCourseName());
		for (int row = 1; row < iTable.getRowCount(); row++) {
			String courseName = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
			if (iVisibleCourses.contains(courseName)) {
				((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setEnabled(false);
				iTable.getCellFormatter().setVisible(row, 0, true);
				iTable.getCellFormatter().setVisible(row, 1, true);
				for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
					boolean vis = iClassifications.getExpected(i) != null;
					iTable.getCellFormatter().setVisible(row, 2 + 2 * i, vis);
					iTable.getCellFormatter().setVisible(row, 3 + 2 * i, vis);
				}
			} else {
				iTable.getCellFormatter().setVisible(row, 0, false);
				iTable.getCellFormatter().setVisible(row, 1, false);
				for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
					iTable.getCellFormatter().setVisible(row, 2 + 2 * i, false);
					iTable.getCellFormatter().setVisible(row, 3 + 2 * i, false);
				}
			}
		}
	}
	
	public void showAllCourses() {
		if (iVisibleCourses != null) {
			for (int i = 1; i < iTable.getRowCount(); i++) {
				String courseName = ((CurriculaCourseSelectionBox)iTable.getWidget(i, 1)).getCourse();
				setSelected(i, iVisibleCourses.contains(courseName));
			}
		}
		iVisibleCourses = null;
		for (int row = 1; row < iTable.getRowCount(); row++) {
			((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).setEnabled(true);
			iTable.getCellFormatter().setVisible(row, 0, true);
			iTable.getCellFormatter().setVisible(row, 1, true);
			for (int i = 0; i < iClassifications.getClassifications().size(); i++) {
				boolean vis = iClassifications.getExpected(i) != null;
				iTable.getCellFormatter().setVisible(row, 2 + 2 * i, vis);
				iTable.getCellFormatter().setVisible(row, 3 + 2 * i, vis);
			}
		}
	}
	
	public boolean canShowStudentsTable(int row) {
		if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.NONE) return false;
		if (row < 1 || row >= iTable.getRowCount()) return false;
		String course = ((CurriculaCourseSelectionBox)iTable.getWidget(row, 1)).getCourse();
		if (iLastCourses == null || !iLastCourses.containsKey(course)) return false;
		int nrOther = 0;
		for (int r = 1; r < iTable.getRowCount(); r ++) {
			if (r == row || !isSelected(r)) continue;
			nrOther ++;
		}
		return (nrOther > 0);
	}
	
	public class StudentsTable extends PopupPanel {
		private FlexTable iT = new FlexTable();
		private VerticalPanel iP = new VerticalPanel();
		private boolean iCanShow = false;
		
		private int count(CurriculumStudentsInterface c, Set<Long> students) {
			if (CurriculumCookie.getInstance().getCurriculaCoursesMode() != Mode.PROJ || c == null) return students.size();
			return c.countProjectedStudents(students);
		}
		
		private StudentsTable(int currentRow) {
			super();
			
			setStyleName("unitime-PopupHint");

			if (iLastCourses == null) return;

			String course = ((CurriculaCourseSelectionBox)iTable.getWidget(currentRow, 1)).getCourse();
			
			CurriculumStudentsInterface[] thisCourse = iLastCourses.get(course);
			CurriculumStudentsInterface[] totals = iLastCourses.get("");
			if (thisCourse == null) return;
			
			iP.add(new Label("Comparing " + course + " " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName().toLowerCase().replace(" enrollment", "") + " students with the other selected courses:"));
			iP.add(iT);
			setWidget(iP);
			
			int column = 0;
			for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
				if (iClassifications.getExpected(c) == null) continue;
				iT.setText(0, 1 + column, iClassifications.getName(c));
				iT.getCellFormatter().setWidth(0, 1 + column, "50px");
				iT.getCellFormatter().setStyleName(0, 1 + column, "unitime-DashedBottom");
				column++;
			}
			
			iT.setText(1, 0, "Students in at least 1 other course");
			iT.setText(2, 0, "Students in at least 2 other courses");
			iT.setText(3, 0, "Students in at least 3 other courses");
			iT.setText(4, 0, "Students in all other courses");
			iT.setText(5, 0, "Students not in any other course");
			int row = 0;
			List<CurriculumStudentsInterface[]> other = new ArrayList<CurriculumStudentsInterface[]>();
			for (int r = 1; r < iTable.getRowCount(); r ++) {
				if (r == currentRow || !isSelected(r)) continue;
				String c = ((CurriculaCourseSelectionBox)iTable.getWidget(r, 1)).getCourse();
				if (c.isEmpty()) continue;
				other.add(iLastCourses.get(c));
				iT.setText(6 + row, 0, "Students shared with " + c);
				row++;
			}

			column = 0;
			int total = 0;
			int totalC[] = new int [other.size()];
			for (int i = 0; i < totalC.length; i++)
				totalC[i] = 0;
			boolean has1 = false, has2 = false, has3 = false, hasAll = false, hasNone = false;
			for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
				CurriculumStudentsInterface tc = totals[c];
				if (iClassifications.getExpected(c) == null) continue;
				Set<Long> thisEnrollment = (thisCourse[c] == null ? null : (CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL ? thisCourse[c].getEnrolledStudents() : thisCourse[c].getLastLikeStudents()));
				if (thisEnrollment != null && count(tc,thisEnrollment) != 0) {
					total += thisEnrollment.size();
					Set<Long> sharedWithOneOther = new HashSet<Long>();
					Set<Long> sharedWithTwoOther = new HashSet<Long>();
					Set<Long> sharedWithThreeOther = new HashSet<Long>();
					Set<Long> sharedWithAll = new HashSet<Long>(thisEnrollment);
					Set<Long> notShared = new HashSet<Long>(thisEnrollment);
					row = 0;
					for (CurriculumStudentsInterface[] o: other) {
						Set<Long> enrl = (o == null || o[c] == null ? null : CurriculumCookie.getInstance().getCurriculaCoursesMode() == Mode.ENRL  ? o[c].getEnrolledStudents() : o[c].getLastLikeStudents());
						if (enrl == null) {
							sharedWithAll.clear();
							row++;
							continue;
						}
						Set<Long> share = new HashSet<Long>();
						for (Long s: thisEnrollment) {
							if (enrl.contains(s)) {
								if (!sharedWithOneOther.add(s))
									if (!sharedWithTwoOther.add(s))
										sharedWithThreeOther.add(s);
								share.add(s);
							}
						}
						for (Iterator<Long> i = sharedWithAll.iterator(); i.hasNext(); )
							if (!enrl.contains(i.next())) i.remove();
						for (Iterator<Long> i = notShared.iterator(); i.hasNext(); )
							if (enrl.contains(i.next())) i.remove();
						if (!share.isEmpty() && count(tc, share) != 0) {
							totalC[row] += share.size();
							iT.setText(6 + row, 1 + column, (CurriculumCookie.getInstance().getCurriculaCoursesPercent() ? NF.format(100.0 * count(tc, share) / count(tc,thisEnrollment)) + "%" : "" + count(tc,share)));
						}
						row++;
					}
					boolean percent = CurriculumCookie.getInstance().getCurriculaCoursesPercent();
					if (!sharedWithOneOther.isEmpty() && count(tc,sharedWithOneOther) != 0) {
						iT.setText(1, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithOneOther) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithOneOther)));
						has1 = true;
					}
					if (!sharedWithTwoOther.isEmpty() && count(tc,sharedWithTwoOther) != 0) {
						iT.setText(2, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithTwoOther) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithTwoOther)));
						has2 = true;
					}
					if (!sharedWithThreeOther.isEmpty() && count(tc,sharedWithThreeOther) != 0) {
						iT.setText(3, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithThreeOther) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithThreeOther)));
						has3 = true;
					}
					if (!sharedWithAll.isEmpty() && count(tc,sharedWithAll) != 0) {
						iT.setText(4, 1 + column, (percent ? NF.format(100.0 * count(tc,sharedWithAll) / count(tc,thisEnrollment)) + "%" : "" + count(tc,sharedWithAll)));
						hasAll = true;
					}
					if (!notShared.isEmpty() && count(tc,notShared) != 0) {
						iT.setText(5, 1 + column, (percent ? NF.format(100.0 * count(tc,notShared) / count(tc,thisEnrollment)) + "%" : "" + count(tc,notShared)));
						hasNone = true;
					}
				}
				column ++;
			}
			if (!has1 || other.size() == 1) iT.getRowFormatter().setVisible(1, false);
			if (!has2 || other.size() == 1) iT.getRowFormatter().setVisible(2, false);
			if (!has3 || other.size() == 1) iT.getRowFormatter().setVisible(3, false);
			if (!hasAll || other.size() <= 3) iT.getRowFormatter().setVisible(4, false);
			if (!hasNone || other.size() == 1) iT.getRowFormatter().setVisible(5, false);
			if (other.size() > 1) {
				int minTotal = -1;
				List<Integer> visible = new ArrayList<Integer>();
				for (row = other.size() - 1; row >= 0; row--) {
					if (totalC[row] < 1)
						iT.getRowFormatter().setVisible(6 + row, false);
					else {
						visible.add(row);
						if (minTotal < 0 || minTotal < totalC[row])
							minTotal = totalC[row];
					}
				}
				while (visible.size() > 10) {
					int limit = minTotal; minTotal = -1;
					for (Iterator<Integer> i = visible.iterator(); i.hasNext() && visible.size() > 10; ) {
						row = i.next();
						if (totalC[row] <= limit) {
							iT.getRowFormatter().setVisible(6 + row, false);
							i.remove();
						} else {
							if (minTotal < 0 || minTotal < totalC[row])
								minTotal = totalC[row];
						}
					}
				}
				if (!visible.isEmpty()) {
					int r = 6 + visible.get(visible.size() - 1);
					int col = 1;
					for (int c = 0; c < iClassifications.getClassifications().size(); c++) {
						if (iClassifications.getExpected(c) == null) continue;
						if (iT.getCellCount(r) <= col || iT.getText(r, col) == null || iT.getText(r, col).isEmpty()) iT.setHTML(r, col, "&nbsp;");
						iT.getCellFormatter().setStyleName(r, col, "unitime-DashedTop");
						col++;
					}
				}

			}
						
			iCanShow = has1 || has2 || hasAll || hasNone;
		}
		
		public boolean canShow() { return iCanShow; }
		
	}
}