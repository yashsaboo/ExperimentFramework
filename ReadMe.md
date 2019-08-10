# Experimental Framework
An experimental framework desgined for Everything Search to test the hypothesis stated for design and performance of an information retieval system.

## File Details
Codebase includes a Java Project with multiple files in ./src folder. This project was built using Eclipse Oxygen with no external libraries requirement. It can be used for following puposes:
1. CSVtoBinary.java: Converts the CSV file to set of blocks
For instance,
```
ArrayList blocks = KD.performCSVtoBinary("src/KD.csv");
```
This creates blocks of rawtype ArrayList for relation KD by giving the file location of the same's csv file.

2. CSVtoByte.java: Stores the blocks created from above function to binary files. The file path for map is fixed which is "src/maps.bin". (will be updated soon)
For instance,
```
CSVtoByte kd = new CSVtoByte();
String filePath = "src/blocks.bin";
kd.createBinaryFile(filePath);
kd.writeIntoFile(blocks, filePath);
kd.readFromFile(blocks, filePath);
```
This stores kd.csv blocks to maps.bin and blocks.bin which is a representation technique used for Experimental Framework of Everything Search.

3. relation.java: The brain of the experimental framework foundation. This has following functionalities:
- T:cols is the list of columns of T --> replaced with int noOfColumns; String[] columnDataType; String relationName; String[] columnNames;
- T:M(val) givens the location where tuples for T:C = val are stored (in map, not in blocks) --> replaced with locationOfTuples(Object val)
- T:reset() moves the cursor of T to its starting location --> replaced with resetPointer()
- T:curr() returns value and count of first column C of T where the cursor is currently present --> replaced with currentPointerCountAndValue()
- T:next() moves the cursor to the next distinct value of first T:C
- T:jump(val) jumps to a location where T:curr() >= val. --> yet to be implemented. it's easy, but might create little complications so doing it in the end.
- T:empty() returns true if the relation has no more tuples --> replaced with isMapEmpty()
- T:numVals() returns number of distinct values of the first column of the relation T --> replaced with distinctValuesOfFirstColumn()
- T:C is the name of first column of T --> replaced with T.columnNames[0]

4. StringSimilarity.java, Pair.java and FileExperiment.java: A dummy files used for testing few aspects of helper functions.

5. Table.java and exp1.java: Ignore those files (will upate the .gitignore file soon)

The following dataFiles are also included:
1. Binary Files:
	- blocks.bin: Stores the block generated from 1st column to N column and maps generated from 2nd column to N column (N is the no of columns in the relation)
	- maps.bin: Stores the map generated for 1st column
2. CSV files: Stores the content of each relational table.
	- D.csv
	- K.csv
	- KD.csv
					
## Requirements to run the code
JRE Environment

### Contributor: [Yash Saboo](https://github.com/yashsaboo) as a Member of [FORWARD Data Lab](http://www.forwarddatalab.org/)