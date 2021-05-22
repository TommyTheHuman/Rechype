package it.unipi.dii.inginf.lsmdb.rechype.profile;



import java.util.List;

public interface ProfileService {

    Profile getProfile(String user);

    String createProfile(String user);

    String deleteProfile(String user);

}
