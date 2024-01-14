package pasa.cbentley.framework.input.src4.gesture;

import java.util.Timer;
import java.util.TimerTask;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.framework.coreui.src4.event.GestureEvent;
import pasa.cbentley.framework.coreui.src4.tech.ITechGestures;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * Describes press, a drag and a release or more complex pointer gesture.
 * <br>
 * <br>
 * A {@link GestureDetector} is created as soon as a Pointer press is recorded. At this point, the system
 * cannot know if a gesture is being made or not by the user.
 * <br>
 * The press x,y coordinate are recorded.
 * <br>
 * 
 * Upon release of pointer the following are used to generate an effect
 * <li>timing
 * <li>acceleration
 * <li>distance from pressed coordinates to released
 * <br>
 * <br>
 * Not using animations because complex gesture may require a lot of specific programatic interaction with Drawable
 * and {@link GestureDetector}.
 * <br>
 * <br>
 * A Gesture is created upon a mouse button press. It lives until another button press is made ?
 * It is made
 * 
 * 
 * A pattern gesture looks for a pattern, it records mouvements into a signature
 * and match that signature to a command.
 * <br>
 * <br>
 * For example, draw a square on the screen
 * <br>
 * 
 * When a specific Gesture is detected, a Gesture Event is generated
 * <li> Swipe Up
 * <li> Swipe Down
 * <li> Swipe Left
 * <br>
 * 
 * @author Charles-Philip Bentley
 *
 */
public class GestureDetector implements ITechGestures, IStringable {

   /**
    * Modifies X and Y values until they reach their final value.
    * <br>
    * This implements the moving image until an event stops.
    * <br>
    * <li> For long presses
    * 
    * @author Charles-Philip Bentley
    *
    */
   public class RunTask extends TimerTask {

      private CanvasAppliInput ctrl;

      private boolean          isStopped = false;

      /**
       * 
       */
      private GestureDetector  pg;

      private int              step      = 0;

      public RunTask(GestureDetector pg, CanvasAppliInput ctrl) {
         if (pg == null)
            throw new NullPointerException();
         this.pg = pg;
         this.ctrl = ctrl;
      }

