package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.framework.coreui.src4.interfaces.BCodes;
import pasa.cbentley.framework.coreui.src4.tech.ITechCodes;

public interface ILocks {

   /**
    * 
    */
   public static final int LOCK_CANCEL_KEY = ITechCodes.KEY_CANCEL;

   public static final int LOCK_KEY        = ITechCodes.KEY_MENU_LEFT;

   public static final int LOCK_P          = ITechCodes.KEY_POUND;

   public static final int LOCK_POUND      = 2;

   public static final int LOCK_PP         = (ITechCodes.KEY_POUND << 8) + ITechCodes.KEY_POUND;

   public static final int LOCK_PS         = (ITechCodes.KEY_POUND << 8) + ITechCodes.KEY_STAR;

   public static final int LOCK_S          = ITechCodes.KEY_STAR;

   public static final int LOCK_S3         = BCodes.createLock(ITechCodes.KEY_STAR, ITechCodes.KEY_NUM3);

   public static final int LOCK_SP         = (ITechCodes.KEY_STAR << 8) + ITechCodes.KEY_POUND;

   public static final int LOCK_SS         = (ITechCodes.KEY_STAR << 8) + ITechCodes.KEY_STAR;

   public static final int LOCK_SS3        = BCodes.createLock(ITechCodes.KEY_STAR, ITechCodes.KEY_STAR, ITechCodes.KEY_NUM3);

   public static final int LOCK_STAR       = 1;

}
