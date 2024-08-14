package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.framework.core.ui.src4.ctx.CoreUiCtx;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;

public abstract class CmdGroupCanvasAppli extends ObjectIC {

   protected final CoreUiCtx cuc;

   public CmdGroupCanvasAppli(InputCtx ic) {
      super(ic);
      cuc = ic.getCUC();
   }

   public abstract void ctrlUIEvent(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas os);

}
