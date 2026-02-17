package com.example.findFrequency;

import android.graphics.Color;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Grapher {

    private static final int remember = 300;
    private GraphView graph;
    private LineGraphSeries<DataPoint>[] mSeries = new LineGraphSeries[3];
    private int x = 0;

    Grapher(View graph) {
        this.graph = (GraphView) graph;
        for (int i = 0; i < 3; i++) {
            mSeries[i] = new LineGraphSeries<DataPoint>();
            this.graph.addSeries(mSeries[i]);
        }

        mSeries[0].setTitle("x");
        mSeries[1].setTitle("y");
        mSeries[2].setTitle("z");
        mSeries[0].setColor(Color.RED);
        mSeries[1].setColor(Color.GREEN);
        mSeries[2].setColor(Color.BLUE);

        this.graph.getViewport().setXAxisBoundsManual(true);
        this.graph.getViewport().setYAxisBoundsManual(false);
    }

    public void reset() {
        x = 0;
        mSeries[0].resetData(new DataPoint[]{});
        mSeries[1].resetData(new DataPoint[]{});
        mSeries[2].resetData(new DataPoint[]{});

    }

    public void addData(long timestamp, float[] values) {
        mSeries[0].appendData(new DataPoint(x, values[0]), true, remember);
        mSeries[1].appendData(new DataPoint(x, values[1]), true, remember);
        mSeries[2].appendData(new DataPoint(x, values[2]), true, remember);
        graph.getViewport().setMinX(x - remember);
        graph.getViewport().setMaxX(x);
        x++;
    }
}
