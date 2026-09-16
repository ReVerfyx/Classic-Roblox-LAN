package lan.classic;
import java.util.*;
/** Shared authoritative 30Hz simulation, independent of rendering and language generation. */
public final class World {
    public final List<Part> parts=new ArrayList<Part>();
    public final LinkedHashMap<Integer,Actor> actors=new LinkedHashMap<Integer,Actor>();
    public final ArrayDeque<String> chat=new ArrayDeque<String>();
    public final Navigation nav=new Navigation();
    private final Map<Integer,List<Integer>> routes=new HashMap<Integer,List<Integer>>();
    public double time;public float water=-8;public String round="LOBBY";public int nextId=1;
    public final ClassicAI social=new ClassicAI();
    public World(){
        Part base=new Part(0,-1,0,128,2,128,0x568647);base.surface=Part.Surface.Studs;parts.add(base);
        parts.add(new Part(0,11.5f,-24,20,1,14,0x96999f));
        for(int x:new int[]{-8,8})for(int z:new int[]{-29,-19})parts.add(new Part(x,5.5f,z,1,11,1,0xb5b6ba));
        Part ladder=new Part(0,6,-16,3,12,0.5f,0x777d86);ladder.ladder=true;ladder.canCollide=false;parts.add(ladder);
        Part spawn=new Part(0,.15f,8,6,.3f,6,0xe2e2e2);spawn.spawn=true;parts.add(spawn);
        parts.add(new Part(-16,1,-4,8,2,5,0xc84c42));parts.add(new Part(18,2,6,8,4,6,0x4269aa));
        int a=nav.add(0,0,4,Navigation.Kind.Ground),b=nav.add(0,0,-14.9f,Navigation.Kind.Ground),c=nav.add(0,12.3f,-14.9f,Navigation.Kind.Ladder),d=nav.add(0,12,-22,Navigation.Kind.Platform);
        nav.edge(a,b);nav.edge(b,a);nav.edge(b,c);nav.edge(c,b);nav.edge(c,d);nav.edge(d,c);
    }
    public synchronized Actor add(String name,boolean bot,String language){Actor a=new Actor(nextId++,name,bot);a.language=language;a.x=(a.id%3-1)*4;a.z=8;actors.put(a.id,a);return a;}
    public synchronized void resetCharacter(int id){Actor a=actors.get(id);if(a!=null&&!a.bot&&a.health>0){a.health=0;a.respawn=0;a.ix=a.iz=0;}}
    public synchronized void remove(int id){actors.remove(id);routes.remove(id);}
    public synchronized void input(int id,float x,float z,boolean jump){Actor a=actors.get(id);if(a==null||a.bot)return;if(Float.isNaN(x)||Float.isNaN(z)||Float.isInfinite(x)||Float.isInfinite(z))return;float len=(float)Math.sqrt(x*x+z*z);a.ix=x/Math.max(1,len);a.iz=z/Math.max(1,len);a.jump|=jump;}
    public synchronized void message(int id,String text){Actor a=actors.get(id);if(a==null)return;text=text.replace('\n',' ').replace('\r',' ').trim();if(text.length()>160)text=text.substring(0,160);if(text.length()==0)return;say(a.name,text);social.respond(this,a,text);}
    public void say(String name,String text){if(chat.size()>=8)chat.removeFirst();chat.add(name+": "+text);}
    public synchronized void tick(float dt){
        time+=dt;float cycle=(float)(time%90);String old=round;
        round=cycle<15?"LOBBY":cycle<20?"PREPARE":cycle<70?"FLASH FLOOD":cycle<80?"SURVIVORS":"RETURN";
        water=cycle>=20&&cycle<70?-1+(cycle-20)*.23f:-8;
        if(!round.equals(old)){
            if(round.equals("SURVIVORS")){String s="";for(Actor a:actors.values())if(a.health>0)s+=a.name+" ";say("Survivors",s);}
            if(round.equals("RETURN"))for(Actor a:actors.values())respawn(a);
            if(round.equals("FLASH FLOOD"))for(Actor a:actors.values())if(a.bot){a.goal="FIND_HIGHER_POSITION";routes.remove(a.id);social.event(this,a,"up");}
        }
        for(Actor a:actors.values()){
            if(a.health<=0){a.state=Actor.State.DEATH;a.respawn+=dt;if(a.respawn>4&&!round.equals("FLASH FLOOD"))respawn(a);continue;}
            if(a.bot)bot(a);
            move(a,dt);a.phase+=dt*(Math.abs(a.vx)+Math.abs(a.vz)>1?9:1);
            if(a.y-2<water){a.health-=25*dt;}if(a.y<-40)a.health=0;
        }
    }
    private void respawn(Actor a){a.x=(a.id%3-1)*4;a.y=3.4f;a.z=8;a.vy=0;a.health=100;a.respawn=0;a.goal="EXPLORE";routes.remove(a.id);}
    private void bot(Actor a){
        if(a.followId>=0&& !round.equals("FLASH FLOOD")){
            Actor t=actors.get(a.followId);if(t!=null&&Math.abs(t.y-a.y)<4){steer(a,t.x,t.z,3);return;}
        }
        List<Integer> route=routes.get(a.id);
        if(route==null){int start=nav.nearest(a.x,a.y-3,a.z);int goal=3;route=nav.path(start,goal);routes.put(a.id,route);a.routeIndex=0;}
        if(a.routeIndex>=route.size()){a.ix=a.iz=0;return;}
        Navigation.Node n=nav.nodes.get(route.get(a.routeIndex));
        if(n.kind==Navigation.Kind.Ladder&&a.y-3<n.y-.3f){steer(a,n.x,n.z,.1f);a.iz=-1;return;}
        if(Math.abs(a.x-n.x)<1&&Math.abs(a.z-n.z)<1&&Math.abs((a.y-3)-n.y)<2){a.routeIndex++;a.ix=a.iz=0;return;}
        steer(a,n.x,n.z,.2f);if(n.kind==Navigation.Kind.Jump)a.jump=true;
    }
    private void steer(Actor a,float x,float z,float stop){float dx=x-a.x,dz=z-a.z,d=(float)Math.sqrt(dx*dx+dz*dz);if(d<stop){a.ix=a.iz=0;}else{a.ix=dx/d;a.iz=dz/d;}}
    private void move(Actor a,float dt){
        a.vx=a.ix*16;a.vz=a.iz*16;if(Math.abs(a.ix)+Math.abs(a.iz)>.05f)a.yaw=(float)Math.toDegrees(Math.atan2(-a.ix,-a.iz));
        boolean climb=false;for(Part p:parts)if(p.ladder&&Math.abs(a.x-p.x)<p.sx/2+.7f&&Math.abs(a.z-p.z)<1.8f&&a.y-3<p.y+p.sy/2+.6f&&a.y+2>p.y-p.sy/2)climb=true;
        if(climb&&Math.abs(a.iz)>.1f){a.vy=-a.iz*9;a.state=Actor.State.CLIMB;a.vz=0;}
        else{if(a.jump&&a.grounded){a.vy=50;a.grounded=false;}a.vy-=196.2f*dt;a.state=a.vy>1?Actor.State.JUMP:a.grounded?Actor.State.IDLE:Actor.State.FALL;}
        a.jump=false;
        float oldX=a.x;a.x+=a.vx*dt;if(blocked(a))a.x=oldX;
        float oldZ=a.z;a.z+=a.vz*dt;if(blocked(a))a.z=oldZ;
        float oldY=a.y;a.y+=a.vy*dt;a.grounded=false;
        for(Part p:parts)if(p.canCollide&&Math.abs(a.x-p.x)<p.sx/2+.85f&&Math.abs(a.z-p.z)<p.sz/2+.45f){
            float top=p.y+p.sy/2,bottom=p.y-p.sy/2;
            if(a.vy<=0&&oldY-3>=top-.15f&&a.y-3<=top){a.y=top+3;a.vy=0;a.grounded=true;if(p.kill)a.health=0;}
            else if(a.vy>0&&oldY+2<=bottom&&a.y+2>=bottom){a.y=bottom-2;a.vy=0;}
        }
        if(a.grounded)a.state=Math.abs(a.vx)+Math.abs(a.vz)>1?Actor.State.WALK:Actor.State.IDLE;
    }
    private boolean blocked(Actor a){for(Part p:parts)if(p.canCollide&&a.y-3<p.y+p.sy/2-.15f&&a.y+2>p.y-p.sy/2+.1f&&Math.abs(a.x-p.x)<p.sx/2+.85f&&Math.abs(a.z-p.z)<p.sz/2+.45f)return true;return false;}
}
