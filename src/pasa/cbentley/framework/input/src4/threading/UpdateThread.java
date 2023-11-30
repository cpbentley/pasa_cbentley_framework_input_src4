package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.EventThreader;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

public class UpdateThread extends EventThreader {

   /**
    * Full of input queue. notified when 
    */
   BlockingQueueUnlimited inputQueue;

   public UpdateThread(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
   }

   public void run() {
      while (isRunning) {
         InputState is;
         try {
            //blocks until there is a request
            Object request = inputQueue.dequeue();
            if (request instanceof InputState) {
               is = (InputState) request;
               super.process(is);
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
