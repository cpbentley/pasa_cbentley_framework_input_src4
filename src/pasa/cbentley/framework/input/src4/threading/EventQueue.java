package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.EventThreader;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.CanvasResult;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

/**
 * Each event is discrete. no optimization
 * @author Charles Bentley
 *
 */
public class EventQueue extends EventThreader {

   private BlockingQueueUnlimited queueUpdateRender;

   /**
    * {@link ITechPaintThread#THREADING_1_UI_UPDATERENDERING}
    * <br>
    * The update and render thread are the same.
    * There is no clocking in this
    */
   public EventQueue(InputCtx ic, CanvasAppliInput canvas) {
      super(ic,canvas);
      queueUpdateRender = new BlockingQueueUnlimited(ic.getUC());
   }

   public void run() {
      IGraphics g = canvas.getGraphics();
      if (g == null) {
         throw new NullPointerException();
      }
      while (isRunning) {
         try {
            //wait for any job. input state or repaint
            Object o = queueUpdateRender.dequeue();
            if (o instanceof Runnable) {
               Runnable r = (Runnable) o;
               r.run();
            } else if (o instanceof InputState) {
               InputState r = (InputState) o;
               canvas.processInputState(r);
            } else if (o instanceof CanvasResult) {
               CanvasResult r = (CanvasResult) o;
               render(g, r);
               canvas.flushGraphics();
            } else {
               throw new IllegalArgumentException();
            }
         } catch (InterruptedException e) {
            //close the thread nicely
            return;
         }
      }
   }
}
