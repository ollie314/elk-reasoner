/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
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

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.elk.syntax.ElkObjectIntersectionOf;

/**
 * Represents all occurrences of an ElkObjectIntersectionOf in an ontology.
 * 
 * @author Frantisek Simancik
 *
 */
public class IndexedObjectIntersectionOf extends IndexedClassExpression {
	
	protected final ElkObjectIntersectionOf elkObjectIntersectionOf;
	
	protected List<IndexedClassExpression> conjuncts;
	
	
	/**
	 * Creates an object that represents the given ElkObjectIntersecionOf. 
	 */
	protected IndexedObjectIntersectionOf(ElkObjectIntersectionOf elkObjectIntersectionOf) {
		this.elkObjectIntersectionOf = elkObjectIntersectionOf;
	}
	

	@Override
	public ElkObjectIntersectionOf getClassExpression() {
		return elkObjectIntersectionOf;
	}
	
	
	/**
	 * @return The indexed class expressions that are the conjuncts of this object intersection. 
	 */
	public List<IndexedClassExpression> getConjuncts() {
		return conjuncts;
	}


	protected void addConjunct(IndexedClassExpression conjunct) {
		if (conjuncts == null)
			conjuncts = new ArrayList<IndexedClassExpression> (2);
		conjuncts.add(conjunct); 
	}
	
	
	
	
	
	@Override
	public <O> O accept(IndexedClassExpressionVisitor<O> visitor) {
		return visitor.visit(this);
	}


}