package com.iflytek.aiui.demo.chat.repository.ping;

import android.graphics.Color;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;


public class HttpTestChartManager {

    private LineChart mLineChart;
    private YAxis mLeftAxis;
    private YAxis mRightAxis;
    private XAxis mXAxis;
    private LineData mLineData;
    private List<ILineDataSet> mLineDataSets = new ArrayList<>();
    private boolean mDoubleTest = false;

    //第一条线x轴数值
    private int mLineIndexA = 0;
    //第二条线x轴数值
    private int mLineIndexB = 0;

    //第一次到达的测试结果对应第一个测试url
    private boolean mIsFstToFst = true;
    private float mMaxScaleX;


    //多条曲线
    public HttpTestChartManager(LineChart lineChart) {
        this.mLineChart = lineChart;
        mMaxScaleX = mLineChart.getViewPortHandler().getMaxScaleX();
        mLeftAxis = this.mLineChart.getAxisLeft();
        mRightAxis = this.mLineChart.getAxisRight();
        mXAxis = this.mLineChart.getXAxis();
        initLineChart();
    }

    /**
     * 初始化LineChar
     */
    private void initLineChart() {
        mLineChart.setDrawGridBackground(false);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setScaleEnabled(true);
        //显示边界
        mLineChart.setDrawBorders(false);
        //折线图例 标签 设置
        Legend legend = mLineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        //显示位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        //X轴设置显示位置在底部
        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mXAxis.setGranularity(1f);
        mXAxis.setLabelCount(10);

        //保证Y轴从0开始，不然会上移一点
        mLeftAxis.setAxisMinimum(0f);
        mRightAxis.setAxisMinimum(0f);
    }

    /**
     * 初始化折线（多条线）
     *
     */
    private void initLineDataSet(String url) {
        LineDataSet lineDataSet = new LineDataSet(null, url);
        lineDataSet.setColor(Color.RED);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setCircleRadius(1.5f);

        lineDataSet.setDrawFilled(true);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setValueTextSize(10f);
        mLineDataSets.add(lineDataSet);

        setDescription("");
        mLineData = new LineData();
        mLineChart.setData(mLineData);
        mLineChart.invalidate();
    }

    public void addNewLine(String url){

        LineDataSet lineDataSet = new LineDataSet(null, url);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setCircleRadius(1.5f);

        lineDataSet.setDrawFilled(true);
        lineDataSet.setCircleColor(Color.GREEN);
        lineDataSet.setHighLightColor(Color.GREEN);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setValueTextSize(10f);
        mLineDataSets.add(lineDataSet);

        mLineChart.invalidate();
        mDoubleTest = true;
    }

    /**
     * 动态添加数据（多条折线图）
     */
    public void addEntry(boolean isFirstResult, String url, long time) {

        if (mLineDataSets.size() == 0) {
            mIsFstToFst = isFirstResult;
            initLineDataSet(url);
            mLineData = new LineData(mLineDataSets);
            mLineChart.setData(mLineData);
        }


        //若第一次到达结果对应第一条线，则第一条线对应第一个测试url，反之第一条线对应第二个测试url
        if(mIsFstToFst != isFirstResult) {
            if(!mDoubleTest){
                addNewLine(url);
            }
            Entry entry = new Entry(mLineIndexA++, time);
            mLineData.addEntry(entry, 1);
        } else {
            Entry entry = new Entry(mLineIndexB++, time);
            mLineData.addEntry(entry, 0);
        }

        mLineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.setVisibleXRangeMaximum(10);
        if (mLineIndexA > 10 || mLineIndexB > 10) {
            mLineChart.getViewPortHandler().setMaximumScaleX(mMaxScaleX);
        }
        mLineChart.moveViewToX((mLineIndexA > mLineIndexB ? mLineIndexA : mLineIndexB));
    }

    public void reset(){
        mLineChart.getViewPortHandler().setMaximumScaleX(1);
        if(mLineChart != null && mLineChart.getLineData() != null) {
            mLineChart.clearValues();
        }
        if(mLineDataSets != null) {
            mLineDataSets.clear();
        }
        mLineIndexA = 0;
        mLineIndexB = 0;
        mDoubleTest = false;
    }

    /**
     * 设置描述信息
     *
     * @param str
     */
    private void setDescription(String str) {
        Description description = new Description();
        description.setText(str);
        mLineChart.setDescription(description);
        mLineChart.invalidate();
    }
}
