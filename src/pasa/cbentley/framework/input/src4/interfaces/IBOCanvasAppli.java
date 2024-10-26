package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.byteobjects.src4.core.interfaces.IByteObject;
import pasa.cbentley.framework.core.ui.src4.tech.IBOCanvasHost;
import pasa.cbentley.framework.core.ui.src4.tech.ITechHostUI;
import pasa.cbentley.framework.input.src4.ctx.IBOCtxSettingsInput;
import pasa.cbentley.framework.input.src4.ctx.IBOTypesInput;
import pasa.cbentley.framework.input.src4.ctx.IConfigInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;

/**
 * The base canvas definition for the {@link InputCtx} module.
 * 
 * It is contained within a {@link IBOCanvasHost} for the default canvas configuration
 * @author Charles Bentley
 *
 */
public interface IBOCanvasAppli extends IByteObject {

   public static final int CANVAS_APP_BASIC_SIZE                = A_OBJECT_BASIC_SIZE + 10;

   public static final int CANVAS_APP_BASE_TYPE                 = IBOTypesInput.TYPE_1_CANVAS_APPLI;

   public static final int CANVAS_APP_FLAG_1_FULLSCREEN         = 1 << 0;

   public static final int CANVAS_APP_FLAG_2_                   = 1 << 1;

   public static final int CANVAS_APP_FLAG_3_                   = 1 << 2;

   public static final int CANVAS_APP_OFFSET_01_FLAG            = A_OBJECT_BASIC_SIZE;

   /**
    * Sub type of canvas
    * 0 if {@link CanvasAppliInput} ?
    * 
    */
   public static final int CANVAS_APP_OFFSET_02_TYPE_SUB1       = A_OBJECT_BASIC_SIZE + 1;

   /**
    * Set for the controlling canvas
    * 
    * <br>
    * <li>{@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}
    * <li>{@link ITechThreadPaint#THREADING_1_UI_UPDATERENDERING}
    * <li>{@link ITechThreadPaint#THREADING_2_UIUPDATE_RENDERING}
    * <li>{@link ITechThreadPaint#THREADING_3_THREE_SEPARATE}
    * 
    * <p>
    * Configured by {@link IBOCtxSettingsInput#CTX_INPUT_OFFSET_02_CANVAS_DEFAULT_THREADING_MODE1}
    * and {@link IConfigInput#getCanvasDefaultThreadingMode()}
    * </p>
    */
   public static final int CANVAS_APP_OFFSET_03_THREADING_MODE1 = A_OBJECT_BASIC_SIZE + 2;

   /**
    * <li>{@link ITechHostUI#SCREEN_0_TOP_NORMAL}
    * <li>{@link ITechHostUI#SCREEN_1_BOT_UPSIDEDOWN}
    * <li>{@link ITechHostUI#SCREEN_2_LEFT_ROTATED}
    * <li>{@link ITechHostUI#SCREEN_3_RIGHT_ROTATED}
    * 
    */
   public static final int CANVAS_APP_OFFSET_04_SCREEN_MODE1    = A_OBJECT_BASIC_SIZE + 3;

   /**
    * 
    */
   public static final int CANVAS_APP_OFFSET_05_DEBUG_FLAGS1    = A_OBJECT_BASIC_SIZE + 4;

   /**
    * 
    */
   public static final int CANVAS_APP_OFFSET_06_BG_COLOR4       = A_OBJECT_BASIC_SIZE + 5;

}
