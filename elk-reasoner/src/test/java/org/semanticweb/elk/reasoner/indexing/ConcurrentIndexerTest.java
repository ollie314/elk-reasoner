/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Oxford University Computing Laboratory
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
package org.semanticweb.elk.reasoner.indexing;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.semanticweb.elk.syntax.ElkClass;
import org.semanticweb.elk.syntax.ElkClassExpression;
import org.semanticweb.elk.syntax.ElkObjectProperty;
import org.semanticweb.elk.syntax.FutureElkObjectFactory;
import org.semanticweb.elk.syntax.FutureElkObjectFactoryImpl;

public class ConcurrentIndexerTest extends TestCase {

	final FutureElkObjectFactory constructor = new FutureElkObjectFactoryImpl();

	public ConcurrentIndexerTest(String testName) {
		super(testName);
	}

	public void testIndexer() throws InterruptedException, ExecutionException {
		Future<ElkClass> human = constructor.getFutureElkClass("Human");
		Future<ElkObjectProperty> has = constructor
				.getFutureElkObjectProperty("has");
		Future<ElkClass> heart = constructor.getFutureElkClass("Heart");
		Future<ElkClass> organ = constructor.getFutureElkClass("Organ");
		Future<? extends ElkClassExpression> heartAndOrgan = constructor
				.getFutureElkObjectIntersectionOf(heart, organ);
		Future<? extends ElkClassExpression> hasHeartAndOrgan = constructor
				.getFutureElkObjectSomeValuesFrom(has, heartAndOrgan);

		final ExecutorService executor = Executors.newCachedThreadPool();		
		IndexingManager indexingManager = new IndexingManager(executor, 8);

		indexingManager.submit(constructor.getFutureElkSubClassOfAxiom(human,
				hasHeartAndOrgan));
		indexingManager.waitCompletion();
		OntologyIndex ontologyIndex = indexingManager.computeOntologyIndex();

		assertTrue(((IndexedObjectIntersectionOf) ontologyIndex
				.getIndexedClassExpression(heartAndOrgan.get())).conjuncts
				.contains(ontologyIndex.getIndexedClassExpression(heart.get())));
		assertTrue(((IndexedObjectIntersectionOf) ontologyIndex
				.getIndexedClassExpression(heartAndOrgan.get())).conjuncts
				.contains(ontologyIndex.getIndexedClassExpression(organ.get())));
		assertTrue(ontologyIndex.getIndexedClassExpression(human.get()).superClassExpressions
				.contains(ontologyIndex.getIndexedClassExpression(hasHeartAndOrgan.get())));
		assertTrue(ontologyIndex.getIndexedClassExpression(heart.get()).negConjunctionsByConjunct == null);

		indexingManager.submit(constructor.getFutureElkEquivalentClassesAxiom(
				human, hasHeartAndOrgan));
		indexingManager.waitCompletion();

		assertTrue(((IndexedObjectIntersectionOf) ontologyIndex
				.getIndexedClassExpression(heartAndOrgan.get())).conjuncts.size() == 2);
		assertFalse(ontologyIndex.getIndexedClassExpression(heart.get()).negConjunctionsByConjunct
				.isEmpty());
		assertNotSame(ontologyIndex.getIndexedClassExpression(human.get()),
				ontologyIndex.getIndexedClassExpression(hasHeartAndOrgan.get()));

		executor.shutdown();
	}

}