package pasa.cbentley.framework.input.src4.gesture;

import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.InputRequests;

/**
 * Every pointer press, a {@link GestureDetector} is started.
 * <br>
 * If {@link InputRequests}  the drawable context starts listening or if the 
 * It is the responsability of the Context to request a gesture.
 * <br>
 * A gesture, Since all Gesture start with the Pointer
 * One {@link GestureDetector} for each pointerID
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class GestureCtrl {

   private GestureDetector[] gestureDectors;

   private CanvasAppliInput  canvas;

   private InputCtx ic;

   public GestureCtrl(InputCtx ic, CanvasAppliInput canvas) {
      this.ic = ic;
      this.canvas = canvas;
      //pointer IDS?
      gestureDectors = new GestureDetector[3];
      for (int i = 0; i < gestureDectors.length; i++) {
         gestureDectors[i] = new GestureDetector(ic,canvas);
      }
   }

   public GestureDetector getGesture(int pointerID) {
      if (pointerID >= gestureDectors.length)
         return null;
      return gestureDectors[pointerID];
   }
}
