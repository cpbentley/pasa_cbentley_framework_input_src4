package pasa.cbentley.framework.input.src4.game;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.threading.GameLoop;

public class GameLoopX extends GameLoop {

   public GameLoopX(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
   }

   public void run() {

      //initialize framedata
      
      frameData.init();
      
      while (isRunning) {
         frameData.startNew();


         while (frameData.isUpdating()) {
            simulationUpdate(); //will check input.. so that over 1 second, we know how many pixels an object moves
            frameData.nextUpdate();
         }

         frameData.computeInterpolation(); //make sure before rendering
         simulationRender();
         frameData.nextFrame();

         while (frameData.isSleeping()) {
            Thread.yield();

            //This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
            //You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
            //FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
            try {
               Thread.sleep(1);
            } catch (Exception e) {
            }
            frameData.tickFrameTime();
         }
      }
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, GameLoopX.class, "@line5");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {
      
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, GameLoopX.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
