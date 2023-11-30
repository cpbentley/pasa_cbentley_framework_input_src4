package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.interfaces.ITimeCtrl;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.CanvasResult;
import pasa.cbentley.framework.input.src4.EventThreader;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.game.FrameData;
import pasa.cbentley.framework.input.src4.interfaces.ITechInput;
import pasa.cbentley.framework.input.src4.interfaces.ITechInputCycle;

/**
 * A Game loop provides a special clocking for {@link ITechInput#THREADING_1_UI_UPDATERENDERING}
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
public abstract class GameLoop extends EventThreader {

   protected CanvasResult canvasResultLoop;

   protected FrameData    frameData;

   private InputState     inputState;

   private int            lastID;

   protected ITimeCtrl    time;

   /**
    * 
    * @param ic
    * @param canvas
    */
   public GameLoop(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      isRunning = true;
      time = ic.getTimeCtrl();
      frameData = new FrameData(ic);

      canvasResultLoop = new CanvasResult(ic, canvas, ITechInputCycle.CYCLE_0_USER_EVENT);
      
      InputState is = canvas.getEvCtrl().getState();
      canvasResultLoop.setInputState(is);
   }

   /**
    * Updates the simulation. Current time. time delta of this simulation tick
    */
   public void doupdate(double time, double dt) {
      //check for input
      InputState is = input();
      is.setSimulationTime(time, dt);
      canvas.simulationUpdate(is);
   }

   public FrameData getFrameData() {
      return frameData;
   }

   public long getTime() {
      return time.getTickNano();
   }

   /**
    * Asks the Simulation to process the input state.
    * 
    * Update the state
    */
   public InputState input() {
      //check if new input by synchronizing to the input thread
      //that state is a copy from the
      //no need to synch since its a swapped reference ? nah.. its need to be volatile
      //not worth it to optimize here since JVM implementation may differs.
      //so the hard hitting full sync has more chance to be enforced on many different hosts
      InputState is = canvas.getEvCtrl().getState();

      //TODO what about a final input state version?
      
      //no requests.. just cmd reading
      
      //input thread gets key pressed event, find a cmd associated with it

      is.setFrameData(frameData);
      //wait for any job. input state or repaint
      int eid = is.getEventID();
      if (eid != lastID) {
         //new event process it
         canvas.processInputState(is);
         lastID = eid;
      } else {
         //set the inputstate to the update with 
         canvas.processInputStateContinuous(is);
      }
      //check the queue of GameCmd

      inputState = is;
      canvasResultLoop.setInputState(inputState);
      return is;
   }

   public void simulationRender() {
      //#debug
      toDLog().pFlow("", this, GameLoop.class, "simulationRender", LVL_04_FINER, true);

      IGraphics g = canvas.getGraphics();
      try {
         render(g, canvasResultLoop);
      } finally {
         //make sure graphics is disposed
         canvas.flushGraphics();
      }
   }

   public void simulationUpdate() {
      //#debug
      toDLog().pFlow("", this, GameLoop.class, "simulationUpdate", LVL_04_FINER, true);
      //check for input
      InputState is = input();
      is.setFrameData(frameData);
      canvas.simulationUpdate(is);
   }

   /**
    * Stops the gameloop.
    */
   public void stop() {
      //#debug
      toDLog().pFlow("", this, GameLoop.class, "stop", LVL_05_FINE, true);
      this.isRunning = false;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, GameLoop.class, "@line96");
      toStringPrivate(dc);
      super.toString(dc.sup());
      dc.nlLvl(frameData, "frameData");
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, GameLoop.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
      dc.appendWithSpace(frameData.toString1Line());
   }

   private void toStringPrivate(Dctx dc) {
   }
   //#enddebug

}
