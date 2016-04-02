package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Collections;

public class StockDetailInformationActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;
    private LineChart lineChartView;
    int maxRange, minRange;
    private String currentStock = "PRICE";
    private Bundle myBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineChartView = (LineChart) findViewById(R.id.chart);

        if(savedInstanceState==null) {
            Intent intent = getIntent();
            if (intent.hasExtra(getResources().getString(R.string.string_symbol))) {
                myBundle = new Bundle();
                currentStock = intent.getStringExtra(getResources().getString(R.string.string_symbol));
                setTitle("Stock Details - "+currentStock.toUpperCase());
                myBundle.putString(getResources().getString(R.string.string_symbol), currentStock);
                getLoaderManager().initLoader(CURSOR_LOADER_ID, myBundle, this);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState=myBundle;
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, myBundle, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.string_symbol))},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        findRange(mCursor);
        fillLineSet();
        mCursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void fillLineSet() {
        if (mCursor == null)
            return;

        mCursor.moveToFirst();
        ArrayList<String> xAxisValues = new ArrayList<>();
        ArrayList<Entry> yAxisValues = new ArrayList<>();

        float price;
        int count = mCursor.getCount();
        for (int i = 0; i < count; i++) {
            price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            xAxisValues.add(i + "");
            yAxisValues.add(new Entry(price, i));
            mCursor.moveToNext();
        }

        LineDataSet set1 = new LineDataSet(yAxisValues, currentStock.toUpperCase());
        set1.setFillAlpha(110);
        set1.setFillColor(Color.parseColor("#b9f6ca"));

        set1.enableDashedLine(10f, 5f, 0f);
        set1.setValueTextColor(Color.WHITE);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.parseColor("#f57c00"));
        set1.setCircleColor(Color.parseColor("#b2ff59"));
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(xAxisValues, dataSets);
        lineChartView.getLegend().setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        lineChartView.getLegend().setWordWrapEnabled(true);
        lineChartView.getLegend().setTextColor(Color.WHITE);
        lineChartView.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartView.getXAxis().setTextColor(Color.GREEN);
        lineChartView.getAxisLeft().setTextColor(Color.GREEN);
        lineChartView.getAxisRight().setEnabled(false);

        lineChartView.setData(data);
        lineChartView.animateXY(1500, 1500);
    }

    public void findRange(Cursor mCursor) {
        ArrayList<Float> mArrayList = new ArrayList<>();
        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mArrayList.add(Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE))));
        }
        maxRange = Math.round(Collections.max(mArrayList));
        minRange = Math.round(Collections.min(mArrayList));
        if (minRange > 100)
            minRange = minRange - 100;
    }
}