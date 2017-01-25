/*
==============================PAOA - Raster-based GIS==========================
Name        : FocalLayer sub-class
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Description : Extends the Layer class with focal operations (neighborhood operations)
===============================================================================
*/

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class FocalLayer extends Layer {

	public FocalLayer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue) {
		super(outLayerName, nRows, nCols, origin, resolution, nullValue);
	}
	
	public FocalLayer(String layerName, String fileName) throws FileNotFoundException {
		super(layerName, fileName);
	}
	
	/*** Focal functions ***/
	protected Layer focalSum(int n_radius, boolean form, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Double> neighborhood;

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				neighborhood = returnNeighborhoodDouble(i, j, n_radius, form);
				double value = 0;

				for (int k = 0; k < neighborhood.size(); k++) {
					value = value + neighborhood.get(k);
				}

				outLayer.values[i][j] = value;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer focalMaximum(int n_radius, boolean form, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Double> neighborhood;

		double[][] max = new double[outLayer.nRows][outLayer.nCols];

		for (int i = 0; i < max.length; i++) {
			for (int j = 0; j < max.length; j++) {
				max[i][j] = Double.NEGATIVE_INFINITY;
			}
		}

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				neighborhood = returnNeighborhoodDouble(i, j, n_radius, form);
				double value = 0;

				for (int z = 0; z < neighborhood.size(); z++) {
					value = neighborhood.get(z);

					if (value >= max[i][j]) {
						max[i][j] = value;
					}
				}

				outLayer.values[i][j] = max[i][j];

			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer focalMinimum(int n_radius, boolean form, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Double> neighborhood;

		double[][] min = new double[outLayer.nRows][outLayer.nCols];

		for (int i = 0; i < min.length; i++) {
			for (int j = 0; j < min.length; j++) {
				min[i][j] = Double.POSITIVE_INFINITY;
			}
		}

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				neighborhood = returnNeighborhoodDouble(i, j, n_radius, form);
				double value = 0;

				for (int z = 0; z < neighborhood.size(); z++) {
					value = neighborhood.get(z);

					if (value <= min[i][j]) {
						min[i][j] = value;
					}
				}

				outLayer.values[i][j] = min[i][j];

			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer focalMean(int r, boolean square, String outLayerName){
		Layer outLayer = new Layer(outLayerName, nRows, nCols, origin,resolution, nullValue); //new empty layer
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) { 
				double sum = 0;
				ArrayList<int[]> all_neighbor = this.returnNeighborhood(i, j, r, square);

				for (int[] neighbor : all_neighbor) {		
					sum = sum + values[neighbor[0]][neighbor[1]];					
				}
				
				double mean = sum/all_neighbor.size();
				outLayer.values[i][j] = mean;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();
		
		return outLayer;
	}

	protected Layer focalVariety(int n_radius, boolean form, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Double> neighborhood;
		ArrayList<Double> uniqueList = new ArrayList<Double>();
		double value;

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				neighborhood = returnNeighborhoodDouble(i, j, n_radius, form);
				value = 0;
				uniqueList.clear();

				for (int k = 0; k < neighborhood.size(); k++) {
					value = neighborhood.get(k);

					if (uniqueList.contains(value)) {
						continue;
					} else {
						uniqueList.add(value);
					}
				}

				outLayer.values[i][j] = uniqueList.size();
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}
}