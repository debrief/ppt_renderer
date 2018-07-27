import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PackPresentation {

    /**
     * Given an unpacked pptx, it creates a pptx file.
     * @param pptx_path pptx file path (Optional). Null to create the unpack_path filename
     * @param unpack_path Folder that contains the pptx slides/documents
     */
    public String pack(String pptx_path, String unpack_path) throws IOException, ZipException {
        if ( unpack_path == null ){
            System.out.println("Provide unpack_path (path to directory containing unpacked pptx)");
            System.exit(1);
        }

        if ( pptx_path == null ){
            if ( unpack_path.charAt(unpack_path.length() - 1) == '/' || unpack_path.charAt(unpack_path.length() - 1) == '\\' ){
                pptx_path = unpack_path.substring(0, unpack_path.length() - 1) + ".pptx";
            }else{
                pptx_path = unpack_path + ".pptx";
            }
        }

        // check if unpack_path is directory or not
        if (!Files.isDirectory(Paths.get(unpack_path))){
            System.out.println("unpack_path provided is not a directory");
            System.exit(1);
        }

        // Pack the unpack_path folder to pptx_path pptx file
        Files.deleteIfExists(new File(pptx_path).toPath());
        ZipFile zipFile = new ZipFile(pptx_path);

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setIncludeRootFolder(false);
        zipFile.addFolder(unpack_path + "/", parameters);
        System.out.println("File packed at " + pptx_path);
        return pptx_path;
    }
}
