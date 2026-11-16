import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.model.ComplianceSummary;
import com.teachmehipaa.model.Learner;

/**
 * Prints the Sandbox Organization's Compliance summary and its first five Learners.
 *
 * <p>Run: {@code ./gradlew runExample -Pexample=Quickstart}
 */
public final class Quickstart {
  public static void main(String[] args) {
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder()
            .clientId(requireEnv("TMH_CLIENT_ID"))
            .clientSecret(requireEnv("TMH_CLIENT_SECRET"))
            .environment(Environment.SANDBOX)
            .build();

    ComplianceSummary summary = client.compliance().retrieve();
    System.out.printf(
        "Course %s: %d of %d Learners compliant%n",
        summary.getCourseId(),
        summary.getTotals().getCompliant(),
        summary.getTotals().getLearners());

    client
        .learners()
        .list()
        .autoPagingStream()
        .limit(5)
        .map(Quickstart::describe)
        .forEach(System.out::println);
  }

  private static String describe(Learner learner) {
    return learner.getEmail() + "\t" + learner.getStatus();
  }

  private static String requireEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      System.err.println("Set " + name + " to your Sandbox API client's credentials.");
      System.exit(1);
    }
    return value;
  }

  private Quickstart() {}
}
