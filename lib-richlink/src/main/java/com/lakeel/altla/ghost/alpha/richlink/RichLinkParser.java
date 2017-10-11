package com.lakeel.altla.ghost.alpha.richlink;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RichLinkParser {

    private String userAgent;

    private int timeoutMillis;

    private boolean keepHtml;

    private RichLinkParser() {
    }

    @NonNull
    public RichLink parse(@NonNull String uriString) throws IOException {
        Connection connection = Jsoup.connect(uriString);

        if (userAgent != null) connection = connection.userAgent(userAgent);
        if (0 < timeoutMillis) connection = connection.timeout(timeoutMillis);

        Document document = connection.get();
        Metadata metadata = new Metadata(document);

        RichLink link = new RichLink();

        link.documentUri = uriString;
        link.documentTitle = document.title();
        link.documentDescription = metadata.getDescription();

        link.ogUri = metadata.getOgUri();
        link.ogTitle = metadata.getOgTitle();
        link.ogType = metadata.getOgType();
        link.ogImageUri = metadata.getOgImageUri();
        link.ogDescription = metadata.getOgDescription();

        if (keepHtml) link.html = document.html();

        return link;
    }

    public static class Builder {

        // A default timeout of jsoup is 3000ms.
        private static final int DEFAULT_TIMEOUT_MILLIS = 3000;

        // A default user-agent of jsoup follows the URLConnection class.
        // This is determined by Android's Java library
        // like "Dalvik / 2.1.0 (Linux; U; Android 6.0; SO-02H Build / 32.1.F.1.38)".
        // Some pages return an unexpected html against this.
        //
        // e.g. 'www.tenki.jp' returns 403, forbidden, against this.
        //
        // Jsoup fails to parse a html generated by Javascript.
        //
        // e.g. YouTube, Google Maps, and so on.
        //
        // So we use the value for Windows 10 Chrome as user-agent.

        private static final String USER_AGENT_ANDROID_CHROME =
                "Mozilla/5.0 (Linux; Android 6.0; SC-02H Build/32.1.F.1.38) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.81 Mobile Safari/537.36";

        private static final String USER_AGENT_OSX_CHROME =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36";

        private static final String USER_AGENT_WIN10_CHROME =
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

        private static final String DEFAULT_USER_AGENT = USER_AGENT_WIN10_CHROME;

        private String userAgent = DEFAULT_USER_AGENT;

        private int timeoutMillis = DEFAULT_TIMEOUT_MILLIS;

        private boolean keepHtml;

        @NonNull
        public Builder userAgent(@Nullable String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        @NonNull
        public Builder timeoutMillis(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        @NonNull
        public Builder keepHtml(boolean keepHtml) {
            this.keepHtml = keepHtml;
            return this;
        }

        @NonNull
        public RichLinkParser build() {
            RichLinkParser parser = new RichLinkParser();
            parser.userAgent = userAgent;
            parser.timeoutMillis = timeoutMillis;
            parser.keepHtml = keepHtml;
            return parser;
        }
    }

    private class Metadata {

        // Basic OG Metadata.

        static final String QUERY_META_OG_TITLE = "meta[property=og:title]";

        static final String QUERY_META_OG_TYPE = "meta[property=og:ogType]";

        static final String QUERY_META_OG_IMAGE = "meta[property=og:image]";

        static final String QUERY_META_OG_URL = "meta[property=og:url]";

        // Optional OG Metadata.

        static final String QUERY_META_OG_DESCRIPTION = "meta[property=og:description]";

        // HTML metadata.

        static final String QUERY_META_DESCRIPTION = "meta[name=description]";

        final Document document;

        final ContentParser parser;

        Metadata(@NonNull Document document) {
            this.document = document;
            parser = new ContentParser(document);
        }

        @Nullable
        String getOgUri() {
            String content = parser.parse(QUERY_META_OG_URL);
            if (content == null) {
                return null;
            } else {
                try {
                    return new URI(content).toString();
                } catch (URISyntaxException e) {
                    return null;
                }
            }
        }

        @Nullable
        String getOgTitle() {
            return parser.parse(QUERY_META_OG_TITLE);
        }

        @Nullable
        String getOgType() {
            return parser.parse(QUERY_META_OG_TYPE);
        }

        @Nullable
        String getOgImageUri() {
            String content = parser.parse(QUERY_META_OG_IMAGE);
            if (content == null) {
                return null;
            } else {
                try {
                    URI baseUri = new URI(document.location());
                    URI relativeUri = new URI(content);
                    return baseUri.resolve(relativeUri).toString();
                } catch (URISyntaxException e) {
                    return null;
                }
            }
        }

        @Nullable
        String getOgDescription() {
            return parser.parse(QUERY_META_OG_DESCRIPTION);
        }

        @Nullable
        String getDescription() {
            return parser.parse(QUERY_META_DESCRIPTION);
        }
    }

    private class ContentParser {

        static final String ATTRIBUTE_CONTENT = "content";

        final Document document;

        ContentParser(@NonNull Document document) {
            this.document = document;
        }

        @Nullable
        String parse(@NonNull String query) {
            Elements elements = document.select(query);
            String content = elements.attr(ATTRIBUTE_CONTENT);
            return (content == null || content.length() == 0) ? null : content;
        }
    }
}
