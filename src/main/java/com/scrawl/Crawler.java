package com.scrawl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.tuple.Pair;


import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a web crawler.
 * Crawls all the internal pages starting from a given address
 * Creates multiple threads for fetching and parsing urls with a default limit defined by MAX_THREADS
 * Author: somilb
 * Date: 2021-03-21
 **/

public class Crawler {
    static final int MAX_THREADS = 10;

    private int threads;
    private final Logger logger = Logger.getLogger(Crawler.class.getName());
    private volatile Set<String> visitedLinks;
    private volatile Queue<String> toVisit;
    private ListeningExecutorService executor;
    private final Parser parser;

    /**
     * Creates a crawler with a given Parser
     *
     * @param parser Parser for fetching and parsing the links in the given page
     */
    public Crawler(final Parser parser) {
        this.visitedLinks = new HashSet<>();
        this.toVisit = new ConcurrentLinkedQueue<>();
        this.threads = MAX_THREADS;
        this.parser = parser;
    }

    /**
     * Creates a crawler with a given parser and max threads
     *
     * @param parser     Parser for fetching and parsing the links in the given page
     * @param maxThreads Maximum threads to spawn for fetching and parsing urls
     */
    public Crawler(final Parser parser, int maxThreads) {
        this.visitedLinks = new HashSet<>();
        this.toVisit = new ConcurrentLinkedQueue<>();
        this.threads = maxThreads;
        this.parser = parser;
    }

    /**
     * Crawls a web page from a given starting url
     * Only internal links are followed, external links and subdomains are ignored
     *
     * @param startUrl  Starting url for the crawler
     * @param pageLimit Maximum pages to crawl (successful), -1 = no limit
     * @return Returns a map of url to internal links found on the page corresponding to url
     */
    public Map<String, Set<String>> crawl(final String startUrl, final int pageLimit) {
        final ExecutorService es = Executors.newFixedThreadPool(threads);
        executor = MoreExecutors.listeningDecorator(es);

        Preconditions.checkArgument(pageLimit != 0);
        final Map<String, Set<String>> pageLinks = new HashMap<>();
        final AtomicInteger taskCount = new AtomicInteger(0);
        boolean pageLimitReached = false;
        final AtomicInteger pagesCrawled = new AtomicInteger(0);

        toVisit.add(startUrl);
        while ((!toVisit.isEmpty() || taskCount.get() != 0) && !pageLimitReached) {
            final String url = toVisit.poll();
            ListenableFuture<Pair<String, Set<String>>> future;

            if (url != null && !visitedLinks.contains(url)) {
                if (pageLimit < 0 || pagesCrawled.get() < pageLimit) {
                    if (pageLimit > 0) {
                        pagesCrawled.incrementAndGet();
                    }
                    final Callable<Pair<String, Set<String>>> callable = () -> {
                        try {
                            visitedLinks.add(url);
                            return parser.parse(url);
                        } catch (Exception e) {
                            logger.log(Level.WARNING, String.format("Error while fetching url %s, %s", url, e.getMessage()));
                            return null;
                        }
                    };
                    future = executor.submit(callable);
                    taskCount.incrementAndGet();

                    Futures.addCallback(future,
                            getCrawlerCallback(startUrl, pageLinks, taskCount),
                            MoreExecutors.directExecutor());
                } else {
                    pageLimitReached = true;
                }
            }

        }
        cleanUp();
        return pageLinks;
    }

    private FutureCallback<Pair<String, Set<String>>> getCrawlerCallback(final String startUrl,
                                                                         final Map<String, Set<String>> pageLinks,
                                                                         final AtomicInteger taskCount) {
        return new FutureCallback<Pair<String, Set<String>>>() {
            @Override
            public void onSuccess(@Nullable Pair<String, Set<String>> result) {
                if (result != null) {
                    pageLinks.putIfAbsent(result.getKey(), result.getValue());
                    if (!result.getValue().isEmpty()) {
                        final Set<String> filteredLinks = filterInternalLinks(startUrl, result.getValue());
                        toVisit.addAll(filteredLinks);
                    }
                }
                taskCount.decrementAndGet();
            }

            @Override
            public void onFailure(Throwable t) {
                logger.log(Level.WARNING, t.getMessage());
                taskCount.decrementAndGet();
            }
        };
    }

    private void cleanUp() {
        try {
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            executor.shutdown();
            visitedLinks.clear();
            toVisit.clear();
            logger.info("Crawler shutdown successful");
        } catch (InterruptedException e) {
            logger.warning("Exception while crawler cleanup");
        }
    }

    private Set<String> filterInternalLinks(final String inputUrl, final Set<String> urls) {
        final Set<String> filtered = new HashSet<>();

        try {
            final URI inputUri = new URI(inputUrl);
            final String inputDomain = inputUri.getHost();
            for (String url : urls) {
                URI uri = new URI(url);
                if (inputDomain.equalsIgnoreCase(uri.getHost())) {
                    filtered.add(uri.toString());
                }
            }
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, String.format("Malformed url: %s", e.getMessage()));
        }
        return filtered;
    }
}
