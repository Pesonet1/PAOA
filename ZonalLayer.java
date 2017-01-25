/*
==============================PAOA - Raster-based GIS==========================
Name        : ZonalLayer sub-class
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Description : Extends the Layer class with zonal operations
===============================================================================
*/

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class ZonalLayer extends Layer {
	
	public ZonalLayer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue) {
		super(outLayerName, nRows, nCols, origin, resolution, nullValue);
	}

	public ZonalLayer(String layerName, String fileName) throws FileNotFoundException {
		super(layerName, fileName);
	}

	/*** Zonal functions ***/
	protected Layer zonalSum(Layer zoneLayer, String outLayerName) {
		ZonalLayer outLayer = new ZonalLayer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		HashMap<Double, Double> hm = new HashMap<Double, Double>();

		double zoneValue = 0; Double zoneObj;
		double cellValue = 0; Double cellObj;

		// Go through every cell and find unique zones and calculate the sum of the values to these
		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {

				// Go through every zone and cell value
				zoneValue = zoneLayer.values[i][j];
				zoneObj = new Double(zoneValue);

				cellValue = this.values[i][j];
				cellObj = new Double(cellValue);

				// Check if the hashmap already contains the key -> if not, add it
				if (hm.containsKey(zoneObj) == false) {
					hm.put(zoneObj, cellObj);
				} else {
					// Add the current zone value to the current cell value and replace it for the specific zone
					Double getValue = hm.get(zoneObj);
					double value = getValue + cellObj;
					hm.put(zoneObj, value);
				}
			}
		}

		// Go through every cell again and assign the correct value based on the zone
		for (int k = 0; k < this.nRows; k++) {
			for (int l = 0; l < this.nCols; l++) {
				zoneValue = zoneLayer.values[k][l];
				double value = hm.get(zoneValue);
				outLayer.values[k][l] = value;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();


		return outLayer;

	}

	protected Layer zonalMinimum(Layer zoneLayer, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		HashMap<Double, Double> hm = new HashMap<Double, Double>();

		double zoneValue = 0; Double zoneObj;
		double cellValue = 0; Double cellObj;

		// Go through every cell in the matrix and find every unique zone and attach the minimum value to it
		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {

				// Go through every zone and cell value
				zoneValue = zoneLayer.values[i][j];
				zoneObj = new Double(zoneValue);

				cellValue = this.values[i][j];
				cellObj = new Double(cellValue);

				// Check if the hashmap already contains the key -> if not, add it
				if (hm.containsKey(zoneObj) == false) {
					hm.put(zoneObj, cellObj);
				} else {
					Double getValue = hm.get(zoneObj);
					// If the current cell value is smaller than the current stored value insert that instead
					if (cellObj < getValue) {
						hm.put(zoneObj, cellObj);
					}
				}
			}
		}

		// Go through the matrix again and assign the zone minimum value to the according outlayer position
		for (int k = 0; k < this.nRows; k++) {
			for (int l = 0; l < this.nCols; l++) {
				zoneValue = zoneLayer.values[k][l];
				double value = hm.get(zoneValue);
				outLayer.values[k][l] = value;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();

		return outLayer;
	}

	protected Layer zonalMaximum(Layer zoneLayer, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		HashMap<Double, Double> hm = new HashMap<Double, Double>();

		double zoneValue = 0; Double zoneObj;
		double cellValue = 0; Double cellObj;

		// Go through every cell in the matrix and find every unique zone and attach the minimum value to it
		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {

				// Go through every zone and cell value
				zoneValue = zoneLayer.values[i][j];
				zoneObj = new Double(zoneValue);

				cellValue = this.values[i][j];
				cellObj = new Double(cellValue);

				// Check if the hashmap already contains the key -> if not, add it
				if (hm.containsKey(zoneObj) == false) {
					hm.put(zoneObj, cellObj);
				} else {
					Double getValue = hm.get(zoneObj);
					// If the current cell value is bigger than the current stored value insert that instead
					if (cellObj > getValue) {
						hm.put(zoneObj, cellObj);
					}
				}
			}
		}

		// Go through the matrix again and assign the zone minimum value to the according outlayer position
		for (int k = 0; k < this.nRows; k++) {
			for (int l = 0; l < this.nCols; l++) {
				zoneValue = zoneLayer.values[k][l];
				double value = hm.get(zoneValue);
				outLayer.values[k][l] = value;
			}
		}
		//create the colour hashmap
		outLayer.initializeGreyscale();


		return outLayer;
	}

	protected Layer zonalVariety(Layer zoneLayer, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue);

		ArrayList<Integer> zone;
		ArrayList<Double> uniqueList = new ArrayList<Double>();
		double value;

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				zone = returnZone(i, j, zoneLayer);
				value = 0;
				uniqueList.clear();

				for (int k : zone) {
					value = this.values[k/this.nCols][k%this.nCols];

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
	
	// Return a zone, this method is only used by the method zonalVariety
	private ArrayList<Integer> returnZone(int x, int y, Layer zoneLayer) {

		ArrayList<Integer> returnArray = new ArrayList<Integer>();

		double zoneValue = zoneLayer.values[x][y];

		for (int i = 0; i<zoneLayer.nRows; i++){
			for (int j = 0; j<zoneLayer.nCols; j++){
				if (zoneLayer.values[i][j]==zoneValue){
					returnArray.add(i*zoneLayer.nCols+j);
				}
			}
		}
		return returnArray;
	}
}
