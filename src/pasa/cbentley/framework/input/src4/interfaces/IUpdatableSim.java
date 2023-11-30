package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.framework.input.src4.game.FrameData;

/**
 * Same idea is Choreographer.FrameCallback in android but better :)
 * 
 * You can have several different simulation cores..
 * 
 * For example 1 on image A graphics, and another simulation drawing on image B graphics.
 * 
 * 
 * @author Charles Bentley
 *
 */
public interface IUpdatableSim {
   /**
    * Called when the time has elapsed. The simulation has to update the state of its objects best it can using
    * the {@link FrameData} information.
    * 
    * Usually {@link FrameData#getInterpolationTime()} will be used to smooth out 
    * @param frame {@link FrameData}
    */
   public void update(FrameData frame);

   /**
    * Returns a value indicating what the simulation master should do with this instance
    * @return
    */
   public int getUpdatableSimState();
}
