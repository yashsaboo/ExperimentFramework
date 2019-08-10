package Relation;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class FileExperiment {
	
	String filePath = "src/blocks.bin";
	File file = new File(filePath); 
	
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
	
	public static int stringBinaryToInteger(String str)
	{
		int result = 0;
		int power = 0;
		for(int i=str.length()-1; i>=0;i--)//Decrementing since we need to go from right to left
		{
			System.out.println("str.charAt(i):"+str.charAt(i));
			result += Integer.parseInt(""+str.charAt(i))*Math.pow(2, power++);
		}
		System.out.println("Result:"+result);
		return result;
	}
	
	public static String generateStringBinaryForMapColumns()//<padding><1stCol><2ndCol>...<nthCol> //Integer = 1, String = 0
	{
		int noOfColumns = 4;
		String[] columnDatatypes = new String[]{"Integer", "Integer", "String", "String"};
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
	
	public static void doMark(DataInputStream dos)
	{
		try{

			dos.reset();
			byte[] str = " ".getBytes();
			str[0] = dos.readByte();
			String string = new String(str, "ASCII");
			System.out.println("Hi"+string);
			
//			System.out.println("Hi"+dos.readInt());
			
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) throws IOException {
//		int a = 19999;
//		ByteBuffer buf = ByteBuffer.allocate(4); // sizeof(int)
//		System.out.println("generateStringBinaryForMapColumns():"+generateStringBinaryForMapColumns());
//		
//	
//		byte[] b = intToBytes(0);
////		System.out.println((String)b[1]);
//		for( int i=0; i<b.length; i++)
//		{
//			System.out.println(b[i]);
//		}
//		
//		int x = bytesToInt(b);
//		System.out.println("X:"+x);


//		
//		
//		
//		String str = Integer.toBinaryString(a);
//		System.out.println(str.length());
//		byte[] b = new byte[16];
//		for( int i=0; i<str.length(); i++)
//		{
//			b[i] = Byte.parseByte(str.charAt(i)+"");
////			System.out.println(b[i]);
//		}
//
		
//
//		if (file.exists())
//			file.delete(); //you might want to check if delete was successfull
//
//		file.createNewFile();
		String filePath = "src/blocks.bin";
		File file = new File(filePath); 
		if (file.exists())
		{

//			try(OutputStream outputStream = new FileOutputStream(filePath);
//					DataOutputStream dos = new DataOutputStream(outputStream); //DOS because Double can only be loaded into file using this library
//					) 
//			{
//				outputStream.write(b);
//			}
//			catch (Exception e)
//			{
//				System.out.println(e.getMessage());
//			}


			try (InputStream inputStream = new FileInputStream(filePath);
					DataInputStream dos = new DataInputStream(new BufferedInputStream(inputStream));
					//DOS because Double can only be loaded into file using this library
					) {
				dos.skipBytes(73);
				dos.mark(15);
				byte[] str = " ".getBytes();
				str[0] = dos.readByte();
				String string = new String(str, "ASCII");
				System.out.println("Hi"+string);
				
				doMark(dos);
				
				dos.reset();
				str[0] = dos.readByte();
				string = new String(str, "ASCII");
				System.out.println("Hi"+string);
				
//				System.out.println("Hi"+dos.readInt());
				
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
	}
}
