package it.unipi.dii.inginf.lsmdb.rechype.recipe;

public class Recipe {

    private String name;
    private String author;
    private String image;
    private String description;
    private String method;
    private String ingredients;   // DOVRA' ESSERE UN ARRAY DI INGREDIENTS

    private boolean vegan;
    private boolean glutenFree;
    private boolean dairyFree;
    private boolean vegetarian;

    private double servings;
    private double readyInMinute;
    private double weightPerServing;
    private double pricePerServing;

    private int likes;

    public Recipe(){}

    public Recipe(String name, String author, String image, String description,
        String method, String ingredients, boolean vegan, boolean glutenFree, boolean dairyFree, boolean vegetarian,
        double servings, double readyInMinute, double weightPerServing, double pricePerServing){
        this.name = name;
        this.author = author;
        this.image = image;
        this.description = description;
        this.method = method;
        this.ingredients = ingredients;
        this.vegan = vegan;
        this.glutenFree = glutenFree;
        this.dairyFree = dairyFree;
        this.vegetarian = vegetarian;
        this.servings = servings;
        this.readyInMinute = readyInMinute;
        this.weightPerServing = weightPerServing;
        this.pricePerServing = pricePerServing;
        this.likes = 0;
    }

//     costruttore passando un documento mongo
//    public Recipe(Document docMongo){
//            name = docMongo.get......
//      assegnamenti
//    }


//     costruttore passando un documento neo4j
//    public Recipe(Record recNeo){
//    assegnamenti
//    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isDairyFree() {
        return dairyFree;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public double getPricePerServing() {
        return pricePerServing;
    }

    public double getReadyInMinute() {
        return readyInMinute;
    }

    public double getServings() {
        return servings;
    }

    public double getWeightPerServing() {
        return weightPerServing;
    }

    public int getLikes() {
        return likes;
    }

    public String getMethod() {
        return method;
    }

}
