package lan.classic;
import java.util.*;
/** Deterministic server-side hazards. Bots query the same hazard model used for damage. */
public final class Disasters {
    public enum Kind { FLASH_FLOOD,FIRE,TORNADO,VOLCANIC_ERUPTION,METEOR_SHOWER,THUNDER_STORM,BLIZZARD,SANDSTORM,ACID_RAIN,EARTHQUAKE,TSUNAMI }
    public Kind kind=Kind.FLASH_FLOOD;
    public float age,hazardX,hazardZ,waveZ=-70;private float eventTimer;
    private final Random random;
    public Disasters(long seed){random=new Random(seed);}
    public void begin(World w,int round){
        age=0;eventTimer=0;waveZ=-70;hazardX=hazardZ=0;
        try{kind=w.config.forcedDisaster.length()>0?Kind.valueOf(w.config.forcedDisaster):Kind.values()[random.nextInt(Kind.values().length)];}catch(IllegalArgumentException e){kind=Kind.FLASH_FLOOD;}
        if(kind==Kind.FIRE){for(Part p:w.parts)if(p.material==Part.Material.Wood){p.burning=true;break;}}
    }
    public boolean highGround(){return kind==Kind.FLASH_FLOOD||kind==Kind.TSUNAMI;}
    public boolean shelter(){return kind==Kind.METEOR_SHOWER||kind==Kind.THUNDER_STORM||kind==Kind.BLIZZARD||kind==Kind.SANDSTORM||kind==Kind.ACID_RAIN||kind==Kind.VOLCANIC_ERUPTION;}
    public void tick(World w,float dt){
        age+=dt;eventTimer-=dt;
        w.water=kind==Kind.FLASH_FLOOD?-1+age*.24f:-8;
        hazardX=(float)Math.sin(age*.13f)*28;hazardZ=(float)Math.cos(age*.1f)*24;
        waveZ=-70+age*3;
        if(eventTimer<=0){eventTimer=.65f;
            if(kind==Kind.METEOR_SHOWER||kind==Kind.VOLCANIC_ERUPTION){Projectile p=new Projectile();p.owner=-1;p.x=random.nextFloat()*100-50;p.z=random.nextFloat()*100-50;p.y=55;p.vy=-32;p.life=4;p.color=0xff6028;p.radius=1.4f;w.projectiles.add(p);}
            if(kind==Kind.THUNDER_STORM){Actor target=null;for(Actor a:w.actors.values())if(a.health>0&&!w.covered(a)&&(target==null||a.y>target.y))target=a;if(target!=null&&random.nextFloat()<.3f){target.health-=35;w.explode(target.x,target.y,target.z,5,-1);}}
            if(kind==Kind.FIRE){List<Part> lit=new ArrayList<Part>();for(Part p:w.parts)if(p.burning){for(Part q:w.parts)if(q.material==Part.Material.Wood&&!q.burning&&distance(p.x,p.z,q.x,q.z)<12)lit.add(q);p.color=0x773a20;}for(Part p:lit)p.burning=true;}
            if(kind==Kind.EARTHQUAKE){for(Part p:w.parts)if(p.joint>=0&&p.sy<8&&random.nextFloat()<.08f){p.anchored=false;p.vx=random.nextFloat()*12-6;p.vz=random.nextFloat()*12-6;}}
            if(kind==Kind.SANDSTORM){for(Part p:w.parts)if(!p.anchored)p.vx+=5;}
        }
        for(Actor a:w.actors.values())if(a.health>0){
            boolean exposed=!w.covered(a);
            switch(kind){
                case FLASH_FLOOD:if(a.y-2<w.water)a.health-=25*dt;break;
                case FIRE:for(Part p:w.parts)if(p.burning&&distance(a.x,a.z,p.x,p.z)<Math.max(p.sx,p.sz)/2+2&&Math.abs(a.y-p.y)<p.sy/2+4)a.health-=20*dt;break;
                case TORNADO:float d=distance(a.x,a.z,hazardX,hazardZ);if(d<15){a.vy=25;a.x+=(hazardX-a.x)*dt*.7f;a.z+=(hazardZ-a.z)*dt*.7f;a.health-=12*dt;}break;
                case TSUNAMI:if(Math.abs(a.z-waveZ)<5&&a.y<14){a.health-=80*dt;a.z+=12*dt;}break;
                case ACID_RAIN:if(exposed)a.health-=10*dt;break;
                case BLIZZARD:if(exposed)a.health-=6*dt;break;
                case SANDSTORM:if(exposed){a.health-=3*dt;a.x+=2*dt;}break;
                default:break;
            }
        }
    }
    public float danger(float x,float y,float z,World w){
        if(highGround())return y<12?100-y*4:0;
        if(kind==Kind.TORNADO)return Math.max(0,45-distance(x,z,hazardX,hazardZ))*4;
        if(kind==Kind.FIRE){float result=0;for(Part p:w.parts)if(p.burning)result+=Math.max(0,15-distance(x,z,p.x,p.z));return result;}
        if(shelter())return w.covered(x,y+3,z)?0:80;
        return y*2;
    }
    private static float distance(float x,float z,float X,float Z){return (float)Math.sqrt((x-X)*(x-X)+(z-Z)*(z-Z));}
}
