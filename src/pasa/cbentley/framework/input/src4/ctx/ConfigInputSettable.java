package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.ctx.ABOCtx;
import pasa.cbentley.byteobjects.src4.ctx.ConfigAbstractBO;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.utils.interfaces.IColors;
import pasa.cbentley.framework.core.ui.src4.tech.ITechHostUI;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

public class ConfigInputSettable extends ConfigAbstractBO implements IConfigInput {

   private int bgColor;

   private int debugFlags;

   private int screenMode;

   private int threadMode;

   public ConfigInputSettable(UCtx uc) {
      super(uc);
      threadMode = ITechThreadPaint.THREADING_0_ONE_TO_RULE_ALL;
      screenMode = ITechHostUI.SCREEN_0_TOP_NORMAL;
      bgColor = IColors.FULLY_OPAQUE_BLACK;
      debugFlags = 0;
   }

   public int getCanvasDefaultBgColor() {
      return bgColor;
   }

   public int getCanvasDefaultDebugFlags() {
      return debugFlags;
   }

   public int getCanvasDefaultScreenMode() {
      return screenMode;
   }

   public int getCanvasDefaultThreadingMode() {
      return threadMode;
   }

   public void postProcessing(ByteObject settings, ABOCtx ctx) {
      super.postProcessing(settings, ctx);
   }

   public void setBgColor(int bgColor) {
      this.bgColor = bgColor;
   }

   public void setDebugFlags(int debugFlags) {
      this.debugFlags = debugFlags;
   }

   public void setScreenMode(int screenMode) {
      this.screenMode = screenMode;
   }

   public void setTreadMode(int mode) {
      this.threadMode = mode;
   }

}
