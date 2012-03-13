package org.blitzem.util;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Utility class to support textual rendering of tables to STDOUT.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class Table {

	/**
	 * Utility class.
	 */
	private Table() {}
	
	/**
	 * Print out information in tabular format. Adapted from {@see http://stackoverflow
	 * .com/questions/275338/java-print-a-2d-string-array-as-a-right
	 * -justified-table/275438#275438}
	 * 
	 * @param table
	 *            a list of table rows, each of which should be a list of
	 *            strings (for each entry in the row).
	 * @param headerUnderline
	 *            whether to underline the header.
	 */
	public static void printTable(List<List<String>> table, boolean headerUnderline) {
			// Find out what the maximum number of columns is in any row
			int maxColumns = 0;
			for (List<String> row : table) {
				maxColumns = Math.max(row.size(), maxColumns);
			}

			// Find the maximum length of a string in each column
			int[] lengths = new int[maxColumns];
			for (List<String> row : table) {
				for (int j = 0; j < row.size(); j++) {
					lengths[j] = Math.max(row.get(j).length(), lengths[j]);
				}
			}

			if (headerUnderline) {
				List<String> underlineRow = Lists.newArrayList();
				for (int j = 0; j < maxColumns; j++) {
					underlineRow.add(Strings.repeat("=", lengths[j]));
				}
				table.add(1, underlineRow);
			}

			// Generate a format string for each column
			String[] formats = new String[lengths.length];
			for (int i = 0; i < lengths.length; i++) {
				formats[i] = "%1$" + lengths[i] + "s" + (i + 1 == lengths.length ? "\n" : " ");
			}

			// Print 'em out
			for (List<String> row : table) {
				for (int j = 0; j < row.size(); j++) {
					System.out.printf(formats[j], row.get(j));
				}
			}
		}

}
