package pasa.cbentley.framework.input.src4.gesture;

import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.CanvasResult;

/**
 * Object being gestured.
 * <br>
 * The object requested
 * @author Charles Bentley
 *
 */
public interface IGesturable {
   /**
    * 
    * @param is
    * @param sr
    */
   public void managePointerDrag(InputState is, CanvasResult sr);

   /**
    * 
    * @param is
    * @param sr
    */
   public void managePointerRelease(InputState is, CanvasResult sr);

}
