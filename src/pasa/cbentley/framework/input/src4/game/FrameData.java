package pasa.cbentley.framework.input.src4.game;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * Owner of a frame data? Canvas? TODO
 * @author Charles Bentley
 *
 */
public class FrameData implements IStringable {

   public static final float MILLISECONDS_IN_A_SECOND = 1000;

   public static final float NANOSECONDS_IN_A_SECOND  = 1000000000;

   public static final int   SECOND_NANO              = 1000000000;

   private int               fps;

   private int               fpsLastSecond;

   /**
    * Count the frames in the current second
    */
   private int               frameCountInSecond;

   private int               frameID;

   private float             hertzRenderTarget;

   private float             hertzUpdate;

   protected final InputCtx  ic;

   private float             interpolation;

   private int               maxUpdateStepsWithoutRender;

   /**
    * The time difference between the frame Time and the last update
    */
   private double            timeDiffSinceLastUpdate;

   private double            timeLastFps;

   private double            timeLastRender;

   /**
    * Current simulation time.
    */
   private double            timeLastUpdate;

   /**
    * Nano seconds between updates
    */
   private double            timeNsBetweenUpdates;

   private double            timeNsTargetBetweenRenders;

   private double            timeThisFrame;

   private int               updateCountFrame;

   private int               updateID;

   public FrameData(InputCtx ic) {
      this.ic = ic;
      maxUpdateStepsWithoutRender = 4;
      setHertzRender(60.0f);
      setHertzUpdate(30.0f);
   }

   /**
    * TODO test this
    */
   public void avoidCatchUp() {
      //If for some reason an update takes forever, we don't want to do an insane number of catchups.
      //If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
      if (timeDiffSinceLastUpdate > timeNsBetweenUpdates) {
         timeLastUpdate = timeThisFrame - timeNsBetweenUpdates;
      }
   }

   /**
    * Compute the ratio of left over time 
    */
   public void computeInterpolation() {
      //
      interpolation = Math.min(1.0f, (float) (timeDiffSinceLastUpdate / timeNsBetweenUpdates));
   }

   public float getElapsedTimeNs() {
      return (float) timeNsBetweenUpdates;
   }

   public float getElapsedTimeMs() {
      return (float) timeNsBetweenUpdates / 1000000f;
   }

   public int getFPS() {
      return fpsLastSecond;
   }

   /**
    * The ratio of the delay
    * @return [0.0,1.0] range
    */
   public float getInterpolationRatio() {
      return interpolation;
   }

   /**
    * time of the rendering within a fixed timestep (in seconds).
    * if maxed out updates
    * The time left over in the current rendering frame
    * @return
    */
   public float getInterpolationTime() {
      return (float) timeDiffSinceLastUpdate;
   }

   public float getInterpolationTimeMs() {
      return (float) timeDiffSinceLastUpdate / 1000000f;
   }

   public float getTimeBetweenUpdates() {
      return (float) timeNsBetweenUpdates;
   }

   public float getTimeTargetBetweenRenders() {
      return (float) timeNsTargetBetweenRenders;
   }

   public int getUpdateID() {
      return updateID;
   }

   public void init() {
      long tickNano = ic.getTimeCtrl().getTickNano();

      this.timeLastRender = tickNano;
      this.timeLastUpdate = tickNano;
   }

   public boolean isSleeping() {
      if (timeThisFrame - timeLastRender < timeNsTargetBetweenRenders) {
         if (timeDiffSinceLastUpdate < timeNsBetweenUpdates) {
            return true;
         }
      }
      return false;
   }

   public boolean isUpdating() {
      if (timeDiffSinceLastUpdate > timeNsBetweenUpdates) {
         if (updateCountFrame < maxUpdateStepsWithoutRender) {
            return true;
         }
      }
      return false;
   }

