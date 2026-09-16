package lan.classic.android;
import android.content.Context;
import android.opengl.*;
import android.view.MotionEvent;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.*;
import lan.classic.*;
/** GLES 1.1 fixed-function renderer. Geometry is cached; no texture or native model memory. */
final class GameView extends GLSurfaceView implements GLSurfaceView.Renderer {
    interface Session {Net.Snapshot snapshot();void input(float x,float z,boolean jump);}
    private final Session session;private final World level=new World();
    private FloatBuffer cube,cylinder;private int cylinderCount;
    volatile float cameraYaw=0,pitch=24;volatile int fps=30;volatile boolean studs=true;
    private static final float[] SHADES={.84f,.72f,.78f,.9f,1,.65f};
    private long lastFrame;private float lastX,lastY;private int cameraPointer=-1;
    GameView(Context c,Session s){super(c);session=s;setEGLConfigChooser(5,6,5,0,16,0);setRenderer(this);setPreserveEGLContextOnPause(true);buildMeshes();}
    private FloatBuffer buffer(float[] v){FloatBuffer b=ByteBuffer.allocateDirect(v.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();b.put(v).position(0);return b;}
    private void buildMeshes(){
        float[] v={-.5f,-.5f,-.5f,.5f,-.5f,-.5f,.5f,.5f,-.5f,-.5f,.5f,-.5f,-.5f,-.5f,.5f,.5f,-.5f,.5f,.5f,.5f,.5f,-.5f,.5f,.5f};
        int[] ix={4,5,6,4,6,7,1,0,3,1,3,2,0,4,7,0,7,3,5,1,2,5,2,6,3,7,6,3,6,2,0,1,5,0,5,4};float[] out=new float[ix.length*3];for(int i=0;i<ix.length;i++)for(int j=0;j<3;j++)out[i*3+j]=v[ix[i]*3+j];cube=buffer(out);
        float[] c=new float[16*12*3];int n=0;for(int i=0;i<16;i++){double a=i*Math.PI/8,b=(i+1)*Math.PI/8;float x=(float)Math.cos(a)*.5f,z=(float)Math.sin(a)*.5f,X=(float)Math.cos(b)*.5f,Z=(float)Math.sin(b)*.5f;float[] f={x,-.5f,z,X,-.5f,Z,X,.5f,Z,x,-.5f,z,X,.5f,Z,x,.5f,z,0,.5f,0,x,.5f,z,X,.5f,Z,0,-.5f,0,X,-.5f,Z,x,-.5f,z};for(float q:f)c[n++]=q;}cylinder=buffer(c);cylinderCount=n/3;
    }
    public void onSurfaceCreated(GL10 g,EGLConfig c){g.glClearColor(.55f,.73f,.92f,1);g.glEnable(GL10.GL_DEPTH_TEST);g.glEnableClientState(GL10.GL_VERTEX_ARRAY);g.glDisable(GL10.GL_DITHER);}
    public void onSurfaceChanged(GL10 g,int w,int h){g.glViewport(0,0,w,h);g.glMatrixMode(GL10.GL_PROJECTION);g.glLoadIdentity();GLU.gluPerspective(g,65,(float)w/Math.max(1,h),.3f,180);g.glMatrixMode(GL10.GL_MODELVIEW);}
    public void onDrawFrame(GL10 g){
        long now=System.nanoTime(),wait=1000000000L/fps-(now-lastFrame);if(wait>0)try{Thread.sleep(wait/1000000,(int)(wait%1000000));}catch(InterruptedException ignored){}lastFrame=System.nanoTime();
        Net.Snapshot s=session.snapshot();Actor me=null;for(Actor a:s.actors)if(a.id==s.you)me=a;
        float x=me==null?0:me.x,y=me==null?3:me.y,z=me==null?8:me.z;
        g.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);g.glLoadIdentity();double angle=Math.toRadians(cameraYaw),elevation=Math.toRadians(pitch);float distance=20;
        GLU.gluLookAt(g,x+(float)Math.sin(angle)*distance,y+3+(float)Math.sin(elevation)*distance,z+(float)Math.cos(angle)*distance,x,y+1,z,0,1,0);
        for(Part p:level.parts){if(p.ladder){box(g,p.x-1.2f,p.y,p.z,.25f,p.sy,.3f,p.color);box(g,p.x+1.2f,p.y,p.z,.25f,p.sy,.3f,p.color);for(int i=1;i<12;i++)box(g,p.x,i,p.z,2.6f,.2f,.4f,p.color);}else box(g,p.x,p.y,p.z,p.sx,p.sy,p.sz,p.color);}
        if(studs){int ox=(int)(x/4)*4,oz=(int)(z/4)*4;for(int xx=ox-20;xx<=ox+20;xx+=4)for(int zz=oz-20;zz<=oz+20;zz+=4)if(Math.abs(xx)<63&&Math.abs(zz)<63)cyl(g,xx,.1f,zz,.7f,.2f,.7f,0x63994f);}
        // Spawn star, built from parts, not a downloaded decal.
        box(g,0,.32f,8,4,.025f,.7f,0x252525);box(g,0,.32f,8,.7f,.025f,4,0x252525);
        for(Actor a:s.actors)drawActor(g,a);
        if(s.water>-2){g.glEnable(GL10.GL_BLEND);g.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);g.glColor4f(.1f,.4f,.85f,.52f);g.glPushMatrix();g.glTranslatef(0,s.water-1,0);g.glScalef(128,2,128);mesh(g,cube,36);g.glPopMatrix();g.glDisable(GL10.GL_BLEND);}
    }
    private void drawActor(GL10 g,Actor a){g.glPushMatrix();g.glTranslatef(a.x,a.y,a.z);g.glRotatef(a.yaw,0,1,0);if(a.state==Actor.State.DEATH)g.glRotatef(85,0,0,1);
        box(g,0,0,0,2,2,1,0x0d69ac);cyl(g,0,1.5f,0,1.25f,1,1.25f,0xf5cd30);
        // Face points towards local -Z; two eyes and a segmented smile.
        box(g,-.23f,1.64f,-.607f,.09f,.12f,.025f,0x151515);box(g,.23f,1.64f,-.607f,.09f,.12f,.025f,0x151515);
        box(g,0,1.27f,-.623f,.36f,.045f,.02f,0x151515);box(g,-.2f,1.32f,-.603f,.055f,.10f,.02f,0x151515);box(g,.2f,1.32f,-.603f,.055f,.10f,.02f,0x151515);
        float swing=a.state==Actor.State.WALK?(float)Math.sin(a.phase)*32:0;
        if(a.state==Actor.State.CLIMB)swing=(float)Math.sin(a.phase*4)*35;
        float arm=a.state==Actor.State.JUMP||a.state==Actor.State.FALL?155:a.state==Actor.State.CLIMB?135:0;
        limb(g,-1.5f,.8f,0,arm+swing,0xf5cd30);limb(g,1.5f,.8f,0,arm-swing,0xf5cd30);
        limb(g,-.5f,-1,0,-swing,0xa4bd47);limb(g,.5f,-1,0,swing,0xa4bd47);g.glPopMatrix();
    }
    private void limb(GL10 g,float x,float y,float z,float a,int color){g.glPushMatrix();g.glTranslatef(x,y,z);g.glRotatef(a,1,0,0);box(g,0,-1,0,1,2,1,color);g.glPopMatrix();}
    private void box(GL10 g,float x,float y,float z,float sx,float sy,float sz,int color){g.glPushMatrix();g.glTranslatef(x,y,z);g.glScalef(sx,sy,sz);g.glVertexPointer(3,GL10.GL_FLOAT,0,cube);for(int face=0;face<6;face++){float shade=SHADES[face];g.glColor4f(((color>>16)&255)/255f*shade,((color>>8)&255)/255f*shade,(color&255)/255f*shade,1);g.glDrawArrays(GL10.GL_TRIANGLES,face*6,6);}g.glPopMatrix();}
    private void cyl(GL10 g,float x,float y,float z,float sx,float sy,float sz,int color){g.glPushMatrix();g.glTranslatef(x,y,z);g.glScalef(sx,sy,sz);g.glColor4f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,1);mesh(g,cylinder,cylinderCount);g.glPopMatrix();}
    private void mesh(GL10 g,FloatBuffer b,int count){g.glVertexPointer(3,GL10.GL_FLOAT,0,b);g.glDrawArrays(GL10.GL_TRIANGLES,0,count);}
    public boolean onTouchEvent(MotionEvent e){int action=e.getActionMasked();if(action==MotionEvent.ACTION_DOWN){cameraPointer=e.getPointerId(0);lastX=e.getX();lastY=e.getY();}else if(action==MotionEvent.ACTION_MOVE){int i=e.findPointerIndex(cameraPointer);if(i>=0){cameraYaw-=(e.getX(i)-lastX)*.3f;pitch=Math.max(5,Math.min(65,pitch+(e.getY(i)-lastY)*.2f));lastX=e.getX(i);lastY=e.getY(i);}}else if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL)cameraPointer=-1;return true;}
}
