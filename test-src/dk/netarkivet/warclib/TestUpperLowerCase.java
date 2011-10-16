package dk.netarkivet.warclib;

import java.io.File;
import java.io.FileInputStream;
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

@RunWith(Parameterized.class)
public class TestUpperLowerCase {

	private int expected;
	private String warcFile;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{5, "test-upper-lower-case.warc"}
		});
	}

	public TestUpperLowerCase(int expected, String warcFile) {
		this.expected = expected;
		this.warcFile = warcFile;
	}

	@Test
	public void test() {
		File file = new File( warcFile );
		InputStream in;

		int records = 0;
		int errors = 0;

		try {
			in = new FileInputStream( file );

			WarcParser parser = new WarcParser( in );
			WarcRecord record;

			while ( (record = parser.nextRecord()) != null ) {
				TestWarc.printRecord(record);
				TestWarc.printRecordErrors(record);

				++records;

				if (record.hasErrors()) {
					errors += record.getValidationErrors().size();
				}
			}

			System.out.println("--------------");
			System.out.println("       Records: " + records);
			System.out.println("        Errors: " + errors);

			parser.close();
			in.close();
		}
		catch (FileNotFoundException e) {
			Assert.fail("Input file missing");
		}
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}

		Assert.assertEquals(expected, records);
		Assert.assertEquals(0, errors);
	}

}
