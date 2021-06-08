package it.unipi.dii.inginf.lsmdb.rechype.gui;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.rechype.population.Populate;
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
import org.bson.Document;
import org.json.JSONObject;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Main extends Application {
    private static Scene mainScene;

    @Override
    public void start(Stage primaryStage) {
        //populateNeo4j();
        //Populate pop=new Populate();
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

    //populating neo4j
    private static void populateNeo4j() {
        System.out.println("ehi");
        MongoCollection<Document> recipesColl = MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES);
        MongoCollection<Document> drinksColl = MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS);
        MongoCollection<Document> ingredientsColl = MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS);

        //INGREDIENTS
        try (MongoCursor<Document> cursor = ingredientsColl.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                //inserting ingredients on neo4j
                try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        if(doc.get("image")!=null) {
                            tx.run("CREATE (i:Ingredient { id:$id, imageUrl: $imageUrl})",
                            parameters("id", doc.getString("_id"),
                                    "imageUrl", doc.getString("image")));
                        }
                        else
                            tx.run("CREATE (i:Ingredient { id:$id })",
                                    parameters("id", doc.getString("_id")));
                        return null;
                    });
                }catch(Neo4jException ne){
                    ne.printStackTrace();
                }
            }
        } catch (MongoException me) {
            me.printStackTrace();
        }

        //RECIPES
        try (MongoCursor<Document> cursor = recipesColl.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                //inserting recipes on neo4j
                try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                    session.writeTransaction((TransactionWork<Void>) tx -> {

                        tx.run("CREATE (r:Recipe { id:$id, name: $name, author: $author, pricePerServing: $pricePerServing, imageUrl: $imageUrl," +
                                        "vegetarian: $vegetarian, vegan: $vegan, dairyFree: $dairyFree, glutenFree: $glutenFree} )",
                                parameters("id", doc.getObjectId("_id").toString(), "name", doc.getString("name"), "author", doc.getString("author"), "pricePerServing", doc.get("pricePerServing"),
                                        "imageUrl", doc.getString("image"), "vegetarian", doc.getBoolean("vegetarian"), "vegan", doc.getBoolean("vegan"), "dairyFree", doc.getBoolean("dairyFree"),
                                        "glutenFree", doc.getBoolean("glutenFree")));
                        return null;
                    });
                }catch(Neo4jException ne){
                    ne.printStackTrace();
                }
            }
        } catch (MongoException me) {
            me.printStackTrace();
        }

        //DRINKS
        try (MongoCursor<Document> cursor = drinksColl.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                try (Session session = Neo4jDriver.getObject().getDriver().session()) { //try to add
                    session.writeTransaction((TransactionWork<Void>) tx -> {
                        tx.run("CREATE (d:Drink { id:$id, name: $name, author: $author, imageUrl: $imageUrl, " +
                                        "tag: $tag})",
                                parameters("id", doc.getObjectId("_id").toString(), "name", doc.getString("name"), "author", doc.getString("author"),
                                        "imageUrl", doc.getString("image"), "tag", doc.getString("tag")));
                        return null;
                    });
                }catch(Neo4jException ne){
                    ne.printStackTrace();
                }
            }
        }catch (MongoException me) {
            me.printStackTrace();
        }
        System.out.println("ok");
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
