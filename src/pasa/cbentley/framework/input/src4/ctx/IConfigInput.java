package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.ctx.IConfigBO;
import pasa.cbentley.framework.coreui.src4.tech.IBOCanvasHost;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

/**
 * Config for {@link InputCtx}
 * 
 * Provides a Java interface to the {@link ITechPaintThread}, {@link IBOCanvasHost}
 * 
 * @author Charles Bentley
 *
 */
public interface IConfigInput extends IConfigBO {

   /**
    * <li>{@link ITechPaintThread#THREADING_0_ONE_TO_RULE_ALL}
    * <li>{@link ITechPaintThread#THREADING_1_UI_UPDATERENDERING}
    * <li>{@link ITechPaintThread#THREADING_2_UIUPDATE_RENDERING}
    * <li>{@link ITechPaintThread#THREADING_3_THREE_SEPARATE}
    * @return
    */
   public int getCanvasDefaultThreadingMode();

   /**
    * <li>{@link IBOCanvasHost#SCREEN_0_TOP_NORMAL}
    * <li>{@link IBOCanvasHost#SCREEN_1_BOT_UPSIDEDOWN}
    * <li>{@link IBOCanvasHost#SCREEN_2_LEFT_ROTATED}
    * <li>{@link IBOCanvasHost#SCREEN_3_RIGHT_ROTATED}
    * @return
    */
   public int getCanvasDefaultScreenMode();

   /**
    * 
    * @return
    */
   public int getCanvasDefaultBgColor();

   /**
    * 
    * @return
    */
   public int getCanvasDefaultDebugFlags();
}
