//RealTimeChart .java
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class RealTimeChart extends ChartPanel implements Runnable {

    private static final long serialVersionUID = 1L;
    private static TimeSeries xTimeSeries;
    private static TimeSeries yTimeSeries;
    private static TimeSeries zTimeSeries;
    private static TimeSeries aTimeSeries; // altitude
    private static mySerialPort port;
    private static Database database;

    public RealTimeChart(String title) {
	super(createChart(title));
	super.setFillZoomRectangle(true);
	super.setMouseWheelEnabled(true);
	port = new mySerialPort();
	database = new Database();
	port.open("COM5");
    }

    private static JFreeChart createChart(String title) {
	xTimeSeries = new TimeSeries("X value");
	yTimeSeries = new TimeSeries("Y value");
	zTimeSeries = new TimeSeries("Z value");
	aTimeSeries = new TimeSeries("Altitude");

	TimeSeriesCollection accelerometerDataset = new TimeSeriesCollection();
	accelerometerDataset.addSeries(xTimeSeries);
	accelerometerDataset.addSeries(yTimeSeries);
	accelerometerDataset.addSeries(zTimeSeries);
	
	TimeSeriesCollection altitudeDataset = new TimeSeriesCollection();
	altitudeDataset.addSeries(aTimeSeries);

	JFreeChart chart = ChartFactory.createTimeSeriesChart(title, // title
		"Time(s)", // x-axis label
		"Value(g)", // y-axis label
		accelerometerDataset, // data
		true, // create legend
		true, // generate tooltips
		false // generate urls
		);

	chart.setBackgroundPaint(Color.WHITE);
	XYPlot plot = chart.getXYPlot();
	plot.setBackgroundPaint(Color.LIGHT_GRAY);
	plot.setDomainGridlinePaint(Color.WHITE);
	plot.setRangeGridlinePaint(Color.BLACK);
	plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	//plot.setDomainCrosshairVisible(true);
	//plot.setRangeCrosshairVisible(true);

	NumberAxis altitudeAxis = new NumberAxis("Altitude");
	altitudeAxis.setFixedDimension(10.0D);
	altitudeAxis.setAutoRangeIncludesZero(false);
	plot.setRangeAxis(1, altitudeAxis);
	plot.setDataset(1, altitudeDataset);
	plot.mapDatasetToRangeAxis(1, 1);
	    
	DeviationRenderer renderer1 = new DeviationRenderer(true, false);
	renderer1.setSeriesPaint(0, Color.RED);
	renderer1.setSeriesPaint(1, Color.CYAN);
	renderer1.setSeriesPaint(2, Color.BLACK);
	plot.setRenderer(0, renderer1);
	
	DeviationRenderer renderer2 = new DeviationRenderer(true, false);
	renderer2.setSeriesPaint(0, Color.BLUE);
	plot.setRenderer(1, renderer2);
	
	ValueAxis valueaxis = plot.getDomainAxis();
	// valueaxis.setAutoRange(true);
	valueaxis.setFixedAutoRange(30000D);

	valueaxis = plot.getRangeAxis();
	valueaxis.setAutoRange(true);

	return chart;
    }

    public void run() {
	while (true) {
	    try {
		String data = port.getData();
		String[] value = data.split(" ");

		Float xValue = Float.parseFloat(value[0]);
		Float yValue = Float.parseFloat(value[1]);
		Float zValue = Float.parseFloat(value[2]);
		Float aValue = Float.parseFloat(value[3]);

		database.insert(xValue, yValue, zValue, aValue);
		xTimeSeries.add(new Millisecond(), xValue);
		yTimeSeries.add(new Millisecond(), yValue);
		zTimeSeries.add(new Millisecond(), zValue);
		aTimeSeries.add(new Millisecond(), aValue);

		Thread.sleep(100);

	    } catch (InterruptedException e) {
		e.printStackTrace();
	    } catch (NullPointerException e) {
		e.printStackTrace();
	    }
	}
    }

    public static void main(String[] args) {
	JFrame frame = new JFrame("Test Chart");
	RealTimeChart rtcp = new RealTimeChart("Accelerometer value");
	frame.getContentPane().add(rtcp, BorderLayout.CENTER);
	frame.pack();
	frame.setVisible(true);
	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	(new Thread(rtcp)).start();
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent windowevent) {
		database.close();
		port.close();
		System.exit(0);
	    }
	});
    }
}