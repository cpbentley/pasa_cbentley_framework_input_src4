package pasa.cbentley.framework.input.src4.game;

import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.threading.CanvasLoopGame;

public class GameLoopVariable extends CanvasLoopGame {

   private boolean isRunning;

   private long    lastFpsTime;

   private int     fps;

   public GameLoopVariable(InputCtx ic,CanvasAppliInput canvas) {
      super(ic,canvas);
   }

   public void run() {

      long lastLoopTime = getTime();
      final int TARGET_FPS = 60;
      // how long a frame should take, which is around 16.6 milli seconds
      final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

      // keep looping round til the game ends
      while (isRunning) {
         // work out how long its been since the last update, this
         // will be used to calculate how far the entities should
         // move this loop
         long now = getTime();
         //the frameTime. Time needed to update and render
         long updateLength = now - lastLoopTime;
         //
         lastLoopTime = now;

         double delta = updateLength / ((double) OPTIMAL_TIME);

         // update the frame counter
         lastFpsTime += updateLength;
         fps++;

         // update our FPS counter if a second has passed since
         // we last recorded
         if (lastFpsTime >= 1000000000) {
            //#debug
            toDLog().pAlways("(FPS: " + fps + ")", this, GameLoopVariable.class, "run", LVL_05_FINE, true);
            lastFpsTime = 0;
            fps = 0;
         }

         // update the game logic
         simulationUpdate();

         // draw everyting
         simulationRender();

         // we want each frame to take 10 milliseconds, to do this
         // we've recorded when we started the frame. We add 10 milliseconds
         // to this and then factor in the current time to give 
         // us our final value to wait for
         // remember this is in ms, whereas our lastLoopTime etc. vars are in ns.
         try {
            long rightNow = getTime();
            Thread.sleep((OPTIMAL_TIME + lastLoopTime - rightNow) / 1000000);
         } catch (Exception e) {

         }
      }
   }

}
