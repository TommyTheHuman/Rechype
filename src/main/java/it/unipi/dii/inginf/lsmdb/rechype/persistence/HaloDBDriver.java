package it.unipi.dii.inginf.lsmdb.rechype.persistence;

import com.oath.halodb.*;
import org.apache.logging.log4j.LogManager;

import java.nio.charset.StandardCharsets;


public class HaloDBDriver {
    private static final HaloDBDriver obj = new HaloDBDriver();
    private HaloDB client;
    private String path="./src/main/resources/it/unipi/dii/inginf/lsmdb/rechype/cache/";

    private HaloDBDriver(){
        HaloDBOptions options= new HaloDBOptions();
        options.setCleanUpTombstonesDuringOpen(true);
        options.setCleanUpInMemoryIndexOnClose(true);
        try {
            client=HaloDB.open(path, options);
        }catch(HaloDBException ex){
            LogManager.getLogger("HaloDBDriver.class").fatal("HaloDB: cache DB not set");
            System.exit(-1);
        }
    }

    public static HaloDBDriver getObject(){
        return obj;
    }

    public void addData(String type, byte[] _id, byte[] obj) throws HaloDBException{
        String key=type+":"+new String(_id)+":JSONObject";
        client.put(key.getBytes(StandardCharsets.UTF_8), obj);
    }

    public byte[] getData(String type, byte[] _id) throws HaloDBException{
        String key=type+":"+new String(_id)+":JSONObject";
        byte[] result=client.get(key.getBytes(StandardCharsets.UTF_8));
        return result;
    }

    public void closeConnection(){
            try {
                HaloDBIterator iterator = client.newIterator();
                //flushing the cache
                while (iterator.hasNext()) {
                    Record record = iterator.next();
                    client.delete(record.getKey());
                }
                HaloDBStats stats = client.stats();
                System.out.println(stats.toString());
                client.close();
            }catch(HaloDBException ex){
                LogManager.getLogger("HaloDBDriver.class").fatal("key value database not closed");
                System.exit(-1);
            }
    }

}

