package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GuiElementsBuilder {

    List<String> idSelectedIngredient;

    public GuiElementsBuilder(){
        idSelectedIngredient = new ArrayList<>();
    }

    public HBox createUserBlock(User user){
        HBox block = new HBox();
        Text userNode = new Text(user.getUsername());
        Text ageNode = new Text(String.valueOf(user.getAge()));
        ImageView countryNode = null;
        ImageView levelNode = null;
        InputStream inputFlag;
        inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/" + user.getCountry() + ".png");

        //setting the icon based on level
        InputStream inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        if(user.getLevel()<5){
            inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        }
        else if(user.getLevel()<10){
            inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/1.png");
        }
        else if(user.getLevel()<15){
            inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/2.png");
        }

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
            JSONObject par = new JSONObject().put("_id", user.getUsername());
            Main.changeScene("UserProfile", par);
            //flushing cache
            HaloDBDriver.getObject().flush();
        });
        return block;
    }

    //creating an ingredient block for a selected search vbox
    public HBox createIngredientBlock(Ingredient ingredient, VBox selectedIngredientVBox){

        HBox block = new HBox();
        Text nameNode = new Text(ingredient.getName());
        String imageUrl;
        Label amount = new Label("");

        if(selectedIngredientVBox == null){
            imageUrl = ingredient.getImageUrl();
            String imgOk = imageUrl.replaceAll("\\s", "-");
            imgOk = imgOk + ".jpg";
            imageUrl = imgOk;
            amount.setText(ingredient.getQuantity().toString());
        }else{
            String imageName = ingredient.getImageUrl();

            imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/" + imageName;

        }

        ImageView imageNode = null;

        try{
            InputStream imageStream = new URL(imageUrl).openStream();
            imageNode = new ImageView(new Image(imageStream, 50,50,false,false));
        }catch(IOException e){
            LogManager.getLogger("AddIngredientController.class").info("Ingredient's image not found");
        }

        if(amount.getText().equals("")) {
            block.getChildren().addAll(imageNode, nameNode);
        }else{
            block.getChildren().addAll(imageNode, nameNode, amount);
        }

        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);
        block.setId(ingredient.getName());

        block.setOnMouseClicked((MouseEvent e) ->{
            if(!idSelectedIngredient.contains(ingredient.getName())){
                idSelectedIngredient.add(ingredient.getName());
                Text selectedNode = new Text(ingredient.getName());
                Button deleteIngredient = new Button("Delete");
                TextField quantity = new TextField();
                quantity.setPrefWidth(50);
                Text grams = new Text("g");
                HBox blockSelected = new HBox(selectedNode);
                HBox controllerIngredient = new HBox(deleteIngredient, quantity, grams);
                controllerIngredient.setSpacing(10.0);
                VBox vbox = new VBox();
                vbox.getChildren().addAll(blockSelected, controllerIngredient, new Separator(Orientation.HORIZONTAL));
                vbox.setSpacing(10.0);
                blockSelected.setAlignment(Pos.CENTER_LEFT);
                blockSelected.setSpacing(10.0);
                selectedIngredientVBox.getChildren().addAll(vbox);

                deleteIngredient.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        selectedIngredientVBox.getChildren().remove(vbox);
                        idSelectedIngredient.remove(ingredient.getName());
                    }
                });
            }
        });
        return block;

    }

    public HBox createSimpleIngredientBlock(JSONObject ingredient){
        HBox block = new HBox();
        Text nameNode;
        String imageName;
        if(ingredient.has("ingredient")) {
            nameNode = new Text(ingredient.getString("ingredient"));
            imageName=ingredient.getString("ingredient").replace(" ", "-");
        }else{
            nameNode = new Text(ingredient.getString("name"));
            imageName=ingredient.getString("name").replace(" ", "-");
        }
        Text amount;
        if(ingredient.get("amount") instanceof String)
            amount = new Text("Amount: "+ingredient.getString("amount"));
        else
            amount = new Text("Amount: "+ingredient.get("amount"));


        String imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/" + imageName+".jpg";
        ImageView imageNode = null;
        InputStream imageStream;
        try{
            imageStream = new URL(imageUrl).openStream();
            imageNode = new ImageView(new Image(imageStream, 50,50,false,false));
        }catch(IOException e){
            imageStream = GuiElementsBuilder.class.getResourceAsStream("/images/icons/no.png");
            imageNode = new ImageView(new Image(imageStream, 50,50,false,false));
            LogManager.getLogger("AddIngredientController.class").info("Ingredient's image not found");
        }
        block.getChildren().addAll(imageNode, new VBox(nameNode, amount));

        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);
        return block;
    }

    public HBox createRecipeBlock(Recipe recipe) {
        HBox mainContainer=new HBox();
        VBox textContainer=new VBox();
        HBox iconContainer=new HBox();
        ImageView imageRecipe=null;
        InputStream inputStream;

        if(recipe.isVegan()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegan_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegan_e.png");
        }
        ImageView veganIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        veganIcon.setCache(true);

        if(recipe.isVegetarian()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegetarian_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegetarian_e.png");
        }
        ImageView vegetarianIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        vegetarianIcon.setCache(true);

        if(recipe.isGlutenFree()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/gluten_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/gluten_e.png");
        }
        ImageView glutenIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        glutenIcon.setCache(true);

        if(recipe.isDairyFree()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/dairy_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/dairy_e.png");
        }
        ImageView dairyIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        dairyIcon.setCache(true);

        inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/cloche.png");
        ImageView standardIconRecipe=new ImageView(new Image(inputStream, 50, 50, false, true));

        //setting the recipe's image of the block
        if(recipe.getImage()!=null) {
            try {
                InputStream imageStream = new URL(recipe.getImage()).openStream();
                imageRecipe=new ImageView(new Image(imageStream, 50, 50, false, true));
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
        mainContainer.setId("mainContainer");

        mainContainer.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", recipe.getId()).append("cached", true);
            Main.changeScene("RecipePage", par);
            //flushing cache
            HaloDBDriver.getObject().flush();
        });

        return mainContainer;
    }

    public HBox createDrinkBlock(Drink drink){
        HBox block=new HBox();
        VBox textBlock=new VBox();
        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);
        textBlock.getChildren().addAll(new Text("name: "+drink.getName()),new Text("author: "+drink.getAuthor()), new Text("tag: "+drink.getTag()));
        InputStream imageStream=null;
        try {
            imageStream = new URL(drink.getImage()).openStream();
        }catch (IOException ie) {
            LogManager.getLogger("GuiElementsBuilder.class").info("Recipe's image not found");
            imageStream = GuiElementsBuilder.class.getResourceAsStream("/images/icons/cloche.png");
        }
        ImageView drinkImage = new ImageView(new Image(imageStream, 50, 50, false, false));
        block.getChildren().addAll(drinkImage, textBlock);

        block.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", drink.getId());
            Main.changeScene("DrinkPage", par);
            //flushing cache
            HaloDBDriver.getObject().flush();
        });
        return block;
    }
}
