package lan.classic.android;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.opengl.GLSurfaceView;
import android.view.*;
import android.widget.*;
import java.nio.*;
import java.io.*;
import android.opengl.GLUtils;
import lan.classic.ClassicMesh;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import lan.classic.Appearance;

/** Offline wardrobe styled after the old mobile Roblox avatar screen. */
final class ClassicAvatarEditor extends LinearLayout {
    interface Actions { void save(Appearance appearance); void cancel(); void navigate(int tab,Appearance appearance); }
    private final Appearance appearance; private final AvatarPreview3D preview; private final Actions actions;
    private final int[] palette={0xf5cd30,0xffffff,0x111111,0x0d69ac,0xc4281c,0x4b974b,0x6b327c,0xa4bd47,0x8b4513,0x96999f};
    private final String[] slots={"Head","Torso","Left Arm","Right Arm","Left Leg","Right Leg"};
    private final String[] categories={"CLOTHES","HATS","FACE","BODY","ANIMATE"};
    private final String[] clothes={"Classic Shirt","Black Hoodie","Green Tee","Blue Uniform","Classic Pants","Dark Jeans"};
    private int category; private boolean expanded; private LinearLayout categoryRow,wardrobe;

    ClassicAvatarEditor(Context c,Appearance source,Actions a){
        super(c);actions=a;appearance=source==null?new Appearance():source.copy();setOrientation(VERTICAL);setBackgroundColor(0xffe8e8e8);
        LinearLayout header=new LinearLayout(c);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(d(6),0,d(7),0);header.setBackgroundColor(0xff123b92);
        TextView back=text("‹",34,Color.WHITE);back.setGravity(Gravity.CENTER);back.setContentDescription("Back");back.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.cancel();}});header.addView(back,new LayoutParams(d(42),d(54)));
        LinearLayout heading=new LinearLayout(c);heading.setOrientation(VERTICAL);TextView title=text("Avatar",20,Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);heading.addView(title);heading.addView(text("Classic R6 wardrobe",11,0xffc7d5f5));header.addView(heading,new LayoutParams(0,d(54),1));
        TextView coins=text("R$  0",13,Color.WHITE);coins.setGravity(Gravity.CENTER);coins.setBackgroundColor(0xff214da1);header.addView(coins,new LayoutParams(d(65),d(34)));TextView inbox=text("▤  0",14,Color.WHITE);inbox.setGravity(Gravity.CENTER);inbox.setBackgroundColor(0xff214da1);header.addView(inbox,new LayoutParams(d(62),d(34)));addView(header,new LayoutParams(-1,d(54)));
        ScrollView scroll=new ScrollView(c);scroll.setFillViewport(true);LinearLayout content=new LinearLayout(c);content.setOrientation(VERTICAL);content.setPadding(d(7),0,d(7),d(8));scroll.addView(content);addView(scroll,new LayoutParams(-1,0,1));
        final FrameLayout stage=new FrameLayout(c);preview=new AvatarPreview3D(c);stage.addView(preview,new FrameLayout.LayoutParams(-1,d(242)));
        LinearLayout rig=new LinearLayout(c);rig.setPadding(d(2),d(2),d(2),d(2));rig.setBackgroundDrawable(tabBackground(false));rig.addView(tab("R6",true),new LinearLayout.LayoutParams(d(56),d(32)));TextView r15=tab("R15",false);r15.setEnabled(false);r15.setAlpha(.45f);rig.addView(r15,new LinearLayout.LayoutParams(d(56),d(32)));FrameLayout.LayoutParams rp=new FrameLayout.LayoutParams(d(116),d(36),Gravity.RIGHT|Gravity.TOP);rp.topMargin=d(9);rp.rightMargin=d(8);stage.addView(rig,rp);
        TextView fullscreen=text("⛶",24,Color.WHITE);fullscreen.setGravity(Gravity.CENTER);fullscreen.setBackgroundColor(0x77000000);fullscreen.setContentDescription("Expand preview");fullscreen.setOnClickListener(new OnClickListener(){public void onClick(View v){expanded=!expanded;stage.getLayoutParams().height=d(expanded?400:242);preview.getLayoutParams().height=stage.getLayoutParams().height;stage.requestLayout();}});FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(d(42),d(42),Gravity.RIGHT|Gravity.BOTTOM);fp.bottomMargin=d(8);fp.rightMargin=d(8);stage.addView(fullscreen,fp);content.addView(stage,new LayoutParams(-1,d(242)));
        categoryRow=new LinearLayout(c);categoryRow.setBackgroundColor(Color.WHITE);categoryRow.setPadding(0,d(2),0,d(2));content.addView(categoryRow,new LayoutParams(-1,d(42)));buildCategories();
        TextView recent=text("RECENT",13,0xff707070);recent.setGravity(Gravity.CENTER_VERTICAL);recent.setPadding(d(8),d(5),d(8),0);content.addView(recent,new LayoutParams(-1,d(35)));
        wardrobe=new LinearLayout(c);wardrobe.setOrientation(VERTICAL);content.addView(wardrobe,new LayoutParams(-1,-2));buildWardrobe();
        LinearLayout bottom=new LinearLayout(c);Button cancel=actionButton("CANCEL"),save=actionButton("SAVE");cancel.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.cancel();}});save.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.save(appearance.copy());}});bottom.addView(cancel,new LayoutParams(0,d(46),1));bottom.addView(save,new LayoutParams(0,d(46),1));content.addView(bottom,new LayoutParams(-1,d(55)));
        LinearLayout nav=new LinearLayout(c);nav.setGravity(Gravity.CENTER);nav.setBackgroundColor(Color.WHITE);String[] navLabels={"⌂\nHOME","▣\nGAMES","●\nAVATAR","▤\nCHAT","•••\nMORE"};for(int i=0;i<navLabels.length;i++){final int target=i;TextView n=text(navLabels[i],10,i==2?0xff00a958:0xff222222);n.setGravity(Gravity.CENTER);if(i==2)n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);if(i!=2)n.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.navigate(target,appearance.copy());}});nav.addView(n,new LayoutParams(0,d(54),1));}addView(nav,new LayoutParams(-1,d(54)));
    }
    protected void onDetachedFromWindow(){preview.onPause();super.onDetachedFromWindow();}
    private int d(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(d(6),d(3),d(6),d(3));return t;}
    private TextView tab(String s,boolean selected){TextView t=text(s,13,selected?Color.WHITE:0xff38558e);t.setGravity(Gravity.CENTER);t.setBackgroundDrawable(tabBackground(selected));return t;}
    private GradientDrawable tabBackground(boolean selected){GradientDrawable g=new GradientDrawable();g.setColor(selected?0xff2f65c2:0xffe7edf9);g.setStroke(d(1),0xff4b75c5);return g;}
    private GradientDrawable tileBackground(){GradientDrawable g=new GradientDrawable();g.setColor(Color.WHITE);g.setStroke(d(2),0xff18a451);return g;}
    private GradientDrawable panel(){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff74787e,0xff393d43});g.setStroke(d(1),0xffb8bbc0);return g;}
    private Button smallButton(String s){Button b=new Button(getContext());b.setText(s);b.setTextColor(0xff333333);b.setTextSize(11);b.setGravity(Gravity.CENTER);b.setSingleLine(true);b.setBackgroundDrawable(panel());return b;}
    private Button actionButton(String s){Button b=smallButton(s);b.setTextColor(Color.WHITE);b.setTextSize(13);return b;}
    private void buildCategories(){categoryRow.removeAllViews();for(int i=0;i<categories.length;i++){final int n=i;TextView t=tab(categories[i],i==category);t.setTextSize(10);t.setBackgroundColor(i==category?0xffee9a24:Color.WHITE);t.setTextColor(0xff333333);t.setOnClickListener(new OnClickListener(){public void onClick(View v){category=n;buildCategories();buildWardrobe();}});categoryRow.addView(t,new LayoutParams(0,d(38),1));}}
    private void buildWardrobe(){
        wardrobe.removeAllViews();
        final String[][] labels={clothes,{"Brown cap","Hard hat","Black hat","Top hat"},{"Smile","Serious","Happy"},slots,{"Classic","Blocky","Cartoon"}};
        String[] items=labels[category];
        for(int row=0;row<(items.length+2)/3;row++){
            LinearLayout line=new LinearLayout(getContext());
            for(int col=0;col<3;col++){
                final int item=row*3+col;if(item>=items.length)break;
                LinearLayout cell=new LinearLayout(getContext());cell.setOrientation(VERTICAL);cell.setGravity(Gravity.CENTER);cell.setBackgroundDrawable(tileBackground());
                cell.addView(new ClothIcon(getContext(),item),new LayoutParams(-1,d(74)));
                TextView caption=text(items[item],10,0xff333333);caption.setGravity(Gravity.CENTER);cell.addView(caption);
                cell.setOnClickListener(new OnClickListener(){public void onClick(View v){
                    if(category==0){if(item<4)appearance.shirt=item;else appearance.pants=item-4;}
                    else if(category==1)appearance.hat=item;
                    else if(category==2)appearance.face=item;
                    else if(category==3)appearance.colors[item]=next(appearance.colors[item]);
                    else appearance.animationPack=item;
                    preview.requestRender();buildWardrobe();
                }});
                LayoutParams lp=new LayoutParams(0,d(104),1);lp.setMargins(d(3),d(3),d(3),d(3));line.addView(cell,lp);
            }wardrobe.addView(line);
        }
    }
    private int next(int c){for(int i=0;i<palette.length;i++)if(palette[i]==c)return palette[(i+1)%palette.length];return palette[0];}
    private String hex(int c){return String.format("#%06X",c&0xffffff);}
    private final class ClothIcon extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final int type;ClothIcon(Context c,int t){super(c);type=t;}
        void part(Canvas c,float x,float y,float w,float h,int color){p.setColor(0xff000000|color);c.drawRect(x,y,x+w,y+h,p);Path q=new Path();q.moveTo(x+w,y);q.lineTo(x+w+5,y-4);q.lineTo(x+w+5,y+h-4);q.lineTo(x+w,y+h);q.close();p.setColor(0xff000000|((color&0xfefefe)>>1));c.drawPath(q,p);p.setColor(0xffbbbbbb);q.reset();q.moveTo(x,y);q.lineTo(x+5,y-4);q.lineTo(x+w+5,y-4);q.lineTo(x+w,y);q.close();c.drawPath(q,p);}
        protected void onDraw(Canvas c){c.save();float sc=Math.min(getWidth()/90f,getHeight()/92f);c.translate(getWidth()/2f-45*sc,8*sc);c.scale(sc,sc);int shirt=new int[]{0x2776b9,0x202020,0x3b8d4b,0x354e92,0x4b556c,0x222a38}[type%6];part(c,30,5,23,18,appearance.colors[0]);part(c,25,26,32,30,shirt);part(c,10,27,13,29,appearance.colors[2]);part(c,59,27,13,29,appearance.colors[3]);part(c,26,57,14,25,0x343a48);part(c,43,57,14,25,0x343a48);if(category==1)part(c,24,1,37,8,shirt);if(category==3){p.setColor(0xff000000|appearance.colors[type]);c.drawCircle(71,12,9,p);}p.setColor(0xff111111);c.drawCircle(36,12,1.4f,p);c.drawCircle(48,12,1.4f,p);c.drawLine(37,18,46,18,p);c.restore();}
    }
    /** GLES 1.1 preview: actual boxes, sleeves, pant shells and hats are
     * rendered in perspective instead of being painted as a flat icon. */
    private final class AvatarPreview3D extends GLSurfaceView implements GLSurfaceView.Renderer {
        private ImportedModel imported; private FloatBuffer cube; private float spin; private volatile float yaw=155;private float lastX;
        AvatarPreview3D(Context c){super(c);setEGLConfigChooser(5,6,5,0,16,0);setRenderer(this);setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);}
        private FloatBuffer mesh(float[] v){FloatBuffer b=ByteBuffer.allocateDirect(v.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();b.put(v).position(0);return b;}
        public void onSurfaceCreated(GL10 gl,EGLConfig cfg){gl.glClearColor(.78f,.68f,.55f,1);gl.glEnable(GL10.GL_DEPTH_TEST);gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);float[] v={-.5f,-.5f,-.5f,.5f,-.5f,-.5f,.5f,.5f,-.5f,-.5f,.5f,-.5f,-.5f,-.5f,.5f,.5f,-.5f,.5f,.5f,.5f,.5f,-.5f,.5f,.5f};int[] ix={4,5,6,4,6,7,1,0,3,1,3,2,0,4,7,0,7,3,5,1,2,5,2,6,3,7,6,3,6,2,0,1,5,0,5,4};float[] out=new float[ix.length*3];for(int i=0;i<ix.length;i++)for(int j=0;j<3;j++)out[i*3+j]=v[ix[i]*3+j];cube=mesh(out);String path=getContext().getSharedPreferences("settings",Context.MODE_PRIVATE).getString("avatarContentPath","");imported=null;if(path.length()>0)try{imported=new ImportedModel(gl,new File(path));}catch(IOException e){post(new Runnable(){public void run(){Toast.makeText(getContext(),"Cannot load imported R6. Import the OBB again.",Toast.LENGTH_LONG).show();}});}}
        public void onSurfaceChanged(GL10 gl,int w,int h){gl.glViewport(0,0,w,h);gl.glMatrixMode(GL10.GL_PROJECTION);gl.glLoadIdentity();float ratio=(float)w/Math.max(1,h);gl.glFrustumf(-ratio,ratio,-1,1,2.5f,60);gl.glMatrixMode(GL10.GL_MODELVIEW);}
        public void onDrawFrame(GL10 gl){spin+=.35f;gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);gl.glLoadIdentity();gl.glTranslatef(0,.3f,-13.5f);gl.glRotatef(-7,1,0,0);gl.glRotatef((float)Math.sin(spin*.01f)*3,0,1,0);box(gl,0,-3.3f,0,12,.2f,9,0x704d36);box(gl,0,1.5f,2.2f,8,6,.2f,0xc3a47b);box(gl,-3.7f,1.5f,0,.2f,6,5,0xb48f6c);box(gl,3.7f,1.5f,0,.2f,6,5,0xb48f6c);for(int side:new int[]{-1,1}){box(gl,side*3,0,1.5f,1.7f,5.8f,1.4f,0x704831);for(int shelf=0;shelf<4;shelf++){box(gl,side*3,-2.3f+shelf*1.3f,.8f,1.7f,.12f,1.9f,0xb38156);box(gl,side*3,-2+shelf*1.3f,.9f,1.1f,.4f,.8f,shelf%2==0?0x455565:0x89503e);}}box(gl,0,3.2f,1,5,.2f,1.5f,0x986e48);box(gl,0,-3.12f,0,4,.25f,3,0xd8cba8);gl.glPushMatrix();gl.glRotatef(yaw,0,1,0);drawAvatar(gl);gl.glPopMatrix();try{Thread.sleep(33);}catch(InterruptedException ignored){}}
        public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){lastX=e.getX();getParent().requestDisallowInterceptTouchEvent(true);}else if(e.getAction()==MotionEvent.ACTION_MOVE){yaw+=(e.getX()-lastX)*.6f;lastX=e.getX();}else getParent().requestDisallowInterceptTouchEvent(false);return true;}
        private void drawAvatar(GL10 gl){if(imported!=null){imported.draw(gl);return;}Appearance ap=appearance;int shirt=new int[]{ap.colors[1],0x171717,0x3b8d4b,0x284a91}[Math.max(0,Math.min(3,ap.shirt))];int pants=new int[]{ap.colors[4],0x1d1d1d,0x34415e}[Math.max(0,Math.min(2,ap.pants))];float walk=(float)Math.sin(spin*.12f)*5;gl.glRotatef(appearance.animationPack==1?walk:appearance.animationPack==2?walk*.5f:0,0,0,1);box(gl,0,.05f,0,2.05f,2.05f,1.08f,shirt);head(gl,ap.colors[0]);box(gl,-1.55f,.05f,0,1.05f,2.05f,1.05f,ap.colors[2]);box(gl,1.55f,.05f,0,1.05f,2.05f,1.05f,ap.colors[3]);box(gl,-.52f,-2.0f,0,1.08f,2.1f,1.08f,pants);box(gl,.52f,-2.0f,0,1.08f,2.1f,1.08f,pants);box(gl,-1.55f,.05f,-.57f,1.08f,1.1f,.08f,shirt);box(gl,1.55f,.05f,-.57f,1.08f,1.1f,.08f,shirt);box(gl,-.52f,-2.0f,-.57f,1.1f,1.1f,.08f,pants);box(gl,.52f,-2.0f,-.57f,1.1f,1.1f,.08f,pants);box(gl,-.23f,1.78f,-.69f,.12f,.14f,.04f,0x151515);box(gl,.23f,1.78f,-.69f,.12f,.14f,.04f,0x151515);drawHat(gl,ap);box(gl,0,1.38f,-.69f,.32f,.045f,.04f,0x151515);box(gl,0,.1f,-.57f,.045f,1.9f,.045f,0xbac0c6);for(int i=0;i<3;i++)box(gl,.25f,.6f-i*.4f,-.58f,.08f,.08f,.04f,0xe0e0e0);}
        private void drawHat(GL10 gl,Appearance ap){if(ap.hat==0)box(gl,0,2.5f,0,1.8f,.28f,1.8f,0x4b2e1a);else if(ap.hat==1){box(gl,0,2.48f,0,1.6f,.35f,1.6f,0xffd83d);box(gl,0,2.67f,0,1.15f,.42f,1.15f,0xffd83d);}else if(ap.hat==2)box(gl,0,2.53f,0,2.5f,.18f,.62f,0x222222);else box(gl,0,2.65f,0,.95f,.8f,.95f,0x8f8f8f);}
        private final class ImportedModel {
            final FloatBuffer[] vertices=new FloatBuffer[6], colors=new FloatBuffer[6];
            final float[][] shade=new float[6][];final int[] counts=new int[6],lastColor={-1,-1,-1,-1,-1,-1};
            final FloatBuffer faceVertices=mesh(new float[]{-.52f,1.15f,-.606f,.52f,1.15f,-.606f,-.52f,2.19f,-.606f,.52f,2.19f,-.606f});
            final FloatBuffer faceUv=mesh(new float[]{1,1,0,1,1,0,0,0});
            int faceTexture;
            ImportedModel(GL10 gl,File directory)throws IOException{
                for(int i=0;i<6;i++){
                    ClassicMesh model;InputStream in=new FileInputStream(new File(directory,AvatarContent.NAMES[i]+".mesh"));
                    try{model=ClassicMesh.read(in);}finally{in.close();}
                    vertices[i]=mesh(model.positions);counts[i]=model.vertexCount;colors[i]=mesh(new float[model.vertexCount*4]);shade[i]=new float[model.vertexCount];
                    for(int v=0;v<model.vertexCount;v++)shade[i][v]=.55f+.45f*Math.max(0,model.normals[v*3]*-.3f+model.normals[v*3+1]*.8f-model.normals[v*3+2]*.5f);
                }
                Bitmap bitmap=BitmapFactory.decodeFile(new File(directory,"face.png").getPath());if(bitmap==null)throw new IOException("Invalid face texture");
                try{int[] ids=new int[1];gl.glGenTextures(1,ids,0);faceTexture=ids[0];gl.glBindTexture(GL10.GL_TEXTURE_2D,faceTexture);gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);GLUtils.texImage2D(GL10.GL_TEXTURE_2D,0,bitmap,0);}finally{bitmap.recycle();}
            }
            void part(GL10 gl,int i,float x,float y,int color){
                if(lastColor[i]!=color){colors[i].position(0);for(float light:shade[i]){colors[i].put(((color>>16)&255)/255f*light);colors[i].put(((color>>8)&255)/255f*light);colors[i].put((color&255)/255f*light);colors[i].put(1);}colors[i].position(0);lastColor[i]=color;}
                gl.glPushMatrix();gl.glTranslatef(x,y,0);gl.glEnableClientState(GL10.GL_COLOR_ARRAY);gl.glColorPointer(4,GL10.GL_FLOAT,0,colors[i]);gl.glVertexPointer(3,GL10.GL_FLOAT,0,vertices[i]);gl.glDrawArrays(GL10.GL_TRIANGLES,0,counts[i]);gl.glDisableClientState(GL10.GL_COLOR_ARRAY);gl.glPopMatrix();
            }
            void draw(GL10 gl){
                Appearance ap=appearance;int shirt=new int[]{ap.colors[1],0x171717,0x3b8d4b,0x284a91}[Math.max(0,Math.min(3,ap.shirt))];int pants=new int[]{ap.colors[4],0x1d1d1d,0x34415e}[Math.max(0,Math.min(2,ap.pants))];
                gl.glPushMatrix();float sway=(float)Math.sin(spin*.12f)*5;gl.glRotatef(ap.animationPack==1?sway:ap.animationPack==2?sway*.5f:0,0,0,1);
                part(gl,0,0,1.65f,ap.colors[0]);part(gl,1,0,0,shirt);part(gl,2,-1.5f,0,ap.colors[2]);part(gl,3,1.5f,0,ap.colors[3]);part(gl,4,-.5f,-2,pants);part(gl,5,.5f,-2,ap.pants==0?ap.colors[5]:pants);
                drawHat(gl,ap);
                if(ap.face==0){gl.glEnable(GL10.GL_TEXTURE_2D);gl.glBindTexture(GL10.GL_TEXTURE_2D,faceTexture);gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);gl.glTexCoordPointer(2,GL10.GL_FLOAT,0,faceUv);gl.glVertexPointer(3,GL10.GL_FLOAT,0,faceVertices);gl.glColor4f(1,1,1,1);gl.glEnable(GL10.GL_BLEND);gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP,0,4);gl.glDisable(GL10.GL_BLEND);gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);gl.glDisable(GL10.GL_TEXTURE_2D);}else{
                    box(gl,-.23f,1.78f,-.61f,.12f,.14f,.04f,0x151515);box(gl,.23f,1.78f,-.61f,.12f,.14f,.04f,0x151515);
                    box(gl,0,1.38f,-.61f,.32f,ap.face==2?.15f:.045f,.04f,0x151515);
                }gl.glPopMatrix();
            }
        }
        private void head(GL10 g,int color){for(int i=0;i<20;i++){g.glPushMatrix();g.glTranslatef(0,1.7f,0);g.glRotatef(i*18,0,1,0);box(g,0,0,.56f,.22f,1.15f,.16f,color);g.glPopMatrix();}box(g,0,1.7f,0,.85f,1.15f,.85f,color);}
        private void box(GL10 gl,float x,float y,float z,float sx,float sy,float sz,int color){gl.glPushMatrix();gl.glTranslatef(x,y,z);gl.glScalef(sx,sy,sz);gl.glColor4f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,1);gl.glVertexPointer(3,GL10.GL_FLOAT,0,cube);for(int face=0;face<6;face++){float shade=new float[]{.85f,.7f,.75f,.9f,1,.6f}[face];gl.glColor4f(((color>>16)&255)/255f*shade,((color>>8)&255)/255f*shade,(color&255)/255f*shade,1);gl.glDrawArrays(GL10.GL_TRIANGLES,face*6,6);}gl.glPopMatrix();}
    }
}
