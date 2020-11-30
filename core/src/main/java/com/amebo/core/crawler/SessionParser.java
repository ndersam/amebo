package com.amebo.core.crawler;

import com.amebo.core.domain.DismissMailNotificationForm;
import com.amebo.core.domain.Session;
import com.amebo.core.domain.User;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.amebo.core.crawler.ExtensionsKt.hasClass;
import static com.amebo.core.crawler.ExtensionsKt.isTag;

public class SessionParser {

    private static final String SHARED = "/shared";
    private static final String FOLLOWED = "/followed";
    private static final String FOLLOWED_BOARDS = "/followedboards";
    private static final String LIKES_AND_SHARES = "/likesandshares";
    private static final String MENTIONS = "/mentions";
    private static final String FOLLOWING = "/following";
    private static final Pattern UPDATE_PATTERN = Pattern.compile("\\((\\d+)\\)");
    private final Element data;
    private final Element summary;

    private Session session = null;

    /**
     * @param summary {@link Element} with id "#up"
     */
    private SessionParser(Element summary) {
        this.summary = summary;
        check(summary);
        data = summary.selectFirst("tbody > tr > td");
        parse();
    }

    /**
     * @param summary {@link Element} with id "#up"
     */
    public static Session parse(Element summary) {
        return new SessionParser(summary).getSession();
    }


    private static boolean hasHref(Element element, String href) {
        return element.attr("href").equalsIgnoreCase(href);
    }

    private static int findUpdatesCount(Element anchor) {
        Matcher m = UPDATE_PATTERN.matcher(anchor.text().trim());
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private void check(Element summary) {
        if (!summary.id().equals("up"))
            throw new IllegalArgumentException("MainHeader only parses the element with id \"up.\"");
    }

    private void parse() {
        session = new Session();

        // IsLoggedIn?
        Element second = data.child(1);
        if (isTag(second, "b") || hasText(second, "guest")) {
            session.setLoggedIn(false);
            return;
        }

        // WhoIsLoggedIn?
        if (isTag(second, "a") && hasClass(second, "user"))
            session.setLoggedIn(true);
        else
            throw new IllegalStateException("Parsing error.... Couldn't ascertain login status");

        session.setActiveUser(new User(second.text()));

        // Updates?
        for (Element anchor : data.select("a")) {
            if (hasHref(anchor, SHARED))
                session.setSharedWithMe(findUpdatesCount(anchor));
            else if (hasHref(anchor, FOLLOWED))
                session.setFollowedTopics(findUpdatesCount(anchor));
            else if (hasHref(anchor, FOLLOWED_BOARDS))
                session.setFollowedBoards(findUpdatesCount(anchor));
            else if (hasHref(anchor, LIKES_AND_SHARES))
                session.setLikesAndShares(findUpdatesCount(anchor));
            else if (hasHref(anchor, MENTIONS))
                session.setMentions(findUpdatesCount(anchor));
            else if (hasHref(anchor, FOLLOWING))
                session.setFollowing(findUpdatesCount(anchor));
        }

        // New mail?
        Element form = summary.selectFirst("form[action=\"/do_dismiss\"]");
        if (form != null) {
            List<User> senders = new ArrayList<>();
            for (Element sender : form.select("input[name=\"pmsenders\"]")) {
                senders.add(new User(sender.attr("value")));
            }
            if (!senders.isEmpty()) {
                String session = form.selectFirst("input[name=\"session\"]").attr("value");
                String redirect = form.selectFirst("input[name=\"redirect\"]").attr("value");
                this.session.setMailNotificationForm(
                        new DismissMailNotificationForm(
                                session,
                                redirect,
                                senders
                        )
                );
            }
        }
    }

    public Session getSession() {
        return session;
    }

    boolean hasText(Element element, String text) {
        return element.text().trim().equalsIgnoreCase(text);
    }
}