package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.structs.listdoublelink.ListElement;
import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ToStringStaticInput;

public abstract class BaseJob extends ListElement implements Runnable, IStringable {

   private JobsEventRunner  lock;

   private int              state;

   /**
    * time elapsed since the last job was executed
    */
   protected int            timeAcc;

   /**
    * Accumulates all the time. Cannot be modified by subclasses
    */
   private int              timeFull;

   /**
    * The time elapsed since the job was started
    */
   protected int            timeTotal;

   protected final InputCtx ic;

   /**
    * The time elapsed since the job was started
    * @return
    */
   public int getTimeTotal() {
      return timeTotal;
   }

   public BaseJob(InputCtx ic, JobsEventRunner run) {
      super(run.getList());
      this.ic = ic;
      this.lock = run;
   }

   /**
    * API call to cancel the job
    */
   public void cancelMe() {
      lock.cancelJob(this);
   }

   /**
    * Returns the lock object on which to synchronize to access BaseJob state
    * inside the Run method.
    * @return
    */
   public Object getLock() {
      return lock;
   }

   /**
    * <li> {@link ITechInputJob#JOB_STATE_1_RUN} if it is time to active
    * <li> {@link ITechInputJob#JOB_STATE_1_RUN} if it is time to active
    * <li> {@link ITechInputJob#JOB_STATE_2_CANCELED} if the job must be canceld
    * @return
    */
   public int getState() {
      return state;
   }

   /**
    * Returns the current time delay before the next event occurence.
    * <br>
    * Called in the {@link JobsEventRunner} thread. This means this method
    * must synchronize to access state outside {@link BaseJob}.
    * <br>
    * @return
    */
   public abstract int getTiming();

   /**
    * Returns true if cancel job
    * Returns
    * @param is
    */
   public abstract boolean isNewEventCanceling(InputState is);

   /**
    * Called in the {@link JobsEventRunner} thread when time returned {@link BaseJob#getTiming()} has elapsed.
    * <br>
    * <br>
    * The implementation must update its state and readies the {@link BEvent}.
    * <br>
    * Creates the event to be consumed by the {@link BaseJob#run()}.
    * <br>
    * <br>
    * May Flag has last and set to cancel 
    * @return
    */
   public abstract void launch();

   /**
    * Run the Job. Calling thread is the UI thread. or thread safe.
    * <br>
    * <br>
    * Implementation cannot read {@link BaseJob} state. It is not synchronized.
    * <br>
    * <br>
    * 
    */
   public abstract void run();

   /**
    * Create a {@link BEvent} for the cancelation
    * @return
    */
   public abstract BEvent getEventCancel();

   public void setState(int state) {
      this.state = state;
   }

   /**
    * Returns the time in millis until the Job must be activated.
    * Negative values means the job is late executing if activate
    * <br>
    * Accumulates time
    * @param delta
    * @return
    */
   public int tickTimeDelta(int delta) {
      timeAcc += delta;
      timeTotal += delta;
      int timeCheck = getTiming(); //return current timeout for a run
      //#debug
      toDLog().pEvent1("delta=" + delta + " timeCheck=" + timeCheck + " timeAcc=" + timeAcc, null, BaseJob.class, "tickTimeDelta");
      int diff = timeAcc - timeCheck;
      if (diff >= 0) {
         timeAcc = diff;
         //enough time has passed. time to fire the job
         setState(ITechInputJob.JOB_STATE_1_RUN);
         //next event will happen as soon as possible
         return 0;
      } else {
         return 0 - diff;
      }
   }

   /**
    * Return the time accumulated since the last event
    * @return
    */
   public int getTimeAccumulated() {
      return timeAcc;
   }

   /**
    * False by default. Subclass to true if a cancel event needs to be fired.
    * 
    * Should only be called locally
    * @return
    */
   boolean cancel() {
      return false;
   }

   public boolean isSendFinalized() {
      return false;
   }

   //#mdebug

   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public UCtx toStringGetUCtx() {
      return ic.getUC();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "BaseJob");
      dc.appendVarWithSpace("State", ToStringStaticInput.toStringJobState(state));
      dc.appendVarWithSpace("TimeElapsed", timeAcc);
      dc.appendVarWithSpace("TimeAcc", timeTotal);

   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "BaseJob");
      dc.appendVarWithSpace("TimeElapsed", timeAcc);
      dc.appendVarWithSpace("State", ToStringStaticInput.toStringJobState(state));
      dc.appendVarWithSpace("TimeAcc", timeTotal);
   }
   //#enddebug

}
