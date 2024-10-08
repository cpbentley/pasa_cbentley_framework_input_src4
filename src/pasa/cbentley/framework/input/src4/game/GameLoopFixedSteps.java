package pasa.cbentley.framework.input.src4.game;

import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.threading.CanvasLoopGame;

/**
 * from 
 * http://www.java-gaming.org/index.php?topic=24220.0
 * 
 * @author Charles Bentley
 *
 */
public class GameLoopFixedSteps extends CanvasLoopGame {
   private int     fps        = 60;

   private int     frameCount = 0;

   private boolean paused;

   public GameLoopFixedSteps(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
   }

   //Only run this in another Thread!
   public void run() {
      //This value would probably be stored elsewhere.
      final double GAME_HERTZ = 30.0;
      //Calculate how many ns each frame should take for our target game hertz.
      final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
      //At the very most we will update the game this many times before a new render.
      //If you're worried about visual hitches more than perfect timing, set this to 1.
      final int MAX_UPDATES_BEFORE_RENDER = 5;
      //We will need the last update time.
      double lastUpdateTime = getTime();
      //Store the last time we rendered.
      double lastRenderTime = getTime();

      //If we are able to get as high as this FPS, don't render again.
      final double TARGET_FPS = 60;
      final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

      //Simple way of finding FPS.
      int lastSecondTime = (int) (lastUpdateTime / 1000000000);

      while (isRunning) {
         double thisFrameTime = getTime();
         int updateCount = 0;

         if (!paused) {
            //Do as many game updates as we need to, potentially playing catchup.
            double timeDiffSinceLastUpdate = thisFrameTime - lastUpdateTime;
            while (timeDiffSinceLastUpdate > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER) {
               simulationUpdate();;
               lastUpdateTime += TIME_BETWEEN_UPDATES;
               updateCount++;
            }

            //If for some reason an update takes forever, we don't want to do an insane number of catchups.
            //If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
            if (timeDiffSinceLastUpdate > TIME_BETWEEN_UPDATES) {
               lastUpdateTime = thisFrameTime - TIME_BETWEEN_UPDATES;
            }

            //Render. To do so, we need to calculate interpolation for a smooth render.
            float interpolation = Math.min(1.0f, (float) (timeDiffSinceLastUpdate / TIME_BETWEEN_UPDATES));
            //render using interpolation in screenresult
            //canvas.render(g, interpolation);
            lastRenderTime = thisFrameTime;

            //Update the frames we got.
            int thisSecond = (int) (lastUpdateTime / 1000000000);
            if (thisSecond > lastSecondTime) {
               System.out.println("NEW SECOND " + thisSecond + " " + frameCount);
               fps = frameCount;
               frameCount = 0;
               lastSecondTime = thisSecond;
            }

            //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
            while (thisFrameTime - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && timeDiffSinceLastUpdate < TIME_BETWEEN_UPDATES) {
               Thread.yield();

               //This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
               //You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
               //FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
               try {
                  Thread.sleep(1);
               } catch (Exception e) {
               }

               thisFrameTime = getTime();
            }
         }
      }
   }
}