      /**
       * Will Run as long as there is a Region of the Image showing on Screen or a Pointer Pressed event is generated
       */
      public void run() {
         //toLog().printFlow("#PointerGesture#run isFinished=" + isGesturedFinished() + " " + pg.toStringOneLine());
         while (!isStopped && !isGesturedFinished()) {
            //repaint with the new positions
            ic.callSerially(new Runnable() {
               public void run() {
                  gesturedSteps();
                  ctrl.pointerGestureEvent(pg.currentGesture);
               }
            });
            //toLog().printFlow("#PointerGesture#run step=" + step + pg.toStringOneLine());

            step++;
            if (step > maxSteps) {
               isStopped = true;
            }
            //should we wait for that repaint event to be processed? We should.
            try {
               Thread.sleep(taskSleepTime);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
         endGesture();
      }
   }

   /**
    * Index in Gesture Array for the X coordinate
    */
   public static final int  ID_0_X              = 0;

   /**
    * Index in Gesture Array for the Y coordinate
    */
   public static final int  ID_1_Y              = 1;

   public static final int  ID_2_Z              = 2;

   /**
    * Was 10 in GestureCanvasDemo
    */
   public static int        taskSleepTime       = 10;

   /**
    * Contains all Gestured variables
    * <li> 
    */
   private int[][]          array               = new int[2][ITechGestures.GESTURE_BASIC_SIZE];

   private int              arrayNum            = 2;

   private CanvasAppliInput ctrl;

   public GestureEvent      currentGesture;

   /**
    * 
    */
   private RunTask          currentGestureTask;

   private int              distanceModDivisor  = 3;

   private IGesturable      draggable;

   private int              draggingMouseButton = 0;

   private int              flags;

   int                      incrMode            = 0;

   /**
    * Minimum increment. this minm
    */
   private int              incrRoot            = 2;

   private boolean          isComplexGesture;

   private boolean          isDoDistanceMod     = true;

   private boolean          isStopped           = false;

   private int              maxSteps            = 50;

   /**
    * Minimum pixels move amplitude for quick starting a gesture.
    */
   public int               mininumDistance     = 15;

   private long             pressedTime;

   private long             releasedTime;

   boolean                  releasePointer;

   boolean                  releaseState        = true;

   /**
    * Computed based on speed between press event and release event.
    */
   private int              rootIncr            = 2;

   /**
    * Time difference between the pressed and the released time
    */
   private long             timeDiff;

   /**
    * Timer used to generates events
    */
   private Timer            timer;

   private int              type;

   private boolean          wasDragged;

   protected final InputCtx ic;

   public GestureDetector(InputCtx ic, CanvasAppliInput ctrl) {
      this.ic = ic;
      this.ctrl = ctrl;
   }

   public void create(int num) {
      if (array == null || array.length <= num) {
         array = new int[num][ITechGestures.GESTURE_BASIC_SIZE];
      }
   }

   /**
    * Ends the Gesture. This method is called.
    * Fill the array with null values.... but that means the position of zero could be used by the gesture thread.
    * <br.
    */
   public void endGesture() {
      //toLog().println("#PointerGesture#endGesture");
      setFlags(ITechGestures.FLAG_11_IS_USED, false);
      if (currentGestureTask != null) {
         //stops current timer
         currentGestureTask.isStopped = true;
      }
      wasDragged = false;

      //wait for on going gesture event to be completely finished befor reseting
      //reset();
   }

   private void gestureCheckBoundaries(int[] ar) {
      boolean isFinished = false;
      int pos = ar[ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT];
      if (pos >= ar[ITechGestures.GESTURE_OFFSET_11_BOUNDARY_END]) {
         pos = ar[ITechGestures.GESTURE_OFFSET_11_BOUNDARY_END];
         isFinished = true;
      }
      if (pos < ar[ITechGestures.GESTURE_OFFSET_10_BOUNDARY_START]) {
         pos = ar[ITechGestures.GESTURE_OFFSET_10_BOUNDARY_START];
         isFinished = true;
      }
      if (ar[ITechGestures.GESTURE_OFFSET_04_INCREMENT] == 0) {
         isFinished = true;
      }
      if (isFinished) {
         ar[ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT] = pos;
         setFlag(ar, ITechGestures.FLAG_13_VAL_IS_FINISHED, isFinished);
      }

   }

   /**
    * Increment function. At each timing, value is incremented.
    * <br>
    * The speed of the gesture depends on time difference between press and release
    * as well as distance.
    * 
    * <br>
    */
   protected void gestureComputeRootIncrement() {
      rootIncr = 2;
      if (timeDiff < 300)
         rootIncr = 4;
      if (timeDiff < 200)
         rootIncr = 6;
      if (timeDiff < 100) {
         rootIncr = 8;
      }
      //rootIncr *= 3;
   }

   public void gesturedSteps() {
      for (int i = 0; i < arrayNum; i++) {
         gestureStep(array[i]);
      }
      //toLog().printState("#PointerGesture#gestureSteps increments " + array[0][GESTURE_OFFSET_04_INCREMENT] + ":" + array[1][GESTURE_OFFSET_04_INCREMENT] + " - ");
   }

   private void gestureStart() {
      currentGesture = null;
      for (int i = 0; i < arrayNum; i++) {
         gestureStartDim(array[i]);
      }
   }

   /**
    * Defines how the gesture will behave.
    * 
    * @param dim
    */
   private void gestureStartDim(int[] dim) {
      int val = 0;
      if (hasValueFlag(dim, ITechGestures.FLAG_14_VAL_DO_IT)) {
         //distance is either in pixels and logical increments
         int xDistance = dim[ITechGestures.GESTURE_OFFSET_08_POINTER_PRESSED] - dim[ITechGestures.GESTURE_OFFSET_09_POINTER_RELEASED];

         //#debug
         toDLog().pState("Pixel Distance between PointerPress and PointerRelease = " + xDistance, this, GestureDetector.class, "gestureStartDim", ITechLvl.LVL_05_FINE, true);
         
         int xMod = 0;
         if (isDoDistanceMod) {
            xMod = Math.abs(xDistance) / distanceModDivisor;
         }
         if (Math.abs(xDistance) > mininumDistance) {
            setFlag(dim, ITechGestures.FLAG_13_VAL_IS_FINISHED, false);
            setFlag(dim, ITechGestures.FLAG_10_IS_GESTURING, true);
            if (xDistance > 0) {
               val = rootIncr + xMod;
            } else {
               val = -rootIncr - xMod;
            }
         } else {
            //disable
            setFlag(dim, ITechGestures.FLAG_13_VAL_IS_FINISHED, true);
            setFlag(dim, ITechGestures.FLAG_10_IS_GESTURING, false);
         }
         dim[ITechGestures.GESTURE_OFFSET_04_INCREMENT] = val;
      }
   }

   /**
    * Check the boundary and once boundary is met, flag the ID as finished.
    * <br>
    * @param d
    */
   public void gestureStep(int[] d) {
      if (incrMode == 0 && !hasValueFlag(d, ITechGestures.FLAG_13_VAL_IS_FINISHED)) {
         int increment = d[ITechGestures.GESTURE_OFFSET_04_INCREMENT];
         int value = d[ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT];
         if (increment < 0) {
            if (increment < -incrRoot) {
               increment++;
            }
         } else {
            if (increment > incrRoot) {
               increment--;
            }
         }
         d[ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT] = value + increment;
         d[ITechGestures.GESTURE_OFFSET_04_INCREMENT] = increment;
         d[ITechGestures.GESTURE_OFFSET_07_STEP]++;

         //final check on the boundaries
         gestureCheckBoundaries(d);
      }
   }

   /**
    * Dragged Drawable is the {@link Drawable} set when Pointer is Pressed on It.
    * {@link Controller#pointerDragged(int, int, int)} forwards drag event to this drawable first.
    * <br>
    * <br>
    * This Drawable will get the release event no matter what and must unregisters
    * @return
    */
   public IGesturable getDraggable() {
      return draggable;
   }

   /**
    * Returns the position of pointer when it was pressed. 
    * <br>
    * {@link ITechGestures#GESTURE_OFFSET_03_BUSINESS_CURRENT}
    * <br>
    * @param id x,y or z
    * @return
    */
   public int getPosition(int id) {
      return array[id][ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT];
   }

   /**
    * Returns the position of pointer when it was pressed. 
    * <br>
    * {@link ITechGestures#GESTURE_OFFSET_01_BUSINESS_PRESSED}
    * <br>
    * @param id x,y or z
    * @return
    */
   public int getPressed(int id) {
      return array[id][ITechGestures.GESTURE_OFFSET_01_BUSINESS_PRESSED];
   }

   public boolean hasFlag(int flag) {
      return BitUtils.hasFlag(flags, flag);
   }

   /**
    * Check the flag for the given id
    * <br>
    * @param id
    * @param flag
    * @return
    */
   public boolean hasValueFlag(int id, int flag) {
      int flags = array[id][ITechGestures.GESTURE_OFFSET_00_FLAGS];
      return BitUtils.hasFlag(flags, flag);
   }

   public boolean hasValueFlag(int[] ar, int flag) {
      return BitUtils.hasFlag(ar[ITechGestures.GESTURE_OFFSET_00_FLAGS], flag);
   }

   public boolean isComplexGesture() {
      return isComplexGesture;
   }

   public boolean isGesturedFinished() {
      boolean isFinished = true;
      for (int i = 0; i < arrayNum; i++) {
         isFinished = isFinished && hasValueFlag(i, ITechGestures.FLAG_13_VAL_IS_FINISHED);
      }
      return isFinished;
   }

   /**
    * Compute the function values or value modifactors.
    * <br>
    * <br>
    * Gesture may be slowed with 2nd button pressed.
    * <br>
    * <br>
    * @param timeDiff
    */
   private void quickStart() {

   }

   private void reset() {
      for (int i = 0; i < arrayNum; i++) {
         IntUtils.fill(array[i], 0);
      }
   }

   public void setBoundaries(int start, int end, int id) {
      array[id][ITechGestures.GESTURE_OFFSET_10_BOUNDARY_START] = start;
      array[id][ITechGestures.GESTURE_OFFSET_11_BOUNDARY_END] = end;

   }

   public void setCurrent(int id, int value) {
      array[id][ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT] = value;
   }

   /**
    */
   public void setDraggable(IGesturable d) {
      this.draggable = d;
   }

   public void setFlag(int[] ar, int flag, boolean v) {
      ar[ITechGestures.GESTURE_OFFSET_00_FLAGS] = BitUtils.setFlag(ar[ITechGestures.GESTURE_OFFSET_00_FLAGS], flag, v);
   }

   /**
    * 
    * @param flag
    * @param v
    */
   public void setFlags(int flag, boolean v) {
      flags = BitUtils.setFlag(flags, flag, v);
   }

   /**
    * 
    * @param flag
    * @param v
    * @param id
    */
   public void setFlags(int flag, boolean v, int id) {
      setFlag(array[id], flag, v);
   }

   public void setPressed(int val, int id) {
      array[id][ITechGestures.GESTURE_OFFSET_01_BUSINESS_PRESSED] = val;
   }

   /**
    * Associate ID and Value to {@link ITechGestures#GESTURE_OFFSET_02_BUSINESS_RELEASED}
    * @param id
    * @param value
    */
   public void setRelease(int id, int value) {
      array[id][ITechGestures.GESTURE_OFFSET_02_BUSINESS_RELEASED] = value;
   }

   public void setReleasePointer(boolean pointer, boolean state) {
      releasePointer = pointer;
      releaseState = state;
   }

   public void simpleDrag(InputState ic) {
      //      toLog().printFlow("#PointerGesture#simpleDrag " + ((d != null) ? d.toStringOneLine() : " null") + " wasDragged=" + wasDragged);
      //      if (ic.isDraggedDragged()) {
      //         wasDragged = true;
      //      }
   }

   /**
    * Called when a {@link Drawable} registers a press on itself.
    * <br>
    * <br>
    * Saves InputConfig x and y at {@link ITechGestures#GESTURE_OFFSET_08_POINTER_PRESSED}
    * <br>
    * @param d
    * @param ic
    */
   public void simplePress(InputState ic) {
      simplePress(0, 0, ic);
   }

   public void simplePress(int value, InputState ic) {
      simplePress(value, 0, ic);
   }

   /**
    * This method must be very fast. It is called at each pointer presses.
    * <br>
    * TODO differentiate drags from different mouse buttons?
    * Called by {@link ViewDrawable#managePointerGesture(InputState)}.
    * <br>
    * <br>
    * {@link ITechGestures#FLAG_11_IS_USED} the use of the same {@link GestureDetector}.
    * <br>
    * Competing Gestures:
    * <br>
    * The basic gesture is a Pressed and Release action that is disabled when a dragging action occurs.
    * This dragging action can be another gesture in action.
    * <br>
    * All gestures are finalized when the release event occurs.
    * @param d
    * @param pressedX parameter of the method, not the actual position of the pointer. (e.g ScrollConfig start value)
    * @param pressedY
    * @param ic
    */
   public void simplePress(int valueX, int valueY, InputState ic) {
      //#debug
      toDLog().pFlow("at " + valueX + "," + valueY, this, GestureDetector.class, "simplePress", ITechLvl.LVL_05_FINE, true);
      
      //      if (hasFlag(FLAG_11_IS_USED)) {
      //         if (hasFlag(FLAG_10_IS_GESTURING) && currentGestureTask != null) {
      //            //stops current timer
      //            currentGestureTask.isStopped = true;
      //         } else {
      //            return;
      //         }
      //      }
      endGesture();

      reset();
      array[ID_0_X][ITechGestures.GESTURE_OFFSET_01_BUSINESS_PRESSED] = valueX;
      array[ID_1_Y][ITechGestures.GESTURE_OFFSET_01_BUSINESS_PRESSED] = valueY;
      array[0][ITechGestures.GESTURE_OFFSET_08_POINTER_PRESSED] = ic.getX();
      array[1][ITechGestures.GESTURE_OFFSET_08_POINTER_PRESSED] = ic.getY();
      draggingMouseButton = ic.getKeyCode();
      pressedTime = System.currentTimeMillis();
      timeDiff = 0;
      setFlags(ITechGestures.FLAG_11_IS_USED, true);
   }

   /**
    * 
    * @param ic
    */
   public void simpleRelease(InputState ic) {

      if (releasePointer) {
         releasePointer = false;
      }
      if (releaseState) {
         releaseState = false;
      }
   }

   /**
    * Create a timer. Called by {@link ImageDrawable#managePointerInputViewPort(InputState)}
    * <br>
    * <br>
    * Records the situation based on the previous pressed configuration.
    * <li>Time interval between press and release event
    * <li> x and y distance vectors
    * <li> gesture button id
    * <br>
    * With 2 button ID, we may have 5 different gestures
    * <li> button 1 pressed and released
    * <li> button 2 pressed and released
    * <li> button 1 and 2 pressed together and released together
    * <li> button 1 and 2 pressed together and button 1 is released
    * <li> button 1 and 2 pressed together and button 2 is released
    * <br>
    * <br>
    * @param d
    * @param ic
    * @param type
    * @param param
    */
   public void simpleReleaseGesture(InputState ic, int relX, int relY, int type, Object[] param) {
      //#debug
      toDLog().pFlow("at " + relX + "," + relY + " for type " + type, this, GestureDetector.class, "simpleReleaseGesture", ITechLvl.LVL_05_FINE, true);

      releasedTime = System.currentTimeMillis();
      array[0][ITechGestures.GESTURE_OFFSET_09_POINTER_RELEASED] = ic.getX();
      array[1][ITechGestures.GESTURE_OFFSET_09_POINTER_RELEASED] = ic.getY();
      array[0][ITechGestures.GESTURE_OFFSET_02_BUSINESS_RELEASED] = relX;
      array[1][ITechGestures.GESTURE_OFFSET_02_BUSINESS_RELEASED] = relY;
      this.type = type;
      if (type == ITechGestures.GESTURE_0_NONE) {
         simpleRelease(ic);
      } else if (type == ITechGestures.GESTURE_1_BOUNDARY) {
         //setFlags(FLAG_10_IS_GESTURING, true);
         timeDiff = releasedTime - pressedTime;
         if (timeDiff < 300) {
            gestureComputeRootIncrement();
            gestureStart();
            //check if any gesture is really going on
            if (isGesturedFinished()) {
               simpleRelease(ic);
            } else {
               RunTask rt = new RunTask(this, ctrl);
               currentGestureTask = rt;
               if (timer == null) {
                  timer = new Timer();
               }
               timer.schedule(rt, 0);
            }
         } else {
            simpleRelease(ic);
         }
      }
      //setDraggedDrawable(null);
   }

   //#mdebug
   public IDLog toLog() {
      return ctrl.toDLog();
   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx sb) {
      sb.root(this, "GestureDetector");
      toStringPrivate(sb);
      sb.append("#PointerGesture #");
      sb.append(arrayNum);
      if (hasFlag(ITechGestures.FLAG_11_IS_USED)) {
         sb.append(" Used");
      }
      if (hasFlag(ITechGestures.FLAG_10_IS_GESTURING)) {
         sb.append(" Gesturing");
      }
      sb.append(" RootIncr=" + rootIncr);
      sb.append(" TimeDiff=" + timeDiff);
      sb.append(" DraggingMouseBut=" + draggingMouseButton);

      sb.nl();
      for (int i = 0; i < arrayNum; i++) {
         int[] d = array[i];
         sb.append(i + 1);
         sb.append(" ");
         if (hasValueFlag(d, ITechGestures.FLAG_14_VAL_DO_IT)) {
            sb.append(" DotIt");
         }
         if (hasValueFlag(d, ITechGestures.FLAG_13_VAL_IS_FINISHED)) {
            sb.append(" Finished");
         }
         sb.tab();
         sb.nl();
         sb.append("Pressed=" + d[ITechGestures.GESTURE_OFFSET_01_BUSINESS_PRESSED]);
         sb.nl();
         sb.append("Position=" + d[ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT]);
         sb.nl();
         sb.append("Released=" + d[ITechGestures.GESTURE_OFFSET_02_BUSINESS_RELEASED]);
         sb.nl();
         sb.append("Increment=" + d[ITechGestures.GESTURE_OFFSET_04_INCREMENT]);
         sb.nl();
         sb.append("Boundary=[" + d[ITechGestures.GESTURE_OFFSET_10_BOUNDARY_START]);
         sb.append(",");
         sb.append(d[ITechGestures.GESTURE_OFFSET_11_BOUNDARY_END] + "]");
         sb.nl();
         sb.append("Step=" + d[ITechGestures.GESTURE_OFFSET_07_STEP]);
         sb.tabRemove();
         sb.nl();
      }
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx sb) {
      sb.root1Line(this, "GestureDetector");
      toStringPrivate(sb);
      sb.append(array[0][0] + ":" + array[1][0] + " - ");
      sb.append("Pos=[" + array[0][ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT] + ":" + array[1][ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT] + "] ");
      sb.append("bx=" + array[0][ITechGestures.GESTURE_OFFSET_10_BOUNDARY_START] + " to " + array[0][ITechGestures.GESTURE_OFFSET_11_BOUNDARY_END] + " - ");
      sb.append("by=" + array[1][ITechGestures.GESTURE_OFFSET_10_BOUNDARY_START] + " to " + array[1][ITechGestures.GESTURE_OFFSET_11_BOUNDARY_END] + " - ");
      sb.append(" RootIncr=" + rootIncr);
      sb.append(" TimeDiff=" + timeDiff);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUCtx();
   }

   //#enddebug

}
