package core;

import java.io.File;

/*******************************************************************
 * IOFunctions
 *
 * A class to hold static functions useful for IO functionality.
 * These functions may be used by many classes throughout the
 * code.
 * 
 * @author dstorch
 * 
 *******************************************************************/

public class IOFunctions {

	/**
	 * Recursively deletes the contents of a directory.
	 * Used for document deletion and renaming.
	 * 
	 * @param dir - the directory to delete
	 * @return true if the deletion was successful,
	 * and false otherwise
	 */
	public static boolean deleteDir(File dir) {

		// if directory, then recur on children
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));

				// short circuit if recursive deletion fails
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
	
}
