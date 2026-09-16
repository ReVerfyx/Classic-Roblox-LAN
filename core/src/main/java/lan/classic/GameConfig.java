package lan.classic;
public final class GameConfig {
    public enum Mode { DISASTERS, SANDBOX, SWORD_FIGHT, ROCKET_ARENA }
    public Mode mode=Mode.DISASTERS;
    public int difficulty=1,chatFrequency=1,aiHz=5;
    public boolean chat=true,individualLanguages;
    /** Imported map scripts are disabled for ordinary clients by default. */
    public boolean trustedScripts=false;
    public int luaInstructionBudget=20000;
    public String language="ru";
    public long seed=System.nanoTime();
    public String forcedDisaster="";
}
