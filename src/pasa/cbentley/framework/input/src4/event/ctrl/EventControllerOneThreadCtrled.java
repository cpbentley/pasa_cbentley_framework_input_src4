package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.DragController3;
import pasa.cbentley.framework.input.src4.InputRequests;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

public class EventControllerOneThreadCtrled extends EventControllerOneThread {
   private DragController3 dragCtrl;

   public EventControllerOneThreadCtrled(InputCtx ic,CanvasAppliInput canvas) {
      super(ic,canvas);
      dragCtrl = new DragController3(ic, this);
      Thread t = new Thread(dragCtrl, "Drag");
      t.start();
   }

   
   public void paintFinished() {
      super.paintFinished();
      dragCtrl.paintFinished();

   }

   
   public void paintStart() {
      super.paintStart();
      dragCtrl.paintStart();
   }

   
   public void pointerDragged(int x, int y, int id) {
      dragCtrl.addEventDragged(x, y, id);
   }

   public void pointerDraggedCtrl(int x, int y, int id) {
   }

   
   public void pointerPressed(int x, int y, int id, int button) {
      dragCtrl.addEventPress(x, y, id, button);
   }

   /**
    * When a Press is first computed, the code
    * looks for the interest of a command reacting
    * on a long or repetition.
    * This request is made through {@link InputRequests}.
    * <br>
    * If both long and repetition interests, a long event will first
    * be generated and will be repeated.
    * This depends on the context of event.
    * If a command manager has the long press 5 secs and swipe up and down
    * @param x
    * @param y
    * @param id
    */
   public void pointerPressedCtrl(int x, int y, int id, int button) {
   }

   
   public void pointerReleased(int x, int y, int id, int button) {
      dragCtrl.addEventRelease(x, y, id, button);
   }

   /**
    * Android Long Presses can be used for default timers. If commands
    * requires a custom long press timer, we will use one here.
    * <br>
    * @param x
    * @param y
    * @param id
    */
   public void pointerReleasedCtrl(int x, int y, int id, int button) {
   }
   
   //#mdebug
   
   public void toString(Dctx dc) {
      dc.root(this, "EventControllerOneThreadCtrled");
      super.toString(dc.sup());
   }

   
   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "EventControllerOneThreadCtrled");
   }
   //#enddebug
}
