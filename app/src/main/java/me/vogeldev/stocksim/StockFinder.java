package me.vogeldev.stocksim;

import android.util.Log;
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

        if(symbol.length() == 0)
            return null;

        Stock stock = YahooFinance.get(symbol);

        Log.i("added", stock.getName());

        if(stock == null)
            return null;

        if(stock.getName().equals("N/A"))
            return null;

        String name = stock.getName();
        String sym = stock.getSymbol();
        double price = stock.getQuote().getPrice().doubleValue();
        String date = stock.getQuote().getLastTradeTime().getTime().toString();

        return new StockQuote(name, sym, price, date);
    }
}
