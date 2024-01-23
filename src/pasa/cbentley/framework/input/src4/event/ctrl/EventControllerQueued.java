package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.FiFoQueue;
import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.EventController;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.gesture.GestureDetector;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

public class EventControllerQueued extends EventController {

   /**
    * {@link InputState} queue. Consumed by the update thread as discrete events.
    * <br>
    * <li> {@link ITechPaintThread#THREAD_0_HOST_HUI} writes host events
    * <li> {@link GestureDetector} events
    * <li> Drag events
    * <li> Repeat events
    */
   private BlockingQueueUnlimited inputQueue;

   /**
    * Used InputState are queued
    */
   private FiFoQueue              inputQueueFresh;

   protected InputState           lastKnownState;

   protected InputState           lastKnownStateAvailable;

   public EventControllerQueued(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      lastKnownState = canvas.createInputState();
      inputQueue = new BlockingQueueUnlimited(ic.getUCtx());
      inputQueueFresh = new FiFoQueue(ic.getUCtx());
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
   public synchronized void endOfEvent(InputState is) {
      if (lastKnownState == is) {
         lastKnownStateAvailable = is;
      } else {
         //dump inputstate
         inputQueueFresh.put(is);
      }

   }

   public void event(BEvent g, CanvasAppliInput canvas) {
      InputState is = getNextInputState();
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
   synchronized InputState getNextInputState() {
      if (lastKnownStateAvailable != null) {
         InputState is = lastKnownStateAvailable;
         lastKnownStateAvailable = null;
         return is;
      } else {
         //get next from queue take a copy of the previous
         InputState is = (InputState) inputQueueFresh.getHead();
         if (is == null) {
            is = canvas.createInputState();
         }
         is.cloneFrom(lastKnownState);
         return is;
      }
   }

   public BlockingQueueUnlimited getQeue() {
      return inputQueue;
   }

   public InputState getState() {
      return lastKnownState;
   }

   public void setLastKnownState(InputState is) {
      this.lastKnownState = is;
   }

   //#mdebug

   public void setQueue(BlockingQueueUnlimited queue) {
      inputQueue = queue;
   }

   public void toString(Dctx dc) {
      dc.root(this, "EventControllerQeued");
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "EventControllerQeued");
   }
   //#enddebug
}
