package lan.classic.android;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import lan.classic.Appearance;

/** Offline wardrobe styled after the old mobile Roblox avatar screen. */
final class ClassicAvatarEditor extends LinearLayout {
    interface Actions { void save(Appearance appearance); void cancel(); }
    private final Appearance appearance; private final Preview preview; private final Actions actions;
    private final int[] palette={0xf5cd30,0xffffff,0x111111,0x0d69ac,0xc4281c,0x4b974b,0x6b327c,0xa4bd47,0x8b4513,0x96999f};
    private final String[] slots={"Head","Torso","Left Arm","Right Arm","Left Leg","Right Leg"};
    private final String[] categories={"BODY","HATS","FACE","CLOTHES"};
    private final String[] clothes={"Classic Shirt","Black Hoodie","Green Tee","Blue Uniform","Classic Pants","Dark Jeans"};
    private int category; private LinearLayout categoryRow,wardrobe;

    ClassicAvatarEditor(Context c,Appearance source,Actions a){
        super(c);actions=a;appearance=source==null?new Appearance():source.copy();setOrientation(VERTICAL);setBackgroundColor(0xffe8e8e8);
        LinearLayout header=new LinearLayout(c);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(d(6),0,d(7),0);header.setBackgroundColor(0xff123b92);
        TextView back=text("‹",34,Color.WHITE);back.setGravity(Gravity.CENTER);back.setContentDescription("Back");back.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.cancel();}});header.addView(back,new LayoutParams(d(42),d(54)));
        LinearLayout heading=new LinearLayout(c);heading.setOrientation(VERTICAL);TextView title=text("Avatar",20,Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);heading.addView(title);heading.addView(text("Classic R6 wardrobe",11,0xffc7d5f5));header.addView(heading,new LayoutParams(0,d(54),1));
        TextView coins=text("R$  0",13,Color.WHITE);coins.setGravity(Gravity.CENTER);coins.setBackgroundColor(0xff214da1);header.addView(coins,new LayoutParams(d(65),d(34)));TextView inbox=text("▤  0",14,Color.WHITE);inbox.setGravity(Gravity.CENTER);inbox.setBackgroundColor(0xff214da1);header.addView(inbox,new LayoutParams(d(62),d(34)));addView(header,new LayoutParams(-1,d(54)));
        ScrollView scroll=new ScrollView(c);scroll.setFillViewport(true);LinearLayout content=new LinearLayout(c);content.setOrientation(VERTICAL);content.setPadding(d(7),0,d(7),d(8));scroll.addView(content);addView(scroll,new LayoutParams(-1,0,1));
        FrameLayout stage=new FrameLayout(c);preview=new Preview(c);stage.addView(preview,new FrameLayout.LayoutParams(-1,d(242)));
        LinearLayout rig=new LinearLayout(c);rig.setPadding(d(2),d(2),d(2),d(2));rig.setBackgroundDrawable(tabBackground(false));rig.addView(tab("R6",true),new LinearLayout.LayoutParams(d(56),d(32)));rig.addView(tab("R15",false),new LinearLayout.LayoutParams(d(56),d(32)));FrameLayout.LayoutParams rp=new FrameLayout.LayoutParams(d(116),d(36),Gravity.RIGHT|Gravity.TOP);rp.topMargin=d(9);rp.rightMargin=d(8);stage.addView(rig,rp);
        TextView fullscreen=text("⛶",24,Color.WHITE);fullscreen.setGravity(Gravity.CENTER);fullscreen.setBackgroundColor(0x77000000);fullscreen.setContentDescription("Preview fullscreen");FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(d(42),d(42),Gravity.RIGHT|Gravity.BOTTOM);fp.bottomMargin=d(8);fp.rightMargin=d(8);stage.addView(fullscreen,fp);content.addView(stage,new LayoutParams(-1,d(242)));
        categoryRow=new LinearLayout(c);categoryRow.setBackgroundColor(Color.WHITE);categoryRow.setPadding(0,d(2),0,d(2));content.addView(categoryRow,new LayoutParams(-1,d(42)));buildCategories();
        TextView recent=text("RECENT",13,0xff707070);recent.setGravity(Gravity.CENTER_VERTICAL);recent.setPadding(d(8),d(5),d(8),0);content.addView(recent,new LayoutParams(-1,d(35)));
        wardrobe=new LinearLayout(c);wardrobe.setOrientation(VERTICAL);content.addView(wardrobe,new LayoutParams(-1,d(300)));buildWardrobe();
        TextView bodyTitle=text("BODY COLORS",13,0xff707070);bodyTitle.setGravity(Gravity.CENTER_VERTICAL);bodyTitle.setPadding(d(8),d(7),d(8),0);content.addView(bodyTitle,new LayoutParams(-1,d(33)));
        for(int i=0;i<6;i++){final int slot=i;Button b=smallButton(slots[i]+"   "+hex(appearance.colors[i]));b.setOnClickListener(new OnClickListener(){public void onClick(View v){appearance.colors[slot]=next(appearance.colors[slot]);((Button)v).setText(slots[slot]+"   "+hex(appearance.colors[slot]));preview.invalidate();}});content.addView(b,new LayoutParams(-1,d(36)));}
        TextView hatsTitle=text("HATS",13,0xff707070);hatsTitle.setGravity(Gravity.CENTER_VERTICAL);hatsTitle.setPadding(d(8),d(7),d(8),0);content.addView(hatsTitle,new LayoutParams(-1,d(33)));
        LinearLayout hats=new LinearLayout(c);for(int i=0;i<4;i++){final int hat=i;Button b=smallButton("Hat "+(i+1));b.setOnClickListener(new OnClickListener(){public void onClick(View v){appearance.hat=hat;preview.invalidate();}});hats.addView(b,new LayoutParams(0,d(40),1));}content.addView(hats,new LayoutParams(-1,d(46)));
        LinearLayout bottom=new LinearLayout(c);Button cancel=actionButton("CANCEL"),save=actionButton("SAVE");cancel.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.cancel();}});save.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.save(appearance.copy());}});bottom.addView(cancel,new LayoutParams(0,d(46),1));bottom.addView(save,new LayoutParams(0,d(46),1));content.addView(bottom,new LayoutParams(-1,d(55)));
    }
    private int d(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(d(6),d(3),d(6),d(3));return t;}
    private TextView tab(String s,boolean selected){TextView t=text(s,13,selected?Color.WHITE:0xff38558e);t.setGravity(Gravity.CENTER);t.setBackgroundDrawable(tabBackground(selected));return t;}
    private GradientDrawable tabBackground(boolean selected){GradientDrawable g=new GradientDrawable();g.setColor(selected?0xff2f65c2:0xffe7edf9);g.setStroke(d(1),0xff4b75c5);return g;}
    private GradientDrawable tileBackground(){GradientDrawable g=new GradientDrawable();g.setColor(Color.WHITE);g.setStroke(d(2),0xff18a451);return g;}
    private GradientDrawable panel(){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff74787e,0xff393d43});g.setStroke(d(1),0xffb8bbc0);return g;}
    private Button smallButton(String s){Button b=new Button(getContext());b.setText(s);b.setTextColor(0xff333333);b.setTextSize(11);b.setGravity(Gravity.CENTER);b.setSingleLine(true);b.setBackgroundDrawable(panel());return b;}
    private Button actionButton(String s){Button b=smallButton(s);b.setTextColor(Color.WHITE);b.setTextSize(13);return b;}
    private void buildCategories(){categoryRow.removeAllViews();for(int i=0;i<categories.length;i++){final int n=i;TextView t=tab(categories[i],i==category);t.setOnClickListener(new OnClickListener(){public void onClick(View v){category=n;buildCategories();}});categoryRow.addView(t,new LayoutParams(0,d(38),1));}}
    private void buildWardrobe(){wardrobe.removeAllViews();for(int row=0;row<3;row++){LinearLayout line=new LinearLayout(getContext());for(int col=0;col<2;col++){final int item=row*2+col;LinearLayout cell=new LinearLayout(getContext());cell.setOrientation(VERTICAL);cell.setGravity(Gravity.CENTER);cell.setPadding(d(2),d(2),d(2),d(2));cell.setBackgroundDrawable(tileBackground());cell.addView(new ClothIcon(getContext(),item),new LayoutParams(-1,d(47)));TextView name=text(clothes[item],10,0xff333333);name.setGravity(Gravity.CENTER);cell.addView(name,new LayoutParams(-1,d(21)));cell.setOnClickListener(new OnClickListener(){public void onClick(View v){if(item<4)appearance.shirt=item;else appearance.pants=item-4;preview.invalidate();}});line.addView(cell,new LayoutParams(0,d(89),1));if(col==0){Space gap=new Space(getContext());line.addView(gap,new LayoutParams(d(6),d(89)));}}wardrobe.addView(line,new LayoutParams(-1,d(94)));}}
    private int next(int c){for(int i=0;i<palette.length;i++)if(palette[i]==c)return palette[(i+1)%palette.length];return palette[0];}
    private String hex(int c){return String.format("#%06X",c&0xffffff);}
    private final class ClothIcon extends View {final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final int type;ClothIcon(Context c,int t){super(c);type=t;}protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setColor(type==0?0xff2776b9:type==1?0xff202020:type==2?0xff3b8d4b:type==3?0xff354e92:0xff4b556c);c.drawRect(w*.32f,h*.12f,w*.68f,h*.86f,p);p.setColor(0xfff5cd30);c.drawCircle(w*.5f,h*.14f,w*.13f,p);p.setColor(0xff222222);p.setStrokeWidth(d(3));c.drawLine(w*.35f,h*.45f,w*.19f,h*.70f,p);c.drawLine(w*.65f,h*.45f,w*.81f,h*.70f,p);}}
    private final class Preview extends View {final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);Preview(Context c){super(c);}void rect(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}protected void onDraw(Canvas c){float s=Math.min(getWidth()/300f,getHeight()/242f);c.save();c.translate(getWidth()/2f,getHeight()/2f);c.scale(s,s);rect(c,-150,-121,300,242,0xffc3a47b);rect(c,-150,54,300,67,0xff704d36);rect(c,-122,-64,54,95,0xff76503a);rect(c,68,-64,54,95,0xff76503a);rect(c,-96,-96,60,9,0xff8d5d3d);rect(c,36,-96,60,9,0xff8d5d3d);int shirtColor=new int[]{appearance.colors[1],0x171717,0x3b8d4b,0x284a91}[Math.max(0,Math.min(3,appearance.shirt))];int pantsColor=new int[]{appearance.colors[4],0x1d1d1d,0x34415e}[Math.max(0,Math.min(2,appearance.pants))];rect(c,-18,-80,36,32,appearance.colors[0]);rect(c,-30,-47,60,39,shirtColor);rect(c,-58,-44,24,39,appearance.colors[2]);rect(c,34,-44,24,39,appearance.colors[3]);rect(c,-28,-7,24,53,pantsColor);rect(c,4,-7,24,53,pantsColor);if(appearance.hat==0)rect(c,-34,-86,68,9,0x4b2e1a);else if(appearance.hat==1)rect(c,-32,-91,64,9,0xffd83d);else if(appearance.hat==2)rect(c,-42,-85,84,6,0x222222);else rect(c,-16,-90,32,12,0x8f8f8f);p.setColor(0x222222);c.drawCircle(-8,-67,2,p);c.drawCircle(8,-67,2,p);c.drawRect(-5,-57,5,-55,p);c.restore();}}
}
