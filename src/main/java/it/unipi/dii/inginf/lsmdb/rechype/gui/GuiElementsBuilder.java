package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class GuiElementsBuilder {

    public HBox createUserBlock(User user){
        HBox block = new HBox();
        Text userNode = new Text(user.getUsername());
        Text ageNode = new Text(String.valueOf(user.getAge()));
        ImageView countryNode = null;
        ImageView levelNode = null;

        InputStream inputFlag;
        inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/" + user.getCountry() + ".png");
        InputStream inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/" + String.valueOf(user.getLevel()) + ".png");

        if (inputFlag == null) {
            inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/Default.png");

        }
        countryNode = new ImageView(new Image(inputFlag));
        levelNode = new ImageView(new Image(inputAvatar,50, 50, false, false));


        block.getChildren().addAll(levelNode, userNode, ageNode, countryNode);

        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);

        block.setId(user.getUsername());

        block.setOnMouseClicked((MouseEvent e) ->{

            JSONObject par = new JSONObject().put("id", user.getUsername());

            Main.changeScene("UserProfile", par);
        });
        return block;
    }

    public HBox createRecipeBlock(Recipe recipe) {
        HBox mainContainer=new HBox();
        VBox textContainer=new VBox();
        HBox iconContainer=new HBox();
        ImageView imageRecipe=null;
        InputStream inputStream;

        if(recipe.isVegan()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegan_on.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegan.png");
        }
        ImageView veganIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        veganIcon.setPreserveRatio(true);
        veganIcon.setSmooth(true);
        veganIcon.setCache(true);

        if(recipe.isVegetarian()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegetarian_on.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegetarian.png");
        }
        ImageView vegetarianIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        vegetarianIcon.setPreserveRatio(true);
        vegetarianIcon.setSmooth(true);
        vegetarianIcon.setCache(true);

        if(recipe.isGlutenFree()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/gluten_on.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/gluten.png");
        }
        ImageView glutenIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        glutenIcon.setPreserveRatio(true);
        glutenIcon.setSmooth(true);
        glutenIcon.setCache(true);

        if(recipe.isDairyFree()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/dairy_on.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/dairy.png");
        }
        ImageView dairyIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        dairyIcon.setPreserveRatio(true);
        dairyIcon.setSmooth(true);
        dairyIcon.setCache(true);

        inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/cloche.png");
        ImageView standardIconRecipe=new ImageView(new Image(inputStream, 20, 20, false, false));

        //setting the recipe's image of the block
        if(recipe.getImage()!=null) {
            try {
                InputStream imageStream = new URL(recipe.getImage()).openStream();
                imageRecipe=new ImageView(new Image(imageStream, 50, 50, false, false));
            }catch(IOException ie){
                LogManager.getLogger("GuiElementsBuilder.class").info("Recipe's image not found");
            }
        }

        //if the image is not loaded from the network a standard image is used
        if(imageRecipe==null){
            imageRecipe = standardIconRecipe;
        }

        //setting the price icon
        Text priceIcon = new Text(Recipe.getPriceSymbol(recipe.getPricePerServing()));
        priceIcon.setStyle("-fx-font-size: 16px");
        iconContainer.getChildren().addAll(veganIcon, vegetarianIcon, dairyIcon, glutenIcon, priceIcon);
        iconContainer.setSpacing(10);
        String name=recipe.getName();
        if(name.length()>40){
            name=name.substring(0, 40)+"...";
        }
        Text text=new Text(name);
        textContainer.getChildren().addAll(iconContainer, text);

        mainContainer.getChildren().addAll(imageRecipe, textContainer);
        mainContainer.setSpacing(5);

        mainContainer.setOnMouseClicked((MouseEvent e) ->{
            Main.changeScene("RecipePage", recipe.getJSON());
        });

        return mainContainer;
    }
}
