package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.framework.input.src4.game.FrameData;

/**
 * Same idea is Choreographer.FrameCallback in android but better :)
 * @author Charles Bentley
 *
 */
public interface IRunnableSim extends Runnable {
   /**
    * Called when the time has elapsed. The simulation has to update the state of its objects best it can using
    * the {@link FrameData} information.
    * 
    * Usually {@link FrameData#getInterpolationTime()} will be used to smooth out 
    * @param frame {@link FrameData}
    */
   public void update(FrameData frame);
}
