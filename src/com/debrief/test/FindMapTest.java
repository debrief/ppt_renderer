package com.debrief.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.debrief.DebriefException;
import com.debrief.FindMap;

class FindMapTest
{

  @Test
  void testGetMapDetails() throws DebriefException
  {
    final String sampleDonorPathFile = Utils.testFolder + File.separator
        + "FindMap";
    
    assertEquals(FindMap.getMapDetails(sampleDonorPathFile),
        new HashMap<String, String>()
        {
          /**
           * Known Result
           */
          private static final long serialVersionUID = -4264437335359313998L;

          {
            put("cx", "6703821");
            put("cy", "4670507");
            put("name", "map");
            put("x", "2486111");
            put("y", "265548");
          }
        });
  }

}
