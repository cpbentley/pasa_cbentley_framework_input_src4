package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;

public abstract class ObjectIC implements IStringable {

   protected final InputCtx ic;

   public ObjectIC(InputCtx ic) {
      this.ic = ic;
   }

   public InputCtx getIC() {
      return ic;
   }
   
   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, ObjectIC.class, 30);
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, ObjectIC.class);
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUC();
   }

   //#enddebug
   

}
