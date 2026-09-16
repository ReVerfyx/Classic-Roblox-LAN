package lan.classic.android;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.Set;

/** Early mobile green-shell navigation, native Views, no online or invented player counts. */
final class ClassicHome extends LinearLayout {
    interface Actions {
        void play(); void host(); void servers(); void settings(); void logout();
        void favorite(boolean value); void friend(String name); void exit();
    }
    private final Actions actions;
    private final boolean ru,recent;
    private boolean favorite;
    private final String user,messages;
    private final Set<String> friends;
    private LinearLayout body,tabs;
    private TextView title;
    private int selected;
    ClassicHome(Context c,String user,boolean ru,boolean recent,boolean favorite,Set<String> friends,String messages,Actions actions) {
        super(c);this.user=user;this.ru=ru;this.recent=recent;this.favorite=favorite;this.friends=friends;this.messages=messages;this.actions=actions;
        setOrientation(VERTICAL);setBackgroundColor(0xffe9e9e9);
        LinearLayout header=new LinearLayout(c);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(d(12),0,d(12),0);header.setBackgroundColor(0xff00b65d);
        TextView logo=text("R",29,Color.WHITE);logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);header.addView(logo,new LayoutParams(d(35),-1));
        title=text("",23,Color.WHITE);header.addView(title,new LayoutParams(0,-1,1));
        Icon search=new Icon(c,5,Color.WHITE);search.setContentDescription(tr("Local servers","Серверы LAN"));search.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.servers();}});header.addView(search,new LayoutParams(d(44),d(44)));
        TextView local=text("LAN",15,Color.WHITE);header.addView(local,new LayoutParams(d(42),-1));addView(header,new LayoutParams(-1,d(54)));
        ScrollView scroll=new ScrollView(c);body=new LinearLayout(c);body.setOrientation(VERTICAL);body.setPadding(d(12),d(10),d(12),d(14));scroll.addView(body);addView(scroll,new LayoutParams(-1,0,1));
        tabs=new LinearLayout(c);tabs.setBackgroundColor(0xfffafafa);addView(tabs,new LayoutParams(-1,d(66)));show(0);
    }
    private int d(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private String tr(String en,String r){return ru?r:en;}
    private TextView text(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(d(4),d(4),d(4),d(4));return t;}
    private void show(final int tab) {
        selected=tab;body.removeAllViews();tabs.removeAllViews();String[] labels={tr("Home","Главная"),tr("Games","Игры"),tr("Friends","Друзья"),tr("Messages","Чат"),tr("More","Ещё")};title.setText(labels[tab]);
        for(int i=0;i<labels.length;i++) {
            final int target=i;LinearLayout item=new LinearLayout(getContext());item.setOrientation(VERTICAL);item.setGravity(Gravity.CENTER);
            int color=i==tab?0xff00b65d:0xff292929;item.addView(new Icon(getContext(),i,color),new LayoutParams(d(32),d(36)));
            TextView label=text(labels[i],10,color);label.setGravity(Gravity.CENTER);item.addView(label,new LayoutParams(-1,d(23)));
            View underline=new View(getContext());underline.setBackgroundColor(i==tab?0xff00b65d:Color.TRANSPARENT);item.addView(underline,new LayoutParams(-1,d(4)));
            item.setContentDescription(labels[i]);item.setOnClickListener(new OnClickListener(){public void onClick(View v){show(target);}});tabs.addView(item,new LayoutParams(0,-1,1));
        }
        if(tab==0) {
            body.addView(text(tr("Hello, ","Привет, ")+user+"!",22,0xff555555));
            section(tr("MY FRIENDS","МОИ ДРУЗЬЯ")+" ("+friends.size()+")",2);friendsPreview();
            section(tr("RECENTLY PLAYED","НЕДАВНИЕ ИГРЫ"),1);
            if(recent)gameCard();else body.addView(text(tr("No games played yet.","Вы ещё не играли."),14,0xff888888));
            section(tr("MY FAVORITES","ИЗБРАННОЕ"),1);if(favorite)gameCard();else body.addView(text(tr("Add Classic Baseplate from Games.","Добавьте Classic Baseplate во вкладке «Игры»."),14,0xff888888));
            if(!recent&&!favorite)gameCard();
        } else if(tab==1) {
            section(tr("LOCAL GAMES","ЛОКАЛЬНЫЕ ИГРЫ"),-1);gameCard();
            section(tr("MULTIPLAYER","СЕТЕВАЯ ИГРА"),-1);
            action(tr("Create LAN game","Создать LAN игру"),new OnClickListener(){public void onClick(View v){actions.host();}});
            action(tr("Local servers / Direct connect","Серверы LAN / Подключение по IP"),new OnClickListener(){public void onClick(View v){actions.servers();}});
        } else if(tab==2) {
            section(tr("MY FRIENDS","МОИ ДРУЗЬЯ")+" ("+friends.size()+")",-1);friendsPreview();
            body.addView(text(tr("Add players from the in-game Players menu. This list is saved on this device.","Добавляйте игроков через игровое меню «Игроки». Список хранится на этом устройстве."),14,0xff777777));
        } else if(tab==3) {
            section(tr("LAST SESSION CHAT","ЧАТ ПОСЛЕДНЕЙ СЕССИИ"),-1);
            TextView history=text(messages.length()==0?tr("No messages yet.","Сообщений пока нет."):messages,16,0xff444444);history.setBackgroundColor(Color.WHITE);body.addView(history);
        } else {
            section(user,-1);
            action(tr("Settings / AI Players","Настройки / AI-игроки"),new OnClickListener(){public void onClick(View v){actions.settings();}});
            action(tr("Log out","Выйти из аккаунта"),new OnClickListener(){public void onClick(View v){actions.logout();}});
            action(tr("Exit application","Закрыть приложение"),new OnClickListener(){public void onClick(View v){actions.exit();}});
            body.addView(text(tr("Independent offline reconstruction. Not an official Roblox client.","Независимая офлайн-реконструкция. Не официальный клиент Roblox."),12,0xff888888));
        }
    }
    private void section(String name,final int target) {
        LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);LayoutParams p=new LayoutParams(-1,d(50));p.topMargin=d(10);body.addView(row,p);
        row.addView(text(name,16,0xff858585),new LayoutParams(0,-1,1));
        if(target>=0){TextView all=text(tr("See All","Все"),14,Color.WHITE);all.setGravity(Gravity.CENTER);all.setBackgroundColor(0xff13b9e5);all.setOnClickListener(new OnClickListener(){public void onClick(View v){show(target);}});row.addView(all,new LayoutParams(d(72),d(34)));}
    }
    private void friendsPreview() {
        if(friends.isEmpty()){TextView empty=text(tr("Your local friends list is empty.","Ваш список друзей пока пуст."),14,0xff888888);empty.setBackgroundColor(Color.WHITE);body.addView(empty);return;}
        LinearLayout row=null;int i=0;
        for(final String name:friends){if(selected==0&&i>=3)break;if(i%3==0){row=new LinearLayout(getContext());row.setBackgroundColor(Color.WHITE);body.addView(row);}LinearLayout tile=new LinearLayout(getContext());tile.setOrientation(VERTICAL);tile.setPadding(d(6),d(6),d(6),d(6));tile.addView(new Preview(getContext(),true),new LayoutParams(-1,d(88)));TextView caption=text(name,12,0xff666666);caption.setGravity(Gravity.CENTER);tile.addView(caption);tile.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.friend(name);}});row.addView(tile,new LayoutParams(0,-2,1));i++;}
    }
    private void gameCard() {
        LinearLayout card=new LinearLayout(getContext());card.setOrientation(VERTICAL);card.setPadding(d(5),d(5),d(5),d(5));card.setBackgroundColor(Color.WHITE);LayoutParams lp=new LayoutParams(-1,-2);lp.bottomMargin=d(10);body.addView(card,lp);
        Preview image=new Preview(getContext(),false);card.addView(image,new LayoutParams(-1,d(138)));card.addView(text("Classic Baseplate",19,0xff292929));card.addView(text(tr("Offline · Flash Flood · R6","Офлайн · Наводнение · R6"),13,0xff929292));
        LinearLayout buttons=new LinearLayout(getContext());card.addView(buttons);TextView play=text(tr("PLAY","ИГРАТЬ"),16,Color.WHITE);play.setGravity(Gravity.CENTER);play.setBackgroundColor(0xff00b65d);play.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.play();}});buttons.addView(play,new LayoutParams(0,d(40),1));
        TextView star=text(favorite?tr("Saved","В избранном"):tr("Favorite","В избранное"),13,0xff2599bf);star.setGravity(Gravity.CENTER);star.setOnClickListener(new OnClickListener(){public void onClick(View v){favorite=!favorite;actions.favorite(favorite);show(selected);}});buttons.addView(star,new LayoutParams(0,d(40),1));image.setOnClickListener(new OnClickListener(){public void onClick(View v){actions.play();}});
    }
    private void action(String name,OnClickListener click){TextView v=text(name,17,0xff333333);v.setBackgroundColor(Color.WHITE);v.setPadding(d(12),0,d(8),0);v.setOnClickListener(click);LayoutParams p=new LayoutParams(-1,d(48));p.bottomMargin=d(8);body.addView(v,p);}
    private static final class Icon extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path path=new Path();private final int kind,color;
        Icon(Context c,int kind,int color){super(c);this.kind=kind;this.color=color;}
        protected void onDraw(Canvas c){c.save();float scale=Math.min(getWidth(),getHeight())/36f;c.translate(getWidth()/2f-16*scale,getHeight()/2f-16*scale);c.scale(scale,scale);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.3f);p.setStrokeJoin(Paint.Join.ROUND);
            if(kind==0){path.reset();path.moveTo(3,14);path.lineTo(16,3);path.lineTo(29,14);path.moveTo(7,12);path.lineTo(7,29);path.lineTo(13,29);path.lineTo(13,21);path.lineTo(20,21);path.lineTo(20,29);path.lineTo(26,29);path.lineTo(26,12);c.drawPath(path,p);}
            else if(kind==1){c.drawRoundRect(new RectF(2,7,30,27),4,4,p);c.drawLine(7,17,15,17,p);c.drawLine(11,13,11,21,p);c.drawCircle(22,14,1,p);c.drawCircle(25,20,1,p);}
            else if(kind==2){c.drawCircle(16,9,6,p);c.drawRoundRect(new RectF(6,17,26,30),5,5,p);}
            else if(kind==3){c.drawRect(3,5,29,24,p);c.drawLine(8,24,8,30,p);c.drawLine(8,30,15,24,p);c.drawLine(8,11,24,11,p);c.drawLine(8,17,24,17,p);}
            else if(kind==4){for(int i=0;i<3;i++)c.drawRect(3+i*11,14,8+i*11,19,p);}
            else {c.drawCircle(13,12,9,p);c.drawLine(20,20,29,29,p);}c.restore();}
    }
    private static final class Preview extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final boolean avatar;
        Preview(Context c,boolean avatar){super(c);this.avatar=avatar;}
        private void rect(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}
        protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();c.save();c.scale(w/320,h/150);
            if(!avatar){rect(c,0,0,320,150,0xff80c8ed);rect(c,0,95,320,55,0xff639145);for(int x=0;x<320;x+=12)for(int y=102;y<150;y+=10)rect(c,x,y,3,2,0xff8db764);rect(c,180,57,116,8,0xffb3b5b9);rect(c,185,65,5,53,0xff91959e);rect(c,279,65,5,53,0xff91959e);rect(c,213,65,3,55,0xffd3d6da);rect(c,235,65,3,55,0xffd3d6da);for(int y=70;y<120;y+=7)rect(c,213,y,25,2,0xffeeeeee);}
            else rect(c,0,0,320,150,0xfffafafa);
            c.translate(avatar?160:95,avatar?25:45);p.setColor(0xfff5cd30);c.drawOval(new RectF(-12,0,12,21),p);rect(c,-18,23,36,34,0xff0d69ac);rect(c,-35,23,15,34,0xfff5cd30);rect(c,20,23,15,34,0xfff5cd30);rect(c,-18,59,16,35,0xffa4bd47);rect(c,2,59,16,35,0xffa4bd47);rect(c,-6,7,2,3,0xff222222);rect(c,5,7,2,3,0xff222222);rect(c,-5,15,11,1,0xff222222);c.restore();}
    }
}
