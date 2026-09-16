package lan.classic;
/** Integration boundary only: no unmeasured native runtime is shipped. */
public interface LLMBackend {
    boolean available(long memoryBudgetBytes);
    String infer(String compactContext,int maxTokens) throws Exception;
    void release();
}
