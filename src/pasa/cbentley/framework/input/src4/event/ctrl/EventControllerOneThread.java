package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.EventController;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.ITechInput;

/**
 * No special treatment.
 * <br>
 * Drag events are queued in the Host event queue and will be serially processed
 * <br>
 * <br>
 * {@link EventControllerOneThreadCtrled} for the Event Queue.
 * @author Charles Bentley
 *
 */
public class EventControllerOneThread extends EventController {
   /**
    * The current {@link InputState}. or the image of the whole queue.
    * <br>
    * In a single thread there is only a single reference.
    * <br>
    * In Mode {@link ITechInput#THREADING_1_UI_UPDATERENDERING},
    * the UI thread updates the {@link InputState}
    * 
    * <br>
    * Any thread can get a reference from this. But it is not advisable.
    * <br>
    * Depending on the thread model
    */
   protected InputState inputState;

   public EventControllerOneThread(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      inputState = canvas.createInputState();
   }

   public void event(BEvent g, CanvasAppliInput canvas) {
      boolean isAccepted = super.event(inputState, g, canvas);
      if (isAccepted) {
         canvas.processInputState(inputState);
      }
   }

   public InputState getState() {
      return inputState;
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
      dc.root(this, "EventControllerOneThread");
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "EventControllerOneThread");
   }
   //#enddebug
}
