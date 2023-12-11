package io.github.kdetard.koki.openmeteo;

import io.github.kdetard.koki.openmeteo.models.OpenMeteoResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;

public interface OpenMeteoService {
    @GET("https://api.open-meteo.com/v1/forecast?latitude=10.82&longitude=106.62&current=temperature_2m,relative_humidity_2m,apparent_temperature,rain&timezone=auto&forecast_days=1")
    Single<OpenMeteoResponse> getWeather();
}
