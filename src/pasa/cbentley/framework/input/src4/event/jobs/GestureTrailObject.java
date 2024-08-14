package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.framework.core.ui.src4.event.GesturePointer;
import pasa.cbentley.framework.core.ui.src4.event.RepeatEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.core.ui.src4.tech.ITechGestures;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;

public class GestureTrailObject extends GestureTrailJob {

   /**
    * Contains all Gestured variables
    * <li> 
    * 
    * <br>
    * <br>
    * This allows to compute increments for x,y but also other business values 
    */
   private int[][] array              = new int[2][ITechGestures.GESTURE_BASIC_SIZE];

   private int     distanceModDivisor = 3;

   int             incrMode           = 0;

   /**
    * Minimum increment. this minm
    */
   private int     incrRoot           = 2;

   private boolean isDoDistanceMod    = true;

   /**
    * Minimum pixels move amplitude for quick starting a gesture.
    */
   public int      mininumDistance    = 15;

   /**
    * Computed based on speed between press event and release event.
    */
   private int     rootIncr           = 2;

   /**
    * Time difference between the pressed and the released time
    */
   private long    timeDiff;

   public GestureTrailObject(InputCtx ic, CanvasAppliInput ctrl, GesturePointer gp, RepeatEvent re) {
      super(ic, ctrl, gp, re);
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

   private void gestureStart() {
      for (int i = 0; i < array.length; i++) {
         gestureStartDim(array[i]);
      }
   }

   /**
    * Defines how the trail will behave.
    * 
    * @param dim
    */
   private void gestureStartDim(int[] dim) {
      int val = 0;
      if (hasValueFlag(dim, ITechGestures.FLAG_14_VAL_DO_IT)) {
         //distance is either in pixels and logical increments
         int xDistance = dim[ITechGestures.GESTURE_OFFSET_08_POINTER_PRESSED] - dim[ITechGestures.GESTURE_OFFSET_09_POINTER_RELEASED];
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

   /**
    * Called when a release 
    * @param gp
    */
   public void init(GesturePointer gp) {
   }

   private void pressInit(GesturePointer gp, int valueX, int valueY) {
      array[1][ITechGestures.GESTURE_OFFSET_08_POINTER_PRESSED] = gp.getY();
      array[0][ITechGestures.GESTURE_OFFSET_08_POINTER_PRESSED] = gp.getX();
      array[0][ITechGestures.GESTURE_OFFSET_01_BUSINESS_PRESSED] = valueX;
      array[1][ITechGestures.GESTURE_OFFSET_01_BUSINESS_PRESSED] = valueY;
      timeDiff = 0;
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
   private void releaseInit(GesturePointer gp, int relX, int relY) {
      array[0][ITechGestures.GESTURE_OFFSET_09_POINTER_RELEASED] = gp.getX();
      array[1][ITechGestures.GESTURE_OFFSET_09_POINTER_RELEASED] = gp.getY();
      array[0][ITechGestures.GESTURE_OFFSET_02_BUSINESS_RELEASED] = relX;
      array[1][ITechGestures.GESTURE_OFFSET_02_BUSINESS_RELEASED] = relY;
      //setFlags(FLAG_10_IS_GESTURING, true);
      timeDiff = gp.getLastEventTime();
      if (timeDiff < 300) {
         //only do the trail if time diff is fast
         gestureComputeRootIncrement();
         gestureStart();
      }
   }

   public void setBoundaries(int start, int end, int id) {
      array[id][ITechGestures.GESTURE_OFFSET_10_BOUNDARY_START] = start;
      array[id][ITechGestures.GESTURE_OFFSET_11_BOUNDARY_END] = end;

   }

   public void setCurrent(int id, int value) {
      array[id][ITechGestures.GESTURE_OFFSET_03_BUSINESS_CURRENT] = value;
   }

   public void setFlag(int[] ar, int flag, boolean v) {
      ar[ITechGestures.GESTURE_OFFSET_00_FLAGS] = BitUtils.setFlag(ar[ITechGestures.GESTURE_OFFSET_00_FLAGS], flag, v);
   }

}
