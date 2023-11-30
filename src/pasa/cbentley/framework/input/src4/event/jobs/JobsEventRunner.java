package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.interfaces.ITimeCtrl;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.structs.listdoublelink.LinkedListDouble;
import pasa.cbentley.core.src4.structs.listdoublelink.ListElement;
import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.DragController3;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.CanvasResult;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ToStringStaticInput;
import pasa.cbentley.framework.input.src4.interfaces.ITechInput;
import pasa.cbentley.framework.input.src4.interfaces.IJobEvent;

/**
 * Run {@link IJobEvent}.
 * <br>
 * Implements a fine grained key repeat event creator for passive Canvas. 
 * <br>
 * <br>
 * Repeats the last {@link InputState} until conditions for the repeat stop.
 * <br>
 * <br>
 * 
 * <li>When the first pressed event is generated, a command might be accomplished.
 * <li>If that commands asks for the InputConfig for it, the repeater will generate repeat event and look for the
 * key release event. 
 * <li>After time intervals a repeat key press event is sent.
 * <li>Again the command must ask for repeater to continue repeat this.
 * <br>
 * <br>
 * Synchronization is tricky.
 * The {@link MCmd} linked to the key press and repeat is thus executed at every tick of the repeater, until
 * the key release event is recieved. That mechanism works for Cabled Mode.
 * <br>
 * <br>
 * <b>Event Thread</b> : why or Why not using: {@link Display#callSerially(Runnable)} ?
 * <br>
 * The repeater may work serially. IN that case, repeat will be dependant on the painting time.
 * When working solo, {@link CanvasResult} are piled up and fired when painting method finishes.
 * <br>
 * In Active Rendering Mode, the repeater is not used.
 * Repeats are done automatically
 * <br>
 * 
 * This thread will be interrupted on pause and created again
 * when Canvas is unpaused.
 * <br>
 * TODO how is this running with {@link ITechInput#THREADING_1_UI_UPDATERENDERING}
 * <br>
 * Don't we want long press to be controlled by update
 * @author Charles-Philip Bentley
 *
 */
public class JobsEventRunner implements Runnable, IStringable {

   private CanvasAppliInput canvas;

   protected final InputCtx ic;

   private LinkedListDouble list;

   private ITimeCtrl        mtimer;

   private volatile int     numJobs;

   private volatile boolean run = true;

   private Thread           runThread;

   private long timeBefore = 0;

   public boolean           waitAgain;

   public JobsEventRunner(InputCtx ic, CanvasAppliInput ctr) {
      this.ic = ic;
      this.canvas = ctr;
      mtimer = ic.getTimeCtrl();
      list = new LinkedListDouble(ic.getUCtx());
   }

   /**
    * Add a JobEvent
    * @param er
    */
   public void addJob(BaseJob er) {
      synchronized (this) {
         //#debug
         toDLog().pEvent1("", er, JobsEventRunner.class, "addJobStart");
         if (runThread == null) {
            run = true;
            startThread();
         }
         er.addToList();
         numJobs++;
         timeBefore = mtimer.getTickMillis();
         this.notify();
      }
      //#debug
      //toDLog().pEvent1(""+Thread.currentThread(), null, JobsEventRunner.class, "addJobEnd");
   }

   /**
    * The {@link BaseJob#cancel()} method is called. The job knows if it wants a cancel event to be fire.
    * @param cJob
    */
   public void cancelJob(BaseJob cJob) {
      //#debug
      toDLog().pEvent1("", cJob, JobsEventRunner.class, "cancelJob");
      //do a launch cancel
      boolean runCancel = cJob.cancel();
      if (runCancel) {
         ic.callSerially(cJob);
      }

      cJob.removeFromList();
      numJobs--;
   }

   /**
    * 
    * @param cJob
    */
   public void finalizeJob(BaseJob cJob) {
      //#debug
      toDLog().pEvent1("", cJob, JobsEventRunner.class, "finalizeJob");
      //send event?
      if (cJob.isSendFinalized()) {
         ic.callSerially(cJob);
      }
      cJob.removeFromList();
      numJobs--;
   }

   /**
    * Returns the list of current jobs
    * @return
    */
   public LinkedListDouble getList() {
      return list;
   }

   /**
    * A new event occured. Run the Cancel Jobs as sub events ?
    * and InputState fall back its current event.
    * Cancel Job is a call back event.. cancel may need which event canceled 
    * <br>
    * This is the rational for using a subEvent VirtualEvent
    * 
    * For example if you want to know if the long press are canceled by move of pointer
    * send a message to 
    * {@link InputState#queuePost(BEvent)}
    * so InputState must have a clean list of {@link BEvent}.
    * 
    * <br>
    * that will be run before current event proceeds
    * Before calling, check volatile if any pending stuff.
    * A new event. check type and cancel pending Jobs. Releases removes Press repeats.
    * Any event cancels a long press.
    * <br>
    * Called in the {@link ITechInput#THREAD_0_HOST_HUI}.
    * <br>
    * @param is
    */
   public void newEvent(InputState is) {
      if (numJobs != 0) {
         synchronized (this) {
            //deletes
            ListElement job = list.getHead();
            while (job != null) {
               ListElement next = job.getNext(); //do this first because we remove current element
               BaseJob cJob = (BaseJob) job;
               boolean cancel = cJob.isNewEventCanceling(is);
               if (cancel) {
                  cancelJob(cJob);
               }
               job = next;
            }
         }
      }
   }

