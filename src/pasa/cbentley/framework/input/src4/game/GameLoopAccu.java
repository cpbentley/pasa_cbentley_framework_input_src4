package pasa.cbentley.framework.input.src4.game;

import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.threading.CanvasLoopGame;

/**
 * Fix Your Timestep!
 * http://gafferongames.com/game-physics/fix-your-timestep/
 * 
 * @author Charles Bentley
 *
 */
public class GameLoopAccu extends CanvasLoopGame {

   public GameLoopAccu(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
   }

   public void run() {

      //time that passes
      double totalTime = 0.0;
      double dt = 0.01;

      double currentTime = getTime();
      double accumulator = 0.0;

      while (isRunning) {
         double newTime = getTime();
         double frameTime = newTime - currentTime;
         if (frameTime > 0.25) {
            frameTime = 0.25;
         }
         currentTime = newTime;

         accumulator += frameTime;

         while (accumulator >= dt) {
            simulationUpdate(totalTime, dt);
            totalTime += dt;
            accumulator -= dt;
         }

         float alpha = (float) (accumulator / dt);

         //canvas.render(g, alpha);

         //when rendering goes too fast wait a little. it is pointless to render 2 identical frames
      }
   }

}
