package fr.next.pa.api;

/**
 * Text interface. Possibility to add, append, insert, highlight text.
 */
public interface Display {

	/** saved file name format **/
	static final String DATE_FORMAT = "yyyy-MM-dd hh-mm";

	/**
	 * @return Text component
	 */
	Object getTextComponent();

	/**
	 * Retrieve a portion of text.
	 * 
	 * @param offset
	 *            The offset, start index
	 * @param length
	 *            Length to retrieve
	 * @return The txt as String
	 */
	String getText();


	/**
	 * Add text at the end of the current text.
	 * 
	 * @param text
	 *            The text
	 */
	void println(String text);

	/**
	 * Insert a text a the specific position.
	 * 
	 * @param offset
	 *            The offset, start index
	 * @param text
	 *            The text
	 */
	void insertText(int offset, String text);

	/**
	 * Replace the current text by the new one.
	 * 
	 * @param start
	 *            start offset
	 * @param end
	 *            end offset
	 * @param text
	 *            the new text
	 */
	void replaceText(int start, int end, String text);

	/**
	 * @return the current select text
	 */
	String getSelectedText();

	/**
	 * Remove all highlights of the current text.
	 */
	void removeHighlights();

	/**
	 * Add a highlight on a portion of text.
	 * 
	 * @param offset
	 *            The offset, start index
	 * @param length
	 *            The length of the portion of text.
	 * @param type
	 *            type of error (css class)
	 */
	void highlight(int offset, int length, String type);
	
	/**
	 * Historic.
	 */
	void save();

	/**
	 * Display a popup with msg as text and an input field.
	 * 
	 * @param actionInputCallBack
	 *            the call back for input
	 * @param msg
	 *            the message to display
	 */
	void actionPopup(ActionInputCallBack actionInputCallBack, String msg);

	/**
	 * Set index of template to 0.
	 */
	void reInitTemplates();

}
