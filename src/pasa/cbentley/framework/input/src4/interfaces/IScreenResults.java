package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.framework.input.src4.CanvasResult;

/**
 * Specific flags
 * @author Charles Bentley
 *
 */
public interface IScreenResults {

   /**
    * Flag {@link CanvasResult} as being used in the repaint cycle before that painting call finishes.
    */
   public static final int FLAG_06_ACTIVE       = 1 << 5;

   /**
    * Special is only valid if a single drawable is in the drawable array. otherwise flag is removed
    * during the repaint flag build process.
    * <br>
    * <br>
    * Used by some {@link IDrawable} for repaint optimizations.
    */
   public static final int FLAG_07_SPECIAL      = 1 << 6;

   public static final int FLAG_10_EVENT_THREAD = 1 << 9;

   public static final int FLAG_11_BUSINESS     = 1 << 10;

   public static final int FLAG_12_ANIMATION    = 1 << 11;

   public static final int FLAG_13_MERGED       = 1 << 12;

   public static final int FLAG_14_BUILT        = 1 << 13;

   /**
    * When an lock is used and should be notified when painting is finished
    */
   public static final int FLAG_15_LOCK         = 1 << 14;

}
