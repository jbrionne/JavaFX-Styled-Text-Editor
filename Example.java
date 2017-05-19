package fr.next.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.next.pa.api.ActionInputCallBack;
import fr.next.pa.api.Display;
import fr.next.pa.api.DisplayOnAction;
import fr.next.pa.api.TargetElement;

/**
 * Core class of your system. Do what you want.
 */
public class Example implements Runnable, DisplayOnAction, ActionInputCallBack {

	private static final String DEFAULT = "default";

	private static final String EOL = System.lineSeparator();
	
	private Map<Integer, Set<Message>> highlightsOffset = new HashMap<>();

	
	private Display display;

	private Object monitorSelectedText = new Object();
	private String selectedText;
	
	private String currentSelection;

	public void init(Display display) {
		this.display = display;
	}

	@Override
	public void run() {
		String test = "HelloWorld";
		display.println(test);
		display.println(test);

		while (true) {
			clear();

			String txt = display.getText();
			String[] lines = txt.split(EOL);
			
			synchronized (monitorSelectedText) {
				if (selectedText != null && !selectedText.isEmpty() && !selectedText.trim().isEmpty()) {
					currentSelection = selectedText;
					int indexOfLine = 0;
					for (String line : lines) {
						int startOffsetOfLine = startOffsetOfLine(lines, indexOfLine);
						Pattern pattern = Pattern.compile(Pattern.quote(selectedText));
						Matcher matcher = pattern.matcher(line);
						while (matcher.find()) {
							int index = matcher.start();
							display.highlight(startOffsetOfLine + index, selectedText.length(),
									"selected-background-1");
						}
						indexOfLine++;
					}
				} 
			}
			
			if (!lines[0].startsWith(test)) {
				int startOffsetOfLine = startOffsetOfLine(lines, 0);
				display.highlight(startOffsetOfLine, lines[0].length(), "error-underline-1");
			}

			display.save();
			
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Register a style.
	 * 
	 * @param offset
	 *            the offset in the text
	 * @param length
	 *            the length
	 * @param type
	 *            the css class
	 */
	void add(int offset, int length, String type, String msg) {
		for (int i = offset; i < offset + length; i++) {
			Set<Message> types = highlightsOffset.get(i);
			if (types == null) {
				types = new HashSet<>();
			}
			types.add(new Message(type, msg));
			highlightsOffset.put(i, types);
		}
		display.highlight(offset, length, type);
	}

	/**
	 * Remove registered style.
	 */
	void clear() {
		highlightsOffset.clear();
		display.removeHighlights();
	}

	
	@Override
	public TargetElement[] autocompletion(int insertionPosition, String template) {
		// TODO Implements your own program.
		int start = insertionPosition;
		String text = display.getText();
		
		while (start > 0) {
			if (!Character.isWhitespace(text.charAt(start))) {
				start--;
			} else {
				break;
			}
		}
		
		TargetElement[] data = new TargetElement[0];
		if (start >= 0 && insertionPosition >= 0 && start <= insertionPosition) {
			data = new TargetElement[10];
			if (template.equals(DEFAULT)) {
				final String subWord = text.substring(start, insertionPosition);
				for (int i = 0; i < data.length; i++) {
					data[i] = new TargetElement(subWord.length(), subWord + i);
				}
			}
		}
		return data;
	}

	@Override
	public List<String> templates() {
		return Arrays.asList(DEFAULT);
	}

	@Override
	public String tooltip(int position) {
		// TODO Implements your own program.
		String txt = display.getText();
		if(txt != null) {
			return "Character '" + txt.substring(position, position + 1) + "'";
		} else {
			return null;
		}
	}

	@Override
	public void onSelectedText(String selectedText, int caretPosition) {
		// TODO Implements your own program.
		synchronized (monitorSelectedText) {
			this.selectedText = selectedText;
		}
	}

	/**
	 * Retrieves the start index of a line.
	 * 
	 * @param lines
	 *            the text lines
	 * @param lineNumber
	 *            the number of the current line
	 * @return the index of the line in the text
	 */
	public int startOffsetOfLine(String[] lines, int lineNumber) {
		int startOffsetOfLine = 0;
		for (int i = 0; i < lineNumber; i++) {
			startOffsetOfLine += lines[i].length() + EOL.length();
		}
		return startOffsetOfLine;
	}

	@Override
	public TargetElement[] secondaryMenu(int insertionPosition) {
		// TODO Implements your own program.
		TargetElement[] data = new TargetElement[0];
		if (currentSelection != null && !currentSelection.isEmpty()) {
			data = new TargetElement[1];
			data[0] = new TargetElement(0, "refactor");
		}
		return data;
	}

	@Override
	public void doAction(String action) {
		display.actionPopup(this, "Refactor with :");
	}

	@Override
	public void input(String result) {
		System.out.println(result);
		while (notOptimizedReplace(display.getText(), result)) {
		}
		currentSelection = null;
	}

	/**
	 * Not optimized replace action on text
	 * 
	 * @param txt
	 *            the text
	 * @param replace
	 *            the text to write
	 * @return true if replace something
	 */
	public boolean notOptimizedReplace(String txt, String replace) {
		String[] lines = txt.split(EOL);
		int indexOfLine = 0;
		for (String line : lines) {
			int startOffsetOfLine = startOffsetOfLine(lines, indexOfLine);
			Pattern pattern = Pattern.compile(Pattern.quote(currentSelection));
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				int index = matcher.start();
				display.replaceText(startOffsetOfLine + index, startOffsetOfLine + index + currentSelection.length(),
						replace);
				return true;
			}
			indexOfLine++;
		}
		return false;
	}
}
