package pasa.cbentley.framework.input.src4;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.interfaces.C;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.structs.FiFoQueue;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.structs.synch.MutexSignal;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.coreui.src4.exec.ExecutionContext;
import pasa.cbentley.framework.coreui.src4.interfaces.IActionFeedback;
import pasa.cbentley.framework.coreui.src4.tech.ITechInputFeedback;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;
import pasa.cbentley.framework.input.src4.interfaces.ITechInputCycle;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;
import pasa.cbentley.framework.input.src4.interfaces.ITechScreenResults;

/**
 * Used by {@link InputState} to coalesce the {@link CanvasAppliInput} consequences of executing code.
 * <br>
 * When a GUI event travels through several classes, this class allows us to track
 * whether or not a repaint has already been called, what areas are to be repainted,
 * what kind of actions code has already done relative to the original event.
 * <br>
 *  
 * Simple
 * Result of a 
 * <li> {@link MCmd} execution 
 * <br>
 * <br>
 * Implements the {@link IActionFeedback}
 * 
 * @author Charles-Philip Bentley
 *
 */
public class CanvasResult extends ObjectIC implements ITechInputFeedback, ITechScreenResults, IStringable {

   /**
    * Short Strings describing the actions that were done
    */
   private IntToStrings       actionStrs;

   public int                 cliph;

   public int                 clipw;

   /**
    * The x coordinate of the repaint rectangle.
    */
   public int                 clipx;

   public int                 clipy;

   protected CanvasAppliInput ctrl;

   /**
    * Identifies type
    * <li> {@link CanvasAppliInput#CYCLE_0_USER_EVENT}
    * <li> {@link CanvasAppliInput#CYCLE_1_BUSINESS_EVENT}
    * <li> {@link CanvasAppliInput#CYCLE_2_ANIMATION_EVENT}
    * <li> {@link CanvasAppliInput#CYCLE_3_MERGED}
    * 
    */
   protected int              cycleContext;

   /**
    * This field is null or not. But there can only be one {@link ExecutionContext}
    * 
    * A new Execution context must wait before the previous one is processed.
    */
   private ExecutionContext   exctx;

   private InputState         inputState;

   private float              interpolation;

   private CanvasResult       linked;

   /**
    * TODO deals with multiple locks. use semaphore
    */
   protected Object           lock;

   /**
    * 
    */
   protected int              repaintFlags;

   /**
    * 
    */
   int                        resultFlags;

   /**
    * Run tasks to be run in the render thread before the rendering frame
    * <br>
    */
   protected FiFoQueue        runUpdates;

   /**
    * The message to be print on the foreground in the next repaint cycle.
    * The message will stay paint for a number of milliseconds
    */
   protected String           screenMessage;

   protected int              screenMessageAnchor;

   protected int              screenMessageTimeOut;

   MutexSignal                sema;

   private volatile boolean   waiters = false;

   /**
    * 
    * @param ic
    * @param ctrl
    * @param cycle
    */
   public CanvasResult(InputCtx ic, CanvasAppliInput ctrl, int cycle) {
      super(ic);
      UCtx uc = ic.getUC();
      sema = new MutexSignal(uc);
      runUpdates = new FiFoQueue(uc);
      actionStrs = new IntToStrings(uc);
      this.ctrl = ctrl;
      cycleContext = cycle;
      if (cycleContext == ITechInputCycle.CYCLE_2_ANIMATION_EVENT) {
         lock = new Integer(0);
         setFlag(ITechScreenResults.FLAG_15_LOCK, true);
      }
      resetAll();
   }

   public void actionDone() {
      actionDone(null, FLAG_01_ACTION_DONE);
   }

   public void actionDone(Object o, int type) {
      if (o == null) {
         setActionDoneRepaint(BitUtils.hasFlag(type, FLAG_01_ACTION_DONE), BitUtils.hasFlag(type, FLAG_02_FULL_REPAINT));
      }
   }

   /**
    * Will woke up next time
    * @throws InterruptedException
    */
   public void addPainterWait() throws InterruptedException {
      if (!waiters)
         waiters = true;
      synchronized (sema) {
         sema.acquire();
      }
   }

