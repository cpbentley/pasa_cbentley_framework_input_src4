package pasa.cbentley.framework.input.src4.threading;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;

/**
 * {@link Runnable} with settable flag isRunning on a {@link CanvasAppliInput}.
 * 
 * <p>
 * Examples of implementations :
 * <li> {@link CanvasLoopQueue}
 * <li> {@link CanvasLoopGame}
 * <li> {@link LoopThreadRender}
 * <li> {@link LoopThreadUpdate}
 * </p>
 * @author Charles Bentley
 *
 */
public abstract class CanvasLoop extends ObjectIC implements Runnable, IStringable {

   protected CanvasAppliInput canvas;

   protected boolean          isRunning;

   public CanvasLoop(InputCtx ic, CanvasAppliInput canvas) {
      super(ic);
      this.canvas = canvas;
   }

   protected void render(IGraphics g, ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {
      canvas.render(g, ec, is, sr);
   }

   protected void process(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas os) {
      canvas.processInputState(ec, is, os);
   }

   public void queueRun(Runnable run) {

   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, CanvasLoop.class, 60);
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, CanvasLoop.class, 60);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("isRunning", isRunning);
   }
   //#enddebug

}
