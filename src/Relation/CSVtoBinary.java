/*Assumptions
1. The sub-relations can be stored in Main Memory
2. The CSV file is sorted
3. The CSV files doesn't have any inverted commas around, even though it is String
4. The CSV file at least has two columns. If it has just one column, then offset builder fails.
5. The offset points before the symbol before the value. example: "ABC _X" the offset of X will point to '_', so that you can mention that (offset-1) number to skip and use offset's place to identify the datatype of leading value.
6. String is prefixed by ',' and Integer is prefixed by '_'
To Do
1. Make MAPs.bin
2. Implement append to BLOCKS.bin
2. If bcol1.colType is integer then Convert the bcol1 value to integer before writing to file because bcol1 is string*/

package Relation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CSVtoBinary extends CSVtoByte{
	
	String tableName; 			//Need to passed as parameterized constructor
	String[] columnDatatypes; 	//Need to passed as parameterized constructor - can be any among the following:"Integer", "Double", "String"
	int noOfColumns; 			//Need to passed as parameterized constructor
	
	public CSVtoBinary(String tableName, String[] columnDatatypes, int noOfColumns){
		this.tableName=tableName;
		this.columnDatatypes = columnDatatypes;
		this.noOfColumns = noOfColumns;
	}
	
	ArrayList<String> columnNames = new ArrayList<String>();//Name of the columns extracted from CSV's first column
	int offsetCounter = -1; 								//Helps in maintaining the previous and new block offsets
	int intInBytes = 4; 									//No of Bytes an integer takes
	int charInBytes = 1;									//No of Bytes a character takes
	int commaInBytes = 1;
	
	//Requires initialize to be called for the initialization
	String bcol1; 	//Stores the value of the current row's column 1's value
	int bcol1Freq; 	//Stores the value of the current row's column 1's value's frequency
	int bcol1Offset;//Stores the value of the current row's column 1's value's offset
	ArrayList bcols;//Stores the unique values of the column 2..N's value sequentially. It's basically flattening the row after row, but only keeping unique values.
	ArrayList<Integer> bcolsFreq;		//Stores the respective frequency of the bcols values
	ArrayList<Integer> bcolsOffset;		//Stores the respective offset of the bcols values
	ArrayList<Integer> bcolsColumnType;	//Stores the respective data-type of the bcols values
	ArrayList prevColValueSeen;
	
	//create MAP.bin and BLOCKS.bin
	public void createBinaryFile(String filePath) throws IOException
	{
		System.out.println("CSVtoBinary");
		File file = new File(filePath); 

		if (file.exists())
			file.delete(); //you might want to check if delete was successfull

		file.createNewFile();
	}
	
	public void writeIntoFile(String filePath, String data)
	{
	}
	
	public void readFromFile(String filePath, String data)
	{
	}
	
	//For given Bcols index, return the data type of that value, in the form of String
	public String getColumnTypeForBcols(int bcolsIndex)
	{
		return columnDatatypes[bcolsColumnType.get(bcolsIndex)];
	}
	
	//For given datatype, it will return how much space will it store in bytes. Note: For String, it will return the storage value of character
	public int getByteSizeBasedOnDataType(String dataType)
	{
		if (dataType.equals("String"))
			return charInBytes;
		return intInBytes;
	}
	
	//For given Bcols index, return the amount of Bytes the info will require
	public int findByteForBcolsBasedOnIndex(int index)
	{
		if(getColumnTypeForBcols(index).equals("String"))
			return charInBytes*bcols.get(index).toString().length();
		return intInBytes;
	}
	
	//Based on the idea that the map is inserted between the bcols value when the column increments (i incrementing)
	public int totalNumberOfMapsInBlock()
	{
		int prevCol = 0; int totalMaps=0;
		for(int i=0;i<bcolsColumnType.size();i++)
		{
			if(bcolsColumnType.get(i)>prevCol)
			{
				totalMaps++;
			}
			prevCol = bcolsColumnType.get(i);
		}
		return totalMaps;		
	}
	
	//Return the total size of the block to help in generating the offset of the end of the block, since we find the offset of an element in bcols going from right to left (decrementing i) in an ArrayList
	public int getTotalSizeOfBlock()
	{
		System.out.println("getTotalSizeOfBlock() function's offsetCounter: "+offsetCounter);
		int offset = 0;
		for(int i=0; i<bcols.size();i++)
		{
			offset += findByteForBcolsBasedOnIndex(i) + intInBytes + commaInBytes + commaInBytes;
			offset += findByteForBcolsBasedOnIndex(i) + intInBytes + commaInBytes + commaInBytes;//Since the map and general v and c will take same amount of space.
//			System.out.println("getTotalSizeOfBlock() findByteForBcolsBasedOnIndex(i): "+findByteForBcolsBasedOnIndex(i));
			if(bcolsColumnType.get(i)==noOfColumns-1)//If the element belongs to last column then add ';' for executing the subrelation() and isMapEmpty()
			{
				offset += commaInBytes + commaInBytes; // for ",;"
			}
		}
//		System.out.println("getTotalSizeOfBlock() function's  offset: "+offset);
		
//		offset *= 2; //Since the map and general v and c will take same amount of space.
		
		if(columnDatatypes[0].equals("String"))
		{
			offset += offsetCounter + charInBytes*bcol1.length() + intInBytes + commaInBytes + commaInBytes;//intInBytes for Integer for Frequency of Column 1
		}
		else
		{
			offset += offsetCounter + intInBytes + intInBytes + commaInBytes + commaInBytes;
		}
		
		offset += (2*(charInBytes+commaInBytes))*totalNumberOfMapsInBlock(); //For delimeters, here " ". 2 because delemiters are present before and after the block
		
		System.out.println("getTotalSizeOfBlock() function's  offset: "+offset);
		return offset;
	}	
	
	//Initializes variables which are supposed to be refreshed after every new block formation
	public void initialise()
	{
		bcol1 = "";
		bcol1Freq = 0;
		bcol1Offset = 0;
		bcols = new ArrayList(); //It won't store first column values
		bcolsFreq = new ArrayList<Integer>();
		bcolsOffset = new ArrayList<Integer>();
		bcolsColumnType = new ArrayList<Integer>(); //It won't mention the type of column for first column

		prevColValueSeen = new ArrayList();
		for(int i=0; i<noOfColumns-1;i++)
		{
			prevColValueSeen.add("");
		}

	}
	
	//Converts the bcols to blocks which has maps inlined along with the frequency and offset present
	public ArrayList createBlocks()
	{
		ArrayList list = new ArrayList();
		int[] noOfValuesToAdd = new int[noOfColumns];
		int prevCol = 0;
		int offset = getTotalSizeOfBlock();offsetCounter = offset;
		
		//initialiseOffset
		for(int i=0;i<bcols.size();i++)
		{
			bcolsOffset.add(0);
		}
		
//		System.out.println("createBlocks() bcols.size(): " +bcols.size());
//		System.out.println("createBlocks() bcolsColumnType.size(): " +bcolsColumnType.size());
		
		//bcols is traversed from last to first because it's easier to find the offset of the last element than the first element, when the total size of the block is known
		//i goes till -1 because we need to add inline map before the first element of bcols.
		for(int i=bcols.size()-1;i>=-1;i--)
		{
//			System.out.println("createBlocks() i: " +i);
			
			ArrayList dummyMap = new ArrayList();
			
			//If this condition is satisfied, then map is created first before attaching a element to the block
			//Two conditions refer to before first element and if the column decreases
			if((i==-1)||bcolsColumnType.get(i)<prevCol)
			{
				int counter=i+1; //Used to traverse the elements ahead of that element. i decreases, while counter decreases
				
				dummyMap.clear();
				dummyMap.add(" ");offset -= (charInBytes); //Add a delimeter and decrease the byte equivalent of it
				dummyMap.add(",");offset -= (commaInBytes);
				
				while((noOfValuesToAdd[prevCol]!=0)||(i==-1))//Traverse the elements ahead of the current i token, to find which elements to put in the list
				{
					if(i==-1)//Change the value i to -2. Later check if i is -2, to break from the loop of i
						i=-2;
					if(bcolsColumnType.get(counter)==prevCol)//Map is being created for prevCol index column
					{
						noOfValuesToAdd[prevCol]--;//Since value has been added, decrement it
						dummyMap.add(bcols.get(counter));dummyMap.add(",");offset -= (commaInBytes);
						dummyMap.add(bcolsOffset.get(counter));dummyMap.add(",");offset -= (commaInBytes);
						
						offset -= (findByteForBcolsBasedOnIndex(counter) + intInBytes);
						counter++;
//						for(int val: noOfValuesToAdd)
//							System.out.print(" val: "+val);
//						System.out.println();
//						System.out.println("createBlocks() bcols.get(counter): "+bcols.get(counter-1));
					}
					else
						counter++;
				}
				dummyMap.add(" ");offset -= (charInBytes);
				dummyMap.add(",");offset -= (commaInBytes);
			}
			dummyMap.addAll(list); list=dummyMap;//This is done so that dummyMap can be added at the beginning of the list
			
			if(i==-2)
				break;
			
			//Operation on ith element
			prevCol = bcolsColumnType.get(i);
			noOfValuesToAdd[prevCol]++;
			
			ArrayList dummy2 = new ArrayList();
			dummy2.add(bcols.get(i));dummy2.add(",");offset -= (commaInBytes);
			dummy2.add(bcolsFreq.get(i));dummy2.add(",");offset -= (commaInBytes);
			if(bcolsColumnType.get(i)==noOfColumns-1)//If the element belongs to last column then add ';' for executing the subrelation() and isMapEmpty()
			{
				dummy2.add(";");offset -= (commaInBytes);
				dummy2.add(",");offset -= (commaInBytes);
			}
			dummy2.addAll(list); list=dummy2;
			
			offset -= (findByteForBcolsBasedOnIndex(i)+intInBytes);
			bcolsOffset.set(i, offset); //offset because we brought the offset right before the value, not on the value. so 200 will be pointing at 100 in [100, 200] since we subtracted the byte representation of 200 completely.
//			System.out.println("createBlocks() (findByteForBcolsBasedOnIndex(i)+intInBytes): "+(findByteForBcolsBasedOnIndex(i)+intInBytes));
//			System.out.println("createBlocks() List: "+list);
			
		}
		displayList(list);
//		System.out.println("createBlocks() List: "+list);
		return list;
	}
	
	public void displayList(ArrayList list)
	{
		System.out.print("createBlocks()  List:[");
		for(int i=0 ;i<list.size(); i++)
		{
			System.out.print(list.get(i)+"");
		}
		System.out.print("]");
		System.out.println();
	}
	
	//create MAP.bin and BLOCKS.bin
	//To create an offset of an element, you need to calculate previous 
	public void offsetBuilder()
	{
		createBlocks();
	}
	
	public int stringBinaryToInteger(String str)
	{
		int result = 0;
		int power = 0;
		for(int i=str.length()-1; i>=0;i--)//Decrementing since we need to go from right to left
		{
//			System.out.println("str.charAt(i):"+str.charAt(i));
			result += Integer.parseInt(""+str.charAt(i))*Math.pow(2, power++);
		}
		System.out.println("Result:"+result);
		return result;
	}
	
	public String generateStringBinaryForMapColumns()//<padding><1stCol><2ndCol>...<nthCol> //Integer = 1, String = 0
	{
		String str = "";
		for(int i=0; i<noOfColumns;i++)
		{
			if(columnDatatypes[i]=="Integer")
				str += "1";
			else
				str += "0";
		}
		return str;
	}
	
	/*map.bin
	<NoOfColumns 'C' integer><4 byte integer which stores the datatype of the columns. Since, we have the number of columns present, we can just access the rightmost 'C' numbers><Value based on column one's datatype>_<Offset>*/
	public void generateMap() throws IOException
	{
		String filePath = "src/map.bin";
		File file = new File(filePath); 
		
		createBinaryFile(filePath);
		
		if (file.exists())
		{

			try(OutputStream outputStream = new FileOutputStream(filePath);
				DataOutputStream dos = new DataOutputStream(outputStream); //DOS because Double can only be loaded into file using this library
					) 
			{
				outputStream.write(intToBytes((int) noOfColumns));
				outputStream.write(intToBytes((int) stringBinaryToInteger(generateStringBinaryForMapColumns())));
				outputStream.write(tableName.getBytes());
				outputStream.write(",".getBytes());//signifies the end of the table name
				for(int i=0; i<columnNames.size();i++)
				{
					outputStream.write(columnNames.get(i).getBytes());
					outputStream.write(",".getBytes());//signifies the end of the column name
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void addDataToMap() throws IOException
	{
		String filePath = "src/map.bin";
		File file = new File(filePath); 
		
		if (file.exists())
		{

			try(OutputStream outputStream = new FileOutputStream(filePath, true);
				DataOutputStream dos = new DataOutputStream(outputStream); //DOS because Double can only be loaded into file using this library
					) 
			{
				
				if(columnDatatypes[0].equals("String"))
				{
					outputStream.write(",".getBytes());
					outputStream.write(bcol1.getBytes());
				}
				else
				{
					outputStream.write("_".getBytes());
					outputStream.write(intToBytes((int)Integer.parseInt(bcol1)));
				}
				outputStream.write("_".getBytes());
				outputStream.write(intToBytes((int)bcol1Offset));
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void endMapFileWithMapAndBlockSeperator()
	{
		
	}
	
	public String[] removeQuotesFromAtrributes(String[] attributes)
	{
		for(int i=0; i<attributes.length;i++)
		{
			System.out.println(attributes[i]);
			if(attributes[i].charAt(0)=='"')
			{
				attributes[i] = attributes[i].substring(1, attributes[i].length()-1);
			}
		}
		return attributes;
	}
	
	//Main Algorithm and uses the helper functions above to convert CSV to Block Representations to Binary File
	public ArrayList performCSVtoBinary(String fileName) throws IOException
	{
//		createBinaryFile("src/MAP.bin");
//		createBinaryFile("src/BLOCKS.bin");
		ArrayList blocks = new ArrayList();//Holds the final block with the bcols1 + bcols1Freq + createBlock()
		blocks.add(",");
		
		Path pathToFile = Paths.get(fileName);
		boolean firstLine = true;
		initialise();
		
        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) { // create an instance of BufferedReader using try with resource, Java 7 feature to close resources

            String line = br.readLine(); // read the first line from the text file

            while (line != null) { // loop until all lines are read
            	
            	String[] attributes = line.split(","); // use string.split to load a string array with the values from each line of the file, using a comma as the delimiter
            	
            	attributes = removeQuotesFromAtrributes(attributes);
            	
//            	for(String attr:attributes)
//            	{
//            		System.out.println("performCSVtoBinary(String fileName) attr:"+attr);
//            	}
            	if(firstLine) //Checks if it's the first line of CSV to store the column names
            	{
            		for(String colName:attributes)
            		{
            			columnNames.add(colName);
            		}
            		firstLine = false;
            		generateMap();
            	}
            	else //Row 2..N
            	{            		
            		for(int i=0; i<noOfColumns; i++)//Iterate over each column for that row
            		{
            			if(i==0)//If it's first column, then only bcol1 and bcol1Freq are modified. Separate from bcols since the maps of the first column will be stored in MAPS.bin, while for other columns the maps will be inlined
            			{
            				if (!bcol1.equals(attributes[i]))//If new value of first column is found
            				{
//            					System.out.println("performCSVtoBinary() bcol1:" +bcol1);
            					if(offsetCounter == -1)//If it's 1 column and 1 row, so it's start of the first block
            						offsetCounter = 1; //Since we added a comma before the first element
            					
            					else
            					{
            						bcol1Offset = offsetCounter;System.out.println("bcol1Offset:"+bcol1Offset);System.out.println("bcol1:"+bcol1);
            						addDataToMap();//Update MAP.bin
//            						for(int ii=0; ii<bcols.size();ii++)
//            						{
//            							System.out.println(bcols.get(ii)+":"+bcols.get(ii).getClass().getName());
//            						}
            						if(columnDatatypes[0].equals("Integer"))
            			            {
            			            	blocks.add(Integer.parseInt(bcol1));blocks.add(",");
            			            }
            						else
            						{
            							blocks.add(bcol1);blocks.add(",");
            						}
            						blocks.add(bcol1Freq);blocks.add(",");
            						blocks.addAll(createBlocks());
//            						blocks.add(";");offsetCounter += (charInBytes);//The space between each block
//            						blocks.add(",");offsetCounter += (commaInBytes);
            						//Update BLOCKS.bin
//            						System.out.println("performCSVtoBinary() -->" + bcol1 + " " + bcol1Freq + " " + bcols + " " + bcolsFreq);
            						initialise();
            					}
            					
            					bcol1 = attributes[i];
            				}
            				++bcol1Freq;
//            				System.out.println("performCSVtoBinary() bcol1:" +bcol1);
//            				System.out.println("performCSVtoBinary() bcol1Freq:" +bcol1Freq);
            			}
            			else//If it's 2...N column
            			{
            				boolean valueAlreadyInBlock = false;
            				System.out.println("performCSVtoBinary() Size of bcols" +bcols.size());
            				for(int j=0; j<bcols.size();j++)//Check each value in partially formed block for the same value with same previous columns attributes
            				{
            					if(bcolsColumnType.get(j)!=i)//Checks if it belongs to the same column. Same value can be present in two different columns so this check is required
            					{
            						prevColValueSeen.set(bcolsColumnType.get(j)-1, bcols.get(j));//prevColValueSeen helps to identify if this new value is 'same' as previously encountered value. 'Same' condition is only satisfied when the previous columns of the row are same. a1,b not same same as a2,b.
//            						System.out.println("performCSVtoBinary() checker prevColValueSeen:" +prevColValueSeen+" attribute:"+attributes[i]+" bcolsColumnType.get(j):"+bcolsColumnType.get(j));
            					}
            					else
            					{
//            						System.out.println("performCSVtoBinary() checker2 bcols.get(j).toString():" +bcols.get(j).toString()+" attribute:"+attributes[i]);
            						if(bcols.get(j).toString().equals(attributes[i]))//Check if same value
            						{
            							valueAlreadyInBlock = true;
            							for(int k=1; k<=i-1;k++)
            							{
            								if(!attributes[k].equals(prevColValueSeen.get(k-1).toString()))
            								{
            									valueAlreadyInBlock=false;
            									break;
            								}
            							}
            						}
            					}
            					if(valueAlreadyInBlock)
            					{
            						bcolsFreq.set(j, bcolsFreq.get(j)+1); //Increment the value at bcolsFreq[j] by 1
            						break;
            					}	
            				}
            				if(!valueAlreadyInBlock)
            				{
            					if(columnDatatypes[i].equals("Integer"))
            						bcols.add(Integer.parseInt(attributes[i]));
            					else
            						bcols.add(attributes[i]);
            					bcolsFreq.add(1);
            					bcolsColumnType.add(i);
            				}
//            				System.out.println(bcol1 + " " + bcol1Freq + " " + bcols + " " + bcolsFreq);
            			}
            		}
            		
            	}
                // read next line before looping if end of file reached, line would be null
                line = br.readLine();
            }
            System.out.println("performCSVtoBinary() bcol1:" +bcol1);
            bcol1Offset = offsetCounter;System.out.println("bcol1Offset:"+bcol1Offset);System.out.println("bcol1:"+bcol1);
            addDataToMap();//Update MAP.bin
            endMapFileWithMapAndBlockSeperator();//This is done so that consistency remains between the maps.bin and inline maps' ending, which comma and space (, )
            if(columnDatatypes[0].equals("Integer"))
            {
            	blocks.add(Integer.parseInt(bcol1));blocks.add(",");
            }
			else
			{
				blocks.add(bcol1);blocks.add(",");
			}
			blocks.add(bcol1Freq);blocks.add(",");
			blocks.addAll(createBlocks());
//			blocks.add(";");offsetCounter += (charInBytes);//The space between each block
			//Update BLOCKS.bin
			System.out.println(bcol1 + " " + bcol1Freq + " " + bcols + " " + bcolsFreq);
			return blocks;

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
            	
        return null;
            	
            	
	}
	
	public static void main(String[] args) throws IOException {
		
		CSVtoBinary KD = new CSVtoBinary("KD", new String[]{"Integer", "String", "Integer"}, 3);
		ArrayList blocks = KD.performCSVtoBinary("src/KD.csv");
		
//		CSVtoBinary D = new CSVtoBinary("D", new String[]{"Integer", "Integer", "Integer", "String"}, 4);
//		ArrayList blocks = D.performCSVtoBinary("src/d.csv");
		
//		KD.createBinaryFile("src/dummy.bin");
		
	}

}


//public void createInlineMapDeleteLater()
//{
//	ArrayList<ArrayList<Pair>> maps = new ArrayList<ArrayList<Pair>>();
//	
//	//create list of Pairs for each columns
//	for(int i=0; i<noOfColumns;i++)
//	{
//		maps.add(new ArrayList<Pair>());
//	}
//	
//	//iterate through each value in bcols and insert the value based on the column
//	for(int i=0; i<bcols.size();i++)
//	{
//		maps.get(bcolsColumnType.get(i)-1).add(new Pair(bcols.get(i),0));//Gets the arraylist for that column. Add the pair with dummyInt=0
//	}
//	for(int i=0; i<noOfColumns;i++)
//	{
//		for(int j=0; j<maps.get(i).size(); j++)
//			System.out.println(maps.get(i).get(j));
//		System.out.println();
//	}
//	
//}
