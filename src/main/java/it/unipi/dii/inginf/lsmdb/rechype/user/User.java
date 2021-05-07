package it.unipi.dii.inginf.lsmdb.rechype.user;

public class User {
    private String username;
    private String country;
    private int age;

    public User(String user, String userCountry, int userAge){
        username = user;
        country = userCountry;
        age = userAge;
    }

    public String getUsername(){
        return username;
    }
}
