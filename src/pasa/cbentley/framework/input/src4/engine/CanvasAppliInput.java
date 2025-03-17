package pasa.cbentley.framework.input.src4.engine;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.stator.StatorWriterBO;
import pasa.cbentley.core.src4.event.BusEvent;
import pasa.cbentley.core.src4.event.IEventConsumer;
import pasa.cbentley.core.src4.interfaces.IExecutor;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.ITechDLogConfig;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.core.src4.stator.StatorReader;
import pasa.cbentley.core.src4.stator.StatorWriter;
import pasa.cbentley.core.src4.structs.synch.FairLock;
import pasa.cbentley.core.src4.structs.synch.MutexSignal;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.framework.core.ui.src4.ctx.CoreUiCtx;
import pasa.cbentley.framework.core.ui.src4.engine.CanvasAppliAbstract;
import pasa.cbentley.framework.core.ui.src4.event.BEvent;
import pasa.cbentley.framework.core.ui.src4.event.DeviceEvent;
import pasa.cbentley.framework.core.ui.src4.event.DeviceEventXY;
import pasa.cbentley.framework.core.ui.src4.event.GestureEvent;
import pasa.cbentley.framework.core.ui.src4.exec.ExecutionContext;
import pasa.cbentley.framework.core.ui.src4.exec.OutputState;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.core.ui.src4.input.ScreenOrientationCtrl;
import pasa.cbentley.framework.core.ui.src4.interfaces.ICanvasAppli;
import pasa.cbentley.framework.core.ui.src4.interfaces.ITechSenses;
import pasa.cbentley.framework.core.ui.src4.tech.IBOCanvasHost;
import pasa.cbentley.framework.core.ui.src4.tech.ITechInput;
import pasa.cbentley.framework.core.ui.src4.tech.ITechEvent;
import pasa.cbentley.framework.core.ui.src4.tech.ITechFeaturesCanvas;
import pasa.cbentley.framework.core.ui.src4.tech.ITechHostUI;
import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;
import pasa.cbentley.framework.coredraw.src4.interfaces.IImage;
import pasa.cbentley.framework.coredraw.src4.interfaces.ITechGraphics;
import pasa.cbentley.framework.input.src4.ctx.IBOCtxSettingsInput;
import pasa.cbentley.framework.input.src4.ctx.IBOTypesInput;
import pasa.cbentley.framework.input.src4.ctx.IFlagsToStringInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ToStringStaticInput;
import pasa.cbentley.framework.input.src4.event.ctrl.EventController;
import pasa.cbentley.framework.input.src4.event.ctrl.EventControllerOneThread;
import pasa.cbentley.framework.input.src4.event.ctrl.EventControllerOneThreadCtrled;
import pasa.cbentley.framework.input.src4.event.ctrl.EventControllerQueuedSynchro;
import pasa.cbentley.framework.input.src4.event.jobs.GestureTrailJob;
import pasa.cbentley.framework.input.src4.event.jobs.JobsEventRunner;
import pasa.cbentley.framework.input.src4.game.CanvasLoopGameFramed;
import pasa.cbentley.framework.input.src4.game.FrameData;
import pasa.cbentley.framework.input.src4.gesture.GestureDetector;
import pasa.cbentley.framework.input.src4.interfaces.IBOCanvasAppli;
import pasa.cbentley.framework.input.src4.interfaces.IJobEvent;
import pasa.cbentley.framework.input.src4.interfaces.ISimulationUpdatable;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;
import pasa.cbentley.framework.input.src4.threading.CanvasLoop;
import pasa.cbentley.framework.input.src4.threading.CanvasLoopGame;
import pasa.cbentley.framework.input.src4.threading.LoopThreadRender;
import pasa.cbentley.framework.input.src4.threading.LoopThreadUpdate;
import pasa.cbentley.framework.input.src4.threading.Simulation;

