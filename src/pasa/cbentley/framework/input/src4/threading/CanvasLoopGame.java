package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.interfaces.ITimeCtrl;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;
import pasa.cbentley.framework.input.src4.game.FrameData;
import pasa.cbentley.framework.input.src4.interfaces.ITechInputCycle;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * A Game loop provides a special clocking for {@link ITechThreadPaint#THREADING_1_UI_UPDATERENDERING}
 * <br>
 * Instead of waiting for Jobs on a queue, a game loops 
 * 
 * Comparison with the Animation Thread
 * It is like the Animation thread
 * 
 * Game Loop Links
 * 
 * 
 * When should I use a fixed or variable time step?
 * http://gamedev.stackexchange.com/questions/1589/when-should-i-use-a-fixed-or-variable-time-step
 * 
 * http://www.java-gaming.org/index.php?topic=24220.0
 * 
 * 
 * 
 * @author Charles Bentley
 *
 */
public abstract class CanvasLoopGame extends CanvasLoop {

   protected FrameData         frameData;

   private InputStateCanvas    inputState;

   private boolean             isAvoidRender;

   private int                 lastID;

   protected OutputStateCanvas outputStateCanvasLoop;

   protected ITimeCtrl         time;

   /**
    * 
    * @param ic
    * @param canvas
    */
   public CanvasLoopGame(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      isRunning = true;
      time = ic.getTimeCtrl();
      frameData = new FrameData(ic);

      outputStateCanvasLoop = new OutputStateCanvas(ic, canvas, ITechInputCycle.CYCLE_0_USER_EVENT);

   }

   public FrameData getFrameData() {
      return frameData;
   }

   /**
    * Asks the Simulation to process the input state.
    * 
    * Update the state
    */
   public InputStateCanvas getInput() {
      //check if new input by synchronizing to the input thread
      //that state is a copy from the
      //no need to synch since its a swapped reference ? nah.. its need to be volatile
      //not worth it to optimize here since JVM implementation may differs.
      //so the hard hitting full sync has more chance to be enforced on many different hosts
      InputStateCanvas is = canvas.getEventController().getInputState();

      ExecutionContextCanvas ec = canvas.createExecutionContextEvent();
      OutputStateCanvas os = ec.getOutputStateCanvas();
      //#debug
      toDLog().pFlow("", this, CanvasLoopGame.class, "input@99", LVL_04_FINER, DEV_X_ONELINE_THRE);

      //TODO what about a final input state version?

      //no requests.. just cmd reading

      //input thread gets key pressed event, find a cmd associated with it

      is.setFrameData(frameData);
      //wait for any job. input state or repaint
      int eid = is.getEventID();
      if (eid != lastID) {
         //new event process it
         canvas.processInputState(ec, is, os);
         lastID = eid;
      } else {
         //set the inputstate to the update with 
         canvas.processInputStateContinuous(is);
      }
      //check the queue of GameCmd

      inputState = is;
      return is;
   }

   public long getTime() {
      return time.getTickNano();
   }

   public void setIsAvoidRender(boolean b) {
      isAvoidRender = b;
   }

   public void simulationRender() {
      //#debug
      toDLog().pFlow("avoidRender=" + isAvoidRender, this, CanvasLoopGame.class, "simulationRender@122", LVL_04_FINER, DEV_X_ONELINE_THRE);
      if (isAvoidRender) {
         return;
      }

      IGraphics g = canvas.getGraphics();
      ExecutionContextCanvas ec = canvas.createExecutionContextCanvas();
      ec.setOutputState(outputStateCanvasLoop);
      ec.setInputState(inputState);
      try {
         render(g, ec, inputState, outputStateCanvasLoop);
      } finally {
         //make sure graphics is disposed
         canvas.flushGraphics();
      }
   }

   public void simulationUpdate() {
      //#debug
      toDLog().pFlow("", this, CanvasLoopGame.class, "simulationUpdate@135", LVL_04_FINER, DEV_4_THREAD | DEV_2_ONELINE);
      //check for input
      InputStateCanvas is = getInput();
      is.setFrameData(frameData);
      canvas.simulationUpdate(is);
   }

   /**
    * Updates the simulation. Current time. time delta of this simulation tick
    */
   public void simulationUpdate(double time, double dt) {
      //check for input
      InputStateCanvas is = getInput();
      is.setSimulationTime(time, dt);
      canvas.simulationUpdate(is);
   }

   /**
    * Stops the gameloop.
    */
   public void stop() {
      //#debug
      toDLog().pFlow("", this, CanvasLoopGame.class, "stop", LVL_05_FINE, true);
      this.isRunning = false;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, CanvasLoopGame.class, "@line96");
      toStringPrivate(dc);
      super.toString(dc.sup());
      dc.nlLvl(frameData, "frameData");
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, CanvasLoopGame.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
      dc.appendWithSpace(frameData.toString1Line());
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("isAvoidRender", isAvoidRender);
   }
   //#enddebug

}
