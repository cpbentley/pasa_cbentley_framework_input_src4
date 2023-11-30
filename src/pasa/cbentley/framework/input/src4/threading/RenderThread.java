package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.EventThreader;
import pasa.cbentley.framework.input.src4.RepaintCtrl;
import pasa.cbentley.framework.input.src4.CanvasResult;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * Reads rendering jobs from the {@link RepaintCtrl}
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class RenderThread extends EventThreader implements Runnable {

   private RepaintCtrl repaintControl;

   public RenderThread(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      this.repaintControl = canvas.getRepaintCtrl();
   }

   public void run() {
      IGraphics g = canvas.getGraphics();
      if (g == null) {
         throw new NullPointerException();
      }
      while (isRunning) {

         //take all Screen jobs as soon as there is one available
         CanvasResult sr = (CanvasResult) repaintControl.getNextRender();
         super.render(g, sr);
         canvas.flushGraphics();
      }
   }

}
