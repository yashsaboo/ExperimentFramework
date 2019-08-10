package Relation;

public class StringSimilarity {
	
	public static int noOfCharactersSameFromStart(String counterString, String comparator)
	{
		String longer = counterString, shorter = comparator;
		if (counterString.length() < comparator.length()) { // longer should always have greater length
			longer = comparator; shorter = counterString;
		}
		
		for(int i = shorter.length(); i>-1;i--)
		{
			if(longer.substring(0, i).equals(shorter.substring(0, i)))
			{
				System.out.println(shorter);
				System.out.println(longer);
//				System.out.println(longer.substring(0, i));
//				System.out.println(shorter.substring(0, i));
				System.out.println(i);
				System.out.println("<<>>");
				return i;
			}
		}
		return 0;
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
	
	public static boolean goNextOrNotForInteger(int counterInt, int val)//Next = 1; Don't = 0
	{
		if(val>counterInt)
			return true;
		else
			return false;
	}
	
	public static void main(String[] args) {
		
		System.out.println((int)'b');
		
		goNextOrNotForString("", "");
		goNextOrNotForString("1234567890", "1");
		goNextOrNotForString("1234567890", "123");
		goNextOrNotForString("1234567890", "1234567");
		goNextOrNotForString("1234567890", "1234567890");
		goNextOrNotForString("1234567890", "1234567980");
		goNextOrNotForString("The quick fox jumped", "The fox jumped");
		goNextOrNotForString("The quick fox jumped", "The fox");
		goNextOrNotForString("kitten", "sitting");
	  }
	
}
