package Classes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Calendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.cache.MultiVMPoolUtil;

import XMLClasses.Response;


//@Service
public class ForecastRetriever implements IForecastRetriever 
{
	
	/**
	 * @param uri Adresa pro získáni předpovědi
	 * @return	raw surová data pro zpracování
	 * @throws Exceptions: Malformed URl/IO(no network)
	 */
	public String AquireForecast(String uri) throws Exception 
	{
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(uri);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
		in.close();
		} catch (Exception e) {
			throw new Exception("Nepodařilo se spojit se serverem");
		} 
		return sb.toString();
	}
	
	/**
	 * @param xmlStr surová data xml pro konverzi
	 * @return report objekt s vybranýmy daty z konvertovaného xml
	 */
	public Report translateXML(String xmlStr)
	{
        try 
        {  
        	JAXBContext jc = JAXBContext.newInstance(Response.class);

        	Unmarshaller unmarshaller = jc.createUnmarshaller();
        	Response res = (Response)unmarshaller.unmarshal(new StringReader(xmlStr));        	

        	Marshaller marshaller = jc.createMarshaller();
        	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        	return new Report(res.getCurrentObservation().getWeather(), res.getCurrentObservation().getTempC(), Calendar.getInstance());
        } catch (Exception e) {  
        	return new Report(null, null, Calendar.getInstance());
        } 
    }

	/* (non-Javadoc)
	 * @see Classes.IForecastRetriever#GetForecast(java.lang.String)
	 * Získáva záznam z cache(kontrola pro cache s ttl/tti větší naž jedna hodina)
	 * Pokud cache vypršela, znovu získá data a cachuje
	 */
	@Override
	public Report GetForecast(String uri)
	{
		try
		{			
			Report out;
			try
			{
				out = (Report) MultiVMPoolUtil.getCache("WeatherPortlet").get("report");
			}
			catch(Exception e)
			{
				out = null;
			}
			Calendar chck = Calendar.getInstance();
			chck.set(Calendar.HOUR_OF_DAY, chck.get(Calendar.HOUR_OF_DAY)-1);
			if(out == null || out.getDate().compareTo(chck) <= 0)
			{
				out = translateXML(AquireForecast(uri));
				MultiVMPoolUtil.getCache("WeatherPortlet").put("report", out);				
			}				
			return out;	
		}		
		catch(Exception e)
		{
			return null;
		}		
	}
	/* (non-Javadoc)
	 * @see Classes.IForecastRetriever#GetForecastForce(java.lang.String)
	 * Nezávisle naplni cache novýmy daty
	 */
	@Override
	public void GetForecastForce(String uri)
	{
		Report out = null;
		try 
		{
			out = translateXML(AquireForecast(uri));
			MultiVMPoolUtil.getCache("WeatherPortlet").put("report", out);	
		} catch (Exception e) 
		{
			// Dummy catch
		}	
	}
	/* (non-Javadoc)
	 * @see Classes.IForecastRetriever#Flush()
	 * "Destruktor"
	 */	
	@Override
	public void Flush()
	{
		MultiVMPoolUtil.getCache("WeatherPortlet").removeAll();
	}

}
