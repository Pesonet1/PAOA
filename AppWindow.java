/*
==============================PAOA - Raster-based GIS==========================
Name        : AppWindow class
Authors     : Alexandre Barbusse, Anna Nordlöv, Oliver Stromann and Petteri Pesonen
Date:		: 2016-12-21
Version     : Final
Copyright   : Free to use for none-commercial and research purpose
Description : Constructs the GUI with all menus and contains the executable main method
===============================================================================
*/

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.swt.events.SelectionAdapter;	
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class AppWindow {

	protected Shell shell; // Main shell
	protected Shell layerInformation; // Layer infobox shell

	// This is used for storing each shell corresponding composite
	private HashMap<String, ScrolledComposite> storeShellComposite = new HashMap<String, ScrolledComposite>();
	private HashMap<String, LayerInformationWindow> storeShellLayerInformation = new HashMap<String, LayerInformationWindow>();

	private boolean continuous; // Boolean value for whether the layer values are continuous or not
	private boolean continuousToDis; // Continuous values to discrete or continuous'
	private boolean equalCustom; // Check what classify method is selected
	private int shellNameCount;

	// These are used in the toolboxes for storing the imported layer names
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Text inputLayerText;
	private Text zoneLayerText;

	// Launch the application.
	public static void main(String[] args) {
		try {
			AppWindow window = new AppWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	// Open the window.
	private void open() {
		Display display = Display.getDefault();
		createContents();

		shell.open();
		shell.layout();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	// Create contents of the main shell window.
	private void createContents() {
		shell = new Shell();
		shell.setMaximized(true);
		shell.setText("PAOA");

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		// FILE
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");

		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);

		MenuItem mntmOpen = new MenuItem(menu_1, SWT.NONE);
		mntmOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openShellLayer();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmOpen.setText("Open");

		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		mntmExit.setText("Exit");

		// MAP ALGEBRA
		MenuItem mntmMapAlgebra = new MenuItem(menu, SWT.CASCADE);
		mntmMapAlgebra.setText("Map Algebra");

		Menu menu_2 = new Menu(mntmMapAlgebra);
		mntmMapAlgebra.setMenu(menu_2);

		MenuItem mntmLocal = new MenuItem(menu_2, SWT.NONE);
		mntmLocal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				localContent();
			}
		});
		mntmLocal.setText("Local");

		MenuItem mntmFocal = new MenuItem(menu_2, SWT.NONE);
		mntmFocal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				focalContent();
			}
		});
		mntmFocal.setText("Focal");

		MenuItem mntmZonal = new MenuItem(menu_2, SWT.NONE);
		mntmZonal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zonalContent();
			}
		});
		mntmZonal.setText("Zonal");

		// RASTER OPERATIONS
		MenuItem mntmRasterOperations = new MenuItem(menu, SWT.CASCADE);
		mntmRasterOperations.setText("Raster Operations");
		
		Menu menu_3 = new Menu(mntmRasterOperations);
		mntmRasterOperations.setMenu(menu_3);

		MenuItem mntmHighPass = new MenuItem(menu_3, SWT.NONE);
		mntmHighPass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				highPassContent();
			}
		});
		mntmHighPass.setText("High Pass");

		MenuItem mntmSurface = new MenuItem(menu_3, SWT.NONE);
		mntmSurface.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				surfaceContent();
			}
		});
		mntmSurface.setText("Surface Tools");

		MenuItem mntmClassify = new MenuItem(menu_3, SWT.NONE);
		mntmClassify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				classifyContent();
			}
		});
		mntmClassify.setText("Classify");

		MenuItem mntmReclass = new MenuItem(menu_3, SWT.NONE);
		mntmReclass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reclassifyContent();
			}
		});
		mntmReclass.setText("Reclassify");

	}

	// Method for opening layers from text files
	private void openShellLayer() throws Exception {

		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.MULTI);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });
		
		if (fileDialogOpen.open() != null) {
			Layer[] layers = new Layer[fileDialogOpen.getFileNames().length];
			for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
				try {
					File filepath = new File(fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
					int fileKb = (int) (filepath.length() / 1024);
					// This file size seems to be the maximum that the application currently can handle
					if (fileKb > 16000) {
						System.out.println("Try smaller files (< 16mb");
						return;
					} else {
						layers[i] = new Layer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
						layers[i].scale = 2;
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
	
				// Check if the shell with the same name already exists -> rename if needed
				checkShellName(layers[i]);
				
				// Create new layer shell to the main shell and set the shells frame and content
				Shell layerShell = new Shell(shell, SWT.SHELL_TRIM);
				layerShellFrame(layerShell, layers[i]);
				layerShellContent(layerShell, layers[i]);
				
				// Add the mousewheel zoom listener to the layer zoom (scaling) functionality
				scrolledCompositeZoomListener(layerShell, layers[i]); 
			}
		} else {
			System.out.println("No file was selected");
		}
	}

	// Sets the layer shell frame
	private void layerShellFrame(Shell layerShell, Layer layer) {

		final FileDialog fileDialogSave = new FileDialog(shell, SWT.SAVE);
		fileDialogSave.setText("Save Layer");
		fileDialogSave.setFilterExtensions(new String[] { "*.txt" });
		fileDialogSave.setFilterNames(new String[] { "Text File(*.txt)" });
		fileDialogSave.setFilterPath("C:/Users/Pete/Documents/AG2411_Project"); // TODO Change this to current projects path?
		fileDialogSave.setFileName(layer.name + "_new");

		Menu layerMenu = new Menu(layerShell, SWT.BAR);
		layerShell.setMenuBar(layerMenu);

		// Save
		MenuItem mntmSaveLayer = new MenuItem(layerMenu, SWT.CASCADE);
		mntmSaveLayer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// THIS IS PARTIALLY COPIED CODE - http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/AfacadeforthesaveFileDialog.htm
				// 1) The user dismisses the dialog by pressing Cancel
				// 2) The selected file name does not exist
				// 3) The user agrees to overwrite existing file
				String fileName = null;
				boolean done = false;
				while (!done) {
					fileName = fileDialogSave.open();
					if (fileName == null) {
						done = true;
					} else {
						File file = new File(fileName);
						if (file.exists()) {
							MessageBox mb = new MessageBox(fileDialogSave.getParent(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
							mb.setMessage(fileName + " already exists. Do you want to replace it?");
							done = mb.open() == SWT.YES;
							layer.save(fileName);
						} else {
							layer.save(fileName);
							done = true;
						}
					}
				}
			}
		});
		mntmSaveLayer.setText("Save");

		// Increase scale
		MenuItem mntmIncreaseScale = new MenuItem(layerMenu, SWT.CASCADE);
		mntmIncreaseScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					layer.scale = layer.scale + 0.2;
					if (layer.scale <= 0.2) {
						layer.scale = 0.2;
						layerShellContent(layerShell, layer);
					} else {
						layerShellContent(layerShell, layer);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}	
		});
		mntmIncreaseScale.setText("+");

		// Decrease scale
		MenuItem mntmDecreaseScale = new MenuItem(layerMenu, SWT.CASCADE);
		mntmDecreaseScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					layer.scale = layer.scale - 0.2;
					if (layer.scale <= 0.2) {
						layer.scale = 0.2;
						System.out.println("Scale cannot be under 0.2!");
						layerShellContent(layerShell, layer);
					} else {
						layerShellContent(layerShell, layer);
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmDecreaseScale.setText("-");
		
		// Color
		MenuItem mntmColor = new MenuItem(layerMenu, SWT.CASCADE);
		mntmColor.setText("Color");
		
		Menu colorMenu = new Menu(mntmColor);
		mntmColor.setMenu(colorMenu);
		
		// Random color
		MenuItem mntmColorMapOn = new MenuItem(colorMenu, SWT.CASCADE);
		mntmColorMapOn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createRandomColors(layerShell, layer);
				
				updateLayerInformation(layerShell, layer);	
			}
		});
		mntmColorMapOn.setText("Random Colors");

		// Set colors
		MenuItem mntmColorToolbox = new MenuItem(colorMenu, SWT.CASCADE);
		mntmColorToolbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Assign the colors for each value in the raster layer currently displayed
				colorContent(layerShell, layer);
				
				updateLayerInformation(layerShell, layer);		
			}
		});
		mntmColorToolbox.setText("Assign colors");

		// Turn colors off -> to black & white
		MenuItem mntmColorMapOff = new MenuItem(colorMenu, SWT.CASCADE);
		mntmColorMapOff.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Reset the layer specific hashmap -> colors will not be stored anymore
				layer.colorValueMap.clear();
				layer.initializeGreyscale();
				try {
					layerShellContent(layerShell, layer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				updateLayerInformation(layerShell, layer);

			}
		});
		mntmColorMapOff.setText("Colors off");

		// Graphical transformations
		MenuItem mntmGraphTrans = new MenuItem(layerMenu, SWT.CASCADE);
		mntmGraphTrans.setText("Transformations");
		
		Menu transMenu = new Menu(mntmGraphTrans);
		mntmGraphTrans.setMenu(transMenu);
		
		// Mirror
		MenuItem mntmMirror = new MenuItem(transMenu, SWT.CASCADE);
		mntmMirror.setText("Mirror");
		
		Menu mirrorMenu = new Menu(mntmMirror);
		mntmMirror.setMenu(mirrorMenu);
		
		// Y-axis
		MenuItem mntmMirrorY = new MenuItem(mirrorMenu, SWT.CASCADE);
		mntmMirrorY.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer mirrorLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = mirrorLayer.mirror_Yaxis(mirrorLayer.name + "_mirrorY");
				outputLayer.scale = layer.scale;
				
				Shell layerMirrorShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
				
				try {
					layerShellFrame(layerMirrorShell, outputLayer);
					layerShellContent(layerMirrorShell, outputLayer);
					scrolledCompositeZoomListener(layerMirrorShell, outputLayer); 
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmMirrorY.setText("Y-axis");
		
		// X-axis
		MenuItem mntmMirrorX = new MenuItem(mirrorMenu, SWT.CASCADE);
		mntmMirrorX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer mirrorLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = mirrorLayer.mirror_Xaxis(mirrorLayer.name + "_mirrorX");
				outputLayer.scale = layer.scale;
				
				Shell layerMirrorShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);

				try {
					layerShellFrame(layerMirrorShell, outputLayer);
					layerShellContent(layerMirrorShell, outputLayer);
					scrolledCompositeZoomListener(layerMirrorShell, outputLayer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmMirrorX.setText("X-axis");
		
		// Diagonal (down-left - upper-right mirror)
		MenuItem mntmMirrorDiagLeft = new MenuItem(mirrorMenu, SWT.CASCADE);
		mntmMirrorDiagLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer mirrorLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = mirrorLayer.mirror_1bisec(mirrorLayer.name + "_diagLeft");
				outputLayer.scale = layer.scale;
				
				Shell layerMirrorShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
				
				try {
					layerShellFrame(layerMirrorShell, outputLayer);
					layerShellContent(layerMirrorShell, outputLayer);
					scrolledCompositeZoomListener(layerMirrorShell, outputLayer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmMirrorDiagLeft.setText("Diagonal Left");
		
		// Diagonal (down-right - upper-left mirror)
		MenuItem mntmMirrorDiagRight = new MenuItem(mirrorMenu, SWT.CASCADE);
		mntmMirrorDiagRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer mirrorLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = mirrorLayer.mirror_2bisec(mirrorLayer.name + "_diagRight");
				outputLayer.scale = layer.scale;
				
				Shell layerMirrorShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
				
				try {
					layerShellFrame(layerMirrorShell, outputLayer);
					layerShellContent(layerMirrorShell, outputLayer);
					scrolledCompositeZoomListener(layerMirrorShell, outputLayer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmMirrorDiagRight.setText("Diagonal Right");
		
		// Rotate 
		MenuItem mntmRotate = new MenuItem(transMenu, SWT.CASCADE);
		mntmRotate.setText("Rotate");
		
		Menu rotateMenu = new Menu(mntmRotate);
		mntmRotate.setMenu(rotateMenu);
		
		MenuItem mntmRotateClockwise = new MenuItem(rotateMenu, SWT.CASCADE);
		mntmRotateClockwise.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer rotateLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = rotateLayer.rotate(-90, layer.name + "_Clockwise90");
				outputLayer.scale = layer.scale;
				
				Shell layerRotateShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
				
				try {
					layerShellFrame(layerRotateShell, outputLayer);
					layerShellContent(layerRotateShell, outputLayer);
					scrolledCompositeZoomListener(layerRotateShell, outputLayer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmRotateClockwise.setText("Clockwise");
		
		MenuItem mntmRotateCounterwise = new MenuItem(rotateMenu, SWT.CASCADE);
		mntmRotateCounterwise.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer rotateLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = rotateLayer.rotate(90, layer.name + "_Counterwise90");
				outputLayer.scale = layer.scale;
		
				Shell layerRotateShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
				
				try {
					layerShellFrame(layerRotateShell, outputLayer);
					layerShellContent(layerRotateShell, outputLayer);
					scrolledCompositeZoomListener(layerRotateShell, outputLayer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		mntmRotateCounterwise.setText("Counterwise");
		
		MenuItem mntmRotateCustom = new MenuItem(rotateMenu, SWT.CASCADE);
		mntmRotateCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				rotateContent(layerShell, layer);
			}
		});
		mntmRotateCustom.setText("Custom");
		
		// Stretch
		MenuItem mntmStretching = new MenuItem(transMenu, SWT.CASCADE);
		mntmStretching.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stretchContent(layerShell, layer);
			}
		});
		mntmStretching.setText("Stretch");
		
		MenuItem mntmLayerInformation = new MenuItem(layerMenu, SWT.CALENDAR);
		mntmLayerInformation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				//check if shellKey exists in hashmap
				String shellKey = layerShell.getText();
				if(storeShellLayerInformation.containsKey(shellKey)){
					//if so, but layer information window is disposed, create a new one and overwrite in hashmap
					if(storeShellLayerInformation.get(shellKey).layerInformation.isDisposed()){
						LayerInformationWindow liw = new LayerInformationWindow(shell, layerShell,  layer);
						storeShellLayerInformation.put(shellKey, liw);	
					}
					//show layer information window
					storeShellLayerInformation.get(shellKey).layerInformation.setVisible(true);
				}				
			}			
		});
		mntmLayerInformation.setText("Info");
	}

	// Sets the image to the layer shell frame
	private void layerShellContent(Shell layerShell, Layer layer) throws Exception {
		int shellWidth = (int) ((int) layer.nCols * layer.scale + 1);
		int shellHeight = (int) ((int) layer.nRows * layer.scale + 1);

		layerShell.setLayout(new FillLayout());
		layerShell.setText(layer.name);
		layerShell.setMinimumSize(320, shellHeight + 65);
		layerShell.setSize(shellWidth + 20, shellHeight + 65);
		
		String shellKey = new String(layerShell.getText());
		ScrolledComposite sc = null;
		LayerInformationWindow liw = null;

		try {
			Image swtImage = null;

			// Retrieve layer specific colors
			HashMap<Double, Double[]> colorHash = layer.colorValueMap;
			swtImage = makeSWTImage(layerShell.getDisplay(), layer.mapSWTcolor(layer.scale, colorHash));

			// Create layer information window, if it does not exist and add it to hashmap -> otherwise get it
			if (storeShellLayerInformation.containsKey(shellKey) == false ){
				liw = new LayerInformationWindow(shell, layerShell,  layer);
				storeShellLayerInformation.put(shellKey, liw);				
			} else {
				liw = storeShellLayerInformation.get(shellKey);				
			}

			// Create scrolled composite if is does not exist and add it to hashmap -> otherwise get it
			if (storeShellComposite.containsKey(shellKey) == false) {
				sc = new ScrolledComposite(layerShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_BACKGROUND);
				storeShellComposite.put(shellKey, sc);
			} else {
				sc = storeShellComposite.get(shellKey);
			}

			// If the scrolled composite has been disposed, create a new one to the same key
			if (sc.isDisposed()) {
				storeShellComposite.remove(shellKey);
				sc = new ScrolledComposite(layerShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_BACKGROUND);
				storeShellComposite.put(shellKey, sc);
			}

			Label imgLabel = new Label(sc, SWT.NONE);

			// Dispose the old image
			Image oldLabel = imgLabel.getImage();
			if (oldLabel != null) { oldLabel.dispose();}

			imgLabel.setImage(swtImage);
			imgLabel.setSize(imgLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			sc.setContent(imgLabel);
			sc.setMinSize(shellWidth, shellHeight);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		DisposeListener dispListener = new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				//close Delete layer information window, if it does exists
				if (storeShellLayerInformation.containsKey(shellKey) == true){
					storeShellLayerInformation.get(shellKey).layerInformation.dispose();
					storeShellLayerInformation.remove(shellKey);	
				}
			}
		};
			
		layerShell.addDisposeListener(dispListener);
		layerShell.open();
	}

	// Creates the color setting tool frame and content
	private void colorContent(Shell layerShell, Layer layer) {
		Shell toolShell = new Shell(layerShell, SWT.SHELL_TRIM);
		toolShell.setText("Set colors");
		toolShell.setSize(600, 350);

		// Get unique values
		ArrayList<Double> uniqueList = layer.getUniqueList();
		double unique = uniqueList.size();
		continuous = false; // At default values are considered as discrete

		Label Label = new Label(toolShell, SWT.NONE);
		Label.setBounds(10, 10, 200, 22);
		Label.setText("Select values and assign a color");
		
		Table colorTable = new Table(toolShell, SWT.MULTI);
		colorTable.setHeaderVisible(true);
		colorTable.setBounds(10, 40, 100, 250);
		Font tFont = new Font(toolShell.getDisplay(), "Calibri", 10, SWT.BOLD);
		colorTable.setFont(tFont);
		
		TableColumn tc1 = new TableColumn(colorTable, SWT.CENTER);
		tc1.setText("Values");
		tc1.setWidth(100);

		// Listen the table and get the selected value/values as a variable
		ArrayList<Double> selectedVals = new ArrayList<Double>();
		colorTable.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				selectedVals.clear();
				TableItem[] selection = colorTable.getSelection();
				for (int i = 0; i < selection.length; i++) {
					try {
						double val = Double.parseDouble(selection[i].getText());
						selectedVals.add(val);
					} catch (NumberFormatException e2) {
						System.out.println("Number in wrong format");
					}
				}
			}
		});

		Button openColorDialogButton = new Button(toolShell, SWT.NONE);
		openColorDialogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// If the data is not continuous or the continuous values are wanted to be discrete
				if (continuous == false || continuousToDis == true) {
					int[] color = returnColorDlgVal(toolShell);
					
					// Assign the colors for the picked values from the table
					for (int i = 0; i < selectedVals.size(); i++) {
						Double valueObj = (double) selectedVals.get(i);
						Double[] rgbColorsObj = new Double[3];

						rgbColorsObj[0] = (double) color[0];
						rgbColorsObj[1] = (double) color[1];
						rgbColorsObj[2] = (double) color[2];

						Color color1 = new Color(toolShell.getDisplay(), color[0], color[1], color[2]);
						int val = selectedVals.get(i).intValue();
						colorTable.getItem(val).setForeground(color1);

						// Collect every value colors into one hashmap
						layer.colorValueMap.put(valueObj, rgbColorsObj);
					}
				}
			}
		});
		openColorDialogButton.setBounds(140, 85, 85, 25);
		formToolkit.adapt(openColorDialogButton, true, true);
		openColorDialogButton.setText("Assign Color");

		Button openLayerButton = new Button(toolShell, SWT.NONE);
		openLayerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Assign the colors to the existing frame
				try {
					layerShellContent(layerShell, layer);
					updateLayerInformation(layerShell, layer);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		openLayerButton.setBounds(140, 120, 85, 25);
		formToolkit.adapt(openLayerButton, true, true);
		openLayerButton.setText("Apply");

		// If there is more than 10 unique values, consider the dataset as continuous
		if (unique > 10) {
			continuous = true;

			MessageBox mb = new MessageBox((Shell) toolShell.getParent(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			mb.setMessage("Values seem to be continuous. Do you want them as discrete or not?");
			continuous = mb.open() == SWT.YES;

			// User wants to assign continuous values as discrete -> put each unique value into a table
			if (continuous == true) {
				continuousToDis = true;
				Collections.sort(uniqueList);
				for (int i = 0; i < unique; i++) {
					TableItem name = new TableItem(colorTable, SWT.NONE);
					name.setText(Double.toString(uniqueList.get(i)));
				}
		
			// Otherwise set the tool box so that only a color ramp can be picked from a combo
			} else {
				colorTable.setVisible(false);
				openColorDialogButton.setVisible(false);
				Label.setText("Select a color ramp");
				continuousToDis = false;
				continuous = true;
				
				Combo combo = new Combo(toolShell, SWT.NONE);
				combo.setBounds(10, 40, 160, 23);
				combo.setItems("Black to Red", "Red to Green", "Blue to Red", "Red to Blue", "Yellow to Blue", "Purple to Green", "Light Blue to Red");
				combo.setText("Select");
				combo.addModifyListener(new ModifyListener( ) {
					public void modifyText(ModifyEvent e) {
						createColorRamp(combo.getSelectionIndex(), uniqueList, layer);
					}
				});
				formToolkit.adapt(combo);
				formToolkit.paintBordersFor(combo);
				
				openLayerButton.setBounds(10, 100, 120, 25);
				openLayerButton.setText("Apply color ramp");
				toolShell.setSize(400, 200);
			}
		// If the values are not continuous set unique values into a table
		} else {
			Collections.sort(uniqueList);
			for (int i = 0; i < unique; i++) {
				TableItem name = new TableItem(colorTable, SWT.NONE);
				name.setText(Double.toString(uniqueList.get(i)));
			}
		}
		
		// If the layer is closed also close the color tool
		if (toolShell.getParent().isDisposed() == true) {
			toolShell.dispose();
		}

		toolShell.open();   
	}

	// Creates the local tool frame and content
	private void localContent() {
		Shell toolShell = new Shell(shell, SWT.SHELL_TRIM);
		toolShell.setText("Local functions");
		toolShell.setSize(600, 320);

		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.MULTI);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });

		ArrayList<LocalLayer> layerList = new ArrayList<LocalLayer>();

		Button openButton = new Button(toolShell, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layerList.clear();
				inputLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					LocalLayer[] layers = new LocalLayer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new LocalLayer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							LocalLayer layer = layers[i];
							layerList.add(layer);
							inputLayerText.append(layer.name + "\r\n");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton.setBounds(10, 10, 75, 25);
		formToolkit.adapt(openButton, true, true);
		openButton.setText("Add Layers");

		inputLayerText = new Text(toolShell, SWT.MULTI);
		inputLayerText.setBounds(10, 50, 160, 115);
		inputLayerText.setText("Select input layers");
		formToolkit.adapt(inputLayerText, true, true);

		Combo combo = new Combo(toolShell, SWT.NONE);
		combo.setBounds(10, 185, 160, 23);
		combo.setItems("localSum", "localMinimum", "localMaximum", "localMean", "localVariety");
		combo.setText("Select method");
		formToolkit.adapt(combo);
		formToolkit.paintBordersFor(combo);

		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Layer methodLayer = null;
				LocalLayer initLayer = null;
				String methodName = combo.getText();
				int method = combo.getSelectionIndex();
				
				if (layerList.size() < 1) {
					System.out.println("Layer wasn't selected");
					return;
				}
				
				for (int i = 0; i < layerList.size(); i++) {
					initLayer = layerList.get(i);
					switch (method) {
						case 0:
							methodLayer = initLayer.localSum("localSum", layerList);
							break;
						case 1:
							methodLayer = initLayer.localMinimum("localMinimum", layerList);
							break;
						case 2:
							methodLayer = initLayer.localMaximum("localMaximum", layerList);
							break;
						case 3:
							methodLayer = initLayer.localMean("localMean", layerList);
							break;
						case 4:
							methodLayer = initLayer.localVariety("localVariety", layerList);
							break;
						default:
							break;
					}	
				}

				StringBuilder sb = new StringBuilder();
				sb.append(methodName + "_");
				for (int i = 0; i < layerList.size(); i++) {
					if (i == layerList.size() - 1) {
						sb.append(layerList.get(i).name);
					} else {
						sb.append(layerList.get(i).name + "_");
					}
				}
				
				Shell layerShell = new Shell(shell, SWT.SHELL_TRIM);
				methodLayer.name = sb.toString();
				methodLayer.scale = 2;
				
				checkShellName(methodLayer);

				try {
					layerShellFrame(layerShell, methodLayer);
					layerShellContent(layerShell, methodLayer);
					scrolledCompositeZoomListener(layerShell, methodLayer); 
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		okButton.setBounds(10, 230, 75, 25);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");	

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setBounds(95, 230, 75, 25);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");

		toolShell.open();
	}

	// Creates the focal tool frame and content
	private void focalContent() {
		Shell toolShell = new Shell(shell, SWT.SHELL_TRIM);
		toolShell.setText("Focal functions");
		toolShell.setSize(600, 350);

		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.SINGLE);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });

		ArrayList<FocalLayer> layerList = new ArrayList<FocalLayer>();

		Button openButton = new Button(toolShell, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layerList.clear();
				inputLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					FocalLayer[] layers = new FocalLayer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new FocalLayer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							FocalLayer layer = layers[i];
							layerList.add(layer);
							inputLayerText.append(layer.name + "\r\n");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton.setBounds(10, 10, 75, 25);
		formToolkit.adapt(openButton, true, true);
		openButton.setText("Add Layer");

		inputLayerText = new Text(toolShell, SWT.MULTI);
		inputLayerText.setBounds(10, 50, 160, 30);
		inputLayerText.setText("Select input layer");
		formToolkit.adapt(inputLayerText, true, true);

		Group groupNeighborhood = new Group(toolShell, SWT.SHADOW_ETCHED_IN);
		groupNeighborhood.setLocation(10, 90);
		groupNeighborhood.setSize(160, 100);
		groupNeighborhood.setLayout(new FillLayout());
		groupNeighborhood.setText("Neighborhood");
		
		Button[] radios = new Button[2];

		radios[0] = new Button(groupNeighborhood, SWT.RADIO);
		radios[0].setBounds(10, 30, 70, 16);
		radios[0].setText("Square");
		radios[0].setSelection(true);
		formToolkit.adapt(radios[0], true, true);

		radios[1] = new Button(groupNeighborhood, SWT.RADIO);
		radios[1].setBounds(80, 30, 70, 16);
		radios[1].setText("Circle");
		formToolkit.adapt(radios[1], true, true);
		
		Label spinnerLabel = new Label(groupNeighborhood, SWT.NONE);
		spinnerLabel.setBounds(10, 67, 40, 22);
		spinnerLabel.setText("Radius");
		
		Spinner spinner = new Spinner(groupNeighborhood, SWT.BORDER);
		spinner.setBounds(60, 65, 60, 22);
		spinner.setMinimum(1);
		formToolkit.adapt(spinner);
		formToolkit.paintBordersFor(spinner);
		
		Combo combo = new Combo(toolShell, SWT.NONE);
		combo.setBounds(10, 210, 160, 23);
		combo.setItems("focalSum", "focalMinimum", "focalMaximum", "focalMean", "focalVariety");
		combo.setText("Select method");
		formToolkit.adapt(combo);
		formToolkit.paintBordersFor(combo);

		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Layer methodLayer = null; FocalLayer inputLayer = null; 
				String methodName = combo.getText();
				int method = combo.getSelectionIndex(); int n_radius = spinner.getSelection();
				boolean form = true; boolean square = radios[0].getSelection();
				if (square == true) { form = true; } else { form = false; }

				if (layerList.size() < 1) {
					System.out.println("Layer wasn't selected");
					return;
				}
				
				for (int i = 0; i < layerList.size(); i++) {
					inputLayer = layerList.get(i);
					String outLayerName = inputLayer.name;
					
					switch (method) {
						case 0:
							methodLayer = inputLayer.focalSum(n_radius, form, outLayerName);
							break;
						case 1:
							methodLayer = inputLayer.focalMinimum(n_radius, form, outLayerName);
							break;
						case 2:
							methodLayer = inputLayer.focalMaximum(n_radius, form, outLayerName);
							break;
						case 3:
							methodLayer = inputLayer.focalMean(n_radius, form, outLayerName);
							break;
						case 4:
							methodLayer = inputLayer.focalVariety(n_radius, form, outLayerName);
							break;
						default:
							break;
					}	
				}

				Shell layerShell = new Shell(shell, SWT.SHELL_TRIM);
				methodLayer.name = inputLayer.name + "_" + methodName;
				methodLayer.scale = 2;

				checkShellName(methodLayer);
				
				try {
					layerShellFrame(layerShell, methodLayer);
					layerShellContent(layerShell, methodLayer);
					scrolledCompositeZoomListener(layerShell, methodLayer); 
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		okButton.setBounds(10, 250, 75, 25);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setBounds(90, 250, 75, 25);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");

		toolShell.open();
	}

	// Creates the zonal tool frame and content
	private void zonalContent() {
		Shell toolShell = new Shell(shell, SWT.SHELL_TRIM);
		toolShell.setText("Zonal functions");
		toolShell.setSize(600, 350);

		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.SINGLE);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });

		ArrayList<ZonalLayer> layerList = new ArrayList<ZonalLayer>();

		Button openButton = new Button(toolShell, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layerList.clear();
				inputLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					ZonalLayer[] layers = new ZonalLayer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new ZonalLayer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							ZonalLayer layer = layers[i];
							layerList.add(layer);
							inputLayerText.append(layer.name + "\r\n");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton.setBounds(10, 10, 100, 25);
		formToolkit.adapt(openButton, true, true);
		openButton.setText("Add inputLayer");

		inputLayerText = new Text(toolShell, SWT.MULTI);
		inputLayerText.setBounds(10, 50, 160, 30);
		inputLayerText.setText("Select input layer");
		formToolkit.adapt(inputLayerText, true, true);

		ArrayList<Layer> zoneLayerList = new ArrayList<Layer>();

		Button openButton1 = new Button(toolShell, SWT.NONE);
		openButton1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoneLayerList.clear();
				zoneLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					Layer[] layers = new Layer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new Layer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							Layer layer = layers[i];
							zoneLayerList.add(layer);
							zoneLayerText.append(layer.name + "\r\n");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton1.setBounds(10, 95, 100, 25);
		formToolkit.adapt(openButton1, true, true);
		openButton1.setText("Add Zonelayer");

		zoneLayerText = new Text(toolShell, SWT.MULTI);
		zoneLayerText.setBounds(10, 135, 160, 30);
		zoneLayerText.setText("Select zonelayer");
		formToolkit.adapt(zoneLayerText, true, true);

		Combo combo = new Combo(toolShell, SWT.NONE);
		combo.setBounds(10, 190, 160, 23);
		combo.setItems("zonalSum", "zonalMinimum", "zonalMaximum", "zonalVariety");
		combo.setText("Select method");
		formToolkit.adapt(combo);
		formToolkit.paintBordersFor(combo);

		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Layer methodLayer = null; ZonalLayer inputLayer = null; Layer zoneLayer = null;
				String methodName = combo.getText();
				int method = combo.getSelectionIndex();

				if (layerList.size() < 1) {
					System.out.println("Layer wasn't selected");
					return;
				}
				
				for (int i = 0; i < layerList.size(); i++) {
					inputLayer = layerList.get(i);
					zoneLayer = zoneLayerList.get(i);	
					String outLayerName = inputLayer.name;

					switch (method) {
						case 0:
							methodLayer = inputLayer.zonalSum(zoneLayer, outLayerName);
							break;
						case 1:
							methodLayer = inputLayer.zonalMinimum(zoneLayer, outLayerName);
							break;
						case 2:
							methodLayer = inputLayer.zonalMaximum(zoneLayer, outLayerName);
							break;
						case 3:
							methodLayer = inputLayer.zonalVariety(zoneLayer, outLayerName);
							break;
						default:
							break;
					}	
				}

				Shell layerShell = new Shell(shell, SWT.SHELL_TRIM);
				methodLayer.name = inputLayer.name + "_" + zoneLayer.name + "_" + methodName;
				methodLayer.scale = 2;

				checkShellName(methodLayer);
				
				try {
					layerShellFrame(layerShell, methodLayer);
					layerShellContent(layerShell, methodLayer);
					scrolledCompositeZoomListener(layerShell, methodLayer); 
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		okButton.setBounds(10, 230, 75, 25);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");	

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setBounds(90, 230, 75, 25);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");

		toolShell.open();
	}
	
	// Creates the highpasss tool frame and content
	private void highPassContent() {
		Shell toolShell = new Shell(shell, SWT.SHELL_TRIM);
		toolShell.setText("High Pass");
		toolShell.setSize(500, 250);
		
		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.SINGLE);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });

		ArrayList<FocalLayer> layerList = new ArrayList<FocalLayer>();

		Button openButton = new Button(toolShell, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layerList.clear();
				inputLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					FocalLayer[] layers = new FocalLayer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new FocalLayer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							FocalLayer layer = layers[i];
							layerList.add(layer);
							inputLayerText.append(layer.name + "\r\n");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton.setBounds(10, 10, 75, 25);
		formToolkit.adapt(openButton, true, true);
		openButton.setText("Add Layer");
		
		inputLayerText = new Text(toolShell, SWT.MULTI);
		inputLayerText.setBounds(10, 50, 160, 30);
		formToolkit.adapt(inputLayerText, true, true);
		inputLayerText.setText("Add inputLayer");
		
		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Layer outputLayer = null;
				Layer inputLayer = null;
		
				if (layerList.size() < 1) {
					System.out.println("Layer wasn't selected");
					return;
				}
				
				for (int i = 0; i < layerList.size(); i++) {
					inputLayer = layerList.get(i);
					
					GeneralLayer highpassLayer = new GeneralLayer(inputLayer.name, inputLayer.nRows, inputLayer.nCols, inputLayer.origin, inputLayer.resolution, inputLayer.resolution, inputLayer.values);
					outputLayer = highpassLayer.highPass(inputLayer.name + "_highpass");
					outputLayer.scale = 2;
				}

				Shell layerHighpassShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
		
				try {
					layerShellFrame(layerHighpassShell, outputLayer);
					layerShellContent(layerHighpassShell, outputLayer);
					scrolledCompositeZoomListener(layerHighpassShell, outputLayer); 
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		okButton.setBounds(10, 100, 75, 25);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setBounds(90, 100, 75, 25);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");
		
		toolShell.open();
	}
	
	// Creates the surface tool frame and content
	private void surfaceContent() {
		Shell toolShell = new Shell(shell, SWT.SHELL_TRIM);
		toolShell.setText("Surface functions");
		toolShell.setSize(600, 300);
		
		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.SINGLE);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });

		ArrayList<GeneralLayer> layerList = new ArrayList<GeneralLayer>();

		Button openButton = new Button(toolShell, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layerList.clear();
				inputLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					GeneralLayer[] layers = new GeneralLayer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new GeneralLayer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							GeneralLayer layer = layers[i];
							layerList.add(layer);
							inputLayerText.append(layer.name + "\r\n");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton.setBounds(10, 10, 75, 25);
		formToolkit.adapt(openButton, true, true);
		openButton.setText("Add Layer");
		
		inputLayerText = new Text(toolShell, SWT.MULTI);
		inputLayerText.setBounds(10, 50, 160, 30);
		formToolkit.adapt(inputLayerText, true, true);
		inputLayerText.setText("Add inputlayer");
		
		Label labelAlt = new Label(toolShell, SWT.NONE);
		labelAlt.setBounds(10, 132, 50, 22);
		labelAlt.setText("Altitude");
		labelAlt.setVisible(false);
		
		Spinner spinnerAlt = new Spinner(toolShell, SWT.BORDER);
		spinnerAlt.setBounds(80, 130, 60, 22);
		spinnerAlt.setMinimum(1);
		spinnerAlt.setMaximum(90);
		spinnerAlt.setSelection(45);
		spinnerAlt.setVisible(false);
		formToolkit.adapt(spinnerAlt);
		formToolkit.paintBordersFor(spinnerAlt);

		Label labelAz = new Label(toolShell, SWT.NONE);
		labelAz.setBounds(10, 162, 50, 22);
		labelAz.setText("Azimuth");
		labelAz.setVisible(false);
		
		Spinner spinnerAz = new Spinner(toolShell, SWT.BORDER);
		spinnerAz.setBounds(80, 160, 60, 22);
		spinnerAz.setMinimum(1);
		spinnerAz.setMaximum(360);
		spinnerAz.setSelection(315);
		spinnerAz.setVisible(false);
		formToolkit.adapt(spinnerAz);
		formToolkit.paintBordersFor(spinnerAz);
		
		Combo combo = new Combo(toolShell, SWT.NONE);
		
		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Layer methodLayer = null; GeneralLayer inputLayer = null;
				int method = combo.getSelectionIndex();
				
				if (layerList.size() < 1) {
					System.out.println("Layer wasn't selected");
					return;
				}
				
				for (int i = 0; i < layerList.size(); i++) {
					inputLayer = layerList.get(i);
					String outLayerName = inputLayer.name + "_" + combo.getText();
				
					switch (method) {
						case 0:
							methodLayer = inputLayer.slope(outLayerName);
							break;
						case 1:
							methodLayer = inputLayer.aspect(outLayerName);
							break;
						case 2:
							methodLayer = inputLayer.hillshade(outLayerName, spinnerAlt.getSelection(), spinnerAz.getSelection());
							break;
						default:
							break;
					}	
				}
				
				Shell layerSurfaceShell = new Shell(shell, SWT.SHELL_TRIM);
				methodLayer.scale = 2;
				checkShellName(methodLayer);
				
				try {
					layerShellFrame(layerSurfaceShell, methodLayer);
					layerShellContent(layerSurfaceShell, methodLayer);
					scrolledCompositeZoomListener(layerSurfaceShell, methodLayer); 
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		okButton.setVisible(false);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setVisible(false);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");
		
		
		combo.setBounds(10, 90, 160, 23);
		combo.setItems("Slope", "Aspect", "Hillshade");
		combo.setText("Select method");
		combo.addModifyListener(new ModifyListener( ) {
			public void modifyText(ModifyEvent e) {
				// If hillshade selected -> set visible to altitude and azimuth degrees
				if (combo.getSelectionIndex() == 2) {
					labelAlt.setVisible(true);
					spinnerAlt.setVisible(true);
					labelAz.setVisible(true);
					spinnerAz.setVisible(true);
					okButton.setVisible(true);
					okButton.setBounds(10, 200, 75, 25);
					cancelButton.setVisible(true);
					cancelButton.setBounds(90, 200, 75, 25);
					toolShell.setSize(600, 300);
				} else {
					labelAlt.setVisible(false);
					spinnerAlt.setVisible(false);
					labelAz.setVisible(false);
					spinnerAz.setVisible(false);
					okButton.setVisible(true);
					okButton.setBounds(10, 130, 75, 25);
					cancelButton.setVisible(true);
					cancelButton.setBounds(90, 130, 75, 25);
					toolShell.setSize(600, 240);
				}
			}
		});
		formToolkit.adapt(combo);
		formToolkit.paintBordersFor(combo);
		
		toolShell.open();
	}
	
	// TODO Algorithms need improvements
	private void classifyContent() {
		Shell toolShell = new Shell(shell, SWT.SHELL_TRIM);
		toolShell.setText("Classify functions");
		toolShell.setSize(600, 420);
	
		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.SINGLE);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });

		ArrayList<GeneralLayer> layerList = new ArrayList<GeneralLayer>();
		
		Button openButton = new Button(toolShell, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layerList.clear();
				inputLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					GeneralLayer[] layers = new GeneralLayer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new GeneralLayer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							GeneralLayer layer = layers[i];
							layerList.add(layer);
							inputLayerText.append(layer.name + "\r\n");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton.setBounds(10, 10, 75, 25);
		formToolkit.adapt(openButton, true, true);
		openButton.setText("Add Layer");
		
		inputLayerText = new Text(toolShell, SWT.MULTI);
		inputLayerText.setBounds(10, 50, 185, 30);
		formToolkit.adapt(inputLayerText, true, true);
		inputLayerText.setText("Add inputLayer");
		
		Label labelClassAmount = new Label(toolShell, SWT.NONE);
		labelClassAmount.setBounds(10, 142, 40, 22);
		labelClassAmount.setVisible(false);
		labelClassAmount.setText("Classes");
		
		Spinner spinnerClassAmount = new Spinner(toolShell, SWT.BORDER);
		spinnerClassAmount.setBounds(65, 140, 70, 22);
		spinnerClassAmount.setMinimum(1);
		spinnerClassAmount.setIncrement(1);
		spinnerClassAmount.setVisible(false);
		formToolkit.adapt(spinnerClassAmount);
		formToolkit.paintBordersFor(spinnerClassAmount);
		
		Table rTable = new Table(toolShell, SWT.SINGLE);
		rTable.setHeaderVisible(true);
		
		TableColumn tc1 = new TableColumn(rTable, SWT.CENTER);
		tc1.setText("Min");
		tc1.setWidth(50);
		TableColumn tc2 = new TableColumn(rTable, SWT.CENTER);
		tc2.setText("Max");
		tc2.setWidth(50);
		TableColumn tc3 = new TableColumn(rTable, SWT.CENTER);
		tc3.setText("Value");
		tc3.setWidth(85);
		
		rTable.setBounds(10, 180, 185, 130);
		rTable.setVisible(false);
		
		Label labelRangeAmount = new Label(toolShell, SWT.NONE);
		labelRangeAmount.setBounds(10, 142, 40, 22);
		labelRangeAmount.setVisible(false);
		labelRangeAmount.setText("Ranges");
		
		Spinner spinnerRangeAmount = new Spinner(toolShell, SWT.BORDER);
		spinnerRangeAmount.setBounds(65, 140, 70, 22);
		spinnerRangeAmount.setMinimum(1);
		spinnerRangeAmount.setIncrement(1);
		spinnerRangeAmount.setVisible(false);
		TableItem rangeVal = new TableItem(rTable, SWT.NONE);
		rangeVal.setText(new String[] { "Min 1", "Max 1", "Value 1"});
		spinnerRangeAmount.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				rTable.removeAll();
				for (int i = 0; i < spinnerRangeAmount.getSelection(); i++) {
					TableItem rangeVal = new TableItem(rTable, SWT.NONE);
					rangeVal.setText(new String[] {"Min " + (i + 1), "Max " + (i + 1), "Value " + (i + 1)});
				}
			}
		});
		formToolkit.adapt(spinnerRangeAmount);
		formToolkit.paintBordersFor(spinnerRangeAmount);
		
		final TableCursor cursor = new TableCursor(rTable, SWT.NONE);
	    final ControlEditor editor = new ControlEditor(cursor);
	    editor.grabHorizontal = true;
	    editor.grabVertical = true;

		// PARTIALLY COPIED CODE -> http://www.java2s.com/Tutorial/Java/0280__SWT/AddselectionlistenertoTableCursor.htm
	    cursor.addSelectionListener(new SelectionAdapter() {   	
	    	// This is called when the user hits Enter
	    	public void widgetDefaultSelected(SelectionEvent event) {
	    		final Text text = new Text(cursor, SWT.NONE);
	    		text.setFocus();
	    		// Add a handler to detect key presses
	    		text.addKeyListener(new KeyAdapter() {
	    			public void keyPressed(KeyEvent event) {
	    				switch (event.keyCode) {
		    				case SWT.CR:
		    					cursor.getRow().setText(cursor.getColumn(), text.getText());
		    				case SWT.ESC:
		    					text.dispose();
		    					break;
		    			}
	    			}
	    		});
	    		editor.setEditor(text);
	    	}
	    });
	    
		Button applyClassesButton = new Button(toolShell, SWT.NONE);
		applyClassesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Layer methodLayer = null; GeneralLayer inputLayer = null;
				
				int method = 0;
				if (equalCustom == false) { method = 0; } else { method = 1; }
				
				if (layerList.size() < 1) {
					System.out.println("Layer wasn't selected");
					return;
				}
				
				for (int i = 0; i < layerList.size(); i++) {
					inputLayer = layerList.get(i);
					String outLayerName = inputLayer.name + "_class";
				
					switch (method) {
						case 0:
							methodLayer = inputLayer.ClassifyEQI(outLayerName, spinnerClassAmount.getSelection());
							break;
						case 1:
							HashMap<Double[], Double> classMap = new HashMap<Double[], Double>();
							TableItem[] tableItems = rTable.getItems();

							for (int k = 0; k < tableItems.length; k++) {
								Double[] minmax = new Double[2];
								minmax[0] = null; minmax[1] = null;
								Double val = null;
								
								// If there is a new value added add this to the hashmap
								if (tableItems[k].getText(0) != null || tableItems[k].getText(1) != null || tableItems[k].getText(2) != null) {
									// Try whether the values are double or integer else dont add anything to hashmap
									try {
										minmax[0] = Double.parseDouble(tableItems[k].getText(0));
										minmax[1] = Double.parseDouble(tableItems[k].getText(1));
										val = Double.parseDouble(tableItems[k].getText(2));
									} catch (NumberFormatException e1) {
										continue;
									}
									
									if (minmax[0] != null || minmax[1] != null) {
										classMap.put(minmax, val);
									}
									
								// If some value is null don't assign any values to them
								} else {
									continue;
								}
							}
							
							methodLayer = inputLayer.ClassifyCustom(outLayerName, classMap);
							break;
					}
				}
					
				Shell layerClassShell = new Shell(shell, SWT.SHELL_TRIM);
				methodLayer.scale = 2;
				checkShellName(methodLayer);

				try {
					layerShellFrame(layerClassShell, methodLayer);
					layerShellContent(layerClassShell, methodLayer);
					scrolledCompositeZoomListener(layerClassShell, methodLayer); 
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		formToolkit.adapt(applyClassesButton, true, true);
		applyClassesButton.setVisible(false);
		applyClassesButton.setText("Apply");
		
		// Create button for that enables equal classes classification
		Button equalClassButton = new Button(toolShell, SWT.NONE);
		equalClassButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Show the selection for the number of classes (spinner)
				labelClassAmount.setVisible(true);
				labelRangeAmount.setVisible(false);
				spinnerClassAmount.setVisible(true);
				spinnerRangeAmount.setVisible(false);
				
				rTable.setVisible(false);
				applyClassesButton.setVisible(true);
				applyClassesButton.setBounds(10, 180, 75, 25);
				toolShell.setSize(600, 260);
				
				equalCustom = false;
			}
		});
		equalClassButton.setBounds(10, 100, 80, 25);
		formToolkit.adapt(equalClassButton, true, true);
		equalClassButton.setText("Equal classes");
		
		// Create button that enables custom classes classification
		Button customClassButton = new Button(toolShell, SWT.NONE);
		customClassButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				equalCustom = true;
				labelClassAmount.setVisible(false);
				labelRangeAmount.setVisible(true);
				spinnerClassAmount.setVisible(false);
				spinnerRangeAmount.setVisible(true);
				
				rTable.setVisible(true);
				applyClassesButton.setVisible(true);
				applyClassesButton.setBounds(10, 330, 75, 25);
				toolShell.setSize(600, 420);
			
				equalCustom = true;
			}
		});
		customClassButton.setBounds(100, 100, 95, 25);
		formToolkit.adapt(customClassButton, true, true);
		customClassButton.setText("Custom classes");
		
		toolShell.open();
	}
	
	// Creates the reclassify tool frame and content
	private void reclassifyContent() {
		Shell toolShell = new Shell(shell, SWT.SHELL_TRIM);
		toolShell.setText("Reclassify functions");
		toolShell.setSize(600, 400);

		Table rTable = new Table(toolShell, SWT.SINGLE);
		rTable.setHeaderVisible(true);
		
		TableColumn tc1 = new TableColumn(rTable, SWT.CENTER);
		tc1.setText("Old Values");
		tc1.setWidth(100);
		TableColumn tc2 = new TableColumn(rTable, SWT.CENTER);
		tc2.setText("New Values");
		tc2.setWidth(100);
		
		rTable.setBounds(10, 100, 200, 200);

		final TableEditor editor = new TableEditor(rTable);
		
		// PARTIALLY COPIED CODE -> http://www.java2s.com/Tutorial/Java/0280__SWT/Editthetextofatableiteminplace.htm
		final int editCol = 1;
		rTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) { oldEditor.dispose(); }
				
				// Identify the selected row
				TableItem item = (TableItem) e.item;
				if (item == null) { return; }

				// The control that will be the editor must be a child of the Table
				Text newEditor = new Text(rTable, SWT.NONE);
				newEditor.setText(item.getText(editCol));
				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent me) {
						Text text = (Text) editor.getEditor();
						editor.getItem().setText(editCol, text.getText());
					}
				});
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, editCol);
			}
		});
		
		final FileDialog fileDialogOpen = new FileDialog(shell, SWT.SINGLE);
		fileDialogOpen.setText("Add Data");
		fileDialogOpen.setFilterExtensions(new String[] { "*.txt" });
		fileDialogOpen.setFilterNames(new String[] { "Text File(*.txt)" });

		ArrayList<GeneralLayer> layerList = new ArrayList<GeneralLayer>();
		
		Button openButton = new Button(toolShell, SWT.NONE);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layerList.clear();
				rTable.removeAll();
				inputLayerText.setText("");
				if (fileDialogOpen.open() != null) {
					GeneralLayer[] layers = new GeneralLayer[fileDialogOpen.getFileNames().length];
					for (int i = 0; i < fileDialogOpen.getFileNames().length; i++) {
						try {
							layers[i] = new GeneralLayer(String.valueOf(i), fileDialogOpen.getFilterPath() + "\\" + fileDialogOpen.getFileNames()[i]);
							GeneralLayer layer = layers[i];
							layerList.add(layer);
							inputLayerText.append(layer.name + "\r\n");
							
							ArrayList<Double> uniqueList = new ArrayList<Double>();
							uniqueList = layer.getUniqueList();
							
							Collections.sort(uniqueList);
							for (int k = 0; k < uniqueList.size(); k++) {
								TableItem old_value = new TableItem(rTable, SWT.NONE);
								old_value.setText(Double.toString(uniqueList.get(k)));
							}
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("No file was selected");
				}
			}
		});
		openButton.setBounds(10, 10, 75, 25);
		formToolkit.adapt(openButton, true, true);
		openButton.setText("Add Layer");
		
		inputLayerText = new Text(toolShell, SWT.MULTI);
		inputLayerText.setBounds(10, 50, 200, 30);
		formToolkit.adapt(inputLayerText, true, true);
		inputLayerText.setText("Add inputLayer");
		
		// Take the values from the table and fill the hashmap
		// if the values have not been changed keep the original one
		HashMap<Double, Double> classMap = new HashMap<Double, Double>();
		
		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Here are every item also with empty strings
				TableItem[] tableItems = rTable.getItems();
				ArrayList<Double> uniqueList = new ArrayList<Double>();
				Layer layer = layerList.get(0);
				uniqueList = layer.getUniqueList();
				Collections.sort(uniqueList);
				for (int i = 0; i < tableItems.length; i++) {
					// If there is a new value added add this to the hashmap
					if (tableItems[i].getText(1) != null) {
						// Check if the added value is a double or integer -> otherwise assign the old value
						try {
							Double val = Double.parseDouble(tableItems[i].getText(1));
							classMap.put(uniqueList.get(i), val);
						} catch (NumberFormatException e1) {
							classMap.put(uniqueList.get(i), uniqueList.get(i));
						}
					// If no int or double values has been added to the table keep the original value
					} else {
						classMap.put(uniqueList.get(i), uniqueList.get(i));
					}
				}
		
				// Create the layer based on the reclassed hashmap values
				Layer methodLayer = null; GeneralLayer inputLayer = null;
			
				if (layerList.size() < 1) {
					System.out.println("Layer wasn't selected");
					return;
				}
				
				for (int i = 0; i < layerList.size(); i++) {
					inputLayer = layerList.get(i);
					String outLayerName = inputLayer.name + "_reclass";
					methodLayer = inputLayer.Reclass(outLayerName, classMap);
				}
				
				Shell layerShell = new Shell(shell, SWT.SHELL_TRIM);
				methodLayer.scale = 2;
				checkShellName(methodLayer);
				
				try {
					layerShellFrame(layerShell, methodLayer);
					layerShellContent(layerShell, methodLayer);
					scrolledCompositeZoomListener(layerShell, methodLayer); 
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
		});
		okButton.setBounds(10, 320, 75, 25);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setBounds(90, 320, 75, 25);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");
		
		toolShell.open();
	}
	
	// Creates the rotation degree selection tool frame and content
	private void rotateContent(Shell layerShell, Layer layer) {
		Shell toolShell = new Shell(layerShell);
		toolShell.setText("Set rotation degree");
		toolShell.setSize(300, 150);
		
		Label labelDegree = new Label(toolShell, SWT.NONE);
		labelDegree.setBounds(10, 12, 20, 22);
		labelDegree.setText("Degree");
		
		Spinner spinnerDegree = new Spinner(toolShell, SWT.BORDER);
		spinnerDegree.setBounds(45, 10, 70, 22);
		spinnerDegree.setMaximum(360);
		spinnerDegree.setIncrement(10);
		formToolkit.adapt(spinnerDegree);
		formToolkit.paintBordersFor(spinnerDegree);
		
		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer rotateLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = rotateLayer.rotate(spinnerDegree.getSelection(), layer.name + "_Counterwise" + Integer.toString(spinnerDegree.getSelection()));
				outputLayer.scale = layer.scale;
				
				Shell layerRotateShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
				
				try {
					layerShellFrame(layerRotateShell, outputLayer);
					layerShellContent(layerRotateShell, outputLayer);
					scrolledCompositeZoomListener(layerRotateShell, outputLayer); 
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		okButton.setBounds(10, 70, 75, 25);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setBounds(90, 70, 75, 25);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");
		
		toolShell.open();
	}
	
	// Creates the stretch dimension selection tool frame and content
	private void stretchContent(Shell layerShell, Layer layer) {
		Shell toolShell = new Shell(layerShell);
		toolShell.setText("Set stretch scales");
		toolShell.setSize(300, 150);
	
		Label labelX = new Label(toolShell, SWT.NONE);
		labelX.setBounds(10, 12, 10, 22);
		labelX.setText("X");
		
		Spinner spinnerX = new Spinner(toolShell, SWT.BORDER);
		spinnerX.setBounds(25, 10, 40, 22);
		spinnerX.setMinimum(1);
		formToolkit.adapt(spinnerX);
		formToolkit.paintBordersFor(spinnerX);

		Label labelY = new Label(toolShell, SWT.NONE);
		labelY.setBounds(80, 12, 10, 22);
		labelY.setText("Y");
		
		Spinner spinnerY = new Spinner(toolShell, SWT.BORDER);
		spinnerY.setBounds(95, 10, 40, 22);
		spinnerY.setMinimum(1);
		formToolkit.adapt(spinnerY);
		formToolkit.paintBordersFor(spinnerY);
		
		Button okButton = new Button(toolShell, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphicalLayer stretchLayer = new GraphicalLayer(layer.name, layer.nRows, layer.nCols, layer.origin, layer.resolution, layer.resolution, layer.values, layer.colorValueMap);
				Layer outputLayer = stretchLayer.stretching(spinnerX.getSelection(), spinnerY.getSelection(), layer.name + "_stretch");
				outputLayer.scale = 2;
				
				Shell layerStretchShell = new Shell(shell, SWT.SHELL_TRIM);
				checkShellName(outputLayer);
				
				try {
					layerShellFrame(layerStretchShell, outputLayer);
					layerShellContent(layerStretchShell, outputLayer);
					scrolledCompositeZoomListener(layerStretchShell, outputLayer); 
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		okButton.setBounds(10, 70, 75, 25);
		formToolkit.adapt(okButton, true, true);
		okButton.setText("OK");

		Button cancelButton = new Button(toolShell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toolShell.close();
			}
		});
		cancelButton.setBounds(90, 70, 75, 25);
		formToolkit.adapt(cancelButton, true, true);
		cancelButton.setText("Cancel");
		
		toolShell.open();
	}
	
	// Creates random colors for the layer. Checks whether the data is discrete or continuous
	private void createRandomColors(Shell layerShell, Layer layer) {
		Random rand = new Random();
		continuous = false;

		// The amount of unique values and amount of all values in the image;
		ArrayList<Double> uniqueList = layer.getUniqueList();

		// Check if the share of unique values is larger than one percent -> data assumably is continuous
		double unique = uniqueList.size();
		if (unique > 10) { continuous = true; }

		double min = layer.getMin(); double max = layer.getMax();
		double valueDif = Math.abs(max - min);
		double dynRes = 255 / valueDif;

		if (continuous == true) {
			// Create a default color ramp for continuous data
			for (int value = 0; value < unique; value++) {
				Double[] rgbColorsObj = new Double[3];
				Double valueObj = (double) uniqueList.get(value);
				double colorValue = (dynRes * (valueObj - min));

				// Changing colors -> "continuous"
				rgbColorsObj[0] = colorValue * rand.nextDouble();
				rgbColorsObj[1] = colorValue * rand.nextDouble();
				rgbColorsObj[2] = colorValue * rand.nextDouble();

				// Collect every value colors into one hashmap
				layer.colorValueMap.put(valueObj, rgbColorsObj);
			}
		} else {
			// Create a random colors for each value in the image
			for (int value = 0; value < unique; value++) {
				Double[] rgbColorsObj = new Double[3];
				Double valueObj = (double) uniqueList.get(value);

				// Assign colors randomly
				for (int i = 0; i < 3; i++) { rgbColorsObj[i] = (double) rand.nextInt(255); }

				// Collect every value colors into one hashmap
				layer.colorValueMap.put(valueObj, rgbColorsObj);
			}
		}

		try {
			layerShellContent(layerShell, layer);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	// Creates prefixed color ramps
	private void createColorRamp(int comboVal, ArrayList<Double> uniqueList, Layer layer) {
		double variety = layer.getVariety();
		double min = layer.getMin();
		double max = layer.getMax();
		double valueDif = Math.abs(max - min);
		double dynRes = 255 / valueDif;
		Collections.sort(uniqueList);
		
		switch (comboVal) {
			case 0:
				for (int value = 0; value < variety; value++) {
					Double valueObj = (double) uniqueList.get(value);
					Double[] rgbColorsObj = new Double[3];
					double colorValue = (dynRes * (valueObj - min));
			
					rgbColorsObj[0] = colorValue;
					rgbColorsObj[1] = (double) 0;
					rgbColorsObj[2] = (double) 0;
					
					layer.colorValueMap.put(valueObj, rgbColorsObj);
				}
				break;
			case 1:
				for (int value = 0; value < variety; value++) {
					Double valueObj = (double) uniqueList.get(value);
					Double[] rgbColorsObj = new Double[3];
					double colorValue = (dynRes * (valueObj - min));
			
					rgbColorsObj[0] = (double) Math.abs(255 - colorValue);
					rgbColorsObj[1] = colorValue;
					rgbColorsObj[2] = (double) 0;
					
					layer.colorValueMap.put(valueObj, rgbColorsObj);
				}
				break;
			case 2:
				for (int value = 0; value < variety; value++) {
					Double valueObj = (double) uniqueList.get(value);
					Double[] rgbColorsObj = new Double[3];
					double colorValue = (dynRes * (valueObj - min));
			
					rgbColorsObj[0] = colorValue;
					rgbColorsObj[1] = (double) 0;
					rgbColorsObj[2] = (double) Math.abs(255 - colorValue);
					
					layer.colorValueMap.put(valueObj, rgbColorsObj);
				}
				break;
			case 3:
				for (int value = 0; value < variety; value++) {
					Double valueObj = (double) uniqueList.get(value);
					Double[] rgbColorsObj = new Double[3];
					double colorValue = (dynRes * (valueObj - min));
			
					rgbColorsObj[0] = (double) Math.abs(255 - colorValue);
					rgbColorsObj[1] = (double) 0;
					rgbColorsObj[2] = colorValue;
					
					layer.colorValueMap.put(valueObj, rgbColorsObj);
				}
				break;
			case 4:
				for (int value = 0; value < variety; value++) {
					Double valueObj = (double) uniqueList.get(value);
					Double[] rgbColorsObj = new Double[3];
					double colorValue = (dynRes * (valueObj - min));
			
					rgbColorsObj[0] = (double) Math.abs(255 - colorValue);
					rgbColorsObj[1] = (double) Math.abs(255 - colorValue);
					rgbColorsObj[2] = colorValue;
					
					layer.colorValueMap.put(valueObj, rgbColorsObj);
				}
				break;
			case 5:
				for (int value = 0; value < variety; value++) {
					Double valueObj = (double) uniqueList.get(value);
					Double[] rgbColorsObj = new Double[3];
					double colorValue = (dynRes * (valueObj - min));
			
					rgbColorsObj[0] = (double) Math.abs(255 - colorValue);
					rgbColorsObj[1] = colorValue;
					rgbColorsObj[2] = (double) Math.abs(255 - colorValue);
					
					layer.colorValueMap.put(valueObj, rgbColorsObj);
				}
				break;
			case 6:
				for (int value = 0; value < variety; value++) {
					Double valueObj = (double) uniqueList.get(value);
					Double[] rgbColorsObj = new Double[3];
					double colorValue = (dynRes * (valueObj - min));
			
					rgbColorsObj[0] = colorValue;
					rgbColorsObj[1] = (double) Math.abs(255 - colorValue);
					rgbColorsObj[2] = (double) Math.abs(255 - colorValue);
					
					layer.colorValueMap.put(valueObj, rgbColorsObj);
				}
				break;
			default:
				break;
		}
	}
	
	// Returns the color that user has picked from the color dialog
	private int[] returnColorDlgVal(Shell toolShell) {
		Color color = null;
		int[] colors = new int[3];

		ColorDialog dlg = new ColorDialog(toolShell);
		RGB rgb = dlg.open();

		if (rgb != null) {
			color = new Color(toolShell.getDisplay(), rgb);
		}

		if (color != null) {
			colors[0] = color.getRed();
			colors[1] = color.getGreen();
			colors[2] = color.getBlue();
		} else {
			colors[0] = 0; colors[1] = 0; colors[2] = 0;
		}

		return colors;
	}

	// Listener for the mouse zooming inside the scrolled composite
	private void scrolledCompositeZoomListener(Shell layerShell, Layer layer) {
		ScrolledComposite sc = storeShellComposite.get(new String(layerShell.getText()));
		if (sc != null) {
			sc.addMouseWheelListener(new MouseWheelListener() {
				public void mouseScrolled(MouseEvent e) {
					if (e.count == 3) {
						layer.scale = layer.scale + 0.2;
						try {
							if (layer.scale < 0.2) {
								layer.scale = 0.2;
								layerShellContent(layerShell, layer);
							} else {
								layerShellContent(layerShell, layer);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						layer.scale = layer.scale - 0.2;
						try {
							if (layer.scale < 0.2) {
								layer.scale = 0.2;
								System.out.println("Scale cannot be under 0.2!");
								layerShellContent(layerShell, layer);
							} else {
								layerShellContent(layerShell, layer);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}

	}

	// Allows multiple same named layers to present at the same time by adding an integer for duplicate layers
	private void checkShellName(Layer layer) {
		for (int k = 0; k < shell.getShells().length; k++) {
			String shellName = shell.getShells()[k].getText();
			if (shellName.contains(layer.name)) {
				shellNameCount += 1;
				layer.name += "_" + shellNameCount;
			}
		}
	}
	
	//updates the layer information window
	private void updateLayerInformation(Shell layerShell, Layer layer){
		//check if shellKey exists in hashmap
		String shellKey = layerShell.getText();
		if(storeShellLayerInformation.containsKey(shellKey)){
			//if so, but layer information window is disposed, create a new one and overwrite in hashmap
			if(storeShellLayerInformation.get(shellKey).layerInformation.isDisposed()){
				LayerInformationWindow liw = new LayerInformationWindow(shell, layerShell,  layer);
				storeShellLayerInformation.put(shellKey, liw);	
			}
			//if so AND layer information window was visible, overwrite it
			else if(storeShellLayerInformation.get(shellKey).layerInformation.isVisible()){
				LayerInformationWindow liw = new LayerInformationWindow(shell, layerShell, layer);
				storeShellLayerInformation.get(shellKey).layerInformation.dispose();
				storeShellLayerInformation.put(shellKey, liw);	
				//show layer information window
				storeShellLayerInformation.get(shellKey).layerInformation.setVisible(true);
			}
		}
	}

	
	// THIS IS COPIED CODE http://www.eclipsezone.com/eclipse/forums/t26813.html
	// Makes an SWT image out of AWT BufferedImage
	private static Image makeSWTImage(Display display, java.awt.Image ai) throws Exception { 
		int width = ai.getWidth(null); 
		int height = ai.getHeight(null); 
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
		Graphics2D g2d = bufferedImage.createGraphics(); 
		g2d.drawImage(ai, 0, 0, null); 
		g2d.dispose(); 
		int[] data = ((DataBufferInt) bufferedImage.getData().getDataBuffer()).getData(); 
		ImageData imageData = new ImageData(width, height, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF)); 
		imageData.setPixels(0, 0, data.length, data, 0); 
		Image swtImage = new Image(display, imageData);

		return swtImage; 
	}

}