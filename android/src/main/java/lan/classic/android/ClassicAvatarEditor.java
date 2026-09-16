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
    ClassicAvatarEditor(Context c,Appearance source,Actions a){super(c);actions=a;appearance=source==null?new Appearance():source.copy();setOrientation(VERTICAL);setBackgroundColor(0xffececec);setPadding(d(8),d(0),d(8),d(8));
        LinearLayout header=new LinearLayout(c);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(d(10),0,d(10),0);header.setBackgroundColor(0xff173b91);TextView title=text("Avatar",20,Color.WHITE);header.addView(title,new LayoutParams(0,d(48),1));TextView r6=text("R6",14,Color.WHITE);r6.setGravity(Gravity.CENTER);r6.setBackgroundColor(0xff4b75d1);header.addView(r6,new LayoutParams(d(48),d(32)));TextView r15=text("R15",14,0xffc8d0e5);r15.setGravity(Gravity.CENTER);r15.setBackgroundColor(0xff29447e);header.addView(r15,new LayoutParams(d(52),d(32)));addView(header,new LayoutParams(-1,d(54)));
        preview=new Preview(c);addView(preview,new LayoutParams(-1,d(190)));
        TextView tabs=text("Body     Hats     Face     Clothes",14,0xff333333);tabs.setGravity(Gravity.CENTER);tabs.setBackgroundColor(Color.WHITE);addView(tabs,new LayoutParams(-1,d(38)));
        for(int i=0;i<6;i++){final int slot=i;Button b=button(names[i]+"  "+hex(appearance.colors[i]));b.setOnClickListener(new OnClickListener(){public void onClick(View v){appearance.colors[slot]=next(appearance.colors[slot]);((Button)v).setText(names[slot]+"  "+hex(appearance.colors[slot]));preview.invalidate();}});addView(b,new LayoutParams(-1,d(38)));}
        TextView recent=text("RECENT / НЕДАВНИЕ",13,0xff777777);recent.setPadding(d(8),d(8),d(8),d(3));addView(recent,new LayoutParams(-1,d(30)));
        LinearLayout wardrobe=new LinearLayout(c);wardrobe.setOrientation(VERTICAL);String[][] clothes={{"Classic Shirt","Black Hoodie"},{"Green Tee","Blue Uniform"},{"Classic Pants","Dark Jeans"}};for(int row=0;row<clothes.length;row++){LinearLayout line=new LinearLayout(c);for(int col=0;col<2;col++){final int item=row*2+col;Button b=button(clothes[row][col]);b.setTextColor(0xff222222);b.setBackgroundDrawable(wardrobeCell());b.setOnClickListener(new OnClickListener(){public void onClick(View v){if(item<4)appearance.shirt=item;else appearance.pants=item-4;preview.invalidate();}});line.addView(b,new LayoutParams(0,d(42),1));}wardrobe.addView(line,new LayoutParams(-1,d(46)));}addView(wardrobe,new LayoutParams(-1,d(138)));
        LinearLayout hats=new LinearLayout(c);hats.setGravity(Gravity.CENTER);for(int i=0;i<4;i++){final int hat=i;Button b=button("Hat "+(i+1));b.setOnClickListener(new OnClickListener(){public void onClick(View v){appearance.hat=hat;preview.invalidate();}});hats.addView(b,new LayoutParams(0,d(40),1));}addView(hats,new LayoutParams(-1,d(46)));
        LinearLayout bottom=new LinearLayout(c);Button cancel=button("CANCEL"),save=button("SAVE");cancel.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.cancel();}});save.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.save(appearance.copy());}});bottom.addView(cancel,new LayoutParams(0,d(44),1));bottom.addView(save,new LayoutParams(0,d(44),1));addView(bottom,new LayoutParams(-1,d(52)));
    }
    private int d(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(d(6),d(4),d(6),d(4));return t;}
    private GradientDrawable panel(){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff73777d,0xff393d43});g.setStroke(d(1),0xffb1b4b9);return g;}
    private GradientDrawable wardrobeCell(){GradientDrawable g=new GradientDrawable();g.setColor(Color.WHITE);g.setStroke(d(2),0xff18a451);return g;}
    private Button button(String s){Button b=new Button(getContext());b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(12);b.setSingleLine(true);b.setBackgroundDrawable(panel());return b;}
    private int next(int c){for(int i=0;i<palette.length;i++)if(palette[i]==c)return palette[(i+1)%palette.length];return palette[0];}
    private String hex(int c){return String.format("#%06X",c&0xffffff);}
    private final class Preview extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);Preview(Context c){super(c);}
        void rect(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}
        protected void onDraw(Canvas c){float s=Math.min(getWidth()/220f,getHeight()/180f);c.save();c.translate(getWidth()/2f,getHeight()/2f);c.scale(s,s);rect(c,-110,-90,220,180,0xffc2a47b);rect(c,-110,45,220,45,0xff6f4e36);rect(c,-92,-48,46,70,0xff76503a);rect(c,46,-48,46,70,0xff76503a);rect(c,-18,-64,36,32,appearance.colors[0]);int shirtColor=new int[]{appearance.colors[1],0x171717,0x3b8d4b,0x284a91}[Math.max(0,Math.min(3,appearance.shirt))];int pantsColor=new int[]{appearance.colors[4],0x1d1d1d,0x34415e}[Math.max(0,Math.min(2,appearance.pants))];rect(c,-30,-30,60,34,shirtColor);rect(c,-58,-27,24,34,appearance.colors[2]);rect(c,34,-27,24,34,appearance.colors[3]);rect(c,-28,4,24,44,pantsColor);rect(c,4,4,24,44,pantsColor);if(appearance.hat==0)rect(c,-34,-70,68,8,0x4b2e1a);else if(appearance.hat==1)rect(c,-32,-74,64,8,0xffd83d);else if(appearance.hat==2)rect(c,-42,-69,84,5,0x222222);else rect(c,-16,-73,32,10,0x8f8f8f);p.setColor(0x222222);c.drawCircle(-8,-51,2,p);c.drawCircle(8,-51,2,p);c.restore();}
    }
}
