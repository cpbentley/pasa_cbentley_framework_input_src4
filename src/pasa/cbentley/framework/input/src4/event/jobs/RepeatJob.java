package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.event.RepeatEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.core.ui.src4.tech.ITechInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.interfaces.IJobEvent;

/**
 * Repeats of a class of events.
 * <br>
 * Several pattern of repeat
 * <li> average wait, 100, 100, 100
 * <li> long waits
 * <li> tiny waits until long
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class RepeatJob extends TimerJob implements IJobEvent {

   /**
    * 
    */
   protected CanvasAppliInput canvas;

   /**
    * Does not include pinging time
    */
   private int                currentEventTimeOut;

   /**
    * The same object being repeatedly fired until the job is done.
    */
   protected RepeatEvent      er;

   private boolean            isPinging;

   /**
    * 
    * @param canvas
    * @param er
    */
   public RepeatJob(InputCtx ic, CanvasAppliInput canvas, RepeatEvent er) {
      super(ic, canvas.getEventRunner());
      this.er = er;
      //set starting state
      isPinging = er.isUsePinging();
      this.canvas = canvas;
      currentEventTimeOut = er.getTimeout();
   }

   /**
    * Return true when {@link RepeatJob} must send event as a cancel event
    */
   boolean cancel() {
      //describe if event is canceled early... finished. intermediary
      er.setCanceled();
      if (er.isSendCancelEvent()) {
         return true;
      } else {
         return false;
      }
   }

   public boolean isSendFinalized() {
      return er.isSendCancelEvent();
   }

   public RepeatEvent getRepeat() {
      return er;
   }

   /**
    * 3 possibilites
    */
   public int getTiming() {
      if (isPinging) {
         //minimum between ping time and time remaining until event
         int ping = er.getPingMillis();
         int remaining = currentEventTimeOut - timeTotal;
         //#debug
         toDLog().pEvent1(ping + " " + remaining + " timeTotal=" + timeTotal, null, RepeatJob.class, "getTiming");
         if (remaining < 0) {
            remaining = 0;
         }
         return Math.min(ping, remaining);
      } else {
         return currentEventTimeOut;
      }
   }

   /**
    * The {@link RepeatEvent} has {@link RepeatEvent#checkCancelers(BEvent)}
    * <br>
    * The usual case of a key release canceling the repeat of the key presses. 
    * <br>
    * However we don't send a Repeat Cancel Event.
    * In this case, the release event is enough.
    */
   public boolean isNewEventCanceling(InputState is) {
      //check if new event cancels.. ask the group of events associated with this job
      return er.checkCancelers(is.getEventCurrent());
   }

   public void launch() {
      if (isPinging) {
         //check if long has reach
         int diff = timeTotal - currentEventTimeOut;
         if (diff >= 0) {
            nextRun();
            //resets 
            timeTotal = diff;
            er.setRepeatState(RepeatEvent.REPEAT_STATE_0_NORMAL);
         } else {
            er.setRepeatState(RepeatEvent.REPEAT_STATE_2_PINGING);
         }
         er.setPingAccu(timeTotal);
      } else {
         nextRun();
      }
   }

   private void nextRun() {
      int val = er.incrementSyncCount();
      if (er.getRepeatType() != ITechInput.REPEAT_0_INFINITE && val >= er.getTarget()) {
         er.setCanceled();
         setState(ITechInputJob.JOB_STATE_3_FINALIZED);
      }
      currentEventTimeOut = er.getTimeout();
      //#debug
      toDLog().pEvent1("#" + val + " nextTimeout=" + currentEventTimeOut, this, RepeatJob.class, "nextRun");
   }

   public void run() {
      //the state of er is not thread safe... must use sync method on shared state.
      canvas.getEventController().event(er);
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, RepeatJob.class, 146);
      dc.appendVarWithSpace("currentEventTimeOut", currentEventTimeOut);
      dc.appendVarWithSpace("isPinging", isPinging);
      super.toString(dc.sup());
      dc.nlLvl(er);

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "RepeatJob");
      super.toString1Line(dc);
   }
   //#enddebug
}
