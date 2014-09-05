package com.ibm.rdf.store.sparql11;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FreebaseQueryUtilityTest<D> extends TestRunner<D> {
	private final String queryDir;
	protected final static Logger logger = LoggerFactory
			.getLogger(FreebaseQueryUtilityTest.class);
	protected static final int[] answers = { 4, 4, 4, 4};

	protected FreebaseQueryUtilityTest(DatabaseEngine<D> engine, D data,
			String queryDir, int[] ans) {
		super(data, engine, ans);
		this.queryDir = queryDir;
	}

	public static class DB2Freebase extends
			FreebaseQueryUtilityTest<DB2TestData> {
		private static final DB2TestData data = DB2TestData.getStore(
				"jdbc:db2://localhost:49999/sl", "fb",
				"db2inst1", "db2inst1", "db2inst1", false);

		public DB2Freebase() {
			super(new DB2Engine(), data,
					"../rdfstore-data/freebase/",
					answers);
		}
	}

	@Test
	public void testQueryQ1() throws Exception {
		String file = queryDir + "q1.sparql";
		System.err.println("Testing:" + file);
		executeQuery(file, 0);
	}

	@Test
	public void testQueryQ2() throws Exception {
		String file = queryDir + "q2a.sparql";
		System.err.println("Testing:" + file);
		executeQuery(file, 1);
	}

	@Test
	public void testQueryQ3() throws Exception {
		String file = queryDir + "q3.sparql";
		System.err.println("Testing:" + file);
		executeQuery(file, 2);
	}

	@Test
	public void testQueryQ4() throws Exception {
		String file = queryDir + "q4.sparql";
		System.err.println("Testing:" + file);
		executeQuery(file, 3);
	}

	@Test
	public void testQueryQ5() throws Exception {
		String file = queryDir + "q5.sparql";
		System.err.println("Testing:" + file);
		executeQuery(file, 4);
	}

	@Test
	public void testQueryQ6() throws Exception {
		String file = queryDir + "q6.sparql";
		System.err.println("Testing:" + file);
		executeQuery(file, 5);
	}

}
