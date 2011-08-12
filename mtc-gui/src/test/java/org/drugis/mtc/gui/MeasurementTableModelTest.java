/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.mtc.gui;

import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.ObservableList;
import java.util.Arrays;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.drugis.common.JUnitUtil;
import static org.easymock.EasyMock.*;

public class MeasurementTableModelTest {
	ObservableList<TreatmentModel> d_treatments;
	ObservableList<StudyModel> d_studies;
	MeasurementTableModel d_model;

	@Before
	public void setUp() {
		TreatmentModel fluox = new TreatmentModel();
		fluox.setId("Fluox");
		TreatmentModel parox = new TreatmentModel();
		parox.setId("Parox");
		TreatmentModel venla = new TreatmentModel();
		venla.setId("Venla");
		d_treatments = new ArrayListModel<TreatmentModel>(Arrays.asList(fluox, parox, venla));

		StudyModel study1 = new StudyModel();
		study1.setId("study1");
		study1.getTreatments().add(fluox);
		study1.getTreatments().add(venla);
		StudyModel study2 = new StudyModel();
		study1.setId("study2");
		d_studies = new ArrayListModel<StudyModel>(Arrays.asList(study1, study2));

		d_model = new MeasurementTableModel(d_studies, d_treatments);
	}

	@Test
	public void testInitialColumns() {
		assertEquals(3, d_model.getColumnCount());
		assertEquals("", d_model.getColumnName(0));
		assertEquals("Responders", d_model.getColumnName(1));
		assertEquals("Sample size", d_model.getColumnName(2));
	}

	@Test
	public void testInitialRows() {
		assertEquals(4, d_model.getRowCount());
	}

	@Test
	public void testInitialValues() {
		assertEquals(d_studies.get(0), d_model.getValueAt(0, 0));
		assertEquals(null, d_model.getValueAt(0, 1));
		assertEquals(null, d_model.getValueAt(0, 2));
		assertEquals(d_studies.get(1), d_model.getValueAt(3, 0));
		assertEquals(null, d_model.getValueAt(3, 1));
		assertEquals(null, d_model.getValueAt(3, 2));
		assertEquals(d_treatments.get(0), d_model.getValueAt(1, 0));
		assertEquals(0, d_model.getValueAt(1, 1));
		assertEquals(0, d_model.getValueAt(1, 2));
		assertEquals(d_treatments.get(2), d_model.getValueAt(2, 0));
		assertEquals(0, d_model.getValueAt(2, 1));
		assertEquals(0, d_model.getValueAt(2, 2));
	}

	@Test
	public void testAddStudyAtEndEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 4, 4, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.add(new StudyModel());
		verify(mock);
	}

	@Test
	public void testAddStudyMiddleEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 3, 5, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		StudyModel study = new StudyModel();
		study.getTreatments().add(d_treatments.get(1));
		study.getTreatments().add(d_treatments.get(2));
		d_studies.add(1, study);
		verify(mock);
	}

	@Test
	public void testAddTreatmentMiddleEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 2, 2, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.get(0).getTreatments().add(1, d_treatments.get(1));
		verify(mock);

		assertEquals(d_treatments.get(0), d_model.getValueAt(1, 0));
		assertEquals(d_treatments.get(1), d_model.getValueAt(2, 0));
		assertEquals(d_treatments.get(2), d_model.getValueAt(3, 0));
		assertEquals(d_studies.get(1), d_model.getValueAt(4, 0));
	}

	@Test
	public void testRemoveTreatmentMiddleEventChaining() {
		TableModelEvent event = new TableModelEvent(d_model, 2, 2, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.get(0).getTreatments().remove(1);
		verify(mock);

		assertEquals(d_studies.get(0), d_model.getValueAt(0, 0));
		assertEquals(d_treatments.get(0), d_model.getValueAt(1, 0));
		assertEquals(d_studies.get(1), d_model.getValueAt(2, 0));
	}

	@Test
	public void testRemoveStudyMiddleEventChaining() {
		StudyModel study = new StudyModel();
		study.getTreatments().add(d_treatments.get(1));
		study.getTreatments().add(d_treatments.get(2));
		d_studies.add(1, study);

		TableModelEvent event = new TableModelEvent(d_model, 3, 5, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
		TableModelListener mock = createStrictMock(TableModelListener.class);
		d_model.addTableModelListener(mock);
		mock.tableChanged(TableModelEventMatcher.eqTableModelEvent(event));
		replay(mock);

		d_studies.remove(1);
		verify(mock);
	}

	@Test
	public void testIsCellEditable() {
		assertFalse(d_model.isCellEditable(0, 0));
		assertFalse(d_model.isCellEditable(0, 1));
		assertFalse(d_model.isCellEditable(0, 2));
		assertFalse(d_model.isCellEditable(1, 0));
		assertTrue(d_model.isCellEditable(1, 1));
		assertTrue(d_model.isCellEditable(1, 2));
		assertFalse(d_model.isCellEditable(2, 0));
		assertTrue(d_model.isCellEditable(2, 1));
		assertTrue(d_model.isCellEditable(2, 2));
		assertFalse(d_model.isCellEditable(3, 0));
		assertFalse(d_model.isCellEditable(3, 1));
		assertFalse(d_model.isCellEditable(3, 2));
	}

	@Test
	public void testEditing() {
		d_model.setValueAt(37, 1, 1);
		assertEquals(37, d_studies.get(0).getResponders(d_treatments.get(0)));
		assertEquals(37, d_model.getValueAt(1, 1));
	}
}


