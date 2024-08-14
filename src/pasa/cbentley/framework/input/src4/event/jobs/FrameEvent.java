package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.framework.core.ui.src4.ctx.CoreUiCtx;
import pasa.cbentley.framework.core.ui.src4.event.RepeatEvent;
import pasa.cbentley.framework.input.src4.game.FrameData;

/**
 * Timer event with {@link FrameData}
 * 
 * @author Charles Bentley
 *
 */
public class FrameEvent extends RepeatEvent {

   private FrameData frame;
   
   public FrameEvent(CoreUiCtx cac) {
      super(cac);
   }

}
