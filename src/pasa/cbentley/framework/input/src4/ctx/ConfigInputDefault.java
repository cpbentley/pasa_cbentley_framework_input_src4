package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.ctx.ABOCtx;
import pasa.cbentley.byteobjects.src4.ctx.ConfigAbstractBO;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.utils.interfaces.IColors;
import pasa.cbentley.framework.coreui.src4.tech.IBOCanvasHost;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

/**
 * 
 * @author Charles Bentley
 *
 */
public class ConfigInputDefault extends ConfigAbstractBO implements IConfigInput {

   public ConfigInputDefault(UCtx uc) {
      super(uc);
   }

   public void postProcessing(ByteObject settings, ABOCtx ctx) {
      super.postProcessing(settings, ctx);
   }
   
   public int getCanvasDefaultThreadingMode() {
      return ITechPaintThread.THREADING_0_ONE_TO_RULE_ALL;
   }

   public int getCanvasDefaultScreenMode() {
      return IBOCanvasHost.SCREEN_0_TOP_NORMAL;
   }

   public int getCanvasDefaultBgColor() {
      return IColors.FULLY_OPAQUE_BLACK;
   }

   public int getCanvasDefaultDebugFlags() {
      // TODO Auto-generated method stub
      return 0;
   }

}
