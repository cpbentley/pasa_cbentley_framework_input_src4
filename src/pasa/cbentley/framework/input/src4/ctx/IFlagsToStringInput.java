package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.core.src4.ctx.IToStringFlags;

public interface IFlagsToStringInput extends IToStringFlags {
   /**
    * Debug prints in move
    */
   public static final int D_FLAG_22_MOVE_POINTERS      = 1 << 21;

   /**
    * 
    */
   public static final int Debug_1_Controller           = 1;

   public static final int Debug_16_RootDrawableDLayers = 16;

   public static final int Debug_2_DragControl          = 2;

   public static final int Debug_32_Clipping_Check      = 8;

   public static final int Debug_4_DrawablesRepaints    = 4;

   public static final int Debug_8_ForceFullRepaints    = 8;

}
