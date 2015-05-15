package me.vogeldev.stocksim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.stream.JsonReader;

public class StockFinder {

    /**
     * Searches Google Finance for the stock symbol given
     * @param symbol
     * @return
     * @throws Exception
     * 
     */
    public static StockQuote getQuote(String symbol) {

        String json = null;
        try {
            // Download JSON file to a String object with the given stock symbol.
            // If this fails, null is returned. This can fail if the symbol is wrong
            // or if there is no internet connection 
            json = readUrl("https://www.google.com/finance/info?q=" + symbol);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        //Create placeholder vaiables
        StockQuote stock = null;
        String id = new String();
        String symb = new String();
        double lastPrice = 0;
        String lastTrade = new String();
        try {
            //Create a JSON reader to parse the String
            JsonReader reader = new JsonReader(new StringReader(json));
            //Setting lenient to true will allow the parser to ignore a feww issues it may find
            //in this case, the Google JSON file starts with a "//" which won't parse properly
            reader.setLenient(true);
            reader.beginObject();

            //Start cycling through the JSON object and pull out the important data
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("id")) {
                    id = reader.nextString();
                } else if (name.equals("t")) {
                    symb = reader.nextString();
                } else if (name.equals("l")) {
                    lastPrice = reader.nextDouble();
                } else if (name.equals("elt")) {
                    lastTrade = reader.nextString();
                } else {
                    //Ignore anything not listed
                    reader.skipValue();
                }
            }

            reader.endObject();
            reader.close();
            
            //create the StockQuote object and return it
            stock = new StockQuote(id, symb, lastPrice, lastTrade);
            return stock;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String readUrl(String urlString) {
        BufferedReader reader = null;
        StringBuffer buffer = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        if(buffer != null)
            return buffer.toString();
        else
            return null;
    }

}
