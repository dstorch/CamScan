package gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {
	
	private String[] _extensions;
	
	public ExtensionFileFilter(String[] extensions) {
		_extensions = extensions;
	}

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
