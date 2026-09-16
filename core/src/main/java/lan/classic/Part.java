package lan.classic;
/** Stud units; center position. No voxel grid. */
public final class Part {
    public enum Shape { Block, Ball, Cylinder, Wedge }
    public enum Material { Plastic, SmoothPlastic, Brick, Concrete, Wood, Metal, Grass, Sand, Slate }
    public enum Surface { Studs, Inlets, Smooth }
    public float x,y,z,sx,sy,sz,rx,ry,rz,transparency,vx,vy,vz;
    public int color;
    public boolean anchored=true,canCollide=true,ladder,kill,spawn;
    public Shape shape=Shape.Block;
    public Material material=Material.Plastic;
    public Surface surface=Surface.Smooth;
    public Part(float x,float y,float z,float sx,float sy,float sz,int color) {
        this.x=x;this.y=y;this.z=z;this.sx=sx;this.sy=sy;this.sz=sz;this.color=color;
    }
}
