class RPM
{
  
  float min, max;
  double threshold;
  float minimizedVolt[] = new float[pSize];
  int peaks = 0, rpm1 = 0;

  void rpmCalc(float[] recVoltage)
  {

    int j = 0, n = 0, cntr = 0;

    for (int i=0; i<pSize; i++)
    {
      minimizedVolt[i] = (float)Math.abs(recVoltage[i]);
    }

    
    min = min(minimizedVolt);
    max = max(minimizedVolt);

    threshold = min+max;
    threshold = (threshold) * 0.400;
    
    if (threshold != 0)
    {
      while (n < pSize)                      // scan through ECG samples
      {
        if (recVoltage[n] > threshold)                    // ECG threshold crossed
        {
          peaks++;
          //println(peaks);
          n = n+1;
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