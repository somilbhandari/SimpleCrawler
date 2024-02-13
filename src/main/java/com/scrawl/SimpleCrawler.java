package com.scrawl;

import java.util.Map;
import java.util.Set;

/**
 * Author: somilb
 * Date: 2021-03-21
 **/

public class SimpleCrawler {

    static final String url = "http://somilbhandari.com";

    public static void main(String[] args) {
        final Parser parser = new HtmlParser();
        final Crawler crawler = new Crawler(parser, 20);

        long start = System.nanoTime();
        final Map<String, Set<String>> linkMap = crawler.crawl(url, -1);
        long end = System.nanoTime();
        for (final Map.Entry<String, Set<String>> links : linkMap.entrySet()) {
            System.out.println(String.format("URL : %s", links.getKey()));
            System.out.println(String.format("LINKS : %s", String.join(",", links.getValue())));
        }
        System.out.println("Total pages successfully crawled : " + linkMap.size());
        System.out.println("Total time taken : " + getHumanRedableTime(end - start));
    }

    private static String getHumanRedableTime(Long nanos) {
        long seconds = nanos / (1000 * 1000 * 1000);
        long sec = seconds % 60;
        long min = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;
        long day = (seconds / (24 * 60 * 60)) % 24;

        return String.format("%d %d:%d:%d", day, hour, min, sec);
    }
}
