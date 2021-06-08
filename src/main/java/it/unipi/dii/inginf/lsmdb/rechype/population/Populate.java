package it.unipi.dii.inginf.lsmdb.rechype.population;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.gui.LandingPageController;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.MongoDriver;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.ObservableList;
import org.bson.Document;
import org.decimal4j.util.DoubleRounder;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import static java.util.Arrays.asList;


public class Populate {
    private List<String> list;
    private UserService userService = UserServiceFactory.create().getService();
    private ProfileService profileService = ProfileServiceFactory.create().getService();
    private RecipeService recipeService = RecipeServiceFactory.create().getService();
    private DrinkService drinkService = DrinkServiceFactory.create().getService();

    public Populate(){
        /*list=new ArrayList<>();
        try {
            Scanner s = new Scanner(new FileInputStream("C:\\Users\\nikos\\IdeaProjects" +
                    "\\Rechype\\src\\main\\java\\it\\unipi\\dii\\inginf\\lsmdb\\rechype\\population\\usernames2.txt"));
            while(s.hasNextLine()){
                list.add(s.nextLine());
                if(list.size()==2000)
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        createUser();*/
        //flushUser();
        //flushRecipe();
        addFollows();
        //createRecipe(new User("Michael", "United States", 41, 0));
        //populateRecipesDrinks();
        //createDrink(new User("Michael", "United States", 41, 0));
        //addLikesRecipe();
        //addLikesDrink();
    }

