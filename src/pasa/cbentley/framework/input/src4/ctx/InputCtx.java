package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.ctx.ABOCtx;
import pasa.cbentley.byteobjects.src4.ctx.IConfigBO;
import pasa.cbentley.byteobjects.src4.ctx.IStaticIDsBO;
import pasa.cbentley.core.src4.ctx.CtxManager;
import pasa.cbentley.core.src4.ctx.ICtx;
import pasa.cbentley.core.src4.interfaces.ITimeCtrl;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.thread.WorkerThread;
import pasa.cbentley.framework.core.src4.ctx.CoreFrameworkCtx;
import pasa.cbentley.framework.coreui.src4.ctx.CoreUiCtx;
import pasa.cbentley.framework.coreui.src4.tech.IBOCanvasHost;
import pasa.cbentley.framework.input.src4.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.interfaces.IBOCanvasAppli;

/**
 * Code context for the {@link CanvasAppliInput}.
 * 
 * Uses a {@link CoreFrameworkCtx} to access Canvas and its {@link CoreUiCtx} events.
 * 
 * @author Charles Bentley
 *
 */
public class InputCtx extends ABOCtx implements IBOCtxSettingsInput {
   public static final int          CTX_ID = 8;

   private BOModuleInput            boModule;

   protected final CoreFrameworkCtx cfc;

   private WorkerThread             workerThread;

   /**
    * Creates an {@link InputCtx} with the default configuration {@link ConfigInputDefault}
    * 
    * @param cfc
    */
   public InputCtx(CoreFrameworkCtx cfc) {
      this(new ConfigInputDefault(cfc.getUC()), cfc);
   }

   /**
    * Creates InputCtx with given configuration. Initialize with {@link CtxManager}.
    * 
    * @param config
    * @param cfc
    */
   public InputCtx(IConfigInput config, CoreFrameworkCtx cfc) {
      super(config, cfc.getBOC());
      this.cfc = cfc;
      boModule = new BOModuleInput(this);

      CtxManager manager = uc.getCtxManager();
      manager.registerStaticRange(this, IStaticIDsBO.SID_BYTEOBJECT_TYPES, IBOTypesInput.AZ_BOTYPE_INPUT_A, IBOTypesInput.AZ_BOTYPE_INPUT_Z);

      if (this.getClass() == InputCtx.class) {
         a_Init();
      }

      //#debug
      toDLog().pInit("", this, InputCtx.class, "Created@65", LVL_04_FINER, true);
   }

   protected void applySettings(ByteObject settingsNew, ByteObject settingsOld) {
      //default canvas settings are applied when calling method
      //#debug
      toDLog().pFlow("", null, InputCtx.class, "applySettings", LVL_04_FINER, true);

   }

   //TODO remove use executor directly
   public void callSerially(Runnable runnable) {
      cfc.getCUC().getExecutor().executeMainLater(runnable);
   }

   /**
    * Create a 
    * 
    * @return {@link IBOCanvasAppli}
    */
   public ByteObject createBOCanvasAppliDefault() {
      int type = IBOTypesInput.TYPE_1_CANVAS_APPLI;
      int size = IBOCanvasAppli.CANVAS_APP_BASIC_SIZE;
      ByteObject tech = cfc.getBOC().getByteObjectFactory().createByteObject(type, size);
      setTechCanvasAppliDefault(tech);
      return tech;
   }

   public void exitInputContext() {
      // TODO Auto-generated method stub

   }

   public ByteObject getBOAppliFromParam(ByteObject boCanvasHost, Object object) {
      if (object != null && object instanceof ByteObject) {
         ByteObject bo = (ByteObject) object;
         if (bo.getType() == IBOTypesInput.TYPE_1_CANVAS_APPLI) {
            return bo;
         }
      }
      if (boCanvasHost != null) {
         ByteObject bo = boCanvasHost.getSubFirst(IBOTypesInput.TYPE_1_CANVAS_APPLI);
         if (bo != null) {
            return bo;
         }
      }
      return createBOCanvasAppliDefault();
   }

   public int getBOCtxSettingSize() {
      return IBOCtxSettingsInput.CTX_INPUT_BASIC_SIZE;
   }

   public CoreFrameworkCtx getCFC() {
      return cfc;
   }

   public int getCtxID() {
      return CTX_ID;
   }

