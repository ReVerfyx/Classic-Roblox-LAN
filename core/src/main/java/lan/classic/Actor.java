package lan.classic;
import java.util.ArrayDeque;
public final class Actor {
    public enum State { IDLE, WALK, JUMP, FALL, CLIMB, DEATH }
    public final int id;
    public final String name;
    public boolean bot,grounded,jump;
    public float x,y=3,z,vx,vy,vz,yaw,ix,iz,health=100,respawn,phase;
    public String language="en",goal="EXPLORE",personality="Helpful";
    public int routeIndex,followId=-1;
    public double nextChat;
    public State state=State.IDLE;
    public final ArrayDeque<String> memory=new ArrayDeque<String>();
    public Actor(int id,String name,boolean bot){this.id=id;this.name=name;this.bot=bot;}
    public void remember(String s){if(memory.size()>=6)memory.removeFirst();memory.addLast(s);}
}
