package it.unipi.dii.inginf.lsmdb.rechype.user;

import org.bson.Document;

import java.io.Serializable;

public class User implements Serializable {
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

    public User(Document doc){
        this.username = doc.get("_id").toString();
        this.country = doc.get("country").toString();
        this.age = Integer.parseInt(doc.get("age").toString());
        this.level = Integer.parseInt(doc.get("level").toString());
    }

    public String getUsername(){
        return username;
    }
    public String getCountry() { return country; }
    public int getAge() { return age; }
    public int getLevel() { return level; }
}
