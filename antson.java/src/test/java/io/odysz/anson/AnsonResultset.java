package io.odysz.anson;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;
import io.odysz.common.LangExt;
import io.odysz.common.Regex;

/**This Resultset is used for non-connected manipulation.
 * Rows and Cols are start at 1, the same as {@link java.sql.Resultset}.<br>
 * TODO This will be changed in the future (It's proved starting at 0 is more bug free).
 * 
 * @author odys-z@github.com
 *
 */
public class AnsonResultset extends Anson {
	private static final boolean debug = true;

	private int colCnt = 0;
	/**current row index, start at 1. */
	private int rowIdx = -1;
	private int rowCnt = 0;

	// TODO docs
	@AnsonField(valType="java.util.ArrayList")
	private ArrayList<ArrayList<Object>> results;

	/**col-index start at 1, map: [alais(upper-case), [col-index, db-col-name(in raw case)]<br>
	 * case 1<pre>
	String colName = rsMeta.getColumnLabel(i).toUpperCase();
	colnames.put(colName, new Object[] {i, rsMeta.getColumnLabel(i)});
	</pre>
	* case 2<pre>
	for (String coln : colnames.keySet()) 
		colnames.put(coln.toUpperCase(), new Object[] {colnames.get(coln), coln});
	</pre>
	 * */
	@AnsonField(valType="[Ljava.lang.Object;")
	private HashMap<String, Object[]> colnames;

	@AnsonField(ignoreTo = true)
	private ResultSet rs;

	@AnsonField(ignoreTo = true, ignoreFrom = true)
	private Connection conn;

	@AnsonField(ignoreTo = true, ignoreFrom = true)
	private Statement stmt;

	/**For paged query, this the total row count*/
	private int total = 0;

	private HashMap<Class<?>,String> stringFormats;

	/** for deserializing */
	public AnsonResultset() { }

	public AnsonResultset(ResultSet rs) throws SQLException {
		ICRconstructor(rs);
	}

	public AnsonResultset(ResultSet rs, Connection connection, Statement statement) throws SQLException {
		this.rs = rs;
		conn = connection;
		stmt = statement;
		ICRconstructor(rs);
		this.rs.beforeFirst();
	}

	public void ICRconstructor(ResultSet rs) throws SQLException {
		results = new ArrayList<ArrayList<Object>>();
		if (rs == null) return;
		ResultSetMetaData rsMeta = rs.getMetaData();
		colCnt = rsMeta.getColumnCount();
		colnames = new HashMap<String, Object[]>();
		for (int i = colCnt; i >= 1; i--) {
			// 2017-11-25, in mysql testing, getColumnName returning original db name, rsMeta.getColumnLabel() returning alias.
			// String colName = rsMeta.getColumnName(i).toUpperCase();
			String colName = rsMeta.getColumnLabel(i).toUpperCase();
			if (colnames.containsKey(colName)) {
				if (debug)
					System.err.println("WARN: Duplicated col name found, only the last one's index reserved: " + colName);
			}
			colnames.put(colName, new Object[] {i, rsMeta.getColumnLabel(i)});
		}
		rowIdx = 0;
		rowCnt = 0;
		while (rs.next()) {
			rowCnt++;
			ArrayList<Object> row = new ArrayList<Object>();
			for (int j = 1; j <= colCnt; j++) {
				row.add(rs.getObject(j));
			}
			results.add(row);
		}
	}

	public AnsonResultset(AnsonResultset icrs) throws SQLException {
		results = new ArrayList<ArrayList<Object>>();
		if (icrs == null) return;
		HashMap<String, Object[]> src_colnames = icrs.getColnames();
		colnames = new HashMap<String, Object[]>();
		for (String cname : src_colnames.keySet()) {
			Object[] v = src_colnames.get(cname);
			colnames.put(cname, new Object[] {v[0], new String((String) v[1])});
		}
		rowIdx = 0;
		rowCnt = 0;
		colCnt = src_colnames.keySet().size();
		while (icrs.next()) {
			rowCnt++;
			ArrayList<Object> row = new ArrayList<Object>();
			for (int j = 1; j <= colCnt; j++) {
				String v = icrs.getString(j);
				if (v == null) row.add("");
				else row.add(new String(v));
			}
			results.add(row);
		}
	}
	
