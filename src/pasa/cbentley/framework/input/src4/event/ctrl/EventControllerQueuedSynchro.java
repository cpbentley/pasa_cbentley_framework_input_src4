package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.FiFoQueue;
import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.gesture.GestureDetector;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * 
 * @author Charles Bentley
 *
 */
public class EventControllerQueuedSynchro extends EventController {

   /**
    * {@link InputState} queue. Consumed by the update thread as discrete events.
    * <br>
    * <li> {@link ITechThreadPaint#THREAD_0_HOST_HUI} writes host events
    * <li> {@link GestureDetector} events
    * <li> Drag events
    * <li> Repeat events
    */
   private BlockingQueueUnlimited inputQueue;

   /**
    * Queue of {@link BEvent}.
    */
   private FiFoQueue              inputQueueFresh;


   public EventControllerQueuedSynchro(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      inputQueue = new BlockingQueueUnlimited(ic.getUC());
      inputQueueFresh = new FiFoQueue(ic.getUC());
   }

   private void checkThread() {
      if (!canvas.isThreadEvent()) {
         throw new IllegalArgumentException();
      }
   }

   /**
    * Called by the update thread.
    * <br>
    * When only one event and no other event was generated, use this state 
    * as the basis for next
    */
   public synchronized void endOfExecutionEvent(ExecutionContextCanvas ec, InputStateCanvas is) {
   }

   public synchronized void event(BEvent event) {
      //#debug
      toDLog().pFlow("Enqueued", event, EventControllerQueuedSynchro.class, "event@62", LVL_04_FINER, DEV_0_1LINE_THREAD);

      inputQueueFresh.put(event);

   }

   public BlockingQueueUnlimited getQeue() {
      return inputQueue;
   }

   /**
    * Look up if queue is not empty
    */
   public synchronized InputStateCanvas getInputState() {
      //#debug
      toDLog().pFlow("numInQueue="+inputQueueFresh.size(), this, EventControllerQueuedSynchro.class, "getState@62", LVL_04_FINER, DEV_0_1LINE_THREAD);

      Object ev = inputQueueFresh.getHead();
      if (ev != null) {
         BEvent event = (BEvent) ev;
         boolean isAccepted = super.event(this.inputState, event, this.canvas);
         //we don't care here
      }
      return inputState;
   }

   public void setQueue(BlockingQueueUnlimited queue) {
      inputQueue = queue;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, EventControllerQueuedSynchro.class, 140);
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, EventControllerQueuedSynchro.class, 140);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug

}
