package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.ICanvasRenderable;

public class CanvasAble extends CanvasAppliInput {

   private ICanvasRenderable cr;

   public CanvasAble(InputCtx ic, ICanvasRenderable cr) {
      super(ic);
      this.cr = cr;
   }

   public void render(IGraphics g, ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {
      cr.render(g, is, sr, getWidth(), getHeight());
   }

   protected void ctrlUIEvent(ExecutionContextCanvas ec, InputStateCanvas ic, OutputStateCanvas sr) {
      cr.ctrlUIEvent(ic, sr);
   }

   public String getTitle() {
      // TODO Auto-generated method stub
      return null;
   }

}
