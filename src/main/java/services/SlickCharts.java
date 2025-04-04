package services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jodd.jerry.Jerry;
import models.Ticker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SlickCharts {

    public static final SlickCharts INSTANCE = new SlickCharts();
    private static final String CACHE_KEY_SPY500 = "spy500";

    private final URI slickChartsUri = URI.create("https://www.slickcharts.com/sp500");
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Cache<String, List<Ticker>> cache = Caffeine.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    /**
     * Fetches and parses the S&P 500 tickers from slickcharts.com.
     * Uses caching to limit requests to once every 7 days.
     *
     * @return a list of Ticker objects representing the S&P 500 companies.
     */
    public List<Ticker> getSpy500() {
        List<Ticker> cachedTickers = cache.getIfPresent(CACHE_KEY_SPY500);
        if (cachedTickers != null) {
            return cachedTickers;
        }

        String responseBody;
        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().uri(slickChartsUri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            responseBody = response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(String.format("Request to %s failed", slickChartsUri), e);
        }

        Jerry doc = Jerry.of(responseBody);
        List<Ticker> tickers = new ArrayList<>();
        doc.s("tbody tr").each((element, index) -> {
            // Ensure there are at least three children elements to avoid index errors
            if (element.children().size() >= 3) {
                String symbol = element.children().get(2).getTextContent().trim();
                String name = element.children().get(1).getTextContent().trim();
                tickers.add(new Ticker(symbol, name));
            }
            return true;
        });

        // Exclude the last 4 entries if the list contains more than 4 elements
        List<Ticker> result = tickers.size() > 4 ? tickers.subList(0, tickers.size() - 4) : tickers;
        cache.put(CACHE_KEY_SPY500, result);
        return result;
    }
}