	/**Construct an empty set, used for appending rows.
	 * @param colnames
	 */
	public AnsonResultset(HashMap<String, Integer> colnames) {
		results = new ArrayList<ArrayList<Object>>();
		colCnt = colnames.size();
		this.colnames = new HashMap<String, Object[]>(colCnt);
		for (String coln : colnames.keySet()) {
			this.colnames.put(coln.toUpperCase(), new Object[] {colnames.get(coln), coln});
		}
		rowIdx = 0;
		rowCnt = 0;
	}

	/**Construct an empty set, used for appending rows.
	 * Cols are deep copied.
	 * @param colnames
	 */
	public AnsonResultset(HashMap<String, Object[]> colnames, boolean toUpperCase) {
		results = new ArrayList<ArrayList<Object>>();
		colCnt = colnames.size();
		this.colnames = new HashMap<String, Object[]>(colCnt);
		for (String coln : colnames.keySet()) {
			this.colnames.put(toUpperCase ? coln.toUpperCase() : coln, colnames.get(coln));
		}
		rowIdx = 0;
		rowCnt = 0;
	}

	/**Append a new row - deep copy, set current row as the appended row.
	 * @param row
	 * @return this
	 */
	public AnsonResultset appendDeeply(ArrayList<Object> row) {
		ArrayList<Object> newRow = new ArrayList<Object>(row.size());
		for (int j = 0; j < row.size(); j++) {
			String v = "";
			try { v = row.get(j).toString(); }
			catch (Exception e) {}
			newRow.add(v);
		}
		results.add(newRow);
		rowCnt++;
		rowIdx = results.size();
		return this;
	}
	
	public AnsonResultset append(ArrayList<Object> includingRow) {
		results.add(includingRow);
		rowCnt++;
		rowIdx = results.size();
		return this;
	}

	/**For debug, generate a fake rs.
	 * @param rows
	 * @param cols
	 */
	public AnsonResultset(int rows, int cols, String... colPrefix) {
		if (rows <= 0 || cols <= 0)
			return;
		results = new ArrayList<ArrayList<Object>>(rows);
		colCnt = cols;
		colnames = new HashMap<String, Object[]>(cols);
		for (int i = colCnt; i >= 1; i--) {
			String colName = (colPrefix == null || colPrefix.length != 1) ? String.valueOf(i) : String.format("%s%s", colPrefix[0].trim(), i);
			colnames.put(colName.toUpperCase(), new Object[] {i, colName});
		}
		rowIdx = 0;
		rowCnt = 0;
		for (int r = 0; r < rows; r++) {
			rowCnt++;
			ArrayList<Object> row = new ArrayList<Object>(colCnt);
			for (int j = 1; j <= colCnt; j++) {
				row.add(String.format("%s, %s", r, j));
			}
			results.add(row);
		}
	}
	
	public AnsonResultset(int rows, String[] colNames, boolean generateData) {
		if (rows <= 0 || colNames == null  || colNames.length == 0)
			return;
		results = new ArrayList<ArrayList<Object>>(rows);
		colCnt = colNames.length;
		this.colnames = new HashMap<String, Object[]>(colCnt);
		for (int i = colCnt; i >= 1; i--) {
			String cn = colNames[i - 1] == null ? String.valueOf(i): colNames[i - 1];
			colnames.put(cn.toUpperCase(), new Object[] {i, cn});
		}
		rowIdx = 0;
		rowCnt = 0;
		for (int r = 0; r < rows; r++) {
			rowCnt++;
			ArrayList<Object> row = new ArrayList<Object>(colCnt);
			for (int j = 1; j <= colCnt; j++) {
				if (generateData)
					row.add(String.format("%s, %s", r, j));
				else row.add("");
			}
			results.add(row);
		}
	}

	public AnsonResultset(String[] colNames) {
		results = new ArrayList<ArrayList<Object>>(16);
		colCnt = colNames.length;
		this.colnames = new HashMap<String, Object[]>(colCnt);
		for (int i = colCnt; i >= 1; i--) {
			// colnames.put(colNames[i - 1] == null ? String.valueOf(i): colNames[i - 1].toUpperCase(), i);
			String cn = colNames[i - 1] == null ? String.valueOf(i): colNames[i - 1];
			colnames.put(cn.toUpperCase(), new Object[] {i, cn});
		}
		rowIdx = 0;
		rowCnt = 0;
	}

