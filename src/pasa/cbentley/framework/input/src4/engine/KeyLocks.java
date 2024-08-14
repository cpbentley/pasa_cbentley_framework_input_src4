package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.core.ui.src4.interfaces.BCodes;
import pasa.cbentley.framework.core.ui.src4.tech.ITechCodes;
import pasa.cbentley.framework.core.ui.src4.tech.ITechInputFeedback;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.ILocks;

public class KeyLocks implements IStringable {

   private InputState    is;

   private OutputStateCanvas sr;

   private InputCtx ic;

   public KeyLocks(InputCtx ic) {
      this.ic = ic;
   }

   public void set(InputState is, OutputStateCanvas sr) {
      this.is = is;
      this.sr = sr;
   }

   /**
    * Current lock
    * TODO if * and ** are both locks? 
    * Sub Locks like * must be locked and unlocked with the special lock command
    * Or Locks may take effect after a period of time with no event
    * Typing * and waiting 5 seconds will lock *
    * Task repainting the Key Status Bar : locking * in 5 4 3 2 1 sec
    * It maybe a special key for one hand operations
    */
   int           lock            = 0;

   /**
    * TODO what this?
    */
   public Object lockingObject;

   /**
    * Current mask (0xFF or 0xFFFF or 0xFFFFFF or 0xFFFFFFFF)
    * Mask is applied on the signature
    */
   int           lockMask        = 0;

   /**
    * The locks (PP,SS,PS,SP etc) that the InputConfig is looking after.
    * Once ** is typed, InputConfig will behave as if ** preclude all events
    * A lock is also active when the last element is pressed. If another key event is
    * recieved, the lock will be active for that key event but then be canceled.
    */
   int[]         locksBluePrints = null;

   /**
    * Signature of the last 4 typed keys. last one being the most significant bit
    * signature is reset to 0 when keys are not typed in sequence
    */
   int           lockSignature;

   /**
    * one lock mask (0xFF, 0xFFFF, 0xFFFFFF, 0xFFFFFFFF) for one the lock definition
    */
   int[]         locksMask       = null;

   /**
    * Remove any lock on the InputConfig by reseting lock fields, including short lock state.
    */
   public void lockCancel() {
      isFullLocked = false;
      isShortLocked = false;
      lockSignature = 0;
      isLockStarted = false;
      lock = 0;
   }

   /**
    * Is the current state has a short lock on it.
    */
   private boolean isShortLocked;

   /**
    * true when InputConfig is locked to a key serie
    */
   boolean         isFullLocked;

   /**
    * Flag set when code should be expecting a lock. That is a lock is in progress
    */
   private boolean isLockStarted = false;

   /**
    * Detects a Short Lock *,*_ <br>
    * Short lock will last as long as the Short Lock trigger is keep pressed <br>
    * 
    * @param keyPressed
    */
   private void lockPress(int keyPressed) {
      if (locksBluePrints != null) {
         if (isLockStarted) {
            //already one key for locking.
            if (keyPressed == ITechCodes.KEY_STAR || keyPressed == ITechCodes.KEY_POUND) {
               //only update if key is correct and timing is correct.
               if (is.isLastKeyFastTyped(keyPressed)) {
                  lockUpdate(keyPressed);
               } else {
                  //else remove lock conditions
                  lockRemove();
               }
            } else {
               //not a key
               lockRemove();
            }
         } else {
            //no key for locking. clean state.
            if (keyPressed == ITechCodes.KEY_STAR || keyPressed == ITechCodes.KEY_POUND) {
               if (is.getNumKeysPressed() == 1) {
                  //update
                  isLockStarted = true;
                  lockUpdate(keyPressed);
               }
            } else {
               lockRemove();
            }
         }
      }
   }

   public boolean isShortLock() {
      return isShortLocked;
   }

   /**
    * True if configuration is locked in the plane
    * @param lockid
    * @return
    */
   public boolean isPlaneLock(int lockid) {
      return lock == lockid;
   }

   public static int getLockSize(int lock) {
      int count = 0;
      int key1 = (lock >> 24) & 0xFF;
      int key2 = (lock >> 16) & 0xFF;
      int key3 = (lock >> 8) & 0xFF;
      int key4 = (lock >> 0) & 0xFF;
      if (key4 != 0)
         count++;
      if (key3 != 0)
         count++;
      if (key2 != 0)
         count++;
      if (key1 != 0)
         count++;
      return count;
   }

   /**
    * True if at least one key is locked
    * @return
    */
   public boolean isKeyLocked() {
      // TODO Auto-generated method stub
      return false;
   }

