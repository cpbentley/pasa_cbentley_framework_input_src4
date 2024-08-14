package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.ExecutionContextCanvas;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.engine.OutputStateCanvas;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * Implementation controls how event state is added to the {@link InputState} for a given {@link CanvasAppliInput}.
 * 
 * <p>
 * It is a layer  that may decide to drop and postpone events because of {@link CanvasAppliInput} settings 
 * such as thread configurations.
 * </p>
 * 
 * @author Charles Bentley
 *
 */
public abstract class EventController extends ObjectIC implements IStringable {

   protected CanvasAppliInput  canvas;

   /**
    * The current {@link InputState}. or the image of the whole queue.
    * <br>
    * In a single thread there is only a single reference.
    * <br>
    * In Mode {@link ITechThreadPaint#THREADING_1_UI_UPDATERENDERING},
    * the UI thread updates the {@link InputState}
    * 
    * <br>
    * Any thread can get a reference from this. But it is not advisable.
    * <br>
    * Depending on the thread model
    */
   protected InputStateCanvas  inputState;

   protected OutputStateCanvas outputState;

   public EventController(InputCtx ic, CanvasAppliInput canvas) {
      super(ic);
      this.canvas = canvas;
      inputState = (InputStateCanvas) canvas.createInputState();
      outputState = (OutputStateCanvas) canvas.createOutputState();
   }

   public CanvasAppliInput getCanvas() {
      return canvas;
   }

   /**
    * 
    * Fetch the current {@link InputStateCanvas}, i.e. the very latest {@link InputStateCanvas}.
    * <br>
    * The {@link InputStateCanvas} is updated by the {@link ITechThreadPaint#THREAD_0_HOST_HUI} by external
    * events.
    * <br>
    * In Threading Configuration 
    * <li>{@link ITechThreadPaint#THREADING_1_UI_UPDATERENDERING} 
    * <li>{@link ITechThreadPaint#THREADING_3_THREE_SEPARATE} 
    * <br>
    * In Threading Configuration
    * <li>{@link ITechThreadPaint#THREADING_2_UIUPDATE_RENDERING} 
    * <br>
    * Implementation may override for specific behavior.
    * @return
    */
   public InputStateCanvas getInputState() {
      return inputState;
   }

   public OutputStateCanvas getOutputState() {
      return outputState;
   }

   /**
    * 
    * @param g
    */
   public abstract void event(BEvent g);

   /**
    * 
    * @param is
    * @param g
    * @return
    */
   public boolean event(InputState is, BEvent g, CanvasAppliInput canvas) {
      boolean isAccepted = is.addEvent(g, canvas);
      return isAccepted;
   }

   public void paintStart() {

   }

   /**
    * Called in the render thread.
    */
   public void paintFinished() {

   }

   /**
    * Called at the end of event processing
    * @param ec TODO
    * @param is
    */
   public void endOfExecutionEvent(ExecutionContextCanvas ec, InputStateCanvas is) {

   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, EventController.class, 100);
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, EventController.class, 100);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug

}
