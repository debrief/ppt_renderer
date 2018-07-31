package com.debrief.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.debrief.DebriefException;
import com.debrief.UnpackFunction;

import net.lingala.zip4j.exception.ZipException;

class UnpackFunctionTest
{

  private final String folderToUnpack = Utils.testFolder + File.separator
      + "UnpackPresentation" + File.separator + "designed.pptx";
  private final String expectedFolder = Utils.testFolder + File.separator
      + "PackPresentation" + File.separator + "designedFolder";
  private String generatedFolder = null;

  @AfterEach
  void tearDown() throws Exception
  {
    try
    {
      FileUtils.deleteDirectory(new File(generatedFolder));
    }
    catch (final IOException e)
    {
      
    }
  }

  @Test
  void testUnpackFunctionString() throws ZipException, DebriefException
  {
    generatedFolder = new UnpackFunction().unpackFunction(folderToUnpack);
    assertTrue(Utils.compareDirectoriesStructures(new File(generatedFolder), new File(expectedFolder)));
  }

}
