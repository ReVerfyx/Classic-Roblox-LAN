package lan.classic;
public final class Projectile {
    public int owner,color=0xc4c4c4;public float x,y,z,vx,vy,vz,life=3,radius=1;
    public Projectile copy(){Projectile p=new Projectile();p.owner=owner;p.color=color;p.x=x;p.y=y;p.z=z;p.vx=vx;p.vy=vy;p.vz=vz;p.life=life;p.radius=radius;return p;}
}