/**
 * Controls the event generation and results to and from 
 * <br>
 * Q:Can we have several {@link CanvasAppliInput} per application?<br>
 * A:Yes.<br>
 * 
 * <br>
 * <br>
 * <b>Responsabilities:</b> 
 * <li>Event control : Key, Pointer, Menu
 * <li>Repeatability for Random and Date
 * <li>Focus control and dispatch between {@link IDrawable}.
 * <li>Associates the GUI menu bar with the {@link CmdCtx} and the {@link MCmd}s.
 * <br>
 * <br>
 * <b>Commands</b>: <br>
 * 3 Modes for generating commands :
 * <li>Cabled : trigger is hardcoded in the {@link IDrawable#manageKeyInput(InputState)}.
 * <li>Loose : A Trie matches InputConfig signature with a {@link MCmd} in a given {@link CmdCtx}. Commands trigger may be dynamically changed.
 * <li>Menu: explicit generation from a visual menu. Change of Trigger is irrelevant. Menu commands is just a special view
 * of available commands in a given context.
 * <br> 
 * <br>
 * <b>Screen Orientation</b> :
 * <br>
 * <li>Host may modidify the screen orientation because of external events. (hardware)
 * <li>the user may decide to rotate the screen (software)
 * <br>
 * When the Host does not support The API generates an event which is caught by the canvas.
 * <br>
 * Canvas settings {@link IBOCanvasAppli#CANVAS_APP_OFFSET_04_SCREEN_MODE1} is updated. if event is internal
 * <br>
 * 
 * <b>Focuses</b> :
 * <li>Key focus. {@link IDrawable#STYLE_06_FOCUSED_KEY}
 * <li>Pointer focus.  Pointer is over. {@link IDrawable#STYLE_07_FOCUSED_POINTER}. It depends on the device.When Pointer is Pressed or Mouse is Over, depe
 * <li>Menu Focus. This is like the Window focus When the {@link CmdCtx} is shown in the Menu.
 * <br>
 * <br>
 * <b>Focus Control</b> <br>
 * {@link IDrawable} : {@link CanvasAppliInput#currentNavInFocus} is the Master Nav In Focus. It is the first to recieve a navigation key event.
 * <br>
 * Event method {@link IDrawable#navigateDown(InputState)} is only called if {@link IDrawable#getNavFlag()} return {@link IDrawable#FLAG_VERTI}.
 * <br>
 * Next {@link IDrawable} in the chain
 * This feature allows two {@link IDrawable} to have the key focus at the same time without being related.
 * A {@link IDrawable} may call the {@link CanvasAppliInput#navigateOut} method to notify that focus should change.
 * The navigational gets the InputConfig first.
 * <br>
 * Don't use it most of the time, use the hierachy
 * A {@link ViewPane} usually don't use this hack UpDown LeftRight dissociation.
 * Case of Master UpDown, Slave LeftRight <br> 
 * When a more specific Drawable is selected inside. It can either take Nav focus immediately or wait
 * for the Select Nav to be issued. <br> 
 * When A Nav UpDown is recieved, slaved Nav loses its slaved key focus. If Select is not a valid, select
 * also removes Focus and Next element in the UpDown Nav recieves focus. A Navigational keeps track of a Vector
 * CurrentPosition Up. 

 * <br>
 * <br>
 * 
 * The controller requests
 * <br>
 * When Focus, change
 * 
 * Temporary FOCUS_LOST events are sent when a Drawable is losing the focus, but will regain the focus shortly. <br>
 * These events can be useful when focus changes are used as triggers for validation of data. <br>
 * For instance, a {@link StringDrawable} may want to commit its contents when the user focuses on another {@link Drawable}. 
 * <br>
 * It can accomplish this by responding to FOCUS_LOST events. <br>
 * However, if the FocusEvent received is temporary, the commit should not be done, since the text field will be receiving the focus again shortly.
 * <br>
 * <br>
 *  UpDown LeftRight dissociation
 * Case of Master UpDown, Slave LeftRight
 * When a more specific Drawable is selected inside. It can either take Nav focus immediately or wait for the Select Nav to be issued
 * When A Nav UpDown is recieved, slaved Nav loses its slaved key focus. If Select is not a valid, select
 * also removes Focus and Next element in the UpDown Nav recieves focus. A Navigational keeps track of a Vector
 * CurrentPosition Up
 * 
 * However if in a UpDown, a Drawable is UpDownLeftRightSelect, there is no way out except reaching the end of the Navigation.
 * <br>
 *  AroundTheClock is disabled. A Tab Key command, similar to the TAB-ALT-TAB in Windows, allows to get out and continue according to the Vector 
 * <br>
 * <br>
 * <b>Event Delivery </b>: <br>
 * <li>Top-Down : Event is given to {@link CanvasAppliInput}, {@link InputState} is updated. that directs to its children.
 * <li>Bottom-up: {@link IDrawable} in with focus first recieves event first.
 * <br>
 * <br>
 * Command events are Bottom-Up. Active {@link IDrawable} whose {@link CmdCtx} generated the command gets it.
 * 
 * 
 * <br>
 * <br>
 * 
 * <b>Event Life Cycle</b>
 * <li>Framework calls {@link MasterCanvas} event method
 * <li>{@link CanvasAppliInput} event method is called 
 * <li> {@link DeviceDriver} may modifies/translate event value to match Framework Event specification
 * <li> {@link InputState} is updated 
 * <li> For Key events, check if {@link InputState} generates a Navigation command.
 * <li> Send it to active {@link IDrawable}. When acted, change Focus state.
 * <li> Send it to Focused Drawable. {@link IDrawable#STYLE_06_FOCUSED_KEY}
 * <li> 
 * <br>
 * <b>Examples</b>:
 * <br>
 * <br>
 * Popup drawable with vertical navigation. When activated, 
 * <li>New DLayer with {@link MasterCanvas#loadDLayer(IDrawable, int)} with {@link IMaster#SHOW_TYPE_1OVER} 
 * <li>Event {@link IDrawable#EVENT_04_KEY_FOCUS_LOSS} for root Drawable
 * <li>Event {@link IDrawable#EVENT_03_KEY_FOCUS_GAIN}  for the popup drawable
 * <li>Possibly a new Master {@link IDrawable}.
 * <li>Pop
 * <li>Root Drawable registers on to learn when Popup is closed
 * <li>
 * <br>
 * <br>
 * <br>
 * State starts with first {@link StringDrawable} with Select and Key Focus state. It is a {@link IDrawable} horiz and vertically Top only
 * to reach the {@link StringEditControl}, if it is following {@link IStringDrawable#SEDIT_OFFSET_05_CONTROL_POSITION1}
 * <br>
 * When Down is Pressed, {@link StringDrawable} , if it has a viewpane scrolls down else delegates a Move Out to Controller who kept a reference of the {@link IDrawable}
 * next in line to deal with the Navigation event. TableView gets event and move down Select and Focus state to the next StringDrawable.
 * Focus lost and then Focus in, StringDrawable set itself in edit mode with the {@link EditModule}. {@link StringEditControl} is moved accordingly.
 * <br>
 * When a ViewPane is there, there is an indirection.
 * <br>
 * When String is not editable but has a ViewPane for scrolling, {@link StringDrawable} may not be set as active {@link IDrawable} until a Select
 * command is issued in the TableView.
 * <br>
 * <br>
 * Command events.
 * <br>
 * <br>
 * TODO During a Drag repaint, drawable fx are disable. Paint must be lightning fast on the drawable dragged.
 * Differentiate some repaint request relative to performance switches.
 * <br>
 * <br>
 * @author Charles-Philip Bentley
 *
 * @see MCmd
 * @see MCtx
 */
public abstract class CanvasAppliInput extends CanvasAppliAbstract implements ICanvasAppli, IEventConsumer, IBOCtxSettingsInput {

   public static final boolean   IS_CLIPPING_CHECK = false;

   private int                   bgColor;

   /**
    * {@link IBOCanvasAppli}
    */
   protected ByteObject          boCanvasAppli;

   private int                   debugFlags;

   /**
    * Manages key/pointer events depending on the threading mode.
    */
   protected EventController     eventController;

   /**
    * Structure that contains repeat events and long press events
    * and other {@link IJobEvent} such as repeating gestures {@link GestureTrailJob}.
    * 
    * TODO clear the queue when loss of focus.
    * 
    * Single instance per Canvas. Compatible with all threading modes
    */
   JobsEventRunner               eventRun;

   private Thread                eventThread;

   private CanvasLoopGame        gameLoop;

   protected final InputCtx      ic;

   private boolean               isCtrlEnabled;

   protected boolean             isDebugInputConfig;

   protected volatile boolean    isRunning         = true;

   private LoopThreadRender      render;

   /**
    * Null when {@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}
    */
   FairLock                      renderLock;

   /**
    * Created by {@link CanvasAppliInput#a_Init()}
    */
   protected RepaintHelper       repaintHelper;

   private ScreenOrientationCtrl screenCtrl;

   MutexSignal                   sema;

   /**
    * The simulation that will display on the canvas.
    * 
    * GUI kit Animations are {@link ISimulationUpdatable}s 
    * GameLoop is a simulation
    */
   private Simulation            simulationCanvas;

   /**
    * Set by the constructor.
    * 
    * Starts in One Thread GUI.
    * <br>
    * 
    * <li>{@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}
    * <li>{@link ITechThreadPaint#THREADING_1_UI_UPDATERENDERING}
    * <li>{@link ITechThreadPaint#THREADING_2_UIUPDATE_RENDERING}
    * <li>{@link ITechThreadPaint#THREADING_3_THREE_SEPARATE}
    * 
    */
   private int                   threadingMode     = -1;

   /**
    * Initialized to the thread that will render.
    * 
    * <br>
    * When Host is initialized, we cannnot be sure of the running thread.
    * Therefore, the render thread in passive will be set during the first
    * rendering
    */
   private Thread                threadRender;

   /**
    * The update thread wait until
    * <li>an event is put into the queue.
    * <li>A Runnable job to be run in the update thread is recieved
    */
   private Thread                threadUpdate;

   private Thread                threadUpdateRender;

   /**
    * Null when {@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}
    */
   private FairLock              updateLock;

   private LoopThreadUpdate      updater;

   private volatile boolean      waiters           = false;

   /**
    * Creates and register the {@link ModuleInput} singleton with {@link ModuleInput#get(IFrameworkCtx)}.
    * <br>
    * The default {@link IBOCanvasAppli} settings from {@link InputCtx#createBOTechCanvasAppliDefault()} are used. 
    * 
    * @param ic
    */
   public CanvasAppliInput(InputCtx ic) {
      this(ic, ic.createBOTechCanvasAppliDefault(), ic.getCUC().createBOCanvasHostDefault());
   }

