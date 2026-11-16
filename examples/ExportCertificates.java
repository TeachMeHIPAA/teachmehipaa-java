import com.teachmehipaa.Environment;
import com.teachmehipaa.TeachMeHipaaClient;
import com.teachmehipaa.exception.OperationFailedException;
import com.teachmehipaa.exception.OperationTimeoutException;
import com.teachmehipaa.model.CertificatesExportRequestFormat;
import com.teachmehipaa.model.ExportResult;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.CertificateExportParams;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Exports the Sandbox Organization's Certificates to a CSV file, waiting for the Operation.
 *
 * <p>Run: {@code ./gradlew runExample -Pexample=ExportCertificates} (writes the API's suggested
 * filename to the working directory).
 */
public final class ExportCertificates {
  public static void main(String[] args) throws IOException {
    TeachMeHipaaClient client =
        TeachMeHipaaClient.builder()
            .clientId(requireEnv("TMH_CLIENT_ID"))
            .clientSecret(requireEnv("TMH_CLIENT_SECRET"))
            .environment(Environment.SANDBOX)
            .build();

    Operation operation =
        client
            .certificates()
            .export(
                CertificateExportParams.builder()
                    .format(CertificatesExportRequestFormat.CSV)
                    .build());
    System.out.println("Started " + operation.getName());

    try {
      ExportResult export =
          operation
              .waitForCompletion(Duration.ofMinutes(10), Duration.ofSeconds(2))
              .getExportResult();
      String suggested = export.filename() != null ? export.filename() : "certificates.csv";
      Path file = Path.of(args.length > 0 ? args[0] : suggested);
      Files.writeString(file, export.csv());
      System.out.println("Wrote " + export.rowCount() + " Certificates to " + file);
    } catch (OperationFailedException failed) {
      System.err.println("Export failed: " + failed.getCode() + " " + failed.getDetail());
      System.exit(1);
    } catch (OperationTimeoutException slow) {
      // The export keeps running: wait on slow.getOperation() again, or fetch it later by its name
      // with client.operations().retrieve(Operation.operationIdFromName(name)).
      System.err.println("Still running after 10 minutes: " + slow.getOperation().getName());
      System.exit(1);
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

  private ExportCertificates() {}
}
