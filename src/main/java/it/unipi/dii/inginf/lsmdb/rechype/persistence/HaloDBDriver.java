package it.unipi.dii.inginf.lsmdb.rechype.persistence;

import com.oath.halodb.*;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;

public class HaloDBDriver {
    private static final HaloDBDriver obj = new HaloDBDriver();
    private HashMap<String, HaloDB> clients;
    private String DBsPath="./src/main/resources/it/unipi/dii/inginf/lsmdb/rechype/cache/";

    private HaloDBDriver(){
        HaloDBOptions options= new HaloDBOptions();
        clients=new HashMap<>();
        try {
            clients.put("users", HaloDB.open(DBsPath+"users", options));
            clients.put("recipes", HaloDB.open(DBsPath+"recipes", options));
            clients.put("ingredients", HaloDB.open(DBsPath+"ingredients", options));
        }catch(HaloDBException ex){
            LogManager.getLogger("HaloDBDriver.class").fatal("HaloDB: cache DB not set");
            System.exit(-1);
        }
    }

    public static HaloDBDriver getObject(){
        return obj;
    }

    public HaloDB getClient(String db){
        return clients.get(db);
    }

    public void closeConnection(){
        //flushing all the dbs and close connection
        clients.forEach((String value, HaloDB db)->{
            try {
                HaloDBIterator iterator = db.newIterator();
                while (iterator.hasNext()) {
                    Record record = iterator.next();
                    db.delete(record.getKey());
                }
                db.close();
            }catch(HaloDBException ex){
                LogManager.getLogger("HaloDBDriver.class").fatal("key value database not closed");
                System.exit(-1);
            }
        });
    }
}

