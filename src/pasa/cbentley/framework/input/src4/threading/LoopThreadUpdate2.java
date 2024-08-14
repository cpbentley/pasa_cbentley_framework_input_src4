package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;

public class LoopThreadUpdate2 extends CanvasLoop {

   /**
    * Full of input queue. notified when 
    */
   BlockingQueueUnlimited inputQueue;

   public LoopThreadUpdate2(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
   }

   public void run() {
      while (isRunning) {
         int lastID = -1;
         try {
            //blocks until there is a request
            Object request = inputQueue.dequeue();
            if (request instanceof InputStateCanvas) {
               ExecutionContextCanvas ec = canvas.createExecutionContextEvent();
               OutputStateCanvas os = ec.getOutputStateCanvas();
               InputStateCanvas is = ec.getInputStateCanvas();
               super.process(ec, is, os);
            } else if (request instanceof Runnable) {
               Runnable run = (Runnable) request;
               run.run();
            }

         } catch (InterruptedException e) {
            //close the thread nicely
            return;
         }
      }
   }

   public void queueRun(Runnable run) {

   }
}
