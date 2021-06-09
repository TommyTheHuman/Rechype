package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.Neo4jDriver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import java.io.IOException;


public class Main extends Application {
    private static Scene mainScene;

    @Override
    public void start(Stage primaryStage) {
        mainScene = new Scene(loadFXML("Landing", new JSONObject()), 1000, 700);
        primaryStage.setTitle("Rechype");
        primaryStage.setScene(mainScene);
        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.setOnHiding( event -> {
            HaloDBDriver.getObject().closeConnection();
            MongoDriver.getObject().closeConnection();
            Neo4jDriver.getObject().closeConnection();
        }
        );
    }

    public static void main(String[] args){
        launch(args);
    }

    private static Parent loadFXML(String fxml, JSONObject par){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/" + fxml + ".fxml"));
            Parent returnValue =  fxmlLoader.load();
            JSONAdder controller = fxmlLoader.getController();
            //assign the JSON object to a variable in the controller
            controller.setParameters(par);
            //this function can load the GUI with parameters from the JSON object
            controller.setGui();
            return returnValue;
        }catch (IOException ie){
            LogManager.getLogger(Main.class.getName()).error("IO: Failed to load resources");
        }
        return null;
    }

    public static void changeScene(String fxml, JSONObject parameters){
        mainScene.setRoot(loadFXML(fxml, parameters));
    }

}
