package pasa.cbentley.framework.input.src4;
//package mordan.controller.input;
//
//import mordan.controller.input.interfaces.ITechInput;
//import mordan.universal.utils.StringBuilder;
//
///**
// * Implements a fine grained key repeat event creator for passive Canvas. 
// * <br>
// * <li>When the first pressed event is generated, a command might be accomplished.
// * <li>If that commands asks for the InputConfig for it, the repeater will generate repeat event and look for the
// * key release event. 
// * <li>After time intervals a repeat key press event is sent.
// * <li>Again the command must ask for repeater to continue repeat this.
// * <br>
// * <br>
// * Synchronization is tricky.
// * The {@link MCmd} linked to the key press and repeat is thus executed at every tick of the repeater, until
// * the key release event is recieved. That mechanism works for Cabled Mode.
// * <br>
// * <br>
// * <b>Event Thread</b> : why or Why not using: {@link Display#callSerially(Runnable)} ?
// * <br>
// * The repeater may work serially. IN that case, repeat will be dependant on the painting time.
// * When working solo, {@link ScreenResult2} are piled up and fired when painting method finishes.
// * <br>
// * In Active Rendering Mode, the repeater is not used.
// * Repeats are done automatically
// * <br>
// * @author Charles-Philip Bentley
// *
// */
//public class Repeater2 implements Runnable {
//
//   /**
//    * Count the number of wake up calls
//    */
//   int                      count          = 0;
//
//   public boolean           isPaused       = false;
//
//   public boolean           run            = true;
//
//   public boolean           waitAgain;
//
//   public boolean           waiting        = false;
//
//   long                     waitTime;
//
//   boolean                  isCallSerially = true;
//
//   private CanvasControlled str;
//
//   public Repeater2(CanvasControlled ctr) {
//      this.str = ctr;
//      waitTime = str.getInputStateCurrent().getTech().get4(ITechInput.INPUT_OFFSET_01_WAIT_REPEAT_KEY_4);
//   }
//
//   /**
//    * Process the event code in the repeater thread.
//    * Synchronization issues arise<br>
//    * <li>
//    */
//   public synchronized void run() {
//      while (run) {
//         try {
//            waitPeriod();
//            if (isPaused) {
//               count = 0;
//               //wait till notified again to generate repeat events
//               wait();
//               //woken up. is going to wait a little then start a repeat event if not paused
//            } else {
//               count++;
//               //System.out.println("Repeat " + count);
//               if (isCallSerially) {
//                  str.getDevice().callSerially(new Runnable() {
//                     public void run() {
//                        str.keyPressedRepeated();
//                     }
//                  });
//               } else {
//                  str.keyPressedRepeated();
//               }
//               //the controller may change the time period to suit the need of the command
//               waitTime = str.getInputStateCurrent().getWaitTimeKey();
//            }
//         } catch (InterruptedException e) {
//            e.printStackTrace();
//         }
//      }
//   }
//
//   /**
//    * Stops the repeater
//    */
//   public void stop() {
//      //SystemLog.printFlow("#Repeater#stop " + ic.toString1Line());
//      isPaused = true;
//   }
//
//   public void toString(StringBuilder sb, String nl) {
//      sb.append("#Repeater count=" + count + " pause=" + isPaused + " waitAgain=" + waitAgain + " waiting=" + waiting + " waitTime=" + waitTime);
//
//   }
//
//   public void toString(StringBuilder sb) {
//      sb.append("#Repeater count=" + count + " pause=" + isPaused + " waitAgain=" + waitAgain + " waiting=" + waiting + " waitTime=" + waitTime);
//   }
//
//   private void waitPeriod() throws InterruptedException {
//      if (waitTime != 0) {
//         waiting = true;
//         wait(waitTime);
//         if (waitAgain) {
//            waitAgain = false;
//            waitPeriod();
//         }
//         waiting = false;
//         //how do we know if thread was woken up?
//      }
//   }
//}