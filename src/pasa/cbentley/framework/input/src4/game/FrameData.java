package pasa.cbentley.framework.input.src4.game;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;

/**
 * Owner of a frame data? Canvas? TODO
 * @author Charles Bentley
 *
 */
public class FrameData extends ObjectIC implements IStringable {

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
   private double            timeNanoBetweenUpdates;

   private double            timeNanoTargetBetweenRenders;

   private double            timeThisFrame;

   private int               updateCountFrame;

   private int               updateID;

   public FrameData(InputCtx ic) {
      super(ic);
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
      if (timeDiffSinceLastUpdate > timeNanoBetweenUpdates) {
         timeLastUpdate = timeThisFrame - timeNanoBetweenUpdates;
      }
   }

   /**
    * Compute the ratio of left over time 
    */
   public void computeInterpolation() {
      //
      interpolation = Math.min(1.0f, (float) (timeDiffSinceLastUpdate / timeNanoBetweenUpdates));
   }

   public float getElapsedTimeMs() {
      return (float) timeNanoBetweenUpdates / 1000000f;
   }

   public float getElapsedTimeNs() {
      return (float) timeNanoBetweenUpdates;
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

   public int getMillisFromNanos(double nanos) {
      return (int) (nanos / 1000000d);
   }

   public float getTimeBetweenUpdates() {
      return (float) timeNanoBetweenUpdates;
   }

   public float getTimeTargetBetweenRenders() {
      return (float) timeNanoTargetBetweenRenders;
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
      if (timeThisFrame - timeLastRender < timeNanoTargetBetweenRenders) {
         if (timeDiffSinceLastUpdate < timeNanoBetweenUpdates) {
            return true;
         }
      }
      return false;
   }

   public boolean isUpdating() {
      if (timeDiffSinceLastUpdate > timeNanoBetweenUpdates) {
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
      timeLastUpdate += timeNanoBetweenUpdates;
      timeDiffSinceLastUpdate -= timeNanoBetweenUpdates;
   }

   /**
    * Usually 60 fps.. frames per second
    * 
    * @param timesPerSecond number of times per second we want to render
    */
   public void setHertzRender(float timesPerSecond) {
      this.hertzRenderTarget = timesPerSecond;
      timeNanoTargetBetweenRenders = NANOSECONDS_IN_A_SECOND / hertzRenderTarget;
   }

   /**
    * Usually 10 update frames per second.
    * 
    * Decides the time between updates.
    * 
    * @param timesPerSecond number of times per second we want to update
    */
   public void setHertzUpdate(float timesPerSecond) {
      this.hertzUpdate = timesPerSecond;
      this.timeNanoBetweenUpdates = NANOSECONDS_IN_A_SECOND / hertzUpdate;
   }

   public void setMaxUpdateStepsWithoutRender(int num) {
      this.maxUpdateStepsWithoutRender = num;
   }

   public void setTimeBetweenUpdates(float timeBetweenUpdates) {
      this.timeNanoBetweenUpdates = timeBetweenUpdates;
   }

   public void setTimeLastRender(float timeLastRender) {
      this.timeLastRender = timeLastRender;
   }

   public void setTimeTargetBetweenRenders(float timeTargetBetweenRenders) {
      this.timeNanoTargetBetweenRenders = timeTargetBetweenRenders;
   }

   public void startNew() {
      //#debug
      toDLog().pFlow("", this, FrameData.class, "startNew@225", LVL_04_FINER, true);

      tickFrameTime();
      updateCountFrame = 0;
      timeDiffSinceLastUpdate = timeThisFrame - timeLastUpdate;
   }

   public void tickFrameTime() {
      timeThisFrame = ic.getTimeCtrl().getTickNano();
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, FrameData.class, toStringGetLine(250));
      toStringPrivate(dc);
      super.toString(dc.sup());

      dc.appendVarWithSpace("interpolation", interpolation);

      dc.appendVarWithSpace("hertzUpdate", hertzUpdate);
      dc.appendVarWithSpace("hertzRenderTarget", hertzRenderTarget);
      dc.appendVarWithSpace("maxUpdateStepsWithoutRender", maxUpdateStepsWithoutRender);

      dc.appendVarWithSpace("fps", fps);
      dc.appendVarWithSpace("fpsLastSecond", fpsLastSecond);

      dc.appendVarWithSpace("timeTargetBetweenRenders", timeNanoTargetBetweenRenders);
      dc.appendVarWithSpace("timeThisFrame", timeThisFrame);
      dc.appendVarWithSpace("timeLastRender", timeLastRender);
      dc.appendVarWithSpace("timeLastUpdate", timeLastUpdate);

      dc.appendVarWithSpace("timeDiffSinceLastUpdate", timeDiffSinceLastUpdate);
      dc.nl();

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, FrameData.class, toStringGetLine(250));
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());

      dc.appendVarWithSpace("interpolTime", getInterpolationTime(), 2);
      dc.appendVarWithSpace("interpolRatio", getInterpolationRatio(), 2);
      dc.appendVarWithSpace("diffSinceLastUpdate", getMillisFromNanos(timeDiffSinceLastUpdate));

   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("frameID", frameID);
      dc.appendVarWithSpace("updateID", updateID);
      dc.append(" ");
      dc.append(getMillisFromNanos(timeNanoBetweenUpdates));
      dc.append(" ms between updates");
   }
   //#enddebug

}
