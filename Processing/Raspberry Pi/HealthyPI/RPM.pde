//////////////////////////////////////////////////////////////////////////////////////////
//
//   RPM: [ Respiration Per Minute]
//      - This Class calculates the Respiratory Peaks According to the respiration data received
//
//   Created : Balasundari, Aug 2016
//   
/////////////////////////////////////////////////////////////////////////////////////////

class RPM
{
  float min, max;                                      // Stores Minimum and Maximum Value
  double threshold;                                    // Stores the threshold
  float minimizedVolt[] = new float[pSize];            // Stores the absoulte values in the buffer
  int peaks = 0, rpm1 = 0;                             // Variables to store the no.of peaks and bpm

  ////////////////////////////////////////////////////////////////////////////////////////////
  //  - Respiration Rate is calculated by:
  //          * Setting a threshold value which is between the minimum and maximum value
  //          * Calculating no.of peaks crossing, the threshold value.
  //   
  ////////////////////////////////////////////////////////////////////////////////////////////

  void rpmCalc(float[] recVoltage)
  {
    int j = 0, n = 0, cntr = 0;
    // Making the array into absolute (positive values only)
    for (int i=0; i<pSize; i++)
    {
      minimizedVolt[i] = (float)Math.abs(recVoltage[i]);
    }
    min = min(minimizedVolt);
    max = max(minimizedVolt);

    threshold = min+max;                      // Calculating the threshold value
    threshold = (threshold) * 0.400;

    if (threshold != 0)
    {
      while (n < pSize)                       // scan through ECG samples
      {
        if (recVoltage[n] > threshold)        // ECG threshold crossed
        {
          peaks++;
          n = n+30;                           // skipping the some samples to avoid repeatation
        } else
          n++;
      }
      rpm.setText(peaks+"");
      peaks = 0;
    } else
    {
      rpm.setText("0");
    }
  }
};