   public void addRun(Runnable d) {
      runUpdates.put(d);
   }

   /**
    * Will not be added if it is already there
    * @param actionStr debug string of the action
    */
   public void debugSetActionDoneRepaint(String actionStr) {
      setActionString(actionStr);
      setActionDoneRepaint();
   }

   public IntToStrings getActionStrs() {
      return actionStrs;
   }

   public ExecutionContext getExecCtx() {
      return exctx;
   }

   /**
    * The inputstate at the time of the 
    * @return
    */
   InputState getInput() {
      return inputState;
   }

   public float getInterpolation() {
      return interpolation;
   }

   public Object getLock() {
      return lock;
   }

   public String getMessage() {
      return screenMessage;
   }

   public int getMessageAnchor() {
      return screenMessageAnchor;
   }

   public int getMessageTimeOut() {
      return screenMessageTimeOut;
   }

   public int getRepaintFlag() {
      return repaintFlags;
   }

   /**
    * Test the repaint flags according to {@link IMaster} protocol.
    * <br>
    * <br>
    * False if repaint flag has not yet been build.
    * <br>
    * <li> {@link ITechPaintThread#REPAINT_01_FULL}
    * <li> {@link ITechPaintThread#REPAINT_01_FULL}
    * 
    * @param flag
    * @return
    */
   public boolean hasRepaintFlag(int flag) {
      return BitUtils.hasFlag(repaintFlags, flag);
   }

   /**
    * Test flags from {@link CanvasResult}.
    * <li> {@link IActionFeedback#FLAG_01_ACTION_DONE}
    * <li> {@link IActionFeedback#FLAG_02_FULL_REPAINT}
    * <li> {@link IActionFeedback#FLAG_03_MENU_REPAINT}
    * <li> {@link IActionFeedback#FLAG_04_RENEW_LAYOUT}
    * <li> {@link IActionFeedback#FLAG_05_USER_MESSAGE}
    * <br>
    * <br>
    * etc
    * <br>
    * 
    * @param flag
    * @return
    */
   public boolean hasResultFlag(int flag) {
      return BitUtils.hasFlag(resultFlags, flag);
   }

   /**
    * True when an Action was flagged on the {@link CanvasResult}.
    * <br>
    * @return
    */
   public boolean isActionDone() {
      return hasResultFlag(FLAG_01_ACTION_DONE);
   }

   /**
    * What about translations?
    * <br>
    * <br>
    * Also clip is not checked when showing the Debug Header
    * <br>
    * <br>
    * @param g
    * @return
    */
   public boolean isClipMatch(IGraphics g) {
      if (clipx != g.getClipX())
         return false;
      if (clipy != g.getClipY())
         return false;
      if (clipw != g.getClipWidth())
         return false;
      if (cliph != g.getClipHeight())
         return false;
      return true;
   }

   /**
    * Builds/Coalesce screen result into a single clip and repaint flags.
    * <br>
    * <br>
    * Returns True if a repaint is needed.
    * <br>
    * The main advandtage to build the clip is to detect conflicting repaints from outside the framework.
    * Partial repaints are not safe, while a full repaint is always safe, but time consuming.
    * <br>
    * <br>
    * TODO When repainting a {@link Drawable} and its header, what mecanism prevent the header from being drawn twice?
    * <br>
    * Clipping area is compute when clipping check is enabled {@link CanvasAppliInput#IS_CLIPPING_CHECK}
    * <br>
    * @return
    */
   public boolean isRepaint() {
      return this.resultFlags != 0;
   }

   public void merge(CanvasResult sr) {
      //simplest is when there is a full repaint.
      if (sr.hasResultFlag(FLAG_02_FULL_REPAINT)) {
         this.setFlag(FLAG_02_FULL_REPAINT, true);
      }
      if (sr.hasResultFlag(FLAG_05_USER_MESSAGE)) {
         this.screenMessage = sr.screenMessage;
         this.screenMessageTimeOut = sr.screenMessageTimeOut;
      }
      //at least one lock is requested
      if (sr.hasResultFlag(ITechScreenResults.FLAG_15_LOCK)) {
         this.setFlag(ITechScreenResults.FLAG_15_LOCK, true);
      }
      linked = sr;
      mergeSub(sr);
   }

