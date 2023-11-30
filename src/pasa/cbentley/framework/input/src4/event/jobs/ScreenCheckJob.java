package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;


/**
 * Check if screenconfig has changed.
 * 
 * There will only be one such job running at the same time.
 * 
 * When a screen change occurs, it fires itself.. but keep running.
 * @author Charles Bentley
 *
 */
public class ScreenCheckJob extends BaseJob {

   public ScreenCheckJob(InputCtx ic,JobsEventRunner run) {
      super(ic,run);
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
