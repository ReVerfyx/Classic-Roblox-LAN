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
    public final GameConfig config;
    public final Disasters disasters;
    public final List<Projectile> projectiles=new ArrayList<Projectile>();
    public String mapName="Classic Baseplate";
    public int mapRevision=1,roundNumber;
    public boolean active;
    private final List<Part> originals=new ArrayList<Part>();
    private int lastCycle=-1;
    private float spawnX,spawnY=3.4f,spawnZ=8;

    public World(){this(new GameConfig());}
    public World(GameConfig config){this.config=config;disasters=new Disasters(config.seed);social.enabled=config.chat;
        Part base=new Part(0,-1,0,128,2,128,0x568647);base.surface=Part.Surface.Studs;parts.add(base);
        parts.add(new Part(0,11.5f,-24,20,1,14,0x96999f));
        for(int x:new int[]{-8,8})for(int z:new int[]{-29,-19})parts.add(new Part(x,5.5f,z,1,11,1,0xb5b6ba));
        Part ladder=new Part(0,6,-16,3,12,0.5f,0x777d86);ladder.ladder=true;ladder.canCollide=false;parts.add(ladder);
        Part spawn=new Part(0,.15f,8,6,.3f,6,0xe2e2e2);spawn.spawn=true;parts.add(spawn);
        parts.add(new Part(-16,1,-4,8,2,5,0xc84c42));parts.add(new Part(18,2,6,8,4,6,0x4269aa));
        int a=nav.add(0,0,4,Navigation.Kind.Ground),b=nav.add(0,0,-14.9f,Navigation.Kind.Ground),c=nav.add(0,12.3f,-14.9f,Navigation.Kind.Ladder),d=nav.add(0,12,-22,Navigation.Kind.Platform);
        nav.edge(a,b);nav.edge(b,a);nav.edge(b,c);nav.edge(c,b);nav.edge(c,d);nav.edge(d,c);
        int cover=nav.add(12,0,-4,Navigation.Kind.Ground);nav.edge(a,cover);nav.edge(cover,a);
        int far=nav.add(-32,0,24,Navigation.Kind.Ground);nav.edge(a,far);nav.edge(far,a);
        parts.add(new Part(12,7,-4,12,1,12,0x985c31));
        for(int xx:new int[]{7,17}){Part post=new Part(xx,3.5f,-8,1,7,1,0x985c31);post.material=Part.Material.Wood;post.joint=1;parts.add(post);}
        for(int i=0;i<6;i++){Part brick=new Part(-24+(i%3)*3,1+(i/3)*2,0,3,2,2,0xb84a3c);brick.material=Part.Material.Brick;brick.joint=2;parts.add(brick);}
        Part seat=new Part(8,1,14,3,2,3,0x735130);seat.seat=true;parts.add(seat);
        Part moving=new Part(-12,2,18,6,1,5,0x386aa0);moving.moving=true;parts.add(moving);
        for(Part part:parts){part.originX=part.x;part.originY=part.y;part.originZ=part.z;originals.add(part.copy());}

    }
    public synchronized Actor add(String name,boolean bot,String language){Actor a=new Actor(nextId++,name,bot);a.language=language;a.x=spawnX+(a.id%3-1)*4;a.y=spawnY;a.z=spawnZ;
        if(bot){a.appearance.colors[1]=new int[]{0x0d69ac,0xc4281c,0x4b974b,0x6b327c}[a.id%4];a.appearance.hat=a.id%4;a.personality=new String[]{"Friendly","Quiet","Competitive","Helpful"}[a.id%4];}
        actors.put(a.id,a);return a;}
    public synchronized void resetCharacter(int id){Actor a=actors.get(id);if(a!=null&&!a.bot&&a.health>0){a.health=0;a.respawn=0;a.ix=a.iz=0;}}
    public synchronized void remove(int id){actors.remove(id);routes.remove(id);}
    public synchronized void input(int id,float x,float z,boolean jump){Actor a=actors.get(id);if(a==null||a.bot)return;if(Float.isNaN(x)||Float.isNaN(z)||Float.isInfinite(x)||Float.isInfinite(z))return;float len=(float)Math.sqrt(x*x+z*z);a.ix=x/Math.max(1,len);a.iz=z/Math.max(1,len);a.jump|=jump;}
    public synchronized void message(int id,String text){Actor a=actors.get(id);if(a==null)return;text=text.replace('\n',' ').replace('\r',' ').trim();if(text.length()>160)text=text.substring(0,160);if(text.length()==0)return;say(a.name,text);social.respond(this,a,text);}
    public void say(String name,String text){if(chat.size()>=8)chat.removeFirst();chat.add(name+": "+text);}
    public synchronized void loadPlace(Place place){
        parts.clear();originals.clear();for(Part p:place.parts){parts.add(p.copy());originals.add(p.copy());}
        nav.nodes.clear();nav.nodes.addAll(place.navigation.nodes);mapName=place.name;mapRevision++;
        spawnX=0;spawnY=8;spawnZ=0;for(Part p:parts)if(p.spawn){spawnX=p.x;spawnY=p.y+p.sy/2+3.1f;spawnZ=p.z;break;}
        routes.clear();for(Actor a:actors.values())respawn(a);
        if(config.trustedScripts){
            for(LuaSandbox.Result result:LuaSandbox.runPlace(this,place,config.luaInstructionBudget))
                if(!result.success)say("Script","disabled: "+result.error);
        }
    }
    public synchronized void tick(float dt){
        time+=dt;float cycle=(float)(time%90);String old=round;
        if(config.mode==GameConfig.Mode.DISASTERS){
            int index=(int)(time/90);if(index!=lastCycle){lastCycle=index;roundNumber=index;}
            active=cycle>=20&&cycle<70;
            round=cycle<15?"LOBBY":cycle<20?"PREPARE":cycle<70?disasters.kind.name():cycle<80?"SURVIVORS":"RETURN";
            if(active&&!wasActive){disasters.begin(this,roundNumber);round=disasters.kind.name();routes.clear();for(Actor a:actors.values())if(a.bot){a.goal=disasters.highGround()?"FIND_HIGHER_POSITION":disasters.shelter()?"FIND_SHELTER":"AVOID_DANGER";social.event(this,a,disasters.highGround()?"up":"safe");}}
            if(active)disasters.tick(this,dt);else water=-8;
            if(!round.equals(old)&&round.equals("SURVIVORS")){StringBuilder names=new StringBuilder();for(Actor a:actors.values())if(a.health>0){a.wins++;names.append(a.name).append(' ');}say("Survivors",names.toString());}
            if(!round.equals(old)&&round.equals("RETURN")){parts.clear();for(Part p:originals)parts.add(p.copy());projectiles.clear();mapRevision++;for(Actor a:actors.values())respawn(a);}
            wasActive=active;
        }else{active=false;water=-8;round=config.mode.name();}
        physicsParts(dt);tickProjectiles(dt);
        for(Actor a:actors.values()){
            a.cooldown=Math.max(0,a.cooldown-dt);
            if(a.health<=0){if(!a.deathCounted){a.deaths++;a.deathCounted=true;}a.state=Actor.State.DEATH;a.respawn+=dt;if(a.respawn>4&&!active)respawn(a);continue;}
            if(a.bot){a.brainTimer-=dt;if(a.brainTimer<=0){a.brainTimer=1f/Math.max(2,config.aiHz);bot(a);}}
            move(a,dt);a.phase+=dt*(Math.abs(a.vx)+Math.abs(a.vz)>1?9:1);
            if(a.y<-40)a.health=0;
        }
    }
    private boolean wasActive;
    private void respawn(Actor a){a.x=spawnX+(a.id%3-1)*4;a.y=spawnY;a.z=spawnZ;a.vy=0;a.health=100;a.respawn=0;a.deathCounted=false;a.goal="EXPLORE";routes.remove(a.id);}
    public boolean covered(Actor a){return covered(a.x,a.y,a.z);}
    public boolean covered(float x,float y,float z){for(Part p:parts)if(p.canCollide&&p.transparency<.8f&&p.y-p.sy/2>y+1&&Math.abs(x-p.x)<p.sx/2&&Math.abs(z-p.z)<p.sz/2)return true;return false;}
    public synchronized void useTool(int id,int tool){
        Actor a=actors.get(id);if(a==null||a.health<=0||a.cooldown>0)return;a.tool=Math.max(0,Math.min(2,tool));
        if(tool==0){for(Part p:parts)if(p.seat&&Math.hypot(a.x-p.x,a.z-p.z)<5){a.x=p.x;a.z=p.z;a.y=p.y+p.sy/2+2;a.state=Actor.State.SEAT;break;}return;}
        a.cooldown=tool==1?.6f:1.6f;
        if(tool==1){for(Actor other:actors.values())if(other.id!=a.id&&other.health>0&&Math.hypot(other.x-a.x,other.z-a.z)<6&&Math.abs(other.y-a.y)<5){other.health-=25;if(other.health<=0)a.kills++;}}
        else if(projectiles.size()<64){Projectile p=new Projectile();p.owner=id;p.x=a.x;p.y=a.y;p.z=a.z;double r=Math.toRadians(a.yaw);p.vx=-(float)Math.sin(r)*45;p.vz=-(float)Math.cos(r)*45;p.x+=p.vx*.08f;p.z+=p.vz*.08f;projectiles.add(p);}
    }
    public void explode(float x,float y,float z,float radius,int owner){
        Set<Integer> broken=new HashSet<Integer>();for(Part p:parts)if(p.joint>=0&&Math.hypot(p.x-x,p.z-z)<radius&&Math.abs(p.y-y)<radius)broken.add(p.joint);
        for(Part p:parts)if(broken.contains(p.joint)){p.anchored=false;p.vx=(p.x-x)*2;p.vy=18;p.vz=(p.z-z)*2;}
        for(Actor a:actors.values())if(a.health>0){float d=(float)Math.sqrt((a.x-x)*(a.x-x)+(a.y-y)*(a.y-y)+(a.z-z)*(a.z-z));if(d<radius){a.health-=90*(1-d/radius);a.vy=20;if(a.health<=0&&owner!=a.id&&actors.containsKey(owner))actors.get(owner).kills++;}}
    }
    private void tickProjectiles(float dt){Iterator<Projectile> it=projectiles.iterator();while(it.hasNext()){Projectile p=it.next();p.life-=dt;p.x+=p.vx*dt;p.y+=p.vy*dt;p.z+=p.vz*dt;boolean hit=p.life<=0;for(Part q:parts)if(q.canCollide&&Math.abs(p.x-q.x)<q.sx/2&&Math.abs(p.y-q.y)<q.sy/2&&Math.abs(p.z-q.z)<q.sz/2){hit=true;break;}for(Actor a:actors.values())if(a.id!=p.owner&&a.health>0&&Math.hypot(a.x-p.x,a.z-p.z)<2&&Math.abs(a.y-p.y)<3)hit=true;if(hit){explode(p.x,p.y,p.z,10,p.owner);it.remove();}}}
    private void physicsParts(float dt){for(Part p:parts){
        if(p.moving&&p.anchored){float nx=p.originX+(float)Math.sin(time*.7)*8,delta=nx-p.x;for(Actor a:actors.values())if(a.grounded&&Math.abs(a.y-3-(p.y+p.sy/2))<.2f&&Math.abs(a.x-p.x)<p.sx/2+1&&Math.abs(a.z-p.z)<p.sz/2+.5f)a.x+=delta;p.x=nx;}
        if(p.anchored)continue;p.vy-=196.2f*dt;float before=p.y;p.x+=p.vx*dt;p.y+=p.vy*dt;p.z+=p.vz*dt;
        for(Part q:parts)if(q!=p&&q.anchored&&q.canCollide&&Math.abs(p.x-q.x)<(p.sx+q.sx)/2&&Math.abs(p.z-q.z)<(p.sz+q.sz)/2){float top=q.y+q.sy/2;if(before-p.sy/2>=top-.1f&&p.y-p.sy/2<=top){p.y=top+p.sy/2;p.vy=0;p.vx*=.9f;p.vz*=.9f;}}
        if(p.y<-80){p.canCollide=false;p.transparency=1;p.vy=0;}
    }}
    private void bot(Actor a){
        if(config.mode==GameConfig.Mode.SWORD_FIGHT||config.mode==GameConfig.Mode.ROCKET_ARENA){Actor enemy=null;double dist=Double.MAX_VALUE;for(Actor b:actors.values())if(b.id!=a.id&&b.health>0){double d=Math.hypot(a.x-b.x,a.z-b.z);if(d<dist){dist=d;enemy=b;}}if(enemy!=null){steer(a,enemy.x,enemy.z,config.mode==GameConfig.Mode.SWORD_FIGHT?3:12);a.yaw=(float)Math.toDegrees(Math.atan2(a.x-enemy.x,a.z-enemy.z));if(dist<(config.mode==GameConfig.Mode.SWORD_FIGHT?6:55))useTool(a.id,config.mode==GameConfig.Mode.SWORD_FIGHT?1:2);if(config.difficulty==2&&a.grounded&&((int)(time*3)+a.id)%11==0)a.jump=true;return;}}
        if(a.followId>=0&&!active){Actor t=actors.get(a.followId);if(t!=null&&Math.abs(t.y-a.y)<4){steer(a,t.x,t.z,3);return;}}
        if(nav.nodes.isEmpty()){a.ix=a.iz=0;return;}
        int goal=0;float best=Float.MAX_VALUE;
        // The authored Baseplate has a recognizable lobby route: walk to the
        // ladder, climb it, then reach the upper platform.  Keeping this route
        // stable makes bots visibly play the place instead of wandering.
        if(!active&&a.goal.equals("EXPLORE")&&nav.nodes.size()>=4) goal=3;
        else for(int i=0;i<nav.nodes.size();i++){Navigation.Node n=nav.nodes.get(i);float score=active?disasters.danger(n.x,n.y,n.z,this):a.goal.equals("FIND_HIGHER_POSITION")?-n.y:Math.abs(i-((int)(time/8)+a.id)%nav.nodes.size());score+=(float)Math.hypot(n.x-a.x,n.z-a.z)*.015f;if(score<best){best=score;goal=i;}}
        List<Integer> route=routes.get(a.id);
        if(route==null||route.isEmpty()||route.get(route.size()-1)!=goal){int start=nav.nearest(a.x,a.y-3,a.z);route=nav.path(start,goal);routes.put(a.id,route);a.routeIndex=0;}
        if(a.routeIndex>=route.size()){a.ix=a.iz=0;return;}
        Navigation.Node n=nav.nodes.get(route.get(a.routeIndex));
        if(n.kind==Navigation.Kind.Ladder&&a.y-3<n.y-.3f){steer(a,n.x,n.z,.1f);a.iz=-1;return;}
        if(Math.abs(a.x-n.x)<1&&Math.abs(a.z-n.z)<1&&Math.abs((a.y-3)-n.y)<2){a.routeIndex++;a.ix=a.iz=0;return;}
        steer(a,n.x,n.z,.2f);if(n.kind==Navigation.Kind.Jump||(n.y>a.y-2&&a.grounded))a.jump=true;
    }
    private void steer(Actor a,float x,float z,float stop){float dx=x-a.x,dz=z-a.z,d=(float)Math.sqrt(dx*dx+dz*dz);if(d<stop){a.ix=a.iz=0;}else{a.ix=dx/d;a.iz=dz/d;}}
    private void move(Actor a,float dt){
        if(a.state==Actor.State.SEAT&&!a.jump&&Math.abs(a.ix)+Math.abs(a.iz)<.1f)return;
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
