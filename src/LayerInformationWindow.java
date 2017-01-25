/*
==============================PAOA - Raster-based GIS==========================
Name        : LayerInformationWindow class
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Description : Creates and updates the information window for each opened layer.
				Providing information about the layer's metadata, histogram and assigned color values.
===============================================================================
*/

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LayerInformationWindow {

	// Fields 
	private Shell parentShell;
	private Shell layerShell;
	public Shell layerInformation;
	private Layer layer;
	private Map<Double, Integer> histogram;
	private ArrayList<Double> uniqueList;

	// legend settings
	private int legendWidth = 20;
	private int legendHeight = 15;
	private int legendMargin = 3;
	private int legendLeftMargin = 10;
	private int legendFontSize = 10;
	private int discreteLimit = 10;
	private Device device = Display.getCurrent ();
	private Font font = new Font(device ,"Arial",legendFontSize, 0);

	//Constructor if layerInformationWindow doesn't exist yet
	public LayerInformationWindow(Shell shell, Shell layerShell, Layer layer) {
		this.parentShell = shell;
		this.layerShell = layerShell;
		this.layer = layer;
		this.uniqueList = layer.getUniqueList();
		Collections.sort(uniqueList);	//sort uniqueList ascending
		this.histogram = histogram();	//calculate histogram HashMap
		this.layerInformation = createLayerInformation();
	}

	//Method for creating LayerInformation Window
	public Shell createLayerInformation() {
		//Layer-Information
		Shell info = new Shell(parentShell);
		info.setLayout(new FillLayout());
		info.setText("Information");
		info.setSize(200,600);
		info.setLocation(layerShell.getBounds().x + layerShell.getBounds().width, layerShell.getBounds().y);

		Rectangle rectShell = info.getBounds();
		Canvas canvas = new Canvas(info, SWT.NONE);	

		//TODO: format as table
		//print layer Informations
		Text layerInfoText = new Text(canvas, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		createText(layerInfoText);		
		layerInfoText.setFont(font);		
		layerInfoText.setBounds(0,0, rectShell.width, 200);
		layerInfoText.setLayoutData(new RowData(200, SWT.DEFAULT));		

		//Draw color values and histogram
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle rectText = layerInfoText.getBounds();
				int variety = layer.getVariety();

				//find histogram bar with highest frequency to normalize it
				int histogramMax = Integer.MIN_VALUE;
				for(Map.Entry<Double, Integer> entry : histogram.entrySet()){						
					if(histogramMax < entry.getValue()){
						histogramMax = entry.getValue();
					}
				}

				//Legend for discrete values
				if(variety <= discreteLimit){
					for (int i = 0; i < uniqueList.size(); i++){		
						//if mapped color exists, draw color
						if (layer.colorValueMap.containsKey(uniqueList.get(i))){
							Double[] rgb = layer.colorValueMap.get(uniqueList.get(i));
							Color color = new Color(device,rgb[0].intValue(),rgb[1].intValue(),rgb[2].intValue());
							e.gc.setBackground(color);
						}
						else
						{
							//TODO: some kind of error handling
							Color errorcolor = new Color (device, 255, 0, 0);
							e.gc.setBackground(errorcolor);
						}
						e.gc.setLineStyle(SWT.LINE_SOLID);
						e.gc.fillRectangle(legendLeftMargin+1,rectText.height + legendMargin + i*(legendHeight + legendMargin) +1, legendWidth-1, legendHeight-1);	
						e.gc.drawRectangle(legendLeftMargin, rectText.height + legendMargin + i*(legendHeight + legendMargin), legendWidth, legendHeight);

						//Write down discrete values
						e.gc.setFont(font);
						e.gc.drawText(String.valueOf(uniqueList.get(i)), legendLeftMargin + legendWidth + legendMargin ,rectText.height + legendMargin + i * (legendHeight + legendMargin), true);
					}

					//Draw histogram outline
					Rectangle rectHistoOutline = new Rectangle(legendLeftMargin + legendWidth  + 50, rectText.height + legendMargin, 100, legendHeight * variety + legendMargin * variety);
					e.gc.setLineStyle(SWT.LINE_DOT);
					Color white = new Color (device, 255, 255, 255);
					e.gc.setBackground(white);
					e.gc.fillRectangle(rectHistoOutline);
					e.gc.drawRectangle(rectHistoOutline);	

					//iterate through histogram map 					
					int relativePos = 0;
					for(Map.Entry<Double, Integer> entry : histogram.entrySet()){						
						//Draw histogram Bar			
						Rectangle rectHistoValue = new Rectangle(rectHistoOutline.x + 1, rectHistoOutline.y + relativePos * (rectHistoOutline.height/histogram.size()), rectHistoOutline.width * entry.getValue()/histogramMax, rectHistoOutline.height / histogram.size() );
						Color color1 = new Color (device, 180, 180, 180);
						e.gc.setLineStyle(SWT.LINE_SOLID);
						e.gc.setBackground(color1);
						e.gc.fillRectangle(rectHistoValue);
						e.gc.drawRectangle(rectHistoValue);	

						//Write down actual and relative value
						double percentage = (double) entry.getValue() * 100 / (layer.nCols * layer.nRows);
						Font smallerFont = new Font(device ,"Arial",legendFontSize-2, 0);
						e.gc.setFont(smallerFont);
						e.gc.drawText(String.valueOf(entry.getValue()) + " (" + formatDouble(percentage) + "%)", rectHistoValue.x + 5, rectHistoValue.y, true);

						//increase relative position
						relativePos++;
					}

					//Draw mean in histogram relative to histogram's size
					double mean = layer.getMean();
					double meanPosition = rectHistoOutline.height / (Math.abs(layer.getMax() - layer.getMin())) * (mean - layer.getMin());				
					Color red = new Color (device, 255, 0, 0);					
					e.gc.setBackground(red);
					e.gc.fillRectangle(legendLeftMargin + legendWidth  + 50, rectText.height + legendMargin + (int) meanPosition ,100,1);
				}

				//Legend for continuous values
				else {
					// display of colors should take up to 256 pixels
					// first case: less than 256 unique values -> make some bigger than 1 pixel
					double fract = 256/uniqueList.size();
					if (fract > 1){
						for (int i = 0; i < uniqueList.size(); i++){
							if (layer.colorValueMap.containsKey(uniqueList.get(i))){
								Double[] rgb = layer.colorValueMap.get(uniqueList.get(i));
								Color color = new Color(device,rgb[0].intValue(),rgb[1].intValue(),rgb[2].intValue());
								e.gc.setBackground(color);
								e.gc.fillRectangle(legendLeftMargin, rectText.height + legendMargin + (int) Math.round(i *fract), legendWidth, (int) Math.round(fract));
							}	
						}
					}
					// second cas: more than 256 unique vales -> leave out some pixels
					else
					{
						int j = 0; //color entries
						for(int i = 0; i < uniqueList.size(); i++){
							if((uniqueList.size()-i) < (256-j)){ //when the rest of the list fits into the rest of the entries
								//draw the colour
								Double[] rgb = layer.colorValueMap.get(uniqueList.get(i));
								Color color = new Color(device,rgb[0].intValue(),rgb[1].intValue(),rgb[2].intValue());
								e.gc.setBackground(color);
								e.gc.fillRectangle(legendLeftMargin, rectText.height + legendMargin + j, legendWidth, 1);

								j++;
							}
							else { // find out how many values have to be skipped so that the rest would fit into the entries
								int k = 2;
								while(((uniqueList.size()-i)/k) > (256-j)){
									k++;
								}
								//draw the next colour
								Double[] rgb = layer.colorValueMap.get(uniqueList.get(i+k));
								Color color = new Color(device,rgb[0].intValue(),rgb[1].intValue(),rgb[2].intValue());
								e.gc.setBackground(color);
								e.gc.fillRectangle(legendLeftMargin, rectText.height + legendMargin + j, legendWidth, 1);

								j++;
								i=i+k;	
							}
						}
					}

					//Draw minimum, maximum value
					e.gc.setFont(font);
					e.gc.drawText(formatDouble(layer.getMin()), legendLeftMargin + legendWidth + legendMargin ,rectText.height + legendMargin, true);
					e.gc.drawText(formatDouble(layer.getMax()), legendLeftMargin + legendWidth + legendMargin ,rectText.height + legendMargin + 255 - legendFontSize, true);

					//Draw histogram outline
					Rectangle rectHistoOutline = new Rectangle(legendLeftMargin + legendWidth  + 50, rectText.height + legendMargin, 100, 256);
					e.gc.setLineStyle(SWT.LINE_DOT);
					Color white = new Color (device, 255, 255, 255);
					e.gc.setBackground(white);
					e.gc.fillRectangle(rectHistoOutline);
					e.gc.drawRectangle(rectHistoOutline);					

					//Iterate through histogram map 
					int relativePos = 0;
					for(Map.Entry<Double, Integer> entry : histogram.entrySet()){
						//Draw histogram Bar
						Rectangle rectHistoVal = new Rectangle(rectHistoOutline.x + 1, rectHistoOutline.y + relativePos * (rectHistoOutline.height/histogram.size()), rectHistoOutline.width * entry.getValue()/histogramMax, rectHistoOutline.height / histogram.size() );
						Color color1 = new Color (device, 128, 128, 128);
						e.gc.setLineStyle(SWT.LINE_SOLID);
						e.gc.setBackground(color1);
						e.gc.fillRectangle(rectHistoVal);
						e.gc.drawRectangle(rectHistoVal);

						//increase relative Position
						relativePos++;
					}

					//Draw mean in histogram
					double mean = layer.getMean();
					double meanPosition = 255 / (Math.abs(layer.getMax() - layer.getMin())) * (mean - layer.getMin());				
					Color red = new Color (device, 255, 0, 0);					
					e.gc.setBackground(red);
					e.gc.fillRectangle(legendLeftMargin + legendWidth  + 50, rectText.height + legendMargin + (int) meanPosition ,100,1);

					//Print highest histogram value
					//Write down actual and relative value
					double percentage = histogramMax * 100 / (layer.nCols * layer.nRows);
					Font smallerFont = new Font(device ,"Arial",legendFontSize-2, 0);

					//TODO: doesn't work
					Text histMax = new Text(canvas, SWT.MULTI | SWT.READ_ONLY | SWT.RIGHT);
					histMax.setFont(smallerFont);
					histMax.setText(String.valueOf(histogramMax) + " (" + String.valueOf(percentage) + "%)");
					histMax.setBounds(canvas.getBounds().x+canvas.getBounds().height, canvas.getBounds().y, legendFontSize, canvas.getBounds().width);
				}
			}
		});
		return info;
	}

	//Formatting doubles adequately 
	private String formatDouble(double value) {
		String formattedValue = "";
		if(value > 100){
			DecimalFormat df = new DecimalFormat("###.#");
			formattedValue = df.format(value);			
		}
		else if (value > 10)
		{
			DecimalFormat df = new DecimalFormat("##.##");
			formattedValue = df.format(value);
		}
		else if (value >1)
		{
			DecimalFormat df = new DecimalFormat("#.###");
			formattedValue = df.format(value);
		}	
		else{
			DecimalFormat df = new DecimalFormat("#.####");
			formattedValue = df.format(value);
		}	    		
		return formattedValue;
	}

	//writes the informations into the text
	private void createText(Text text){
		text.setText("Name:\t\t" + layer.name + "\r\n\r\n");
		text.append("Columns:\t\t" + layer.nCols + "\r\n");
		text.append("Rows:\t\t" + layer.nRows + "\r\n");		
		text.append("X Origin:\t\t" + layer.origin[0] + "\r\n");
		text.append("Y Origin:\t\t" + layer.origin[1] + "\r\n");
		text.append("Resolution:\t" + layer.resolution + "\r\n\r\n");
		text.append("Minimum:\t" + formatDouble(layer.getMin()) + "\r\n");
		text.append("Maximum:\t" + formatDouble(layer.getMax()) + "\r\n");		
		text.append("Mean:\t\t" + formatDouble(layer.getMean()) + "\r\n");
		text.append("Variety:\t\t" + layer.getVariety() + "\r\n");
	}

	// Calculate the Histogram TreeMap
	public Map<Double, Integer> histogram(){ 
		Map<Double, Integer> mapHistogram = new TreeMap<Double, Integer>(); //TreeMap to have a sorted list of values
		double min = layer.getMin();
		int barNumber = uniqueList.size();

		//histogram for more than 64 unique values -> store upper edge of intervals in HashMap
		if (barNumber > 64) {
			barNumber = 63;

			// define interval width
			double range = Math.abs(layer.getMax() - min);
			double interval = range/barNumber;		

			//initialize Histogram bins in TreeMap with 0
			for(int i = 0; i < barNumber; i++){
				mapHistogram.put(i* interval + min, 0);
			}

			//count occurrences of values to histogram bins
			for (int r = 0; r < layer.nRows; r++) {
				for (int c = 0; c < layer.nCols; c++) {
					double val = layer.values[r][c];
					//step through TreeMap to find the correct bin
					for(Map.Entry<Double, Integer> entry : mapHistogram.entrySet()){
						if(val<entry.getKey()){
							Integer amount = new Integer(entry.getValue());
							amount++;
							mapHistogram.put(entry.getKey(), amount);
							break;
						}
					}
				}					
			}
		}

		// histogram for less than 64 unique values -> store values itself in HashMap
		else {
			for (int r = 0; r < layer.nRows; r++) {
				for (int c = 0; c < layer.nCols; c++) {
					double value = layer.values[r][c];
					int amount = 0;					
					if (mapHistogram.containsKey(value)) {
						amount = mapHistogram.get(value);
						amount++;
					} else {
						amount = 1;
					}
					mapHistogram.put(value, amount);
				}
			}
		}
		return mapHistogram;
	}

}
