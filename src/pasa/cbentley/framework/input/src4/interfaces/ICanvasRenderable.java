package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.core.ui.src4.event.EventAppli;
import pasa.cbentley.framework.core.ui.src4.interfaces.ICanvasAppli;
import pasa.cbentley.framework.core.ui.src4.interfaces.ICanvasHost;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;

/**
 * How does it know it gets resized?
 * <br>
 * {@link EventAppli}
 * 
 * <br>
 * How does it access the underlying {@link ICanvasAppli} and {@link ICanvasHost} it is running on?
 * <br>
 * i.e. the display environment
 * 
 * @author Charles Bentley
 *
 */
public interface ICanvasRenderable extends IStringable {
   
   /**
    * Just renders at origin. not aware of size ? font size etc? access to underlying canvas?
    * The clip gives some info.
    * <br>
    * The width and height
    * @param g
    * @param is
    * @param sr
    */
   public void render(IGraphics g, InputStateCanvas is, OutputStateCanvas sr, int w, int h);

   /**
    * App events provides functionnality for 
    * <li> resizes
    * @param ic
    * @param sr
    */
   public void ctrlUIEvent(InputStateCanvas ic, OutputStateCanvas sr);

}
