package fr.next.pa.fx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.next.pa.api.ActionInputCallBack;
import fr.next.pa.api.Display;
import fr.next.pa.api.DisplayOnAction;

/**
 * Java implementation of Display interface.
 */
public class DisplayFx implements Display {

	/** text component **/
	private StyledEditorCustom textComp;

	public DisplayFx(DisplayOnAction displayOnAction) {
		textComp = new StyledEditorCustom(displayOnAction);
	}

	@Override
	public void save() {
		String date = new SimpleDateFormat(DATE_FORMAT).format(new Date());
		File fileName = new File("tmp-" + date + ".log");
		try (BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));) {
			outFile.write(textComp.getText());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getText() {
		return textComp.getText();
	}

	@Override
	public void println(String msg) {
		textComp.println(msg);
	}

	@Override
	public void insertText(int offsets, String msg) {
		textComp.insertText(offsets, msg);
	}

	@Override
	public void replaceText(int start, int end, String msg) {
		textComp.replaceText(start, end, msg);
	}

	@Override
	public void removeHighlights() {
		synchronized (this) {
			textComp.removeHighlights();
		}
	}

	@Override
	public void highlight(int offset, int length, String type) {
		synchronized (this) {
			textComp.highlight(offset, length, type);
		}
	}

	@Override
	public StyledEditorCustom getTextComponent() {
		return textComp;
	}

	@Override
	public String getSelectedText() {
		return textComp.getSelectedText();
	}

	@Override
	public void actionPopup(ActionInputCallBack inputCallBack, String msg) {
		textComp.actionPopup(inputCallBack, msg);
	}

	@Override
	public void reInitTemplates() {
		textComp.reInitTemplates();
	}
}
