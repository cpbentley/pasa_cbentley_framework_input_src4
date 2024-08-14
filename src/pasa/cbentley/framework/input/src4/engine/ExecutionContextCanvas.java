package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.framework.core.ui.src4.exec.ExecutionContext;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

public class ExecutionContextCanvas extends ExecutionContext {

   protected final InputCtx ic;

   public ExecutionContextCanvas(InputCtx ic) {
      super(ic.getCUC());

      this.ic = ic;
   }

   public InputStateCanvas getInputStateCanvas() {
      return (InputStateCanvas) is;
   }

   public OutputStateCanvas getOutputStateCanvas() {
      return (OutputStateCanvas) os;
   }
}
