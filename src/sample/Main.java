package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        BorderPane rootElement = (BorderPane) loader.load();
        Scene scene = new Scene(rootElement, 800, 600);
        //dont know why this is here
        //scene.getStylesheets().add(getClass().getResource("whatever.css").toExternalForm());
        primaryStage.setTitle("Camera fun with OpenCV and JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we)
            {
                controller.setClosed();
            }
        }));
    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
