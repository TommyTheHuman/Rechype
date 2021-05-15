package it.unipi.dii.inginf.lsmdb.rechype.user;

public class User {
    private String username;
    private String country;
    private int age;
    private int level;

    public User(String user, String userCountry, int userAge, int userLevel){
        username = user;
        country = userCountry;
        age = userAge;
        level = userLevel;
    }

    public String getUsername(){
        return username;
    }
    public String getCountry() { return country; }
    public int getAge() { return age; }
    public int getLevel() { return level; }
}
