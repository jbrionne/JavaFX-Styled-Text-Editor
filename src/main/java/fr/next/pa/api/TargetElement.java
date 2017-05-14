package fr.next.pa.api;

/**
 * AutoCompletion element.
 */
public class TargetElement {

	/** position in text **/
	private int position;
	
	/** target value **/
	private String text;
	
	public TargetElement(int position, String text) {
		super();
		this.position = position;
		this.text = text;
	}
	
	public int getPosition() {
		return position;
	}
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		//Display
		return text;
	}
}
