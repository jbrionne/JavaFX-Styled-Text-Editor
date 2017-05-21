package fr.next.pa.fx;

import java.util.Arrays;

import com.sun.webkit.graphics.WCPoint;

import fr.next.pa.api.DisplayOnAction;
import fr.next.pa.api.TargetElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;

/**
 * Autocompletion as popup menu.
 */
class AutoCompletionList {

	/** Pop menu **/
	private Popup popupMenu;

	/** Container of autocompletion **/
	private StyledEditorCustom textComponent;

	/** Autocompletion element list **/
	private TargetElement[] autoCompletionElements;

	/** helper action **/
	private DisplayOnAction helperAction;

	AutoCompletionList(Popup popupMenu, StyledEditorCustom textComponent, DisplayOnAction helperAction) {
		this.textComponent = textComponent;
		this.popupMenu = popupMenu;
		this.helperAction = helperAction;
	}

	void init(int position, String template, WCPoint bounds) {
		popupMenu.getContent().clear();
		ListView<TargetElement> list = createSuggestionList(helperAction, position, template);
		if (list.getItems().size() > 0) {
			popupMenu.getContent().addAll(list);
			popupMenu.show(textComponent, bounds.getX(), bounds.getY());
		}
	}

	/**
	 * Hide popup and initialize templates.
	 */
	void hide() {
		textComponent.reInitTemplates();
		popupMenu.hide();
	}

	/**
	 * Create the autocompletion element list. Apply LIST class style.
	 * 
	 * @param helperAction
	 *            helper action
	 * @param start
	 *            start index in text
	 * @param template
	 *            template of autocompletion to apply
	 * @param insertPosition
	 *            the insertion position in the text
	 * @return list of autocompletion element as ListView
	 */
	private ListView<TargetElement> createSuggestionList(DisplayOnAction helperAction, final int start, String template) {

		ListView<TargetElement> list = new ListView<>();
		list.getStylesheets().add(Style.CSSFILE);
		list.getStyleClass().add(Style.AUTOLIST);
		autoCompletionElements = helperAction.autocompletion(start, template);

		ObservableList<TargetElement> items = FXCollections.observableArrayList(Arrays.asList(autoCompletionElements));
		list.setItems(items);

		if (items.size() > 0) {
			list.getSelectionModel().select(0);
		}

		list.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (e.getClickCount() == 2) {
					insertSelection(list, start);
				}
			}
		});

		list.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode() == KeyCode.ENTER) {
					insertSelection(list, start);
				} else if (e.getCode() == KeyCode.ESCAPE) {
					hide();
				}
			}
		});
		return list;
	}

	/**
	 * Insert in container element the selected item of autocompletion. Hide the
	 * element at the end.
	 * 
	 * @param list
	 *            the list of autocompletion element
	 * @param insertPosition
	 *            the insertion position in the text
	 */
	void insertSelection(ListView<TargetElement> list, int insertPosition) {
		TargetElement s = list.getSelectionModel().getSelectedItem();
		if (s != null) {
			final String selectedSuggestion = s.getText().substring(s.getPosition());
			textComponent.insertTextAndUpdateCaretPosition(insertPosition, selectedSuggestion);
		}
		hide();
	}
}
