package pasa.cbentley.framework.input.src4.gesture;

import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;

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
   public void managePointerDrag(InputState is, OutputStateCanvas sr);

   /**
    * 
    * @param is
    * @param sr
    */
   public void managePointerRelease(InputState is, OutputStateCanvas sr);

}
