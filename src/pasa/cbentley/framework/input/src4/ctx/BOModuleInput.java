package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.core.ui.src4.ctx.ToStringStaticCoreUi;
import pasa.cbentley.framework.input.src4.interfaces.IBOCanvasAppli;

public class BOModuleInput extends BOModuleAbstract {

   protected final InputCtx ic;

   public BOModuleInput(InputCtx ic) {
      super(ic.getBOC());
      this.ic = ic;
   }

   public ByteObject getFlagOrderedBO(ByteObject bo, int offset, int flag) {
      // TODO Auto-generated method stub
      return null;
   }

   public ByteObject merge(ByteObject root, ByteObject merge) {
      int type = merge.getType();
      switch (type) {
      }
      return null;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, BOModuleInput.class, "@line5");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public boolean toString(Dctx dc, ByteObject bo) {
      int type = bo.getType();
      switch (type) {
         case IBOTypesInput.TYPE_1_CANVAS_APPLI:
            dc.rootN(bo, "IBOCanvasAppli");
            int subType = bo.get1(IBOCanvasAppli.CANVAS_APP_OFFSET_02_TYPE_SUB1);
            dc.appendVarWithSpace("subType", subType);
            
            int threadMode = bo.get1(IBOCanvasAppli.CANVAS_APP_OFFSET_03_THREADING_MODE1);
            dc.appendVarWithNewLine("treadMode", ToStringStaticInput.toStringThreadingMode(threadMode));
            
            int screenMode = bo.get1(IBOCanvasAppli.CANVAS_APP_OFFSET_04_SCREEN_MODE1);
            dc.appendVarWithNewLine("screenMode", ToStringStaticCoreUi.toStringScreenMode(screenMode));
            
            int bgColor = bo.get4(IBOCanvasAppli.CANVAS_APP_OFFSET_06_BG_COLOR4);
            dc.appendVarWithNewLine("bgColor", bgColor);
          
            //look for a sub type string
            if(subType != 0) {
               dc.nl();
               //boc.getBOModuleManager().toStringSubType(dc, bo, subType);
            }
            
            break;
         default:
            return false;
      }
      return true;
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, BOModuleInput.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   public boolean toString1Line(Dctx dc, ByteObject bo) {
      int type = bo.getType();
      switch (type) {
         case IBOTypesInput.TYPE_1_CANVAS_APPLI:
            dc.rootN(bo, "CanvasAppli");
            break;
         default:
            return false;
      }
      return true;
   }

   public String toStringGetDIDString(int did, int value) {
      // TODO Auto-generated method stub
      return null;
   }

   public String toStringOffset(ByteObject o, int offset) {
      // TODO Auto-generated method stub
      return null;
   }

   private void toStringPrivate(Dctx dc) {
      
   }

   public String toStringType(int type) {
      return ToStringStaticInput.toStringTypeBO(type);
   }

   //#enddebug
   

}
