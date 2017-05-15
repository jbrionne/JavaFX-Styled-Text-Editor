package fr.next.pa.fx;

import java.util.List;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.control.skin.FXVK;
import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.WebPage;
import com.sun.webkit.graphics.WCPoint;

import fr.next.pa.api.ActionInputCallBack;
import fr.next.pa.api.DisplayOnAction;
import javafx.animation.PauseTransition;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import javafx.scene.input.MouseButton;

/**
 * Skin of styled editor. Based on HTML tags. Update the style on action.
 */
public class StyledEditorSkinCustom extends BehaviorSkinBase<StyledEditorCustom, StyledEditorBehaviorCustom> {

	/** System and of line **/
	private static final String EOL = System.lineSeparator();

	/** HighLight style **/
	private HighLight highLight = new HighLight();

	/** grid pane of styled editor **/
	private GridPane gridPane;

	/** web view **/
	private WebView webView;

	/** web page **/
	private WebPage webPage;

	/** current HTML text **/
	private String cachedHTMLText = "<html><head><link rel=\"stylesheet\" href=\""
			+ getClass().getResource("/code-area.css")
			+ "\"></head><body class=\"code-area default-fill-1\" contenteditable=\"true\"></body></html>";

	/** remove component on demand **/
	private ListChangeListener<Node> itemsListener = c -> {
		while (c.next()) {
			if (c.getRemovedSize() > 0) {
				for (Node n : c.getList()) {
					if (n instanceof WebView) {
						webPage.dispose();
					}
				}
			}
		}
	};

	/** caret position line offset **/
	private int caretLinePos;

	/** caret position column effset **/
	private int caretColPos;

	/** caret bounds **/
	private WCPoint caretBounds;

	/** horizontal scroll position **/
	private int scrollHPosition;

	/** vertical scroll position **/
	private int scrollVPosition;

	/** update the style of the current text **/
	private PauseTransition pauseUpdateStyle;

	/** mouse position scene X **/
	private double mouseSceneX;

	/** mouse position scene Y **/
	private double mouseSceneY;

	/** focus **/
	private static PseudoClass CONTAINS_FOCUS_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("contains-focus");

	/** monitor to avoid concurrent modification on highLights **/
	private Object monitorHighLights = new Object();
	
	/** previous caret position **/
	private int[] previousCaretPosition;