   public void mergeSub(CanvasResult sr) {

   }

   /**
    * Notifies paint lock and sets {@link ITechScreenResults#FLAG_06_ACTIVE} to false.
    * <br>
    * <br>
    * 
    */
   public void paintEnd() {
      setFlag(ITechScreenResults.FLAG_06_ACTIVE, false);
      //
      if (hasResultFlag(FLAG_15_SCREEN_LOCK)) {
         CanvasResult sr = this;
         do {
            //release
            if (sr.hasResultFlag(FLAG_15_SCREEN_LOCK)) {
               sr.sema.releaseAll();
            }
            sr = sr.linked;
         } while (sr != null);
      }
      if (lock != null) {
         //if (Controller.getMe().isLockingAnimation) {
         synchronized (lock) {
            //#debug
            toDLog().pFlow("Notifying Lock " + this.hashCode() + " " + Thread.currentThread().getName(), null, CanvasResult.class, "paintEnd");
            //paint thread
            lock.notifyAll();
         }
         //}
      }
   }

   /**
    * Resets all {@link CanvasResult} properties except cycle
    * <li>Reset Clip
    * <li> Zero flags and repaint flags
    * <li> Zero action strings
    * <li>Iterate over stored {@link Drawable} that were repainted and remove {@link IViewDrawable#VIEWSTATE_02_REPAINTING_CONTENT}
    */
   public void resetAll() {
      clipx = 0;
      clipy = 0;
      clipw = ctrl.getWidth();
      cliph = ctrl.getHeight();
      resultFlags = 0;
      repaintFlags = 0;
      actionStrs.nextempty = 0; //reset action strings
      screenMessage = null;
      screenMessageAnchor = 0;
      screenMessageTimeOut = 0;
   }

   public void setActionDone() {
      setFlag(FLAG_01_ACTION_DONE, true);
   }

   /**
    * Flags config for full repaint of all canvases and action done.
    * <br>
    * <br>
    * Normally, only one action may be generated by event.
    * <br>
    * However when UP and DOWN are pressed together, they may generated 2 action done on the same loop
    */
   public void setActionDoneRepaint() {
      setFlag(FLAG_01_ACTION_DONE, true);
      setFlag(FLAG_02_FULL_REPAINT, true);
   }

   public void setActionDoneRepaint(boolean done, boolean repaint) {
      setFlag(FLAG_01_ACTION_DONE, done);
      setFlag(FLAG_02_FULL_REPAINT, repaint);
   }

   public void setActionDoneRepaint(int x, int y, int w, int h) {
      if (hasRepaintFlag(FLAG_01_ACTION_DONE)) {
         //union of clip
         int[] v = new int[4];
         v = ic.getUC().getGeo2dUtils().getUnion(clipx, clipy, clipw, cliph, x, y, w, h, v);
         clipx = v[0];
         clipy = v[1];
         clipw = v[2];
         cliph = v[3];
      } else {
         clipx = x;
         clipy = y;
         clipw = w;
         cliph = h;
      }

   }

   /**
    * Special repaint that simply display a message on screen
    * <br>
    * <br>
    * TODO this must be a System CMD resulting from event
    * @param string
    */
   public void setActionDoneRepaintMessage(String string) {
      setActionDoneRepaintMessage(string, 0);
   }

   /**
    * Draws a string message over the virtual canvas. 
    * <br>
    * <br>
    * without repainting component during the next painting cycle.
    * Message will not be repainted at next repaint 
    * Optimally, it should be called when processing a user key event
    * Method must be called from the User Event Thread. 
    *     * A non zero time out will display the message for x milliseconds
    * A Message repaint will be generated and only the message will erased 
    * and background is repainted.
    * Performance will depend on the Paint Mode
    * @param str
    */
   public void setActionDoneRepaintMessage(String string, int timeout) {
      this.setActionDoneRepaintMessage(string, timeout, C.ANC_4_CENTER_CENTER);
   }

