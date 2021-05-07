package it.unipi.dii.inginf.lsmdb.rechype.recipe;

public class Recipe {
    private String name;
    private String author;
    private String summary;
    private String image;
    private String description;
    private boolean vegan;
    private boolean glutenFree;
    private boolean dairyFree;
    private boolean vegetarian;
    private double servings;
    private double readyInMinute;
    private double weightPerServing;
    private double pricePerServing;
    private String method;
    private int like;

    public Recipe(){}

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

    public String getSummary() {
        return summary;
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

    public int getLike() {
        return like;
    }

    public String getMethod() {
        return method;
    }

}
