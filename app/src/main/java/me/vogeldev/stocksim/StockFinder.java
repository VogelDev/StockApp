package me.vogeldev.stocksim;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockFinder {

    /**
     * Searches Google Finance for the stock symbol given
     * @param symbol
     * @return
     * 
     */
    public static StockQuote getQuote(String symbol) {

        Stock stock = YahooFinance.get(symbol);
        String name = stock.getName();
        String sym = stock.getSymbol();
        double price = stock.getQuote().getPrice().doubleValue();
        String date = stock.getQuote().getLastTradeTime().getTime().toString();

        StockQuote quote = new StockQuote(name, sym, price, date);

        return quote;
    }
}
