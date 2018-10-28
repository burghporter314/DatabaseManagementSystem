/*
 * Dylan Porter (with Zachary Kuchar)
 * Duquesne University
 * Database Management Systems
 */

package com.x10host.burghporter314.DatabaseManagementSystem;

public class Column {

	private String name;
	private int size;
	
	/**
	 * Sets information about the column
	 * @param name of the column
	 * @param size of the column
	 */
	
	public Column(String name, int size) {
		this.name = name;
		this.size = size;
	}
	
	/**
	 * Parses a String of the format "name length" to create a column.
	 * @param input
	 */
	
	public Column(String input) {
		
		//Split the String by spaces
		String[] components = input.split("\\s");
		
		//First element will be the column name
		this.name = components[0];
		
		//Second element will be the length of the column
		this.size = Integer.parseInt(components[1]);
		
	}
	
	/**
	 * Returns the name of the column.
	 * @return
	 */
	
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the size of the column
	 * @return
	 */
	
	public int getSize() {
		return this.size;
	}
	
	/**
	 * Sets the name of the column
	 * @param name
	 */
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the size of the column
	 * @param size
	 */
	
	public void setSize(int size) {
		this.size = size;
	}
	
}