   /**
    * Called after a rendering
    */
   public void nextFrame() {
      frameID += 1;
      fps += 1;

      timeLastRender = timeThisFrame;
      //
      if (timeLastFps > NANOSECONDS_IN_A_SECOND) {
         fpsLastSecond = fps;
         fps = 0;
         timeLastFps = 0;
      }
      //#debug
      toDLog().pFlow("", this, CanvasLoopGameFramed.class, "nextFrame@182", LVL_03_FINEST, true);
   }

   /**
    * called after an update
    */
   public void nextUpdate() {
      updateID++;
      updateCountFrame++;
      timeLastUpdate += timeNsBetweenUpdates;
      timeDiffSinceLastUpdate -= timeNsBetweenUpdates;
   }

   public void setHertzRender(float timesPerSecond) {
      this.hertzRenderTarget = timesPerSecond;
      timeNsTargetBetweenRenders = NANOSECONDS_IN_A_SECOND / hertzRenderTarget;
   }

   /**
    * Decides the time between updates
    * @param timesPerSecond
    */
   public void setHertzUpdate(float timesPerSecond) {
      this.hertzUpdate = timesPerSecond;
      timeNsBetweenUpdates = NANOSECONDS_IN_A_SECOND / hertzUpdate;
   }

   public void setMaxUpdateStepsWithoutRender(int num) {
      this.maxUpdateStepsWithoutRender = num;
   }

   public void setTimeBetweenUpdates(float timeBetweenUpdates) {
      this.timeNsBetweenUpdates = timeBetweenUpdates;
   }

   public void setTimeLastRender(float timeLastRender) {
      this.timeLastRender = timeLastRender;
   }

   public void setTimeTargetBetweenRenders(float timeTargetBetweenRenders) {
      this.timeNsTargetBetweenRenders = timeTargetBetweenRenders;
   }

   public void startNew() {
      //#debug
      toDLog().pFlow("", this, FrameData.class, "startNew@225", LVL_04_FINER, true);

      tickFrameTime();
      updateCountFrame = 0;
      timeDiffSinceLastUpdate = timeThisFrame - timeLastUpdate;
   }

   public int getMillisFromNanos(double nanos) {
      return (int) (nanos / 1000000d);
   }

   public void tickFrameTime() {
      timeThisFrame = ic.getTimeCtrl().getTickNano();
   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, FrameData.class, 250);
      toStringPrivate(dc);

      dc.appendVarWithSpace("interpolation", interpolation);

      dc.appendVarWithSpace("hertzUpdate", hertzUpdate);
      dc.appendVarWithSpace("hertzRenderTarget", hertzRenderTarget);
      dc.appendVarWithSpace("maxUpdateStepsWithoutRender", maxUpdateStepsWithoutRender);

      dc.appendVarWithSpace("fps", fps);
      dc.appendVarWithSpace("fpsLastSecond", fpsLastSecond);

      dc.appendVarWithSpace("timeTargetBetweenRenders", timeNsTargetBetweenRenders);
      dc.appendVarWithSpace("timeThisFrame", timeThisFrame);
      dc.appendVarWithSpace("timeLastRender", timeLastRender);
      dc.appendVarWithSpace("timeLastUpdate", timeLastUpdate);

      dc.appendVarWithSpace("timeDiffSinceLastUpdate", timeDiffSinceLastUpdate);
      dc.nl();

   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, FrameData.class, 277);
      toStringPrivate(dc);
      dc.appendVarWithSpace("interpolationRatio", getInterpolationRatio(), 2);
      dc.appendVarWithSpace("diffSinceLastUpdate", getMillisFromNanos(timeDiffSinceLastUpdate));

   }

   public UCtx toStringGetUCtx() {
      return ic.getUC();
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("frameID", frameID);
      dc.appendVarWithSpace("updateID", updateID);
      dc.append(" ");
      dc.append(getMillisFromNanos(timeNsBetweenUpdates));
      dc.append(" ms between updates");

   }

   //#enddebug

}
