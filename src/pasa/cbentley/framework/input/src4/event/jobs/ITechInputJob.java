package pasa.cbentley.framework.input.src4.event.jobs;

import pasa.cbentley.core.src4.interfaces.ITech;

public interface ITechInputJob extends ITech {

   int JOB_STATE_0_WAITING   = 0;
   int JOB_STATE_1_RUN       = 1;
   /**
    * Internal for saying the job must be removed from the runner queue.
    */
   int JOB_STATE_2_CANCELED  = 2;
   int JOB_STATE_3_FINALIZED = 3;

}
