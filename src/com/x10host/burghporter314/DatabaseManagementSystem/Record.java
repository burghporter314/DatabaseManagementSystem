package com.x10host.burghporter314.DatabaseManagementSystem;

import java.util.HashMap;

public class Record {
	
	private HashMap<String, String> valueMapper = new HashMap<String, String>();	
	
	private Column[] columns;
	private String[] values;
	private int position = 0;
	
	/**
	 * Initialize the Record and utilize Column[] to create a potential hashmap.
	 * @param columns
	 */
	
	public Record(Column[] columns) {
		this.columns = columns;
		this.values = new String[columns.length];
	}
	
	/**
	 * 
	 * @param value
	 */
	
	public void addValue(String value) {
		this.valueMapper.put(this.columns[position].getName(), value);
		values[position++] = value;
	}
	
	/**
	 * Returns the values associated with each column
	 * @return
	 */
	
	public String[] getValues() {
		return this.values;
	}
	
	/**
	 * Sets the values that each column is mapped to.
	 * @param values
	 */
	
	public void setValues(String[] values) {
		this.values = values;
		this.position = values.length - 1;
	}
	
	/**
	 * Get the columns associated with this Record Object
	 * @return
	 */
	
	public Column[] getColumns() {
		return this.columns;
	}
	
	/**
	 * Set the columns associated with this Record Object
	 * @param columns
	 */
	
	public void setColumns(Column[] columns) {
		this.columns = columns;
	}
	
	/**
	 * Return HashMap that maps each column VALUE to a VALUE
	 * @return
	 */
	
	public HashMap<String, String> getValueMapper() {
		return this.valueMapper;
	}
	
	/**
	 * Sets the mapper for the Record Object
	 * @param valueMapper
	 */
	
	public void setValueMapper(HashMap<String, String> valueMapper) {
		this.valueMapper = valueMapper;
	}
	
	/**
	 * Checks to see if all columns are matched to a value
	 * @return
	 */
	
	public boolean isFull() {
		return ((position + 1) == this.values.length);
	}
	
	/**
	 * Compares two records to see if they are equal. 
	 * Must match all column values specified by the user.
	 */
	
	@Override
	public boolean equals(Object o) {
		
		if(o == this) {
			return true;
		}
		
		if(!(o instanceof Record)) {
			return false;
		}
		
		Record record = (Record)(o);
		HashMap<String, String> map = record.getValueMapper();
		
		for(String key : map.keySet()) {
			
			if(map.get(key).isEmpty()) { continue; }
			
			if(!this.valueMapper.get(key).equals(map.get(key))) {
				return false;
			}
		}
		
		return true;
		
	}
	
	
}
