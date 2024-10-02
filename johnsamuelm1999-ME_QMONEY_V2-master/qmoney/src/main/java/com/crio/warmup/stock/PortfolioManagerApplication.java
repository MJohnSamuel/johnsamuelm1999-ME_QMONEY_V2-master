
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
  private static String tiingoToken = "d22980fe5644bd0b234d8a5d561ce68193fb0559";
  public static String getToken() {
    return tiingoToken;
  }

  // https://api.tiingo.com/tiingo/daily/GOOGL/prices?startDate=2019-12-20&endDate=2019-12-20&token=3cfebc2f5f80aca2dabab151aacbf96147d706f1

  private static String ApiRoot = "https://api.tiingo.com/tiingo/daily/";
  private static RestTemplate restTemplate = new RestTemplate();

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
    List<String> list = new ArrayList<String>();
    List<? extends Object> list2 = new ArrayList<PortfolioTrade>();
    
    for(PortfolioTrade trade : portfolioTrades){
      list.add(trade.getSymbol());
    }
     return list;
  }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplicationNaive.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/johnsamuelm1999-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@33990a0c";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "29";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException, RuntimeException {
    
    List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
    List<TotalReturnsDto> totalReturnsDtos = new ArrayList<TotalReturnsDto>();
    List<String> listOfQuotes = new ArrayList<String>();
    LocalDate localDate = LocalDate.parse(args[1]); 

    for(PortfolioTrade trade : portfolioTrades){
      String resourceUrl = prepareUrl(trade,localDate,tiingoToken);
      TiingoCandle[] response = restTemplate.getForObject(resourceUrl, TiingoCandle[].class);

      totalReturnsDtos.add(new TotalReturnsDto(trade.getSymbol(), response[response.length -1].getClose()));
      }

      Collections.sort(totalReturnsDtos,new closePiceComparator());
      for(TotalReturnsDto dto: totalReturnsDtos ){
        listOfQuotes.add(dto.getSymbol());
      }
     return listOfQuotes;
  }
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File trades = resolveFileFromResources(filename);
    ObjectMapper obj = getObjectMapper(); 
    PortfolioTrade[] portfolioTrades = obj.readValue(trades,PortfolioTrade[].class);
     return Arrays.asList(portfolioTrades);
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     //https://api.tiingo.com/tiingo/daily/aapl/prices?startDate=2019-01-02&endDate=2019-01-02&token=3cfebc2f5f80aca2dabab151aacbf96147d706f1
     String consUri = ApiRoot + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
     return consUri;
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {

    return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String resourceUrl = prepareUrl(trade, endDate, token);
    List<Candle> candleArray = new ArrayList<Candle>();    

    TiingoCandle[] candles = restTemplate.getForObject(resourceUrl, TiingoCandle[].class);

    for(TiingoCandle tingoCandle : candles){
      candleArray.add(tingoCandle);
    }
     return candleArray;
    }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

    List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
    List<AnnualizedReturn> annualizeReturn = new ArrayList<AnnualizedReturn>();
    for(PortfolioTrade trade : portfolioTrades ){
      annualizeReturn.add(calculateAnnualizedReturns(
        LocalDate.parse(args[1]), trade, 
        getOpeningPriceOnStartDate(fetchCandles(trade, LocalDate.parse(args[1]),tiingoToken)), 
        getClosingPriceOnEndDate(fetchCandles(trade, LocalDate.parse(args[1]),tiingoToken))));
    }

    Collections.sort(annualizeReturn);
     return annualizeReturn;
  }

  private static double calculateTotalNumOfYears(LocalDate startDate , LocalDate endDate){
    return startDate.until(endDate, ChronoUnit.DAYS)/364.24;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

        double total_returns = (sellPrice - buyPrice) / buyPrice;

        double total_num_years = calculateTotalNumOfYears(trade.getPurchaseDate(), endDate);

        // if(total_num_years == 0 ){
        //   total_num_years = (endDate.getDayOfYear() - trade.getPurchaseDate().getDayOfYear()) / 365;
        // }
        double Annualized_Returns = (Math.pow((1+ total_returns) ,(1 / total_num_years) ) - 1 );
      return new AnnualizedReturn(trade.getSymbol(), Annualized_Returns, total_returns);
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {

       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       File trades = resolveFileFromResources(file);
       ObjectMapper obj = getObjectMapper(); 
       PortfolioTrade[] portfolioTrades = obj.readValue(trades,PortfolioTrade[].class);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
  }
}

