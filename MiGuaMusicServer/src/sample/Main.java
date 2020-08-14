package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        //Application.setUserAgentStylesheet(getClass().getResource("stylecss.css").toExternalForm());
        primaryStage.setTitle("米瓜音乐服务端");
        primaryStage.getIcons().add(new Image("/image/icon.png"));
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 1300, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
