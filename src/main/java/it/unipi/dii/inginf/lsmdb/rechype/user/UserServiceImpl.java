package it.unipi.dii.inginf.lsmdb.rechype.user;

class UserServiceImpl implements UserService {
    private static UserDao userDao=new UserDao();

    public boolean login(String user, String pass){
        return userDao.checkPassword(user, pass);
    }
}
