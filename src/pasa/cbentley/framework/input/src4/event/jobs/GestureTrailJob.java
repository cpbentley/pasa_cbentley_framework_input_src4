package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coreui.src4.event.GestureEvent;
import pasa.cbentley.framework.coreui.src4.event.GesturePointer;
import pasa.cbentley.framework.coreui.src4.event.RepeatEvent;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.IJobEvent;

/**
 * Job created when a {@link GesturePointer} is completed and should keep generating events until conditions are met.
 * <br>
 * <li>Scrolling :Fling the screen to scroll and scrolling continues for a few seconds or until
 * a press is recorded.
 * <li>
 * <br>
 * <br>
 * <b>How to use </b>?
 * <br>
 * Used to repeat a fling event for instance.
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class GestureTrailJob extends RepeatJob implements IJobEvent {

   /**
    * The {@link GesturePointer}
    */
   GestureEvent             g           = null;

   /**
    * Initialized by constructor
    */
   protected GesturePointer gp;

   private int              pointerID;

   private int              timeDefault = 200;

   public GestureTrailJob(InputCtx ic,CanvasAppliInput ctrl, GesturePointer gp, RepeatEvent re) {
      super(ic,ctrl, re);
      this.gp = gp;
   }

   public int getPointerID() {
      return pointerID;
   }

   public int getTiming() {
      return timeDefault;
   }

   public boolean isNewEventCanceling(InputState is) {
      //TODO maybe another key can cancel the trail?
      //when the trail offers
      if (is.isPointerButton0Pressed(pointerID)) {
         //notify ? code may want to animate back to new state
         //or code may want to .. we need a callback anyways for the cancel.
         //ideally it should be done before current event...
         return true;
      }
      return false;
   }

   public void launch() {
      setState(ITechInputJob.JOB_STATE_0_WAITING);
   }

   public void run() {
      canvas.event(er);
   }

   public void setTimingDefault(int time) {
      this.timeDefault = time;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "GestureTrail");
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "GestureTrail");
      super.toString1Line(dc);
   }
   //#enddebug
}
