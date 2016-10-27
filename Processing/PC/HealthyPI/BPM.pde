//////////////////////////////////////////////////////////////////////////////////////////
//
//   BPM: [ Beats Per Minute]
//      - This Class calculates the Heart Rate According to the ecg data received
//
//   Created : Balasundari, Jul 2016
//   
/////////////////////////////////////////////////////////////////////////////////////////

class BPM
{

  float min, max;                                      // Stores Minimum and Maximum Value
  double threshold;                                    // Stores the threshold 
  float minimizedVolt[] = new float[pSize];            // Stores the absoulte values in the buffer
  int beats = 0, bpm = 0;                              // Variables to store the no.of peaks and bpm

  ////////////////////////////////////////////////////////////////////////////////////////////
  //  - Heart Value is calculated by:
  //          * Setting a threshold value which is between the minimum and maximum value
  //          * Calculating no.of peaks crossing, the threshold value.
  //          * Calculate the Heart rate with the no.of peaks achieved with the no.of seconds
  //   
  ////////////////////////////////////////////////////////////////////////////////////////////

  void bpmCalc(float[] recVoltage)
  {

    int j = 0, n = 0, cntr = 0;

    // Making the array into absolute (positive values only)

    for (int i=0; i<pSize; i++)
    {
      recVoltage[i] = (float)Math.abs(recVoltage[i]);
    }

    j = 0;
    for (int i = 0; i < pSize; i++)
    {
      minimizedVolt[j++] = recVoltage[i];
    }
    
    // Calculating the minimum and maximum value
    
    min = min(minimizedVolt);
    max = max(minimizedVolt);

    if ((int)min == (int)max)
    {
      bpm1.setText("0");
    } else
    {
      threshold = min+max;                                     // Calculating the threshold value
      threshold = (threshold) * 0.400;

      if (threshold != 0)
      {
        while (n < pSize)                                      // scan through ECG samples
        {
          if (minimizedVolt[n] > threshold)                    // ECG threshold crossed
          {
            beats++;
            n = n+30;                                          // skipping the some samples to avoid repeatation
          } else
            n++;
        }
        bpm = (beats*60)/8;

        bpm1.setText(bpm+"");                                  // Calculated BPM is displayed
        beats = 0;
      } else
      {
        bpm1.setText("0");
      }
    }
  }
};