package net.arisnight;

import net.arisnight.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        MainView view = new MainView(getHostServices());
        Scene scene = new Scene(view, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        for (int size : new int[]{32, 64, 128, 256}) {
            stage.getIcons().add(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/icons/icon-" + size + ".png"))));
        }

        stage.setTitle("KeyStash");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}