	public AnsonResultset(ArrayList<String> colNames) {
		results = new ArrayList<ArrayList<Object>>(16);
		colCnt = colNames.size();
		this.colnames = new HashMap<String, Object[]>(colCnt);
		for (int i = colCnt; i >= 1; i--) {
			// colnames.put(colNames.get(i - 1) == null ? String.valueOf(i): colNames.get(i - 1).toUpperCase(), i);
			String cn = colNames.get(i - 1) == null ? String.valueOf(i): colNames.get(i - 1);
			colnames.put(cn.toUpperCase(), new Object[] {i, cn});
		}
		rowIdx = 0;
		rowCnt = 0;
	}

	/** @return column names */
	public HashMap<String, Object[]> getColnames() {
		return colnames;
	}

	public boolean hasCol(String c) {
		return LangExt.isblank(c) ? false
				: getColnames().containsKey(c.trim().toUpperCase());
	}
	
	public ArrayList<ArrayList<Object>> getRows() {
		return results;
	}

	public boolean next() throws SQLException {
		rowIdx++;
		if (rs != null) rs.next();
		if (rowIdx > rowCnt) return false;
		else return true;
	}
	
	/**last start at 1, included in nexting range.<br>
	 * If current index = 4, nextUntill(5) return true;<br>
	 * If current index = 5, nextUntill(5) return false;
	 * @param last
	 * @return true: ok
	 * @throws SQLException
	 */
	public boolean nextUntill(int last) throws SQLException {
		rowIdx++;
		if (rs != null) rs.next();
		if (rowIdx > rowCnt || rowIdx > last) return false;
		else return true;
	}
	
	public int append(AnsonResultset more) throws SQLException {
		// check cols
		if (colCnt != more.getColCount()) throw new SQLException("Columns not matched.");
		results.addAll(((AnsonResultset)more).results);
		rowCnt += ((AnsonResultset)more).rowCnt;
		return rowCnt;
	}
	
	/**Add a formatter to type of clz when converting to String.
	 * @param clz
	 * @param format
	 * @return this
	 */
	public AnsonResultset stringFormat(Class<?> clz, String format) {
		if (stringFormats == null)
			stringFormats = new HashMap<Class<?>, String>();
		stringFormats.put(clz, format);
		return this;
	}
	
