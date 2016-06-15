package nl.wtcw.vle.datafluo.core.engine;


import nl.wtcw.vle.datafluo.core.flow.Flow;
import nl.wtcw.vle.datafluo.event.*;

public interface Engine<T>{

  
  public String compile(T topology);
  public void run();
  public void stop();
  public void pause();
  public void resume();
  public String getContextId();
  public Flow getFlow();
  public String getStatus();
  public boolean isRunning();
  public boolean isStopped();
  public boolean isPaused();
  //public void addWorkflowStateListener(WorkflowStateListener workflowStateListener);
  //public void removeWorkflowStateListener(WorkflowStateListener workflowStateListener);

}
