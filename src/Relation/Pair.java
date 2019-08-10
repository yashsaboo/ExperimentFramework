package Relation;

public class Pair<Object, Integer> {
	
	private Object value;
    private int offset;
    
	public Pair(Object value, int offset) {
		this.value = value;
		this.offset = offset;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
