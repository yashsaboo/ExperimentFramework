package Relation;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class relation {
	
	int noOfColumns;
	String relationName;
	ArrayList<String> columnDataType;
	ArrayList<String> columnNames;
	String mapFilePath;
	String blocksFilePath;
	int pointer;
	boolean inLine;
	int mapStartsAt;
	
	//Constructor
	public relation()
	{
		noOfColumns = 0;
		relationName = null;
		columnDataType = null;
		columnNames = null;
		pointer = 1;
		inLine = false;
		mapFilePath = null;
		blocksFilePath = null;
		mapStartsAt = 0;
	}
	
	//Method to Invoke when subrelation causes the map shift from a separate file to inline
	public void changesForMapInline(int mapStartsAt)
	{
		this.noOfColumns--;
		if(this.relationName.contains("subrelation"))
		{
			this.relationName = this.relationName.substring(0, this.relationName.length()-1) + (Integer.parseInt(""+this.relationName.charAt(this.relationName.length()-1))+1);
		}
		else
			this.relationName = relationName + " subrelation 1";
		this.columnDataType.remove(0);//Removes the first column
		this.columnNames.remove(0);//Removes the first column
		this.mapFilePath = this.blocksFilePath;
		this.pointer = mapStartsAt;
		this.inLine = true;
		this.mapStartsAt = mapStartsAt;
	}
	
	//Method to convert the second token of Map Header to Binary equivalent to find the column's data type
	public ArrayList<String> extractColumnDataTypesFromMap(int noOfColumns, String columnDataTypeInBinaryNumber)
	{
		ArrayList<String> columnDataType = new ArrayList<String>();
		for(int i=0; i<noOfColumns; i++)
		{
			if((columnDataTypeInBinaryNumber.length()>i)&&(columnDataTypeInBinaryNumber.charAt(i)=='1'))
				columnDataType.add("Integer");
			else
				columnDataType.add("String");
		}
		
		return columnDataType;
	}
	
	//Method to initialise file paths
	public void initialiseRelationFilePath(String mapFilePath, String blocksFilePath)
	{
		this.mapFilePath = mapFilePath;
		this.blocksFilePath = blocksFilePath;
	}
	
	public String findStringFromGivenOffsetAndDelimeter(int offset, String delimeter, DataInputStream dos, boolean incrementPointer) throws IOException
	{
		if(offset!=-1)//Ignore offset
		{
			dos.skipBytes(offset-1);
			if (incrementPointer)
				pointer = offset;
		}
		String value = "";

		while(true)
		{
			byte[] str = " ".getBytes();
			str[0] = dos.readByte(); if (incrementPointer) pointer++;
			String string = new String(str, "ASCII");
//			System.out.println("findStringFromGivenOffsetAndDelimeter() String:"+string);
			if(string.equals(delimeter))
				break;
			else
				value+=string;
		}

		return value;
	}
	
	public int findIntegerFromGivenOffsetAndDelimeter(int offset, DataInputStream dos, boolean incrementPointer) throws IOException
	{
		if(offset!=-1)//Ignore offset
		{
			dos.skipBytes(offset-1);
			if (incrementPointer)
				pointer = offset;
		}
		int value = dos.readInt(); if (incrementPointer) pointer=pointer+4;
//		System.out.println("findIntegerFromGivenOffsetAndDelimeter() value:"+value);
		return value;
	}
	
	//Method to extract information from Map's header
	public void processMapHeader()//Only if not inLine
	{
		File file = new File(mapFilePath); 

		if ((file.exists()) && (!inLine))
		{
			
			try (InputStream inputStream = new FileInputStream(mapFilePath);
					DataInputStream dos = new DataInputStream(inputStream); //DOS because Double can only be loaded into file using this library
					) {
				
				noOfColumns = dos.readInt(); pointer = pointer+4;
				
				//Find Data type of Columns
				int columnDataTypeInBinaryString = dos.readInt(); pointer = pointer+4;
				String columnDataTypeInBinaryNumber = Integer.toBinaryString(columnDataTypeInBinaryString);
				columnDataType = extractColumnDataTypesFromMap(noOfColumns, columnDataTypeInBinaryNumber);
				
				//Find Table Name
				relationName = findStringFromGivenOffsetAndDelimeter(-1, ",", dos, true);
//				System.out.println("processMapHeader() pointer:"+pointer);
				//Find Column Names
				columnNames = new ArrayList<String>();
				for(int i=0; i<noOfColumns; i++)
				{
					columnNames.add(findStringFromGivenOffsetAndDelimeter(-1, ",", dos, true));
//					System.out.println("columnNames:"+columnNames.get(i));
				}
				
				this.mapStartsAt = pointer;
//				System.out.println("pointer:"+pointer);

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static boolean goNextOrNotForString(String counterString, String val)//Next = true; Don't = false;
	{
		String longer = counterString, shorter = val;
		if (counterString.length() < val.length()) { // longer should always have greater length
			longer = val; shorter = counterString;
		}
		longer = longer.toLowerCase();
		shorter = shorter.toLowerCase();
		
		//If Exactly equal
		if(counterString.equals(val))
			return false;
		
		else if(longer.substring(0, shorter.length()).equals(shorter.substring(0, shorter.length())))
		{
			if(longer.equals(counterString))
				return false;
			else
				return true;
		}
		else
		{
			for(int i=0; i<shorter.length(); i++)
			{
				if(val.charAt(i)>counterString.charAt(i))
					return true;
				else if(val.charAt(i)<counterString.charAt(i))
					return false;
			}
		}
		return false;
	}
	
	public static boolean goNextOrNotForInteger(int counterInt, int val)//Next = true; Don't = false;
	{
		if(val>counterInt)
			return true;
		else
			return false;
	}
	
	public ArrayList distinctValuesOfFirstColumn()
	{
		File file = new File(mapFilePath);
		ArrayList returnList = new ArrayList();
		if (file.exists())
		{

			try (InputStream inputStream = new FileInputStream(mapFilePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
//				if(!inLine)
				{
					dos.skipBytes(mapStartsAt-1);
//					System.out.println("distinctValuesOfFirstColumn() mapStartsAt:"+mapStartsAt);
					dos.mark(500);//random integer inserted for now
					while(dos.available()>0)
					{
						byte[] str = " ".getBytes();
						str[0] = dos.readByte();
						String string = new String(str, "ASCII");
//						System.out.println("distinctValuesOfFirstColumn() String:"+string);
						if(string.equals("_"))
						{
							returnList.add(findIntegerFromGivenOffsetAndDelimeter(-1, dos, false));
//							System.out.println("distinctValuesOfFirstColumn() value:"+returnList.get(returnList.size()-1));
							dos.skipBytes(5);
						}
						else if(string.equals(","))
						{
							String value = findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false);
							if(value.startsWith(" "))
								break;
							else
								returnList.add(value);
//							System.out.println("distinctValuesOfFirstColumn() value:"+returnList.get(returnList.size()-1));
							dos.skipBytes(4);
						}
						else
							break;
					}
				}

			}catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return returnList;
	}
	
	//Returns -1 is the value is not present
	public int locationOfTuples(Object val)
	{
		File file = new File(mapFilePath);
		int returnPosition = 0;
		if (file.exists())
		{

			try (InputStream inputStream = new FileInputStream(mapFilePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
//				if(!inLine)
				{
					dos.skipBytes(mapStartsAt-1); returnPosition = mapStartsAt;
//					System.out.println("locationOfTuples(Object val) mapStartsAt: "+mapStartsAt);
					dos.mark(500);//random integer inserted for now
					while(dos.available()>0)
					{
						byte[] str = " ".getBytes();
						str[0] = dos.readByte();returnPosition++;
						String string = new String(str, "ASCII");
//						System.out.println("locationOfTuples(Object val) String:"+string);
						if(string.equals("_"))
						{
							//						System.out.println("value:"+findIntegerFromGivenOffsetAndDelimeter(-1, dos, false));
							if(findIntegerFromGivenOffsetAndDelimeter(-1, dos, false)==(int)val)
								return returnPosition-1;
							else
								returnPosition += 4;
							dos.skipBytes(5); returnPosition += 5;
						}
						else if(string.equals(","))
						{
							int prevDOSAvailables = dos.available();
//							System.out.println("value:"+findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false));
							String value = findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false);
							if(value.startsWith(" "))
								break;
							else
							{
								if(value.equals((String)val))
									return returnPosition-1;
								else
									returnPosition += (prevDOSAvailables-dos.available());
								dos.skipBytes(4); returnPosition += 4;
							}
						}
						else
							break;
					}
				}

			}catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return -1;
	}
	
	public void jump(Object val)
	{
		File file = new File(mapFilePath);
		if (file.exists())
		{

			try (InputStream inputStream = new FileInputStream(mapFilePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
//				if(!inLine)
				{
					dos.skipBytes(pointer-1);
//					System.out.println("locationOfTuples(Object val) mapStartsAt: "+mapStartsAt);
					dos.mark(500);//random integer inserted for now
					while(dos.available()>0)
					{
						byte[] str = " ".getBytes();
						str[0] = dos.readByte();pointer++;
						String string = new String(str, "ASCII");
//						System.out.println("locationOfTuples(Object val) String:"+string);
						if(string.equals("_"))
						{
							//						System.out.println("value:"+findIntegerFromGivenOffsetAndDelimeter(-1, dos, false));
							int counterValue = findIntegerFromGivenOffsetAndDelimeter(-1, dos, false);
							if(goNextOrNotForInteger(counterValue, (int)val))
								pointer += 4;
							else
							{
								pointer--;break;
							}
							dos.skipBytes(5); pointer += 5;
						}
						else if(string.equals(","))
						{
							int prevDOSAvailables = dos.available();
//							System.out.println("value:"+findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false));
							String counterValue = findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false);
							if(counterValue.startsWith(" "))
								break;
							else
							{
								if(goNextOrNotForString(counterValue, (String)val))
									pointer += (prevDOSAvailables-dos.available());
								else
								{
									pointer--;break;
								}
									
								dos.skipBytes(4); pointer += 4;
							}
						}
						else
							break;
					}
				}

			}catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	
	public void resetPointer()
	{
		pointer = mapStartsAt;
	}
	
	public boolean isMapEmpty()
	{
		File file = new File(mapFilePath);
//		System.out.println("isMapEmpty() mapFilePath:"+mapFilePath);
		if (file.exists())
		{
			try (InputStream inputStream = new FileInputStream(mapFilePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
				dos.skipBytes(pointer-1);
//				System.out.println("mapNotEmpty() pointer:"+pointer);
				if(!inLine)
				{					
//					System.out.println("mapNotEmpty() dos.available():"+dos.available());
					if(dos.available()>0)
						return false;
					else
						return true;
				}
				else
				{
					byte[] str = " ".getBytes();
					str[0] = dos.readByte();
					String string = new String(str, "ASCII");
//					System.out.println("isMapEmpty() string1:"+string);
					if(string.equals(","))
					{
						str[0] = dos.readByte();
						string = new String(str, "ASCII");
//						System.out.println("isMapEmpty() string2:"+string);
						if(string.equals(" "))
							return true;
						else
							return false;
					}
					else if(string.equals("_"))
					{
						return false;
					}
					else
					{
						return true;
					}
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return true;			
	}
	
	public ArrayList findFrequencyAndValueForGivenOffset(int offset)
	{
		File file = new File(blocksFilePath);
		ArrayList returnList = new ArrayList();
		
		if (file.exists())
		{
			try (InputStream inputStream = new FileInputStream(blocksFilePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
				dos.skipBytes(offset-1);
				byte[] str = " ".getBytes();
				str[0] = dos.readByte();
				String string = new String(str, "ASCII");
//				System.out.println("findFrequencyAndValueForGivenOffset(int offset) String:"+string);
				if(string.equals("_"))
				{
					returnList.add(findIntegerFromGivenOffsetAndDelimeter(-1, dos, false));
					//Go to the blockfilepath and find the count. Nested DOS.
					str[0] = dos.readByte();
					string = new String(str, "ASCII");
					if(string.equals("_"))
					{
						returnList.add(dos.readInt());
					}
				}
				else if(string.equals(","))
				{
					returnList.add(findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false));
					returnList.add(dos.readInt());
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return returnList;				
	}
	
	public int findCurrentOffset(int pointer, String filePath)
	{
		
		File file = new File(filePath);
		
		if (file.exists())
		{
			try (InputStream inputStream = new FileInputStream(filePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
//				System.out.println("findCurrentOffset() In file.exists()");
				if(!isMapEmpty())//Make function
				{
//					System.out.println("findCurrentOffset() In !isMapEmpty()");
//					if(!inLine)
					{
						dos.skipBytes(pointer-1);
//						System.out.println("findCurrentOffset() pointer:"+(pointer));
						dos.mark(500);//random integer inserted for now
						byte[] str = " ".getBytes();
						str[0] = dos.readByte();
						String string = new String(str, "ASCII");
//						System.out.println("findCurrentOffset() String:"+string);
						if(string.equals("_"))
						{
							findIntegerFromGivenOffsetAndDelimeter(-1, dos, false);
							//Go to the blockfilepath and find the count. Nested DOS.
							str[0] = dos.readByte();
							string = new String(str, "ASCII");
							if(string.equals("_"))
							{
								return dos.readInt();
							}
						}
						else if(string.equals(","))
						{
							findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false);
							return dos.readInt();
						}
						else
						{
							System.out.println("Error in finding current value and count");
							return -1;
						}
					}
				}

			}catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return -1;
	}
	
	public ArrayList currentPointerCountAndValue()
	{
		File file = new File(mapFilePath);
		ArrayList valueAndFreqPair = null;
		int offsetInBlock = findCurrentOffset(pointer, mapFilePath);
		if(offsetInBlock!=-1)
		{
//			System.out.println("currentPointerCountAndValue() offsetInBlock:"+offsetInBlock);
			valueAndFreqPair = findFrequencyAndValueForGivenOffset(offsetInBlock);
		}
		return valueAndFreqPair;
	}
	
	public void next()
	{
		File file = new File(mapFilePath);
		ArrayList returnList = new ArrayList();
		if (file.exists())
		{
			try (InputStream inputStream = new FileInputStream(mapFilePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
				if(!isMapEmpty())//Make function
//				if(!inLine)
				{
					dos.skipBytes(pointer-1);
//					System.out.println();
//					System.out.println("next() pointer:"+pointer);
					dos.mark(500);//random integer inserted for now

					byte[] str = " ".getBytes();
					str[0] = dos.readByte();pointer++;
					String string = new String(str, "ASCII");
//					System.out.println("next() string:"+string);
					//						System.out.println("String:"+string);
					if(string.equals("_"))
					{
//						System.out.println("next() pointer:"+pointer);
						findIntegerFromGivenOffsetAndDelimeter(-1, dos, true);
						//							System.out.println("value:"+findIntegerFromGivenOffsetAndDelimeter(-1, dos, false));
						dos.skipBytes(5);pointer += 5;
//						System.out.println("next() pointer:"+pointer);
					}
					else if(string.equals(","))
					{
						findStringFromGivenOffsetAndDelimeter(-1, "_", dos, true);
						//							System.out.println("value:"+findStringFromGivenOffsetAndDelimeter(-1, "_", dos, false));
						dos.skipBytes(4);pointer += 4;
					}
					else
						System.out.println("Error in next()");
				}

			}catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public int findNewMapStartForSubRelation()
	{
		File file = new File(blocksFilePath);
		ArrayList returnList = new ArrayList();
		int offset = findCurrentOffset(pointer, mapFilePath);
//		System.out.println("findNewMapStartForSubRelation() offset1:"+(offset));
		if (file.exists())
		{
			try (InputStream inputStream = new FileInputStream(blocksFilePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
					) {
				dos.skipBytes(offset-1);
				while(true)
				{
					byte[] str = " ".getBytes();
					str[0] = dos.readByte();offset++;
					String string = new String(str, "ASCII");
//					System.out.println("findNewMapStartForSubRelation() String:"+string);
					
					if(string.equals(","))
					{
//						System.out.println("findNewMapStartForSubRelation() offset2:"+(offset));
						str[0] = dos.readByte();offset++;
						string = new String(str, "ASCII");
						if(string.equals(";"))//End of Block
						{
							return -1;
						}
						if(string.equals(" "))
						{
//							System.out.println("findNewMapStartForSubRelation() offset3:"+(offset));
							return offset;
						}
					}
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return -1;
	}
	
	public void subRelation()
	{
		//When going from maps.bin to blocks.bin, that is when map is !inLine
//		if(!inLine)
		if(noOfColumns>1)
		{
			System.out.println("<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>.");
			changesForMapInline(findNewMapStartForSubRelation());
//			findCurrentOffset(35, blocksFilePath);
		}
		else
		{
			System.out.println("Subrelation Not Possible");
		}
		//When going from blocks.bin to blokcs.bin, that is when map is inLine
		
	}
	
	public static void main(String[] args) throws IOException {
		
//		relation kd = new relation();
//		
//		kd.initialiseRelationFilePath("src/map.bin", "src/blocks.bin");
//		kd.processMapHeader();
//		System.out.println("relationName:"+kd.relationName);
//		System.out.println("Distinct Values:"+kd.distinctValuesOfFirstColumn());
//		System.out.println("Location:"+kd.locationOfTuples(2));
//		kd.next();kd.next();kd.next();
//		System.out.println("isMapEmpty:"+kd.isMapEmpty());
//		kd.resetPointer();kd.next();
//		System.out.println("currentPointerCountAndValue:"+kd.currentPointerCountAndValue());
		
		relation kdsubrel = new relation();
		kdsubrel.initialiseRelationFilePath("src/map.bin", "src/blocks.bin");
		kdsubrel.processMapHeader();
		System.out.println("relationName:"+kdsubrel.relationName);
		System.out.println("Location:"+kdsubrel.locationOfTuples("3"));
		System.out.println("Distinct Values:"+kdsubrel.distinctValuesOfFirstColumn());
//		kdsubrel.next();
//		kdsubrel.jump("11");
		System.out.println("currentPointerCountAndValue:"+kdsubrel.currentPointerCountAndValue());
		
		kdsubrel.subRelation();
		System.out.println("relationName:"+kdsubrel.relationName);
		System.out.println("Location:"+kdsubrel.locationOfTuples(200));
		System.out.println("Distinct Values:"+kdsubrel.distinctValuesOfFirstColumn());
		kdsubrel.next();kdsubrel.next();
		System.out.println("isMapEmpty:"+kdsubrel.isMapEmpty());
		kdsubrel.resetPointer();kdsubrel.next();
		System.out.println("currentPointerCountAndValue:"+kdsubrel.currentPointerCountAndValue());
		
		kdsubrel.subRelation();
		System.out.println("relationName:"+kdsubrel.relationName);
		System.out.println("Location:"+kdsubrel.locationOfTuples("s3"));
		System.out.println("Distinct Values:"+kdsubrel.distinctValuesOfFirstColumn());
		kdsubrel.next();kdsubrel.next();
		System.out.println("isMapEmpty:"+kdsubrel.isMapEmpty());
		kdsubrel.resetPointer();
//		kdsubrel.next();kdsubrel.next();
		kdsubrel.jump("s4");
		System.out.println("currentPointerCountAndValue:"+kdsubrel.currentPointerCountAndValue());
//		
//		kdsubrel.subRelation();
//		System.out.println("relationName:"+kdsubrel.relationName);
//		System.out.println("Distinct Values:"+kdsubrel.distinctValuesOfFirstColumn());
//		System.out.println("currentPointerCountAndValue:"+kdsubrel.currentPointerCountAndValue());
//		
//		kdsubrel.subRelation();
//		System.out.println("relationName:"+kdsubrel.relationName);
	}

}
