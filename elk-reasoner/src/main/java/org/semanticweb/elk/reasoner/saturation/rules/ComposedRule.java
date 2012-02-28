/*
 * #%L
 * ELK Reasoner
 * 
 * $Id$
 * $HeadURL$
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
package org.semanticweb.elk.reasoner.saturation.rules;

import org.semanticweb.elk.reasoner.saturation.expressions.Queueable;

public class ComposedRule<T extends Queueable> extends UnaryRule<T> {
	final protected UnaryRule<? super T> rule1;
	final protected UnaryRule<? super T> rule2;
	
	public ComposedRule(UnaryRule<? super T> rule1, UnaryRule<? super T> rule2) {
		super(null);
		this.rule1 = rule1;
		this.rule2 = rule2;
	}

	public void apply(T argument, Context context) {
		rule1.apply(argument, context);
		rule2.apply(argument, context);
	}

}
