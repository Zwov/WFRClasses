package Classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Report implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5563796650989228979L;
	private String cond;
	private String temp;
	private Calendar stamp;
	private String scrutiny;

	public Report(String one, String two, Calendar three)
	{
		this.cond = one;
		this.temp = two;
		this.stamp = three;
	}

	
	@Override
	public Calendar getDate() {
		return this.stamp;
	}

	@Override
	public String getConditions() {
		return this.cond;
	}

	@Override
	public String getTemperature() {
		return this.temp;
	}
	
	
	/* (non-Javadoc)
	 * @see Classes.IReport#getToString()
	 * Vrací pole stringů korespondující s view.jsp
	 */	
	@Override
	public String[] getToString() {
		return new String[]{"Počasí: " + this.cond, "Teplota: " + this.temp,"Aktualizováno: " +  new SimpleDateFormat("HH:mm").format(this.stamp.getTime())};

	}

}