    private void flushUser(){
       MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).drop();
       MongoDriver.getObject().getCollection(MongoDriver.Collections.PROFILES).drop();
       System.out.println("ok");
    }

    private void flushRecipe(){
        try {
            String[] values = {"Spoonacular", "PunkAPI", "CocktailsDB"};
            MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES) //recipe
                    .deleteMany(Filters.nin("author", asList(values)));
            System.out.println("ok");
        }catch(MongoException me){
            me.printStackTrace();
        }
    }

    private void createUser(){

        int min = 14;
        int max = 99;
        int age;
        int country_index;
        List<String> i_nations=new ArrayList<>();
        i_nations.add("Italy");
        i_nations.add("Japan");
        i_nations.add("United States");
        i_nations.add("Germany");
        i_nations.add("Russia");
        ObservableList<String> nations = LandingPageController.getNations();
        try {
            System.out.println("ehi");
            for (int i = 0; i < list.size(); i++) {
                age = (int) Math.floor(Math.random() * (max - min + 1) + min);
                if (i % 10 == 0) {
                    country_index=(int) Math.floor(Math.random() * (5));
                    userService.register(list.get(i), "pass", i_nations.get(country_index), age);
                }
                else{
                    country_index = (int) Math.floor(Math.random() * (249));
                    userService.register(list.get(i), "pass", nations.get(country_index), age);
                }
                profileService.createProfile(list.get(i));
            }
            System.out.println("ok");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void addFollows(){
        System.out.println("parto");
        try(MongoCursor<Document> cursor =
                    MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find().iterator()){
            while(cursor.hasNext()){
                //prendo uno user
                Document me = cursor.next();
                //gli aggiungo 5 follow casuali facendo prima check se ce l'ha già
                Document user;
                for(int i=0; i<5; i++) {
                    int skip=(int)Math.floor(Math.random() * (2000));
                    try(MongoCursor<Document> cursor2 = MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find()
                    .skip(skip).limit(1).iterator())
                    {
                        user=cursor2.next();
                        if(user.getString("_id").equals(me.getString("_id")))
                            continue;
                        if(!userService.checkForFollow(me.getString("_id"), user.getString("_id"))){
                            userService.addFollow(me.getString("_id"), user.getString("_id"),"Follow");
                        }
                    }catch (MongoException ex){
                        ex.printStackTrace();
                    }
                }

            }
            System.out.println("ok");
        }catch (MongoException me){
            me.printStackTrace();
        }
    }

    //CANCELLA SETLOGGEDUSER
    private void addLikesRecipe(){
        //prendo un sottoinsieme di ricette e di drink casuali e gli aggiungo un numero variabile di like
        int howmany=1000;
        System.out.println("Parto recipe");
        int skip1=(int)Math.floor(Math.random() * (18000));
        try(MongoCursor<Document> cursor =
                    MongoDriver.getObject().getCollection(MongoDriver.Collections.RECIPES).find().skip(skip1).limit(howmany)
        .iterator()){
            int counter=0;
            while(cursor.hasNext()) {
                Document recipe=cursor.next();
                String id=new JSONObject(recipe.toJson()).getJSONObject("_id").getString("$oid");
                //prendo uno user
                Document userDoc;
                System.out.println(recipe);
                int max=5;
                int min=1;
                //gli aggiungo un numero di likes da utenti casuali compreso tra min e max
                int index=(int)Math.floor(Math.random() * (max - min + 1) + min);
                for(int i=0; i<index; i++){
                    int skip2=(int)Math.floor(Math.random() * (2000));
                    try(MongoCursor<Document> cursorUsers =
                    MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find().skip(skip2).limit(1).iterator()){
                        userDoc=cursorUsers.next();
                        User userEntity=new User(userDoc);
                        userService.setLoggedUser(userEntity);
                        if(!userService.checkRecipeLike(id, "recipe")){
                            recipeService.addLike(id, userDoc.getString("_id"));
                        }
                    }
                }
            }
            System.out.println("ok");
        }catch (MongoException me){
            me.printStackTrace();
        }
    }

    //Add liked drink
    private void addLikesDrink(){
        System.out.println("parto drink");
        //prendo un sottoinsieme di ricette e di drink casuali e gli aggiungo un numero variabile di like
        int howmany=500;
        int skip1=(int)Math.floor(Math.random() * (1500));
        try(MongoCursor<Document> cursor =
                    MongoDriver.getObject().getCollection(MongoDriver.Collections.DRINKS).find().skip(skip1).limit(howmany)
                            .iterator()){
            int counter=0;
            while(cursor.hasNext()){
                Document recipe=cursor.next();
                String id=new JSONObject(recipe.toJson()).getJSONObject("_id").getString("$oid");
                //prendo uno user
                Document userDoc;
                int max=5;
                int min=3;
                //gli aggiungo un numero di likes da utenti casuali compreso tra min e max
                int index=(int)Math.floor(Math.random() * (max - min + 1) + min);
                for(int i=0; i<index; i++){
                    int skip2=(int)Math.floor(Math.random() * (2000));
                    try(MongoCursor<Document> cursorUsers =
                        MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find().skip(skip2).limit(1).iterator()){
                        userDoc=cursorUsers.next();

                        User userEntity=new User(userDoc);
                        userService.setLoggedUser(userEntity);
                        if(!userService.checkRecipeLike(id, "drink")){
                            drinkService.addLike(id, userDoc.getString("_id"));
                        }
                    }
                }
            }
            System.out.println("ok");
        }catch (MongoException me){
            me.printStackTrace();
        }
    }

    private void populateRecipesDrinks(){
        int howmany=100;
        int skip1=(int)Math.floor(Math.random() * (1900));
        try(MongoCursor<Document> cursor =
                    MongoDriver.getObject().getCollection(MongoDriver.Collections.USERS).find().skip(skip1).limit(howmany)
                            .iterator()){
            while(cursor.hasNext()) {
                Document doc=cursor.next();
                System.out.println(doc);
                User user=new User(doc);
                int max=10;
                int min=3;
                //gli aggiungo un numero di likes da utenti casuali compreso tra min e max
                int index=(int)Math.floor(Math.random() * (max - min + 1) + min);
                for(int i=0; i<index; i++){
                    createRecipe(user);
                    createDrink(user);
                }
            }
            System.out.println("ok");
        }catch (MongoException me){
            me.printStackTrace();
        }
    }


    private void createRecipe(User user){
        UserService userService = UserServiceFactory.create().getService();
        RecipeService recipeService = RecipeServiceFactory.create().getService();
        //add recipe (recipeDao)
        //add Nested Recipe (userDao)
        String name="Recipe of "+user.getUsername()+" "+Math.floor(Math.random() * (20));
        List<Integer> priceRange=new ArrayList<>();
        priceRange.add(500);
        priceRange.add(1000);
        priceRange.add(1500);
        priceRange.add(5000);
        int choosePrice=(int)Math.floor(Math.random() * (4));
        int sup=priceRange.get(choosePrice);
        int inf;
        if(choosePrice==0)
            inf=1;
        else
            inf=priceRange.get(choosePrice-1);
        List<List> result=ingredientsRetriever();
        List<Document> docIngredients=result.get(0);
        List<Document> docIngredientsNut=result.get(1);
        List<Document> docNutrients=new ArrayList<>();
        List<Ingredient> listIngredients=new ArrayList<>();
        for(int i=0; i<docIngredientsNut.size(); i++){
            listIngredients.add(new Ingredient(docIngredientsNut.get(i)));
        }
        String nutrName;
        Double nutrAmount;
        String nutrUnit;
        Double[] nutrientsTotAmount = {0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
        int counter = 0;
        Map<String, Integer> divi=new HashMap<>(); //divisor for each unit to transform the value in grams
        divi.put("mg", 1000);
        divi.put("cg", 100);
        divi.put("dg", 10);
        divi.put("g", 1);
        // Get the sum of nutrients of all ingredients selected
        for(Ingredient ingr: listIngredients){
            if(ingr.getNutrients() != null){
                for(int i=0; i<ingr.getNutrients().size(); i++) {
                    nutrName = ingr.getNutrients().get(i).getNutrName();
                    nutrAmount = ingr.getNutrients().get(i).getNutrAmount();
                    nutrUnit = ingr.getNutrients().get(i).getNutrUnit();
                    if (!nutrUnit.equals("kcal")) {
                        // Convert in grams
                        nutrAmount = nutrAmount / divi.get(nutrUnit);
                    }
                    if (nutrName.equals("Fiber")) {
                        // Sum nutrients considering that nutrAmount is for 100g
                        nutrientsTotAmount[0] += nutrAmount * docIngredients.get(counter).getDouble("amount")/ 100;
                    }
                    if (nutrName.equals("Carbohydrates")) {
                        nutrientsTotAmount[1] += nutrAmount * docIngredients.get(counter).getDouble("amount") / 100;
                    }
                    if (nutrName.equals("Calories")) {
                        nutrientsTotAmount[2] += nutrAmount * docIngredients.get(counter).getDouble("amount") / 100;
                    }
                    if (nutrName.equals("Sugar")) {
                        nutrientsTotAmount[3] += nutrAmount * docIngredients.get(counter).getDouble("amount") / 100;
                    }
                    if (nutrName.equals("Fat")) {
                        nutrientsTotAmount[4] += nutrAmount * docIngredients.get(counter).getDouble("amount") / 100;
                    }
                    if (nutrName.equals("Calcium")) {
                        nutrientsTotAmount[5] += nutrAmount * docIngredients.get(counter).getDouble("amount") / 100;
                    }
                    if (nutrName.equals("Protein")) {
                        nutrientsTotAmount[6] += nutrAmount * docIngredients.get(counter).getDouble("amount") / 100;
                    }
                }
            }
            counter++;
        }

        String[] nutrNames = {"Fiber","Carbohydrates","Calories","Sugar","Fat","Calcium","Protein"};

        for(Integer i = 0; i<7; i++) {
            if(nutrNames[i].equals("Calories")){
                docNutrients.add(new Document().append("name", nutrNames[i]).append("amount", DoubleRounder.round(nutrientsTotAmount[i],2)).append("unit", "kcal"));
            }else{
                docNutrients.add(new Document().append("name", nutrNames[i]).append("amount", DoubleRounder.round(nutrientsTotAmount[i],2)).append("unit", "g"));
            }
        }

        Document docRecipe=
        new Document().append("name", name).append("author", user.getUsername())
                .append("vegetarian", Math.random() < 0.5).append("vegan", Math.random() < 0.5).append("glutenFree", Math.random() < 0.5)
                .append("dairyFree", Math.random() < 0.5).append("pricePerServing", Math.floor(Math.random() * (sup - inf + 1) + inf))
                .append("weightPerServing", Math.floor(Math.random() * (500) + 1))
                .append("servings", (int)Math.floor(Math.random() * (8) + 1)).append("image", "")
                .append("description", name+" is the best one").append("readyInMinutes", (int)Math.floor(Math.random() * (120) + 5))
                .append("method", "A good recipe, check my instagram for more")
                .append("likes", 0).append("ingredients", docIngredients)
                .append("nutrients", docNutrients);
        if(recipeService.addRecipe(docRecipe).equals("RecipeAdded")) {
            userService.setLoggedUser(user);
            userService.addNewRecipe(docRecipe, "recipe");
        }
        return;
    }

    private void createDrink(User user){
        String name="Drink of "+user.getUsername()+" "+Math.floor(Math.random() * (20));
        List<String> tag=new ArrayList<>();
        tag.add("other");
        tag.add("beer");
        tag.add("cocktail");
        int randomIndex=(int)Math.floor(Math.random() * (3));
        List<List> result=ingredientsRetriever();
        List<Document> docIngredients=result.get(0);

        Document docDrink = new Document().append("name", name).append("author", user.getUsername())
                .append("description", name+" is the best one").append("image", "")
                .append("method", "A good drink, check my instagram for more").append("ingredients", docIngredients)
                .append("likes", 0).append("tag", tag.get(randomIndex));

        if(drinkService.addDrink(docDrink).equals("DrinkAdded")) {
            userService.setLoggedUser(user);
            userService.addNewRecipe(docDrink, "drink");
        }
        return;
    }

    //DA CAMBIARE FRA DRINK E RECIPE
    private List<List> ingredientsRetriever(){
        int skip1=(int)Math.floor(Math.random() * (2948));
        List<Document> ingredients = new ArrayList<>();
        List<Document> ingredientsWithNutrients = new ArrayList<>();
        List<List> listDoc=new ArrayList<>();
        IngredientService ingredientService = IngredientServiceFactory.create().getService();
        try(MongoCursor<Document> cursor =
            MongoDriver.getObject().getCollection(MongoDriver.Collections.INGREDIENTS).find(Filters.exists("nutrients", false)) //per i drink mettere Filters.exists("nutrients", false) e limit a 5
            .skip(skip1).limit(5).iterator()){
            while(cursor.hasNext()){
                Document doc = cursor.next();
                ingredientsWithNutrients.add(doc);
                Document toInsert=new Document();
                toInsert.append("ingredient", doc.getString("_id"));
                toInsert.append("amount", Math.floor(Math.random() * (300)));
                ingredients.add(toInsert);
            }
        }catch (MongoException me){
            me.printStackTrace();
        }
        listDoc.add(ingredients);
        listDoc.add(ingredientsWithNutrients);
        return listDoc;
    }

}
