package lan.classic.android;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import lan.classic.*;

/** Translucent classic escape menu. Underlying LAN simulation intentionally keeps running. */
final class ClassicPauseMenu extends Dialog {
    interface Actions {
        Net.Snapshot snapshot(); boolean isFriend(String name); void addFriend(String name);
        int fps(); void fps(int value); boolean studs(); void studs(boolean value);
        void reset(); void leave();
    }
    private final Actions actions;private final boolean ru;
    private LinearLayout root,content,footer,tabs;
    private int selected;
    ClassicPauseMenu(Context context,boolean ru,Actions actions) {
        super(context);this.actions=actions;this.ru=ru;requestWindowFeature(Window.FEATURE_NO_TITLE);
        root=new LinearLayout(context);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(d(8),d(8),d(8),d(8));
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xe64f5963,0xdb2e343b});bg.setStroke(d(1),0xffa1acb6);root.setBackgroundDrawable(bg);
        tabs=new LinearLayout(context);root.addView(tabs,new LinearLayout.LayoutParams(-1,d(43)));
        ScrollView sc=new ScrollView(context);content=new LinearLayout(context);content.setOrientation(LinearLayout.VERTICAL);sc.addView(content);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,0,1);cp.topMargin=d(8);root.addView(sc,cp);
        footer=new LinearLayout(context);root.addView(footer,new LinearLayout.LayoutParams(-1,d(48)));setContentView(root);setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams wp=getWindow().getAttributes();wp.dimAmount=.35f;getWindow().setAttributes(wp);
        page(0);
    }
    public void show(){super.show();android.util.DisplayMetrics m=getContext().getResources().getDisplayMetrics();getWindow().setLayout(Math.min(d(760),(int)(m.widthPixels*.95f)),(int)(m.heightPixels*.9f));}
    private int d(int n){return (int)(n*getContext().getResources().getDisplayMetrics().density+.5f);}
    private String tr(String en,String r){return ru?r:en;}
    private TextView text(String value,int size){TextView t=new TextView(getContext());t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(d(8),d(8),d(8),d(8));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private Button button(String label,View.OnClickListener click){Button b=new Button(getContext());b.setText(label);b.setTextSize(12);b.setTextColor(Color.WHITE);b.setPadding(d(3),0,d(3),0);GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff637382,0xff3d4756});bg.setStroke(d(1),0xff8498aa);b.setBackgroundDrawable(bg);b.setOnClickListener(click);return b;}
    private void cell(LinearLayout row,View view,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,weight);p.setMargins(d(2),d(3),d(2),d(3));row.addView(view,p);}
    private void page(int tab) {
        selected=tab;content.removeAllViews();footer.removeAllViews();tabs.removeAllViews();
        String[] names={tr("Players","Игроки"),tr("Settings","Настройки"),tr("Help","Помощь")};
        for(int i=0;i<names.length;i++){final int target=i;Button b=button(names[i],new View.OnClickListener(){public void onClick(View v){page(target);}});if(tab==i)b.setTextColor(0xff61d7ff);cell(tabs,b,1);}
        if(tab==0) {
            Net.Snapshot s=actions.snapshot();content.addView(text(tr("Invite friends: join the same Wi-Fi → Local Servers","Пригласить друзей: тот же Wi-Fi → Серверы LAN"),12));
            for(final Actor a:s.actors) {
                LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setBackgroundColor(a.id%2==0?0x336f8192:0x22556473);
                row.addView(text(a.name,14),new LinearLayout.LayoutParams(0,d(44),1));
                Button view=button(tr("View","Профиль"),new View.OnClickListener(){public void onClick(View v){profile(a);}});row.addView(view,new LinearLayout.LayoutParams(d(76),d(34)));
                if(a.id!=s.you){final Button add=button(actions.isFriend(a.name)?tr("Added","Добавлен"):tr("Add Friend","В друзья"),null);add.setEnabled(!actions.isFriend(a.name));add.setOnClickListener(new View.OnClickListener(){public void onClick(View v){actions.addFriend(a.name);add.setText(tr("Added","Добавлен"));add.setEnabled(false);}});row.addView(add,new LinearLayout.LayoutParams(d(90),d(34)));}
                content.addView(row,new LinearLayout.LayoutParams(-1,d(46)));
            }
        } else if(tab==1) {
            content.addView(text(tr("Frame rate","Частота кадров"),16));LinearLayout rate=new LinearLayout(getContext());
            for(final int value:new int[]{20,30,60}){Button b=button(value+" FPS"+(actions.fps()==value?"  •":""),new View.OnClickListener(){public void onClick(View v){actions.fps(value);page(1);}});cell(rate,b,1);}content.addView(rate,new LinearLayout.LayoutParams(-1,d(45)));
            CheckBox studs=new CheckBox(getContext());studs.setText(tr("Classic surface studs","Классические выступы на деталях"));studs.setTextColor(Color.WHITE);studs.setChecked(actions.studs());studs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){public void onCheckedChanged(CompoundButton b,boolean checked){actions.studs(checked);}});content.addView(studs);
            content.addView(text(tr("AI and language settings are available in the main menu before starting a session.","Настройки AI и языка доступны в главном меню перед запуском сессии."),13));
        } else {
            content.addView(text(tr("Move: left thumbstick\nCamera: swipe empty space\nJump: round arrow on the right\nClimb: push towards a ladder; pull back to descend\nChat: the Chat button\n\nThe LAN game continues while this menu is open.","Движение: джойстик слева\nКамера: свайп по свободному месту\nПрыжок: круглая стрелка справа\nЛестница: двигайтесь к ней; назад — спуск\nОбщение: кнопка Chat\n\nLAN-сессия продолжается, пока это меню открыто."),15));
        }
        cell(footer,button(tr("Reset Character","Сбросить персонажа"),new View.OnClickListener(){public void onClick(View v){confirm(false);}}),1);
        cell(footer,button(tr("Leave Game","Выйти из игры"),new View.OnClickListener(){public void onClick(View v){confirm(true);}}),1);
        cell(footer,button(tr("Resume Game","Продолжить"),new View.OnClickListener(){public void onClick(View v){dismiss();}}),1);
    }
    private void profile(Actor a){content.removeAllViews();content.addView(text(a.name,24));content.addView(text(tr("Health: ","Здоровье: ")+Math.max(0,(int)a.health)+"\n"+tr("Current session player","Игрок текущей сессии"),16));content.addView(button(tr("Back to players","Назад к игрокам"),new View.OnClickListener(){public void onClick(View v){page(0);}}));}
    private void confirm(final boolean leave) {
        content.removeAllViews();footer.removeAllViews();
        content.addView(text(leave?tr("Are you sure you want to leave the game?","Вы действительно хотите выйти из игры?"):tr("Reset your character?","Сбросить персонажа?"),22));
        if(leave)content.addView(text(tr("If this device hosts the game, its LAN session will end for everyone.","Если это устройство — хост, LAN-сессия завершится для всех игроков."),14));
        cell(footer,button(tr("Cancel","Отмена"),new View.OnClickListener(){public void onClick(View v){page(selected);}}),1);
        cell(footer,button(leave?tr("Leave","Выйти"):tr("Reset","Сбросить"),new View.OnClickListener(){public void onClick(View v){dismiss();if(leave)actions.leave();else actions.reset();}}),1);
    }
}
