/*
==============================PAOA - Raster-based GIS==========================
Name        : Layer superclass
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Description : Stores the raster file's metadata and data. Has some all-purpose methods that are used for all sub-classes
===============================================================================
*/

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import java.lang.Math;

public class Layer {

	public String name;
	public int nRows;
	public int nCols;
	public double[] origin = new double[2];
	public double resolution;
	public double[][] values;
	public double nullValue;
	public double scale; // Store layer specific scale
	public HashMap<Double, Double[]> colorValueMap = new HashMap<Double, Double[]>(); // HashMap to store the color for each value

	// Layer constructor for reading layer files (.txt)
	public Layer(String layerName, String fileName) throws FileNotFoundException {

		File rFile = new File(fileName);
		FileReader fReader = new FileReader(rFile);
		BufferedReader bReader = new BufferedReader(fReader);

		String text;
		String lName = rFile.getName(); // Set layer name as the fileName
		this.name = lName.substring(0, lName.length() - 4);
		try {
			text = bReader.readLine(); // Column
			String[] cols = text.split(" ");
			this.nCols = Integer.parseInt(cols[cols.length - 1]);

			text = bReader.readLine(); // Rows
			String[] rows = text.split(" "); 
			this.nRows = Integer.parseInt(rows[rows.length - 1]);

			text = bReader.readLine(); // Origin x
			String[] xll = text.split(" ");
			this.origin[0] = Double.parseDouble(xll[xll.length - 1]);

			text = bReader.readLine(); // Origin y
			String[] yll = text.split(" ");
			this.origin[1] = Double.parseDouble(yll[yll.length - 1]);

			text = bReader.readLine(); // Resolution
			String[] res = text.split(" ");
			this.resolution = Double.parseDouble(res[res.length - 1]);

			text = bReader.readLine(); // No Data
			String[] nodata = text.split(" ");
			this.nullValue = Double.parseDouble(nodata[nodata.length - 1]);

			values = new double[nRows][nCols];
			// Assign values to every xy-coordinate pair in the raster image
			for (int i = 0; i < nRows; i++) {
				text = bReader.readLine(); // Read every row value
				String[] valueString = text.split(" ");
				for (int j = 0; j < nCols; j++) {
					// It takes a correct value from an array according to the column space		
					String value = valueString[j];

					// Format the string value into double -> accepts commas also
					NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
					Number number = format.parse(value);
					double valueDouble = number.doubleValue();

					// Assign a value for each coordinate of the raster values[i][j] = value;
					values[i][j] = valueDouble;
				}
			}

			bReader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		initializeGreyscale();
	}

	// Layer constructor for new layers
	public Layer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue) {
		this.name = outLayerName;
		this.nRows = nRows;
		this.nCols = nCols;
		this.origin = origin;
		this.resolution = resolution;
		this.values = new double[this.nRows][this.nCols];
		this.nullValue = nullValue;
		initializeGreyscale();
	}

	// Layer constructor for new layers with a given color Map
	public Layer(String outLayerName, int nRows, int nCols, double[] origin, double resolution, double nullValue, HashMap<Double,Double[]> colorValueMap) {
		this.name = outLayerName;
		this.nRows = nRows;
		this.nCols = nCols;
		this.origin = origin;
		this.resolution = resolution;
		this.values = new double[this.nRows][this.nCols];
		this.nullValue = nullValue;
		this.colorValueMap = colorValueMap;
	}
	
	//initialize the colorValueMap with Greyscale values
	protected void initializeGreyscale() {
		double min = getMin();
		double max = getMax();
		double valueDif = Math.abs(max - min);
		double dynRes = 255 / valueDif;
		ArrayList<Double> uniqueList = this.getUniqueList();

		for (int i = 0; i < uniqueList.size(); i++){	

			//if entry not in color map, put it there
			if (!this.colorValueMap.containsKey(uniqueList.get(i))){
				Double valueObj = uniqueList.get(i);
				Double[] color = new Double[3];
				double greyValue = (int) (dynRes * (valueObj - min));

				color[0] = greyValue;
				color[1] = greyValue;
				color[2] = greyValue;

				colorValueMap.put(valueObj, color);
			}
		}
	}

