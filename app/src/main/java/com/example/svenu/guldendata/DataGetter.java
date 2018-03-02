package com.example.svenu.guldendata;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by svenu on 22-2-2018.
 */

public class DataGetter {

    private final String TAG = "DataGetter";

    private Context context;
    private int interval;
    private int timeSpan;
    private int timeSpanInterval;
    private ArrayList<GuldenData> shortAverageValues;
    private ArrayList<GuldenData> longAverageValues;
    private ArrayList<GuldenData> sellPoints;
    private ArrayList<GuldenData> buyPoints;
    private JsonGetter jsonGetter;
    private GraphView graphView;
    private ProgressDialog progressDialog;

    private String comparison = "NLG-EUR";
    private final float beginEuros = 20;
    private final int extraDataPoints = 20;

    // Intervals
    public static final int fiveMinutes = 300;
    public static final int quarter = 900;
    public static final int hour = 3600;
    public static final int day = 86400;

    public DataGetter(Context context, int interval, int timeSpan, int timeSpanInterval) {
        this.context = context;
        this.interval = interval;
        this.timeSpan = timeSpan;
        this.timeSpanInterval = timeSpanInterval;
        this.shortAverageValues = new ArrayList<>();
        this.longAverageValues = new ArrayList<>();
        this.sellPoints = new ArrayList<>();
        this.buyPoints = new ArrayList<>();
        this.jsonGetter = new JsonGetter();
    }

    public DataGetter(Context context, String comparison, int interval, int timeSpan, int timeSpanInterval) {
        this.context = context;
        this.comparison = comparison;
        this.interval = interval;
        this.timeSpan = timeSpan;
        this.timeSpanInterval = timeSpanInterval;
        this.shortAverageValues = new ArrayList<>();
        this.longAverageValues = new ArrayList<>();
        this.sellPoints = new ArrayList<>();
        this.buyPoints = new ArrayList<>();
        this.jsonGetter = new JsonGetter();
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public long getTimeSpan() {
        return timeSpan;
    }

    public long getTimeSpanInterval() {
        return timeSpanInterval;
    }

    public void setTimeSpan(int timeSpan, int timeSpanInterval) {
        this.timeSpan = timeSpan;
        this.timeSpanInterval = timeSpanInterval;
    }

    public ArrayList<GuldenData> getShortAverageValues() {
        return shortAverageValues;
    }

    public ArrayList<GuldenData> getLongAverageValues() {
        return longAverageValues;
    }

    public void retrieveData(GraphView graphView, ProgressDialog progressDialog) {
        this.graphView = graphView;
        this.progressDialog = progressDialog;

        progressDialog.setMessage("Retrieving data...");
        progressDialog.show();

        long currentTime = new Date().getTime() / 1000;
        long beginTime = currentTime - (((long) timeSpan * timeSpanInterval) + extraDataPoints * interval);
        String url = "https://api.nocks.com/api/v2/trade-market/" + comparison + "/candles/";
        url = url + beginTime + "/" + currentTime + "/" + interval;
        jsonGetter.getJson(context, url, dataResponse);
    }

    private JsonGetter.DataResponse dataResponse = new JsonGetter.DataResponse() {
        @Override
        public void onJsonResponse(JSONObject response) {
            try {
                JSONArray data = response.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonObject = data.getJSONObject(i);
                    long timeStamp = jsonObject.getLong("timestamp");
                    float factor = Float.valueOf(jsonObject.getString("average"));

                    GuldenData gulden = new GuldenData(factor, timeStamp);
                    shortAverageValues.add(gulden);
                }
                calculateLongAverageValues();
                drawGraph();
            }
            catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }
    };

