package com.aestheticsw.jobkeywords.domain.termfrequency;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Locale;

import org.junit.Test;

import com.aestheticsw.jobkeywords.domain.termfrequency.QueryKey;

public class QueryKeyTest {

    @Test
    public void constructor() {
        QueryKey param1 = new QueryKey("query", Locale.FRANCE, "Lyon");

        assertEquals("query", param1.getQuery());
        assertEquals(Locale.FRANCE, param1.getLocale());
        assertEquals("Lyon", param1.getCity());
    }
    
    @Test 
    public void nullValues() {
        QueryKey key = new QueryKey(null, null, null);
        assertEquals(null, key.getQuery());
        assertEquals(null, key.getLocale());
        assertEquals(null, key.getCity());

        // this hard-coded value looks brittle based on dependency on HashCodeBuilder.  
        assertEquals(861101, key.hashCode());
    }
    
    @Test
    public void nullEquals() {
        QueryKey key1 = new QueryKey(null, null, null);
        QueryKey key2 = new QueryKey(null, null, null);
        QueryKey key3 = new QueryKey(null, null, "Lyon");
        
        assertEquals(key1, key2);
        assertFalse(key1.equals(key3));
    }

    @Test
    public void equals() {
        QueryKey param1 = new QueryKey("query", Locale.FRANCE, "Lyon");
        QueryKey param2 = new QueryKey("query", Locale.FRANCE, "Lyon");
        assertEquals(param1, param2);
        assertNotEquals(param1, null);
        assertNotEquals(null, param1);
        assertEquals(param1, param1);

        QueryKey param3 = new QueryKey("uery", Locale.FRANCE, "Lyon");
        assertNotEquals(param1, param3);
        QueryKey param6 = new QueryKey("query", Locale.US, "Lyon");
        assertNotEquals(param1, param6);
        QueryKey param7 = new QueryKey("query", Locale.FRANCE, "Lyo");
        assertNotEquals(param1, param7);
    }

    @Test
    public void hashcode() {
        QueryKey param1 = new QueryKey("query", Locale.FRANCE, "Lyon");
        QueryKey param2 = new QueryKey("query", Locale.FRANCE, "Lyon");
        assertEquals(param1.hashCode(), param2.hashCode());
        assertNotEquals(param1.hashCode(), 0);
        assertNotEquals(0, param1.hashCode());
        assertEquals(param1.hashCode(), param1.hashCode());

        QueryKey param3 = new QueryKey("uery", Locale.FRANCE, "Lyon");
        assertNotEquals(param1.hashCode(), param3.hashCode());
        QueryKey param6 = new QueryKey("query", Locale.US, "Lyon");
        assertNotEquals(param1.hashCode(), param6.hashCode());
        QueryKey param7 = new QueryKey("query", Locale.FRANCE, "Lyo");
        assertNotEquals(param1.hashCode(), param7.hashCode());
    }

    @Test
    public void comparator() {
        Comparator<QueryKey> comp = new QueryKey.QueryKeyComparator();

        QueryKey param1 = new QueryKey("query1", Locale.FRANCE, "Lyon");
        QueryKey param2 = new QueryKey("query2", Locale.FRANCE, "Lyon");
        assertTrue(comp.compare(param1, param2) < 0);

        QueryKey param2_2 = new QueryKey("query2", Locale.FRANCE, "Lyon");
        assertEquals(0, comp.compare(param2, param2_2));

        QueryKey param3 = new QueryKey("Aquery", Locale.FRANCE, "Lyon");
        QueryKey param6 = new QueryKey("Aquery", Locale.US, "Lyon");
        assertTrue(comp.compare(param3, param6) < 0);

        QueryKey param7 = new QueryKey("query", Locale.US, "San Francisco");
        QueryKey param8 = new QueryKey("query", Locale.US, "A City");
        assertTrue(comp.compare(param7, param8) > 0);

        QueryKey param9 = new QueryKey("query", Locale.US, null);
        assertTrue(comp.compare(param8, param9) > 0);
    }

}
