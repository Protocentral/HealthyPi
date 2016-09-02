public class SPO2_cal
{
  float Vdc = 0;
  float Vac = 0;
  float spo2_cal_array[] = new float[pSize];
  
  float SPO2 = 0;


  public float SPO2_Value(float spo2_array[])
  {
    SPO2 = 0;
    int k = 0;
    for(int i = 50; i < spo2_array.length; i++)
    {
     // float roundoff = Math.round((spo2_array[i] * spo2_array[i])*100)/100;
      spo2_cal_array[k++] = spo2_array[i] * spo2_array[i];
    }
    SPO2 = sum(spo2_cal_array, k);
    
    return (SPO2);
    //int  p = 0;
    //try
    //{
    //  int j = 0;
    //  while (j < spo2_array.length)
    //  {
    //    int i = 0;
    //    if (spo2_array[j] >= 0)
    //    {
    //      while (spo2_array[i] >= 0)
    //      {
    //        spo2_cal_array[i] = spo2_array[i]*spo2_array[i];
    //        i++;
    //      }
    //      while (spo2_array[i] < 0)
    //      {
    //        spo2_cal_array[i] = spo2_array[i]*spo2_array[i];
    //        i++;
    //      }

    //      j = j+i;
    //      //   println("po :"+i+" "+j);
    //    } else
    //    {
    //      while (spo2_array[i] < 0)
    //      {
    //        spo2_cal_array[i] = spo2_array[i]*spo2_array[i];
    //        i++;
    //      }
    //      while (spo2_array[i] >= 0)
    //      {
    //        spo2_cal_array[i] = spo2_array[i]*spo2_array[i];
    //        i++;
    //      }
    //      while (spo2_array[i] < 0)
    //      {
    //        rem_array[p] = spo2_array[i];
    //        i++;
    //        p++;
    //      }
    //      float minValue = min(rem_array);
    //      int count = 0;
    //      for (int k = i-p; k < i; k++)
    //      {
    //        if (rem_array[k] == minValue)
    //        {  
    //          spo2_cal_array[k] = rem_array[k]*rem_array[k];
    //          count++;
    //          break;
    //        } else
    //        {
    //          spo2_cal_array[k] = rem_array[k]*rem_array[k];
    //          count++;
    //        }
    //      }
    //      j = j+count;
    //      // println("ne :"+i+" "+j);
    //    }
  }


  public float sum(float array[], int len)
  {
    float spo2 = 0;
    for (int p = 0; p < len; p++)
    {
      spo2 = spo2 + array[p];
    }
    Vac = (float)Math.sqrt(spo2/len);
    
    //println(Vac);
    return Vac;
  }
}