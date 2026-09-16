package lan.classic.android;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiManager;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;
import lan.classic.*;
public final class MainActivity extends Activity implements GameView.Session {
    private final Handler handler=new Handler();private Accounts accounts;private android.content.SharedPreferences settings;
    private String user="",language="ru";private int bots=1,fps=30;private boolean aiEnabled,ultraLow,playing;
    private volatile World world;private Net.Host host;private Net.Client client;private int localId;
    private GameView game;private TextView status,players,chat,health;private WifiManager.MulticastLock multicast;
    private LinearLayout menu;private float moveX,moveZ,stickX,stickZ;private int generation;
    private TouchControl joystick;
    public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);accounts=new Accounts(this);settings=getSharedPreferences("settings",MODE_PRIVATE);language=settings.getString("language","ru");
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo();am.getMemoryInfo(info);ultraLow=info.totalMem<=600L*1024*1024||am.getMemoryClass()<=64;fps=settings.getInt("fps",ultraLow?20:30);login(false);
    }
    private String t(String en,String ru){return language.equals("ru")?ru:en;}
    private GradientDrawable panel(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff73777d,0xff393d43});d.setStroke(dp(1),0xffb1b4b9);return d;}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.WHITE);v.setPadding(dp(6),dp(4),dp(6),dp(4));v.setTypeface(Typeface.SANS_SERIF);return v;}
    private Button button(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setBackgroundDrawable(panel());b.setOnClickListener(l);return b;}
    private LinearLayout page(String title){ScrollView sc=new ScrollView(this);sc.setFillViewport(true);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(dp(20),dp(12),dp(20),dp(12));root.setBackgroundColor(0xff777f88);sc.addView(root);TextView logo=label("ROBLOX",40);logo.setTextColor(0xffef3434);logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(logo);root.addView(label(title,16));menu=new LinearLayout(this);menu.setOrientation(LinearLayout.VERTICAL);menu.setPadding(dp(12),dp(8),dp(12),dp(8));menu.setBackgroundDrawable(panel());root.addView(menu,new LinearLayout.LayoutParams(dp(400),-2));setContentView(sc);return menu;}
    private void addButton(LinearLayout root,String s,View.OnClickListener l){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(42));p.topMargin=dp(5);root.addView(button(s,l),p);}
    private EditText field(LinearLayout root,String hint,boolean password){EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(Color.WHITE);e.setHintTextColor(0xffdddddd);e.setHint(hint);e.setInputType(password?InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_CLASS_TEXT);root.addView(e);return e;}
    private LinearLayout loginPage() {
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        FrameLayout frame=new FrameLayout(this);
        frame.addView(new ClassicLoginArt(this,false),new FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column=new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        column.setPadding(dp(26),dp(28),dp(26),dp(20));
        scroll.addView(column,new ScrollView.LayoutParams(-1,-2));
        column.addView(new ClassicLoginArt(this,true),new LinearLayout.LayoutParams(-1,dp(110)));
        LinearLayout form=new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);
        fp.topMargin=dp(18);column.addView(form,fp);
        frame.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        setContentView(frame);
        return form;
    }
    private EditText authField(LinearLayout form,String hint,boolean password) {
        EditText e=new EditText(this);
        e.setSingleLine(true);e.setTextSize(19);e.setTextColor(0xff333333);e.setHintTextColor(0xff969c9f);
        e.setHint(hint);e.setPadding(dp(12),dp(6),dp(12),dp(6));
        e.setInputType(password?InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xfff1f3f3,0xffffffff});
        bg.setCornerRadius(dp(8));bg.setStroke(dp(1),0xff8aafb9);e.setBackgroundDrawable(bg);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.bottomMargin=dp(10);form.addView(e,lp);
        return e;
    }
    private void login(final boolean signup){LinearLayout root=loginPage();final EditText name=authField(root,t("Username","Имя пользователя"),false),pass=authField(root,t("Password","Пароль"),true);final EditText confirm=signup?authField(root,t("Confirm Password","Повторите пароль"),true):null;final TextView error=label("",13);error.setTextColor(0xff8e1717);root.addView(error);
        addButton(root,signup?t("Sign Up","Зарегистрироваться"):t("Login","Войти"),new View.OnClickListener(){public void onClick(final View v){final String n=name.getText().toString().trim();final char[] p=pass.getText().toString().toCharArray();if(!n.matches("[A-Za-z0-9_]{3,20}")||p.length<6){error.setText("Username: 3–20 A–Z, 0–9, _; password: 6+ characters");return;}if(signup&&!pass.getText().toString().equals(confirm.getText().toString())){error.setText("Passwords do not match");return;}v.setEnabled(false);new Thread(new Runnable(){public void run(){String problem=null;try{if(signup)accounts.register(n,p);else if(!accounts.login(n,p))problem="Invalid username or password";}catch(Exception e){problem=e.getMessage();}finally{Arrays.fill(p,'\0');}final String result=problem;handler.post(new Runnable(){public void run(){v.setEnabled(true);if(result==null){user=n;home();}else error.setText(result);}});}},"password").start();}});
        addButton(root,signup?t("Login","Уже есть аккаунт? Войти"):t("Register New Account","Зарегистрировать аккаунт"),new View.OnClickListener(){public void onClick(View v){login(!signup);}});
        Button register=(Button)root.getChildAt(root.getChildCount()-1);
        register.setBackgroundColor(Color.TRANSPARENT);register.setTextColor(0xff24526b);register.setTextSize(17);
    }
    private void home(){playing=false;setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);LinearLayout root=page("Classic LAN • 2012 • milestone 0.1");root.addView(label(user+" — "+(ultraLow?"ULTRA LOW":"NORMAL"),14));
        addButton(root,t("OFFLINE","ИГРАТЬ ОФЛАЙН"),new View.OnClickListener(){public void onClick(View v){start(false,null,Net.PORT);}});
        addButton(root,t("CREATE LAN GAME","СОЗДАТЬ LAN ИГРУ"),new View.OnClickListener(){public void onClick(View v){start(true,null,Net.PORT);}});
        addButton(root,t("LOCAL SERVERS","СЕРВЕРЫ В WI-FI"),new View.OnClickListener(){public void onClick(View v){servers();}});
        addButton(root,t("Settings","Настройки"),new View.OnClickListener(){public void onClick(View v){preferences();}});
        root.addView(label(t("Independent reconstruction. Not an official Roblox client.","Независимая реконструкция. Не официальный клиент Roblox."),11));
    }
    private void preferences(){final LinearLayout root=page(t("Settings","Настройки"));root.addView(label("Language / Язык",14));final String[] codes={"en","ru","de","es","fr","pt","pl"};Spinner langs=new Spinner(this);langs.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"English","Русский","Deutsch","Español","Français","Português","Polski"}));langs.setSelection(Arrays.asList(codes).indexOf(language));root.addView(langs);langs.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int p,long id){language=codes[p];settings.edit().putString("language",language).apply();}});
        final CheckBox ai=new CheckBox(this);ai.setText("Enable AI Players");ai.setTextColor(Color.WHITE);ai.setChecked(aiEnabled);root.addView(ai);ai.setOnClickListener(new View.OnClickListener(){public void onClick(View v){if(!ai.isChecked()){aiEnabled=false;return;}ai.setChecked(false);new AlertDialog.Builder(MainActivity.this).setTitle("WARNING").setMessage("AI Players require additional CPU and memory.\n\nThis feature is not recommended for older devices and may cause lower FPS, longer loading times, increased RAM and battery usage, or crashes on devices with very little memory.").setNegativeButton("Cancel",null).setPositiveButton("Enable Anyway",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int which){aiEnabled=true;ai.setChecked(true);}}).show();}});
        final String[] counts={"1","2","3","4","6","8","12","16"};root.addView(label("Bots — "+(ultraLow?"recommended: 1–2":"maximum: 16"),14));Spinner count=new Spinner(this);count.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,counts));count.setSelection(Arrays.asList(counts).indexOf(String.valueOf(bots)));root.addView(count);final TextView warning=label("",12);root.addView(warning);count.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int p,long id){bots=Integer.parseInt(counts[p]);warning.setText(ultraLow&&bots>2?"Not recommended for this device.":"");}});
        root.addView(label("AI Chat Engine: CLASSIC\nTINY LLM: unavailable — ARMv7 memory benchmarks required",13));
        final String[] rates={"20","30","60"};Spinner rate=new Spinner(this);rate.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,rates));rate.setSelection(Arrays.asList(rates).indexOf(String.valueOf(fps)));root.addView(label("FPS",14));root.addView(rate);rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int p,long id){fps=Integer.parseInt(rates[p]);settings.edit().putInt("fps",fps).apply();}});
        addButton(root,t("Back","Назад"),new View.OnClickListener(){public void onClick(View v){home();}});
    }
    private void servers(){final int request=++generation;final LinearLayout root=page("Local Servers");final TextView info=label(t("Searching Wi-Fi…","Поиск в Wi-Fi…"),14);root.addView(info);final EditText ip=field(root,"IP: 192.168.1.50",false),port=field(root,"Port: 53640",false);port.setText("53640");port.setInputType(InputType.TYPE_CLASS_NUMBER);
        addButton(root,"DIRECT CONNECT",new View.OnClickListener(){public void onClick(View v){try{int p=Integer.parseInt(port.getText().toString());if(p<1||p>65535)throw new NumberFormatException();start(false,ip.getText().toString().trim(),p);}catch(NumberFormatException e){info.setText("Invalid port");}}});
        addButton(root,t("Back","Назад"),new View.OnClickListener(){public void onClick(View v){generation++;home();}});
        acquireMulticast();
        new Thread(new Runnable() {
            public void run() {
                try {
                    final List<String> found = Net.search();
                    handler.post(new Runnable() {
                        public void run() {
                            if (request != generation) return;
                            info.setText(found.isEmpty() ? t("No servers. Check Wi-Fi and router client isolation.", "Серверов нет. Проверь Wi-Fi и изоляцию клиентов роутера.") : "Local Servers");
                            for (String line : found) {
                                final String[] p = line.split("\\|");
                                if (p.length < 7) continue;
                                addButton(root, p[3] + " · " + p[5] + "/" + p[6] + " · PLAY", new View.OnClickListener() {
                                    public void onClick(View v) {
                                        start(false, p[0], Integer.parseInt(p[2]));
                                    }
                                });
                            }
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() {
                            if (request == generation) info.setText(e.toString());
                        }
                    });
                }
            }
        }, "search").start();
    }

    private void acquireMulticast(){if(multicast!=null)return;WifiManager wifi=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);if(wifi!=null){multicast=wifi.createMulticastLock("classic-lan");multicast.setReferenceCounted(false);multicast.acquire();}}
    private void start(boolean lan,String address,int port){generation++;stopSession();try{if(address==null){world=new World();Actor a=world.add(user,false,language);localId=a.id;if(aiEnabled)for(int i=0;i<bots;i++)world.add("Builder"+(i+1),true,language);host=new Net.Host(world,lan,port);if(lan)acquireMulticast();}else client=new Net.Client(address,port,user,language);playing=true;showGame();}catch(Exception e){stopSession();home();new AlertDialog.Builder(this).setTitle("Connection error").setMessage(e.toString()).setPositiveButton("OK",null).show();}}
    private void showGame(){FrameLayout root=new FrameLayout(this);root.setMotionEventSplittingEnabled(true);game=new GameView(this,this);game.fps=fps;game.studs=!ultraLow;root.addView(game);status=label("",13);place(root,status,Gravity.TOP|Gravity.CENTER_HORIZONTAL,340,32,0,0);players=label("",12);players.setBackgroundColor(0x88505050);place(root,players,Gravity.TOP|Gravity.RIGHT,150,180,0,38);chat=label("",12);chat.setBackgroundColor(0x55303030);place(root,chat,Gravity.TOP|Gravity.LEFT,280,160,4,38);health=label("Health: 100",14);health.setTextColor(0xff66ee55);place(root,health,Gravity.BOTTOM|Gravity.RIGHT,150,36,4,4);
        Button back=button("Menu",new View.OnClickListener(){public void onClick(View v){new AlertDialog.Builder(MainActivity.this).setTitle("Menu").setItems(new String[]{"Resume","Leave Game"},new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int i){if(i==1){stopSession();home();}}}).show();}});place(root,back,Gravity.TOP|Gravity.LEFT,76,34,4,2);
        Button speak=button("Chat",new View.OnClickListener(){public void onClick(View v){final EditText e=new EditText(MainActivity.this);e.setSingleLine(true);new AlertDialog.Builder(MainActivity.this).setTitle("Chat").setView(e).setPositiveButton("Send",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int i){String s=e.getText().toString();if(client!=null)client.chat(s);else if(world!=null)world.message(localId,s);}}).setNegativeButton("Cancel",null).show();}});place(root,speak,Gravity.TOP|Gravity.LEFT,76,34,84,2);
        TouchControl.Listener controls=new TouchControl.Listener() {
            public void move(float x,float z){stickX=x;stickZ=z;applyStick(false);}
            public void jump(){applyStick(true);}
        };
        joystick=new TouchControl(this,false,controls);
        place(root,joystick,Gravity.BOTTOM|Gravity.LEFT,122,122,18,24);
        TouchControl jump=new TouchControl(this,true,controls);
        place(root,jump,Gravity.BOTTOM|Gravity.RIGHT,78,78,24,44);
        TextView backpack=label("Backpack   [ empty ]",13);backpack.setGravity(Gravity.CENTER);backpack.setBackgroundDrawable(panel());place(root,backpack,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,210,42,0,4);setContentView(root);handler.post(hud);handler.post(inputTick);
    }
    private void applyStick(boolean jump) {
        double r=Math.toRadians(game==null?0:game.cameraYaw);
        moveX=(float)(stickX*Math.cos(r)+stickZ*Math.sin(r));
        moveZ=(float)(-stickX*Math.sin(r)+stickZ*Math.cos(r));
        input(moveX,moveZ,jump);
    }
    private final Runnable inputTick=new Runnable(){public void run(){if(!playing)return;applyStick(false);handler.postDelayed(this,33);}};
    private void place(FrameLayout root,View v,int gravity,int w,int h,int x,int y){FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(dp(w),dp(h),gravity);if((gravity&Gravity.RIGHT)==Gravity.RIGHT)p.rightMargin=dp(x);else p.leftMargin=dp(x);if((gravity&Gravity.BOTTOM)==Gravity.BOTTOM)p.bottomMargin=dp(y);else p.topMargin=dp(y);root.addView(v,p);}
    private final Runnable hud=new Runnable(){public void run(){if(!playing)return;Net.Snapshot s=snapshot();status.setText("Classic Baseplate  |  "+s.round);String names="Players\n";for(Actor a:s.actors){names+=a.name+"\n";if(a.id==s.you)health.setText("Health: "+Math.max(0,(int)a.health));}players.setText(names);String text="";for(String line:s.chat)text+=line+"\n";if(client!=null&&!client.error.isEmpty())text+=client.error;chat.setText(text);handler.postDelayed(this,200);}};
    public Net.Snapshot snapshot(){Net.Client c=client;if(c!=null)return c.latest;World w=world;return w==null?new Net.Snapshot():Net.snapshot(w,localId);}
    public void input(float x,float z,boolean jump){if(client!=null){client.x=x;client.z=z;client.jump|=jump;}else if(world!=null)world.input(localId,x,z,jump);}
    private void stopSession(){playing=false;handler.removeCallbacks(hud);handler.removeCallbacks(inputTick);stickX=stickZ=moveX=moveZ=0;if(client!=null){client.close();client=null;}if(host!=null){host.close();host=null;}world=null;if(multicast!=null){if(multicast.isHeld())multicast.release();multicast=null;}}
    protected void onPause(){super.onPause();stickX=stickZ=0;if(joystick!=null)joystick.reset();input(0,0,false);if(game!=null)game.onPause();}
    protected void onResume(){super.onResume();if(game!=null&&playing)game.onResume();}
    protected void onDestroy(){generation++;stopSession();super.onDestroy();}
    public void onBackPressed(){generation++;if(playing)stopSession();if(user.length()>0)home();else super.onBackPressed();}
    public void onLowMemory(){super.onLowMemory();if(world!=null)synchronized(world){for(Actor a:world.actors.values())a.memory.clear();}if(game!=null){game.studs=false;game.fps=20;}Toast.makeText(this,"AI cache cleared. Classic mode active.",Toast.LENGTH_LONG).show();}
}
