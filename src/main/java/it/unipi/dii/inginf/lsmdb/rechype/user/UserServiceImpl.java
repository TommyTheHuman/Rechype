package it.unipi.dii.inginf.lsmdb.rechype.user;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

class UserServiceImpl implements UserService {
    private static UserDao userDao=new UserDao();
    private static User loggedUser;

    public boolean login(String user, String pass){
        loggedUser = userDao.checkLogin(user, pass);
        if(loggedUser == null){
            return false;
        }
        return true;
    }

    public String register(String username, String password, String confPassword, String country, int age){
        JSONObject Json =  userDao.checkRegistration(username, password, confPassword, country, age);
        loggedUser = new User(Json.get("_id").toString(), Json.get("country").toString(), Integer.parseInt(Json.get("age").toString()), Integer.parseInt(Json.get("level").toString()));
        return Json.get("response").toString();
    }
    public User getLoggedUser(){
        return loggedUser;
    }


    public List<User> searchUser(String text, int offset, int quantity) {
        return userDao.getUsersByText(text, offset, quantity);
    }

    public JSONObject getCachedUser(String key){
        return userDao.getUserByKey(key);
    }

    public String deleteUser(String username){
        if(userDao.deleteUser(username))
            return "DeleteOk";
        else
            return "Abort";
    }

    public List<Document> getNestedRecipes(String user){
        return userDao.getUserRecipe(user);
    }

    @Override
    public String addFollow(String myName, String userName, String btnStatus) { return userDao.followUser(myName, userName, btnStatus);}

    @Override
    public Boolean checkForFollow(String myName, String userName) {return userDao.checkUserFollow(myName, userName);}

    public String addNewRecipe(Document doc){ return userDao.addNestedRecipe(doc, loggedUser); }
}
