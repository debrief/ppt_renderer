package com.debrief.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.debrief.DebriefException;
import com.debrief.PackPresentation;

import net.lingala.zip4j.exception.ZipException;

class PackPresentationTest
{

  private final String folderToPack = Utils.testFolder + File.separator
      + "PackPresentation" + File.separator + "designedFolder";
  private final String folderToPackTest = Utils.testFolder + File.separator
      + "PackPresentation" + File.separator + "designedFolderTest";
  private final String expectedPptx = Utils.testFolder + File.separator
      + "PackPresentation" + File.separator + "designed.pptx";
  private String generatedPptx = null;

  @AfterEach
  void tearDown() throws Exception
  {
    new File(generatedPptx).delete();
  }

  @Test
  void testPack() throws IOException, ZipException, DebriefException
  {
    FileUtils.copyDirectory(new File(folderToPack), new File(folderToPackTest),
        true);
    generatedPptx = new PackPresentation().pack(null, folderToPackTest);

    assertFalse(new File(folderToPackTest).exists());
    assertTrue(FileUtils.contentEquals(new File(generatedPptx), new File(
        expectedPptx)));
  }

}
