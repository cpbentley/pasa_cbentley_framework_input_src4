package pasa.cbentley.framework.input.src4;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.structs.FiFoQueue;
import pasa.cbentley.framework.input.src4.ctx.IFlagsToStringInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.ITechInput;
import pasa.cbentley.framework.input.src4.interfaces.ITechInputCycle;
import pasa.cbentley.framework.input.src4.interfaces.IScreenResults;

/**
 * Manages the {@link CanvasResult} and repaint mechanics for a {@link CanvasAppliInput}
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class RepaintCtrl implements IStringable, ITechInputCycle {
   /**
    * 
    */
   protected CanvasResult     canvasResultBusiness;

   /**
    * The {@link CanvasAppliInput} on which this repaint manages.
    */
   protected CanvasAppliInput canvas;

   /**
    * Match current thread against this object to know if code is executing
    * in the thread of
    * <li> {@link CanvasAppliInput#keyPressed(int)}
    * <li> {@link CanvasAppliInput#keyReleased(int)}
    * <li> {@link CanvasAppliInput#pointerPressed(int, int, int)}
    * <li> {@link CanvasAppliInput#pointerReleased(int, int, int)}
    * 
    */
   protected Thread           eventThread;

   /**
    * Applicative State:
    * This enables Controller to know if a Business ScreenResult process is active
    * in the same time as a Event.
    */
   protected boolean          isEventThreadState;

   /**
    * We must protect thread access because GUI request {@link CanvasResult} from it
    * an
    */
   private FiFoQueue          queue;

   /**
    * Reserve of {@link CanvasResult}.
    * <br>
    * Reset with {@link CanvasResult#resetAll()}
    */
   private FiFoQueue          queueSR;

   protected FiFoQueue        runUpdates;

   /**
    * Base {@link CanvasResult}.
    * <br>
    * When active, Controller creates a new one.
    * 
    */
   protected CanvasResult     canvasResultEvent;

   protected final InputCtx   ic;

   /**
    * T
    * @param canvas
    */
   public RepaintCtrl(InputCtx ic, CanvasAppliInput canvas) {
      this.ic = ic;
      this.canvas = canvas;
      UCtx uc = ic.getUCtx();
      queue = new FiFoQueue(uc);
      queueSR = new FiFoQueue(uc);
      if(this.getClass() == RepaintCtrl.class) {
         constructHelpers();
      }
   }
   
   public void constructHelpers() {
      canvasResultEvent = create(CYCLE_0_USER_EVENT);
      canvasResultBusiness = create(CYCLE_1_BUSINESS_EVENT);
   }

   /**
    * Use default one or create a new one.
    * <br>
    * How do we make sure it has been resetted?
    */
   void attachScreenResult() {
      CanvasResult sr = canvasResultEvent;
      if (canvasResultEvent.hasResultFlag(IScreenResults.FLAG_06_ACTIVE)) {
         //use another
         sr = new CanvasResult(ic, canvas, CYCLE_0_USER_EVENT);
      } else {
         sr.resetAll();
      }
   }

   public void clearResult(CanvasResult sr) {
      sr.resetAll();
      queue.put(sr);
   }

   /**
    * Any business thread with an effect create a {@link CanvasResult} here.
    * 
    * @param id
    * @return
    */
   public CanvasResult create(int id) {
      return new CanvasResult(ic, canvas, id);
   }

   public void endEvent() {
      isEventThreadState = false;
      eventThread = null;
   }

   protected CanvasResult getEmptySR(int type) {
      synchronized (queueSR) {
         CanvasResult sr = (CanvasResult) queueSR.getHead();
         if (sr == null) {
            sr = create(type);
         } else {
            sr.setType(type);
         }
         return sr;
      }
   }

   public CanvasResult getExternResult() {
      //repaint call originates from the outside of the Bentley Framework
      //#debug
      canvas.toDLog().pDraw("Repaint Call From Outside : Null ScreenResult", null, RepaintCtrl.class, "getExternResult");

      CanvasResult sr = getEmptySR(CYCLE_0_USER_EVENT);
      //do a full repaint according to framework semantics
      sr.setRepaintFlag(ITechInput.REPAINT_01_FULL, true);
      sr.setRepaintFlag(ITechInput.REPAINT_02_EXTERNAL, true);
      return sr;
   }

   /**
    * Merge all pending Results into 1 to be used by the thread {@link ITechInput#THREAD_2_RENDER}
    * <br>
    * Never returns null.
    * @return
    */
   public synchronized CanvasResult getNextRender() {
      synchronized (queue) {
         //reads pending repaints
         CanvasResult sr = (CanvasResult) queue.getHead();
         CanvasResult s = null;
         while ((s = (CanvasResult) queue.getHead()) != null) {
            sr.merge(s);
         }
         //if none.. still null.
         if (sr == null) {
            //create one
            sr = getExternResult();
         }
         //debug stuff
         if (canvas.hasDebugFlag(IFlagsToStringInput.Debug_8_ForceFullRepaints)) {
            sr.setRepaintFlag(ITechInput.REPAINT_01_FULL, true);
         }
         if (sr == null) {
            throw new NullPointerException();
         }
         //sr.setInputState(ic);
         return sr;
      }
   }

   /**
    * Returns the {@link CanvasResult} inside the Event Thread (Keys Pointers)
    * Returns the Event {@link CanvasResult} if one event is currently being processed.
    * <br>
    * Calling it outside will generate an {@link IllegalStateException} exception.
    * Return a business {@link CanvasResult} otherwise.
    * @return
    */
   public CanvasResult getScreenResult() {

      if (isEventThreadState) {
         return canvasResultEvent;
      } else {
         return canvasResultBusiness;
      }
   }

   /**
    * ScreenResult for logic/business threads.
    * <br>
    * They know for certain they are running inside a business thread
    * @return
    */
   public CanvasResult getScreenResultBusi() {
      return canvasResultBusiness;
   }

   /**
    * Tells if an Event is dealt with by the EventThread.
    * @return
    */
   public boolean isEventThread() {
      return Thread.currentThread() == eventThread;
   }

   public boolean isEventThreadState() {
      return isEventThreadState;
   }

   /**
    * Queues the {@link CanvasResult} for repaint.
    * <br>
    * If there are several requests? 
    * What happens depends on the threading mode.
    * <br>
    * In a ASAP queue single thread, all jobs are processed as soon.
    * <br>
    * In single rendering thread, they all will be merged at the start of the next rendering
    * loop.
    * <br>
    * <br>
    * The request is dropped when it doesn't bring 
    * @param sr
    */
   public void queueRepaint(CanvasResult sr) {
      queue.put(sr);
   }

   /**
    * This method will block until the {@link CanvasResult} has been honored.
    * <br>
    * This method MUST be called outisde the rendering thread.
    * 
    * @param sr
    * @throws InterruptedException if the calling thread is interrupted.
    * <br>
    * for instance all animations are stopped.
    */
   public void queueRepaintBlock(CanvasResult sr) throws InterruptedException {
      //we lock on the semaphore. very unlikely but this prevent the repaint from fully completing
      //between repaint and semaphore release
      synchronized (sr.sema) {
         queue.put(sr);
         //send repaint event // wake up dormant render thread
         canvas.repaintAfterUIEvent();
         //blocks
         sr.sema.acquire();
      }
   }

   /**
    * Calls for a repaint of the screen according to the {@link CanvasResult} data.
    * <br>
    * <br>
    * In Active Rendering mode, the {@link CanvasResult} is merged to Active one.
    * In Active mode, call this may notify the rendering thread if it is sleeping.
    * Rendering thread may decide to sleep because no work is needed.
    * <br>
    * <br>
    * Adds a {@link CanvasResult} controls what has to be drawn.
    * <br>
    * We must match the paint call to all {@link CanvasResult} originators
    * <br>
    * When Event and Business collide:
    * <br>
    * Called from 
    * <li> Animation Thread
    * <li> Business Thread
    * <li> Event Thread
    * <br>
    * <br>
    * What happens if many different threads call this method at the same time? How do we make sure, the repaint
    * is coherent, and a request isn't lost.
    * ie. first request gets registered, second request gets registered at the ScreenResult, then repaint occurs,
    * then 2nd repaint call is made without its ScreenResult.
    * <br>
    * <br> 
    * @param sr
    */
   public void repaint(CanvasResult sr) {
      //call the repaint method
      if (sr.hasRepaintFlag(ITechInput.REPAINT_01_FULL)) {
         canvas.repaintHiJack();
      } else {
         //#debug
         //canvas.toLog().ptFlow("Repaint Clip[x,y=" + sr.clipx + "," + sr.clipy + " w,h=" + sr.clipw + "," + sr.cliph + "]", null, RepaintCtrl.class, "repaint");

         //problem with this is that the debug area will not be repainted.
         //so when DEBUG, ignore clip matching
         //the clip values are relative to 0,0 of the Virtual Canvas.
         canvas.repaint(sr.clipx, sr.clipy, sr.clipw, sr.cliph);
      }
   }

   private CanvasResult rotate() {
      synchronized (queue) {
         CanvasResult sr = (CanvasResult) queue.getHead();
         if (sr == null) {
            sr = create(CYCLE_1_BUSINESS_EVENT);
            queue.put(sr);
         }
         return sr;
      }
   }

   /**
    * After calling this method a new fresh {@link CanvasResult} will be referenced
    * 
    */
   public void rotateBusinessResult() {
      canvasResultBusiness = rotate();
   }

   public void rotateGUIResult() {
      canvasResultEvent = rotate();
   }

   /**
    * Returns the {@link CanvasResult} to be used to give feedback of this event
    */
   public CanvasResult startEvent() {
      isEventThreadState = true;
      eventThread = Thread.currentThread();
      CanvasResult sr = getEmptySR(CYCLE_0_USER_EVENT);
      //set the type

      return sr;
   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "RepaintCtrl");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "RepaintCtrl");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUCtx();
   }

   //#enddebug

}
