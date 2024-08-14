package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.structs.synch.BlockingQueueUnlimited;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;

public class LoopThreadUpdate extends CanvasLoop {

   /**
    * Full of input queue. notified when 
    */
   BlockingQueueUnlimited inputQueue;

   public LoopThreadUpdate(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      isRunning = true;
   }

   public void run() {
      while (isRunning) {
         int lastID = -1;

         ExecutionContextCanvas ec = canvas.createExecutionContextEvent();
         OutputStateCanvas os = ec.getOutputStateCanvas();
         InputStateCanvas is = ec.getInputStateCanvas();
         canvas.processInputState(ec, is, os);
         //is.setFrameData(frameData);
         //wait for any job. input state or repaint
         int eid = is.getEventID();
         if (eid != lastID) {
            //new event process it
            lastID = eid;
         } else {
            //set the inputstate to the update with 
            canvas.processInputStateContinuous(is);
         }
         canvas.simulationUpdate(is);
      }
   }

}
