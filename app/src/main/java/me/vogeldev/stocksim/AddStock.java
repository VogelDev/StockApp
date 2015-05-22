package me.vogeldev.stocksim;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class AddStock extends Activity implements OnClickListener {

    EditText etSearch;
    EditText etShares;

    TextView tvName;
    TextView tvPrice;
    TextView tvOwned;
    TextView tvSell;
    TextView tvBuy;
    TextView tvCost;
    TextView tvDif;

    Button btnSearch;
    Button btnMinus;
    Button btnAdd;
    Button btnBuy;
    Button btnSell;

    StockQuote quote;
    DecimalFormat formatter;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);
        init();
    }

    private void init() {
        etSearch = (EditText) findViewById(R.id.infoSymbol);
        etShares = (EditText) findViewById(R.id.infoShares);

        tvName = (TextView) findViewById(R.id.infoName);
        tvPrice = (TextView) findViewById(R.id.infoCurrent);
        tvOwned = (TextView) findViewById(R.id.infoOwned);
        tvSell = (TextView) findViewById(R.id.infoSellVal);
        tvBuy = (TextView) findViewById(R.id.infoBuyVal);
        tvCost = (TextView) findViewById(R.id.infoCost);
        tvDif = (TextView) findViewById(R.id.infoDif);

        btnMinus = (Button) findViewById(R.id.infoMinus);
        btnAdd = (Button) findViewById(R.id.infoAdd);
        btnSearch = (Button) findViewById(R.id.infoSearch);
        btnBuy = (Button) findViewById(R.id.infoBuy);
        btnSell = (Button) findViewById(R.id.infoSell);

        btnMinus.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnSell.setOnClickListener(this);
        btnBuy.setOnClickListener(this);

        formatter = new DecimalFormat("$#,###.00");
    }

    @Override
    public void onClick(View v) {
        int shares = Integer.valueOf(etShares.getText().toString());
        boolean change = false;

        switch (v.getId()) {
            case R.id.infoAdd:
                if (shares < Integer.MAX_VALUE)
                    shares++;
                change = true;
                break;
            case R.id.infoBuy:
                new StockBuy().execute();
                break;
            case R.id.infoMinus:
                if (shares > 0)
                    shares--;
                change = true;
                break;
            case R.id.infoSearch:
                new StockLookup().execute(new String[]{etSearch.getText().toString()});

                break;
            case R.id.infoSell:

                break;
        }

        etShares.setText(String.valueOf(shares));

        if(change && quote != null) {
            setSell(shares, quote.getLastPrice());
            setBuy(shares, quote.getLastPrice());
        }
    }

    private void setSell(int shares, double price) {
        double difference = quote.getSharePrice() - price;
        double sell = difference * shares;

        tvSell.setText(formatter.format(sell));
    }

    private void setBuy(int shares, double price) {
        double buy = shares * price;
        tvBuy.setText(formatter.format(buy));
    }

    private class StockUpdate extends AsyncTask<String, Integer, Long> {
        TodoListSQLHelper sqlHelper;

        protected Long doInBackground(String... symbols) {

            String symbol = symbols[0].toUpperCase();

            sqlHelper = new TodoListSQLHelper(AddStock.this);
            SQLiteDatabase sqLiteDatabase = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();
            long success = 0l;

            Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_SHARES, null, "SYMBOL = '" + symbol + "'", null, null, null, null);

            if (cursor.getCount() == 0) {
                quote = StockFinder.getQuote(symbol);

                if (quote == null)
                    return success;

                quote.addShares(1, quote.getLastPrice());
            } else {
                cursor.moveToFirst();
                String name = cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.SHARES_NAME));
                String sym = cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.SHARES_SYMBOL));
                double price = cursor.getDouble(cursor.getColumnIndex(TodoListSQLHelper.SHARES_CURRENT));
                String trade = cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.SHARES_LASTCHECK));
                int count = cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.SHARES_COUNT));
                double total = cursor.getDouble(cursor.getColumnIndex(TodoListSQLHelper.SHARES_COST));
                quote = new StockQuote(name, sym, price, trade);
                quote.setShares(count);
                quote.setTotalCost(total * count);
                quote.addShares(1, price);

                String deleteTodoItemSql = "DELETE FROM " + TodoListSQLHelper.TABLE_SHARES +
                        " WHERE " + TodoListSQLHelper.SHARES_SYMBOL + " = '" + sym + "'";
                sqLiteDatabase.execSQL(deleteTodoItemSql);
            }

            //write the stock info to the database
            values.put(TodoListSQLHelper.SHARES_NAME, quote.getName());
            values.put(TodoListSQLHelper.SHARES_SYMBOL, quote.getSymbol());
            values.put(TodoListSQLHelper.SHARES_CURRENT, quote.getLastPrice());
            values.put(TodoListSQLHelper.SHARES_LASTCHECK, quote.getLastTrade());
            values.put(TodoListSQLHelper.SHARES_COUNT, quote.getShares());
            values.put(TodoListSQLHelper.SHARES_COST, (quote.getTotalCost() / quote.getShares()));
            sqLiteDatabase.insertWithOnConflict(TodoListSQLHelper.TABLE_SHARES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            sqLiteDatabase.close();
            cursor.close();
            success = 1l;
            return success;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            if (result == 1) {
                tvName.setText(quote.getName());
                tvPrice.setText(formatter.format(quote.getLastPrice()));
                int shares = Integer.valueOf(etShares.getText().toString());
                setBuy(shares, quote.getLastPrice());
                setSell(shares, quote.getLastPrice());
            } else {
                Toast.makeText(getBaseContext(), "Error finding stock", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class StockLookup extends AsyncTask<String, Integer, Long> {
        TodoListSQLHelper sqlHelper;

        protected Long doInBackground(String... symbols) {

            String symbol = symbols[0].toUpperCase();

            sqlHelper = new TodoListSQLHelper(AddStock.this);
            SQLiteDatabase sqLiteDatabase = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();
            long success = 0l;

            Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_SHARES, null, "SYMBOL = '" + symbol + "'", null, null, null, null);


            quote = StockFinder.getQuote(symbol);

            if (quote == null)
                return success;

            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                double price = cursor.getDouble(cursor.getColumnIndex(TodoListSQLHelper.SHARES_CURRENT));
                String trade = cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.SHARES_LASTCHECK));
                int count = cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.SHARES_COUNT));
                double total = cursor.getDouble(cursor.getColumnIndex(TodoListSQLHelper.SHARES_COST));

                quote.setLastTrade(trade);
                quote.setLastPrice(price);
                quote.setShares(count);
                quote.setTotalCost(total * count);
                quote.setSharePrice();
            }
            sqLiteDatabase.close();
            cursor.close();
            success = 1l;
            return success;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            if (result == 1) {
                tvName.setText(quote.getName());
                tvPrice.setText(formatter.format(quote.getLastPrice()));
                tvOwned.setText(String.valueOf(quote.getShares()));
                tvCost.setText(formatter.format(quote.getSharePrice()));

                double difference = quote.getSharePrice() - quote.getLastPrice();
                tvDif.setText((formatter.format(difference)));

                if(difference < 0)
                    tvDif.setTextColor(Color.RED);


                int shares = Integer.valueOf(etShares.getText().toString());
                setBuy(shares, quote.getLastPrice());
                setSell(shares, quote.getLastPrice());
            } else {
                Toast.makeText(getBaseContext(), "Error finding stock", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class StockBuy extends AsyncTask<String, Integer, Long> {
        TodoListSQLHelper sqlHelper;
        String error;

        protected Long doInBackground(String... symbols) {

            long success = 0l;

            if (quote == null)
                return success;

            sqlHelper = new TodoListSQLHelper(AddStock.this);
            SQLiteDatabase sqLiteDatabase = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            int shares = Integer.valueOf(etShares.getText().toString());

            try {
                subMoney(values, sqLiteDatabase, shares);
                updateStock(values, sqLiteDatabase, shares);
                success = 1l;
            }catch(Exception e){
                error = e.getMessage();
                success = 2l;
            }finally{
                sqLiteDatabase.close();
            }

            return success;
        }

        private void updateStock(ContentValues values, SQLiteDatabase sqLiteDatabase, int shares){

            values.clear();

            Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_SHARES, null, "SYMBOL = '" + quote.getSymbol() + "'", null, null, null, null);

            quote.addShares(shares, quote.getLastPrice());

            if (cursor.getCount() != 0) {
                cursor.moveToFirst();

                String deleteTodoItemSql = "DELETE FROM " + TodoListSQLHelper.TABLE_SHARES +
                        " WHERE " + TodoListSQLHelper.SHARES_SYMBOL + " = '" + quote.getSymbol() + "'";
                sqLiteDatabase.execSQL(deleteTodoItemSql);
            }

            //write the stock info to the database
            values.put(TodoListSQLHelper.SHARES_NAME, quote.getName());
            values.put(TodoListSQLHelper.SHARES_SYMBOL, quote.getSymbol());
            values.put(TodoListSQLHelper.SHARES_CURRENT, quote.getLastPrice());
            values.put(TodoListSQLHelper.SHARES_LASTCHECK, quote.getLastTrade());
            values.put(TodoListSQLHelper.SHARES_COUNT, quote.getShares());
            values.put(TodoListSQLHelper.SHARES_COST, (quote.getTotalCost() / quote.getShares()));
            sqLiteDatabase.insertWithOnConflict(TodoListSQLHelper.TABLE_SHARES, null, values, SQLiteDatabase.CONFLICT_IGNORE);

            cursor.close();

        }

        private void subMoney(ContentValues values, SQLiteDatabase sqLiteDatabase, int shares) throws Exception{
            values.clear();
            Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_PLAYER, null, null, null, null, null, null);

            cursor.moveToFirst();

            double money = cursor.getDouble(2);
            money -= quote.getLastPrice() * shares;

            if(money < 0){
                cursor.close();
                throw new Exception("Not enough money.");
            }

            String name = cursor.getString(1);

            String deleteOld = "DELETE FROM " + TodoListSQLHelper.TABLE_PLAYER +
                    " WHERE " + TodoListSQLHelper.PLAYER_NAME + " = '" + name + "'";
            sqLiteDatabase.execSQL(deleteOld);

            values.put(TodoListSQLHelper.PLAYER_NAME, name);
            values.put(TodoListSQLHelper.PLAYER_MONEY, money);
            sqLiteDatabase.insertWithOnConflict(TodoListSQLHelper.TABLE_PLAYER, null, values, SQLiteDatabase.CONFLICT_IGNORE);

            cursor.close();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            if (result == 1) {
                tvName.setText(quote.getName());
                tvPrice.setText(formatter.format(quote.getLastPrice()));


                int shares = Integer.valueOf(etShares.getText().toString());
                setBuy(shares, quote.getLastPrice());
                setSell(shares, quote.getLastPrice());
            } else if(result == 0) {
                Toast.makeText(getBaseContext(), "Error - Search for a stock first.", Toast.LENGTH_LONG).show();
            } else if(result == 2){
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
            }
        }
    }


}
