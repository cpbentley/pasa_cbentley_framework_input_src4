package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.framework.coreui.src4.interfaces.BCodes;
import pasa.cbentley.framework.coreui.src4.tech.IBCodes;

public interface ILocks {

   /**
    * 
    */
   public static final int LOCK_CANCEL_KEY = IBCodes.KEY_CANCEL;

   public static final int LOCK_KEY        = IBCodes.KEY_MENU_LEFT;

   public static final int LOCK_P          = IBCodes.KEY_POUND;

   public static final int LOCK_POUND      = 2;

   public static final int LOCK_PP         = (IBCodes.KEY_POUND << 8) + IBCodes.KEY_POUND;

   public static final int LOCK_PS         = (IBCodes.KEY_POUND << 8) + IBCodes.KEY_STAR;

   public static final int LOCK_S          = IBCodes.KEY_STAR;

   public static final int LOCK_S3         = BCodes.createLock(IBCodes.KEY_STAR, IBCodes.KEY_NUM3);

   public static final int LOCK_SP         = (IBCodes.KEY_STAR << 8) + IBCodes.KEY_POUND;

   public static final int LOCK_SS         = (IBCodes.KEY_STAR << 8) + IBCodes.KEY_STAR;

   public static final int LOCK_SS3        = BCodes.createLock(IBCodes.KEY_STAR, IBCodes.KEY_STAR, IBCodes.KEY_NUM3);

   public static final int LOCK_STAR       = 1;

}
