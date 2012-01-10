package org.jwat.warc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

@RunWith(Parameterized.class)
public class TestDigestFields {

    private int expected_records;
    private int[] expected_errors;
    private String warcFile;

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                {4, new int[]{0, 2, 2, 2}, "test-digest-fields.warc"}
        });
    }

    public TestDigestFields(int records, int[] errors, String warcFile) {
        this.expected_records = records;
        this.expected_errors = errors;
        this.warcFile = warcFile;
    }

    @Test
    public void test() {
        boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

        InputStream in;

        int records = 0;
        int errors = 0;

        try {
            in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

            WarcReader reader = WarcReaderFactory.getReader(in);
            WarcRecord record;

            while ((record = reader.getNextRecord()) != null) {
                if (bDebugOutput) {
                    RecordDebugBase.printRecord(record);
                    RecordDebugBase.printRecordErrors(record);
                }

                record.close();

                errors = 0;
                if (record.hasErrors()) {
                    errors = record.getValidationErrors().size();
                }

                Assert.assertEquals(expected_errors[records], errors);

                ++records;
            }

            reader.close();
            in.close();

            if (bDebugOutput) {
                RecordDebugBase.printStatus(records, errors);
            }
        }
        catch (FileNotFoundException e) {
            Assert.fail("Input file missing");
        }
        catch (IOException e) {
            Assert.fail("Unexpected io exception");
        }

        Assert.assertEquals(expected_records, records);
    }

}