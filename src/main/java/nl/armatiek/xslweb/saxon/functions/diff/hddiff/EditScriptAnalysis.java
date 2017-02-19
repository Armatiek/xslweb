/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package nl.armatiek.xslweb.saxon.functions.diff.hddiff;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sksamuel.diffpatch.DiffMatchPatch;
import com.sksamuel.diffpatch.DiffMatchPatch.Diff;

import de.fau.cs.osr.hddiff.editscript.EditOp;
import de.fau.cs.osr.hddiff.editscript.EditOpDelete;
import de.fau.cs.osr.hddiff.editscript.EditOpInsert;
import de.fau.cs.osr.hddiff.editscript.EditOpMove;
import de.fau.cs.osr.hddiff.editscript.EditOpUpdate;
import de.fau.cs.osr.hddiff.utils.ReportItem;

public class EditScriptAnalysis
{
	private LinkedList<Edit> editScript = new LinkedList<>();
	
	private Map<GenericEditOp, List<Edit>> editsByOp = new HashMap<>();
	
	private Map<String, List<Edit>> editsByLabel = new HashMap<>();
	
	private int charsInserted;
	
	private int charsDeleted;
	
	private DiffMatchPatch dfp;
	
	// =========================================================================
	
	public EditScriptAnalysis()
	{
	}
	
	public EditScriptAnalysis(LinkedList<Diff> diff)
	{
		for (Diff d : diff)
		{
			switch (d.operation)
			{
				case DELETE:
					charsDeleted += d.text.length();
					break;
				case INSERT:
					charsInserted += d.text.length();
					break;
				case EQUAL:
				default:
					break;
			}
		}
	}
	
	public EditScriptAnalysis(List<EditOp> es)
	{
		dfp = new DiffMatchPatch();
		for (EditOp eo : es)
		{
			switch (eo.getType())
			{
				case INSERT:
					addInsert((EditOpInsert) eo);
					break;
				case DELETE:
					addDelete((EditOpDelete) eo);
					break;
				case MOVE:
					addMove((EditOpMove) eo);
					break;
				case UPDATE:
					addUpdate((EditOpUpdate) eo);
					break;
				case SPLIT:
				  throw new UnsupportedOperationException();
			}
		}
	}
	
	// =========================================================================
	
	private void addUpdate(EditOpUpdate upd)
	{
		if (upd.getUpdatedNode().isTextLeaf())
		{
			LinkedList<Diff> diff = dfp.diff_main(
					upd.getUpdatedNode().getTextContent(),
					upd.getUpdatedNodeRight().getTextContent(), false);
			
			int charsDeleted = 0;
			int charsInserted = 0;
			for (Diff d : diff)
			{
				switch (d.operation)
				{
					case DELETE:
						charsDeleted += d.text.length();
						break;
					case INSERT:
						charsInserted += d.text.length();
						break;
					case EQUAL:
					default:
						break;
				}
			}
			
			add(new Edit(
					GenericEditOp.UPD,
					charsInserted,
					charsDeleted));
		}
		else
		{
			add(new Edit(
					GenericEditOp.UPD,
					upd.getUpdatedNode().getLabel(),
					null));
		}
	}
	
	private void addMove(EditOpMove mov)
	{
		add(new Edit(
				GenericEditOp.MOV,
				mov.getMovedNode().getLabel(),
				null));
	}
	
	private void addDelete(EditOpDelete del)
	{
		add(new Edit(
				GenericEditOp.DEL,
				del.getDeletedNode().getLabel(),
				null));
		
		if (del.getDeletedNode().isTextLeaf())
		{
			add(new Edit(
					GenericEditOp.DEL,
					null,
					del.getDeletedNode().getTextContent()));
		}
	}
	
	private void addInsert(EditOpInsert ins)
	{
		add(new Edit(
				GenericEditOp.INS,
				ins.getInsertedNode().getLabel(),
				null));
		
		if (ins.getInsertedNode().isTextLeaf())
		{
			add(new Edit(
					GenericEditOp.INS,
					null,
					ins.getInsertedNode().getTextContent()));
		}
	}
	
	public void add(Edit edit)
	{
		editScript.add(edit);
		addMultiMap(editsByOp, edit.op, edit);
		addMultiMap(editsByLabel, edit.label, edit);
		switch (edit.op)
		{
			case DEL:
				if (edit.text != null)
					charsDeleted += edit.text.length();
				break;
			
			case INS:
				if (edit.text != null)
					charsInserted += edit.text.length();
				break;
			
			case UPD:
				charsInserted += edit.ins;
				charsDeleted += edit.del;
				break;
			
			default:
				break;
		}
	}
	
