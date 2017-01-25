/*
==============================PAOA - Raster-based GIS==========================
Name        : GraphicalLayer sub-class
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Description : Extends the Layer class with graphical operations (transformations)
===============================================================================
*/

import java.io.FileNotFoundException;
import java.util.HashMap;

public class GraphicalLayer extends Layer {

	public GraphicalLayer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue) {
		super(outLayerName, nRows, nCols, origin, resolution, nullValue);
	}
	
	public GraphicalLayer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue, double[][] values, HashMap<Double, Double[]> colorValueMap) {
		super(outLayerName, nRows, nCols, origin, resolution, nullValue);
		this.values = values;
		this.colorValueMap = colorValueMap;
	}
	
	public GraphicalLayer(String layerName, String fileName) throws FileNotFoundException {
		super(layerName, fileName);
	}

	// Mirroring
	protected Layer mirror_Yaxis(String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue, this.colorValueMap);

		for (int i=0; i<this.nRows;i++){
			for (int j=0; j<this.nCols;j++){
				outLayer.values[i][j] = this.values[i][this.nCols-(j+1)];
			}
		}
		return outLayer;
	}

	protected Layer mirror_Xaxis(String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue, this.colorValueMap);

		for (int i=0; i<this.nRows;i++){
			for (int j=0; j<this.nCols;j++){
				outLayer.values[i][j] = this.values[this.nRows-(i+1)][j];
			}
		}
		return outLayer;
	}

	protected Layer mirror_1bisec(String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue, this.colorValueMap);

		for (int i=0; i<this.nRows;i++){
			for (int j=0; j<this.nCols;j++){
				outLayer.values[i][j] = this.values[this.nRows-(j+1)][this.nCols-(i+1)];
			}
		}
		return outLayer;
	}

	protected Layer mirror_2bisec(String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue, this.colorValueMap);

		for (int i=0; i<this.nRows;i++){
			for (int j=0; j<this.nCols;j++){
				outLayer.values[i][j] = this.values[j][i];
			}
		}
		return outLayer;
	}

	// Rotating
	protected Layer rotate(double angle, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, this.nRows, this.nCols, this.origin, this.resolution, this.nullValue, this.colorValueMap);

		int centerj = ((int) this.nCols / 2); 
		int centeri = ((int) this.nRows / 2);
		int k=0;
		int l=0;
		int m=0;
		int n=0;
		double radians = Math.toRadians(angle),
				cos = Math.cos(radians), sin = Math.sin(radians);

		for (int i=0; i<this.nRows;i++){
			for (int j=0; j<this.nCols;j++){
				m = i - centeri;
				n = j - centerj;
				k = ((int) (m * cos + n * sin)) + centeri;
				l = ((int) (n * cos - m * sin)) + centerj;
				if (k >= 0 && k < this.nRows && l >= 0 && l < this.nCols){
					outLayer.values[i][j]=this.values[k][l];
				}
				else{
					outLayer.values[i][j]=this.nullValue;
				}
			}
		}
		return outLayer;
	}

	// Stretching
	protected Layer stretching(int x_scale, int y_scale, String outLayerName) {
		Layer outLayer = new Layer(outLayerName, (this.nRows) * y_scale, (this.nCols) * x_scale, this.origin, this.resolution, this.nullValue, this.colorValueMap);

		for (int i=0; i<this.nRows;i++){
			for (int j=0; j<this.nCols;j++){
				for (int k = 0; k < y_scale; k++) {
					for (int z = 0; z < x_scale; z++) {
						outLayer.values[i*y_scale + k][j*x_scale + z]=this.values[i][j];
					}
				}
			}
		}
		return outLayer;
	}
}
