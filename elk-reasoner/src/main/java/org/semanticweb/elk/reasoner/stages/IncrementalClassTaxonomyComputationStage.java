/**
 * 
 */
package org.semanticweb.elk.reasoner.stages;

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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.reasoner.incremental.IncrementalStages;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClass;
import org.semanticweb.elk.reasoner.taxonomy.ClassTaxonomyComputation;
import org.semanticweb.elk.reasoner.taxonomy.model.UpdateableNode;
import org.semanticweb.elk.util.collections.Operations;
import org.semanticweb.elk.util.collections.Operations.Condition;

/**
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
class IncrementalClassTaxonomyComputationStage extends
		ClassTaxonomyComputationStage {

	private static final Logger LOGGER_ = Logger
			.getLogger(IncrementalClassTaxonomyComputationStage.class);

	public IncrementalClassTaxonomyComputationStage(
			AbstractReasonerState reasoner) {
		super(reasoner);
	}

	@Override
	public String getName() {
		return IncrementalStages.TAXONOMY_CONSTRUCTION.toString();
	}

	@Override
	public List<ReasonerStage> getDependencies() {
		return Arrays
				.asList((ReasonerStage) new IncrementalTaxonomyCleaningStage(reasoner));
	}

	@Override
	void initComputation() {
		super.initComputation();
		if (LOGGER_.isInfoEnabled())
			LOGGER_.info(getName() + " using " + workerNo + " workers");
		// TODO: at some point we need to clear non-saturated contexts when everything is fine
		// currently this is cleaned during the taxonomy cleaning stage, but this stage might not
		// be executed at all; also, the non saturated contexts are not cleaned at all during
		// incremental consistency checking. Something needs to be done about it.
		reasoner.saturationState.getWriter().clearNotSaturatedContexts();

		final Collection<IndexedClass> indexedClasses = reasoner.ontologyIndex
				.getIndexedClasses();
		
		if (!reasoner.useIncrementalTaxonomy()) {
			reasoner.taxonomy = null;
		}
		
		if (reasoner.taxonomy == null) {
			
			if (LOGGER_.isInfoEnabled())
				LOGGER_.info("Using non-incremental taxonomy");			
			
			computation_ = new ClassTaxonomyComputation(indexedClasses,
					reasoner.getProcessExecutor(), workerNo, progressMonitor,
					reasoner.ontologyIndex);
		} else {
			
			Collection<IndexedClass> modified = new AbstractSet<IndexedClass>() {

				@Override
				public Iterator<IndexedClass> iterator() {
					return Operations.filter(
							reasoner.ontologyIndex.getIndexedClasses(),
							new Condition<IndexedClass>() {

								@Override
								public boolean holds(IndexedClass clazz) {
									UpdateableNode<ElkClass> node = reasoner.taxonomy
											.getUpdateableNode(clazz
													.getElkClass());

									return node == null || node.isModified();
								}
							}).iterator();
				}

				@Override
				public int size() {
					// TODO: this is only an upper bound; calculate exactly
					return indexedClasses.size();
				}

			};

			
			//System.out.println("Classes to be updated in the taxonomy: " + modified);

			computation_ = new ClassTaxonomyComputation(modified,
					reasoner.getProcessExecutor(), workerNo, progressMonitor,
					reasoner.ontologyIndex, reasoner.taxonomy);
		}

	}
}