package gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/************************************************
 * ExtensionFileFilter
 * 
 * This file filter class is added to the file
 * chooser. It makes sure that only image files
 * can be selected from the dialog box.
 * 
 * @author dstorch
 * 
 ************************************************/

public class ExtensionFileFilter extends FileFilter {
	
	private String[] _extensions;
	
	public ExtensionFileFilter(String[] extensions) {
		_extensions = extensions;
	}

	/**
	 * Returns true if the file has a valid extension,
	 * and returns false otherwise.
	 * 
	 * @param f - a file
	 * @return true if it is valid for the user to select
	 * the file, otherwise false
	 */
	@Override
	public boolean accept(File f) {
		String name = f.getAbsolutePath();
		name = name.toLowerCase();
		
		if (f.isDirectory()) return true;
		
		for (String ext : _extensions) {
			if (name.endsWith(ext)) return true;
		}
		
		// if true has not been returned already, then false
		return false;
	}

	/**
	 * @return the String which will display the valid
	 * file extensions to the user
	 */
	@Override
	public String getDescription() {
		
		String output = "";
		
		for (int i = 0; i < _extensions.length; i++) {
			output += _extensions[i];
			if (i != (_extensions.length-1)) output += ", ";
		}
		
		return output;
	}

}
