package me.vogeldev.stocksim;

public class StockQuote {
    //ID
    private String id;
    //Name
    private String name;
    //Symbol
    private String symbol;
    //Last quote
    private double lastPrice;
    //Last update
    private String lastTrade;
    //Shares
    private int shares;
    //Total spent
    private double totalCost;
    //Average share worth;
    private double sharePrice;

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Creates new object with initial data.
     */
    public StockQuote(String name, String symbol, double lastPrice, String lastTrade) {
        this.name = name;
        this.symbol = symbol;
        this.lastPrice = lastPrice;
        this.lastTrade = lastTrade;
        shares = 0;
        totalCost = 0;
    }

    @Override
    public String toString() {

        //loss/gain
        double difference = totalCost - (lastPrice * shares);

        return symbol + "\t" + shares + "\n" + lastPrice + "\t" + difference;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String t) {
        this.symbol = t;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double l) {
        lastPrice = l;
    }

    public String getLastTrade() {
        return lastTrade;
    }

    public void setLastTrade(String elt) {
        lastTrade = elt;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public double getTotalCost() {
        return totalCost;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addShares(int shares, double price) {

        this.shares += shares;
        totalCost += (shares * price);
        sharePrice = totalCost / shares;
    }

    public double sellShares(int shares) {

        this.shares -= shares;
        double worth = shares * lastPrice;
        totalCost -= worth;
        sharePrice = totalCost / this.shares;

        return worth;
    }

    public void setSharePrice(){
        sharePrice = totalCost / shares;
    }

    public double getSharePrice(){
        return sharePrice;
    }
}
