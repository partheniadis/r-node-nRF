/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.hrs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Point;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * This class uses external library AChartEngine to show dynamic real time line graph for HR values
 */
public class LineGraphView {
	//TimeSeries will hold the data in x,y format for single chart
	private TimeSeries mSeries = new TimeSeries("Finger");
	private TimeSeries mSeries2 = new TimeSeries("Environment");
	private TimeSeries mSeries3 = new TimeSeries("Object");


	//XYMultipleSeriesDataset will contain all the TimeSeries
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	//XYMultipleSeriesRenderer will contain all XYSeriesRenderer and it can be used to set the properties of whole Graph
	private XYMultipleSeriesRenderer mMultiRenderer = new XYMultipleSeriesRenderer();
	private static LineGraphView mInstance = null;

	/**
	 * singleton implementation of LineGraphView class
	 */
	public static synchronized LineGraphView getLineGraphView() {
		if (mInstance == null) {
			mInstance = new LineGraphView();
		}
		return mInstance;
	}

	/**
	 * This constructor will set some properties of single chart and some properties of whole graph
	 */
	public LineGraphView() {
		//add single line chart mSeries
		mDataset.addSeries(mSeries);
		mDataset.addSeries(mSeries2);
		mDataset.addSeries(mSeries3);


		//XYSeriesRenderer is used to set the properties like chart color, style of each point, etc. of single chart
		final XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
		final XYSeriesRenderer seriesRenderer2 = new XYSeriesRenderer();
		final XYSeriesRenderer seriesRenderer3 = new XYSeriesRenderer();
		//set line chart color to Black
		seriesRenderer.setColor(Color.RED);
		//set line chart style to square points
		seriesRenderer.setPointStyle(PointStyle.SQUARE);
		seriesRenderer.setFillPoints(true);

		seriesRenderer2.setColor(Color.BLUE);
		//set line chart style to square points
		seriesRenderer2.setPointStyle(PointStyle.CIRCLE);
		seriesRenderer2.setFillPoints(true);

		seriesRenderer3.setColor(Color.GREEN);
		//set line chart style to square points
		seriesRenderer3.setPointStyle(PointStyle.DIAMOND);
		seriesRenderer3.setFillPoints(true);

		final XYMultipleSeriesRenderer renderer = mMultiRenderer;
		//set whole graph background color to transparent color
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setMargins(new int[] { 50, 65, 40, 5 }); // top, left, bottom, right
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		renderer.setAxesColor(Color.BLACK);
		renderer.setAxisTitleTextSize(24);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.LTGRAY);
		renderer.setLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.DKGRAY);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setYLabelsPadding(4.0f);
		renderer.setXLabelsColor(Color.DKGRAY);
		renderer.setLabelsTextSize(20);
		renderer.setLegendTextSize(20);
		//Disable zoom
		renderer.setPanEnabled(false, false);
		renderer.setZoomEnabled(false, false);
		//set title to x-axis and y-axis
		renderer.setXTitle("    Time (seconds)");
		renderer.setYTitle("               °C Temperature");
		renderer.addSeriesRenderer(seriesRenderer);
		renderer.addSeriesRenderer(seriesRenderer2);
		renderer.addSeriesRenderer(seriesRenderer3);


	}

	/**
	 * return graph view to activity
	 */
	public GraphicalView getView(Context context) {
		final GraphicalView graphView = ChartFactory.getLineChartView(context, mDataset, mMultiRenderer);
		return graphView;
	}

	/**
	 * add new x,y value to chart
	 */
	public void addValue(Point p1, Point p2, Point p3) {
		mSeries.add(p1.x, p1.y);
		mSeries2.add(p2.x, p2.y);
		mSeries3.add(p3.x, p3.y);
	}

	/**
	 * clear all previous values of chart
	 */
	public void clearGraph() {
		mSeries.clear();
		mSeries2.clear();
		mSeries3.clear();
	}

}