   /**
    * 
    * @param ic
    * @param techCanvasAppli {@link IBOCanvasAppli}
    */
   public CanvasAppliInput(InputCtx ic, ByteObject techCanvasAppli) {
      this(ic, techCanvasAppli, ic.getCUC().createBOCanvasHostDefault());
   }

   /**
    * Creates an {@link CanvasAppliInput} with the given Host and Appli parameters
    * 
    * Subclass must call {@link CanvasAppliInput#a_Init()} at the end of its constructor.
    * 
    * @param ic {@link InputCtx}
    * @param boCanvasAppli {@link IBOCanvasAppli}
    * @param boCanvasHost  {@link IBOCanvasHost}
    */
   public CanvasAppliInput(InputCtx ic, ByteObject boCanvasAppli, ByteObject boCanvasHost) {
      super(ic.getCUC(), boCanvasHost);
      this.ic = ic;
      if (boCanvasAppli == null) {
         throw new NullPointerException();
      }
      //#debug
      boCanvasAppli.checkType(IBOTypesInput.TYPE_1_CANVAS_APPLI);

      this.boCanvasAppli = boCanvasAppli;
      threadRender = Thread.currentThread();
      threadUpdate = Thread.currentThread();
      threadUpdateRender = Thread.currentThread();
      eventThread = Thread.currentThread();
      sema = new MutexSignal(ic.getUC());

      //#debug
      toDLog().pCreate("Init Thread=" + threadRender, this, CanvasAppliInput.class, "Created@356", LVL_04_FINER, true);

   }

   /**
    * Convention is that this method is called by the extending class
    */
   public void a_Init() {
      repaintHelper = createRepaintHelper();
      eventRun = new JobsEventRunner(ic, this); //thread will auto start when first job is added

      applySettings(boCanvasAppli);
   }

   /**
    * If Render thread is painting, calling thread waits.
    * <br>
    * If not painting, it goes through.
    * Will woke up next time
    * @throws InterruptedException
    */
   public void addPainterWait() throws InterruptedException {
      if (!waiters) {
         waiters = true;
      }
      synchronized (sema) {
         sema.acquire();
      }
   }

   /**
    * Applies the {@link IBOCanvasAppli}
    */
   public void applySettings(ByteObject boCanvasAppli) {

      boCanvasAppli.checkType(IBOTypesInput.TYPE_1_CANVAS_APPLI);

      //debug settings
      int threadMode = boCanvasAppli.get1(IBOCanvasAppli.CANVAS_APP_OFFSET_03_THREADING_MODE1);
      setThreadingMode(threadMode);
      boolean b = boCanvasAppli.hasFlag(IBOCanvasAppli.CANVAS_APP_OFFSET_01_FLAG, IBOCanvasAppli.CANVAS_APP_FLAG_1_FULLSCREEN);
      //apply settings on canvas now?
      setFullScreenMode(b);

      int debugF = boCanvasAppli.get1(IBOCanvasAppli.CANVAS_APP_OFFSET_05_DEBUG_FLAGS1);
      debugFlags = debugF;
      bgColor = boCanvasAppli.get4(IBOCanvasAppli.CANVAS_APP_OFFSET_06_BG_COLOR4);
   }

   public void checkThreadRender() {
      if (!isThreadRender()) {
         throw new IllegalThreadStateException("Must be the Render Thread");
      }
   }

   public void checkThreadUpdate() {
      if (!isThreadUpdate()) {
         throw new IllegalThreadStateException("Must be the Update Thread");
      }
   }

   public void consumeEvent(BusEvent e) {

   }

   /**
    * A GUI will override this method to return a more specific {@link ExecutionContext}
    */
   public ExecutionContext createExecutionContext() {
      return new ExecutionContextCanvas(ic);
   }

   /**
    * Raw not initialized
    * @return
    */
   public ExecutionContextCanvas createExecutionContextCanvas() {
      ExecutionContext ec = createExecutionContext();
      return (ExecutionContextCanvas) ec;
   }

   public ExecutionContextCanvas createExecutionContextEvent() {
      ExecutionContextCanvas ec = createExecutionContextCanvas();
      InputStateCanvas is = eventController.getInputState();
      ec.setInputState(is);
      OutputStateCanvas os = eventController.getOutputState();
      ec.setOutputState(os);
      return ec;
   }

   public ExecutionContextCanvas createExecutionContextPaint() {
      ExecutionContextCanvas ec = createExecutionContextCanvas();
      InputStateCanvas is = eventController.getInputState();
      ec.setInputState(is);
      OutputStateCanvas os = repaintHelper.getNextRender();
      ec.setOutputState(os);
      return ec;
   }

   /**
    * Used by {@link EventController} implementation to add events to it according to the controller strategy
    * @return
    */
   public InputState createInputState() {
      return new InputStateCanvas(ic, this);
   }

   public OutputState createOutputState() {
      return new OutputStateCanvas(ic, this);
   }

   /**
    * Override this for more specific {@link RepaintHelper}
    * @return
    */
   protected RepaintHelper createRepaintHelper() {
      return new RepaintHelper(ic, this);
   }

   /**
    * Called in the update thread {@link ITechThreadPaint#THREAD_1_UPDATE}.
    * <br>
    * Process the requests {@link InputRequests}
    * @param is
    * @param sr
    */
   private void ctrlEventEnd(InputStateCanvas is, OutputStateCanvas sr) {
      final int eventID = is.getEventID();
      switch (eventID) {
         case ITechEvent.EVID_01_KEYBOARD_PRESS:
            keyPressedEnd(is, sr);
            break;
         case ITechEvent.EVID_02_KEYBOARD_RELEASE:
            keyReleasedEnd(is, sr);
            break;
         case ITechEvent.EVID_11_POINTER_PRESS:
            pointerPressedEnd(is, sr);
            break;
         case ITechEvent.EVID_12_POINTER_RELEASE:
            pointerReleasedEnd(is, sr);
            break;
         case ITechEvent.EVID_13_POINTER_MOVE:
            pointerMovedEnd(is, sr);
            break;
         case ITechEvent.EVID_14_POINTER_DRAG:
            pointerDraggedEnd(is, sr);
            break;
         case ITechEvent.EVID_15_GESTURE:
            pointerGesturedEnd(is, sr);
            break;
         case ITechEvent.EVID_20_WHEEL:
            pointerWheelEnd(is, sr);
            break;
         default:
            break;
      }
   }

   /**
    * Called in the update thread {@link ITechThreadPaint#THREAD_1_UPDATE}.
    * <br>
    * Time for generic actions.
    * <br>
    * <br>
    * Gesture Destroyer:
    * <li> Notifies {@link GestureDetector}
    * @param is
    * @param sr
    */
   private void ctrlEventStart(InputStateCanvas is, OutputStateCanvas sr) {
      final int eventID = is.getEventID();
      switch (eventID) {
         case ITechEvent.EVID_01_KEYBOARD_PRESS:
            keyPressedStart(is, sr);
            break;
         case ITechEvent.EVID_02_KEYBOARD_RELEASE:
            keyReleasedStart(is, sr);
            break;
         case ITechEvent.EVID_14_POINTER_DRAG:
            pointerDraggedStart(is, sr);
            break;
         case ITechEvent.EVID_12_POINTER_RELEASE:
            pointerReleasedStart(is, sr);
            break;
         case ITechEvent.EVID_11_POINTER_PRESS:
            pointerPressedStart(is, sr);
            break;
         case ITechEvent.EVID_15_GESTURE:
            pointerGesturedStart(is, sr);
            break;
         case ITechEvent.EVID_13_POINTER_MOVE:
            pointerMovedStart(is, sr);
            break;
         case ITechEvent.EVID_20_WHEEL:
            pointerWheelStart(is, sr);
            break;
         case ITechEvent.EVID_40_CANVAS:
            startEventCanvas(is, sr);
            break;
         default:
            break;
      }
   }

