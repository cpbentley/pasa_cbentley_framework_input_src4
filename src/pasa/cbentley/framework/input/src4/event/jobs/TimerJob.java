package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * Used to generates Long Press and Repeat Events.
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class TimerJob extends BaseJob {


   public TimerJob(InputCtx ic, JobsEventRunner run) {
      super(ic,run);
      // TODO Auto-generated constructor stub
   }

   public int getTiming() {
      // TODO Auto-generated method stub
      return 0;
   }

   public boolean isNewEventCanceling(InputState is) {
      // TODO Auto-generated method stub
      return false;
   }

   public void launch() {
      // TODO Auto-generated method stub

   }

   public void run() {
      // TODO Auto-generated method stub

   }

   public BEvent getEventCancel() {
      // TODO Auto-generated method stub
      return null;
   }

}
