# Operations and files

Samples use a `client` built as in [Getting started](getting-started.md).

## Operations

Some work finishes after the request that started it returns: exports,
imports, bulk changes, Test deliveries and redrives. Those methods return an
**Operation**. Its `isDone()` says whether it has finished; once it has,
`getResponse()` holds the result, or `getError()` holds the problem.

`waitForCompletion()` polls until the Operation is done and returns its final
state. An Operation that is already done, as exports always are, returns
without a request. An export's `getExportResult()` then hands back the CSV
file:

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

| Call                                      | Waits                                                                |
| ----------------------------------------- | -------------------------------------------------------------------- |
| `waitForCompletion()`                     | Up to 5 minutes, 1 s before the first poll, backing off up to 10 s   |
| `waitForCompletion(timeout, interval)`    | Up to `timeout`, `interval` before the first poll, backing off to 10 s |

An Operation that finished with an error throws `OperationFailedException`,
which carries the problem's `getCode()` and `getDetail()` like any API error.
One still running at the timeout throws `OperationTimeoutException`. Both
carry the Operation in `getOperation()`, so you can wait on it again. An
interrupted thread stops waiting with `OperationInterruptedException` and
keeps its interrupt flag.

## Resume waiting on an Operation

An Operation's `getName()` identifies it. Save it, and later fetch the
Operation with `operations().retrieve`, such as from another process.
`Operation.operationIdFromName` turns the name into the id that method takes:

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

Here `database` stands in for your own storage.

## CSV exports

`learners().export`, `enrollments().export` and `certificates().export` each
start an export Operation. The finished CSV follows RFC 4180, with CRLF line
endings and a header row; `getExportResult()` decodes it to a string, with a
suggested `filename()` (or `null`) and the `rowCount()` (data rows, not
counting the header). It throws `IllegalStateException` if you call it before
the Operation is done, so always wait first.

The [certificate export example](../../examples/ExportCertificates.java)
writes the file to disk.

## CSV imports

`enrollments().importEnrollments` invites everyone in a CSV file with an
`email` column, and optional `name` and `courseId` columns, up to 500 rows.
`enrollments().previewImport` checks the same file first and reports, row by
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

The method is `importEnrollments`, since `import` is a Java keyword.
`FileUpload` takes a `Path`, bytes with a filename, or an `InputStream` with a
filename. Each call reads it once, so a retry resends exactly the same bytes.

## Certificate PDFs

`certificates().downloadPdf` returns the whole PDF as a `BinaryResponse`, with
its `data()`, `contentType()` and `filename()` (or `null`). For large files,
or to pass the file straight through, `downloadPdfStream` returns a
`BinaryStreamResponse` to read as it arrives. Close the stream, ideally with
try-with-resources, to release the connection:

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

With `downloadPdfStream`, retries and the `timeout` cover only getting the
response started; a failure while you read the stream surfaces from the
stream as an `IOException`. Revoked and expired Certificates come back
watermarked.

## Bulk changes

Three methods act on many records in one request, and each record succeeds or
fails on its own:

| Method                        | Acts on                                   | Returns                     |
| ----------------------------- | ----------------------------------------- | --------------------------- |
| `learners().batchUpsert`      | Up to 500 Learners, each created or found | An Operation                |
| `enrollments().batch`         | Up to 500 Enrollments, by id              | The result of each, at once |
| `enrollments().batchByFilter` | Up to 500 Enrollments matching a filter   | An Operation                |

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

An `enrollments().batch` result's `getOutcome()` is `succeeded`, `skipped` or
`failed`; a failed one carries its `getProblem()`.