   /**
    * <li> {@link C#ANC_4_CENTER_CENTER}
    * <li> {@link C#ANC_2_TOP_RIGHT}
    * <li> {@link C#ANC_6_BOT_LEFT}
    * 
    * @param string
    * @param timeout
    * @param pos 
    */
   public void setActionDoneRepaintMessage(String string, int timeout, int anchor) {
      screenMessageTimeOut = timeout;
      screenMessage = string;
      screenMessageAnchor = anchor;
      setFlag(FLAG_01_ACTION_DONE, true);
      setFlag(FLAG_02_FULL_REPAINT, true);
   }

   /**
    * Adds the action String
    * @param actionStr
    */
   public void setActionString(String actionStr) {
      if (actionStr == null)
         return;
      if (actionStrs.getFirstStringIndex(actionStr) == -1) {
         actionStrs.add(actionStr);
      }
   }

   public void setExCtx(ExecutionContext exd) {
      this.exctx = exd;
   }

   public void setFlag(int flag, boolean v) {
      resultFlags = BitUtils.setFlag(resultFlags, flag, v);
   }

   public void setFullRepaint() {
      setFlag(FLAG_02_FULL_REPAINT, true);
   }

   public void setInputState(InputState ic) {
      this.inputState = ic;
   }

   public void setInterpolation(float interpol) {
      interpolation = interpol;
   }

   /**
    * Set descriptive flags.
    * <br>
    * The paint process will check those flags to adapt its technique.
    * <br>
    * 
    * @param flag
    * @param v
    */
   public void setRepaintFlag(int flag, boolean v) {
      repaintFlags = BitUtils.setFlag(repaintFlags, flag, v);
   }

   public void setType(int type) {
      cycleContext = type;
   }


   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, CanvasResult.class, 530);
      toStringPrivate(dc);
      super.toString(dc.sup());

      dc.nl();
      if (repaintFlags == 0) {
         dc.append("RepaintsFlags:NONE");
      } else {
         dc.append("RepaintsFlags:");
         if (hasRepaintFlag(ITechPaintThread.REPAINT_01_FULL)) {
            dc.append("Full");
         }
         if (hasRepaintFlag(ITechPaintThread.REPAINT_02_EXTERNAL)) {
            dc.append("External");
         }
      }
      dc.nl();
      dc.append("ResultFlags");
      if (hasResultFlag(FLAG_01_ACTION_DONE)) {
         dc.appendWithSpace("ActionDone");
      }
      if (hasResultFlag(FLAG_02_FULL_REPAINT)) {
         dc.appendWithSpace("FullRepaint");
      }
      if (hasResultFlag(FLAG_04_RENEW_LAYOUT)) {
         dc.appendWithSpace("RenewLayout");
      }

      dc.nl();
      toStringActionString1Line(dc);
      dc.nl();
      dc.append("Clip:[" + clipx + "," + clipy + " " + clipw + "," + cliph + "]");
      dc.nlLvl("Link", linked);
      dc.appendVarWithSpace("Message", screenMessage);
      dc.appendVarWithSpace("Anchor", screenMessage);
      dc.appendVarWithSpace("Interpolation", StringUtils.prettyFloat(interpolation));
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, CanvasResult.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());

   }

   public void toStringActionString1Line(Dctx sb) {
      for (int i = 0; i < actionStrs.nextempty; i++) {
         sb.append(" [");
         sb.append(actionStrs.strings[i]);
         sb.append("]");
      }
   }


   public String toStringClip(IGraphics g) {
      Dctx d = new Dctx(inputState.toStringGetUCtx());
      toStringClip(g, d);
      return d.toString();
   }

   public void toStringClip(IGraphics g, Dctx sb) {
      sb.append("SR:[" + clipx + "," + clipy + " " + clipw + "," + cliph + "]");
      sb.append(" Graphics:[" + g.getClipX() + "," + g.getClipY() + " " + g.getClipWidth() + "," + g.getClipHeight() + "]");
   }

   private void toStringPrivate(Dctx dc) {

   }

   public String toStringRepaintFlags() {
      return "";
   }
   //#enddebug

}
