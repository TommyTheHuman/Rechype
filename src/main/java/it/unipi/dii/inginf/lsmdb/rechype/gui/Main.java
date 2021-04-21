package it.unipi.dii.inginf.lsmdb.rechype.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception{
        scene = new Scene(loadFXML("login"),1400,800);

        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private static void loadFXML(String name){

    }

    public void cambiaScena(String ){

    }

    public static void main(String[] args) {
        launch(args);
    }
}
