package lan.classic;
/** Stud units; center position. No voxel grid. */
public final class Part {
    public enum Shape { Block, Ball, Cylinder, Wedge }
    public enum Material { Plastic, SmoothPlastic, Brick, Concrete, Wood, Metal, Grass, Sand, Slate }
    public enum Surface { Studs, Inlets, Smooth }
    public int joint=-1;
    public boolean seat,moving,burning;
    public float originX,originY,originZ;
    public float x,y,z,sx,sy,sz,rx,ry,rz,transparency,vx,vy,vz;
    public int color;
    public boolean anchored=true,canCollide=true,ladder,kill,spawn;
    public Shape shape=Shape.Block;
    public Material material=Material.Plastic;
    public Surface surface=Surface.Smooth;
    public Part(float x,float y,float z,float sx,float sy,float sz,int color) {
        this.x=x;this.y=y;this.z=z;this.sx=sx;this.sy=sy;this.sz=sz;this.color=color;
    }
    public Part copy(){Part p=new Part(x,y,z,sx,sy,sz,color);p.rx=rx;p.ry=ry;p.rz=rz;p.transparency=transparency;p.vx=vx;p.vy=vy;p.vz=vz;p.anchored=anchored;p.canCollide=canCollide;p.ladder=ladder;p.kill=kill;p.spawn=spawn;p.seat=seat;p.moving=moving;p.burning=burning;p.joint=joint;p.originX=originX;p.originY=originY;p.originZ=originZ;p.shape=shape;p.material=material;p.surface=surface;return p;}
}
