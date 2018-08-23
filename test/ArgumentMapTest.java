import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class ArgumentMapTest {

	@Nested
	public class FlagTests {

		// this method generates several tests automatically
		@TestFactory
		public Stream<DynamicTest> testTrue() {
			// these are the test cases that will be generated
			String[] tests = {
					"-a", "-1", "-hello", "--world",
			};

			// this streams through the test cases
			// for each test case arg, it creates a test
			// that asserts isFlag(arg) is true
			return Stream.of(tests).map(arg -> dynamicTest(
					"flag: [" + arg + "]",
					() -> assertTrue(ArgumentMap.isFlag(arg))));
		}

		@TestFactory
		public Stream<DynamicTest> testFalse() {
			String[] tests = {
					"1", "a-b-c", "hello", "hello world", "", " ", "\t", "-", "- ", null
			};

			return Stream.of(tests).map(arg -> dynamicTest(
					"flag: [" + arg + "]",
					() -> assertFalse(ArgumentMap.isFlag(arg))));
		}
	}

	@Nested
	public class ValueTests {

		@TestFactory
		public Stream<DynamicTest> testFalse() {
			String[] tests = {
					"-a", "-1", "-hello", "--world", "", " ", "\t", "-", "- ", null
			};

			return Stream.of(tests).map(arg -> dynamicTest(
					"value: [" + arg + "]",
					() -> assertFalse(ArgumentMap.isValue(arg))));
		}

		@TestFactory
		public Stream<DynamicTest> testTrue() {
			String[] tests = {
					"1", "a-b-c", "hello", "hello world"
			};

			return Stream.of(tests).map(arg -> dynamicTest(
					"value: [" + arg + "]",
					() -> assertTrue(ArgumentMap.isValue(arg))));
		}
	}

	@Nested
	public class CountTests {

		@Test
		public void test01() {
			String[] args = { "-loquat" };
			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test02() {
			String[] args = { "-grape", "raisin" };
			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test03() {
			String[] args = { "-tomato", "-potato" };
			int expected = 2;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test04() {
			String[] args = { "rhubarb" };
			int expected = 0;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test05() {
			String[] args = { "constant", "change" };
			int expected = 0;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test06() {
			String[] args = { "pine", "-apple" };
			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test07() {
			String[] args = { "-aubergine", "eggplant", "-courgette", "zucchini" };
			int expected = 2;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test08() {
			String[] args = { "-tangerine", "satsuma", "-tangerine", "clementine", "-tangerine", "mandarin" };
			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void test09() {
			String[] args = {};
			int expected = 0;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		// it is okay to throw a null pointer exception here
		@Test
		public void test10() {
			String[] args = null;
			assertThrows(java.lang.NullPointerException.class,
					() -> new ArgumentMap(args).numFlags());
		}
	}

	@Nested
	public class ParseTests {
		private ArgumentMap map;
		private String debug;

		@BeforeEach
		public void setup() {
			String[] args = { "-a", "42", "-b", "bat", "cat", "-d", "-e", "elk", "-e", "-f" };

			this.map = new ArgumentMap();
			this.map.parse(args);

			this.debug = "\n" + this.map.toString() + "\n";
		}

		@AfterEach
		public void teardown() {
			this.map = null;
		}

		@Test
		public void testNumFlags() {
			int expected = 5;
			int actual = this.map.numFlags();

			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testHasFlag() {
			assertTrue(this.map.hasFlag("-d"), this.debug);
		}

		@Test
		public void testHasLastFlag() {
			assertTrue(this.map.hasFlag("-f"), this.debug);
		}

		@Test
		public void testHasntFlag() {
			assertFalse(this.map.hasFlag("-g"), this.debug);
		}

		@Test
		public void testHasValue() {
			assertTrue(this.map.hasValue("-a"), this.debug);
		}

		@Test
		public void testHasFlagNoValue() {
			assertFalse(this.map.hasValue("-d"), this.debug);
		}

		@Test
		public void testNoFlagNoValue() {
			assertFalse(this.map.hasValue("-g"), this.debug);
		}

		@Test
		public void testGetValueExists() {
			String expected = "bat";
			String actual = this.map.getString("-b");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValueNull() {
			String expected = null;
			String actual = this.map.getString("-d");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValueNoFlag() {
			String expected = null;
			String actual = this.map.getString("-g");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValueRepeatedFlag() {
			String expected = null;
			String actual = this.map.getString("-e");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetDefaultExists() {
			String expected = "bat";
			String actual = this.map.getString("-b", "bee");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetDefaultNull() {
			String expected = "dog";
			String actual = this.map.getString("-d", "dog");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetDefaultMissing() {
			String expected = "goat";
			String actual = this.map.getString("-g", "goat");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testDoubleParse() {
			String[] args = { "-a", "42", "-b", "bat", "cat", "-d", "-e", "elk", "-e", "-f" };
			this.map.parse(args);

			int expected = 5;
			int actual = this.map.numFlags();

			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValidPath() {
			String[] args = { "-p", "." };
			ArgumentMap map = new ArgumentMap(args);

			Path expected = Paths.get(".");
			Path actual = map.getPath("-p");
			assertEquals(expected, actual);
		}

		@Test
		public void testGetInValidPath() {
			String[] args = { "-p" };
			ArgumentMap map = new ArgumentMap(args);

			Path expected = null;
			Path actual = map.getPath("-p");
			assertEquals(expected, actual);
		}
	}
}
