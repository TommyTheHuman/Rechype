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
        String response = Json.get("response").toString();
        if(response.equals("RegOk")) {
            loggedUser = new User(Json.get("_id").toString(), Json.get("country").toString(),
                    Integer.parseInt(Json.get("age").toString()), Integer.parseInt(Json.get("level").toString()));
        }
        return response;
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

    public String addNewRecipe(Document doc, String type){ return userDao.addNestedRecipe(doc, loggedUser, type); }
    public boolean checkRecipeLike(String _id, String type){ return userDao.checkRecipeLike(getLoggedUser().getUsername(), _id, type); }
    public boolean checkSavedRecipe(String _id, String type){ return userDao.checkSavedRecipe(getLoggedUser().getUsername(), _id, type); }
    public String removeRecipe(String _id, String type){ return userDao.removeNestedRecipe(getLoggedUser().getUsername(), _id, type); }
    @Override
    public String addFollow(String myName, String userName, String btnStatus) { return userDao.followUser(myName, userName, btnStatus);}

    @Override
    public Boolean checkForFollow(String myName, String userName) {return userDao.checkUserFollow(myName, userName);}

    public List<Document> getRecipes(String username){ return userDao.getUserRecipe(username); }


}
