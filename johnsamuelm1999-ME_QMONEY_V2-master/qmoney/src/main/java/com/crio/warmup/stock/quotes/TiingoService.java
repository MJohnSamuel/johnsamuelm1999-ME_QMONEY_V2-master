
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

private RestTemplate restTemplate;
private static String tiingoToken = "d22980fe5644bd0b234d8a5d561ce68193fb0559";

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    String APIKEY = tiingoToken;
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate + "&endDate=" + endDate + "&token=" + APIKEY;
//https:api.tiingo.com/tiingo/daily/GOOGL/prices?startDate=2019-01-01&endDate=2019-01-04&token=3cfebc2f5f80aca2dabab151aacbf96147d706f1"
         return uriTemplate;
}


  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {

        List<Candle> stockQuotes = new ArrayList<Candle>();
        String jsonResponse = restTemplate.getForObject(buildUri(symbol, from, to), String.class);

            // Step 2: Use ObjectMapper to deserialize the JSON string into TiingoCandle[]
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            TiingoCandle[] candles = objectMapper.readValue(jsonResponse, TiingoCandle[].class);
            
         for(TiingoCandle candle: candles){
          stockQuotes.add(candle);
         }
     return stockQuotes;
    // TODO Auto-generated method stub
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

}