   private void ctrlInputRequests(InputStateCanvas is) {
      //take new requests.. fire 1 time requests.. merge new with root requests
      if (is.hasInputRequests()) {

         //repeats
         InputRequests ir = is.getInputRequestNew();
         ir.apply(is);
         ir.clear();

         //TODO gesture trail
         //upon pointer release check gesture detector to cancel long press
         //         if (ir.getGesturesFlag() != 0) {
         //            int pointerID = is.getPointerID();
         //            GestureDetector gd = gestureCtrl.getGesture(pointerID);
         //            if (ir.hasLongPress()) {
         //               if (is.isPressed()) {
         //                  //start
         //               }
         //
         //            }
         //            if (is.isDragged()) {
         //               //compare position with timing
         //               if (ir.hasSlide()) {
         //
         //               }
         //               gd.simpleDrag(is);
         //            }
         //         }
      }
   }

   protected void ctrlKey(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {
      int mod = is.getMode();
      switch (mod) {
         case ITechInput.MOD_0_PRESSED:
            ctrlKeyPressed(ec, is, sr);
            break;
         case ITechInput.MOD_1_RELEASED:
            ctrlKeyReleased(is, sr);
            break;
         default:
            break;
      }
   }

   /**
    * Called in the update thread {@link ITechThreadPaint#THREAD_1_UPDATE}
    * @param ec TODO
    * @param ic
    */
   protected void ctrlKeyPressed(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {

   }

   protected void ctrlKeyReleased(InputStateCanvas is, OutputStateCanvas sr) {

   }

   /**
    * Convienence.
    * 
    * All ctrl methods are disable by default. To enable
    * @param ec TODO
    * @param sr
    * @param ic
    */
   protected void ctrlPointer(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {
      //#debug
      toDLog().pFlow("", this, CanvasAppliInput.class, "ctrlPointer", LVL_05_FINE, true);

      int mod = is.getMode();
      switch (mod) {
         case ITechInput.MOD_0_PRESSED:
            ctrlPointerPressed(ec, is, sr);
            break;
         case ITechInput.MOD_1_RELEASED:
            ctrlPointerReleased(ec, is, sr);
            break;
         case ITechInput.MOD_3_MOVED:
            ctrlPointerMoved(ec, is, sr);
            break;
         case ITechInput.MOD_5_WHEELED:
            ctrlPointerWheeled(ec, is, sr);
            break;
         default:
            break;
      }

   }

   protected void ctrlPointerDragged(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {

   }

   /**
    * Routing method for Pointer 
    * @param ec TODO
    * @param ic
    */
   protected void ctrlPointerMoved(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {

   }

   protected void ctrlPointerPressed(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {

   }

   protected void ctrlPointerReleased(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {

   }

   /**
    * Override this
    * @param ec TODO
    * @param ic
    * @param sr
    */
   protected void ctrlPointerWheeled(ExecutionContextCanvas ec, InputStateCanvas ic, OutputStateCanvas sr) {

   }

   /**
    * Canvas implementation overrides this method when it wants to full control.
    * <br>
    * <br>
    * It can also call {@link CanvasAppliInput#processEventToCanvas(InputState, OutputStateCanvas)}.
    * This method does the basic separation
    * @param is
    * @param sr
    */
   protected abstract void ctrlUIEvent(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr);

   /**
    * We delegate to the {@link EventController}.
    * 
    * When {@link EventControllerOneThreadCtrled}, it queues pointer move event when necessary. <a>
    * It is chosen by the gameloop if any.
    */
   protected void eventToCanvas(BEvent ev) {

      //#debug
      toDLog().pEvent("START ->", ev, CanvasAppliInput.class, "eventToCanvas@"+toStringGetLine(700), LVL_04_FINER, DEV_X_ONELINE_THREAD);

      eventController.event(ev);

      //release lock
      //so when another tread wants it need to acquire lock ?

      //#debug
      toDLog().pEvent("END ->", ev, CanvasAppliInput.class, "eventToCanvas@"+toStringGetLine(700), LVL_04_FINER, DEV_X_ONELINE_THREAD);

   }

   /**
    * Called when the application loses the input context.
    * <br>
    * unable to 
    */
   public void exitInputContext() {
      InputStateCanvas is = eventController.getInputState();
      is.resetPresses();
   }

   public void fixRotation(DeviceEventXY dex) {
      if (screenCtrl != null) {
         if (dex.getDeviceMode() != ITechInput.MOD_5_WHEELED) {
            int x = dex.getX();
            int y = dex.getY();
            dex.updateX(screenCtrl.rotationXChange(x, y));
            dex.updateY(screenCtrl.rotationYChange(x, y));
         }
      }
   }

   public void fixRotationKey(DeviceEvent dex) {
      if (screenCtrl != null) {
         int newButton = screenCtrl.rotationKeyChange(dex.getDeviceButton());
         dex.updateButton(newButton);
      }
   }

   /**
    * The default background color of this canvas. Usually either black or white.
    * @return
    */
   public int getBgColor() {
      return bgColor;
   }

   /**
    * {@link IBOCanvasAppli}
    * @return
    */
   public ByteObject getBOCanvasAppli() {
      return boCanvasAppli;
   }

   public EventController getEventController() {
      return eventController;
   }

   public JobsEventRunner getEventRunner() {
      return eventRun;
   }

   public Thread getEventThread() {
      return eventThread;
   }

   public int getHeight() {
      if (screenCtrl != null) {
         int orientation = screenCtrl.getOrientation();
         if (orientation == ITechHostUI.SCREEN_0_TOP_NORMAL || orientation == ITechHostUI.SCREEN_1_BOT_UPSIDEDOWN) {
            return super.getHeight();
         } else {
            return super.getWidth();
         }
      } else {
         return super.getHeight();
      }
   }

   public InputCtx getIC() {
      return ic;
   }

   /**
    * Returns the {@link InputStateCanvas} being used to aggregate events on this canvas.
    * 
    * what about several canvases ?
    * <li>each {@link CanvasAppliInput} has its own {@link InputStateCanvas}.
    * <li>When mouse is going out of Canvas and into another Canvas, new Canvas has the option to reset
    * or take data from previous canvas
    * <li>Gamepads maybe be assigned a Canvas towards which 
    * <li> Some events are published on all Canvases with {@link CoreUiCtx#publishEventOnAllCanvas(BEvent)}
    * Every Canvas will process it
    * 
    * @return
    */
   InputStateCanvas getInputStateCanvas() {
      return eventController.getInputState();
   }

   public RepaintHelper getRepaintCtrl() {
      return repaintHelper;
   }

   /**
    * Returns the {@link Simulation} object of this {@link CanvasAppliInput} on which apps can add
    * {@link ISimulationUpdatable} that want to tick tock to the business time of this canvas' simulation.
    * @return
    */
   public Simulation getSimulationLazy() {
      if (simulationCanvas == null) {
         simulationCanvas = new Simulation(ic);
      }
      return simulationCanvas;
   }

   public int getThreadingMode() {
      return threadingMode;
   }

   public int getWidth() {
      if (screenCtrl != null) {
         int orientation = screenCtrl.getOrientation();
         if (orientation == ITechHostUI.SCREEN_0_TOP_NORMAL || orientation == ITechHostUI.SCREEN_1_BOT_UPSIDEDOWN) {
            return super.getWidth();
         } else {
            return super.getHeight();
         }
      } else {
         return super.getWidth();
      }
   }

   /**
    * {@link IFlagsToStringInput}
    * @param flag
    * @return
    */
   public boolean hasDebugFlag(int flag) {
      return BitUtils.hasFlag(debugFlags, flag);
   }

   protected boolean isCtrlEnabled() {
      return isCtrlEnabled;
   }

   public boolean isDragControlled() {
      return eventController instanceof EventControllerOneThreadCtrled;
   }

   /**
    * Is the current thread the event thread.
    * @return
    */
   public boolean isThreadEvent() {
      return Thread.currentThread() == eventThread;
   }

   /**
    * Is the current thread the render thread.
    * @return
    */
   public boolean isThreadRender() {
      Thread t = Thread.currentThread();
      //#debug
      //toLog().ptInit(t + " vs "+ threadRender, null, CanvasControlled.class, "isRenderThread");
      return t == threadRender;
   }

   /**
    * Is the current thread the update thread.
    * @return
    */
   public boolean isThreadUpdate() {
      Thread t = Thread.currentThread();
      //#debug
      //toLog().ptInit(t + " vs "+ threadRender, null, CanvasControlled.class, "isRenderThread");
      return t == threadUpdate;
   }

   /**
    * After the process, check if a request for repeat was made by the code
    * <br>
    * <br>
    * 
    * @param is
    * @param sr
    */
   private void keyPressedEnd(InputStateCanvas is, OutputStateCanvas sr) {
   }

   private void keyPressedStart(InputStateCanvas is, OutputStateCanvas sr) {
   }

   private void keyReleasedEnd(InputStateCanvas is, OutputStateCanvas sr) {
   }

   private void keyReleasedStart(InputStateCanvas is, OutputStateCanvas sr) {

   }

   /**
    * Call this in a Update thread to execute Render code.
    * Synchronize
    * @throws InterruptedException. do clean up before dying
    */
   public void lockAcquireRender() {
      if (renderLock != null) {
         try {
            renderLock.lock();
         } catch (InterruptedException e) {

         }
      }
   }

   /**
    * Call this in a Update thread to execute Update code.
    * Synchronize
    * @throws InterruptedException. do clean up before dying
    */
   public void lockAcquireUpdate() throws InterruptedException {
      if (updateLock != null) {
         checkThreadUpdate();
         updateLock.lock();
      }
   }

   public void lockReleaseRender() {
      if (renderLock != null) {
         renderLock.unlock();
      }
   }

   /**
    * Notify waiters that the update thread has released the lock.
    * <br>
    * Call this in a business thread to execute Update code.
    */
   public void lockReleaseUpdate() {
      if (updateLock != null) {
         updateLock.unlock();
      }
   }

   /**
    * Some Hosts requires a hard pause because no events
    * will be sent for onDestroyed ?
    */
   public void onPause() {
      //we don't need this thread to run? But why ? In J2SE it works fine
      eventRun.stop();
      threadRender.interrupt();
      threadUpdate.interrupt();
   }

   public void onUnPause() {

   }

   /**
    * Called in the thread {@link ITechThreadPaint#THREAD_0_HOST_HUI} when an external paint event is generated.
    * 
    * This is never called in other cases
    * 
    * <p>
    * The framework creates a {@link OutputStateCanvas} with a full repaint and asks
    * queue that paint request in the painting queue.
    * </p>
    * 
    * <p>
    * What about animations in {@link ITechThreadPaint#THREAD_0_HOST_HUI} ?
    * 
    * </p>
    */
   public void paint(IGraphics g) {
      eventThread = Thread.currentThread();
      //create an context of the paint, this include a merge all the ScreenResults.
      //in the active rendering mode, the context is always the same.
      //call might be made for drawing a screenshot in a separate thread
      //the rendering access data to draw

      ExecutionContextCanvas ec = createExecutionContextPaint();
      OutputStateCanvas renderCause = ec.getOutputStateCanvas();
      InputStateCanvas is = ec.getInputStateCanvas();

      if (hasDebugFlag(IFlagsToStringInput.Debug_32_Clipping_Check) && !renderCause.isClipMatch(g)) {
         //log clip mismatch
         //TODO clip mismatch with translation
         //#debug
         toDLog().pDraw("Repaint Call From Outside : Clip Mismatch " + renderCause.toStringClip(g), null, CanvasAppliInput.class, "paint", ITechLvl.LVL_05_FINE, false);

         renderCause.setRepaintFlag(ITechThreadPaint.REPAINT_01_FULL, true);
         renderCause.setRepaintFlag(ITechThreadPaint.REPAINT_02_EXTERNAL, true);
      }
      //when rendering a rotated screen. we first draw on an image ? 
      // what if host allows transformation matrix on the IGraphics
      if (screenCtrl != null && screenCtrl.isRotated()) {
         IImage img = ic.getCUC().getImageFactory().createImage(getWidth(), getHeight(), 0);
         render(img.getGraphics(), ec, is, renderCause);
         int transformation = IImage.TRANSFORM_0_NONE;
         switch (screenCtrl.getOrientation()) {
            case ITechHostUI.SCREEN_2_LEFT_ROTATED:
               transformation = IImage.TRANSFORM_5_ROT_90;
               break;
            case ITechHostUI.SCREEN_3_RIGHT_ROTATED:
               transformation = IImage.TRANSFORM_6_ROT_270;
               break;
            case ITechHostUI.SCREEN_1_BOT_UPSIDEDOWN:
               transformation = IImage.TRANSFORM_3_ROT_180;
               break;
            default:
               break;
         }
         g.drawRegion(img, 0, 0, img.getWidth(), img.getHeight(), transformation, 0, 0, ITechGraphics.TOP | ITechGraphics.LEFT);
      } else {
         //send a render request to the render thread that might be waiting
         render(g, ec, is, renderCause);
      }
   }

   /**
    * Called when the {@link CanvasAppliInput} has finished rendering.
    */
   public void paintEnd() {
      eventController.paintFinished();

      //check if at least one waiter
      if (waiters) {
         //notify waiters
         //synchronize on the structure holding the waiters
         synchronized (sema) {
            //signals waiting threads that the rendering has finished
            sema.releaseAll();
            //set performance flag to false
            waiters = false;
         }
      }
   }

   public void paintStart() {
      eventController.paintStart();
   }

   private void pointerDraggedEnd(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerDraggedEnd", LVL_03_FINEST, true);
   }

   /**
    * Inside the Update Thread
    * @param is
    * @param sr
    */
   private void pointerDraggedStart(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerDraggedStart", LVL_03_FINEST, true);
   }

   private void pointerGesturedEnd(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent(" ", null, CanvasAppliInput.class, "pointerGesturedEnd", LVL_03_FINEST, true);
   }

   private void pointerGesturedStart(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent(" ", null, CanvasAppliInput.class, "pointerGesturedStart", LVL_03_FINEST, true);
   }

   /**
    * Called in Gui thread by
    * <li> Host who detected a Gesture such as {@link ITechSenses#GESTURE_TYPE_05_SHAKE}
    * <li> {@link GestureDetector} in its thread sends residual Gesture event
    * <br>
    * <br>
    * <br>
    * a {@link GestureDetector} is generating events to be acted upon.
    * <br>
    * <br>
    * For example a fling to scroll a map and the map continue scrolling
    * on its own for a set of steps or until a touch press is recorded.
    * <br>
    * Code requests on {@link InputRequests} the gesture they are looking for.
    * Code requests Swipes up and down. for some kind of actions.
    * Upon the Touch, requests Gesture Swipes.
    * Upon Releases.
    * <br>
    * The Gesture class generates Gesture events. Long presses and others.
    * <br>
    * Allows for concurrent {@link GestureDetector}.
    * <br>
    * This method sets the {@link InputState} to Gesture mode
    * <br>
    * <br>
    * 
    * @param pg
    */
   public void pointerGestureEvent(GestureEvent g) {
      event(g);
   }

   private void pointerMovedEnd(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerMovedEnd@" + toStringGetLine(1100), LVL_03_FINEST, true);
   }

   private void pointerMovedStart(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerMovedStart@" + toStringGetLine(1105), LVL_03_FINEST, true);
   }

   /**
    * After a Press event, depending on processing, the code
    * is interested by some events.
    * <br>
    * In the input request, Gesture listener for the requested Gestures.
    * The Gesture Detector
    * @param is
    * @param sr
    */
   private void pointerPressedEnd(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerPressedEnd@" + toStringGetLine(1119), LVL_03_FINEST, true);
   }

   /**
    * The {@link GestureDetector} is created when needed by the code/components.
    * <br>
    * <br>
    * For example the Scroll bar if it wants to start a gesture.
    * @param is
    * @param sr
    */
   private void pointerPressedStart(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerPressedStart@" + toStringGetLine(1135), LVL_03_FINEST, true);
   }

   private void pointerReleasedEnd(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerReleasedEnd@" + toStringGetLine(1135), LVL_03_FINEST, true);
   }

   /**
    * Check for requested Gestures.
    * 
    * @param is
    * @param sr
    */
   private void pointerReleasedStart(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerReleasedStart@" + toStringGetLine(1135), LVL_03_FINEST, true);
   }

   private void pointerWheelEnd(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerWheelEnd@" + toStringGetLine(1135), LVL_03_FINEST, true);
   }

   private void pointerWheelStart(InputState is, OutputStateCanvas sr) {
      //#debug
      toDLog().pEvent("at [" + is.getX() + "," + is.getY() + "]", null, CanvasAppliInput.class, "pointerWheelStart@" + toStringGetLine(1135), LVL_03_FINEST, true);
   }

   public void postRotation() {

   }

   /**
    * Convenience method for sub class implementation of
    * 
    * {@link CanvasAppliInput#ctrlUIEvent(InputState, OutputStateCanvas)}.
    * 
    * <br>
    * This method will direct to the sub methods
    * <li> {@link CanvasAppliInput#ctrlKeyPressed(ExecutionContextCanvas, InputState, OutputStateCanvas)}
    * 
    * <li> {@link CanvasAppliInput#ctrlKeyReleased(InputState, OutputStateCanvas)}
    * 
    * @see CanvasAppliInput
    */
   protected void processEventToCanvas(ExecutionContextCanvas ec, InputStateCanvas ic, OutputStateCanvas sr) {
      int eid = ic.getEventID();
      switch (eid) {
         case ITechEvent.EVID_01_KEYBOARD_PRESS:
            ctrlKeyPressed(ec, ic, sr);
            break;
         case ITechEvent.EVID_02_KEYBOARD_RELEASE:
            ctrlKeyReleased(ic, sr);
            break;
         case ITechEvent.EVID_11_POINTER_PRESS:
            ctrlPointerPressed(ec, ic, sr);
            break;
         case ITechEvent.EVID_12_POINTER_RELEASE:
            ctrlPointerReleased(ec, ic, sr);
            break;
         case ITechEvent.EVID_20_WHEEL:
            ctrlPointerWheeled(ec, ic, sr);
            break;
         default:
            break;
      }
   }

   /**
    * Called in the thread {@link ITechThreadPaint#THREAD_1_UPDATE}.
    * <br>
    * and it returned true.
    * <br>
    * <br>
    * Updates the simulation/model and create a {@link OutputStateCanvas}
    * for the rendering thread  {@link ITechThreadPaint#THREAD_1_UPDATE} to process
    * @param is
    */
   public void processInputState(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas os) {
      //#debug
      toDLog().pEvent("Start of Method. Current Event=", is.getEventCurrent(), CanvasAppliInput.class, "processInputState" + toStringGetLine(1226), ITechLvl.LVL_03_FINEST, true);

      //deal with all events in our local queue.
      //usually a single event will be treated at a time.
      //however when an event cancel another prending job. a cancel event is process
      //also when a pointer is released it may send a gesture event too.
      //when a pointer select a virtual keyboard also? do we allow this or queue is better?
      //it is fine. we can't assure one event one paint anyways

      //iterate over the queue of events.. each event may cancel and create a new one.
      //focus loss event, generate release key event which in turn cancel repeat event

      //cancels long press jobs, repeats jobs.. those might generate a CANCEL event.
      // this event must be 
      eventRun.newEvent(is);
      //send cancel events here.. will be using the same screenresult?

      //process subvirtualEvents

      //after this step, cannot be queuedPre

      processInputStateEvent(ec, is, os);

      /////
      //send a screen result for the paint
      screenResult(os);

      //notify repaint control of end of event
      repaintHelper.endEvent();

      eventController.endOfExecutionEvent(ec, is); //same for eventCtrl

      //#debug
      toDLog().pEvent("End of Method", is, CanvasAppliInput.class, "processInputState@" + toStringGetLine(1259), ITechLvl.LVL_03_FINEST, true);
   }

   /**
    * 
    * @param is
    */
   public void processInputStateContinuous(InputState is) {
      //#debug
      toDLog().pFlow("", this, CanvasAppliInput.class, "processInputStateContinuous@" + toStringGetLine(1268), LVL_05_FINE, DEV_X_ONELINE_THREAD);
   }

   /**
    * 
    * <li> {@link CanvasAppliInput#ctrlEventStart(InputState, OutputStateCanvas)}
    * <li> {@link CanvasAppliInput#ctrlUIEvent(InputState, OutputStateCanvas)}
    * <li> {@link CanvasAppliInput#ctrlEventEnd(InputState, OutputStateCanvas)}
    * <li> {@link CanvasAppliInput#ctrlInputRequests(InputState)} often this will {@link InputState#queuePost(BEvent)}
    * <br>
    * @param sr
    */
   private void processInputStateEvent(ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr) {
      //send specific start of event hooks 
      ctrlEventStart(is, sr);

      //we use action.. but maybe we need to pass through every
      //several actions might be done?
      //processing by the implementation. for example commands
      if (isCtrlEnabled) {
         if (is.isTypeDevice()) {
            if (is.isTypeDevicePointer()) {
               ctrlPointer(ec, is, sr);
            } else if (is.isTypeDeviceMouse()) {
               ctrlPointer(ec, is, sr);
            } else if (is.isTypeDeviceKeyboard()) {
               ctrlKey(ec, is, sr);
            }
            //add ctrl support for others
         }
      }
      ctrlUIEvent(ec, is, sr);

      //send specific end of event hooks 
      ctrlEventEnd(is, sr);

      //in some cases, events needs to be repeated or repeitition needs to stop
      ctrlInputRequests(is);
   }

   /**
    * Render on {@link IGraphics} for the {@link OutputStateCanvas} and {@link InputStateCanvas}.
    * 
    * Any render requests is using an {@link ExecutionContextCanvas}
    * 
    * The InputState is a carbon copy of the state.
    * <br>
    * When Render is done. TODO use an interface to the input state to prevent
    * code from modifying it.
    * <br>
    * Rendering cannot write to {@link InputState}
    * @param g
    * @param ec TODO
    * @param is
    * @param sr
    */
   public abstract void render(IGraphics g, ExecutionContextCanvas ec, InputStateCanvas is, OutputStateCanvas sr);

   /**
    * Does a full repaint
    */
   public void repaint() {
      //redirect api call
      RepaintHelper repaintCtrl = getRepaintCtrl();
      OutputStateCanvas sr = repaintCtrl.getScreenResult();
      repaintCtrl.repaint(sr);
   }

   /**
    * Call this for a repaint inside the UI Thread.
    * Calls {@link DisplayableAbstract#repaint()}
    * Calling this method allows the {@link CanvasAppliInput} to better
    * synchronized repaints with other threads and compute statistics.
    * <br>
    * 
    */
   public void repaintAfterUIEvent() {
      this.repaint();
   }

   void repaintHiJack() {
      super.repaint();
   }

   private void runExec(Runnable run, boolean threadSafe) {
      IExecutor exec = ic.getExecutor();
      if (threadSafe) {
         exec.executeWorker(run); //TODO may add an ID for thread worker
      } else {
         exec.executeMainLater(run);
      }
   }

   /**
    * Any code that modifies rendering state must be done here.
    * <br>
    * @param run
    * @param threadSafe
    */
   public void runRender(Runnable run, boolean threadSafe) {
      if (Thread.currentThread() == threadRender) {
         run.run();
      } else if (threadRender != null) {
         render.queueRun(run);
      } else {
         runExec(run, threadSafe);
      }
   }

   public void runUpdate(Runnable run) {
      this.runUpdate(run, false);
   }

   /**
    * Run the Runnable in the update thread.
    * <br>
    * When there is no update thread, code is run in the ui thread or its own thread
    * <br>
    * When not thread safe, must be run
    * @param run
    */
   public void runUpdate(Runnable run, boolean threadSafe) {
      if (Thread.currentThread() == threadUpdate) {
         run.run();
      } else if (threadUpdate != null) {
         updater.queueRun(run);
      } else {
         runExec(run, threadSafe);
      }
   }

   /**
    * Decides what to do with the thread {@link ITechThreadPaint#THREAD_1_UPDATE}.
    * <br>
    * In {@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}, a repaint is called.
    * <br> and the repaint is queued
    * Otherwise, the {@link OutputStateCanvas} is queued in the Render State
    * <br>
    * <br>
    * Process the {@link OutputStateCanvas} for screen actions. 
    * <br>
    * Actions (most common first)
    * <li>renewlayout: ask for Drawable to do a layout as if VirtualCanvas dimension were changed
    * <li>repaint: no argument. the whole screen is repainted <br>
    * <li>repaint: list of Objects : only the drawables in the list are repainted. <br>
    * <li>repaint special. Flags with {@link IMaster#REPAINT_10_SPECIAL}.
    * <br>
    * <br>
    * One has to be careful of a the repeater thread that may modify the {@link InputState}.
    * <br>
    * After the screen action has been called, the {@link InputState} is reset with {@link OutputStateCanvas#resetAll()}.
   
    * @param os
    */
   public void screenResult(OutputStateCanvas os) {
      if (threadingMode == ITechThreadPaint.THREADING_0_ONE_TO_RULE_ALL) {
         boolean isDebuggingOutputCanvas = hasDebugFlag(IFlagsToStringInput.Debug_1_Controller) && os.isActionDone();
         if (isDebuggingOutputCanvas) {
            //only debug when there is an action
            //#debug
            toDLog().pFlow("", os, CanvasAppliInput.class, "screenResult");
         }
         if (hasDebugFlag(IFlagsToStringInput.Debug_8_ForceFullRepaints)) {
            os.setActionDoneRepaint();
         }
         //do we have to repaint
         if (os.isRepaint()) {
            repaintHelper.repaint(os);
         }
      } else {
         repaintHelper.queueRepaint(os);
      }
   }

   /**
    * Sets a game loop in the driver seat.
    * <br>
    * For a single component, better to use the game loop
    * outside
    */
   private void separateUIFromGameLoopClocking() {

   }

   /** 
    * Must be called outside the GUI thread.
    * Forces any pending repaint requests to be serviced immediately. This method blocks until the pending requests have been serviced.
    * <br>
    * If there are no pending repaints, or if this canvas is not visible on the display, this call does nothing and returns immediately.
    * <br>
    * <br>
    * 
    * Warning: This method blocks until the call to the application's paint() method returns. 
    * <br>
    * The application has no control over which thread calls paint(); it may vary from implementation to implementation.
    * <br>
    * If the caller of serviceRepaints() holds a lock that the paint() method acquires, this may result in deadlock. 
    * <br>
    * Therefore, callers of serviceRepaints() must not hold any locks that might be acquired within the paint() method. 
    * <br>
    * The Display.callSerially() method provides a facility where an application can be called back after painting has completed,
    * avoiding the danger of deadlock. 
    * <br>
    * <br>
    * What if 2 different threads call this?
    */
   public void serviceRepaints() {
      //queue
      //getRepaintCtrl().queueRepaintBlock(sr);
      throw new RuntimeException();
   }

   /**
    * 
    * @param color
    */
   public void setBgColor(int color) {
      bgColor = color;
      boCanvasAppli.set4(IBOCanvasAppli.CANVAS_APP_OFFSET_06_BG_COLOR4, color);
   }

   public void setCtrlEnableTrue() {
      isCtrlEnabled = true;
   }

   public void setDebugFlag(int flag, boolean v) {
      debugFlags = BitUtils.setFlag(debugFlags, flag, v);
   }

   /**
    * Synchronize this How?
    * @return
    */
   public int setNextThreadingMode() {
      threadingMode++;
      if (threadingMode > ITechThreadPaint.THREADING_4_UI_CLOCKING) {
         threadingMode = ITechThreadPaint.THREADING_0_ONE_TO_RULE_ALL;
      }
      setThreadingMode(threadingMode);
      return threadingMode;
   }

   /**
    * Can be called anywhere in the constructor
    * Manages the switch.
    * <br>
    * Create an {@link EventController}
    * <br>
    * <br>
    * <li> {@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}
    * <li> {@link ITechThreadPaint#THREADING_1_UI_UPDATERENDERING}
    * <li> {@link ITechThreadPaint#THREADING_2_UIUPDATE_RENDERING}
    * <li> {@link ITechThreadPaint#THREADING_3_THREE_SEPARATE}
    * @param threadingMode
    */
   public void setThreadingMode(int threadingMode) {
      //#debug
      toDLog().pInit1("Mode to " + ToStringStaticInput.toStringThreadingMode(threadingMode) + " from " + ToStringStaticInput.toStringThreadingMode(this.threadingMode), null, CanvasAppliInput.class, "setThreadingMode@" + toStringGetLine(1516));

      if (this.threadingMode == threadingMode) {
         return;
      }
      setThreadStop();
      //we first must make sure the threads finish and 
      if (threadingMode == ITechThreadPaint.THREADING_0_ONE_TO_RULE_ALL) {
         setThreadingMode0();
      } else if (threadingMode == ITechThreadPaint.THREADING_1_UI_UPDATERENDERING) {
         //check if host canvas supports active rendering.
         boolean isActiveRendering = this.getCanvasHost().isCanvasFeatureSupported(ITechFeaturesCanvas.SUP_ID_15_ACTIVE_RENDERING);
         if (isActiveRendering) {
            this.getCanvasHost().setCanvasFeature(ITechFeaturesCanvas.SUP_ID_15_ACTIVE_RENDERING, true);
            setThreadingMode1();
         } else {
            //#debug
            toDLog().pAlways("Active Rendering Not Supported", this, toStringGetLine(CanvasAppliInput.class, "setThreadingMode", 1533), LVL_08_INFO, true);
            setThreadingMode0();
         }
      } else if (threadingMode == ITechThreadPaint.THREADING_2_UIUPDATE_RENDERING) {
         setThreadingMode2();
      } else if (threadingMode == ITechThreadPaint.THREADING_3_THREE_SEPARATE) {
         setThreadingMode3();
      } else {
         throw new IllegalArgumentException("Ünknown threading mode " + threadingMode);
      }

      if (eventController == null) {
         throw new NullPointerException("We must initialize eventCtrl here");
      }
      //#debug
      toDLog().pFlowBig("Thread Mode is now :[" + ToStringStaticInput.toStringThreadingMode(threadingMode) + "]", this, CanvasAppliInput.class, "setThreadingMode@1563");
   }

   private void setThreadingMode3() {
      gameLoop = new CanvasLoopGameFramed(ic, this);
      gameLoop.setIsAvoidRender(true);

      FrameData frameData = gameLoop.getFrameData();
      frameData.setMaxUpdateStepsWithoutRender(5);
      frameData.setHertzUpdate(10.0f);
      frameData.setHertzRender(60.0f);

      CanvasLoop et = new LoopThreadRender(ic, this);
      threadRender = new Thread(et, "Render");

      EventControllerQueuedSynchro eventCtrl = new EventControllerQueuedSynchro(ic, this);
      this.eventController = eventCtrl;

      updater = new LoopThreadUpdate(ic, this);

      threadUpdate = new Thread(gameLoop, "Update");

      threadRender.start();
      threadUpdate.start();

      threadUpdateRender = null;
   }

   private void setThreadingMode2() {
      ///only one rendering thread.
      CanvasLoop et = new LoopThreadRender(ic, this);
      threadRender = new Thread(et, "Render");
      threadRender.start();

      threadUpdateRender = null;
   }

   private void setThreadingMode1() {
      //what kind of game loop?
      if (threadingMode == ITechThreadPaint.THREADING_1_UI_UPDATERENDERING) {
         return;
      }
      
      gameLoop = new CanvasLoopGameFramed(ic, this);
      FrameData frameData = gameLoop.getFrameData();
      frameData.setMaxUpdateStepsWithoutRender(5);
      frameData.setHertzUpdate(10.0f);
      frameData.setHertzRender(60.0f);

      //#debug
      toDLog().pInit("GameLoopX created for THREADING_1_UI_UPDATERENDERING", gameLoop, CanvasAppliInput.class, "setThreadingMode", LVL_05_FINE, true);

      threadUpdateRender = new Thread(gameLoop, "UpdateRender");
      threadUpdateRender.start();

      threadRender = threadUpdateRender;
      threadUpdate = threadUpdateRender;
      
      this.threadingMode = ITechThreadPaint.THREADING_1_UI_UPDATERENDERING;
   }

   private void setThreadingMode0() {
      //TODO remove this
      if (threadingMode == ITechThreadPaint.THREADING_0_ONE_TO_RULE_ALL) {
         return;
      }
      //do we need a drag queue controller?
      boolean isDragControlled = canvasHost.isCanvasFeatureEnabled(ITechHostUI.FEAT_01_DRAG_CONTROLLER);
      if (isDragControlled) {
         eventController = new EventControllerOneThreadCtrled(ic, this);
      } else {
         eventController = new EventControllerOneThread(ic, this);
      }
      //stop any existing thread
      if (gameLoop != null) {
         gameLoop.stop();
      }
      threadUpdateRender = Thread.currentThread();
      threadRender = Thread.currentThread();
      //game loop in this thread mode ?
      //game must implement its own game loop

      this.threadingMode = ITechThreadPaint.THREADING_0_ONE_TO_RULE_ALL;
   }

   private void setThreadStop() {
      int oldThreadMode = this.threadingMode;
      if (oldThreadMode == -1) {
         return;
      }
      //we first must make sure the threads finish and 
      if (oldThreadMode == ITechThreadPaint.THREADING_0_ONE_TO_RULE_ALL) {
         //nothing to stop
      } else {
         if (threadUpdate != null) {
            threadUpdate.interrupt();
         }
         //threads are never null.
         if (threadRender != null) {
            threadRender.interrupt();
         }
         if (threadUpdateRender != null) {
            threadUpdateRender.interrupt();
         }
      }
   }

   /**
    * {@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}, the call be done serially
    * @param sim
    */
   public void simulationAdd(ISimulationUpdatable sim) {
      getSimulationLazy().simulationAdd(sim);
   }

   /**
    * tick simulations running
    * @param is
    */
   public void simulationUpdate(InputStateCanvas is) {
      getSimulationLazy().simulationUpdate(is);
   }

   public void startEventCanvas(InputState is, OutputStateCanvas sr) {

   }

   public void stateReadFrom(StatorReader state) {
      super.stateReadFrom(state);
   }

   public void stateWriteTo(StatorWriter state) {
      super.stateWriteTo(state);
      //a priori no state that won't be constructed back with boCanvasAppli

   }

   public void stateWriteToParamSub(StatorWriter state) {
      super.stateWriteToParamSub(state);
      ((StatorWriterBO) state).dataWriteByteObject(boCanvasAppli); //it is not null
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, CanvasAppliInput.class, 1497);
      toStringPrivate(dc);
      super.toString(dc.sup());

      dc.nlThread("eventThread", eventThread);
      dc.nlThread("threadRender", threadRender);
      dc.nlThread("threadUpdate", threadUpdate);
      dc.nlThread("threadUpdateRender", threadUpdateRender);

      dc.nlLvl(boCanvasAppli, "boCanvasAppli");

      dc.nlLvl(simulationCanvas, "simulationCanvas");

      dc.nlLvl(eventRun, "JobsEventRunner");
      dc.nlLvl(repaintHelper, "RepaintCtrl");
      dc.nlLvl(eventController, "EventController");
   }

   /**
    * Called when  {@link Dctx} see the same object for another time
    * @param dc
    */
   public void toString1Line(Dctx dc) {
      dc.root1Line(this, CanvasAppliInput.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("bgColor", bgColor);
   }
   //#enddebug

}
