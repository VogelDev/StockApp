package me.vogeldev.stocksim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    //Create Objects.
    private ListView myList;
    private ListAdapter todoListAdapter;
    private TodoListSQLHelper todoListSQLHelper;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myList = (ListView) findViewById(R.id.list);
        ImageButton fabImageButton = (ImageButton) findViewById(R.id.fab_image_button);
        ImageButton btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        final ArrayList<String> list = new ArrayList<>();
        final MyCustomAdapter adapter = new MyCustomAdapter(this, list);
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        (ListView) findViewById(R.id.list),
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {

                                    String deleteTodoItemSql = "DELETE FROM " + TodoListSQLHelper.TABLE_SHARES +
                                            " WHERE " + TodoListSQLHelper._ID+ " = '" + todoListAdapter.getItemId(position) + "'";

                                    todoListSQLHelper = new TodoListSQLHelper(MainActivity.this);
                                    SQLiteDatabase sqlDB = todoListSQLHelper.getWritableDatabase();
                                    sqlDB.execSQL(deleteTodoItemSql);
                                    updateTodoList();

                                }
                            }

                        });
        findViewById(R.id.list).setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        // findViewById(R.id.list).setOnScrollListener(touchListener.makeScrollListener());

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StockUpdate().execute();
            }
        });

        fabImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                list.add("New Item");
                adapter.notifyDataSetChanged();
                AlertDialog.Builder todoTaskBuilder = new AlertDialog.Builder(MainActivity.this);
                todoTaskBuilder.setTitle("Add Todo Task Item");
                todoTaskBuilder.setMessage("describe the Todo task...");
                final EditText todoET = new EditText(MainActivity.this);
                todoTaskBuilder.setView(todoET);
                AlertDialog.Builder builder = todoTaskBuilder.setPositiveButton("Add Task", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String todoTaskInput = todoET.getText().toString();

                        new StockLookup().execute(todoTaskInput);
                    }
                });

                todoTaskBuilder.setNegativeButton("Cancel", null);

                todoTaskBuilder.create().show();
                */
                Intent i = new Intent(MainActivity.this, AddStock.class);
                startActivityForResult(i, 0);
            }
        });

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView tvStock = (TextView)view;

                Bundle bundle = new Bundle();
                bundle.putString("Name", tvStock.getText().toString());
                Intent i = new Intent(MainActivity.this, AddStock.class);
                startActivityForResult(i, 0, bundle);
            }

        });

        //show the ListView on the screen
        // The adapter MyCustomAdapter is responsible for maintaining the data backing this list and for producing
        // a view to represent an item in that data set.

        updateTodoList();
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
            getMenuInflater().inflate(R.menu.main, menu);
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
        //noinspection SimplifiableIfStatement
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            ArrayList<String> list = new ArrayList<>();

            MyCustomAdapter adapter = new MyCustomAdapter(this, list);

            list.add("New Item");
            adapter.notifyDataSetChanged();
            final AlertDialog.Builder todoTaskBuilder = new AlertDialog.Builder(MainActivity.this);
            todoTaskBuilder.setTitle("Add Todo Task Item");
            todoTaskBuilder.setMessage("describe the Todo task...");
            final EditText todoET = new EditText(MainActivity.this);
            todoTaskBuilder.setView(todoET);
            todoTaskBuilder.setPositiveButton("Add Task", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    String todoTaskInput = todoET.getText().toString();

                    new StockLookup().execute(todoTaskInput);

                    //update the list UI
                    updateTodoList();

                }
            });

            todoTaskBuilder.setNegativeButton("Cancel", null);

            todoTaskBuilder.create().show();


            //show the ListView on the screen
            // The adapter MyCustomAdapter is responsible for maintaining the data backing this list and for producing
            // a view to represent an item in that data set.

            updateTodoList();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        updateTodoList();
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
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
    private void updateTodoList() {
        todoListSQLHelper = new TodoListSQLHelper(MainActivity.this);
        SQLiteDatabase sqLiteDatabase = todoListSQLHelper.getReadableDatabase();

        //cursor to read from database
        Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_SHARES,
                null, null, null, null, null, null);

        //binds the list with the UI
        todoListAdapter = new SimpleCursorAdapter(
                this,
                R.layout.due,
                cursor,
                new String[]{TodoListSQLHelper.SHARES_SYMBOL, TodoListSQLHelper.SHARES_COUNT, TodoListSQLHelper.SHARES_CURRENT, TodoListSQLHelper.SHARES_COST},
                new int[]{R.id.tvSymbol, R.id.tvShares, R.id.tvPrice, R.id.tvDif},
                0
        );

        myList.setAdapter(todoListAdapter);
        //sqLiteDatabase.close();
        //cursor.close();
    }

    //closing the item
    public void onDoneButtonClick(View view) {
        View v = (View) view.getParent();
        TextView todoTV = (TextView) v.findViewById(R.id.tvSymbol);
        String todoTaskItem = todoTV.getText().toString();

        String deleteTodoItemSql = "DELETE FROM " + TodoListSQLHelper.TABLE_SHARES +
                " WHERE " + TodoListSQLHelper.SHARES_SYMBOL + " = '" + todoTaskItem + "'";

        todoListSQLHelper = new TodoListSQLHelper(MainActivity.this);
        SQLiteDatabase sqlDB = todoListSQLHelper.getWritableDatabase();
        sqlDB.execSQL(deleteTodoItemSql);
        updateTodoList();
        sqlDB.close();
    }


    private class StockLookup extends AsyncTask<String, Integer, Long> {
        StockQuote quote;
        protected Long doInBackground(String... symbols) {

            String symbol = symbols[0].toUpperCase();

            todoListSQLHelper = new TodoListSQLHelper(MainActivity.this);
            SQLiteDatabase sqLiteDatabase = todoListSQLHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();
            long success = 0l;

            Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_SHARES, null, "SYMBOL = '" + symbol +"'", null, null, null, null );

            if(cursor.getCount() == 0){
                quote = StockFinder.getQuote(symbol);

                if(quote == null)
                    return success;

                quote.addShares(1, quote.getLastPrice());
            }else{
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
            if(result == 1)
                updateTodoList();
            else {
                Toast.makeText(getBaseContext(), "Error finding stock", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class StockUpdate extends AsyncTask<String, Integer, Long>{

        protected Long doInBackground(String... param) {
            StockQuote quoteOld, quoteNew;
            StockQuote[] stocks;
            todoListSQLHelper = new TodoListSQLHelper(MainActivity.this);
            SQLiteDatabase sqLiteDatabase = todoListSQLHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.clear();
            long success = 0l;

            Cursor cursor = sqLiteDatabase.query(TodoListSQLHelper.TABLE_SHARES, null, null, null, null, null, null );

            if(cursor.getCount() == 0){
                return success;
            }else{
                stocks = new StockQuote[cursor.getCount()];
                cursor.moveToFirst();
                do {
                    String name = cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.SHARES_NAME));
                    String sym = cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.SHARES_SYMBOL));
                    double price = cursor.getDouble(cursor.getColumnIndex(TodoListSQLHelper.SHARES_CURRENT));
                    String trade = cursor.getString(cursor.getColumnIndex(TodoListSQLHelper.SHARES_LASTCHECK));
                    int count = cursor.getInt(cursor.getColumnIndex(TodoListSQLHelper.SHARES_COUNT));
                    double total = cursor.getDouble(cursor.getColumnIndex(TodoListSQLHelper.SHARES_COST));
                    quoteOld = new StockQuote(name, sym, price, trade);
                    quoteOld.setShares(count);
                    quoteOld.setTotalCost(total * count);

                    quoteNew = StockFinder.getQuote(sym);

                    if (quoteNew == null)
                        return success;

                    quoteOld.setLastPrice(quoteNew.getLastPrice());
                    quoteOld.setLastTrade(quoteNew.getLastTrade());

                    String deleteTodoItemSql = "DELETE FROM " + TodoListSQLHelper.TABLE_SHARES +
                            " WHERE " + TodoListSQLHelper.SHARES_SYMBOL + " = '" + sym + "'";
                    sqLiteDatabase.execSQL(deleteTodoItemSql);

                    stocks[cursor.getPosition()] = quoteOld;

                    cursor.moveToNext();
                }while(!cursor.isAfterLast());
            }
            cursor.close();
            for(StockQuote quote: stocks){
                //write the stock info to the database
                values.put(TodoListSQLHelper.SHARES_NAME, quote.getName());
                values.put(TodoListSQLHelper.SHARES_SYMBOL, quote.getSymbol());
                values.put(TodoListSQLHelper.SHARES_CURRENT, quote.getLastPrice());
                values.put(TodoListSQLHelper.SHARES_LASTCHECK, quote.getLastTrade());
                values.put(TodoListSQLHelper.SHARES_COUNT, quote.getShares());
                values.put(TodoListSQLHelper.SHARES_COST, (quote.getTotalCost() / quote.getShares()));
                sqLiteDatabase.insertWithOnConflict(TodoListSQLHelper.TABLE_SHARES, null, values, SQLiteDatabase.CONFLICT_IGNORE);

            }

            sqLiteDatabase.close();
            success = 1l;
            return success;
        }

        protected void onProgressUpdate(Integer... params) {
        }

        protected void onPostExecute(Long result) {
            if(result == 1) {
                updateTodoList();
                Toast.makeText(getBaseContext(), "Updated", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getBaseContext(), "Error Updating", Toast.LENGTH_LONG).show();
            }
        }
    }
}