   /**
    * Set the locks to be looking for and that will lock the InputConfig
    * @param locks
    */
   public void setLocks(int[] locks) {
      locksBluePrints = locks;
      locksMask = new int[locksBluePrints.length];
      for (int i = 0; i < locksBluePrints.length; i++) {
         int size = getLockSize(locksBluePrints[i]);
         if (size == 1)
            locksMask[i] = 0xFF;
         if (size == 2)
            locksMask[i] = 0xFFFF;
         if (size == 3)
            locksMask[i] = 0xFFFFFF;
         if (size == 4)
            locksMask[i] = 0xFFFFFFFF;

      }
   }

   /**
    * Is the InputConfig locked
    * True if fully locked or shortLocked
    * @return
    */
   public boolean isLocked() {
      return isFullLocked || isShortLocked;
   }

   public void addPressed(int key) {
      if (key == ILocks.LOCK_CANCEL_KEY) {
         lockCancel();
         //make sure this code is called after the reset
      }
      lockPress(key);
   }

   public String getLockDebugString() {
      return getLockString(lock);
   }

   public int[] getLocks() {
      return locksBluePrints;
   }

   /**
    * Returns the signature of the current locking signature
    * @return
    */
   public String getLockString() {
      return getLockString(lockSignature);
   }

   /**
    * Decompose the lock int value into 4 bytes, get the Key Char for each
    * and return the String
    * @param lock
    * @return
    */
   public String getLockString(int lock) {
      int key1 = (lock >> 24) & 0xFF;
      int key2 = (lock >> 16) & 0xFF;
      int key3 = (lock >> 8) & 0xFF;
      int key4 = (lock >> 0) & 0xFF;

      String s = "";
      if (key4 != 0)
         s = BCodes.getChar(key4) + s;
      if (key3 != 0)
         s = BCodes.getChar(key3) + s;
      if (key2 != 0)
         s = BCodes.getChar(key2) + s;
      if (key1 != 0)
         s = BCodes.getChar(key1) + s;
      return s;
   }

   /**
    * 
    * @return
    */
   public int getKeysLock() {
      return lock;
   }

   public void addReleased(int key) {
      lockRelease(key);//must be called last once
   }

   /**
    * Called when a key is released and when a lock condition exists.
    * @param key
    */
   private void lockRelease(int key) {
      if (isShortLocked) {
         //this check will be false if short lock was used because the key press is registered
         if (is.isKeyTyped()) { // we have a key type
            if (key == ITechCodes.KEY_STAR || key == ITechCodes.KEY_POUND) {
               isFullLocked = true;
               isShortLocked = false;
               isLockStarted = false;
               lockSignature = 0;
            }
         } else {
            lockCancel();
         }
         sr.setFlag(ITechInputFeedback.FLAG_02_FULL_REPAINT, true);
      } else if (isFullLocked) {
         if (!is.isKeyTyped()) {
            lockSignature = 0;
         }
      }
   }

   /**
    * Doesn't remove short Lock
    */
   private void lockRemove() {
      if (lockSignature != 0 && !isShortLocked) {
         lockSignature = 0;
      }
      isLockStarted = false;
   }

   private void lockUpdate(int keyPressed) {
      lockSignature = (lockSignature << 8) + (keyPressed & 0xFF);
      for (int i = 0; i < locksBluePrints.length; i++) {
         if (locksBluePrints[i] == (lockSignature & locksMask[i])) {
            if (isFullLocked) {
               lockCancel();
            } else {
               lock = locksBluePrints[i];
               lockMask = locksMask[i];
               isShortLocked = true;//set to true until the next key event decides if we have a full lock
               lockSignature = 0;
            }
         }
      }
      //TODO call for only a repaint on the Drawable drawing lock status.
      sr.setFlag(ITechInputFeedback.FLAG_02_FULL_REPAINT, true);
   }
   
   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "KeyLocks");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx sb) {
      sb.append("#KeysLock ");
      sb.nl();
      //debug the lock
      sb.nl();
      sb.append("#Lock");
      sb.nl();
      sb.append("isLocked=" + isFullLocked + " Lock=" + getLockString(lock) + " LockSig=" + lockSignature + " LockString=" + getLockString());
      //
      if (locksBluePrints != null) {
         for (int i = 0; i < locksBluePrints.length; i++) {
            sb.append(getLockString(locksBluePrints[i]) + ":" + locksMask[i] + " - ");
         }
         sb.nl();
      }
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "KeyLocks");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUC();
   }

   //#enddebug
   

}
