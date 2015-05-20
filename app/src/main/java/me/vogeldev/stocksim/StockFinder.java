package me.vogeldev.stocksim;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockFinder {

    /**
     * Searches Yahoo Finance for the stock symbol given
     * @param symbol Symbol of the stock to be searched for
     * @return Returns a StockQuote containing the queried information
     * 
     */
    public static StockQuote getQuote(String symbol) {

        Stock stock = YahooFinance.get(symbol);

        if(stock == null)
            return null;

        String name = stock.getName();
        String sym = stock.getSymbol();
        double price = stock.getQuote().getPrice().doubleValue();
        String date = stock.getQuote().getLastTradeTime().getTime().toString();

        return new StockQuote(name, sym, price, date);
    }
}
