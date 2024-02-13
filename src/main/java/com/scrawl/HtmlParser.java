package com.scrawl;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Author: somilb
 * Date: 2021-03-21
 **/


public class HtmlParser implements Parser {
    final Logger logger = Logger.getLogger(Crawler.class.getName());
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";


    /**
     * Parses an HTML page and returns absolute links on the page
     *
     * @param url URL to fetch and parse
     * @return Pair of input url and links (converted to absolute links) on the page
     * @throws IOException if error is encountered while fetching the html page
     */
    public Pair<String, Set<String>> parse(final String url) throws IOException {
        logger.info(" > " + Thread.currentThread().getId() + ", Parsing URL " + url);
        final Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
        final Elements links = doc.select("a[href]");

        final Set<String> pageLinks = new HashSet<>();
        for (Element l : links) {
            String link = l.attr("abs:href");
            pageLinks.add(removeTrailingChars(link));
        }
        return Pair.of(url, pageLinks);
    }

    private String removeTrailingChars(final String link) {
        if (link.endsWith("/") || link.endsWith("#") || link.endsWith("?")) {
            return link.substring(0, link.length()-1);
        } else {
            return link;
        }
    }
}
