package Relation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Table {
	
	/*Attributes of Table: columnNames and data*/
	String tableName;
	List columnNames;
	List<List> data; 
	String[] columnDatatypes;
	
	@SuppressWarnings("rawtypes")
	public Table(String tableName, String[] dataTypes){
		this.tableName=tableName;
		columnNames = new ArrayList<String>();
		data = new ArrayList<List>();
		columnDatatypes = dataTypes;
	}
	
	public int noOfColumns()
	{
		return columnDatatypes.length;
	}
	
	@SuppressWarnings("rawtypes")
	public void displayTable() 	//Traverse elements of Table
	{
		Iterator i = data.iterator();
	    System.out.println("The ArrayList elements for "+tableName+" are:");
	    System.out.println(columnNames);
	    while (i.hasNext()) {
	       System.out.println(i.next());
	    }
	    System.out.println("");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List lookup(int id, List<List> list) { //Returns a row based on the given id(primary key) of the table
		for(List x : list) {
		    if (x.contains(id)) {
		    	List dummy = new ArrayList<Object>(x);
		    	dummy.remove(dummy.indexOf(id)); //Removes the column of id since this returned list will be merged with table1's row. If not removed, then duplication of column occurs.
//		    	System.out.println(dummy);
		    	return dummy;
		    }
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void populateTable(String fileName) { //Inserts data into Table t from the CSV filename by passing the table's column's datatype in sequence
		
		Path pathToFile = Paths.get(fileName);
		boolean firstLine = true;
		
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) { // create an instance of BufferedReader using try with resource, Java 7 feature to close resources

            String line = br.readLine(); // read the first line from the text file

            while (line != null) { // loop until all lines are read
            	
            	String[] attributes = line.split(","); // use string.split to load a string array with the values from each line of the file, using a comma as the delimiter
            	
            	if(firstLine)
            	{
            		for(String colName:attributes)
            		{
            			columnNames.add(colName);
            		}
            		firstLine = false;
            	}
            	else
            	{
            		ArrayList dummyList = new ArrayList();
            		int colNameCounter = 0;
            		for(String dataType:columnDatatypes)
            		{
            			if(dataType=="Integer")
            			{
            				dummyList.add(Integer.parseInt(attributes[colNameCounter++]));
            			}
            			else if(dataType=="Double")
            			{
            				dummyList.add(Double.parseDouble(attributes[colNameCounter++]));
            			}
            			else
            			{
            				System.out.println(attributes[colNameCounter]);
            				dummyList.add(attributes[colNameCounter++]);
            			}
            		}
            		data.add(dummyList);
            	}

                // read next line before looping if end of file reached, line would be null
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
	}

	public static void main(String[] args) {
		
		Table KD = new Table("KD",new String[]{"Integer", "String", "Double"});
		KD.populateTable("src/kd.csv");
		KD.displayTable();
	}

}