   /**
    * 3 states:
    * <li> {@link DragController3#STATE_0_WAITING} waiting for a drag event to come in
    * <li> {@link DragController3#STATE_1_SENDING_EVENT} sending event
    * <li> {@link DragController3#STATE_2_FILLING_QUEUE} filling queue
    */
   public void run() {
      while (run) {
         try {
            //#debug
            //toDLog().pEvent1("Waiting for Lock", null, JobsEventRunner.class, "run");
            //sync on the lock
            synchronized (this) {
               int nextWait = 5000;
               long time = mtimer.getTickMillis();
               int delta = (int) (time - timeBefore);
               timeBefore = time;

               //#debug
               toDLog().pEvent1("After a delta of " + delta + " ms" + ", looking for " + list.getNumElements() + " jobs", null, JobsEventRunner.class, "run");

               ListElement job = list.getHead();
               while (job != null) {
                  ListElement next = job.getNext();
                  BaseJob cJob = (BaseJob) job;

                  int nextMe = cJob.tickTimeDelta(delta);

                  if (nextMe < nextWait) {
                     nextWait = nextMe;
                  }
                  //#debug
                  toDLog().pEvent1("Job scheduled in " + nextMe + " ms", cJob, JobsEventRunner.class, "run");

                  int state = cJob.getState();

                  //when a job is due to execution.
                  //it is sent in the ui thread
                  //one time jobs are removed from the list

                  if (state == ITechInputJob.JOB_STATE_1_RUN) {
                     runJob(cJob);
                     state = cJob.getState();
                     //#debug
                     toDLog().pEvent1("Job Sent For Run. New State is " + ToStringStaticInput.toStringJobState(state), cJob, JobsEventRunner.class, "run");
                  }
                  //after running it or because of timing, maybe state is cancel or finalized
                  //otherwise the job goes in waiting mode
                  if (state == ITechInputJob.JOB_STATE_2_CANCELED) {
                     cancelJob(cJob);
                  } else if (state == ITechInputJob.JOB_STATE_3_FINALIZED) {
                     finalizeJob(cJob);
                  } else {
                     //job goes in waiting mode
                     cJob.setState(ITechInputJob.JOB_STATE_0_WAITING);
                  }
                  job = next;
               }

               if (list.getHead() == null) {
                  //#debug
                  toDLog().pEvent1("Waiting Infinite", this, JobsEventRunner.class, "run");
                  //wait indefenite
                  this.wait();
               } else {
                  //when zero, waits indefinitely so set it to 10
                  if (nextWait <= 0) {
                     //#debug
                     toDLog().pEvent1("Waiting " + nextWait + " ms", this, JobsEventRunner.class, "run");
                     nextWait = 10;
                  }
                  //#debug
                  toDLog().pEvent1("Waiting " + nextWait + " ms", this, JobsEventRunner.class, "run");
                  this.wait(nextWait);
               }
            }
         } catch (InterruptedException e) {
            //#debug
            toDLog().pEvent("InterruptedException ", this, JobsEventRunner.class, "run");
            //we must exit cleanly
            run = false;
         }
      }

   }

   private void runJob(BaseJob je) {
      je.launch();
      ic.callSerially(je);
   }

   public void startThread() {
      runThread = new Thread(this, "JobsEventRunner");
      runThread.start();
   }

   public synchronized void stop() {
      if (runThread != null) {
         runThread.interrupt();
      }
   }

   public IDLog toDLog() {
      return canvas.toDLog();
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "JobsEventRunner");
      dc.appendVarWithSpace("NumJobs", numJobs);
      dc.appendVarWithSpace("Running", run);
      dc.appendVarWithSpace("Time", timeBefore);
      if (runThread == null) {
         dc.append("Null Thread");
      } else {
         dc.appendVarWithSpace("Thread", runThread.toString());
      }
      ListElement job = list.getHead();
      int count = 0;
      while (job != null) {
         ListElement next = job.getNext();
         BaseJob cJob = (BaseJob) job;
         dc.nlLvl("Job" + count, cJob);
         job = next;
         count++;
      }
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "JobsEventRunner");
      dc.appendVarWithSpace("NumJobs", numJobs);
      dc.appendVarWithSpace("Running", run);
      dc.appendVarWithSpace("Time", timeBefore);
      if (runThread == null) {
         dc.append("Null Thread");
      } else {
         dc.appendVarWithSpace("Thread", runThread.toString());
      }
   }

   public UCtx toStringGetUCtx() {
      return ic.getUCtx();
   }

   //#enddebug
}