   public ICtx[] getCtxSub() {
      return new ICtx[] { cfc };
   }

   public CoreUiCtx getCUC() {
      return cfc.getCUC();
   }

   public CoreFrameworkCtx getHOC() {
      return cfc;
   }

   /**
    * Parameters used when no {@link IBOCanvasHost} is explicitely define when a {@link CanvasAppliInput}
    * is created.
    * @return
    */
   public ByteObject createBOTechCanvasAppliDefault() {

      int type = IBOTypesInput.TYPE_1_CANVAS_APPLI;
      int size = IBOCanvasAppli.CANVAS_APP_BASIC_SIZE;
      //
      ByteObject tech = cfc.getBOC().getByteObjectFactory().createByteObject(type, size);

      ByteObject ctxSettings = this.getBOCtxSettings();
      tech.set1(IBOCanvasAppli.CANVAS_APP_OFFSET_03_THREADING_MODE1, ctxSettings.get1(CTX_INPUT_OFFSET_02_CANVAS_DEFAULT_THREADING_MODE1));
      tech.set1(IBOCanvasAppli.CANVAS_APP_OFFSET_04_SCREEN_MODE1, ctxSettings.get1(CTX_INPUT_OFFSET_03_CANVAS_DEFAULT_SCREEN_MODE1));
      tech.set1(IBOCanvasAppli.CANVAS_APP_OFFSET_05_DEBUG_FLAGS1, ctxSettings.get1(CTX_INPUT_OFFSET_04_CANVAS_DEFAULT_DEBUG_FLAGS1));
      tech.set4(IBOCanvasAppli.CANVAS_APP_OFFSET_06_BG_COLOR4, ctxSettings.get1(CTX_INPUT_OFFSET_05_CANVAS_DEFAULT_BG_COLOR4));
      return tech;
   }

   public ITimeCtrl getTimeCtrl() {
      return cfc.getTimeCtrl();
   }

   public WorkerThread getWorkerThreadInput() {
      if (workerThread == null) {
         workerThread = new WorkerThread(getUC());
      }
      return workerThread;
   }

   protected void matchConfig(IConfigBO config, ByteObject settings) {
      IConfigInput configi = (IConfigInput) config;
      settings.set1(CTX_INPUT_OFFSET_02_CANVAS_DEFAULT_THREADING_MODE1, configi.getCanvasDefaultScreenMode());
      settings.set1(CTX_INPUT_OFFSET_03_CANVAS_DEFAULT_SCREEN_MODE1, configi.getCanvasDefaultScreenMode());
      settings.set1(CTX_INPUT_OFFSET_04_CANVAS_DEFAULT_DEBUG_FLAGS1, configi.getCanvasDefaultDebugFlags());
      settings.set4(CTX_INPUT_OFFSET_05_CANVAS_DEFAULT_BG_COLOR4, configi.getCanvasDefaultBgColor());
   }

   public void setTechCanvasAppliDefault(ByteObject tech) {
      ByteObject ctxSettings = this.getBOCtxSettings();
      tech.set1(IBOCanvasAppli.CANVAS_APP_OFFSET_03_THREADING_MODE1, ctxSettings.get1(CTX_INPUT_OFFSET_02_CANVAS_DEFAULT_THREADING_MODE1));
      tech.set1(IBOCanvasAppli.CANVAS_APP_OFFSET_04_SCREEN_MODE1, ctxSettings.get1(CTX_INPUT_OFFSET_03_CANVAS_DEFAULT_SCREEN_MODE1));
      tech.set1(IBOCanvasAppli.CANVAS_APP_OFFSET_05_DEBUG_FLAGS1, ctxSettings.get1(CTX_INPUT_OFFSET_04_CANVAS_DEFAULT_DEBUG_FLAGS1));
      tech.set4(IBOCanvasAppli.CANVAS_APP_OFFSET_06_BG_COLOR4, ctxSettings.get4(CTX_INPUT_OFFSET_05_CANVAS_DEFAULT_BG_COLOR4));
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, InputCtx.class, 136);
      toStringPrivate(dc);
      super.toString(dc.sup());
      dc.nlLvl(createBOCanvasAppliDefault(), "createTechCanvasAppliDefault");
      dc.nlLvl(cfc, "cfc");
      dc.nlLvl(workerThread, "workerThread");
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "InputCtx");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }

   //#enddebug

}
