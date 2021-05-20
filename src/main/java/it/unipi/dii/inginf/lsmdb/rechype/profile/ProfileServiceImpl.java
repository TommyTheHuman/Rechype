package it.unipi.dii.inginf.lsmdb.rechype.profile;

import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileDAO;

class ProfileServiceImpl implements ProfileService {
    private static ProfileDAO profileDAO=new ProfileDAO();

    public Profile getProfile(String user){
        return profileDAO.getProfileByUsername(user);
    }

    public String createProfile(String username){
        if(profileDAO.insertProfile(username)){
            return "ProfileOk";
        }else{
            return "Abort";
        }
    }

    public String deleteProfile(String username){
        if(profileDAO.deleteProfile(username)){
            return "DeleteOK";
        }else{
            return "Abort";
        }
    }

}
