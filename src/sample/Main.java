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
    public void start(Stage primaryStage) throws Exception {
        try {
            //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            BorderPane rootElement = (BorderPane) loader.load();
            // whitesmoke background
            //rootElement.setStyle("-fx-background-color: whitesmoke;");
            Scene scene = new Scene(rootElement, 800, 600);
            //dont know why this is here
            //scene.getStylesheets().add(getClass().getResource("whatever.css").toExternalForm());
            primaryStage.setTitle("Face Detection");
            primaryStage.setScene(scene);
            primaryStage.show();

            Controller controller = loader.getController();
            // initialize basic objects
            controller.init();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    controller.setClosed();
                }
            }));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
