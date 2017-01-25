/*
==============================PAOA - Raster-based GIS==========================
Name        : LocalLayer sub-class
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Description : Extends the Layer class with local operations
===============================================================================
*/

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LocalLayer extends Layer {

	public LocalLayer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue) {
		super(outLayerName, nRows, nCols, origin, resolution, nullValue);
	}
	
	public LocalLayer(String layerName, String fileName) throws FileNotFoundException {
		super(layerName, fileName);
	}

	/*** Local functions -> support multiple layer input ***/
	protected Layer localSum(String outLayerName, ArrayList<LocalLayer> inputLayers) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);
		Layer currentLayer = null;

		double[][] sum = new double[outLayer.nRows][outLayer.nCols];

		for (int k = 0; k < inputLayers.size(); k++) {
			currentLayer = (Layer) inputLayers.get(k);

			for (int i = 0; i < currentLayer.nRows; i++) {
				for (int j = 0; j < currentLayer.nCols; j++) {
					double value = sum[i][j];
					sum[i][j] = value + currentLayer.values[i][j];
				}
			}
		}

		for (int i = 0; i < currentLayer.nRows; i++) {
			for (int j = 0; j < currentLayer.nCols; j++) {
				outLayer.values[i][j] = sum[i][j];
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer localMaximum(String outLayerName, ArrayList<LocalLayer> inputLayers) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);
		Layer currentLayer = null;

		double[][] max = new double[outLayer.nRows][outLayer.nCols];

		for (int i = 0; i < max.length; i++) {
			for (int j = 0; j < max.length; j++) {
				max[i][j] = Double.NEGATIVE_INFINITY;
			}
		}

		for (int k = 0; k < inputLayers.size(); k++) {
			currentLayer = (Layer) inputLayers.get(k);

			for (int i = 0; i < currentLayer.nRows; i++) {
				for (int j = 0; j < currentLayer.nCols; j++) {
					double value = currentLayer.values[i][j];

					if (value >= max[i][j]) {
						max[i][j] = value;
					}
				}
			}
		}

		// Fill the max values to the outLayer
		for (int i = 0; i < max.length; i++) {
			for (int j = 0; j < max.length; j++) {
				outLayer.values[i][j] = max[i][j];
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();
		
		return outLayer;
	}

	protected Layer localMinimum(String outLayerName, ArrayList<LocalLayer> inputLayers) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);
		Layer currentLayer = null;

		double[][] min = new double[outLayer.nRows][outLayer.nCols];

		for (int i = 0; i < min.length; i++) {
			for (int j = 0; j < min.length; j++) {
				min[i][j] = Double.POSITIVE_INFINITY;
			}
		}

		for (int k = 0; k < inputLayers.size(); k++) {
			currentLayer = (Layer) inputLayers.get(k);

			for (int i = 0; i < currentLayer.nRows; i++) {
				for (int j = 0; j < currentLayer.nCols; j++) {
					double value = currentLayer.values[i][j];

					if (value <= min[i][j]) {
						min[i][j] = value;
					}
				}
			}
		}

		// Fill the max values to the outLayer
		for (int i = 0; i < min.length; i++) {
			for (int j = 0; j < min.length; j++) {
				outLayer.values[i][j] = min[i][j];
			}
		}
		
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer localMean(String outLayerName, ArrayList<LocalLayer> inputLayers) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);
		Layer currentLayer = null;

		double[][] mean = new double[outLayer.nRows][outLayer.nCols];

		for (int k = 0; k < inputLayers.size(); k++) {
			currentLayer = (Layer) inputLayers.get(k);

			for (int i = 0; i < currentLayer.nRows; i++) {
				for (int j = 0; j < currentLayer.nCols; j++) {
					double value = currentLayer.values[i][j];			
					double preValue = mean[i][j];

					mean[i][j] = preValue + value;
				}
			}
		}

		for (int i = 0; i < mean.length; i++) {
			for (int j = 0; j < mean.length; j++) {
				double value = mean[i][j];
				double meanValue = value / inputLayers.size();

				outLayer.values[i][j] = meanValue;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer localVariety(String outLayerName, ArrayList<LocalLayer> inputLayers){

		Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,resolution, nullValue); //new empty layer

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) { 
				ArrayList<Double> pixel_values = new ArrayList<Double>(); //will store all values in current pixel from every layer 
				pixel_values.add(this.values[i][j]); //adds current layer pixel values
				for(Layer inlayer : inputLayers){

					pixel_values.add(inlayer.values[i][j]);
				}
				Set<Double> pixel_set = new HashSet<Double>(pixel_values); //convert to a set to get the variety by length of set

				outLayer.values[i][j] = pixel_set.size();
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();
		
		return outLayer;
	}
}
