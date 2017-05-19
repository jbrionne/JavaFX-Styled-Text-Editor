package fr.next.pa.fx;

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import fr.next.pa.api.ActionInputCallBack;
import fr.next.pa.api.DisplayOnAction;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

/**
 * Behavior of styled editor. Mouse and key event. Popup management.
 */
public class StyledEditorBehaviorCustom extends BehaviorBase<StyledEditorCustom> {

	/** autocompletion popup **/
	private Popup autocompletionPopup;

	/** secondary menu popup **/
	private Popup secondaryMenuPopup;

	/** tooltip **/
	private Popup tooltip;

	/** autocompletion list **/
	private AutoCompletionList autoCompletionList;

	/** autocompletion list **/
	private SecondaryMenuList secondaryMenuList;

	/** tooltip message **/
	private Label tooltipMsg;

	/** template index of autocompletion **/
	private int templateIndex = 0;

	/** templates **/
	private List<String> templates;

	/** on action **/
	private DisplayOnAction displayOnAction;

	/**
	 * @param htmlEditor
	 *            htmlEditor control class
	 * @param displayOnAction
	 *            call back engine
	 */
	public StyledEditorBehaviorCustom(StyledEditorCustom htmlEditor, DisplayOnAction displayOnAction) {
		super(htmlEditor, new ArrayList<KeyBinding>());
		this.displayOnAction = displayOnAction;
		this.templates = displayOnAction.templates();

		// prepare autocompletion popup
		this.autocompletionPopup = new Popup();
		autocompletionPopup.setAutoFix(false);
		autocompletionPopup.setHideOnEscape(true);
		autocompletionPopup.setAutoHide(true);
		autocompletionPopup.hide();
		this.autoCompletionList = new AutoCompletionList(autocompletionPopup, getControl(), displayOnAction);

		// prepare secondary menu popup
		this.secondaryMenuPopup = new Popup();
		secondaryMenuPopup.setAutoFix(false);
		secondaryMenuPopup.setHideOnEscape(true);
		secondaryMenuPopup.setAutoHide(true);
		secondaryMenuPopup.hide();
		this.secondaryMenuList = new SecondaryMenuList(secondaryMenuPopup, getControl(), displayOnAction);

		// prepare tooltip popup
		this.tooltip = new Popup();
		this.tooltipMsg = new Label();
		tooltipMsg.getStylesheets().add(Style.CSSFILE);
		tooltipMsg.getStyleClass().add(Style.TOOLTIP);
		tooltip.getContent().add(tooltipMsg);
		tooltip.hide();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		reInitTemplates();
		autocompletionPopup.hide();
		secondaryMenuPopup.hide();
		tooltip.hide();

		if (e.isSecondaryButtonDown()) {
			showSecondayMenu();
		}
	}
	
	/**
	 * @return true if one of popup is currently shown.
	 */
	public boolean isShowing() {
		return autocompletionPopup.isShowing() || secondaryMenuPopup.isShowing() || tooltip.isShowing();
	}

	/**
	 * if Ctrl + space key then we show autocompletion popup.
	 * 
	 * @param e
	 *            key event
	 */
	public void onKeyReleased(KeyEvent e) {
		if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
			showAutoCompletion(templates.get(templateIndex));
			templateIndex++;
			if (templateIndex >= templates.size()) {
				reInitTemplates();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		displayOnAction.onSelectedText(getControl().getSelectedText(), getControl().getCaretPosition());
	}

	/**
	 * Set the current template index to 0.
	 */
	public void reInitTemplates() {
		this.templateIndex = 0;
	}

	/**
	 * Show the autocompletion popup.
	 * 
	 * @param template
	 *            the template to apply
	 */
	void showAutoCompletion(String template) {
		final int position = getControl().getCaretPosition();
		autoCompletionList.init(position, template, getControl().getCaretBounds());
	}

	/**
	 * Show the tooltip popup.
	 * 
	 * @param text
	 *            the text to display
	 */
	void showToolTip(String text) {
		tooltipMsg.setText(text);
		tooltip.show(getControl(), getControl().getMouseBounds().getX(), getControl().getMouseBounds().getY());
	}

	/**
	 * Hide the tooltip popup.
	 */
	void hideToolTip() {
		tooltip.hide();
	}

	/**
	 * Show the secondary menu component
	 */
	void showSecondayMenu() {
		final int position = getControl().getCaretPosition();
		secondaryMenuList.init(position, getControl().getCaretBounds());
	}

	/**
	 * Show the action Popup in order to accept a user input.
	 * 
	 * @param actionInputCallBack
	 *            action input call back
	 * @param msg label of the popup
	 */
	public void showActionPopup(ActionInputCallBack actionInputCallBack, String msg) {
		hideAllPopups();

		Popup menuPopup = new Popup();
		menuPopup.setAutoFix(false);
		menuPopup.setHideOnEscape(true);
		menuPopup.setAutoHide(false);

		Label msgLabel = new Label(msg);
		TextField input = new TextField();
		VBox popUpVBox = new VBox(5, msgLabel, input);
		popUpVBox.getStylesheets().add(Style.CSSFILE);
		popUpVBox.getStyleClass().add(Style.ACTION);
		menuPopup.getContent().addAll(popUpVBox);
		menuPopup.show(getControl(), getControl().getCaretBounds().getX(), getControl().getCaretBounds().getY());

		input.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode() == KeyCode.ENTER) {
					menuPopup.hide();
					actionInputCallBack.input(input.getText());
				} else if (e.getCode() == KeyCode.ESCAPE) {
					menuPopup.hide();
				}
			}
		});
	}

	/**
	 * Hide autocompletion, secondary menu and tooltip popups.
	 */
	private void hideAllPopups() {
		secondaryMenuPopup.hide();
		autocompletionPopup.hide();
		tooltip.hide();
	}
}
