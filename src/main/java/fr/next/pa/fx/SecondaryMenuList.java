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
 * Seondary menu as popup menu.
 */
class SecondaryMenuList {

	/** Pop menu **/
	private Popup popupMenu;

	/** Container of seconday menu **/
	private StyledEditorCustom textComponent;

	/** Secondary menu element list **/
	private TargetElement[] secondaryMenuElements;

	/** helper action **/
	private DisplayOnAction helperAction;

	SecondaryMenuList(Popup popupMenu, StyledEditorCustom textComponent, DisplayOnAction helperAction) {
		this.textComponent = textComponent;
		this.popupMenu = popupMenu;
		this.helperAction = helperAction;
	}

	void init(int start, WCPoint bounds) {
		popupMenu.getContent().clear();
		ListView<TargetElement> list = createSuggestionList(helperAction, start);
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
	 * Create the seconday menu element list. Apply LIST class style.
	 * 
	 * @param helperAction
	 *            helper action
	 * @param start
	 *            start index in text
	 * @return list of secondary menu element as ListView
	 */
	private ListView<TargetElement> createSuggestionList(DisplayOnAction helperAction, final int start) {

		ListView<TargetElement> list = new ListView<>();
		list.getStylesheets().add(Style.CSSFILE);
		list.getStyleClass().add(Style.SECONDARYMENU);
		secondaryMenuElements = helperAction.secondaryMenu(start);

		ObservableList<TargetElement> items = FXCollections.observableArrayList(Arrays.asList(secondaryMenuElements));
		list.setItems(items);

		if (items.size() > 0) {
			list.getSelectionModel().select(0);
		}

		list.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (e.getClickCount() == 2) {
					doAction(helperAction, list);
				}
			}
		});

		list.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode() == KeyCode.ENTER) {
					doAction(helperAction, list);
				} else if (e.getCode() == KeyCode.ESCAPE) {
					hide();
				}
			}
		});
		return list;
	}

	/**
	 * Do action in container element the selected item of secondary menu. Hide
	 * the element at the end.
	 * 
	 * @param helperAction
	 *            helper action
	 * @param list
	 *            the list of secondary menu action element
	 */
	void doAction(DisplayOnAction helperAction, ListView<TargetElement> list) {
		TargetElement s = list.getSelectionModel().getSelectedItem();
		if (s != null) {
			final String selectedAction = s.getText();
			helperAction.doAction(selectedAction);
		}
		hide();
	}
}
