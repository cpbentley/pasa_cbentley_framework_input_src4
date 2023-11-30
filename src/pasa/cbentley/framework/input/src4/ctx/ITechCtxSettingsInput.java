package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.tech.ITechCtxSettings;
import pasa.cbentley.framework.coreui.src4.tech.ITechCanvasHost;
import pasa.cbentley.framework.input.src4.interfaces.ITechInput;

/**
 * Config for {@link InputCtx}
 * 
 * Contains a {@link ITechCanvasHost} for the default canvas configuration
 * @author Charles Bentley
 *
 */
public interface ITechCtxSettingsInput extends ITechCtxSettings {
   /**
    * True when application is running in One Thumb Mode
    */
   public static final int INPUT_FLAG_8_ONE_THUMB                             = 1 << 7;

   public static final int CTX_INPUT_BASIC_SIZE                               = CTX_BASIC_SIZE + 40;

   public static final int CTX_INPUT_FLAG_1_FULLSCREEN                        = 1;

   public static final int CTX_INPUT_FLAG_2_                                  = 2;

   public static final int CTX_INPUT_FLAG_3_                                  = 4;

   public static final int CTX_INPUT_OFFSET_01_FLAG                           = CTX_BASIC_SIZE;

   /**
    * Set for the controlling canvas
    * 
    * <br>
    * <li>{@link ITechInput#THREADING_0_ONE_TO_RULE_ALL}
    * <li>{@link ITechInput#THREADING_1_UI_UPDATERENDERING}
    * <li>{@link ITechInput#THREADING_2_UIUPDATE_RENDERING}
    * <li>{@link ITechInput#THREADING_3_THREE_SEPARATE}
    * 
    */
   public static final int CTX_INPUT_OFFSET_02_CANVAS_DEFAULT_THREADING_MODE1 = CTX_BASIC_SIZE + 1;

   /**
    * <li>{@link ITechCanvasHost#SCREEN_0_TOP_NORMAL}
    * <li>{@link ITechCanvasHost#SCREEN_1_BOT_UPSIDEDOWN}
    * <li>{@link ITechCanvasHost#SCREEN_2_LEFT_ROTATED}
    * <li>{@link ITechCanvasHost#SCREEN_3_RIGHT_ROTATED}
    * 
    */
   public static final int CTX_INPUT_OFFSET_03_CANVAS_DEFAULT_SCREEN_MODE1    = CTX_BASIC_SIZE + 2;

   /**
    * Debug flags for a Canvas.
    */
   public static final int CTX_INPUT_OFFSET_04_CANVAS_DEFAULT_DEBUG_FLAGS1    = CTX_BASIC_SIZE + 3;

   public static final int CTX_INPUT_OFFSET_05_CANVAS_DEFAULT_BG_COLOR4       = CTX_BASIC_SIZE + 4;

}
