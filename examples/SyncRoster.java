import com.teachmehipaa.AppInfo;
import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.exception.TeachMeHipaaException;
import com.teachmehipaa.model.Learner;
import com.teachmehipaa.model.LearnerWriteResult;
import com.teachmehipaa.param.LearnerListParams;
import com.teachmehipaa.param.LearnerUpsertByExternalIdParams;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Syncs a roster from your own system into the Sandbox Organization, keyed by External ID, then
 * lists the Learners who are no longer on it.
 *
 * <p>Run: {@code ./gradlew runExample -Pexample=SyncRoster}
 */
public final class SyncRoster {
  /** Stands in for your HR system: each person's own id, email and name. */
  record Person(String employeeId, String email, String name) {}

  private static final List<Person> ROSTER =
      List.of(
          new Person("hr-1001", "ana@example.com", "Ana Example"),
          new Person("hr-1002", "ben@example.com", "Ben Example"),
          new Person("hr-1003", "cy@example.com", "Cy Example"));

  public static void main(String[] args) {
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder()
            .clientId(requireEnv("TMH_CLIENT_ID"))
            .clientSecret(requireEnv("TMH_CLIENT_SECRET"))
            .environment(Environment.SANDBOX)
            .appInfo(AppInfo.of("roster-sync-example"))
            .build();

    // upsertByExternalId invites a person the first time and returns them every time after, so
    // the whole sync is safe to run again after a failure.
    for (Person person : ROSTER) {
      try {
        LearnerWriteResult learner =
            client
                .learners()
                .upsertByExternalId(
                    person.employeeId(),
                    LearnerUpsertByExternalIdParams.builder()
                        .email(person.email())
                        .name(person.name())
                        .build());
        System.out.println(
            person.employeeId() + ": " + (learner.isCreated() ? "invited" : "already enrolled"));
      } catch (TeachMeHipaaException failed) {
        System.err.println(
            person.employeeId()
                + ": failed ("
                + failed.getCode()
                + ", request "
                + failed.getRequestId()
                + ")");
      }
    }

    Set<String> onRoster = ROSTER.stream().map(Person::employeeId).collect(Collectors.toSet());
    LearnerListParams everyone = LearnerListParams.builder().limit(100).build();
    for (Learner learner : client.learners().list(everyone).autoPagingIterable()) {
      if (learner.getExternalId() != null && !onRoster.contains(learner.getExternalId())) {
        // Offboarding is permanent, so this example only reports the Learner. Call
        // client.learners().offboard(learner.getLearnerId()) to remove them.
        System.out.println(learner.getExternalId() + ": no longer on the roster");
      }
    }
  }

  private static String requireEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      System.err.println("Set " + name + " to your Sandbox API client's credentials.");
      System.exit(1);
    }
    return value;
  }

  private SyncRoster() {}
}
