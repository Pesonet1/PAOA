/*
==============================PAOA - Raster-based GIS==========================
Name        : GeneralLayer sub-class
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Description : Extends the Layer class with general raster operations (classifications, surface-tools)
===============================================================================
*/

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class GeneralLayer extends Layer {

	public GeneralLayer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue) {
		super(outLayerName, nRows, nCols, origin, resolution, nullValue);
	}

	public GeneralLayer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue, double[][] values) {
		super(outLayerName, nRows, nCols, origin, resolution, nullValue);
		this.values = values;
	}

	public GeneralLayer(String layerName, String fileName) throws FileNotFoundException {
		super(layerName, fileName);
	}


	protected Layer Reclass(String outLayerName, HashMap<Double, Double> classMap) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue); //new empty layer
		Set<Double> oldValues = classMap.keySet(); // creates a set of the values that are to be reclassified

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				if (oldValues.contains(this.values[i][j])) { //if current pixel should be reclassified
					outLayer.values[i][j] = classMap.get(this.values[i][j]); //gets new value for current pixel
				}		
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	//Equal-Interval-Classification of data
	protected Layer ClassifyEQI(String outLayerName, int nrClass){
		Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,resolution, nullValue); //new empty layer
		double min = this.getMin();
		double max = this.getMax() + 1;
		double divider = (max-min)/nrClass; //decides the break of each class
		ArrayList<double[]> ranges = new ArrayList<double[]>(); // will contain all ranges of each class
		double[] interval = new double[2]; // will contain low and high border of each class

		double low = min;
		double high = divider + min;
		while(high <= max){
			interval[0] = low;
			interval[1] = high;
			ranges.add(interval.clone());
			low = high;
			high = high + divider; 
		}
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				for(double[] range : ranges){
					if(this.values[i][j] >= range[0] && this.values[i][j] < range[1]){
						outLayer.values[i][j] = ranges.indexOf(range) + 1;
					}
				}
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer ClassifyCustom(String outLayerName, HashMap<Double[], Double> classMap){ //HashMap (key[], value)
		Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,resolution, nullValue); //new empty layer
		Set<Double[]> ranges = classMap.keySet(); // creates set of ranges to be classified
		
		//Sets all pixels that will be reclassified
		for (Double[] key : ranges){ 
			double low = key[0]; //lower limit in range
			double high = key[1]; //upper limit in range
			
			for (int i = 0; i < nRows; i++) {
				for (int j = 0; j < nCols; j++) {
					if(this.values[i][j] >= low && this.values[i][j] < high){ // if current pixel is within key-range, set to key-value
						outLayer.values[i][j] = classMap.get(key);
					}
				}
			}
		}
		// Sets all other pixels to the same as in inLayer (those that should not be reclassified)
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				if(outLayer.values[i][j] == outLayer.nullValue){
					outLayer.values[i][j] = this.values[i][j];
				}
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();
		
		return outLayer;
	}


	private double[][] highPassValues(int i, int j){
		//Assign the high pass filter values to the same index as the pixels neighborhood 
		double[][] filterValues = new double[nRows][nCols];
		try{
			filterValues[i-1][j-1] = -0.7;
			filterValues[i-1][j] = -1.0;
			filterValues[i-1][j+1] = -0.7;
			filterValues[i][j-1] = -1.0;
			filterValues[i][j] = 6.8;
			filterValues[i][j+1] = -1.0;
			filterValues[i+1][j-1] = -0.7;
			filterValues[i+1][j] = -1.0;
			filterValues[i+1][j+1] = -0.7;
		}
		catch(ArrayIndexOutOfBoundsException exception){
			;
		}

		return filterValues;
	}

	protected Layer highPass(String outLayerName){
		Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,resolution, nullValue); //new empty layer

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {

				ArrayList<int[]> neighborhood = this.returnNeighborhood(i, j, 1, true); //contains coordinates of neighbors
				double[][] filterValues = this.highPassValues(i, j); //separate method 
				double newValue = 0; //new value of pixel after filter application

				for (int[] neighbor : neighborhood){

					double oldValue = this.values[neighbor[0]][neighbor[1]];
					double filterValue = filterValues[neighbor[0]][neighbor[1]];
					newValue = newValue + (oldValue*filterValue); 
				}
				outLayer.values[i][j] = newValue;
			}			
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	/*** Slope raster function ***/
	protected Layer slope(String outLayerName){
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Double> neighborhood;
		double var_dx;
		double var_dy;
		double rise_run;
		double slope_degrees;

		for (int m = 0; m < this.nRows; m++) {
			for (int n = 0; n < this.nCols; n++) {
				neighborhood = returnSlopeNeighborhood(m,n);
				double value = 0;

				double a = neighborhood.get(0);
				double d = neighborhood.get(1);
				double g = neighborhood.get(2);
				double b = neighborhood.get(3);
				//				double e = neighborhood.get(4);
				double h = neighborhood.get(5);
				double c = neighborhood.get(6);
				double f = neighborhood.get(7);
				double i = neighborhood.get(8);

				var_dx = ((c + 2*f +i) - (a + 2*d + g)) / (8 * this.resolution);
				var_dy = ((g + 2*h +i) - (a + 2*b + c)) / (8 * this.resolution);
				rise_run = Math.sqrt(Math.pow(var_dx, 2) + Math.pow(var_dy, 2));
				slope_degrees = Math.atan(rise_run) * (180/Math.PI);
				value = slope_degrees;

				outLayer.values[m][n] = value;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer aspect(String outLayerName){
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Double> neighborhood;
		double var_dx;
		double var_dy;
		double aspect;
		double aspect_rad;

		for (int m = 0; m < this.nRows; m++) {
			for (int n = 0; n < this.nCols; n++) {

				neighborhood = returnSlopeNeighborhood(m,n);
				double value = 0;


				double a = neighborhood.get(0);
				double d = neighborhood.get(1);
				double g = neighborhood.get(2);
				double b = neighborhood.get(3);
				//				double e = neighborhood.get(4);
				double h = neighborhood.get(5);
				double c = neighborhood.get(6);
				double f = neighborhood.get(7);
				double i = neighborhood.get(8);

				var_dx = ((c + 2*f +i) - (a + 2*d + g)) / (8 * this.resolution);
				var_dy = ((g + 2*h +i) - (a + 2*b + c)) / (8 * this.resolution);
				aspect_rad = Math.atan2(var_dy, -var_dx);

				if (var_dx != 0){
					aspect_rad = Math.atan2(var_dy, -var_dx);

					if (aspect_rad < 0){
						aspect_rad = 2*Math.PI + aspect_rad;
					}
				}
				else {
					if (var_dy > 0){
						aspect_rad = Math.PI;
					}
					else if (var_dy < 0){
						aspect_rad = 2*Math.PI - (Math.PI/2);
					}
				}

				aspect = aspect_rad * (180/Math.PI);
				aspect = (double) 90.0 - aspect;
				if (aspect < 0){
					aspect = (double) aspect + 360.0;
				}
				value = aspect;
				outLayer.values[m][n] = value;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer hillshade(String outLayerName, double altitude_deg, double azimuth_deg){
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Double> neighborhood;
		double var_dx;
		double var_dy;
		//		double aspect;
		double aspect_rad;
		double rise_run;
		double slope_rad;
		double zenith_deg;
		double zenith_rad;
		double azimuth_math;
		double azimuth_rad;
		double hillshade;

		for (int m = 0; m < this.nRows; m++) {
			for (int n = 0; n < this.nCols; n++) {

				neighborhood = returnSlopeNeighborhood(m,n);
				double value = 0;

				double a = neighborhood.get(0);
				double d = neighborhood.get(1);
				double g = neighborhood.get(2);
				double b = neighborhood.get(3);
				//				double e = neighborhood.get(4);
				double h = neighborhood.get(5);
				double c = neighborhood.get(6);
				double f = neighborhood.get(7);
				double i = neighborhood.get(8);

				var_dx = ((c + 2*f +i) - (a + 2*d + g)) / (8 * this.resolution);
				var_dy = ((g + 2*h +i) - (a + 2*b + c)) / (8 * this.resolution);
				aspect_rad = Math.atan2(var_dy, -var_dx);

				if (var_dx != 0){
					aspect_rad = Math.atan2(var_dy, -var_dx);

					if (aspect_rad < 0){
						aspect_rad = 2*Math.PI + aspect_rad;
					}
				}
				else {
					if (var_dy > 0){
						aspect_rad = Math.PI;
					}
					else if (var_dy < 0){
						aspect_rad = 2*Math.PI - (Math.PI/2);
					}
				}

				rise_run = Math.sqrt(Math.pow(var_dx, 2) + Math.pow(var_dy, 2));
				slope_rad = Math.atan(rise_run);

				zenith_deg = 90 - altitude_deg;
				zenith_rad = zenith_deg * (Math.PI/180);

				azimuth_math = 360 - azimuth_deg + 90;
				if (azimuth_math <=  360){
					azimuth_math = azimuth_math - 360;
				}
				azimuth_rad = azimuth_math * (Math.PI/180);

				hillshade = 255 * ( (Math.cos(zenith_rad) * (Math.cos(slope_rad))) + (Math.sin(zenith_rad) * Math.sin(slope_rad) * Math.cos(azimuth_rad - aspect_rad)));
				value = hillshade;
				outLayer.values[m][n] = value;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}
}
