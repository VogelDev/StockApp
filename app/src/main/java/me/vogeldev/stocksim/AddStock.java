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

        if(change) {
            setSell();
            setBuy();
        }
    }

    private void setSell() {

        int shares = etShares.getText().toString() != null ? Integer.valueOf(etShares.getText().toString()) : 0;
        double price = tvPrice.getText().toString() != null ? Double.valueOf(tvPrice.getText().toString()) : 0;
        double difference = quote.getSharePrice() - price;
        String sell = String.valueOf(difference * shares);

        tvSell.setText(sell);
    }

    private void setBuy() {

        int shares = etShares.getText().toString() != null ? Integer.valueOf(etShares.getText().toString()) : 0;
        double price = tvPrice.getText().toString() != null ? Double.valueOf(tvPrice.getText().toString()) : 0;
        String buy = String.valueOf(shares * price);

        tvBuy.setText(buy);
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
                tvPrice.setText(String.valueOf(quote.getLastPrice()));
                setBuy();
                setSell();
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
                tvPrice.setText(String.valueOf(quote.getLastPrice()));
                tvOwned.setText(String.valueOf(quote.getShares()));
                tvCost.setText(String.valueOf(quote.getSharePrice()));
                tvDif.setText((String.valueOf(quote.getSharePrice() - Double.valueOf(tvPrice.getText().toString()))));

                if(Double.valueOf(tvDif.getText().toString()) < 0)
                    tvDif.setTextColor(Color.RED);

                setBuy();
                setSell();
            } else {
                Toast.makeText(getBaseContext(), "Error finding stock", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class StockBuy extends AsyncTask<String, Integer, Long> {
        TodoListSQLHelper sqlHelper;

        protected Long doInBackground(String... symbols) {

            long success = 0l;

            if (quote == null)
                return success;

            sqlHelper = new TodoListSQLHelper(AddStock.this);
            SQLiteDatabase sqLiteDatabase = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();

            Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_SHARES, null, "SYMBOL = '" + quote.getSymbol() + "'", null, null, null, null);


            quote.addShares(Integer.valueOf(etShares.getText().toString()), quote.getLastPrice());

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
                tvPrice.setText(String.valueOf(quote.getLastPrice()));

                setBuy();
                setSell();
            } else {
                Toast.makeText(getBaseContext(), "Error - Search for a stock first.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
