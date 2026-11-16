# Operations and files

Samples use a `client` built as in [Getting started](getting-started.md).

## Operations

Exports, imports, bulk changes, Test deliveries and redrives can finish after
the request that started them. Their methods return an `Operation`:

| Method | Returns |
| --- | --- |
| `getName()` | `organizations/{orgId}/operations/{operationId}` |
| `isDone()` | Whether the work has finished |
| `getResponse()` | The result as a `JsonNode`, once done without error |
| `getError()` | The `Problem`, once done with an error |
| `getMetadata()` | Progress details, keyed by name |
| `waitForCompletion()` | Polls until done; returns the final state |
| `waitForCompletionAsync()` | The same, without blocking; see [Async](async.md#operations) |

An Operation that is already done, as every export is, returns from
`waitForCompletion()` without a request. `getExportResult()` then decodes the
CSV:

<!-- sample: operations -->
```java
import com.teachmehipaa.model.ExportResult;
import com.teachmehipaa.model.LearnersExportRequestFormat;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.LearnerExportParams;
import java.time.Duration;

Operation export =
    client
        .learners()
        .export(
            LearnerExportParams.builder()
                .format(LearnersExportRequestFormat.CSV)
                .courseId("hipaa-2026")
                .build());
ExportResult result =
    export.waitForCompletion(Duration.ofMinutes(1), Duration.ofSeconds(2)).getExportResult();
System.out.println(result.filename() + " " + result.rowCount() + " " + result.csv().length());
```

| Overload | Waits |
| --- | --- |
| `waitForCompletion()` | Up to `Operation.DEFAULT_WAIT_TIMEOUT` (5 min); first poll after `DEFAULT_POLL_INTERVAL` (1 s), backing off to 10 s |
| `waitForCompletion(Duration timeout, Duration interval)` | Up to `timeout`; first poll after `interval`, backing off to 10 s, never closer than `interval` |

The last poll lands on the deadline. Outcomes other than success throw:

| Exception | Cause | `getOperation()` |
| --- | --- | --- |
| `OperationFailedException` | The Operation finished with an error; `getCode()` and `getDetail()` come from its problem | The failed Operation |
| `OperationTimeoutException` | Still running at the deadline | The last state polled; wait on it again |
| `OperationInterruptedException` | The waiting thread was interrupted; the interrupt flag stays set | The last state polled |

## Resuming an Operation

`getName()` identifies an Operation across processes. Persist it, and later
fetch the Operation through `operations().retrieve`.
`Operation.operationIdFromName` extracts the id that method takes; `database`
stands for your storage:

<!-- sample: operations-resume -->
```java
import com.teachmehipaa.model.CertificatesExportRequestFormat;
import com.teachmehipaa.model.Operation;
import com.teachmehipaa.param.CertificateExportParams;

Operation started =
    client
        .certificates()
        .export(
            CertificateExportParams.builder()
                .format(CertificatesExportRequestFormat.CSV)
                .build());
database.saveOperationName(started.getName());

// Later, maybe in another process:
Operation operation =
    client.operations().retrieve(Operation.operationIdFromName(database.loadOperationName()));
System.out.println(operation.waitForCompletion().getExportResult().rowCount());
```

## CSV exports

`learners().export`, `enrollments().export` and `certificates().export` start
an export Operation. The file is RFC 4180 CSV with CRLF line endings and a
header row. `getExportResult()` returns an `ExportResult` with the decoded
`csv()`, a suggested `filename()` (may be `null`) and `rowCount()`, the number
of data rows. Calling it before the Operation is done throws
`IllegalStateException`.

[`ExportCertificates.java`](../../examples/ExportCertificates.java) writes the
file to disk.

## CSV imports

`enrollments().importEnrollments` invites everyone in a CSV with an `email`
column and optional `name` and `courseId` columns, up to 500 rows. The method
is not named `import` because `import` is a reserved word in Java.
`enrollments().previewImport` validates the same file and reports, row by
row, what an import would do, without inviting anyone:

<!-- sample: csv-import -->
```java
import com.teachmehipaa.FileUpload;
import com.teachmehipaa.model.ImportPreview;
import com.teachmehipaa.param.EnrollmentImportParams;
import com.teachmehipaa.param.EnrollmentPreviewImportParams;
import com.teachmehipaa.param.ImportOptions;
import java.nio.file.Path;

void importRoster(Path roster) {
  ImportPreview preview =
      client
          .enrollments()
          .previewImport(
              EnrollmentPreviewImportParams.builder()
                  .file(FileUpload.of(roster))
                  .options(ImportOptions.builder().courseId("hipaa-2026").build())
                  .build());
  System.out.println(preview.getValidRows() + " of " + preview.getTotalRows() + " rows are valid");

  if (preview.getValidRows() == preview.getTotalRows()) {
    client
        .enrollments()
        .importEnrollments(
            EnrollmentImportParams.builder()
                .file(FileUpload.of(roster))
                .options(ImportOptions.builder().courseId("hipaa-2026").build())
                .build())
        .waitForCompletion();
  }
}
```

`FileUpload` wraps a `Path`, a `byte[]` with a filename, or an `InputStream`
with a filename. A `Path` is read each time a call sends it; a stream is read
once, when you create the `FileUpload`. Either way, every retry of a call
sends the same bytes.

## Certificate PDFs

`certificates().downloadPdf` buffers the PDF into a `BinaryResponse` with
`data()`, `contentType()` and `filename()` (may be `null`).
`downloadPdfStream` returns a `BinaryStreamResponse` instead, whose
`inputStream()` delivers bytes as they arrive. It implements `AutoCloseable`
and holds the connection until closed, so use try-with-resources:

<!-- sample: pdf-download -->
```java
import com.teachmehipaa.core.BinaryResponse;
import com.teachmehipaa.core.BinaryStreamResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

void saveCertificate(Path directory) throws IOException {
  BinaryResponse pdf = client.certificates().downloadPdf("cert_01JD8F");
  String filename = pdf.filename() != null ? pdf.filename() : "certificate.pdf";
  Files.write(directory.resolve(filename), pdf.data());

  try (BinaryStreamResponse stream = client.certificates().downloadPdfStream("cert_01JD8F")) {
    Files.copy(stream.inputStream(), directory.resolve("certificate-copy.pdf"));
  }
}
```

Retries and the `timeout` cover the streamed download only until the response
headers arrive. A failure while you read surfaces as an `IOException` from the
stream. Revoked and expired Certificates are returned watermarked.

## Bulk changes

Three methods change many records in one request; each record succeeds or
fails on its own:

| Method | Selects | Returns |
| --- | --- | --- |
| `learners().batchUpsert` | Up to 500 Learners, each created or matched | `Operation` |
| `enrollments().batch` | Up to 500 Enrollments by id | `EnrollmentsBatchResults`, immediately |
| `enrollments().batchByFilter` | Up to 500 Enrollments matching a filter | `Operation` |

<!-- sample: bulk-changes -->
```java
import com.teachmehipaa.model.EnrollmentsBatchRequestOperation;
import com.teachmehipaa.model.EnrollmentsBatchRequestSelectFilterFilterStatus;
import com.teachmehipaa.model.EnrollmentsBatchResults;
import com.teachmehipaa.model.EnrollmentsBatchResultsResultsItem;
import com.teachmehipaa.param.EnrollmentBatchByFilterParams;
import com.teachmehipaa.param.EnrollmentBatchParams;
import com.teachmehipaa.param.EnrollmentsBatchRequestSelectEnrollmentIds;
import com.teachmehipaa.param.EnrollmentsBatchRequestSelectFilter;
import com.teachmehipaa.param.EnrollmentsBatchRequestSelectFilterFilter;
import java.util.List;

EnrollmentsBatchResults reminded =
    client
        .enrollments()
        .batch(
            EnrollmentBatchParams.builder()
                .operation(EnrollmentsBatchRequestOperation.REMIND)
                .select(
                    EnrollmentsBatchRequestSelectEnrollmentIds.builder()
                        .enrollmentIds(List.of("enr_01", "enr_02"))
                        .build())
                .build());
for (EnrollmentsBatchResultsResultsItem result : reminded.getResults()) {
  System.out.println(result.getEnrollmentId() + " " + result.getOutcome());
}

client
    .enrollments()
    .batchByFilter(
        EnrollmentBatchByFilterParams.builder()
            .operation(EnrollmentsBatchRequestOperation.RECERTIFY)
            .select(
                EnrollmentsBatchRequestSelectFilter.builder()
                    .filter(
                        EnrollmentsBatchRequestSelectFilterFilter.builder()
                            .status(EnrollmentsBatchRequestSelectFilterFilterStatus.EXPIRED)
                            .courseId("hipaa-2026")
                            .build())
                    .build())
            .build())
    .waitForCompletion();
```

Each `enrollments().batch` result's `getOutcome()` is `succeeded`, `skipped`
or `failed`; a failed result carries `getProblem()`.
