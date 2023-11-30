package pasa.cbentley.framework.input.src4.gesture;

import pasa.cbentley.framework.coreui.src4.tech.ITechGestures;

/**
 * Encapsulates a Gesture occurence
 * <br>
 * @author Charles Bentley
 *
 */
public class GestureInput {

   /**
    * <li>{@link ITechGestures#GESTURE_TYPE_1_DRAG_SLIDE}
    * <li>{@link ITechGestures#GESTURE_TYPE_3_FLING}
    * <li>{@link ITechGestures#GESTURE_DOUBLE_TYPE_1_PINCH}
    */
   private int        type;

   public GestureInput(int type) {
      this.type = type;
   }
   
   public int getType() {
      return type;
   }
   private IGesturable draggable;

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
    */
   public void setDraggable(IGesturable d) {
      this.draggable = d;
   }
   
   /**
    * The Pinch is a distance between 2 different pointers. The root pointer is used as a base
    * for computing the pinch distance. Good android read here http://developer.android.com/training/gestures/scale.html.
    * <br>
    * Frameworks that don't support touch may emulate pinch with a key. The key is pressed, pinch is started,
    * mouse is moved and distance is double the distance since the pinch key press.
    * @return
    */
   public boolean isPinched() {
      // TODO Auto-generated method stub
      return false;
   }
}
