package lan.classic.android;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import lan.classic.Appearance;

/** Small, offline R6 editor styled after the old rectangular Roblox panels. */
final class ClassicAvatarEditor extends LinearLayout {
    interface Actions { void save(Appearance appearance); void cancel(); }
    private final Appearance appearance; private final Preview preview; private final Actions actions;
    private final int[] palette={0xf5cd30,0xffffff,0x111111,0x0d69ac,0xc4281c,0x4b974b,0x6b327c,0xa4bd47,0x8b4513,0x96999f};
    private final String[] names={"Head","Torso","Left Arm","Right Arm","Left Leg","Right Leg"};
    ClassicAvatarEditor(Context c,Appearance source,Actions a){super(c);actions=a;appearance=source==null?new Appearance():source.copy();setOrientation(VERTICAL);setBackgroundColor(0xff777f88);setPadding(d(12),d(8),d(12),d(8));
        TextView title=text("CHARACTER",20,Color.WHITE);title.setGravity(Gravity.CENTER);addView(title,new LayoutParams(-1,d(42)));
        preview=new Preview(c);addView(preview,new LayoutParams(-1,d(190)));
        TextView tabs=text("Body     Hats     Face     Clothes",14,0xffe0e0e0);tabs.setGravity(Gravity.CENTER);tabs.setBackgroundDrawable(panel());addView(tabs,new LayoutParams(-1,d(38)));
        for(int i=0;i<6;i++){final int slot=i;Button b=button(names[i]+"  "+hex(appearance.colors[i]));b.setOnClickListener(new OnClickListener(){public void onClick(View v){appearance.colors[slot]=next(appearance.colors[slot]);((Button)v).setText(names[slot]+"  "+hex(appearance.colors[slot]));preview.invalidate();}});addView(b,new LayoutParams(-1,d(38)));}
        LinearLayout hats=new LinearLayout(c);hats.setGravity(Gravity.CENTER);for(int i=0;i<4;i++){final int hat=i;Button b=button("Hat "+(i+1));b.setOnClickListener(new OnClickListener(){public void onClick(View v){appearance.hat=hat;preview.invalidate();}});hats.addView(b,new LayoutParams(0,d(40),1));}addView(hats,new LayoutParams(-1,d(46)));
        LinearLayout bottom=new LinearLayout(c);Button cancel=button("CANCEL"),save=button("SAVE");cancel.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.cancel();}});save.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.save(appearance.copy());}});bottom.addView(cancel,new LayoutParams(0,d(44),1));bottom.addView(save,new LayoutParams(0,d(44),1));addView(bottom,new LayoutParams(-1,d(52)));
    }
    private int d(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(d(6),d(4),d(6),d(4));return t;}
    private GradientDrawable panel(){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff73777d,0xff393d43});g.setStroke(d(1),0xffb1b4b9);return g;}
    private Button button(String s){Button b=new Button(getContext());b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(12);b.setSingleLine(true);b.setBackgroundDrawable(panel());return b;}
    private int next(int c){for(int i=0;i<palette.length;i++)if(palette[i]==c)return palette[(i+1)%palette.length];return palette[0];}
    private String hex(int c){return String.format("#%06X",c&0xffffff);}
    private final class Preview extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);Preview(Context c){super(c);}
        void rect(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}
        protected void onDraw(Canvas c){float s=Math.min(getWidth()/220f,getHeight()/180f);c.save();c.translate(getWidth()/2f,getHeight()/2f);c.scale(s,s);rect(c,-110,-90,220,180,0xff9cc7dc);rect(c,-110,58,220,32,0xff639145);rect(c,-18,-64,36,32,appearance.colors[0]);rect(c,-30,-30,60,34,appearance.colors[1]);rect(c,-58,-27,24,34,appearance.colors[2]);rect(c,34,-27,24,34,appearance.colors[3]);rect(c,-28,4,24,44,appearance.colors[4]);rect(c,4,4,24,44,appearance.colors[5]);if(appearance.hat==0)rect(c,-34,-70,68,8,0x4b2e1a);else if(appearance.hat==1)rect(c,-32,-74,64,8,0xffd83d);else if(appearance.hat==2)rect(c,-42,-69,84,5,0x222222);else rect(c,-16,-73,32,10,0x8f8f8f);p.setColor(0x222222);c.drawCircle(-8,-51,2,p);c.drawCircle(8,-51,2,p);c.restore();}
    }
}
