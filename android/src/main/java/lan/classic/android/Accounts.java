package lan.classic.android;
import android.content.*;
import android.util.Base64;
import java.security.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/** Device-local identity. Credentials are never sent over LAN. */
final class Accounts {
    private final SharedPreferences prefs;
    Accounts(Context c){prefs=c.getSharedPreferences("accounts",Context.MODE_PRIVATE);}
    boolean exists(String user){return prefs.contains(user+".hash");}
    private byte[] derive(char[] password,byte[] salt)throws Exception{PBEKeySpec spec=new PBEKeySpec(password,salt,120000,256);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();}finally{spec.clearPassword();}}
    boolean login(String user,char[] password)throws Exception{if(!exists(user))return false;byte[] salt=Base64.decode(prefs.getString(user+".salt",""),Base64.NO_WRAP);return MessageDigest.isEqual(derive(password,salt),Base64.decode(prefs.getString(user+".hash",""),Base64.NO_WRAP));}
    void register(String user,char[] password)throws Exception{if(exists(user))throw new Exception("Username exists");byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);byte[] hash=derive(password,salt);if(!prefs.edit().putString(user+".salt",Base64.encodeToString(salt,Base64.NO_WRAP)).putString(user+".hash",Base64.encodeToString(hash,Base64.NO_WRAP)).putString(user+".id",java.util.UUID.randomUUID().toString()).commit())throw new Exception("Could not save account");}
}
