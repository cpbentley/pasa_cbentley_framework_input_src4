package pasa.cbentley.framework.input.src4;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.event.ctrl.EventControllerOneThreadCtrled;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

/**
 * Queue up event from the Host UI thread
 * Controls the dragging events for efficiency in a passive rendering Canvas.
 * <br>
 * This class queues dragging events recieved from the HostUIEventThread. When rendering is finished.
 * <br>
 * <br>
 * This class sends an aggregate of all drag events recieved during the rendering.
 * Otherwise every individual dragging event would be all sent individually each requiring a rendering.
 * <br>
 * <br>
 * Class that tries to alievate the fact we don't have access to the queued events.
 * Painting thus may also be done in another thread when dragging occurs. Painting would return without having painted anything!
 * You would have a repaint to draw just the cached image. <br>
 * So that's basically caching at the top level.<br>
 * <br>
 * <b>Summary</b>
 * <br>
 * Host UI Event Thread {@link ITechPaintThread#THREAD_0_HOST_HUI} calls this method {@link DragController3#addEventDragged(int, int, int)}
 * when a drag event is generated by the host. It queues events until the painting thread is done painting
 * at which time it is notified and sends the Drag event. 
 * <br>
 * <br>
 * How does this class know the Render thread is busy or waiting for a paint job?
 * 
 * <br>
 * <li>First Drag Event is received. {@link DragController3} thread is notified. [Event Thread]
 * <li>Drag Event is processed is {@link DragController3} thread so that Drag Event may enqueue. [Drag Thread]
 * <li>Generating a Repaint
 * <li>Queue Drag events: problem they are enqueue as well [Event Thread]
 * <li>When End of Paint is received, Notified [Event Thread]
 * <li>Wait 10 milliseconds to recieve queued events and take the last before send the Drag Event serially [Drag Thread]
 * <li>If no events, {@link DragController3} goes into wait mode. [Drag Thread]
 * <br>
 * <br>
 * It is always better for a Drawable being dragged to use a cache.
 * <br>
 * This class is used when this is not possible because said {@link Drawable} has dynamic content
 * <br>
 * <br>
 * @author Charles-Philip Bentley
 *
 */
public class DragController3 implements Runnable, IStringable {

   /**
    * Controller is waiting for a drag event to come in.
    */
   private static final int STATE_0_WAITING       = 0;

   /**
    * 
    */
   private static final int STATE_1_SENDING_EVENT = 1;

   /**
    * after the paint, it waits for queue drag events to fill in.
    * <br>
    * Then it takes one event and send it.
    * <br>
    * If no event after the paint, go into {@link DragController3#STATE_0_WAITING}
    */
   private static final int STATE_2_FILLING_QUEUE = 2;

   private static final int WAIT_IDLE_TIME        = 10;

   private static String debugState(int state) {
      switch (state) {
         case STATE_0_WAITING:
            return "Waiting";
         case STATE_1_SENDING_EVENT:
            return "SendingEvent";
         case STATE_2_FILLING_QUEUE:
            return "FillingQueue";
         default:
            return "Unknown";
      }
   }

   /**
    * Number of Drag Event queued.
    */
   private int                            count;

   private EventControllerOneThreadCtrled ctrl;

   protected final InputCtx               ic;

   private int                            id;

   private boolean                        isPress;

   private boolean                        isRelease;

   private int[]                          press   = null;

   private int[]                          release = null;

   private boolean                        run     = true;

   private int                            state;

   private int                            x;

   private int                            y;

   public DragController3(InputCtx ic, EventControllerOneThreadCtrled ctrl) {
      this.ic = ic;
      this.ctrl = ctrl;
   }

   /**
    * Host UI Event Thread {@link ITechPaintThread#THREAD_0_HOST_HUI} calls this method.
    * Drag events are queued and flattened.
    * Press Events and Release Events are always send in order.
    * All events after a release are ignored.
    * @param x
    * @param y
    * @param id
    */
   public synchronized void addEventDragged(int x, int y, int id) {
      if (isPress) {
         this.x = x;
         this.y = y;
         this.id = id;
         this.isRelease = false;
         count++;
         //notify
         if (state == STATE_0_WAITING) {
            notify();
         }
      }
   }

   public synchronized void addEventPress(int x, int y, int id, int button) {
      this.x = x;
      this.y = y;
      this.id = id;
      press = new int[] { x, y, id, button };
      this.isPress = true;
      this.isRelease = false;
      if (state == STATE_0_WAITING) {
         notify();
      }
   }

   public synchronized void addEventRelease(int x, int y, int id, int button) {
      this.x = x;
      this.y = y;
      this.id = id;
      release = new int[] { x, y, id, button };
      this.isPress = false;
      this.isRelease = true;
      if (state == STATE_0_WAITING) {
         notify();
      }
   }

   /**
    * Called by Painting Thread when it has finished. 
    * <br>
    * Always in response to a ScreenResult from a Drag Event
    * <br>
    * Wake up
    */
   public synchronized void paintFinished() {
      if (state == STATE_2_FILLING_QUEUE) {
         //#debug
         toDLog().pFlow("STATE_FILLING QUEUE notifying end of paint after DragEvent", this, DragController3.class, "paintFinished", ITechLvl.LVL_05_FINE, true);
         notify();
      }
   }

   public void paintStart() {
   }

   /**
    * 3 states:
    * <li> {@link DragController3#STATE_0_WAITING} waiting for a drag event to come in
    * <li> {@link DragController3#STATE_1_SENDING_EVENT} sending event
    * <li> {@link DragController3#STATE_2_FILLING_QUEUE} filling queue
    */
   public synchronized void run() {
      while (run) {
         try {
            //System.out.println("#DragControl State=" + state + " Count=" + count);
            switch (state) {
               case STATE_0_WAITING:
                  //
                  wait();
                  setState(STATE_1_SENDING_EVENT);
                  break;
               case STATE_1_SENDING_EVENT:
                  //while sending event in the GUI thread, we wait and enqueue drag events.
                  setState(STATE_2_FILLING_QUEUE);
                  //send event in the event thread
                  sendEvent();
                  //wait for repaint to finish or new drag event to notify
                  wait();
                  break;
               case STATE_2_FILLING_QUEUE:
                  wait(WAIT_IDLE_TIME);
                  if (count > 0 || release != null || press != null) {
                     setState(STATE_1_SENDING_EVENT);
                  } else {
                     setState(STATE_0_WAITING);
                  }
                  break;
               default:
                  break;
            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   private void sendEvent() {
      ic.callSerially(new Runnable() {
         public void run() {
            sendEventMy();
         }
      });
   }

   /**
    * 
    */
   private void sendEventMy() {
      if (press != null) {
         ctrl.pointerPressedCtrl(press[0], press[1], press[2], press[3]);
         press = null;
      } else if (release != null) {
         ctrl.pointerReleasedCtrl(release[0], release[1], release[2], press[3]);
         release = null;
         count = 0; //reset count to zero.
      } else {
         ctrl.pointerDraggedCtrl(x, y, id);
         count = 0;
      }
   }

   private void setState(int state) {
      this.state = state;
   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "DragController3");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "DragController3");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUCtx();
   }

   //#enddebug
   

}
