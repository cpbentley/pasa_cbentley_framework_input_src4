package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.ctx.IConfigBO;
import pasa.cbentley.framework.coreui.src4.tech.ITechCanvasHost;
import pasa.cbentley.framework.input.src4.interfaces.ITechInput;

/**
 * Config for {@link InputCtx}
 * 
 * Provides a Java interface to the {@link ITechInput}, {@link ITechCanvasHost}
 * 
 * @author Charles Bentley
 *
 */
public interface IConfigInput extends IConfigBO {

   /**
    * <li>{@link ITechInput#THREADING_0_ONE_TO_RULE_ALL}
    * <li>{@link ITechInput#THREADING_1_UI_UPDATERENDERING}
    * <li>{@link ITechInput#THREADING_2_UIUPDATE_RENDERING}
    * <li>{@link ITechInput#THREADING_3_THREE_SEPARATE}
    * @return
    */
   public int getCanvasDefaultThreadingMode();

   /**
    * <li>{@link ITechCanvasHost#SCREEN_0_TOP_NORMAL}
    * <li>{@link ITechCanvasHost#SCREEN_1_BOT_UPSIDEDOWN}
    * <li>{@link ITechCanvasHost#SCREEN_2_LEFT_ROTATED}
    * <li>{@link ITechCanvasHost#SCREEN_3_RIGHT_ROTATED}
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
