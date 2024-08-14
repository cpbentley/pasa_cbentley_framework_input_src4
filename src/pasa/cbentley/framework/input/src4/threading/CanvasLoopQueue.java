package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * Each event is discrete. no optimization.
 * 
 * @author Charles Bentley
 *
 */
public class CanvasLoopQueue extends CanvasLoop {

   private BlockingQueueUnlimited queueUpdateRender;

   /**
    * {@link ITechThreadPaint#THREADING_1_UI_UPDATERENDERING}
    * <br>
    * The update and render thread are the same.
    * There is no clocking in this
    */
   public CanvasLoopQueue(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      queueUpdateRender = new BlockingQueueUnlimited(ic.getUC());
   }

   public synchronized void addToQueue(Object o) {
      this.queueUpdateRender.enqueue(o);
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
            } else if (o instanceof InputStateCanvas) {
               ExecutionContextCanvas ec = canvas.createExecutionContextEvent();
               OutputStateCanvas os = ec.getOutputStateCanvas();
               InputStateCanvas is = ec.getInputStateCanvas();
               canvas.processInputState(ec, is, os);
            } else if (o instanceof OutputStateCanvas) {
               ExecutionContextCanvas ec = canvas.createExecutionContextPaint();
               OutputStateCanvas os = ec.getOutputStateCanvas();
               InputStateCanvas is = ec.getInputStateCanvas();
               render(g, ec, is, os);
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