	public List<Edit> getEditScript()
	{
		return Collections.unmodifiableList(editScript);
	}
	
	// =========================================================================
	
	@Override
	public String toString()
	{
		return toShortString() +
				printNodeStatsByOp(GenericEditOp.INS) +
				printNodeStatsByOp(GenericEditOp.DEL) +
				printNodeStatsByOp(GenericEditOp.MOV) +
				printNodeStatsByOp(GenericEditOp.UPD);
	}
	
	public String toShortString()
	{
		return String.format("" +
				"GenericEditScriptAnalysis:\n" +
				"  Edit script length: %d\n" +
				"    Insertions: %4d\n" +
				"    Deletions:  %4d\n" +
				"    Moves:      %4d\n" +
				"    Updates:    %4d\n" +
				"    Chars Ins:  %4d\n" +
				"    Chars Del:  %4d\n",
				editScript.size(),
				getMultiMapSize(editsByOp, GenericEditOp.INS),
				getMultiMapSize(editsByOp, GenericEditOp.DEL),
				getMultiMapSize(editsByOp, GenericEditOp.MOV),
				getMultiMapSize(editsByOp, GenericEditOp.UPD),
				charsInserted,
				charsDeleted);
	}
	
	public void report(ReportItem item, String cat)
	{
		item.recordFigure(cat + "a) Edit script size", editScript.size(), "#ops");
		item.recordFigure(cat + "b) Inserts", getMultiMapSize(editsByOp, GenericEditOp.INS), "#ops");
		item.recordFigure(cat + "c) Deletes", getMultiMapSize(editsByOp, GenericEditOp.DEL), "#ops");
		item.recordFigure(cat + "d) Moves", getMultiMapSize(editsByOp, GenericEditOp.MOV), "#ops");
		item.recordFigure(cat + "e) Updates", getMultiMapSize(editsByOp, GenericEditOp.UPD), "#ops");
		item.recordFigure(cat + "g) Chars ins", charsInserted, "#ops");
		item.recordFigure(cat + "h) Chars del", charsDeleted, "#ops");
	}
	
	private Object printNodeStatsByOp(GenericEditOp needle)
	{
		HashSet<String> tmp = new HashSet<>(editsByLabel.keySet());
		tmp.remove(null);
		LinkedList<String> keys = new LinkedList<>(tmp);
		Collections.sort(keys);
		keys.add(null);
		StringBuilder b = new StringBuilder();
		b.append(String.format("    %s by label:\n", needle));
		boolean empty = true;
		for (String key : keys)
		{
			int count = 0;
			List<Edit> ops = editsByLabel.get(key);
			if (ops != null)
				for (Edit op : ops)
				{
					if (op.op == needle)
						++count;
				}
			if (count > 0)
			{
				empty = false;
				if (key == null)
					key = "#text";
				String paddedKey = StringUtils.rightPad(StringUtils.abbreviate(key, 16) + ":", 17);
				b.append(String.format("      %s %4d\n", paddedKey, count));
			}
		}
		return empty ? "" : b.toString();
	}
	
	// =========================================================================
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addMultiMap(
			Map map,
			Object key,
			Object value)
	{
		List x = (List) map.get(key);
		if (x == null)
		{
			x = new LinkedList();
			map.put(key, x);
		}
		x.add(value);
	}
	
	@SuppressWarnings("rawtypes")
	private int getMultiMapSize(Map editsByOp, Object key)
	{
		List list = (List) editsByOp.get(key);
		if (list == null)
			return 0;
		return list.size();
	}
	
	// =========================================================================
	
	public static enum GenericEditOp
	{
		DEL, INS, MOV, UPD, NONE
	}
	
	// =========================================================================
	
	public static final class Edit
	{
		public final GenericEditOp op;
		
		public final String label;
		
		public final String text;
		
		public final int ins;
		
		public final int del;
		
		// ---------------------------------------------------------------------
		
		public Edit(GenericEditOp op, String label, String text)
		{
			this.op = op;
			this.label = label;
			this.text = text;
			this.ins = 0;
			this.del = 0;
		}
		
		public Edit(GenericEditOp op, int ins, int del)
		{
			this.op = op;
			this.label = null;
			this.text = null;
			this.ins = ins;
			this.del = del;
		}
		
		@Override
		public String toString()
		{
			return "Edit [op=" + op + ", label=" + label + ", text=" + text + ", ins=" + ins + ", del=" + del + "]";
		}
	}
}
