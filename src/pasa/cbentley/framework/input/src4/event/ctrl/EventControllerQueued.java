package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.FiFoQueue;
import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.gesture.GestureDetector;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * 
 * @author Charles Bentley
 *
 */
public class EventControllerQueued extends EventController {

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
    * Used InputState are queued
    */
   private FiFoQueue              inputQueueFresh;

   protected InputStateCanvas           lastKnownStateAvailable;

   public EventControllerQueued(InputCtx ic, CanvasAppliInput canvas) {
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
   public synchronized void endOfEvent(InputStateCanvas is) {
      if (inputState == is) {
         lastKnownStateAvailable = is;
      } else {
         //dump inputstate
         inputQueueFresh.put(is);
      }

   }

   public void event(BEvent g) {
      //#debug
      toDLog().pFlow("msg", this, EventControllerQueued.class, "getNextInputState@85", LVL_04_FINER, DEV_X_ONELINE_THREAD);

      InputStateCanvas is = getNextInputState();
      boolean isAccepted = super.event(is, g, canvas);
      if (isAccepted) {
         //get next inputstate
         inputQueue.enqueue(is);
         setLastKnownState(is);
      }
   }

   /**
    * Called in the UI thread
    * @return
    */
   private synchronized InputStateCanvas getNextInputState() {
      //#debug
      toDLog().pFlow("msg", this, EventControllerQueued.class, "getNextInputState@85", LVL_04_FINER, DEV_X_ONELINE_THREAD);
      if (lastKnownStateAvailable != null) {
         InputStateCanvas is = lastKnownStateAvailable;
         lastKnownStateAvailable = null;
         return is;
      } else {
         //get next from queue take a copy of the previous
         InputStateCanvas is = (InputStateCanvas) inputQueueFresh.getHead();
         if (is == null) {
            is = (InputStateCanvas) canvas.createInputState();
         }
         is.cloneFrom(inputState);
         return is;
      }
   }

   public BlockingQueueUnlimited getQeue() {
      return inputQueue;
   }

   public void setLastKnownState(InputStateCanvas is) {
      this.inputState = is;
   }

   public void setQueue(BlockingQueueUnlimited queue) {
      inputQueue = queue;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, EventControllerQueued.class, 140);
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, EventControllerQueued.class, 140);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug

}