	// Save raster file with new specified name
	protected void save(String outputFileName) {
		File file = new File(outputFileName);
		FileWriter fWriter;

		try {
			fWriter = new FileWriter(file);

			fWriter.write("ncols " + nCols + "\r\n");
			fWriter.write("nrows " + nRows + "\r\n");
			fWriter.write("xllcorner " + origin[0] + "\r\n");
			fWriter.write("yllcorner " + origin[1] + "\r\n");
			fWriter.write("cellsize " + resolution + "\r\n");
			fWriter.write("NODATA_value " + nullValue + "\r\n");

			// Matrix
			for (int i = 0; i < nRows; i++) {
				for (int j = 0; j < nCols; j++) {
					fWriter.write(values[i][j] + " ");
					if (j == nCols - 1) {
						fWriter.write("\r\n");
					}
				}
			}

			fWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected BufferedImage mapSWTcolor(double scale, HashMap<Double, Double[]> colorHash) {
		BufferedImage image = new BufferedImage((int) ((int) this.nCols * scale) + 1, (int) ((int) this.nRows * scale) + 1, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();

		// Default color array
		int[] setColor = new int[3];

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				double value = this.values[i][j];
				for (Double key : colorHash.keySet()) {
					if (value == key) {
						Double[] colorArray = colorHash.get(key);
						setColor[0] = colorArray[0].intValue(); 
						setColor[1] = colorArray[1].intValue(); 
						setColor[2] = colorArray[2].intValue();

						for (int g = 0; g < scale; g++) {
							for (int z = 0; z < scale; z++) {
								int cols = (int) Math.floor(j * scale + g);
								int rows = (int) Math.floor(i * scale + z);
								raster.setPixel(cols, rows, setColor);
							}
						}

						break;
					} else {
						// Set default color as white
						setColor[0] = 255; setColor[1] = 255; setColor[2] = 255;

						for (int g = 0; g < scale; g++) {
							for (int z = 0; z < scale; z++) {
								int cols = (int) Math.floor(j * scale + g);
								int rows = (int) Math.floor(i * scale + z);
								raster.setPixel(cols, rows, setColor);
							}
						}
					}
				}
			} // End j
		} // End i

		return image;
	}

	// Get a max value from the raster image
	protected double getMax() {
		double max = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				double currentMax = this.values[i][j];
				if (currentMax > max) {
					max = this.values[i][j];
				}
			}
		}
		return max;
	}

	// Get the minimum value from the raster image
	protected double getMin() {
		double min = Double.POSITIVE_INFINITY;

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				double currentMin = this.values[i][j];
				if (currentMin < min) {
					min = this.values[i][j];
				}
			}
		}
		return min;
	}
	
	// Total Mean
	protected double getMean() {
		double mean = 0;
		double sum = 0;

		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				sum = sum + this.values[i][j];
			}
		}
		mean = sum / (this.nCols * this.nRows);

		return mean;	
	}
	
	// Get the total Variety of the raster image
	protected int getVariety() {
		int variety = this.getUniqueList().size();
		return variety;
	}

	// Get a list contaning every value that occurs in the layer
	protected ArrayList<Double> getUniqueList() {
		ArrayList<Double> uniqueList = new ArrayList<Double>();
		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				double value = this.values[i][j];
				if (uniqueList.contains(value)) {
					continue;
				} else {
					uniqueList.add(value);
				}
			}
		}
		return uniqueList;
	}

	// Get all values presented in the layer
	protected ArrayList<Double> getAllValues() {
		ArrayList<Double> allValues = new ArrayList<Double>();
		for (int i = 0; i < this.nRows; i++) {
			for (int j = 0; j < this.nCols; j++) {
				double value = this.values[i][j];
				allValues.add(value);
			}
		}
		return allValues;
	}
	
	// The following four methods below return neighborhoods and zones. The reason why different operations (map algebra)
	// use different neighborhood and zone methods is because they were written by different authors.
	
	// Return a neighborhood with a form of circle or square with desired size
	protected ArrayList<Double> returnNeighborhoodDouble(int x, int y, int n_radius, boolean form) {

		ArrayList<Double> collectArray = new ArrayList<Double>();
		ArrayList<Double> returnArray = new ArrayList<Double>();

		int neighborhoodRow = 0; 
		int neighborhoodCol = 0; 

		// Calculate the row and column amount in the new matrix and add values
		int countC = 0; int maxC = 0;
		for (int i = 0; i < 2 * n_radius + 1; i++) {
			countC = 0;
			for (int j = 0; j < 2 * n_radius + 1; j++) {

				// Values for accessing the cells inside the matrix
				int index_x = - n_radius + j; int index_y = - n_radius + i;
				// New x & y coordinates are indexed inside the new matrix
				int new_x = x + index_x; int new_y = y + index_y;

				if (form == true) {
					// if values are outside of the bounds go to next matrix cell
					if (new_x <= -1 || new_y <= -1 || new_x >= nRows || new_y >= nCols) {
						continue;
					} else {
						countC++;
						collectArray.add(values[new_x][new_y]);
					}
				} else {
					// if values are outside of the bounds go to next matrix cell
					if (new_x <= -1 || new_y <= -1 || new_x >= nRows || new_y >= nCols) {
						continue;
					} else {
						double distance = Math.sqrt(Math.pow(index_x, 2) + Math.pow(index_y, 2));
						if (distance <= (double) n_radius) {
							countC++;
							collectArray.add(values[new_x][new_y]);
						} else {
							continue;
						}
					}
				}

				if (countC == 1) {
					neighborhoodRow++;
				}

				if (maxC < countC) {
					neighborhoodCol = countC;
					maxC = countC;
				}

			}
		}

		// Iterate over the new neighborhood matrix and add the collected values
		int counter = -1;
		for (int row = 0; row < neighborhoodRow; row++) {
			for (int col = 0; col < neighborhoodCol; col++) {
				counter++;
				// If the counter goes over the colleArray size break the loop -> this is for the situation when there
				// is less values than the dimension allows to have
				if (counter >= collectArray.size()) {
					break;
				} else {
					double value = new Double(collectArray.get(counter));
					returnArray.add(value);	
				}
			} // end col
		} // end row

		return returnArray;
	}

	// This method returns the square or circular neighborhood of a cell. 
	// Used by localVariety, focalMean, zonalMean and zonalVariety
	protected ArrayList<int[]> returnNeighborhood (int i, int j, int r, boolean square){
		ArrayList<int[]> neighborhood = new ArrayList<int[]>();
		//top left corner of neighborhood

		int firstY = Math.max(0, i-r);
		int firstX = Math.max(0, j-r);

		//lower right corner of neighborhood
		int lastY = Math.min(nRows-1, i+r);
		int lastX = Math.min(nCols-1, j+r);

		if(square = true){			
			for (int k = firstY; k<=lastY; k++){ //k and g initially make up the coordinates of the top left corner of the neighborhood
				for (int g = firstX; g<=lastX; g++){
					int[] neighbor = new int[2];
					neighbor[0] = k;
					neighbor[1] = g;
					neighborhood.add(neighbor);
				}
			}
		}
		else if (square = false){
			for (int k = firstY; k<=lastY; k++){ //k and g initially make up the coordinates of the top left corner of the neighborhood
				for (int g = firstX; g<=lastX; g++){
					double deltax = i-k;
					double deltay = j-g;
					double distance = Math.pow(Math.pow(deltax,2)+Math.pow(deltay,2),0.5);
					if(distance <r){
						int[] neighbor = new int[2];
						neighbor[0] = k;
						neighbor[1] = g;
						neighborhood.add(neighbor);
					}
				}
			}
		}
		return neighborhood;
	}

	// Return a neighborhood of 3x3 cells centered on the input cell appropriated for
	// the slope calculation. In the case of some neighborhood-cells falling outside the raster - for input cells on the border or on the edges -
	// the current pixels (center cell) value is applied to these cells.
	// The slope, aspect, and hillshade operations use this method.
	protected ArrayList<Double> returnSlopeNeighborhood(int x, int y) {

		ArrayList<Double> returnArray = new ArrayList<Double>();
		returnArray = returnNeighborhoodDouble(x,y,1,true);

		if (x==0){
			if (y>0 && y<this.nCols-1){

				returnArray = returnNeighborhoodDouble(x+1,y,1,true);

				for (int q=0;q<3;q++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(2);
					returnArray.set(3*q, value);
				}

				for (int q=0; q<3;q++){
					for (int r=0; r<2; r++){
						double value = returnNeighborhoodDouble(x,y,1,true).get(2*q+r);
						returnArray.set(3*q+r+1, value);
					}

				}
			}
			else if(y==0){

				returnArray = returnNeighborhoodDouble(x+1,y+1,1,true);

				for  (int q_r=0;q_r<3;q_r++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(0);
					returnArray.set(q_r,value);
					returnArray.set(3*q_r,value);
				}

				for (int q=0; q<2; q++){
					for (int r=0; r<2; r++){
						double value = returnNeighborhoodDouble(x,y,1,true).get(2*q+r);
						returnArray.set(3*q+(r+4), value);
					}
				}
			}

			else{

				returnArray = returnNeighborhoodDouble(x+1,y-1,1,true);

				for (int q_r=0; q_r<3; q_r++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(2);
					returnArray.set(8-q_r, value);
					returnArray.set(3*q_r, value);
				}

				for (int q=0; q<2; q++){
					for (int r=0; r<2; r++){
						double value = returnNeighborhoodDouble(x,y,1,true).get(2*q+r);
						returnArray.set(3*q+(r+1), value);
					}
				}

			}
		}

		if (x==nRows-1){
			if (y>0 && y<this.nCols-1){

				returnArray = returnNeighborhoodDouble(x-1,y,1,true);

				for (int q=0;q<3;q++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(3);
					returnArray.set(3*q+2, value);
				}

				for (int q=0; q<3;q++){
					for (int r=0; r<2; r++){
						double value = returnNeighborhoodDouble(x,y,1,true).get(2*q+r);
						returnArray.set(3*q+r, value);
					}

				}
			}
			else if(y==0){

				returnArray = returnNeighborhoodDouble(x-1,y+1,1,true);

				for  (int q_r=0;q_r<3;q_r++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(1);
					returnArray.set(q_r,value);
					returnArray.set(3*q_r+2,value);
				}

				for (int q=0; q<2; q++){
					for (int r=0; r<2; r++){
						double value = returnNeighborhoodDouble(x,y,1,true).get(2*q+r);
						returnArray.set(3*(q+1)+r, value);
					}
				}
			}

			else{

				returnArray = returnNeighborhoodDouble(x-1,y-1,1,true);

				for (int q_r=0; q_r<3; q_r++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(3);
					returnArray.set(6+q_r, value);
					returnArray.set(3*q_r+2, value);
				}

				for (int q=0; q<2; q++){
					for (int r=0; r<2; r++){
						double value = returnNeighborhoodDouble(x,y,1,true).get(2*q+r);
						returnArray.set(3*q+r, value);
					}
				}

			}
		}

		if (y==0 && x>0 && x<nRows-1){

			returnArray = returnNeighborhoodDouble(x,y+1,1,true);

			for (int q=0; q<3; q++){
				double value = returnNeighborhoodDouble(x,y,1,true).get(1);
				returnArray.set(q, value);
			}

			for (int q=0; q<2; q++){
				for (int r=0; r<3; r++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(3*q+r);
					returnArray.set(3*(q+1)+r, value);
				}
			}
		}


		if (y==nCols-1 && x>0 && x<nRows-1){

			returnArray = returnNeighborhoodDouble(x,y-1,1,true);

			for (int r=0; r<3; r++){
				double value = returnNeighborhoodDouble(x,y,1,true).get(4);
				returnArray.set(r+6, value);
			}

			for (int q=0; q<2; q++){
				for (int r=0; r<3; r++){
					double value = returnNeighborhoodDouble(x,y,1,true).get(3*q+r);
					returnArray.set(3*q+r, value);
				}
			}
		}


		return returnArray;
	}

}
