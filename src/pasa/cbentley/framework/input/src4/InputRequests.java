package pasa.cbentley.framework.input.src4;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.interfaces.C;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.core.src4.structs.IntToInts;
import pasa.cbentley.core.src4.structs.IntToObjects;
import pasa.cbentley.core.src4.structs.listdoublelink.LinkedListDouble;
import pasa.cbentley.core.src4.structs.listdoublelink.ListElementHolder;
import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.coreui.src4.event.DeviceEventGroup;
import pasa.cbentley.framework.coreui.src4.event.EventKey;
import pasa.cbentley.framework.coreui.src4.event.GestureArea;
import pasa.cbentley.framework.coreui.src4.event.GestureEvent;
import pasa.cbentley.framework.coreui.src4.event.GesturePath;
import pasa.cbentley.framework.coreui.src4.event.GesturePointer;
import pasa.cbentley.framework.coreui.src4.event.RepeatEvent;
import pasa.cbentley.framework.coreui.src4.event.VoiceEvent;
import pasa.cbentley.framework.coreui.src4.interfaces.IHostGestures;
import pasa.cbentley.framework.coreui.src4.tech.ITechGestures;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;
import pasa.cbentley.framework.input.src4.event.NUpleEvent;
import pasa.cbentley.framework.input.src4.event.jobs.BaseJob;
import pasa.cbentley.framework.input.src4.event.jobs.GestureTrailJob;
import pasa.cbentley.framework.input.src4.event.jobs.JobsEventRunner;
import pasa.cbentley.framework.input.src4.event.jobs.RepeatJob;

