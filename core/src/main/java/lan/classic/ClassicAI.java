package lan.classic;
import java.util.*;
/** No model allocations in CLASSIC mode. One shared service for every actor. */
public final class ClassicAI {
    private final Map<String,String[]> words=new HashMap<String,String[]>();
    public ClassicAI(){
        words.put("en",new String[]{"hi","ok","up!","I'm here","by the tower"});
        words.put("ru",new String[]{"привет","ок","наверх!","я тут","у башни"});
        words.put("de",new String[]{"hallo","ok","nach oben!","hier","am Turm"});
        words.put("es",new String[]{"hola","vale","arriba!","aquí","en la torre"});
        words.put("fr",new String[]{"salut","ok","en haut !","ici","à la tour"});
        words.put("pt",new String[]{"oi","ok","para cima!","aqui","na torre"});
        words.put("pl",new String[]{"cześć","ok","na górę!","tutaj","przy wieży"});
    }
    public boolean enabled=true;
    private String word(Actor a,int i){String[] w=words.get(a.language);return (w==null?words.get("en"):w)[i];}
    public void event(World w,Actor a,String event){if(enabled&&w.time>=a.nextChat){w.say(a.name,word(a,2));a.nextChat=w.time+12+a.id*2;}}
    public void respond(World w,Actor p,String message){
        String s=message.toLowerCase(Locale.ROOT);int count=0;
        for(Actor a:w.actors.values())if(a.bot){
            boolean follow=contains(s,"follow","за мной","suis-moi","sígueme","folge","siga","za mną");
            boolean up=contains(s,"наверх","цунами","вода","flood","up","tsunami","arriba","oben","haut","cima","górę");
            int response=contains(s,"привет","hi","hello","hallo","hola","salut","oi","cześć")?0:contains(s,"где","where","wo","dónde","où","onde","gdzie")?4:3;
            if(follow){a.followId=p.id;a.goal="FOLLOW_PLAYER";response=1;}else if(up){a.followId=-1;a.goal="FIND_HIGHER_POSITION";response=2;}
            a.remember(p.name+": "+message);
            if(enabled&&w.time>=a.nextChat&&count++<1){w.say(a.name,word(a,response));a.nextChat=w.time+8;}
        }
    }
    private boolean contains(String s,String... terms){for(String t:terms)if(s.contains(t))return true;return false;}
}
