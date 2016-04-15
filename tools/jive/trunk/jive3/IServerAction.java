package jive3;

/**
 * Interface used by the Create Server Dialog
 */
public interface IServerAction {
  public void doJob(String server,String className,String[] devices);
}
