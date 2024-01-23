package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.interfaces.IBOCtxSettings;
import pasa.cbentley.framework.coreui.src4.tech.IBOCanvasHost;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

/**
 * Config for {@link InputCtx}
 * 
 * Contains a {@link IBOCanvasHost} for the default canvas configuration
 * @author Charles Bentley
 *
 */
public interface IBOCtxSettingsInput extends IBOCtxSettings {
   /**
    * True when application is running in One Thumb Mode
    */
   public static final int INPUT_FLAG_8_ONE_THUMB                             = 1 << 7;

   public static final int CTX_INPUT_BASIC_SIZE                               = CTX_BASIC_SIZE + 40;

   public static final int CTX_INPUT_FLAG_1_FULLSCREEN                        = 1 << 0;

   public static final int CTX_INPUT_FLAG_2_                                  = 1 << 1;

   public static final int CTX_INPUT_FLAG_3_                                  = 1 << 2;

   public static final int CTX_INPUT_FLAG_4_                                  = 1 << 3;

   public static final int CTX_INPUT_FLAG_5_                                  = 1 << 4;

   public static final int CTX_INPUT_FLAG_6_                                  = 1 << 5;

   public static final int CTX_INPUT_FLAG_7_                                  = 1 << 6;

   public static final int CTX_INPUT_FLAG_8_                                  = 1 << 7;

   public static final int CTX_INPUT_OFFSET_01_FLAG                           = CTX_BASIC_SIZE;

   /**
    * Set for the controlling canvas
    * 
    * <br>
    * <li>{@link ITechPaintThread#THREADING_0_ONE_TO_RULE_ALL}
    * <li>{@link ITechPaintThread#THREADING_1_UI_UPDATERENDERING}
    * <li>{@link ITechPaintThread#THREADING_2_UIUPDATE_RENDERING}
    * <li>{@link ITechPaintThread#THREADING_3_THREE_SEPARATE}
    * 
    */
   public static final int CTX_INPUT_OFFSET_02_CANVAS_DEFAULT_THREADING_MODE1 = CTX_BASIC_SIZE + 1;

   /**
    * <li>{@link IBOCanvasHost#SCREEN_0_TOP_NORMAL}
    * <li>{@link IBOCanvasHost#SCREEN_1_BOT_UPSIDEDOWN}
    * <li>{@link IBOCanvasHost#SCREEN_2_LEFT_ROTATED}
    * <li>{@link IBOCanvasHost#SCREEN_3_RIGHT_ROTATED}
    * 
    */
   public static final int CTX_INPUT_OFFSET_03_CANVAS_DEFAULT_SCREEN_MODE1    = CTX_BASIC_SIZE + 2;

   /**
    * Debug flags for a Canvas.
    */
   public static final int CTX_INPUT_OFFSET_04_CANVAS_DEFAULT_DEBUG_FLAGS1    = CTX_BASIC_SIZE + 3;

   public static final int CTX_INPUT_OFFSET_05_CANVAS_DEFAULT_BG_COLOR4       = CTX_BASIC_SIZE + 4;

}
