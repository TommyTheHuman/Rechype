package it.unipi.dii.inginf.lsmdb.rechype.user;

class UserServiceImpl implements UserService {
    private static UserDao userDao=new UserDao();
    private static User loggedUser;

    public boolean login(String user, String pass){
        loggedUser = userDao.checkLogin(user, pass);
        if(loggedUser.equals(null)){
            return false;
        }
        return true;
    }

    public String register(String username, String password, String confPassword, String country, int age){
        return userDao.checkRegistration(username, password, confPassword, country, age);
    }
    public User getLoggedUser(){
        return loggedUser;
    }
}
