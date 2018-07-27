import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class UnpackFunction
{

  public void unpackFunction(final String pptx_path) throws ZipException
  {
    unpackFunction(pptx_path, "");
  }

  public void unpackFunction(final String pptx_path, String unpack_path)
      throws ZipException
  {
    if (unpack_path.isEmpty())
    {
      unpack_path = pptx_path.substring(0, pptx_path.length() - 5);
    }

    // check if unpack_path is directory or not
    if (!Files.exists(Paths.get(pptx_path)) || !pptx_path.endsWith("pptx"))
    {
      System.out.println("pptx_path provided is not a pptx file");
      System.exit(1);
    }

    // Unpack the pptx file
    System.out.println("Unpacking pptx file...");
    if (Files.notExists(Paths.get(unpack_path)))
    {
      new File(unpack_path).mkdir();
    }

    if (Files.exists(Paths.get(unpack_path)))
    {
      try
      {
        FileUtils.deleteDirectory(new File(unpack_path));
      }
      catch (final IOException e)
      {
        System.out.println("Impossible to remove the directory " + unpack_path);
        System.exit(1);
      }
    }

    final ZipFile zip_ref = new ZipFile(pptx_path);
    zip_ref.extractAll(unpack_path);
    System.out.println("File unpacked at " + unpack_path);

  }
}