    public void drawGraph() {
        // Make a line of short-term average.
        LineGraphSeries<DataPoint> shortAverageSeries = appendLineSeriesData(new ArrayList<>(shortAverageValues.subList(extraDataPoints, shortAverageValues.size())));
        shortAverageSeries.setTitle("Short-term Average");
        shortAverageSeries.setColor(Color.BLUE);
        graphView.addSeries(shortAverageSeries);

        // Make a line of long-term average
        LineGraphSeries<DataPoint> longAverageSeries = appendLineSeriesData(longAverageValues);
        longAverageSeries.setTitle("Long-term Average");
        longAverageSeries.setColor(Color.RED);
        graphView.addSeries(longAverageSeries);

        calculateSellBuyPoints();

        // Set points on the sell-points
        PointsGraphSeries<DataPoint> sellPointSeries = appendPointSeriesData(sellPoints);
        sellPointSeries.setTitle("Sellpoints");
        sellPointSeries.setColor(Color.GREEN);
        sellPointSeries.setSize(5);
        graphView.addSeries(sellPointSeries);

        // Set points on the buy-points
        PointsGraphSeries<DataPoint> buyPointSeries = appendPointSeriesData(buyPoints);
        buyPointSeries.setTitle("Buypoints");
        buyPointSeries.setColor(Color.RED);
        buyPointSeries.setSize(5);
        graphView.addSeries(buyPointSeries);

        // set date label formatter
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, DateFormat.getDateInstance()));

        // set manual x bounds to have nice steps
        graphView.getViewport().setMinX(shortAverageValues.get(0).getTimeMillis());
        graphView.getViewport().setMaxX(shortAverageValues.get(shortAverageValues.size() - 1).getTimeMillis());
        graphView.getViewport().setXAxisBoundsManual(true);
        progressDialog.dismiss();
    }

    private PointsGraphSeries<DataPoint> appendPointSeriesData(ArrayList<GuldenData> values) {
        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
        int dataSize = values.size();
        for (int i = 0; i < dataSize; i++) {
            GuldenData gulden = values.get(i);
            Date date = new Date(gulden.getTimeMillis());
            float shortAverageValue = gulden.getFactor();
            series.appendData(new DataPoint(date, shortAverageValue), true, dataSize);
        }
        return series;
    }

    private void calculateSellBuyPoints() {
        int dataSize = longAverageValues.size() - 1;
        for (int i = 0; i < dataSize; i++) {
            float shortAverage = shortAverageValues.get(i + extraDataPoints).getFactor();
            float nextShortAverage = shortAverageValues.get(i + 1 + extraDataPoints).getFactor();

            float longAverage = longAverageValues.get(i).getFactor();
            float nextLongAverage = longAverageValues.get(i + 1).getFactor();
            if (shortAverage < longAverage && nextShortAverage >= nextLongAverage) {
                buyPoints.add(shortAverageValues.get(i + extraDataPoints));
            }
            else if (shortAverage >= longAverage && nextShortAverage < nextLongAverage && buyPoints.size() > 0) {
                sellPoints.add(shortAverageValues.get(i + extraDataPoints));
            }
        }

        float totalEuro = beginEuros;
        for (int j = 0; j < sellPoints.size(); j++) {
            float totalGulden = totalEuro / buyPoints.get(j).getFactor();
            totalEuro = totalGulden * sellPoints.get(j).getFactor();
        }

        Log.d(TAG, "totalWinst: " + totalEuro);
    }

    private LineGraphSeries<DataPoint> appendLineSeriesData(ArrayList<GuldenData> values) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        int dataSize = values.size();
        for (int i = 0; i < dataSize; i++) {
            GuldenData gulden = values.get(i);
            Date date = new Date(gulden.getTimeMillis());
            float shortAverageValue = gulden.getFactor();
            series.appendData(new DataPoint(date, shortAverageValue), true, dataSize);
        }
        return series;
    }

    private void calculateLongAverageValues() {
        int dataSize = shortAverageValues.size();
        for (int i = extraDataPoints; i < dataSize; i++) {
            float longAverageValue = 0;
            for (int j = i - extraDataPoints; j < i; j++) {
                GuldenData longGulden = shortAverageValues.get(j);
                longAverageValue += longGulden.getFactor();
            }
            longAverageValue = longAverageValue / extraDataPoints;
            longAverageValues.add(new GuldenData(longAverageValue, shortAverageValues.get(i).getTimeSeconds()));
        }
    }
}
