/**
 * 
 */
package org.semanticweb.elk.reasoner.tracing.factories;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.reasoner.ReasonerJob;
import org.semanticweb.elk.reasoner.indexing.model.IndexedContextRoot;
import org.semanticweb.elk.reasoner.saturation.inferences.ClassInference;

/**
 * A job for computing applied {@link ClassInference}s with the given
 * {@link IndexedContextRoot} origin. Intended to be used with
 * {@link ContextTracingFactory}.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 * @author "Yevgeny Kazakov"
 */
public class ContextTracingJob<R extends IndexedContextRoot> extends
		ReasonerJob<R, Iterable<? extends ClassInference>> {

	public ContextTracingJob(R input) {
		super(input);
	}

	@Override
	public String toString() {
		return getInput().toString() + " [class inference tracing]";

	}

	@Override
	protected void setOutput(Iterable<? extends ClassInference> output) {
		super.setOutput(output);
	}

}
