package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.byteobjects.src4.tech.ITechByteObject;
import pasa.cbentley.framework.coreui.src4.tech.ITechCanvasHost;
import pasa.cbentley.framework.input.src4.ctx.IBOTypesInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * Config for {@link InputCtx}
 * 
 * Contains a {@link ITechCanvasHost} for the default canvas configuration
 * @author Charles Bentley
 *
 */
public interface ITechCanvasAppli extends ITechByteObject {

   public static final int CANVAS_APP_BASIC_SIZE                = A_OBJECT_BASIC_SIZE + 10;

   public static final int CANVAS_APP_TYPE                      = IBOTypesInput.TYPE_1_TECH_CANVAS_APPLI;

   public static final int CANVAS_APP_FLAG_1_FULLSCREEN         = 1 << 0;

   public static final int CANVAS_APP_FLAG_2_                   = 1 << 1;

   public static final int CANVAS_APP_FLAG_3_                   = 1 << 2;

   public static final int CANVAS_APP_OFFSET_01_FLAG            = A_OBJECT_BASIC_SIZE;

   /**
    * Sub type of canvas
    */
   public static final int CANVAS_APP_OFFSET_02_TYPE1           = A_OBJECT_BASIC_SIZE + 1;

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
   public static final int CANVAS_APP_OFFSET_03_THREADING_MODE1 = A_OBJECT_BASIC_SIZE + 2;

   /**
    * <li>{@link ITechCanvasHost#SCREEN_0_TOP_NORMAL}
    * <li>{@link ITechCanvasHost#SCREEN_1_BOT_UPSIDEDOWN}
    * <li>{@link ITechCanvasHost#SCREEN_2_LEFT_ROTATED}
    * <li>{@link ITechCanvasHost#SCREEN_3_RIGHT_ROTATED}
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
