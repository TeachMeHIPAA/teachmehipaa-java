package com.teachmehipaa;

import static com.teachmehipaa.Fixtures.ACCESS_TOKEN;
import static com.teachmehipaa.Fixtures.ORG_ID;
import static com.teachmehipaa.Fixtures.TOKEN_ROUTE;
import static com.teachmehipaa.Fixtures.clientFor;
import static com.teachmehipaa.Fixtures.tokenJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teachmehipaa.core.EventTail;
import com.teachmehipaa.core.Page;
import com.teachmehipaa.core.TransportRequest;
import com.teachmehipaa.model.Event;
import com.teachmehipaa.model.EventType;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerStatus;
import com.teachmehipaa.param.EventListParams;
import com.teachmehipaa.param.EventTailParams;
import com.teachmehipaa.param.LearnerListParams;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class PaginationTest {
  private static final String LEARNERS_ROUTE = "GET /v1/organizations/org_acme/learners";
  private static final String EVENTS_ROUTE = "GET /v1/organizations/org_acme/events";

  private static TeachMeHipaaClient clientOn(FakeTransport api) {
    return clientFor(api).organizationId(ORG_ID).build();
  }

  private static TeachMeHipaaClient clientOn(FakeTransport api, FakeSleeper sleeper) {
    return clientFor(api).organizationId(ORG_ID).sleeper(sleeper).build();
  }

  private static FakeTransport api() {
    return new FakeTransport().on(TOKEN_ROUTE, Reply.json(tokenJson(ACCESS_TOKEN)));
  }

  /** research {@code api-sdk-surface.md} §4: {@code {items, nextCursor}}. */
  private static Reply page(String nextCursor, String... items) {
    return Reply.json(
        "{\"items\": ["
            + String.join(", ", items)
            + "], \"nextCursor\": "
            + (nextCursor == null ? "null" : "\"" + nextCursor + "\"")
            + "}");
  }

  private static String learner(String learnerId) {
    return """
        {"learnerId": "%s", "email": "ana@example.com", "name": "Ana Lima",
         "externalId": "hr-123", "status": "in_progress",
         "course": {"courseId": "hipaa-essentials", "title": "HIPAA Essentials"},
         "invitedAt": "2026-11-01T09:00:00Z", "startedAt": null, "completedAt": null,
         "dueAt": null, "certificate": null}
        """
        .formatted(learnerId);
  }

  /** {@code dev#/components/schemas/Event}, a {@code learner.certified} envelope. */
  static String event(String eventId) {
    return """
        {"eventId": "%s", "eventType": "learner.certified", "orgId": "org_acme",
         "occurredAt": "2026-11-15T16:31:30Z",
         "data": {"learnerId": "sub_01JD8Z4N3QX5V7W9Y2B4C6D8EG", "email": "ana@example.com",
                  "externalId": "hr-123", "courseId": "hipaa-essentials", "status": "compliant",
                  "occurredAt": "2026-11-15T16:31:30Z"}}
        """
        .formatted(eventId);
  }

  private static List<String> learnerIds(Iterable<Learner> learners) {
    List<String> learnerIds = new ArrayList<>();
    learners.forEach(learner -> learnerIds.add(learner.getLearnerId()));
    return learnerIds;
  }

  private static List<String> queries(FakeTransport api, String route) {
    return api.requestsTo(route).stream().map(request -> request.uri().getRawQuery()).toList();
  }

  @Test
  void iteratesEveryLearnerAcrossPagesFollowingNextCursorThroughAShortPageUntilItIsNull() {
    FakeTransport api =
        api()
            .on(LEARNERS_ROUTE, page("cur_2", learner("sub_1"), learner("sub_2")))
            // Filters apply per page, so a page can be short with more to come.
            .on(LEARNERS_ROUTE, page("cur_3"))
            .on(LEARNERS_ROUTE, page(null, learner("sub_3")));

    List<String> learnerIds = new ArrayList<>();
    for (Learner learner :
        clientOn(api)
            .learners()
            .list(LearnerListParams.builder().limit(2).build())
            .autoPagingIterable()) {
      learnerIds.add(learner.getLearnerId());
    }

    assertEquals(List.of("sub_1", "sub_2", "sub_3"), learnerIds);
    assertEquals(
        List.of("limit=2", "limit=2&cursor=cur_2", "limit=2&cursor=cur_3"),
        queries(api, LEARNERS_ROUTE));
  }

  @Test
  void stopsFetchingPagesWhenTheLoopBreaks() {
    FakeTransport api =
        api()
            .on(LEARNERS_ROUTE, page("cur_2", learner("sub_1"), learner("sub_2")))
            .on(LEARNERS_ROUTE, page("cur_3", learner("sub_3"), learner("sub_4")))
            .on(LEARNERS_ROUTE, page(null, learner("sub_5")));

    List<String> learnerIds = new ArrayList<>();
    for (Learner learner : clientOn(api).learners().list().autoPagingIterable()) {
      learnerIds.add(learner.getLearnerId());
      if (learner.getLearnerId().equals("sub_3")) {
        break;
      }
    }

    assertEquals(List.of("sub_1", "sub_2", "sub_3"), learnerIds);
    assertEquals(2, api.requestsTo(LEARNERS_ROUTE).size());
  }

  @Test
  void sendsTheFirstCallsFiltersLimitAndRequestOptionsWithEveryPageUnvalidated() {
    FakeTransport api =
        api()
            .on(LEARNERS_ROUTE, page("cur_2", learner("sub_1")))
            .on(LEARNERS_ROUTE, page(null, learner("sub_2")));

    // 500 is over the API's maximum of 100: the server, not the client, rejects it.
    Page<Learner> first =
        clientOn(api)
            .learners()
            .list(
                LearnerListParams.builder()
                    .limit(500)
                    .status(LearnerStatus.IN_PROGRESS)
                    .courseId("hipaa-essentials")
                    .build(),
                RequestOptions.builder()
                    .requestId("req_sync_42")
                    .timeout(Duration.ofSeconds(5))
                    .build());

    assertEquals(List.of("sub_1", "sub_2"), learnerIds(first.autoPagingIterable()));
    assertEquals(
        List.of(
            "limit=500&status=in_progress&courseId=hipaa-essentials",
            "limit=500&status=in_progress&courseId=hipaa-essentials&cursor=cur_2"),
        queries(api, LEARNERS_ROUTE));
    for (TransportRequest request : api.requestsTo(LEARNERS_ROUTE)) {
      assertEquals("req_sync_42", request.headers().get("x-request-id"));
      assertEquals(Duration.ofSeconds(5), request.timeout());
    }
  }

  @Test
  void pagesTheEventLogByAfterTheLastEventsIdNotCursor() {
    // §4: nextCursor is the last Event id while more rows exist; an empty page can only pass
    // nextCursor on.
    FakeTransport api =
        api()
            .on(EVENTS_ROUTE, page("evt_02", event("evt_01"), event("evt_02")))
            .on(EVENTS_ROUTE, page("evt_02b"))
            .on(EVENTS_ROUTE, page(null, event("evt_03")));

    List<String> eventIds = new ArrayList<>();
    for (Event event :
        clientOn(api)
            .events()
            .list(EventListParams.builder().type(EventType.LEARNER_CERTIFIED).build())
            .autoPagingIterable()) {
      eventIds.add(event.getEventId());
    }

    assertEquals(List.of("evt_01", "evt_02", "evt_03"), eventIds);
    assertEquals(
        List.of(
            "type=learner.certified",
            "type=learner.certified&after=evt_02",
            "type=learner.certified&after=evt_02b"),
        queries(api, EVENTS_ROUTE));
  }

  @Test
  void returnsTheFirstPageWhichFetchesThePagesAfterItOnRequest() {
    FakeTransport api =
        api()
            .on(
                LEARNERS_ROUTE,
                page("cur_2", learner("sub_1")).withHeader("x-request-id", "req_page_1"))
            .on(LEARNERS_ROUTE, page("cur_3", learner("sub_2")))
            .on(LEARNERS_ROUTE, page(null, learner("sub_3")));

    Page<Learner> first =
        clientOn(api).learners().list(LearnerListParams.builder().limit(1).build());

    assertEquals(List.of("sub_1"), learnerIds(first.items()));
    assertEquals("cur_2", first.nextCursor());
    assertEquals("req_page_1", first.getLastResponse().requestId());
    assertTrue(first.hasNextPage());
    assertEquals(1, api.requestsTo(LEARNERS_ROUTE).size());

    Page<Learner> second = first.nextPage();
    assertEquals(List.of("sub_2"), learnerIds(second.items()));

    List<List<String>> pages = new ArrayList<>();
    for (Page<Learner> page : second.pages()) {
      pages.add(learnerIds(page.items()));
    }
    assertEquals(List.of(List.of("sub_2"), List.of("sub_3")), pages);
    assertEquals(3, api.requestsTo(LEARNERS_ROUTE).size());
  }

  @Test
  void refusesNextPageOnTheLastPageWithoutSendingARequest() {
    FakeTransport api = api().on(LEARNERS_ROUTE, page(null, learner("sub_1")));

    Page<Learner> page = clientOn(api).learners().list();

    assertFalse(page.hasNextPage());
    assertThrows(NoSuchElementException.class, page::nextPage);
    assertEquals(1, api.requestsTo(LEARNERS_ROUTE).size());
  }

  @Test
  void streamsObjectsAcrossPagesFetchingOnlyThePagesTheStreamNeeds() {
    FakeTransport api =
        api()
            .on(LEARNERS_ROUTE, page("cur_2", learner("sub_1"), learner("sub_2")))
            .on(LEARNERS_ROUTE, page("cur_3", learner("sub_3"), learner("sub_4")))
            .on(LEARNERS_ROUTE, page(null, learner("sub_5")));

    Page<Learner> first = clientOn(api).learners().list();
    var stream = first.autoPagingStream().map(Learner::getLearnerId);
    assertEquals(1, api.requestsTo(LEARNERS_ROUTE).size());

    assertEquals(List.of("sub_1", "sub_2", "sub_3"), stream.limit(3).toList());
    assertEquals(2, api.requestsTo(LEARNERS_ROUTE).size());
  }

  private static List<String> eventIds(Iterable<Event> events) {
    List<String> eventIds = new ArrayList<>();
    events.forEach(event -> eventIds.add(event.getEventId()));
    return eventIds;
  }

  @Test
  void tailResumesFromARememberedEventIdYieldsOnlyNewerEventsAndReturnsWhenCaughtUp() {
    FakeSleeper sleeper = new FakeSleeper();
    FakeTransport api =
        api()
            .on(EVENTS_ROUTE, page("evt_04", event("evt_03"), event("evt_04")))
            // §4: nextCursor is null at the current end, so it gives no resume point; the tail
            // keeps the last id itself.
            .on(EVENTS_ROUTE, page(null, event("evt_05")));

    EventTail tail =
        clientOn(api, sleeper)
            .events()
            .tail(
                EventTailParams.builder()
                    .after("evt_02")
                    .type(EventType.LEARNER_CERTIFIED)
                    .limit(2)
                    .build());
    assertEquals("evt_02", tail.lastEventId());

    assertEquals(List.of("evt_03", "evt_04", "evt_05"), eventIds(tail));
    assertEquals("evt_05", tail.lastEventId());
    assertEquals(
        List.of(
            "limit=2&after=evt_02&type=learner.certified",
            "limit=2&after=evt_04&type=learner.certified"),
        queries(api, EVENTS_ROUTE));
    assertEquals(List.of(), sleeper.sleeps());
  }

  @Test
  void tailStartsAtTheOldestRetainedEventWhenGivenNoId() {
    FakeTransport api = api().on(EVENTS_ROUTE, page(null, event("evt_01")));

    EventTail tail = clientOn(api).events().tail();
    assertNull(tail.lastEventId());

    assertEquals(List.of("evt_01"), eventIds(tail));
    assertNull(api.requestsTo(EVENTS_ROUTE).get(0).uri().getRawQuery());
    assertEquals("evt_01", tail.lastEventId());
  }

  @Test
  void followingTailPollsFromItsLastSeenIdEveryTwoSecondsUntilTheThreadIsInterrupted() {
    FakeSleeper sleeper = new FakeSleeper();
    FakeTransport api =
        api()
            .on(EVENTS_ROUTE, page(null, event("evt_03")))
            .on(EVENTS_ROUTE, page(null))
            .on(EVENTS_ROUTE, page(null, event("evt_04"), event("evt_05")));

    EventTail tail =
        clientOn(api, sleeper)
            .events()
            .tail(EventTailParams.builder().after("evt_02").follow(true).build());
    List<String> eventIds = new ArrayList<>();
    for (Event event : tail) {
      eventIds.add(event.getEventId());
      if (event.getEventId().equals("evt_04")) {
        Thread.currentThread().interrupt();
      }
    }

    // The interrupt lands mid-page: evt_05 was fetched but never handed out, so a resume from
    // lastEventId picks it up.
    assertTrue(Thread.interrupted(), "the interrupt flag is left set");
    assertEquals(List.of("evt_03", "evt_04"), eventIds);
    assertEquals("evt_04", tail.lastEventId());
    assertEquals(
        List.of("after=evt_02", "after=evt_03", "after=evt_03"), queries(api, EVENTS_ROUTE));
    assertEquals(List.of(Duration.ofSeconds(2), Duration.ofSeconds(2)), sleeper.sleeps());
  }

  @Test
  void followingTailWaitsTheGivenIntervalAndStopsPollingWhenTheLoopBreaks() {
    FakeSleeper sleeper = new FakeSleeper();
    FakeTransport api =
        api().on(EVENTS_ROUTE, page(null)).on(EVENTS_ROUTE, page(null, event("evt_03")));

    EventTail tail =
        clientOn(api, sleeper)
            .events()
            .tail(
                EventTailParams.builder()
                    .after("evt_02")
                    .follow(true)
                    .interval(Duration.ofMillis(250))
                    .build());
    for (Event event : tail) {
      assertEquals("evt_03", event.getEventId());
      break;
    }

    assertEquals(List.of("after=evt_02", "after=evt_02"), queries(api, EVENTS_ROUTE));
    assertEquals(List.of(Duration.ofMillis(250)), sleeper.sleeps());
    assertEquals("evt_03", tail.lastEventId());
  }

  @Test
  void followingTailEndsQuietlyWhenInterruptedWhileWaitingBetweenPolls() {
    FakeTransport api = api().on(EVENTS_ROUTE, page(null, event("evt_03")));
    TeachMeHipaaClient client =
        clientFor(api)
            .organizationId(ORG_ID)
            .sleeper(
                duration -> {
                  throw new InterruptedException();
                })
            .build();

    EventTail tail =
        client.events().tail(EventTailParams.builder().after("evt_02").follow(true).build());

    assertEquals(List.of("evt_03"), eventIds(tail));
    assertTrue(Thread.interrupted(), "the interrupt flag is restored");
    assertEquals(1, api.requestsTo(EVENTS_ROUTE).size());
    assertEquals("evt_03", tail.lastEventId());
  }
}
