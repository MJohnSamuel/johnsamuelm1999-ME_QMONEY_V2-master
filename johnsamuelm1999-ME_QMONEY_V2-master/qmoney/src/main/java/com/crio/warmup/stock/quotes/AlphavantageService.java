
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.stockQuoteCompare;

import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {


  private RestTemplate restTemplate;


  protected AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static String alphaVantageKey = "LD52L5DBJO3RH5C7";

  protected String buildUri(String symbol) {

    // String APIKEY = alphaVantageKey;
    String uriTemplate = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&outputsize=full&apikey=" + alphaVantageKey;
//https:api.tiingo.com/tiingo/daily/GOOGL/prices?startDate=2019-01-01&endDate=2019-01-04&token=3cfebc2f5f80aca2dabab151aacbf96147d706f1"
         return uriTemplate;
}

private static ObjectMapper getObjectMapper() {
  ObjectMapper objectMapper = new ObjectMapper();
  objectMapper.registerModule(new JavaTimeModule());
  return objectMapper;
}

//https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=LD52L5DBJO3RH5C7

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException ,StockQuoteServiceException {
    // TODO Auto-generated method stub

    List<Candle> stockQuotes = new ArrayList<Candle>();
    try {
      String dailyResponse = restTemplate.getForObject(buildUri(symbol),String.class);
    AlphavantageDailyResponse alphavantageDailyResponse = getObjectMapper().readValue(dailyResponse, AlphavantageDailyResponse.class);
    alphavantageDailyResponse.getCandles().forEach((date,candles) -> {
      if((from.isEqual(date)||from.isBefore(date)) && (to.isEqual(date)||to.isAfter(date))){
        candles.setDate(date);
        stockQuotes.add(candles);
      }});
          
     Collections.sort(stockQuotes,new stockQuoteCompare());
     return stockQuotes;
    } catch (Exception e) {
      //TODO: handle exception
      throw new StockQuoteServiceException(e.getMessage());
    }
    
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement the StockQuoteService interface as per the contracts. Call Alphavantage service
  //  to fetch daily adjusted data for last 20 years.
  //  Refer to documentation here: https://www.alphavantage.co/documentation/
  //  --
  //  The implementation of this functions will be doing following tasks:
  //    1. Build the appropriate url to communicate with third-party.
  //       The url should consider startDate and endDate if it is supported by the provider.
  //    2. Perform third-party communication with the url prepared in step#1
  //    3. Map the response and convert the same to List<Candle>
  //    4. If the provider does not support startDate and endDate, then the implementation
  //       should also filter the dates based on startDate and endDate. Make sure that
  //       result contains the records for for startDate and endDate after filtering.
  //    5. Return a sorted List<Candle> sorted ascending based on Candle#getDate
  //  IMP: Do remember to write readable and maintainable code, There will be few functions like
  //    Checking if given date falls within provided date range, etc.
  //    Make sure that you write Unit tests for all such functions.
  //  Note:
  //  1. Make sure you use {RestTemplate#getForObject(URI, String)} else the test will fail.
  //  2. Run the tests using command below and make sure it passes:
  //    ./gradlew test --tests AlphavantageServiceTest
  //CHECKSTYLE:OFF
    //CHECKSTYLE:ON
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  1. Write a method to create appropriate url to call Alphavantage service. The method should
  //     be using configurations provided in the {@link @application.properties}.
  //  2. Use this method in #getStockQuote.

}

