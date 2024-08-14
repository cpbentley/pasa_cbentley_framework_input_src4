package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.structs.FiFoQueue;
import pasa.cbentley.framework.input.src4.ctx.IFlagsToStringInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;
import pasa.cbentley.framework.input.src4.interfaces.ITechInputCycle;
import pasa.cbentley.framework.input.src4.interfaces.ITechScreenResults;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * Manages the {@link OutputStateCanvas} and repaint mechanics for a {@link CanvasAppliInput}
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class RepaintHelper extends ObjectIC implements IStringable, ITechInputCycle {
   /**
    * 
    */
   protected OutputStateCanvas     canvasResultBusiness;

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
    * We must protect thread access because GUI request {@link OutputStateCanvas} from it
    * an
    */
   private FiFoQueue          queue;

   /**
    * Reserve of {@link OutputStateCanvas}.
    * <br>
    * Reset with {@link OutputStateCanvas#resetAll()}
    */
   private FiFoQueue          queueSR;

   protected FiFoQueue        runUpdates;

   /**
    * Base {@link OutputStateCanvas}.
    * <br>
    * When active, Controller creates a new one.
    * 
    */
   protected OutputStateCanvas     canvasResultEvent;


   /**
    * T
    * @param canvas
    */
   public RepaintHelper(InputCtx ic, CanvasAppliInput canvas) {
      super(ic);
      this.canvas = canvas;
      UCtx uc = ic.getUC();
      queue = new FiFoQueue(uc);
      queueSR = new FiFoQueue(uc);
      if(this.getClass() == RepaintHelper.class) {
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
      OutputStateCanvas sr = canvasResultEvent;
      if (canvasResultEvent.hasResultFlag(ITechScreenResults.FLAG_06_ACTIVE)) {
         //use another
         sr = new OutputStateCanvas(ic, canvas, CYCLE_0_USER_EVENT);
      } else {
         sr.resetAll();
      }
   }

   public void clearResult(OutputStateCanvas sr) {
      sr.resetAll();
      queue.put(sr);
   }

   /**
    * Any business thread with an effect create a {@link OutputStateCanvas} here.
    * 
    * @param id
    * @return
    */
   public OutputStateCanvas create(int id) {
      return new OutputStateCanvas(ic, canvas, id);
   }

   public void endEvent() {
      isEventThreadState = false;
      eventThread = null;
   }

   protected OutputStateCanvas getEmptyOutputState(int type) {
      synchronized (queueSR) {
         OutputStateCanvas sr = (OutputStateCanvas) queueSR.getHead();
         if (sr == null) {
            sr = create(type);
         } else {
            sr.setType(type);
         }
         return sr;
      }
   }

   public OutputStateCanvas getExternResult() {
      //repaint call originates from the outside of the Bentley Framework
      //#debug
      canvas.toDLog().pDraw("Repaint Call From Outside : Null ScreenResult", null, RepaintHelper.class, "getExternResult@145");

      OutputStateCanvas sr = getEmptyOutputState(CYCLE_0_USER_EVENT);
      //do a full repaint according to framework semantics
      sr.setRepaintFlag(ITechThreadPaint.REPAINT_01_FULL, true);
      sr.setRepaintFlag(ITechThreadPaint.REPAINT_02_EXTERNAL, true);
      return sr;
   }

   /**
    * Merge all pending Results into 1 to be used by the thread {@link ITechThreadPaint#THREAD_2_RENDER}
    * <br>
    * Never returns null.
    * @return
    */
   public synchronized OutputStateCanvas getNextRender() {
      synchronized (queue) {
         //reads pending repaints
         OutputStateCanvas sr = (OutputStateCanvas) queue.getHead();
         //if none.. still null.
         if (sr == null) {
            //create one
            sr = getExternResult();
         }
         
         OutputStateCanvas s = null;
         while ((s = (OutputStateCanvas) queue.getHead()) != null) {
            sr.merge(s);
         }
     
         //debug stuff
         if (canvas.hasDebugFlag(IFlagsToStringInput.Debug_8_ForceFullRepaints)) {
            sr.setRepaintFlag(ITechThreadPaint.REPAINT_01_FULL, true);
         }
         if (sr == null) {
            throw new NullPointerException();
         }
         //sr.setInputState(ic);
         return sr;
      }
   }

   /**
    * Returns the {@link OutputStateCanvas} inside the Event Thread (Keys Pointers)
    * Returns the Event {@link OutputStateCanvas} if one event is currently being processed.
    * <br>
    * Calling it outside will generate an {@link IllegalStateException} exception.
    * Return a business {@link OutputStateCanvas} otherwise.
    * @return
    */
   public OutputStateCanvas getScreenResult() {

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
   public OutputStateCanvas getScreenResultBusi() {
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
    * Queues the {@link OutputStateCanvas} for repaint.
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
   public void queueRepaint(OutputStateCanvas sr) {
      queue.put(sr);
   }

   /**
    * This method will block until the {@link OutputStateCanvas} has been honored.
    * <br>
    * This method MUST be called outisde the rendering thread.
    * 
    * @param sr
    * @throws InterruptedException if the calling thread is interrupted.
    * <br>
    * for instance all animations are stopped.
    */
   public void queueRepaintBlock(OutputStateCanvas sr) throws InterruptedException {
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
    * Calls for a repaint of the screen according to the {@link OutputStateCanvas} data.
    * <br>
    * <br>
    * In Active Rendering mode, the {@link OutputStateCanvas} is merged to Active one.
    * In Active mode, call this may notify the rendering thread if it is sleeping.
    * Rendering thread may decide to sleep because no work is needed.
    * <br>
    * <br>
    * Adds a {@link OutputStateCanvas} controls what has to be drawn.
    * <br>
    * We must match the paint call to all {@link OutputStateCanvas} originators
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
   public void repaint(OutputStateCanvas sr) {
      //call the repaint method
      if (sr.hasRepaintFlag(ITechThreadPaint.REPAINT_01_FULL)) {
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

   private OutputStateCanvas rotate() {
      synchronized (queue) {
         OutputStateCanvas sr = (OutputStateCanvas) queue.getHead();
         if (sr == null) {
            sr = create(CYCLE_1_BUSINESS_EVENT);
            queue.put(sr);
         }
         return sr;
      }
   }

   /**
    * After calling this method a new fresh {@link OutputStateCanvas} will be referenced
    * 
    */
   public void rotateBusinessResult() {
      canvasResultBusiness = rotate();
   }

   public void rotateGUIResult() {
      canvasResultEvent = rotate();
   }

   /**
    * Returns the {@link OutputStateCanvas} to be used to give feedback of this event
    */
   public OutputStateCanvas startEvent() {
      isEventThreadState = true;
      eventThread = Thread.currentThread();
      OutputStateCanvas sr = getEmptyOutputState(CYCLE_0_USER_EVENT);
      //set the type

      return sr;
   }


   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, RepaintHelper.class, 350);
      toStringPrivate(dc);
      super.toString(dc.sup());
      
      dc.nlLvl(canvasResultEvent, "canvasResultEvent");
      dc.nlLvl(canvasResultBusiness, "canvasResultBusiness");
      dc.nlLvl(canvas, "canvas");
      dc.nlLvl(queue, "queue");
      dc.nlLvl(queueSR, "queueSR");
      dc.nlLvl(runUpdates, "runUpdates");
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("isEventThreadState", isEventThreadState);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, RepaintHelper.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   


}
