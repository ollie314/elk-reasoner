/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.reasoner;

import org.semanticweb.elk.testing.TestOutput;

/**
 * A test delegate for tests with output.
 * 
 * @author Peter Skocovsky
 *
 * @param <AO>
 *            The type of actual test output.
 */
public interface ReasoningTestWithOutputDelegate<AO extends TestOutput>
		extends ReasoningTestDelegate<AO> {

	/**
	 * Called at the beginning of the test with output.
	 * 
	 * @throws Exception
	 */
	void initWithOutput() throws Exception;

}
