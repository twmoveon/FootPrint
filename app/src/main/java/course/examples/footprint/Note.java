package course.examples.footprint;

import android.location.Location;

import java.io.Serializable;
import java.util.Map;



public class Note implements Serializable{
    public String title;
    public String content;
    public String userid;
    public Long timestamp;
    public Double lon;
    public Double lat;
    public String address;
   // public String noteid;
    public String nickname;

    public Note(Map<String, Object> datamap){
        title = (String) datamap.get("title");
        content = (String) datamap.get("content");
        userid = (String) datamap.get("userid");
        timestamp = (Long) datamap.get("timestamp");
        lon = (Double) datamap.get("lon");
        lat = (Double) datamap.get("lat");
        address = (String) datamap.get("address");
       // noteid = (String) datamap.get("noteid");
        nickname = (String) datamap.get("nickname");
    }

    public Note(){}

    public Note(String newTitle, String newContent, String newUserid, Long newTimestamp, Double newLon, Double newLat, String newAddress, String newNickname){
        title = newTitle;
        content = newContent;
        userid = newUserid;
        timestamp = newTimestamp;
        lon = newLon;
        lat = newLat;
        address = newAddress;
        nickname = newNickname;
    }

    public Note(String newTitle, String newContent, String newUserid, Long newTimestamp, Location location, String newAddress, String newNickname){
        title = newTitle;
        content = newContent;
        userid = newUserid;
        timestamp = newTimestamp;
        lon = location.getLongitude();
        lat = location.getAltitude();
        address = newAddress;
        nickname = newNickname;
    }

}
