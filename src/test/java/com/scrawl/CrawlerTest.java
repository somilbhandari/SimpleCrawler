package com.scrawl;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class CrawlerTest {

    Parser parser;

    final String url1 = "http://www.a.com";
    final String url2 = "http://www.a.com/b";
    final String url3 = "http://www.a.com/c";

    final Set<String> links1 = new HashSet<>(Arrays.asList("http://www.a.com", "http://www.a.com/b", "http://www.a.com/c"));

    final Set<String> links2 = new HashSet<>(Arrays.asList("http://www.a.com/b", "http://www.a.com/c", "http://www.b.com"));

    final Set<String> links3 = Collections.emptySet();

    final Pair<String, Set<String>> p1 = Pair.of(url1, links1);
    final Pair<String, Set<String>> p2 = Pair.of(url2, links2);
    final Pair<String, Set<String>> p3 = Pair.of(url3, links3);

    @Before
    public void setUp() throws Exception {
        parser = Mockito.mock(HtmlParser.class);
        Mockito.when(parser.parse(url1)).thenReturn(p1);
        Mockito.when(parser.parse(url2)).thenReturn(p2);
        Mockito.when(parser.parse(url3)).thenReturn(p3);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCrawl() throws Exception {
        final Crawler crawler = new Crawler(parser, 5);

        final Map<String, Set<String>> result2 = crawler.crawl(url1, 2);
        Assert.assertEquals(2, result2.size());

        final Map<String, Set<String>> result1 = crawler.crawl(url1, 3);
        Assert.assertEquals(3, result1.size());
        Assert.assertEquals(links1, result1.get(url1));
        Assert.assertEquals(links2, result1.get(url2));
        Assert.assertEquals(links3, result1.get(url3));

        final Map<String, Set<String>> result3 = crawler.crawl(url1, 1);
        Assert.assertEquals(1, result3.size());
    }

    @Test
    public void testCrawlDefaultThreads() throws Exception {
        final Crawler crawler = new Crawler(parser);
        final Map<String, Set<String>> result1 = crawler.crawl(url1, -1);
        Assert.assertEquals(3, result1.size());
        Assert.assertEquals(links1, result1.get(url1));
        Assert.assertEquals(links2, result1.get(url2));
        Assert.assertEquals(links3, result1.get(url3));
    }
}