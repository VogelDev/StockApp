package me.vogeldev.stocksim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


public class StockList extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private ListView myList;
    private ListAdapter stockAdapter;
    private StockSQLHelper sqlHelper;


    //Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;
    //Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        myList = (ListView) findViewById(R.id.stockList);
        ImageButton btnAddStock = (ImageButton) findViewById(R.id.btnAdd);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        btnAddStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StockList.this, AddStock.class);
                startActivity(i);
            }
        });

        myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tvSymbol = (TextView) view.findViewById(R.id.tvSymbol);
                String symbol = tvSymbol.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString("symbol", symbol);
                Intent i = new Intent(StockList.this, AddStock.class);
                i.putExtras(bundle);

                startActivity(i);

                return false;
            }
        });

        updateStockList();
        checkLogin();
    }

    private void checkLogin() {

        sqlHelper = new StockSQLHelper(StockList.this);
        final SQLiteDatabase sqlDB = sqlHelper.getWritableDatabase();

        Cursor cursor = sqlDB.query(StockSQLHelper.TABLE_PLAYER,
                null, null, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() == 0) {
            AlertDialog.Builder login = new AlertDialog.Builder(StockList.this);
            login.setTitle("Login");
            login.setMessage("What is your name?");

            final EditText input = new EditText(StockList.this);
            login.setView(input);

            login.setPositiveButton("Enter Name", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = input.getText().toString();

                    ContentValues values = new ContentValues();
                    values.clear();

                    values.put(StockSQLHelper.PLAYER_NAME, name);
                    values.put(StockSQLHelper.PLAYER_MONEY, StockSQLHelper.INITIAL_MONEY);

                    sqlDB.insertWithOnConflict(StockSQLHelper.TABLE_PLAYER,
                            null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    sqlDB.close();

                    updateStockList();
                }
            });

            cursor.close();

            login.create().show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateStockList();
    }

    private void updateStockList() {
        sqlHelper = new StockSQLHelper(StockList.this);
        SQLiteDatabase sqlDB = sqlHelper.getReadableDatabase();

        Cursor cursor = sqlDB.query(StockSQLHelper.TABLE_SHARES,
                null, null, null, null, null, null);

        stockAdapter = new SimpleCursorAdapter(
                this,
                R.layout.stocks,
                cursor,
                new String[]{
                        StockSQLHelper.SHARES_SYMBOL,
                        StockSQLHelper.SHARES_COUNT,
                        StockSQLHelper.SHARES_CURRENT,
                        StockSQLHelper.SHARES_COST},
                new int[]{
                        R.id.tvSymbol,
                        R.id.tvShares,
                        R.id.tvPrice,
                        R.id.tvDif},
                0);
        myList.setAdapter(stockAdapter);

        cursor = sqlDB.query(StockSQLHelper.TABLE_PLAYER,
                null, null, null, null, null, null);

        if(cursor.getCount() != 0){
            cursor.moveToFirst();

            TextView tvName = (TextView) findViewById(R.id.tvName);
            TextView tvMoney = (TextView) findViewById(R.id.tvMoney);

            tvName.setText(cursor.getString(1));

            double money = cursor.getDouble(2);
            DecimalFormat formatter = new DecimalFormat("$#,###.00");
            tvMoney.setText(formatter.format(money));
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position){
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            case 1:
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.stock_list, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_stock_list, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((StockList) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private class StockUpdate extends AsyncTask<String, Integer, Long>{

        protected Long doInBackground(String... param) {
            StockQuote quoteOld, quoteNew;
            StockQuote[] stocks;
            sqlHelper = new StockSQLHelper(StockList.this);
            SQLiteDatabase sqlDB = sqlHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();
            long success = 0l;

            Cursor cursor = sqlDB.query(StockSQLHelper.TABLE_SHARES, null, null, null, null, null, null );

            if(cursor.getCount() == 0){
                return success;
            }else{
                stocks = new StockQuote[cursor.getCount()];
                cursor.moveToFirst();
                do {
                    String name = cursor.getString(cursor.getColumnIndex(StockSQLHelper.SHARES_NAME));
                    String sym = cursor.getString(cursor.getColumnIndex(StockSQLHelper.SHARES_SYMBOL));
                    double price = cursor.getDouble(cursor.getColumnIndex(StockSQLHelper.SHARES_CURRENT));
                    String trade = cursor.getString(cursor.getColumnIndex(StockSQLHelper.SHARES_LASTCHECK));
                    int count = cursor.getInt(cursor.getColumnIndex(StockSQLHelper.SHARES_COUNT));
                    double total = cursor.getDouble(cursor.getColumnIndex(StockSQLHelper.SHARES_COST));
                    quoteOld = new StockQuote(name, sym, price, trade);
                    quoteOld.setShares(count);
                    quoteOld.setTotalCost(total * count);

                    quoteNew = StockFinder.getQuote(sym);

                    if (quoteNew == null)
                        return success;

                    quoteOld.setLastPrice(quoteNew.getLastPrice());
                    quoteOld.setLastTrade(quoteNew.getLastTrade());

                    String deleteTodoItemSql = "DELETE FROM " + StockSQLHelper.TABLE_SHARES +
                            " WHERE " + StockSQLHelper.SHARES_SYMBOL + " = '" + sym + "'";
                    sqlDB.execSQL(deleteTodoItemSql);

                    stocks[cursor.getPosition()] = quoteOld;

                    cursor.moveToNext();
                }while(!cursor.isAfterLast());
            }
            cursor.close();
            for(StockQuote quote: stocks){
                //write the stock info to the database
                values.put(StockSQLHelper.SHARES_NAME, quote.getName());
                values.put(StockSQLHelper.SHARES_SYMBOL, quote.getSymbol());
                values.put(StockSQLHelper.SHARES_CURRENT, quote.getLastPrice());
                values.put(StockSQLHelper.SHARES_LASTCHECK, quote.getLastTrade());
                values.put(StockSQLHelper.SHARES_COUNT, quote.getShares());
                values.put(StockSQLHelper.SHARES_COST, (quote.getTotalCost() / quote.getShares()));
                sqlDB.insertWithOnConflict(StockSQLHelper.TABLE_SHARES, null, values, SQLiteDatabase.CONFLICT_IGNORE);

            }

            sqlDB.close();
            success = 1l;
            return success;
        }

        protected void onProgressUpdate(Integer... params) {
        }

        protected void onPostExecute(Long result) {
            if(result == 1) {
                updateStockList();
                Toast.makeText(getBaseContext(), "Updated", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getBaseContext(), "Error Updating", Toast.LENGTH_LONG).show();
            }
        }
    }

}
