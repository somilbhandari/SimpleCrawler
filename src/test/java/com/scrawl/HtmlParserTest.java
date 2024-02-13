package com.scrawl;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Jsoup.class)
public class HtmlParserTest {

    @InjectMocks
    final Parser parser = new HtmlParser();

    final String p1 = "/monzohome.html", p1Base = "https://monzo.com";
    final String p2 = "/small.html", p2Base = "https://example.com";
    Document doc1, doc2;

    final String urlDoc2 = "https://www.iana.org/domains/example";

    @Before
    public void setUp() throws Exception {
        final Path resourceDirectory = Paths.get("src","test","resources");
        final String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        final File inputBig = new File(absolutePath + p1);
        final File inputSmall = new File(absolutePath + p2);
        doc1 = Jsoup.parse(inputBig, "UTF-8", p1Base);
        doc2 = Jsoup.parse(inputSmall, "UTF-8", p2Base);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParser() throws Exception {
        final String url = "dummy";

        final Connection connection = Mockito.mock(Connection.class);
        PowerMockito.mockStatic(Jsoup.class);
        Mockito.when(connection.userAgent(Mockito.anyString())).thenReturn(connection);
        Mockito.when(connection.get()).thenReturn(doc1);

        PowerMockito.when(Jsoup.connect(Mockito.anyString())).thenReturn(connection);

        final Pair<String, Set<String>> res = parser.parse(url);
        Assert.assertEquals(51, res.getValue().size());

        Mockito.when(connection.userAgent(Mockito.anyString())).thenReturn(connection);
        Mockito.when(connection.get()).thenReturn(doc2);
        PowerMockito.when(Jsoup.connect(Mockito.anyString())).thenReturn(connection);
        final Pair<String, Set<String>> res2 = parser.parse(url);
        Assert.assertEquals(1, res2.getValue().size());
        Assert.assertEquals(urlDoc2, res2.getValue().iterator().next());
    }
}