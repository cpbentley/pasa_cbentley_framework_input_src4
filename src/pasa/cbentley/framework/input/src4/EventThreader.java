package pasa.cbentley.framework.input.src4;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * 
 * @author Charles Bentley
 *
 */
public abstract class EventThreader implements Runnable, IStringable {

   protected CanvasAppliInput canvas;

   protected boolean          isRunning;

   protected final InputCtx   ic;

   public EventThreader(InputCtx ic, CanvasAppliInput canvas) {
      this.ic = ic;
      this.canvas = canvas;
   }

   protected void render(IGraphics g, CanvasResult sr) {
      canvas.renderMe(g, sr.getInput(), sr);
   }

   protected void process(InputState is) {
      canvas.processInputState(is);
   }

   public void queueRun(Runnable run) {

   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, EventThreader.class, 45);
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("isRunning", isRunning);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "EventThreader");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUCtx();
   }

   //#enddebug

}
