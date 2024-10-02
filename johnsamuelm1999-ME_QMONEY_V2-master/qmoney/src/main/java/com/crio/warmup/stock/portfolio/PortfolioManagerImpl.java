
package com.crio.warmup.stock.portfolio;


import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private  RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;
  private static String tiingoToken = "d22980fe5644bd0b234d8a5d561ce68193fb0559";

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate,StockQuotesService stockQuotesService) {
    this.restTemplate = restTemplate;
    this.stockQuotesService = stockQuotesService;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }


  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    String APIKEY = tiingoToken;
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate + "&endDate=" + endDate + "&token=" + APIKEY;
//https:api.tiingo.com/tiingo/daily/AAPL/prices?startDate=16-09-2024&token=3cfebc2f5f80aca2dabab151aacbf96147d706f1"
         return uriTemplate;
}


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

        List<Candle> stockQuotes = new ArrayList<Candle>();
        TiingoCandle[] candles = restTemplate.getForObject(buildUri(symbol, from, to),
         TiingoCandle[].class);
         for(TiingoCandle candle: candles){
          stockQuotes.add(candle);
         }
     return stockQuotes;
    // TODO Auto-generated method stub
  }
  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.

  private static double calculateTotalNumOfYears(LocalDate startDate , LocalDate endDate){
    return startDate.until(endDate, ChronoUnit.DAYS)/364.24;
  }

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
 }


 public static Double getClosingPriceOnEndDate(List<Candle> candles) {
   return candles.get(candles.size()-1).getClose();
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

  

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    // TODO Auto-generated method stub

    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    for(PortfolioTrade trade: portfolioTrades){
      try {
        List<Candle> temp = stockQuotesService.getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
        Double buyPrice = getOpeningPriceOnStartDate(temp);
        Double sellPrice = getClosingPriceOnEndDate(temp);
        annualizedReturns.add(calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice));
      } catch (JsonProcessingException | StockQuoteServiceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    Collections.sort(annualizedReturns, getComparator());
    return annualizedReturns;
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }



  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
