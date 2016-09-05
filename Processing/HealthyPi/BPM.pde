class BPM
{

  float min, max;
  double threshold;
  float minimizedVolt[] = new float[pSize];
  int beats = 0, bpm = 0;

  void bpmCalc(float[] recVoltage)
  {

    int j = 0, n = 0, cntr = 0;

    for (int i=0; i<pSize; i++)
    {
      recVoltage[i] = (float)Math.abs(recVoltage[i]);
    }

    j = 0;
    for (int i = 0; i < pSize; i++)
    {
      minimizedVolt[j++] = recVoltage[i];
    }
    min = min(minimizedVolt);
    max = max(minimizedVolt);

    if ((int)min == (int)max)
    {
      bpm1.setText("0");
    } else
    {
      threshold = min+max;
      threshold = (threshold) * 0.400;


      if (threshold != 0)
      {
        while (n < pSize)                      // scan through ECG samples
        {
          if (minimizedVolt[n] > threshold)                    // ECG threshold crossed
          {
            beats++;
            n = n+30;
          } else
            n++;
        }
        bpm = (beats*60)/8;

        bpm1.setText(bpm+"");
        beats = 0;
      } else
      {
        bpm1.setText("0");
      }
    }
  }
};