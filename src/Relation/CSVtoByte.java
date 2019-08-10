package Relation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

public class CSVtoByte {
	
	public void useSerializer(ArrayList blocks)
	{
		try
        {
            FileOutputStream fos = new FileOutputStream("listData.bin");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(blocks);
            oos.close();
            fos.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
	}
	
//	https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
	public static byte[] intToBytes(int x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.putInt(x);
	    return buffer.array();
	}

	public static int bytesToInt(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getInt();
	}
	
	//create MAP.bin and BLOCKS.bin
	public void createBinaryFile(String filePath) throws IOException
	{
		System.out.println("CSVtoByte");
		File file = new File(filePath); 

		if (file.exists())
			file.delete(); //you might want to check if delete was successfull

		file.createNewFile();		
	}

	public void writeIntoFile(ArrayList blocks, String filePath)
	{
		File file = new File(filePath);
		
//		blocks.removeAll(blocks);
//		blocks.add(1);

		if (file.exists())
		{

			try(OutputStream outputStream = new FileOutputStream(filePath);
				DataOutputStream dos = new DataOutputStream(outputStream); //DOS because Double can only be loaded into file using this library
					) 
			{
				System.out.print("writeIntoFile() List:[");
				for(int i=1; i<blocks.size();i=i+2)
				{
//					System.out.println(i+":"+blocks.get(i));
					if(blocks.get(i).getClass().getName().equals("java.lang.Integer"))//Integer: _
					{
						outputStream.write("_".getBytes());
						System.out.print("_");
						outputStream.write(intToBytes((int) blocks.get(i)));
						System.out.print((int) blocks.get(i));
					}
					else
					{
						outputStream.write(((String) blocks.get(i-1)).getBytes());
						System.out.print(",");
						outputStream.write(((String) blocks.get(i)).getBytes());
						System.out.print(blocks.get(i));
					}
					
//					for(int ii=0; ii<blocks.size();ii++)
//					{
//						System.out.println(blocks.get(ii)+":"+blocks.get(ii).getClass().getName());
//					}
				}
				 
			} 
			catch (IOException ex) {
				ex.printStackTrace();
			}
			
//			System.out.println("Length:"+file.length());
		}
	}
	
	public void readFromFile(ArrayList blocks, String filePath)
	{
		File file = new File(filePath); 
//		System.out.println("In Read file");	
		if (file.exists())
		{

			try (InputStream inputStream = new FileInputStream(filePath);
				 DataInputStream dos = new DataInputStream(inputStream); //DOS because Double can only be loaded into file using this library
				) {
				String value = "";
				System.out.println();
				System.out.print("readFromFile()  List:[");
				int lengthOfFile = dos.available();
				while(dos.available()>0)
				{
					byte[] str = " ".getBytes();
					str[0] = dos.readByte();
					String string = new String(str, "ASCII");
					
					if(string.equals("_"))
					{
						if(value.length()>0)
						{
							System.out.print("," + value);
							value = "";
						}
						int integer = dos.readInt();
						System.out.print("_"+ integer);
					}
					else if(string.equals(","))
					{
						continue;
					}
					else if(string.equals(" "))
					{
						System.out.print(", ");
						value = "";
					}
					else if(string.equals(";"))
					{
							System.out.print(",;");
							value = "";
					}
					else	
					{
						value += string;
					}
				}
				 
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		CSVtoBinary KD = new CSVtoBinary("K", new String[]{"Integer", "String", "String"}, 3);
		ArrayList blocks = KD.performCSVtoBinary("src/K.csv");
		
//		CSVtoBinary KD = new CSVtoBinary("D", new String[]{"Integer", "Integer", "Integer", "String"}, 4);
//		ArrayList blocks = KD.performCSVtoBinary("src/d.csv");
		
		KD.displayList(blocks);
		
		CSVtoByte kd = new CSVtoByte();
		
		String filePath = "src/blocks.bin";
		kd.createBinaryFile(filePath);
		kd.writeIntoFile(blocks, filePath);
		kd.readFromFile(blocks, filePath);
	}
}