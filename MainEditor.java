package fr.next.model;

import fr.next.pa.api.Display;
import fr.next.pa.fx.DisplayFx;
import fr.next.pa.fx.StyledEditorCustom;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main Editor with javaFX
 */
public class MainEditor extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("EditorFx");
		StackPane root = new StackPane();
		Scene scene = new Scene(root, 300, 250);
		primaryStage.setScene(scene);

		final Example media = new Example();
		Display display = new DisplayFx(media);
		media.init(display);
		StyledEditorCustom codeArea = (StyledEditorCustom) display.getTextComponent();
		root.getChildren().add(codeArea);

		primaryStage.show();

		// Launching of the core system
		Thread t = new Thread() {
			public void run() {
				media.run();
			}
		};
		t.start();
	}
}
