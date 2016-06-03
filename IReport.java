package Classes;

import java.io.Serializable;
import java.util.Calendar;

public interface IReport extends Serializable{
	public Calendar getDate();
	public String getConditions();
	public String getTemperature();
	public String[] getToString();
}
