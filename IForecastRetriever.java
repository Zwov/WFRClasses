package Classes;

public interface IForecastRetriever {
	void GetForecastForce(String uri);
	void Flush();
	Report GetForecast(String uri);
	
}
