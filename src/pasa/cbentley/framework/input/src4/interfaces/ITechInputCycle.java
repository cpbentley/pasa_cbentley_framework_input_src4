package pasa.cbentley.framework.input.src4.interfaces;

public interface ITechInputCycle {
   /**
    * 
    */
   public static final int CYCLE_0_USER_EVENT      = 0;

   public static final int CYCLE_1_BUSINESS_EVENT  = 1;

   public static final int CYCLE_2_ANIMATION_EVENT = 2;

   /**
    * A merged combination of the 3 above
    */
   public static final int CYCLE_3_MERGED          = 3;

   public static final int EVENT_0_REFRESH_CLEAN   = 0;

   /**
    * Simply do a relayout.
    */
   public static final int EVENT_1_CLEAN           = 1;
}
