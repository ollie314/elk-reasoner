/**
 * 
 */
package org.semanticweb.elk.reasoner.incremental;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2012 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.semanticweb.elk.io.IOUtils;
import org.semanticweb.elk.owl.exceptions.ElkException;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClassAxiom;
import org.semanticweb.elk.owl.iris.ElkPrefix;
import org.semanticweb.elk.owl.parsing.Owl2ParseException;
import org.semanticweb.elk.owl.parsing.Owl2Parser;
import org.semanticweb.elk.owl.parsing.Owl2ParserAxiomProcessor;
import org.semanticweb.elk.owl.parsing.javacc.Owl2FunctionalStyleParserFactory;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.elk.reasoner.ReasonerFactory;
import org.semanticweb.elk.reasoner.ReasoningTestManifest;
import org.semanticweb.elk.reasoner.TestReasonerUtils;
import org.semanticweb.elk.reasoner.stages.SimpleStageExecutor;
import org.semanticweb.elk.testing.PolySuite;
import org.semanticweb.elk.testing.TestInput;
import org.semanticweb.elk.testing.TestOutput;

/**
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
@RunWith(PolySuite.class)
public abstract class BaseIncrementalReasoningCorrectnessTest<EO extends TestOutput, AO extends TestOutput> {

	// logger for this class
	protected static final Logger LOGGER_ = Logger
			.getLogger(BaseIncrementalReasoningCorrectnessTest.class);

	final static int REPEAT_NUMBER = 5;
	final static double DELETE_RATIO = 0.2;

	protected final ReasoningTestManifest<EO, AO> manifest;
	protected List<ElkAxiom> axioms;

	public BaseIncrementalReasoningCorrectnessTest(
			ReasoningTestManifest<EO, AO> testManifest) {
		manifest = testManifest;
	}

	@Before
	public void before() throws IOException, Owl2ParseException {
		assumeTrue(!ignore(manifest.getInput()));

		InputStream stream = null;

		try {
			stream = manifest.getInput().getInputStream();
			axioms = loadAxioms(stream);
		} finally {
			IOUtils.closeQuietly(stream);
		}

	}

	@SuppressWarnings("static-method")
	protected boolean ignore(TestInput input) {
		return false;
	}

	/**
	 * The main test method
	 * 
	 * @throws ElkException
	 */
	@Test
	public void incrementalReasoning() throws ElkException {
		TestChangesLoader initialLoader = new TestChangesLoader();
		// TODO tweak TestChangesLoader to be able to use one loader for several
		// reasoners
		TestChangesLoader changeLoader1 = new TestChangesLoader();
		TestChangesLoader changeLoader2 = new TestChangesLoader();
		Reasoner standardReasoner = TestReasonerUtils.createTestReasoner(
				new SimpleStageExecutor(), 1);
		Reasoner incrementalReasoner = new ReasonerFactory().createReasoner();

		standardReasoner.registerOntologyLoader(initialLoader);
		standardReasoner.registerOntologyChangesLoader(changeLoader1);
		incrementalReasoner.registerOntologyLoader(initialLoader);
		incrementalReasoner.registerOntologyChangesLoader(changeLoader2);

		standardReasoner.setIncrementalMode(false);
		incrementalReasoner.setIncrementalMode(true);
		// initial load
		add(initialLoader, axioms);
		// initial correctness check
		correctnessCheck(standardReasoner, incrementalReasoner, -1);

		long seed = /*1353518711098L;*/System.currentTimeMillis();
		Random rnd = new Random(seed);

		for (int i = 0; i < REPEAT_NUMBER; i++) {
			// delete some axioms
			standardReasoner.setIncrementalMode(false);
			
			Set<ElkAxiom> deleted = getRandomSubset(axioms, rnd, DELETE_RATIO);

			/*for (ElkAxiom del : deleted) {
				System.err.println(OwlFunctionalStylePrinter.toString(del));
			}*/

			// incremental changes
			changeLoader1.clear();
			changeLoader2.clear();
			remove(changeLoader1, deleted);
			remove(changeLoader2, deleted);

			correctnessCheck(standardReasoner, incrementalReasoner, seed);
			
			standardReasoner.setIncrementalMode(false);
			// add the axioms back
			changeLoader1.clear();
			changeLoader2.clear();
			add(changeLoader1, deleted);
			add(changeLoader2, deleted);

			correctnessCheck(standardReasoner, incrementalReasoner, seed);
		}
	}

	private void add(TestChangesLoader loader, Collection<ElkAxiom> axiomList) {
		for (ElkAxiom axiom : axiomList) {
			loader.add(axiom);
		}
	}

	private void remove(TestChangesLoader loader, Collection<ElkAxiom> axiomList) {
		for (ElkAxiom axiom : axiomList) {
			loader.remove(axiom);
		}
	}

	protected Set<ElkAxiom> getRandomSubset(List<ElkAxiom> axioms, Random rnd,
			double fraction) {
		Collections.shuffle(axioms, rnd);

		Set<ElkAxiom> subset = new HashSet<ElkAxiom>();

		for (int i = 0; i < axioms.size()
				&& subset.size() <= fraction * axioms.size(); i++) {
			ElkAxiom axiom = axioms.get(i);

			if (!filterAxiom(axiom)) {
				subset.add(axiom);
			}
		}

		return subset;
	}

	protected boolean filterAxiom(ElkAxiom axiom) {
		return !(axiom instanceof ElkClassAxiom);
	}

	protected List<ElkAxiom> loadAxioms(InputStream stream) throws IOException,
			Owl2ParseException {
		Owl2Parser parser = new Owl2FunctionalStyleParserFactory()
				.getParser(stream);
		final List<ElkAxiom> axioms = new ArrayList<ElkAxiom>();

		parser.accept(new Owl2ParserAxiomProcessor() {

			@Override
			public void visit(ElkPrefix elkPrefix) throws Owl2ParseException {
			}

			@Override
			public void visit(ElkAxiom elkAxiom) throws Owl2ParseException {
				axioms.add(elkAxiom);
			}
		});

		return axioms;
	}

	protected abstract void correctnessCheck(Reasoner standardReasoner,
			Reasoner incrementalReasoner, long seed) throws ElkException;
}