	public String getString(int colIndex) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) return null;
			if (results.get(rowIdx - 1).get(colIndex - 1) == null) return null;
			// else return results.get(rowIdx - 1).get(colIndex - 1).toString();
			else {
				Object v = results.get(rowIdx - 1).get(colIndex - 1);
				return stringFormats != null && stringFormats.containsKey(v.getClass()) ?
						String.format(stringFormats.get(v.getClass()), v) : v.toString();
			}
		} catch (Exception e) {
			throw new SQLException(e.getMessage() + " Empty Results?");
		}
	}
	
	public String getString(String colName) throws SQLException {
		if (colName == null) return null;
		return getString((Integer) (colnames.get(colName.toUpperCase())[0]));
	}
	
	/**If field is a date value, return string formatted by sdf.
	 * @param colName
	 * @param sdf
	 * @return string value
	 * @throws SQLException
	 */
	public String getString(String colName, SimpleDateFormat sdf) throws SQLException {
		if (colName == null) return null;
		return getString((Integer)colnames.get(colName.toUpperCase())[0], sdf);
	}
	
	/**If field is a date value, return string formatted by sdf.
	 * @param colIndex
	 * @param sdf
	 * @return string value
	 * @throws SQLException
	 */
	public String getString(int colIndex, SimpleDateFormat sdf) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) return null;
			if (results.get(rowIdx - 1).get(colIndex - 1) == null) return null;
			else {
				Object obj = results.get(rowIdx - 1).get(colIndex - 1);
				if (obj instanceof Date)
					return sdf.format(obj);

				// return results.get(rowIdx - 1).get(colIndex - 1).toString();
				Object v = results.get(rowIdx - 1).get(colIndex - 1);
				return stringFormats != null && stringFormats.containsKey(v.getClass()) ?
						String.format(stringFormats.get(v.getClass()), v) : v.toString();
			}
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}
	/**
	 * if null, change to ""
	 * @param colName
	 * @return string value
	 * @throws SQLException
	 */
	public String getStringNonull(String colName) throws SQLException {
		if (colName == null) return "";
		String s = getString((Integer)colnames.get(colName.toUpperCase())[0]);
		return s == null? "" : s;
	}
	
	/**if value is equals case insensitive to 1,true, yes, y, t, decimal > 0.001 return true, else return false;
	 * @param colIndex
	 * @return string value
	 * @throws SQLException
	 */
	public boolean getBoolean(int colIndex) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) return false;
			if (results.get(rowIdx - 1).get(colIndex - 1) == null) return false;
			else {
				try {
					String v = String.valueOf(results.get(rowIdx - 1).get(colIndex - 1)).trim();
					if (v == null) return false;
					v = v.toLowerCase();

					if (v.equals("1")) return true;
					if (v.equals("true")) return true;
					if (v.equals("yes")) return true;
					if (v.equals("y")) return true;
					if (v.equals("t")) return true;
					try {
						Double d = Double.valueOf(v);
						if (d >= 0.001d) return true;
					}
					catch (Exception e){}
					//if (v.equals("0")) return false;
					//if (v.equals("false")) return false;
					return false;
				} catch (Exception e) {
					return false;
				}
			}
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	public boolean getBoolean(String colName) throws SQLException {
		return getBoolean((Integer)colnames.get(colName.toUpperCase())[0]);
	}

	public double getDouble(int colIndex) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) throw new SQLException("Null row to be accessed.");
			if (results.get(rowIdx - 1).get(colIndex - 1) == null) throw new SQLException("Null value to be converted to double.");
			else return Double.valueOf(results.get(rowIdx - 1).get(colIndex - 1).toString());
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	public double getDouble(String colName) throws SQLException {
		return getDouble((Integer)colnames.get(colName.toUpperCase())[0]);
	}

	public BigDecimal getBigDecimal(int colIndex) throws SQLException {
		return BigDecimal.valueOf(getDouble(colIndex));
	}

	public BigDecimal getBigDecimal(String colName) throws SQLException {
		return BigDecimal.valueOf(getDouble((Integer)colnames.get(colName.toUpperCase())[0]));
	}
	
	public Date getDate(int index)throws SQLException{
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) throw new SQLException("Null row to be accessed.");
			if (results.get(rowIdx - 1).get(index - 1) == null) return null;
			// Oracle Datetime, Mysql Date, datetime can safely cast to date.
			// If your debugging arrived here, you may first check you database column type.
			else return (Date)results.get(rowIdx - 1).get(index - 1);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	public Date getDate(String colName)throws SQLException{
		return getDate((Integer)colnames.get(colName.toUpperCase())[0]);
	}

	public int getInt(int colIndex) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) throw new SQLException("Null row to be accessed.");
			if (results.get(rowIdx - 1).get(colIndex - 1) == null) throw new SQLException("Null value to be converted to int.");
			else return Integer.valueOf(results.get(rowIdx - 1).get(colIndex - 1).toString());
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	public int getInt(String col, int deflt) {
		try {
			return getInt(col);
		} catch (SQLException e) {
			return deflt;
		}
	}
	
	public long getLong(int colIndex) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) throw new SQLException("Null row to be accessed.");
			if (results.get(rowIdx - 1).get(colIndex - 1) == null) throw new SQLException("Null value to be converted to long.");
			else return Long.valueOf(results.get(rowIdx - 1).get(colIndex - 1).toString());
		}catch (Exception e) {throw new SQLException(e.getMessage());}
	}

	public long getLong(String colName) throws SQLException {
		return getLong((Integer)colnames.get(colName.toUpperCase())[0]);
	}

	public int getInt(String colName) throws SQLException {
		return getInt((Integer)colnames.get(colName.toUpperCase())[0]);
	}

	public Blob getBlob(int colIndex) throws SQLException {
		try {
			if (rs == null) throw new SQLException("Can not get Blob constructed by OracleHelper.select(). To access Blob, use OracleHelper.selectBlob()");
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) throw new SQLException("Null row to be accessed.");
			if (results.get(rowIdx - 1).get(colIndex - 1) == null) throw new SQLException("Null value to be converted to blob.");
			else return (Blob)rs.getBlob(colIndex);
		}catch (Exception e) {throw new SQLException(e.getMessage());}
	}
	
	public Blob getBlob(String colName) throws SQLException {
		return getBlob((Integer)colnames.get(colName.toUpperCase())[0]);
	}

	public Object getObject(int colIndex) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) throw new SQLException("Null row to be accessed.");
			//if (results.get(rowIdx - 1).get(colIndex - 1) == null) throw new SQLException("Null value to be converted to object.");
			else return results.get(rowIdx - 1).get(colIndex - 1);
		}catch (Exception e) {throw new SQLException(e.getMessage());}

	}

	/**
	 * Get current row index.<br>
	 * Row index start from 1.<br>
	 * The last row indix == getRowCount()
	 * @return string value
	 */
	public int getRow() {
		if (results == null) return 0;
		return rowIdx;
	}
	
	public int getColumnCount() {
		return colCnt;
	}
	
	public void first() throws SQLException {
		if (getRow() <= 0) throw new SQLException("Resultset out of boundary.");
		rowIdx = 1;
		if (rs != null) rs.first();
	}

	public AnsonResultset beforeFirst() throws SQLException {
		if (getRow() > 0) rowIdx = 0;
		if (rs != null) rs.beforeFirst();
		return this;
	}
	
	/**idx start from 1. before(1) == beforeFirst().<br>
	 * As java.sql.Resultset don't support this method,
	 * calling this will throw an exception if this object are created from a java.sql.Resultset.
	 * @param idx
	 * @return this
	 * @throws SQLException
	 */
	public AnsonResultset before(int idx) throws SQLException {
		if (rs != null) throw new SQLException("before(int) can't been called when there is an associate java.sql.Resultset.");
		rowIdx = idx - 1;
		return this;
	}

	public void close() throws SQLException {
		if (rs != null) {
			rs.close();
			stmt.close();
			conn.close();
			rs = null;
		}
	}

	public boolean previous() throws SQLException {
		rowIdx--;
		if (rs != null) rs.previous();
		if (0 < rowIdx && rowIdx <= rowCnt) return true;
		else return false;
	}

	/**Get col name in raw case.<br>
	 * @param i start at 1
	 * @return column name or null
	 */
	public String getColumnName(int i) {
		for (String cn : colnames.keySet()) {
			if (((Integer)colnames.get(cn)[0]) == i)
				return (String) colnames.get(cn)[1];
		}
		return null;
	}

	/**Set column raw name.
	 * @param i index
	 * @param n name
	 */
	public void setColumnName(int i, String n) {
		for (String cn : colnames.keySet()) {
			if (((Integer)colnames.get(cn)[0]) == i) {
				colnames.get(cn)[1] = n;
				break;
			}
		}
	}
	
	/**
	 * @param upper_bumps [key: UPPER-CASE, value: bumpCase]
	 * @return this (with column names updated)
	public SResultset setColumnCase(HashMap<String, String> upper_bumps) {
		if (upper_bumps == null) return this;

		for (String cn : colnames.keySet()) {
			if (upper_bumps.containsKey(cn)) {
				String bump =  (upper_bumps.get(cn));
				if (bump != null)
					colnames.get(cn)[1] = bump;
			}
		}
		return this;
	}
	 */

	/**/
	public int getRowCount() {
		return rowCnt;
	}

	public int getColCount() {
		return colCnt;
	}
	
	/**idx start at 0 */
	public ArrayList<Object> getRowAt(int idx) throws SQLException {
		if (results == null || idx < 0 || idx >= results.size()) 
			throw new SQLException("Row index out of boundary. idx: " + idx);
		return results.get(idx);
	}

	/**Set value to current row
	 * @param colIndex
	 * @param v
	 * @return this
	 * @throws SQLException 
	 */
	public AnsonResultset set(int colIndex, String v) throws SQLException {
		try {
			if (rowIdx <= 0 || results == null || results.get(rowIdx - 1) == null) return this;
			//if (results.get(rowIdx - 1).get(colIndex - 1) == null) return false;
			if (results.get(rowIdx - 1).size() < colIndex) return this;
			else {
				results.get(rowIdx - 1).set(colIndex - 1, v);
				return this;
			}
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	/**Set value to current row
	 * @param colName
	 * @param v
	 * @return this
	 * @throws SQLException 
	 */
	public AnsonResultset set (String colName, String v) throws SQLException {
		return set((Integer)colnames.get(colName.toUpperCase())[0], v);
	}

	/**find the first row that contain a matched value in field <i>col</i>. Matching are done by <i>regex</i>.
	 * @param col
	 * @param regex
	 * @return row index or 0
	 * @throws SQLException
	 */
	public int findFirst(String col, String regex) throws SQLException {
		beforeFirst();
		Regex regx = new Regex(regex);
		while(next()) {
			String target = getString(col);
			if (regx.match(target))
				return rowIdx;
		}
		return 0;
	}

	public ArrayList<Object> getRowCells() {
		return results.get(rowIdx - 1);
	}
	
	public int printSomeData(boolean err, int max, String... includeCols) {
		try {
			printHeaders();
			if (includeCols != null && includeCols.length > 0) {
				if (!"*".equals(includeCols[0])) {
					for (int ix = 0; ix < includeCols.length; ix++)
						if (err) System.err.print("\t" + includeCols[ix]);
						else System.out.print("\t" + includeCols[ix]);

					// line feed
					if (err) System.err.println("");
					else System.out.println("");

					beforeFirst();
					while (next() && getRow() <= max) {
						for (String incCol : includeCols) 
							printcell(err, incCol);
//					for (int ix = 0; ix <= max; ix++) {
//						if (!next())
//							break;
//						for (String incCol : includeCols) {
//							if (err)
//								System.err.print(String.format("%s : %s  ", incCol, getString(incCol)));
//							else
//								System.out.print(String.format("%s : %s  ", incCol, getString(incCol)));
//						}

						// end line
						if (err) System.err.println("");
						else System.out.println("");
					}
				}
				else {
					beforeFirst();
					while (next() && getRow() <= max) {
						for (int c = 1; c <= getColCount(); c++) 
							printcell(err, c);
				
						// end line
						if (err) System.err.println("");
						else System.out.println("");
					}
				}
			}
		} catch (Exception e) {}
		return results == null ? 0 : results.size();
	}

	private void printcell(boolean err, String c) throws SQLException {
		if (err)
			System.err.print("\t" + getString(c));
		else
			System.out.print("\t" + getString(c));
	}

	private void printcell(boolean err, int c) throws SQLException {
		if (err)
			System.err.print(String.format("%s : %s  ", c, getString(c)));
		else
			System.out.print(String.format("%s : %s  ", c, getString(c)));
	}

	private void printHeaders() {
		for (int c = 0; c < colnames.size(); c++)
			System.out.print(String.format("%s : %s\t", c + 1, getColumnName(c + 1)));

		System.out.println(String.format("\nrow count: %d", results == null ? 0 : results.size()));
	}

	/**get {rowCount, colCount, headers: {@link #colnames}, rows: {@link #results}}
	 * @param includeColHeader
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
//	public SemanticObject convert2Jarr(boolean includeColHeader) throws UnsupportedEncodingException {
//		// JsonObjectBuilder b = Json.createObjectBuilder();
//		SemanticObject b = new SemanticObject();
////		b.add("rowCount", results == null ? 0 : results.size());
////		b.add("colCount", colnames == null ? 0 : colnames.size());
////		if (gson == null) gson = new Gson();
////		b.add("headers", gson.toJson(colnames));
////
////		Type rt = new TypeToken<ArrayList<ArrayList<String>>>(){}.getType();
////		b.add("rows", gson.toJson(results, rt ));
//
//		return b;
//	}

	/**Collect fields value that can be used in "IN" condition, e.g. 'v1', 'v2', ...
	 * @param rs
	 * @param fields
	 * @return ['row0 field-val', 'row1 field-val', ...]
	 * @throws SQLException
	 */
	public static String collectFields(AnsonResultset rs, String... fields) throws SQLException {
		String s = "";
		if (rs != null) {
			rs.beforeFirst();
			while (rs.next()) {
				for (String f : fields) {
					if (s.length() > 0)
						s += ",";
					// 2018.10.07 see UpdateBatch#recordUpdate() condition handling section
					// s += String.format("'%s'", rs.getString(f));
					s += rs.getString(f);
				}
			}
			rs.beforeFirst();
		}
		return s;
	}

	public String getString(int rowix, String field) throws SQLException {
		if (results == null || results.size() < rowix)
			return null;
		int colix = (Integer) colnames.get(field.toUpperCase())[0];
		// return results == null ? null : (String) results.get(rowix - 1).get(colix - 1);
		return getString(colix);
	}

	public int total() {
		return total < getRowCount() ? getRowCount() : total;
	}

	public AnsonResultset total(int total) {
		this.total = total;
		return this;
	}
}
