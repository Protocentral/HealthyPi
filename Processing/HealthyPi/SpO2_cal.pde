public class SPO2_cal
{
  float Vdc = 0;
  float Vac = 0;
  float spo2_cal_array[] = new float[pSize];

  float SPO2 = 0;

  public void rawDataArray(float ir_array[], float red_array[], double ir_avg, double red_avg)
  {

    float RedAC = s.SPO2_Value(red_array);
    float IrAC = s.SPO2_Value(ir_array);
    float value = (RedAC/abs((float)red_avg))/(IrAC/abs((float)ir_avg));

    /********  Emprical Formalae  *********/
    //float SpO2 = 10.0002*(value)-52.887*(value) + 26.817*(value) + 98.293;
    //  float SpO2 =((0.81-0.18*(value))/(0.73+0.11*(value)));
    float SpO2=110-25*(value);
    SpO2 = (int)(SpO2 * 100);
    SpO2 = SpO2/100;
    SP02.setText(SpO2+"");
  }


  public float SPO2_Value(float spo2_array[])
  {
    SPO2 = 0;
    int k = 0;
    for (int i = 50; i < spo2_array.length; i++)
    {
      spo2_cal_array[k++] = spo2_array[i] * spo2_array[i];
    }
    SPO2 = sum(spo2_cal_array, k);



    return (SPO2);
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