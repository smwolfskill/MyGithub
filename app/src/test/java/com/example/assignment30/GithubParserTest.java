package com.example.assignment30;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class GithubParserTest {
    public static GithubParser githubParser;
    public static GithubParser.Param param1;
    public static GithubParser.Param param1_equiv;
    public static GithubParser.Param param1_supp;
    public static GithubParser.Param param2;

    @BeforeClass
    public static void initOnce() {
        boolean[] mode1 = new boolean[] {false, true, false, false, false, false};
        boolean[] mode1_equiv = new boolean[] {false, true, false, false, false, false};
        boolean[] mode1_supp = new boolean[] {false, false, true, true, false, false};
        boolean[] mode2_search = new boolean[] {false, true, false, false, false, true};
        param1 = new GithubParser.Param(null, "target1", mode1,
                0, 0, null);
        param1_equiv = new GithubParser.Param(null, "target1", mode1_equiv,
                0, 0, null);
        param1_supp = new GithubParser.Param(null, "target1", mode1_supp,
                0, 0, null);
        param2 = new GithubParser.Param(null, "target1", mode2_search,
                0, 0, null); //global search for repos matching "target1"
        githubParser = new GithubParser(param1);
    }

    @Test
    public void testIsEquivalentTo() {
        //Test 1: assert self-equivalence
        assertTrue(githubParser.isEquivalentTo(param1));

        //Test 2: assert equivalence by value
        assertTrue(githubParser.isEquivalentTo(param1_equiv));

        //Test 3: assert not equivalent with one that is only supplementary
        assertFalse(githubParser.isEquivalentTo(param1_supp));

        //Test 4: assert not equivalent with one neither equivalent nor supplementary
        assertFalse(githubParser.isEquivalentTo(param2));
    }

    @Test
    public void testIsSupplementaryTo() {
        //Test 1: assert self-supplementary
        assertTrue(githubParser.isSupplementaryTo(param1));

        //Test 2: assert supplementary with one that is equivalent as well
        assertTrue(githubParser.isSupplementaryTo(param1_equiv));

        //Test 3: assert supplementary with one that is not equivalent but is supplementary
        assertTrue(githubParser.isSupplementaryTo(param1_supp));

        //Test 4: assert not supplementary with one neither equivalent nor supplementary
        assertFalse(githubParser.isSupplementaryTo(param2));
    }

    @Test
    public void testParamCopy() {
        GithubParser.Param param1copy = new GithubParser.Param(param1);
        assertTrue(param1copy.isEquivalentTo(param1));
    }
}
