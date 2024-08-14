package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * No special treatment.
 * Drag events are queued in the Host event queue and will be serially processed
 * 
 * {@link EventControllerOneThreadCtrled} for the Event Queue.
 * 
 * @author Charles Bentley
 *
 */
public class EventControllerOneThread extends EventController {

   public EventControllerOneThread(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);

   }

   public void event(BEvent ev) {
      boolean isAccepted = super.event(inputState, ev, canvas);
      if (isAccepted) {
         ExecutionContextCanvas ec = canvas.createExecutionContextCanvas();
         ec.setInputState(inputState);
         ec.setOutputState(outputState);
         canvas.processInputState(ec, inputState, outputState);
      }
   }

   /**
    * Called in the render thread.
    * <br>
    * Send queued Dragged
    */
   public void paintFinished() {
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, EventControllerOneThread.class, 63);
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, EventControllerOneThread.class, 63);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug

}
