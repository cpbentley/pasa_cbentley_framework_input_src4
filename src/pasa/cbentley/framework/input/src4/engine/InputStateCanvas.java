package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.listdoublelink.LinkedListDouble;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.event.RepeatEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.event.jobs.BaseJob;
import pasa.cbentley.framework.input.src4.event.jobs.JobsEventRunner;
import pasa.cbentley.framework.input.src4.event.jobs.RepeatJob;
import pasa.cbentley.framework.input.src4.game.FrameData;
import pasa.cbentley.framework.input.src4.gesture.GestureDetector;

public class InputStateCanvas extends InputState {

   private FrameData           frameData;

   /**
    * Pointer Gesture is set to null, when requested upon release.
    */
   protected GestureDetector[] gestures = new GestureDetector[3];

   protected final InputCtx    ic;

   /**
    * Previous requests still valid.
    * Never null
    */
   private final InputRequests inputRequests;

   private float interpolation;

   protected InputStateCanvas(InputCtx ic, CanvasAppliInput canvas) {
      super(ic.getCUC());
      this.ic = ic;

      inputRequests = new InputRequests(ic, canvas, this);

   }



   public CanvasAppliInput getCanvasAppli() {
      return (CanvasAppliInput) eventCanvas;
   }

   /**
    * Retursn the current
    * @return
    */
   public FrameData getFrameData() {
      return frameData;
   }

   /**
    * 
    * @return
    */
   public InputRequests getInputRequestNew() {
      return inputRequests;
   }

   /**
    * Keeps track the Bentley framework level which Host event producer
    * is running.
    * <br>
    * When Application framework doesn't need a gesture type anymore,
    * it switches off the service at the host level.
    * <br>
    * Returns the object to request Gesture behaviors.
    * <br>
    * Repeats a InputState event until some conditions occur. (a key is released)
    * <br>
    * @return
    */
   public InputRequests getInputRequestRoot() {
      return inputRequests;
   }

   public float getInterpolation() {
      return interpolation;
   }

   /**
    * Look up active {@link RepeatJob} with src object as source.
    * @param src
    * @return
    */
   public RepeatJob getRepeatJob(Object src) {
      CanvasAppliInput eventCanvas = getCanvasAppli();
      JobsEventRunner runner = eventCanvas.getEventRunner();
      //you need to sync on the list
      synchronized (runner) {
         LinkedListDouble list = runner.getList();
         BaseJob bj = (BaseJob) list.getHead();
         while (bj != null) {
            if (bj instanceof RepeatJob) {
               RepeatEvent re = ((RepeatJob) bj).getRepeat();
               if (re.getSource() == src) {
                  return (RepeatJob) bj;
               }
            }
            bj = (BaseJob) bj.getNext();
         }
      }
      return null;
   }

   /**
    * Uses the {@link JobsEventRunner}
    * @param re
    * @return
    */
   public RepeatJob getRepeatJob(RepeatEvent re) {
      //#debug
      toDLog().pEvent("Trying to find RepeatJob for ", re, InputState.class, "getRepeatJob", LVL_05_FINE, false);
      CanvasAppliInput eventCanvas = getCanvasAppli();
      JobsEventRunner runner = eventCanvas.getEventRunner();
      //you need to sync on the list
      synchronized (runner) {
         LinkedListDouble list = runner.getList();
         BaseJob bj = (BaseJob) list.getHead();
         while (bj != null) {
            if (bj instanceof RepeatJob) {
               RepeatEvent rev = ((RepeatJob) bj).getRepeat();
               if (rev == re) {
                  //#debug
                  toDLog().pEvent("Found RepeatJob", bj, InputState.class, "getRepeatJob", LVL_05_FINE, true);
                  return (RepeatJob) bj;
               }
            }
            bj = (BaseJob) bj.getNext();
         }
      }
      return null;
   }

   /**
    * Get active repeat jobs linked to a Device (keyboard. mouse. 
    * @param src
    * @return
    */
   public RepeatJob[] getRepeatJobs(Object src) {
      //TODO
      return null;
   }

   public boolean hasInputRequests() {
      return inputRequests != null;
   }

   protected void newEventCancelerSub(BEvent be) {
      //if event is accepted event updates active NUples
      inputRequests.newEvent(be);
   }

   public void requestRepeat(RepeatEvent er) {
      inputRequests.requestRepeat(er);
   }

   public void setFrameData(FrameData frameData) {
      this.frameData = frameData;
   }

   public void setInterpolation(float interpol) {
      interpolation = interpol;
   }

   /**
    * Make sure current thread is the thread
    */
   public void toLogThreadCheck() {
      boolean isThread = true;
      if (!isThread) {
         CanvasAppliInput eventCanvas = getCanvasAppli();
         //#debug
         toDLog().pEvent(Thread.currentThread() + " != " + eventCanvas.getEventThread(), null, InputState.class, "toLogThreadCheck");
         throw new IllegalThreadStateException();
      }
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, InputStateCanvas.class, 150);
      toStringPrivate(dc);
      super.toString(dc.sup());

      dc.nlLvl(inputRequests, "inputRequests");
      dc.appendVarWithSpace("Interpolation", StringUtils.prettyFloat(interpolation));
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, InputStateCanvas.class, 150);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug





}
