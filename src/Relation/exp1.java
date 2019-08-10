package Relation;

import java.io.FileReader;

import com.opencsv.CSVReader;

public class exp1 {
	
	public static void createNodes(Table t)
	{
		 int noOfColumns = t.noOfColumns();
		 
	}
	
	
	public static void main(String[] args) {
		Table KD = new Table("KD",new String[]{"Integer", "String", "Double"});
		KD.populateTable("src/kd.csv");
		KD.displayTable();
	}
	
	
}
