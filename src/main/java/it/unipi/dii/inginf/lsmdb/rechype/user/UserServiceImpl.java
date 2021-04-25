package it.unipi.dii.inginf.lsmdb.rechype.user;

class UserServiceImpl implements UserService {
    private static UserDao userDao=new UserDao();

    public boolean login(String user, String pass){
        return userDao.checkLogin(user, pass);
    }

    public boolean register(String username, String password, String confPassword, String country, int age){
        return userDao.checkRegistration(username, password, confPassword, country, age);
    }



}
