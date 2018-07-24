import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;


public class UnpackFunction {
	
	public void unpackFunction(String pptx_path, String unpack_path) {
		// We don't need to check if the arguments are passed
		/*
	    if(pptx_path is None):
        print("Provide pptx_path (path to packed pptx file)")
        sys.exit(1)
		 */
		
		if ( unpack_path.isEmpty() ) {
			unpack_path = pptx_path.substring(0, pptx_path.length() - 5);
		}
		
		// check if unpack_path is directory or not
		if ( !Files.exists(Paths.get(pptx_path)) || !pptx_path.endsWith("pptx")) {
			System.out.println("pptx_path provided is not a pptx file");
			System.exit(1);
		}
		
		// Unpack the pptx file
		System.out.println("Unpacking pptx file...");
		if ( Files.notExists(Paths.get(unpack_path)) ) {
			new File(unpack_path).mkdir();
		}
		
		try {
			// TODO
			/**
			 * We are not checking if the directory was empty
			 * because extracting the Zip file. Is it needed?
			 * 
			 * Saul Hidalgo
			 */
			ZipFile zip_ref = new ZipFile(pptx_path);
			zip_ref.extractAll(unpack_path);
			System.out.println("File unpacked at " + unpack_path);
		} catch (ZipException e) {
			System.out.println("Unzippable or corrupted pptx file");
			System.exit(1);
		}
		
	}
	
	public void unpackFunction(String pptx_path) {
		unpackFunction(pptx_path, "");
	}
}
