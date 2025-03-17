package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.core.ui.src4.exec.ExecutionContext;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

public class ExecutionContextCanvas extends ExecutionContext {

   protected final InputCtx ic;

   public ExecutionContextCanvas(InputCtx ic) {
      super(ic.getCUC());

      this.ic = ic;
   }

   public void endRender() {
      OutputStateCanvas os = getOutputStateCanvas();
      os.endRender();
   }

   public InputStateCanvas getInputStateCanvas() {
      return (InputStateCanvas) is;
   }

   public OutputStateCanvas getOutputStateCanvas() {
      return (OutputStateCanvas) os;
   }

   public void startRender() {
      OutputStateCanvas os = getOutputStateCanvas();

   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, ExecutionContextCanvas.class, toStringGetLine(30));
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, ExecutionContextCanvas.class, toStringGetLine(30));
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug

}
