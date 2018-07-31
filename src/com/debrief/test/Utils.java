package com.debrief.test;

import java.io.File;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

public class Utils
{
  public static final String testFolder = "expected_test_results";
  
  public static boolean compareDirectoriesStructures(
      File generatedPptxTemporaryFolder, File expectedPptxTemporaryFolder)
  {
    HashSet<String> gen = new HashSet<>();
    for (File genFile : FileUtils.listFiles(generatedPptxTemporaryFolder, null,
        true))
    {
      gen.add(genFile.getAbsolutePath().substring(generatedPptxTemporaryFolder
          .getAbsolutePath().length()));
    }
    HashSet<String> exp = new HashSet<>();
    for (File expFile : FileUtils.listFiles(expectedPptxTemporaryFolder, null,
        true))
    {
      exp.add(expFile.getAbsolutePath().substring(expectedPptxTemporaryFolder
          .getAbsolutePath().length()));
    }

    return exp.containsAll(gen) && gen.containsAll(exp);
  }
}
