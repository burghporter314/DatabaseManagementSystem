/*
 * Dylan Porter (with Zachary Kuchar)
 * Duquesne University
 * Database Management Systems
 * Credit to https://docs.oracle.com/javase/7/docs/api/java/io/RandomAccessFile.html
 */

package com.x10host.burghporter314.DatabaseManagementSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class DatabaseFile {
	
	private String name;
	private ArrayList<Column> columns;
	
	private int recordSize;
	private long firstRecordPosition;
	private final String EXTENSION = ".dbf";
	private final int MIN_SPACES = 30;
	private RandomAccessFile fileParser;
	private Scanner input;
	
	/**
	 * This Constructor assumes that the database file already exists
	 */
	
	public DatabaseFile(String fileName) throws IOException {
		
		this();
		this.name = fileName;
		if(!((new File(fileName + EXTENSION)).exists())) {
			throw new java.lang.Error("Could not Find File"); // The File has not been created, so we can't open it.
		}
		
		this.fileParser = new RandomAccessFile(fileName + EXTENSION, "rw");
		
		this.columns = getColumns();
		this.firstRecordPosition = this.fileParser.getFilePointer();
	}
	
	/**
	 * Assumes the File Does not Exist
	 */
	
	public DatabaseFile() {
		recordSize = 0;
		columns = new ArrayList<Column>();
		this.input = new Scanner(System.in);
	}
	
	/**
	 * Creates a new table by writing to a .dbf file the new columns specified by user.
	 * @throws IOException
	 */
	
	public void createTable() throws IOException {
		
		(new File(this.name + EXTENSION)).createNewFile();
		this.fileParser = new RandomAccessFile(this.name + EXTENSION, "rw");
		
		this.fileParser.writeInt(this.columns.size());
		this.fileParser.writeInt(this.recordSize);
		
		for(Column column : this.columns) {
			this.fileParser.writeUTF(column.getName()); 
			this.fileParser.writeInt(column.getSize());
		}

		this.fileParser.seek(0);
		this.columns = getColumns();
		this.firstRecordPosition = this.fileParser.getFilePointer();
		//this.fileParser.close();
		
	}
	
	/**
	 * Inserts a new Record into the database file (writes to the file in binary format where char = 2 bytes)
	 * @param record is used to map column to value
	 * @throws IOException
	 */
	
	public void insert(Record record) throws IOException {
		
		HashMap<String, String> map = record.getValueMapper();
		long pos = (new File(this.name + this.EXTENSION)).length();
		
		this.fileParser.seek(pos);
		
		for(Column column: this.columns) {
			addEntry(column.getSize(), map.get(column.getName()));
		}
		
	}
	
	/**
	 * Removes a record from the file by tombstoning it (inserts a # at the first position of the record)
	 * For an entry to be removed, all values specified by the user should be matched (excluding blanks)
	 * @param record 
	 * @throws IOException
	 */
	
	public void remove(Record record) throws IOException {
		for(long i = this.firstRecordPosition; i < this.fileParser.length(); i += this.recordSize * 2) {
			removeIfEquals(i, record);
		}
	}
	
	/**
	 * Lists the contents of the file by first printing the columns and then the corresponding data.
	 * @throws IOException
	 */
	
	public void listFile() throws IOException {
		
		this.fileParser.seek(0);
		int numColumns = this.fileParser.readInt();
		int max = findMaxColumn(MIN_SPACES, this.columns);
		
		for(Column column: this.columns) {
			System.out.print(String.format("%1$-" + (max) + "s", column.getName()));;
		}
		System.out.println("");
		
		for(long i = this.firstRecordPosition; i < this.fileParser.length(); i += this.recordSize * 2) {
			
			this.fileParser.seek(i);
			if(this.fileParser.readChar() == '#') { continue; }
			
			Record record = getRecord(i);
			int counter = 0;
			
			for(String s : record.getValues()) {
				System.out.print(String.format("%1$-" + (max) + "s", s));
			}
			
			System.out.println("");
		}

	}
	
	/**
	 * Writes all non-deleted records to a new 'temp' file and then renames the file back to original file name.
	 * @throws IOException
	 */
	
	public void purge() throws IOException {
		
		String originalName = this.getName();
		this.setName(this.getName() + "temp");
		
		this.fileParser.close();
		createTable();
		
		RandomAccessFile tempReader = new RandomAccessFile(originalName + EXTENSION, "rw");
		tempReader.seek(this.firstRecordPosition);

		while(tempReader.getFilePointer() < tempReader.length()) {
			char s = tempReader.readChar();
			if(s == '#') {
				tempReader.skipBytes((this.recordSize * 2) - 2);
				continue;
			} 

			this.fileParser.writeChar(s);
		}
		
		tempReader.close();
		this.fileParser.close();
		
		(new File(originalName + EXTENSION)).delete();
		(new File(this.getName() + EXTENSION)).renameTo(new File(originalName + EXTENSION));
		
	}
	
	/**
	 * Appends a new entry to the database file. 
	 * @param columnSize Used to determine how many blanks to prepend on each element in the entry.
	 * @param value is the value associated with the column value in the row.
	 * @throws IOException
	 */
	
	private void addEntry(int columnSize, String value) throws IOException {
		
		int paddedZeros = columnSize - value.length();
		int itemsRemaining = columnSize;
		
		while(paddedZeros > 0) {
			this.fileParser.writeChar(' ');
			paddedZeros--;
			itemsRemaining--;
		}
		
		for(int i = 0; itemsRemaining > 0; i++) {
			this.fileParser.writeChar(value.charAt(i));
			itemsRemaining--;
		}
		
	}
	
	/**
	 * If there exists a column with size > 30, return the max column size.
	 * @param value lowest value allowed by user for spacing
	 * @param columns specifies the set of columns to find max size for
	 * @return maximum value of the column size if > 30
	 */
	
	private int findMaxColumn(int value, ArrayList<Column> columns) {
		
		for(Column column: columns) {
			if(column.getSize() > value) {
				value = column.getSize();
			}
		}
		
		return value;
	}
	
	/**
	 * Adds a knew column to this object's column arraylist. 
	 * @param column
	 */
	public void addColumn(Column column) {
		this.columns.add(column);
		recordSize += column.getSize();
	}
	
	/**
	 * Reads the database file and extracts the columns from the metadata information
	 * @return ArrayList<Column> representing this file's columns.
	 * @throws IOException
	 */
	
	private ArrayList<Column> getColumns() throws IOException {
		
		ArrayList<Column> columns = new ArrayList<Column>();
		
		int numColumns = this.fileParser.readInt();
		this.recordSize = this.fileParser.readInt();

		for(int i = 0; i < numColumns; i++) {
			columns.add(new Column(this.fileParser.readUTF(), 
					this.fileParser.readInt()));
		}
		
		return columns;
		
	}
	
	/**
	 * Gets the record starting at position pos from the file. 
	 * @param pos starting index of file
	 * @return new record representing data read from file.
	 * @throws IOException
	 */
	
	private Record getRecord(long pos) throws IOException {
		
		this.fileParser.seek(pos);
		Record record = new Record(this.getColumnArray());
		
		for(Column column: this.columns) {
			
			StringBuilder s = new StringBuilder("");
			for(int i = 0; i < column.getSize(); i++) {
				s.append(this.fileParser.readChar());
			}
			
			record.addValue(s.toString().trim());
		}
		
		return record;
		
	}
	
	/**
	 * Tombstone the record by inserting a # at the beginning of the record.
	 * @param index starting index of the record
	 * @param record object to compare with current record.
	 * @throws IOException
	 */
	
	private void removeIfEquals(long index, Record record) throws IOException {
		Record currentRecord = getRecord(index);
		if(currentRecord.equals(record)) {
			this.fileParser.seek(index);
			this.fileParser.writeChar('#'); //Tombstone
		}
	}
	
	/**
	 * Returns the name of the file
	 * @return
	 */
	
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of the file
	 * @param name
	 */
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the columns associated with this object in an array format.
	 * @return
	 */
	
	public Column[] getColumnArray() {
		
		Column[] temp = new Column[this.columns.size()];
		
		for(int i = 0; i < this.columns.size(); i++) {
			temp[i] = this.columns.get(i);
		}
		
		return temp;
	}
	
}
