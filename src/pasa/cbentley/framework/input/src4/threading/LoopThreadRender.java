package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.RepaintHelper;

/**
 * Reads rendering jobs from the {@link RepaintHelper}
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class LoopThreadRender extends CanvasLoop implements Runnable {

   private RepaintHelper repaintControl;

   public LoopThreadRender(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      isRunning = true;
      this.repaintControl = canvas.getRepaintCtrl();
   }

   public void run() {

      while (isRunning) {

         IGraphics g = canvas.getGraphics();
         if (g == null) {
            throw new NullPointerException();
         }
         ExecutionContextCanvas ec = canvas.createExecutionContextPaint();
         OutputStateCanvas os = ec.getOutputStateCanvas();
         InputStateCanvas is = ec.getInputStateCanvas();
         super.render(g, ec, is, os);
         canvas.flushGraphics();

         Thread.yield();

         //This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
         //You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
         //FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
         try {
            Thread.sleep(1);
         } catch (Exception e) {
         }

      }
   }

}
