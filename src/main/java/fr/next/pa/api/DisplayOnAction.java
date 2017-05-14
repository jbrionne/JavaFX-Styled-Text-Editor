package fr.next.pa.api;

import java.util.List;

/**
 * Suggestion, tooltip interface for core system.
 */
public interface DisplayOnAction {

	/**
	 * Autocompletion element proposal.
	 * 
	 * @param insertPosition
	 *            position of autocompletion request
	 * @param template
	 *            the template to display
	 * @return AutoCompletion Element array
	 */
	TargetElement[] autocompletion(int insertPosition, String template);

	/**
	 * Secondary menu element proposal
	 * 
	 * @param insertPosition
	 *            position of secondary menu request
	 * @return SecondaryMenu Element array
	 */
	TargetElement[] secondaryMenu(int insertPosition);

	/**
	 * Return the text result of the action : Full text.
	 * 
	 * @param action
	 *            the action
	 * @param parent
	 *            the parent window
	 */
	void doAction(String action);
	
	
	/**
	 * @return list of templates
	 */
	List<String> templates();

	/**
	 * Tooltip proposal.
	 * 
	 * @param position
	 *            position of tooltip request
	 * @return tooltip as string
	 */
	String tooltip(int position);

	/**
	 * Call at each event on selected text.
	 * 
	 * @param selectedText
	 *            the selected text
	 * @param caretPosition
	 *            the current caret position in text
	 */
	void onSelectedText(String selectedText, int caretPosition);

}
