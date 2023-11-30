package pasa.cbentley.framework.input.src4;

import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.ICanvasRenderable;

public class CanvasAble extends CanvasAppliInput {

   private ICanvasRenderable cr;

   public CanvasAble(InputCtx ic, ICanvasRenderable cr) {
      super(ic);
      this.cr = cr;
   }

   protected void render(IGraphics g, InputState is, CanvasResult sr) {
      cr.render(g, is, sr,getWidth(),getHeight());
   }

   protected void ctrlUIEvent(InputState ic, CanvasResult sr) {
      cr.ctrlUIEvent(ic, sr);
   }

   public String getTitle() {
      // TODO Auto-generated method stub
      return null;
   }


}