/**
 * Collects requests for events during the update process of an event. At the end of the update cycle,
 * {@link InputRequests} registers events at the Host or {@link JobsEventRunner} of the {@link CanvasAppliInput} =.
 * <br>
 * <br>
 * Class of {@link InputState} from which application code requests input requests.
 * <br>
 * Requests ranges from
 * <li>Repeating key events, usually for keys
 * <li>Host Gesture such as {@link ITechGestures#GESTURE_FLAG_5_SHAKE}
 * <li> Timer for a long press {@link ITechGestures#GESTURE_TYPE_4_LONG_PRESS}
 * <li> {@link GestureTrailJob}
 * <br>
 * <br>
 * One time requests will be erased 
 * 
 * Requests by running code to the InputState machine.
 * <br>
 * When a key press or pointer press event occurs, the application may want to listen to
 * repetitions or pointer gestures. 
 * <br>
 * When  pointer is pressed, dragging events may generate commands.
 * Is it precise dragging or intent dragging?
 * The Host may have a say in the definition of intent dragging. Because a finger pointer
 * is dependant on the hardware implementation, the vector threshold before dragging intention
 * will vary.
 * While on desktop, we want intent to be very fast
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class InputRequests extends ObjectIC implements IStringable {

   /**
    * index is
    * <li> {@link ITechGestures#GESTURE_TYPE_1_DRAG_SLIDE}
    */
   private IntBuffer[]      actual;

   private CanvasAppliInput canvas;

   private int              dragReleaseGestures;

   /**
    * 
    */
   public RepeatEvent       er;

   private GestureArea      gestureAreaGrid;

   private IntToObjects     gestureBaseJobs;

   private IntToObjects     gestureEvents;

   /**
    * Remove listeners requests.
    * <br>
    * GestureType linked to Request ID
    */
   private IntToInts        gestureNegatives;

   private int              gesturePointer;

   /**
    * Add listeners requests. Valid during the event frame. Cleared.
    * <br>
    * GestureType linked to Request ID
    */
   private IntToInts        gesturePositives;

   private int              gridPath  = -1;


   private InputState       is;

   IntToObjects             itosJob;

   /**
    * List of active {@link NUpleJob}.
    * <br>
    * 
    * every event may disactivate it.
    */
   LinkedListDouble         nupleListActive;

   private IntToObjects     repeats;

   private int              requestID = 1;

   public InputRequests(InputCtx ic, CanvasAppliInput canvas, InputState is) {
      super(ic);
      this.canvas = canvas;
      this.is = is;
      actual = new IntBuffer[ITechGestures.MAX_GESTURE];
      UCtx uc = ic.getUC();
      repeats = new IntToObjects(uc);
      nupleListActive = new LinkedListDouble(uc);
      itosJob = new IntToObjects(uc);
      gesturePositives = new IntToInts(uc);
      gestureNegatives = new IntToInts(uc);
      gestureBaseJobs = new IntToObjects(uc);

      gestureEvents = new IntToObjects(uc);
   }

   private void addIt(int inputType, int requestID) {
      if (actual[inputType] == null) {
         actual[inputType] = new IntBuffer(ic.getUC());
      }
      actual[inputType].addInt(requestID);
   }

   public void addTrail(GestureTrailJob gt) {
      gestureBaseJobs.add(gt);
   }

   public void apply() {
      doPositives();
      doNegatives();
      for (int i = 0; i < gestureBaseJobs.nextempty; i++) {
         BaseJob gt = (BaseJob) gestureBaseJobs.getObjectAtIndex(i);
         canvas.eventRun.addJob(gt);
      }
   }

   /**
    * 
    * @param is
    */
   public void apply(InputState is) {

      apply();
   }

   private void cancelNUples(InputState is, BEvent e) {
      //#debug
      //canvas.toLog().ptEvent1("Active Nuples " + nupleListActive.getNumElements(), e, InputRequests.class, "cancelNuples");

      LinkedListDouble list = nupleListActive;
      ListElementHolder bj = (ListElementHolder) list.getHead();
      while (bj != null) {
         ListElementHolder next = (ListElementHolder) bj.getNext();
         NUpleEvent nuple = (NUpleEvent) bj.getObject();
         boolean isCancel = nuple.isNewEventCanceling(is, e);
         if (isCancel) {
            //#debug
            canvas.toDLog().pEvent1("Cancelling " + nuple.toString1Line(), e, InputRequests.class, "cancelNuples");
            bj.removeFromList();
         }
         bj = next;
      }
   }

   public void clear() {
      er = null;
      dragReleaseGestures = 0;
      gesturePositives.clear();
      gestureNegatives.clear();
      gestureBaseJobs.clear();
      gestureAreaGrid = null;
      gridPath = -1;

   }

   private void doNegatives() {
      IntToInts req = gestureNegatives;
      int numKeys = req.getNumKeys();
      for (int i = 0; i < numKeys; i++) {
         int inputType = req.getIndexedUno(i);
         int requestID = req.getIndexedDuo(i);
         if (hasIt(inputType, requestID)) {
            actual[inputType].removeAll(requestID);
            if (actual[inputType].getSize() == 0) {
               ic.getCUC().getHostGestures().disableGesture(inputType);
            }
         }

      }
   }

   private void doPositives() {
      IntToInts req = gesturePositives;
      int numKeys = req.getNumKeys();
      for (int i = 0; i < numKeys; i++) {
         int inputType = req.getIndexedUno(i);
         int requestID = req.getIndexedDuo(i);
         //host may check gestures by itself.
         if (!hasGestureRunning(inputType)) {
            IHostGestures hostGestures = ic.getCUC().getHostGestures();
            hostGestures.enableGesture(inputType);
         }
         //check if gesture service is not running.
         if (!hasIt(inputType, requestID)) {
            addIt(inputType, requestID);
         }
      }
   }

   public void gestureDragRemove() {
      dragReleaseGestures = 0;
   }

   /**
    * The upper module knows when it is interested by a Gesture drag.
    * <br>
    * Those gesture are computed upon pointer release and analyse the speed
    * and path.
    * <br>
    * This gesture is enabled when a pointer is pressed. Context
    * or command control knows if a gesture is interesting. 
    * <br>
    * 
    */
   public void gestureDragRequest1Time() {
      dragReleaseGestures = 1; //set special flag for those drag/release gestures

   }

   public GesturePath gestureGridPathRequest1Time(GesturePointer gp, int gridPath) {
      return gestureGridPathRequest1Time(gp, gridPath, canvas.getGACanvas());
   }

   /**
    * The Grid Path
    * <li> {@link C#ANC_0_TOP_LEFT}
    * <li> {@link C#ANC_2_TOP_RIGHT}
    * <li> {@link C#ANC_6_BOT_LEFT}
    * <li> {@link C#ANC_8_BOT_RIGHT}
    * <li> {@link C#ANC_1_TOP_CENTER}
    * <li> {@link C#ANC_3_CENTER_LEFT}
    * <li> {@link C#ANC_5_CENTER_RIGHT}
    * <li> {@link C#ANC_7_BOT_CENTER}
    */
   public GesturePath gestureGridPathRequest1Time(GesturePointer gp, int gridPath, GestureArea ga) {
      this.gridPath = gridPath;
      this.gestureAreaGrid = ga;
      int[] path = gp.getPath(gridPath, ga);
      GesturePath ge = new GesturePath(ic.getCUC(), gridPath, path, gp, ga);
      gestureEvents.add(ge);
      return ge;
   }

   /**
    * Request {@link GesturePointer} tracking for the current key.
    * When key is released... track until...another release event? 
    * or a press
    * The first key of a pointer always have a GesturePointer.
    * <br>
    * This method is for other keys
    */
   public void gesturePointer() {
      gesturePointer = 1;
   }

   /**
    * Register a gesture to be listened to and sent as a {@link GestureEvent} when detected
    * <br>
    * <br>
    * Until method {@link InputRequests#gestureUnRegister(int, int)} is called.
    * <br>
    * Request ID allows seperate part of event handling code to request a listener.
    * <br>
    * Part of a code may decide to not need it anymore.. however the other component still
    * needs it.
    * <br>
    * Only when all request ID have called
    * {@link InputRequests#gestureUnRegister(int, int)} with their request ID,
    * will the listener be stopped.
    * <br>
    * Also used to register gesture sensors or light sensors
    * <br>
    * @param inputType
    * @param requestID
    */
   public void gestureRequest(int inputType, int requestID) {
      gesturePositives.add(inputType, requestID);
   }

   public GestureEvent gestureSwipeFlingRequest1Time(GesturePointer gp) {
      return gestureSwipeFlingRequest1Time(gp, canvas.getGACanvas());
   }

   /**
    * 
    * @param gp
    */
   public GestureEvent gestureSwipeFlingRequest1Time(GesturePointer gp, GestureArea ga) {
      int dragGestureType = gp.getDragGestureType();
      if (dragGestureType > 0) {
         GestureEvent g = new GestureEvent(ic.getCUC(), dragGestureType, gp);
         gestureEvents.add(g);
         return g;
      } else {
         return null;
      }
   }

   public void gestureUnRegister(int gestureType, int requestID) {
      gestureNegatives.add(gestureType, requestID);
   }

   public int getGridPath() {
      return gridPath;
   }

   public GestureArea getGridPathArea() {
      return gestureAreaGrid;
   }

   /**
    * NUple Jobs don't need
    * @param src
    * @return
    */
   public NUpleEvent getNUpleJob(EventKey src) {
      LinkedListDouble list = nupleListActive;
      ListElementHolder bj = (ListElementHolder) list.getHead();
      while (bj != null) {
         NUpleEvent nuple = (NUpleEvent) bj.getObject();
         if (nuple.getKeyEventFire().isEquals(src)) {
            return nuple;
         }
         bj = (ListElementHolder) bj.getNext();
      }
      return null;
   }

   /**
    * Has a dragging gesture
    * <li> {@link ITechGestures#GESTURE_TYPE_1_DRAG_SLIDE}
    * <li> {@link ITechGestures#GESTURE_TYPE_2_SWIPE}
    * <li> {@link ITechGestures#GESTURE_TYPE_3_FLING}
    * <br>
    * <br>
    * Those gestures are one time gesture detected on the pointer release
    * <br>
    * @return
    */
   public boolean hasGestureDrag() {
      return dragReleaseGestures != 0;
   }

   public boolean hasGesturePath() {
      return gridPath != -1;
   }

   /**
    * True if inputs is producing for 
    * @param inputType
    * @param requestID
    * @return
    */
   public boolean hasGestureRequest(int inputType, int requestID) {
      return hasIt(inputType, requestID);
   }

   /**
    * True when a Gesture listener is listening and will fire events.
    * 
    * @param inputType
    * @return
    */
   public boolean hasGestureRunning(int inputType) {
      if (actual[inputType] == null || actual[inputType].getSize() == 0) {
         return false;
      }
      return true;
   }

   /**
    * Is there a request for 
    * @param inputType
    * @param requestID
    * @return
    */
   private boolean hasIt(int inputType, int requestID) {
      if (actual[inputType] == null) {
         return false;
      } else {
         return actual[inputType].contains(requestID);
      }
   }

   public boolean hasRepeats() {
      return repeats.nextempty != 0;
   }

   public void newEvent(BEvent be) {
      cancelNUples(is, be);
   }

   public int nextID() {
      return requestID++;
   }

   /**
    * Those {@link BaseJob} will be queued at the end.
    * <br>
    * <li> {@link NUpleJob}
    * <li> {@link RepeatJob}
    * 
    * @param job
    */
   public void requestJob(BaseJob job) {
      gestureBaseJobs.add(job);
   }

   /**
    * Returns null if already there as a reference
    * @param keListed
    * @return
    */
   public NUpleEvent requestNUpleJob(EventKey ek) {
      NUpleEvent nu = getNUpleJob(ek);
      if (nu == null) {
         nu = new NUpleEvent(ic, ek, is);
         nupleListActive.addFreeHolder(nu);
         return nu;
      } else {
         return null;
      }
   }

   /**
    * 
    * @param gestureType
    */
   public void requestOneTimeGesture(int gestureType) {
      gesturePositives.add(gestureType, 0);
   }

   /**
    * 
    * @param er
    */
   public void requestRepeat(RepeatEvent er) {
      //
      if (er.getSource() == null) {
         throw new NullPointerException();
      }
      RepeatJob rj = new RepeatJob(ic, canvas, er);
      requestRepeatJob(rj);
   }

   /**
    * 
    * @param job
    */
   public void requestRepeatJob(RepeatJob job) {
      gestureBaseJobs.add(job);
   }

   public void requestRepetition(RepeatEvent er) {
      this.er = er;
      //repeats.add(er);
   }

   /**
    * Posts the {@link DeviceEventGroup}
    * @param deg
    */
   public void requestSimulJob(DeviceEventGroup deg) {
      is.queuePost(deg);
   }

   public void requestUpdate() {

   }

   /**
    * Requests the host for voice commands
    * @param ve
    */
   public void requestVoice(VoiceEvent ve) {
      throw new RuntimeException();
   }



   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, InputRequests.class, 530);
      toStringPrivate(dc);
      super.toString(dc.sup());
      
      dc.appendVarWithSpace("gesturePointer", gesturePointer);
      dc.appendVarWithSpace("requestID", requestID);
      dc.appendVarWithSpace("dragReleaseGestures", dragReleaseGestures);
      dc.nl();

      dc.nlLvl(repeats, "repeats");
      dc.nlLvlArray(actual, "actual IntBuffer[]");

      dc.nlLvl(gestureAreaGrid, "gestureAreaGrid");
      dc.nlLvl(gestureBaseJobs, "gestureBaseJobs");
      dc.nlLvl(gestureEvents, "gestureEvents");
      dc.nlLvl(gesturePositives, "gesturePositives");
      dc.nlLvl(gestureNegatives, "gestureNegatives");
      dc.appendVarWithSpace("gridPath", gridPath);

      dc.nlLvl(itosJob, "itosJob");
      dc.nlLvl(nupleListActive, "nupleListActive");
   }

   private void toStringPrivate(Dctx dc) {
      
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, InputRequests.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
