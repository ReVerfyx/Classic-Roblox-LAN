package lan.classic;

import java.util.*;

/** Names and provenance policy for classic places. Original assets are never downloaded. */
public final class ClassicPlaces {
    public static final class Entry {
        public final String name; public final boolean bundled;
        Entry(String n,boolean b){name=n;bundled=b;}
    }
    private ClassicPlaces(){}
    public static List<Entry> catalog(){
        List<Entry> out=new ArrayList<Entry>();
        out.add(new Entry("Classic Baseplate",true));
        String[] imported={"Natural Disaster Survival","Crossroads","Happy Home in Robloxia","Chaos Canyon","Glass Houses","Rocket Arena","Sword Fight on the Heights","Work at a Pizza Place"};
        for(String name:imported)out.add(new Entry(name,false));
        return Collections.unmodifiableList(out);
    }
    public static boolean ordinaryUsersMayCreate(){return false;}
}