	public StyledEditorSkinCustom(StyledEditorCustom htmlEditor, DisplayOnAction displayOnAction) {
		super(htmlEditor, new StyledEditorBehaviorCustom(htmlEditor, displayOnAction));

		getChildren().clear();

		gridPane = new GridPane();
		gridPane.getStyleClass().add("grid");
		getChildren().addAll(gridPane);

		webView = new WebView();
		webView.setContextMenuEnabled(false);
		gridPane.add(webView, 0, 2);
		ColumnConstraints column = new ColumnConstraints();
		column.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().add(column);

		webPage = Accessor.getPageFor(webView.getEngine());

		updateCaretAndScrollPosition();

		PauseTransition pause = new PauseTransition(Duration.seconds(2));
		webView.addEventHandler(MouseEvent.ANY, e -> Platform.runLater(() -> {
			updateCaretAndScrollPosition();
			pause.setOnFinished(event -> ((StyledEditorBehaviorCustom) getBehavior())
					.showToolTip(displayOnAction.tooltip(getCaretPosition())));
			pause.playFromStart();
			((StyledEditorBehaviorCustom) getBehavior()).hideToolTip();

		}));

		webView.addEventHandler(KeyEvent.ANY, e -> Platform.runLater(() -> {
			updateCaretAndScrollPosition();
			pause.playFromStart();
			((StyledEditorBehaviorCustom) getBehavior()).hideToolTip();
		}));

		pauseUpdateStyle = new PauseTransition(Duration.seconds(1));
		pauseUpdateStyle.setOnFinished(event -> updateStyle());

		getSkinnable().focusedProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (newValue) {
						webView.requestFocus();

						scrollTo(webView, scrollHPosition, scrollVPosition);
					}
				}
			});
		});

		webView.focusedProperty().addListener((observable, oldValue, newValue) -> {
			pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, newValue);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (PlatformImpl.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD)) {
						Scene scene = getSkinnable().getScene();
						if (newValue) {
							FXVK.attach(webView);
						} else if (scene == null || scene.getWindow() == null || !scene.getWindow().isFocused()
								|| !(scene.getFocusOwner() instanceof TextInputControl)) {
							FXVK.detach();
						}
					}
				}
			});
		});

		webView.getEngine().getLoadWorker().workDoneProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				webView.requestLayout();
				scrollTo(webView, scrollHPosition, scrollVPosition);
				if(previousCaretPosition != null && caretBounds != null) {
					simulateClick(previousCaretPosition[0], previousCaretPosition[1], caretBounds.getX(), caretBounds.getY());
				}
			});

			double totalWork = webView.getEngine().getLoadWorker().getTotalWork();
			if (newValue.doubleValue() == totalWork) {
				cachedHTMLText = null;
			}
		});

		webView.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
			this.mouseSceneX = e.getSceneX();
			this.mouseSceneY = e.getSceneY();
		});

		webView.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> Platform.runLater(() -> {
			updateCaretAndScrollPosition();
			if (!e.isSynthesized()) {
				getBehavior().mousePressed(e);
			}
		}));

		webView.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> Platform.runLater(() -> {
			updateCaretAndScrollPosition();
			if (!e.isSynthesized()) {
				if (!getSelectedText().equals("")) {
					execute();
				}
				getBehavior().mouseReleased(e);
			}
		}));

		webView.addEventHandler(KeyEvent.KEY_TYPED, e -> Platform.runLater(() -> {
			updateCaretAndScrollPosition();
			execute();
		}));

		webView.setOnKeyReleased(e -> Platform.runLater(() -> {
			getBehavior().onKeyReleased(e);
		}));

		setHTMLText(cachedHTMLText);
		webView.setFocusTraversable(true);
		gridPane.getChildren().addListener(itemsListener);
	}

	/**
	 * Update caret and scroll position only if there is no selected text (else
	 * the value is equals to 0).
	 */
	private void updateCaretAndScrollPosition() {
		if (getSelectedText().equals("")) {
			String renderTree = webPage.getRenderTree(webPage.getMainFrame());
			int caretIndex = renderTree.lastIndexOf("caret: ");
			if (caretIndex != -1) {
				String caretInfo = renderTree.substring(caretIndex);
				String info[] = caretInfo.split(" ");
				caretColPos = Integer.valueOf(info[2]);
				caretLinePos = Integer.valueOf(info[9]);
				this.previousCaretPosition = webPage.getClientTextLocation(0);
				this.caretBounds = webPage.getPageClient().windowToScreen(new WCPoint(previousCaretPosition[0], previousCaretPosition[1] + previousCaretPosition[3]));
				this.scrollHPosition = getHScrollValue(webView);
				this.scrollVPosition = getVScrollValue(webView);
			}
		}
	}

	/**
	 * @return cached HTML or request to webPage the HTML text.
	 */
	public final String getHTMLText() {
		return cachedHTMLText != null ? cachedHTMLText : webPage.getHtml(webPage.getMainFrame());
	}

	/**
	 * Update current cached text and load the webPage with this html text.
	 * 
	 * @param htmlText
	 *            the html text.
	 */
	public final void setHTMLText(String htmlText) {
		cachedHTMLText = htmlText;
		webPage.load(webPage.getMainFrame(), htmlText, "text/html");
	}

	@Override
	protected void layoutChildren(final double x, final double y, final double w, final double h) {
		super.layoutChildren(x, y, w, h);
	}

	/**
	 * Print job.
	 * 
	 * @param job
	 *            the job
	 */
	public void print(PrinterJob job) {
		webView.getEngine().print(job);
	}

	/**
	 * Organize highlights with the offset/css type/priority. Synchronized.
	 * 
	 * @return highlights sections
	 */
	public List<HighLightSection> computeHighlights() {
		synchronized (monitorHighLights) {
			return highLight.computeHighlights();
		}
	}

	/**
	 * Remove HighLights. Synchronized.
	 */
	public void removeHighlights() {
		synchronized (monitorHighLights) {
			highLight.clear();
		}
	}

	/**
	 * Add a HighLight. Synchronized.
	 * 
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length of text to highlights
	 * @param type
	 *            the type
	 */
	public void highlight(int offset, int length, String type) {
		synchronized (monitorHighLights) {
			highLight.add(offset, length, type);
		}
	}

	/**
	 * @return the webpage current selected text.
	 */
	public String getSelectedText() {
		return webPage.getClientSelectedText();
	}

	/**
	 * @return the caret position in user coordinate.
	 */
	public int getCaretPosition() {
		updateCaretAndScrollPosition();
		String txt = getText();
		String[] lines = txt.split(EOL);
		int length = 0;
		for (int i = 0; i < caretLinePos; i++) {
			length = length + lines[i].length() + EOL.length();
		}
		return caretColPos + length;
	}

	/**
	 * @return caret bounds.
	 */
	public WCPoint getCaretBounds() {
		return caretBounds;
	}

	/**
	 * Add the current text at the end of the text in a html div tag.
	 * 
	 * @param msg
	 *            the message to print
	 */
	public void println(String msg) {
		String stringx = "<div>" + msg + "</div>";
		String html = getHTMLText();
		int endIndex = html.indexOf("</body></html>");
		setHTMLText(html.substring(0, endIndex) + stringx + html.substring(endIndex));
	}

	/**
	 * Insert the text at the indicated offset. User offset coordinates.
	 * 
	 * @param offset
	 *            the offset
	 * @param msg
	 *            the message to insert
	 */
	public void insertText(int offset, String msg) {
		int htmlOffset = getCorrespondingOffset(offset);
		String html = getHTMLText();
		setHTMLText(html.substring(0, htmlOffset) + msg + html.substring(htmlOffset));
	}

	/**
	 * Replace the indicated text by start and end offsets with the message in
	 * arguments. User offset coordinates.
	 * 
	 * @param start
	 *            start offset
	 * @param end
	 *            end offset
	 * @param msg
	 *            the text to insert
	 */
	public void replaceText(int start, int end, String msg) {
		int startHtmlOffset = getCorrespondingOffset(start);
		int endHtmlOffset = getCorrespondingOffset(end);
		String html = getHTMLText();
		setHTMLText(html.substring(0, startHtmlOffset) + msg + html.substring(endHtmlOffset));
		execute();
	}

	/**
	 * Convert the user offset with html system offset.
	 * 
	 * @param offset
	 *            the offset in user coordinates.
	 * @return offset in html coordinates.
	 */
	public int getCorrespondingOffset(int offset) {
		String html = getHTMLText();
		String toFind = "contenteditable=\"true\">";
		int index = html.indexOf(toFind) + toFind.length();
		int calculateOffset = -1;
		while (calculateOffset < offset) {
			if (html.charAt(index) == '<') {
				int endIndex = html.indexOf('>', index);
				String tag = html.substring(index, endIndex + 1);
				if (tag.equals("</div>")) { // || tag.equals("<br>") ) {
					// new line
					calculateOffset = calculateOffset + EOL.length();
					if (calculateOffset == offset) {
						// found
						break;
					}
				}
				index = endIndex;
			} else if (html.charAt(index) == '&') {
				int endIndex = html.indexOf(';', index);
				String tag = html.substring(index, endIndex + 1);
				if (tag.equals("&gt;") || tag.equals("&lt;")) {
					calculateOffset++;
					if (calculateOffset == offset) {
						// found
						break;
					}
					index = endIndex;
				}
			} else {
				calculateOffset++;
			}
			if (calculateOffset == offset) {
				// found
				break;
			}
			index++;
		}
		return index;
	}
	
	private void simulateClick(double x, double y, double screenX, double screenY) {
		webView.fireEvent(new MouseEvent(null, webView, MouseEvent.MOUSE_PRESSED, x, y, screenX,
				screenY, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, true,
				false, false, null));
		webView.fireEvent(new MouseEvent(null, webView, MouseEvent.MOUSE_RELEASED, x, y, screenX,
				screenY, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, true,
				false, false, null));
	}

	/**
	 * @return the user text by replacing the html tags.
	 */
	public String getText() {
		String html = getHTMLText();
		html = html.replaceAll("<div>", "");
		html = html.replaceAll("</div>", System.lineSeparator());
		html = html.replaceAll("\\<.*?>", "");
		html = html.replaceAll("&lt;", "<");
		html = html.replaceAll("&gt;", ">");
		return html;
	}

	/**
	 * Update the style.
	 */
	public void updateStyle() {
		String htmlOrigin = getHTMLText();
		htmlOrigin = htmlOrigin.replaceAll("<span.*?>", "");
		htmlOrigin = htmlOrigin.replaceAll("</span>", "");
		setHTMLText(htmlOrigin);

		List<HighLightSection> highLightSections = computeHighlights();

		for (HighLightSection highLight : highLightSections) {
			int start = highLight.getStart();
			int end = highLight.getEnd();
			int htmlStart = getCorrespondingOffset(start);
			int htmlEnd = getCorrespondingOffset(end);

			StringBuilder styles = new StringBuilder();
			for (String style : highLight.getType()) {
				if (styles.length() > 0) {
					styles.append(" ");
				}
				styles.append(style);
			}
			String html = getHTMLText();
			setHTMLText(html.substring(0, htmlStart) + "<span class=\"" + styles.toString() + "\">"
					+ html.substring(htmlStart, htmlEnd) + "</span>" + html.substring(htmlEnd));
		}
	}

	/**
	 * set current timer to 0. Update style action.
	 */
	public void execute() {
		pauseUpdateStyle.playFromStart();
	}

	/**
	 * Redirect reInitTemplates to behavior class.
	 */
	public void reInitTemplates() {
		getBehavior().reInitTemplates();
	}

	/**
	 * Show action popup.
	 * 
	 * @param inputCallBack
	 *            action input call back
	 * @param msg
	 *            the message
	 */
	public void actionPopup(ActionInputCallBack inputCallBack, String msg) {
		getBehavior().showActionPopup(inputCallBack, msg);
	}

	/**
	 * Scroll to the indicated positions.
	 * 
	 * @param view
	 *            the web view
	 * @param x
	 *            horizontal scroll position
	 * @param y
	 *            vertical scroll position
	 */
	public void scrollTo(WebView view, int x, int y) {
		view.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
	}

	/**
	 * @param view
	 *            the web view
	 * @return vertical scroll position.
	 */
	public int getVScrollValue(WebView view) {
		return (Integer) view.getEngine().executeScript("document.body.scrollTop");
	}

	/**
	 * @param view
	 *            the web view
	 * @return horizontal scroll position.
	 */
	public int getHScrollValue(WebView view) {
		return (Integer) view.getEngine().executeScript("document.body.scrollLeft");
	}

	/**
	 * @return mouse screen coordinates.
	 */
	public WCPoint getMouseBounds() {
		return webPage.getPageClient().windowToScreen(new WCPoint((float) mouseSceneX, (float) mouseSceneY));
	}
}
